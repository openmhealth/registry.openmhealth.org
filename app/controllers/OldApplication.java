package controllers;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import models.DSU;
import models.Skeema;
import models.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import play.Play;
import play.libs.OpenID;
import play.mvc.*;
import net.coobird.thumbnailator.*;
import net.coobird.thumbnailator.geometry.Positions;
import org.jsoup.nodes.Element;
import play.modules.search.Query;
import play.modules.search.Search;
import utils.TemplateEngine;

;

public class OldApplication extends AbstractDomController {

  private static final TemplateEngine T =
          new TemplateEngine(OldApplication.class, "index.html");

  public static void debug() throws FileNotFoundException, IOException {
    renderHtml(staticHtml("Application/index.html"));
  }

  public static void index() {
    renderHtml(getSection("index"));
  }

  public static void about() {
    renderHtml(getSection("about"));
  }

  public static void units() {
    renderHtml(getSection("units"));
  }
  //--------------------------------------------------------------------------
  // Search
  //--------------------------------------------------------------------------

  public static void search(String q) {
    Document dom = getSection("searchResults");
    dom.select("[name=q]").val(q);
    searchUsers(q, dom);
    searchSchemas(q, dom);
    searchUnits(q, dom);
    Element noResults = dom.select(".noResults").first();
    if (noResults != null) {
      noResults.text(noResults.text() + " " + q);
    }
    renderHtml(dom);
  }

  private static void searchUsers(String q, Document dom) {
    Query query = Search.search(
            "firstName:" + quote(q)
            + " OR lastName:" + quote(q)
            + " OR organization:" + quote(q)
            + " OR description:" + quote(q),
            User.class);
    List<User> users = query.fetch();
    if (users.size() > 0) {
      bindList(dom.select("#searchResults .authors ul").first(), users);
      dom.select(".noResults").remove();
    } else {
      dom.select("#searchResults .authors").remove();
    }
  }

  private static void searchSchemas(String q, Document dom) {
    Query query = Search.search(
            "skeemaID:" + quote(q)
            + " OR description:" + quote(q)
            + " OR keyWords:" + quote(q),
            Skeema.class)
            .page(0, 100000);
    List<Skeema> skeemas = query.fetch();
    if (skeemas.size() > 0) {
      bindList(dom.select("#searchResults .schemas ul").first(), skeemas);
      dom.select(".noResults").remove();
    } else {
      dom.select("#searchResults .schemas").remove();
    }
  }

  private static void searchUnits(String q, Document dom) {
    dom.select("#searchResults .units").remove();
  }

  private static String quote(String s) {
    return "\"" + s + "\"";
  }

  //--------------------------------------------------------------------------
  // Schemas
  //--------------------------------------------------------------------------
  public static void schema(Long id) {
    Skeema skeema = Skeema.findById(id);
    Document dom = getSectionDom("schema");
    bindElement(dom.select("#schema").first(), skeema);
    dom.select("a.edit").attr("href", "/schema/edit/" + skeema.id);
    Element div = dom.select("#schema .maintainers").first();
    div.html("");
    for (User user : skeema.maintainers) {
      div.appendElement("a").attr("href", "/author/" + user.id)
              .text(user.firstName + " " + user.lastName);
      div.appendElement("span").text(", ");
    }
    div.select("span").last().remove();

    renderHtml(dom);
  }

  public static void schemas() {
    List<Skeema> skeemas = Skeema.findAll();
    Document dom = getSection("schemas");
    bindList(dom.select("#schemas ul").first(), skeemas);
    renderHtml(dom);
  }

  public static void editSchema(Long id) {
    Skeema skeema = Skeema.findById(id);
    Document dom = getSectionDom("editSchema");
    if (skeema == null) {
      renderText("Schema was not found.");
    } else {
    }
    bindForm(dom.select("#editSchema form").first(), skeema);
    bindErrors(dom.select("#editSchema form").first(), skeema);
    renderHtml(dom);
  }

  //--------------------------------------------------------------------------
  // Authors
  //--------------------------------------------------------------------------
  public static void person(Long id) {
    Document t = T.getTemplate("person");
    renderHtml(t);

    /*
     User user = User.findById(id);
     Document dom = getSectionDom("author");
     bindElement(dom.select("#author").first(), user);
     dom.select("a.edit").attr("href", "/author/edit/" + user.id);
     renderHtml(dom);
     */
  }

  public static void people() {
    Document dom = getSection("authors");
    List<User> users = User.findAll();
    bindList(dom.select("#authors ul").first(), users);
    renderHtml(dom);
  }
  //--------------------------------------------------------------------------
  // UNITS
  //--------------------------------------------------------------------------

  public static void createDSU() {
    Document dom = getSectionDom("createDSU");
    renderHtml(dom);
  }

  public static void dsu(Long id) {
    DSU dsu = DSU.findById(id);
    Document dom = getSectionDom("dsu");
    bind(dom.select("#dsu").first(), dsu);
    bindList(dom.select("#dsu ul.schemas").first(), dsu.skeemas);
    renderHtml(dom);
  }

  //--------------------------------------------------------------------------
  // File Utils
  //--------------------------------------------------------------------------
  public static void attachment(String uuid) {
    renderBinary(new File(Play.applicationPath, "/data/attachments/" + uuid));
  }

  public static void thumbnail(Integer w, Integer h, String uuid) throws IOException {
    File dst = new File(Play.applicationPath, "/data/thumbnails/"
            + w + "x" + h + "/" + uuid + ".png");
    if (!dst.exists()) {
      if (!dst.getParentFile().exists()) {
        dst.getParentFile().mkdirs();
      }
      File src = new File(Play.applicationPath, "/data/attachments/" + uuid);
      Thumbnails.of(src)
              .crop(Positions.CENTER)
              .size(w, h)
              .outputFormat("png")
              .toFile(dst);
    }
    renderBinary(dst);
  }

  public static void createAccount() {
    renderHtml(getSection("createAccount"));
  }

  public static void editAccount(Long id) {
    User user = User.findById(id);
    Document dom = getSectionDom("editAccount");
    if (user == null) {
      renderText("This user ID was not found.");
    } else {
      if (user.openID != null) {
        dom.select("h2").html("Edit Account (Open ID)");
        dom.select(".changePassword").remove();
      } else {
      }
    }
    bindForm(dom.select("#editAccount form").first(), user);
    bindErrors(dom.select("#editAccount form").first(), user);
    renderHtml(dom);
  }

  public static void openID(String openIdURL) {
    System.out.println("authenticate: " + OpenID.isAuthenticationResponse());
    if (OpenID.isAuthenticationResponse()) {
      OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
      if (verifiedUser == null) {
        renderText("error", "Oops. Authentication has failed");
        // signIn();
      } else {
        System.out.println("verifiedUser.id: " + verifiedUser.id);
        User user = User.find("byOpenID", verifiedUser.id).first();
        System.out.println("user : " + user);
        if (user == null) {
          user = new User();
          user.openID = verifiedUser.id;
          user.save();
          redirect("/author/edit/" + user.id);
        } else {
          session.put(USER_ID, user.id);
          redirect("/");
        }
      }
    } else {
      OpenID.id(openIdURL).verify(); // will redirect the user
    }
  }

  public static void signIn(User user) {
    if (request.method.equalsIgnoreCase("get")) {
      renderHtml(getSection("signIn"));
    } else if (request.method.equals("post")) {
      User fetchedUser = User.find("byEmail", user.email).first();
    }
  }

  public static void signOut() {
    session.remove(USER_ID);
    redirect("/");
  }
}
