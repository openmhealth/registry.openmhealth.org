package controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jobs.AddSchemas;
import models.DPU;
import models.DSU;
import models.DVU;
import models.Skeema;
import models.User;
import org.jsoup.nodes.Document;
import play.Play;
import play.data.validation.Valid;
import play.data.validation.Validation;
import play.db.jpa.JPABase;
import play.libs.F.Action;
import play.mvc.*;

public class UpdateController extends Templator {

  public static void createUser(@Valid User user) {
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      Document dom = getTemplate("createAccount");
      bind(dom.select("form.createEmailAccount"), user);
      if (user.password == null) {
        dom.select(".fakePassword, .fakePasswordRepeat").addClass("error");
      }
      dom.select("span.error").first().text("Please fix errors and try again.");
      renderHtml(dom);
    } else if (user.openID != null) { //openid
      user.save();
      session.put(USER_ID, user.id);
      redirect("/person/edit/" + user.id);
    } else { //email
      user.save();
      session.put(USER_ID, user.id);
      redirect("/person/" + user.id);
    }
  }

  public static void editUser(@Valid User user) {
    //User fetchedUser = getUser();
    User fetchedUser = User.findById(user.id);
    if (!user.id.equals(fetchedUser.id)) {
      if (user == null) {
        redirect("/sign-in");
      }
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect(params.get("refURL"));
    }
    fetchedUser.update(user);
    fetchedUser.save();
    user = fetchedUser;
    session.put(USER_ID, user.id);
    redirect("/person/" + user.id);
  }

  public static void deleteUser(String e, String p) {
    User fetchedUser = User.find("byEmail", e).first();
    if (fetchedUser != null) {
      fetchedUser.delete();
    }
    redirect("/");
  }

  public static void createSchema(@Valid Skeema skeema) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/schema/create");
    } else {
      skeema.save();
      redirect("/schema/" + skeema.id);
    }
  }

  public static void editSchema(@Valid Skeema skeema) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/schema/edit");
    } else {
      skeema.save();
      redirect("/schema/" + skeema.id);
    }
  }

  public static void deleteSchema(Long id) {
    redirect("/");
  }

  public static void deleteDPU(Long id) {
    redirect("/");
  }

  public static void deleteDSU(Long id) {
    redirect("/");
  }

  public static void deleteDVU(Long id) {
    redirect("/");
  }

  public static void createDSU(@Valid DSU dsu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    dsu.maintainers.add(user);
    String schemaIDs = params.get("skeemas");
    if (schemaIDs != null) {
      String[] sa = schemaIDs.split(",");
      AddSchemas addSchemas = new AddSchemas(dsu, user);
      for (String schemaID : sa) {
        addSchemas.addSchema(schemaID, user);
      }
      addSchemas.now();
      redirect("/uploadStatus");
    }
  }

  public static void createDPU(@Valid DPU dpu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/DPU/edit");
    } else {
      dpu.maintainers.add(user);
      dpu.skeemas = includeSchemas(params.get("skeemas"));
      dpu.save();
      redirect("/DPU/" + dpu.id);
    }
    dpu.maintainers.add(user);

  }

  public static void createDVU(@Valid DVU dvu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/DVU/edit");
    } else {
      dvu.maintainers.add(user);
      dvu.skeemas = includeSchemas(params.get("skeemas"));
      dvu.save();
      redirect("/DVU/" + dvu.id);
    }
    dvu.maintainers.add(user);
  }

  public static void editDSU(@Valid DSU dsu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/schema/edit");
    } else {
      DSU fetchedDSU = DSU.findById(dsu.id);
      if (fetchedDSU != null) {
        fetchedDSU.update(dsu);
        fetchedDSU.skeemas = includeSchemas(params.get("skeemas"));
        fetchedDSU.save();
      }
      redirect("/DSU/" + fetchedDSU.id);
    }
  }

  public static void editDPU(@Valid DPU dpu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/schema/edit");
    } else {
      DPU fetchedDPU = DPU.findById(dpu.id);
      if (fetchedDPU != null) {
        fetchedDPU.update(dpu);
        fetchedDPU.skeemas = includeSchemas(params.get("skeemas"));
        fetchedDPU.save();
      }
      redirect("/DPU/" + fetchedDPU.id);
    }
  }

  public static void editDVU(@Valid DVU dvu) {
    User user = getUser();
    if (user == null) {
      redirect("/sign-in");
    }
    if (Validation.hasErrors()) {
      params.flash();
      Validation.keep();
      redirect("/schema/edit");
    } else {
      DVU fetchedDVU = DVU.findById(dvu.id);
      if (fetchedDVU != null) {
        fetchedDVU.update(dvu);
        fetchedDVU.skeemas = includeSchemas(params.get("skeemas"));
        fetchedDVU.save();
      }
      redirect("/DVU/" + fetchedDVU.id);
    }
  }

  private static ArrayList<Skeema> includeSchemas(String schemaIDs) {
    List<Skeema> allSkeemas = Skeema.findAll();
    ArrayList<Skeema> skeemas = new ArrayList<Skeema>();
    if (schemaIDs != null) {
      String[] sa = schemaIDs.split(",");
      for (String schemaID : sa) {
        Skeema s = contains(allSkeemas, schemaID);
        if (s != null) {
          skeemas.add(s);
        }
      }
    }
    return skeemas;
  }

  private static Skeema contains(List<Skeema> skeemas, String skeemaID) {
    for (Skeema s : skeemas) {
      if (s.skeemaID.trim().equals(skeemaID.trim())) {
        return s;
      }
    }
    return null;
  }
}
