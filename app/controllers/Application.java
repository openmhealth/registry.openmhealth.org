package controllers;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.DPU;
import models.DSU;
import models.DVU;
import models.Skeema;
import models.Star;
import models.User;
import org.jsoup.nodes.Document;
import play.libs.OpenID;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.cache.Cache;
import play.db.jpa.GenericModel;
import play.db.jpa.GenericModel.JPAQuery;
import play.db.jpa.JPABase;
import play.db.jpa.Model;
import play.vfs.VirtualFile;
import utils.TemplateEngine;

public class Application extends Templator {
  
  private static final TemplateEngine T =
          new TemplateEngine(Application.class, "index.html");
  
  public static void debug() throws FileNotFoundException, IOException {
    VirtualFile vf = VirtualFile.fromRelativePath(
            "app/views/Application/index.html");
    renderBinary(vf.getRealFile());
  }
  
  private static List truncate(List input) {
    return input;
  }
  
  private static List truncateX(List input) {
    int length = input.size() < 7 ? input.size() : 7;
    ArrayList output = new ArrayList();
    for (int i = 0; i < length; i++) {
      output.add(input.get(i));
    }
    return output;
  }
  
  private static void tombstone() {
    Document dom = getTemplate("tombstone");
    dom.select(".welcome, .nav, .search").remove();
    renderHtml(dom);
  }
  
  public static void index() {
    if (request.domain.equalsIgnoreCase("registry.openmhealth.org")) {
      tombstone();
      return;
    }
    
    Document dom = getTemplate("index");
    // ---
    JPAQuery jpa = Skeema.find(
            "select s from Skeema s "
            + "order by s.id DESC");
    List<Skeema> skeemas = jpa.fetch();
    // ---
    // New Schemas --
    Elements ul = dom.select("#index .newestSchemas");
    bind(ul, truncate(skeemas));
    // ---
    // Stared Schemas --
    List<Star> stars = Star.find("byType", "schema").fetch();
    HashMap<Skeema, Long> starCount = new HashMap<Skeema, Long>();
    for (Skeema skeema : skeemas) {
      starCount.put(skeema, Star.count("nodeID = ?", skeema.id + ""));
    }
    List<Map.Entry> entries = new ArrayList<Map.Entry>(starCount.entrySet());
    Collections.sort(entries, new Comparator() {
      public int compare(Object o1, Object o2) {
        Map.Entry e1 = (Map.Entry) o1;
        Map.Entry e2 = (Map.Entry) o2;
        return ((Comparable) e2.getValue()).compareTo(e1.getValue());
      }
    });
    skeemas.clear();
    for (int i = 0; i < entries.size(); i++) {
      skeemas.add((Skeema) entries.get(i).getKey());
    }

    // ---
    // Starred Schemas --
    ul = dom.select("#index .staredSchemas");
    bind(ul, skeemas);
    // ---
    // DSUs --
    ul = dom.select("#index .dsus");
    bind(ul, truncate(DSU.findAll()));
    // ---
    // DPUs --
    ul = dom.select("#index .dpus");
    bind(ul, truncate(DPU.findAll()));
    // ---
    // DVUs --
    ul = dom.select("#index .dvus");
    bind(ul, truncate(DVU.findAll()));
    // ---
    // People --
    ul = dom.select("#index .people");
    bind(ul, truncate(User.findAll()));
    // ---
    // Keywords --
    Element kw = dom.select("#index .keywords").first();
    kw.html("");
    HashMap<String, Integer> keyWords = new HashMap<String, Integer>();
    addKeys(Skeema.findAll(), keyWords);
    addKeys(DSU.findAll(), keyWords);
    addKeys(DPU.findAll(), keyWords);
    addKeys(DVU.findAll(), keyWords);
    Set<String> keys = new TreeSet<String>(keyWords.keySet());
    for (String key : keys) {
      kw.appendElement("a").attr("href", "/search?q=" + key).
              text(key)
              //  text(key + " (" + keyWords.get(key) + ")")
              .appendElement("span").text(",");
      
    }
    renderHtml(dom);
  }
  
  private static void addKeys(List<JPABase> models, HashMap<String, Integer> keyWords) {
    try {
      for (JPABase model : models) {
        Field field = model.getClass().getField("keyWords");
        String k = (String) field.get(model);
        if (k != null) {
          StringTokenizer tok = new StringTokenizer(k);
          while (tok.hasMoreTokens()) {
            String word = tok.nextToken(",");
            if (!keyWords.containsKey(word)) {
              keyWords.put(word, 1);
            } else {
              Integer val = keyWords.get(word);
              keyWords.put(word, val.intValue() + 1);
            }
          }
        }
      }
      
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
    } catch (SecurityException ex) {
      Logger.getLogger(Application.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  public static void about() {
    renderHtml(getTemplate("about"));
  }
  
  public static void units() {
    renderHtml(getTemplate("units"));
  }

  //--------------------------------------------------------------------------
  // Schemas
  //--------------------------------------------------------------------------
  public static void schema(Long id) {
    Skeema skeema = Skeema.findById(id);
    
    if (skeema == null) {
      send404();
    }
    Document dom = getTemplate("schema");
    bind(dom.select("#schema"), skeema);
    Element div = dom.select("#schema .maintainers").first();
    div.html("");
    for (User user : skeema.maintainers) {
      div.appendElement("a").attr("href", "/person/" + user.id)
              .text(user.firstName + " " + user.lastName);
      div.appendElement("span").text(", ");
    }
    Elements comma = div.select("span");
    if (comma != null) {
      comma.last().remove();
    }
    Element dsusElm = dom.select("#schema .dsus").first();
    List<DSU> dsus = DSU.findAll();
    //
    for (DSU dsu : dsus) {
      if (dsu.skeemas.contains(skeema)) {
        Element dsuElm = dsusElm.appendElement("div");
        dsuElm.addClass("dsu").appendElement("a").addClass("bold")
                .attr("href", "/DSU/" + dsu.id).text(dsu.name);
        //https://test.ohmage.org/app/omh/v1.0/registry/read?payload_id=omh:ohmage:observer:edu.ucla.cens.contextsens:accelerometer
        //
        dsuElm.appendElement("a").text("(test and view schema)")
                .addClass("test").attr("href", dsu.URL + "?payload_id="
                + skeema.skeemaID);
      }
    }
    
    List<Star> stars = Star.find("byNodeID", id.toString()).fetch();
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase("schema")) {
        dom.select(".stars").addClass("selected");
      }
    }
    renderHtml(dom);
    
  }
  
  public static void schemas() {
    List<Skeema> skeemas = Skeema.findAll();
    Document dom = getTemplate("schemas");
    bind(dom.select("#schemas ul"), skeemas);
    renderHtml(dom);
  }
  
  public static void editSchema(Long id) {
    Skeema skeema = Skeema.findById(id);
    Document dom = getTemplate("editSchema");
    if (skeema == null) {
      renderText("Schema was not found.");
    } else {
    }
    bind(dom.select("h2"), skeema);
    bind(dom.select("#editSchema form"), skeema);
    renderHtml(dom);
  }

  //--------------------------------------------------------------------------
  // PEOPLE PERSON AUTH
  //--------------------------------------------------------------------------
  public static void signIn(User user) {
    Document dom = getTemplate("signIn");
    if (request.method.equalsIgnoreCase("get")) {
      renderHtml(dom);
    } else if (request.method.equalsIgnoreCase("post")) {
      User fetchedUser = User.find("byEmail", user.email).first();
      System.out.println("fetchedUser = " + fetchedUser);
      if (fetchedUser == null) {
        dom.select("form.emailSignIn")
                .append("<span class='error'>This user doesn't exist.</span>");
        dom.select("form.emailSignIn .email").addClass("error")
                .attr("placeholder", user.email);
      } else {// check password
        if (fetchedUser.password.equals(user.password)) {
          session.put(USER_ID, fetchedUser.id);
          redirect("/");
        } else {
        }
      }
    }
    renderHtml(dom);
  }
  
  public static void signOut() {
    session.remove(USER_ID);
    redirect("/");
  }
  
  public static void person(Long id) {
    User user = User.findById(id);
    if (user == null) {
      send404();
    }
    Document dom = getTemplate("person");
    bind(dom.select("#person").not(".starred, .contributed"), user);

    // Contributed --
    // -- Skeemas 
    List<Skeema> allSkeemas = Skeema.findAll();
    ArrayList<Skeema> skeemas = new ArrayList<Skeema>();
    for (Skeema s : allSkeemas) {
      if (s.maintainers.contains(user)) {
        skeemas.add(s);
      }
    }
    bind(dom.select(".contributed .schemas ul"), skeemas);
    // -- DSU 
    List<DSU> allDSU = DSU.findAll();
    ArrayList<DSU> dsus = new ArrayList<DSU>();
    for (DSU dsu : allDSU) {
      if (dsu.maintainers.contains(user)) {
        dsus.add(dsu);
      }
    }
    bind(dom.select(".contributed .dsus ul"), dsus);
    // -- DPU 
    List<DPU> allDPU = DPU.findAll();
    ArrayList<DPU> dpus = new ArrayList<DPU>();
    for (DPU dpu : allDPU) {
      if (dpu.maintainers.contains(user)) {
        dpus.add(dpu);
      }
    }
    bind(dom.select(".contributed .dpus ul"), dpus);
    // -- DVU 
    List<DVU> allDVU = DVU.findAll();
    ArrayList<DVU> dvus = new ArrayList<DVU>();
    for (DVU dvu : allDVU) {
      if (dvu.maintainers.contains(user)) {
        dvus.add(dvu);
      }
    }
    bind(dom.select(".contributed .dvus ul"), dvus);
    // Starred --
    // -- Skeemas 

    skeemas.clear();
    dsus.clear();
    dpus.clear();
    dvus.clear();
    
    JPAQuery jpa = Star.find(
            "select s from Star s "
            + "where s.user = ?",
            user);
    List<Star> stars = jpa.fetch();
    System.out.println("stars " + stars);
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase("schema")) {
        skeemas.add((Skeema) Skeema.findById(new Long(star.nodeID)));
      } else if (star.type.equalsIgnoreCase("dsu")) {
        dsus.add((DSU) DSU.findById(new Long(star.nodeID)));
      } else if (star.type.equalsIgnoreCase("dpu")) {
        dpus.add((DPU) DPU.findById(new Long(star.nodeID)));
      } else if (star.type.equalsIgnoreCase("dvu")) {
        dvus.add((DVU) DVU.findById(new Long(star.nodeID)));
      }
    }
    System.out.println("skeemas: " + skeemas);
    System.out.println("elm: " + dom.select(".starred .schemas ul").size());
    bind(dom.select(".starred .schemas ul"), skeemas);
    bind(dom.select(".starred .dsus ul"), dsus);
    bind(dom.select(".starred .dpus ul"), dpus);
    bind(dom.select(".starred .dvus ul"), dvus);
    Elements ul = dom.select("ul");

    // -- Render
    renderHtml(dom);
  }
  
  public static void editPerson(Long id) {
    Document dom = getTemplate("editPerson");
    User user = User.findById(id);
    if (user == null) {
      renderText("This user ID was not found.");
    } else {
      if (user.openID != null && !user.openID.trim().isEmpty()) {
        dom.select("h2").html("Edit Account (Open ID)");
        dom.select(".changePassword").remove();
      } else {
        dom.select("h2").html("Edit Account");
      }
    }
    bind(dom.select("#editPerson"), user);
    renderHtml(dom);
  }
  
  public static void people() {
    List<User> users = User.findAll();
    Document dom = getTemplate("people");
    bind(dom.select("#people ul"), users);
    renderHtml(dom);
  }
  
  public static void createPerson() {
    renderHtml(getTemplate("createAccount"));
  }

  //--------------------------------------------------------------------------
  // UNITS
  //--------------------------------------------------------------------------
  public static void createDSU() {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("createDSU");
    renderHtml(dom);
  }
  
  public static void createDPU() {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("createDPU");
    renderHtml(dom);
  }
  
  public static void createDVU() {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("createDVU");
    renderHtml(dom);
  }
  
  public static void dsu(Long id) {
    DSU dsu = DSU.findById(id);
    if (dsu == null) {
      send404();
    }
    Document dom = getTemplate("dsu");
    bind(dom.select("#dsu"), dsu);
    Elements ul = dom.select("#dsu ul.user");
    ul.select("a.toHref").addClass("id");
    bind(ul, dsu.maintainers);
    ul = dom.select("#dsu ul.schemas");
    ul.select(" li a").addClass("id");
    bind(ul, dsu.skeemas);
    List<Star> stars = Star.find("byNodeID", id.toString()).fetch();
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase("dsu")) {
        dom.select(".stars").addClass("selected");
      }
    }
    renderHtml(dom);
  }
  
  public static void dpu(Long id) {
    DPU dpu = DPU.findById(id);
    if (dpu == null) {
      send404();
    }
    Document dom = getTemplate("dpu");
    bind(dom.select("#dpu"), dpu);
    Elements ul = dom.select("#dpu ul.user");
    ul.select("a.toHref").addClass("id");
    bind(ul, dpu.maintainers);
    
    ul = dom.select("#dpu ul.schemas");
    ul.select(" li a").addClass("id");
    bind(ul, dpu.skeemas);
    
    bindStars(dom, id);
    renderHtml(dom);
  }
  
  private static void bindStars(Document dom, Long id) {
    List<Star> stars = Star.find("byNodeID", id.toString()).fetch();
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase("dpu")) {
        dom.select(".stars").addClass("selected");
      }
    }
  }
  
  public static void dvu(Long id) {
    DVU dvu = DVU.findById(id);
    if (dvu == null) {
      send404();
    }
    Document dom = getTemplate("dvu");
    bind(dom.select("#dvu"), dvu);
    Elements ul = dom.select("#dvu ul.user");
    ul.select("a.toHref").addClass("id");
    bind(ul, dvu.maintainers);
    
    ul = dom.select("#dvu ul.schemas");
    ul.select(" li a").addClass("id");
    bind(ul, dvu.skeemas);
    
    List<Star> stars = Star.find("byNodeID", id.toString()).fetch();
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase("dvu")) {
        dom.select(".stars").addClass("selected");
      }
    }
    renderHtml(dom);
  }
  
  public static void dsus() {
    List<DSU> dsus = DSU.findAll();
    Document dom = getTemplate("dsus");
    bind(dom.select("#dsus"), dsus);
    JsonParser parser = new JsonParser();
    for (Element elm : dom.select("#dsus ul li")) {
      Element orgs = elm.select(".organizations").first();
      orgs.html("");
      Element lastComma = null;
      Element maint = elm.select(".maintainers").first();
      maint.remove();
      JsonArray array = parser.parse(new StringReader(maint.text())).getAsJsonArray();
      for (JsonElement obj : array) {
        JsonObject user = obj.getAsJsonObject();
        String org = user.get("organization").getAsString();
        if (!orgs.text().contains(org)) {
          Element div = orgs.appendElement("div");
          div.appendElement("span").text(org);
          lastComma = div.appendElement("span").text(", ");
        }
      }
      if (lastComma != null) {
        lastComma.remove();
      }
    }
    renderHtml(dom);
  }
  
  public static void dpus() {
    List<DPU> dpus = DPU.findAll();
    Document dom = getTemplate("dpus");
    bind(dom.select("#dpus"), dpus);
    bindMaintainers("#dpus", dom);
    renderHtml(dom);
  }
  
  public static void dvus() {
    List<DVU> dvus = DVU.findAll();
    Document dom = getTemplate("dvus");
    bind(dom.select("#dvus"), dvus);
    bindMaintainers("#dvus", dom);
    renderHtml(dom);
  }
  
  public static void editDSU(Long id) {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("editDsu");
    DSU dsu = DSU.findById(id);
    if (dsu == null) {
      send404();
    } else {
    }
    bind(dom.select("#editDsu"), dsu);
    editSchemaField(dom.select(".dsuSkeemas").first(), dsu.skeemas);
    renderHtml(dom);
  }
  
  private static void editSchemaField(Element textarea, List<Skeema> skeemas) {
    StringBuilder sb = new StringBuilder();
    for (Skeema skeema : skeemas) {
      sb.append(skeema.skeemaID.trim());
      sb.append(",\n");
    }
    textarea.val(sb.toString());
  }
  
  public static void editDPU(Long id) {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("editDpu");
    DPU dpu = DPU.findById(id);
    if (dpu == null) {
      send404();
    } else {
    }
    bind(dom.select("#editDpu"), dpu);
    renderHtml(dom);
  }
  
  public static void editDVU(Long id) {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    Document dom = getTemplate("editDvu");
    DPU dpu = DPU.findById(id);
    if (dpu == null) {
      send404();
    } else {
    }
    bind(dom.select("#editDvu"), dpu);
    renderHtml(dom);
  }
  
  private static void bindMaintainers(String section, Document dom) {
    JsonParser parser = new JsonParser();
    for (Element elm : dom.select(section + " ul li")) {
      Element orgs = elm.select(".organizations").first();
      orgs.html("");
      Element lastComma = null;
      Element maint = elm.select(".maintainers").first();
      maint.remove();
      JsonArray array = parser.parse(new StringReader(maint.text())).getAsJsonArray();
      for (JsonElement obj : array) {
        JsonObject user = obj.getAsJsonObject();
        String org = user.get("organization").getAsString();
        if (!orgs.text().contains(org)) {
          Element div = orgs.appendElement("div");
          div.appendElement("span").text(org);
          lastComma = div.appendElement("span").text(", ");
        }
      }
      if (lastComma != null) {
        lastComma.remove();
      }
    }
  }
  
  public static void uploadStatus() {
    if (getUserID() == null) {
      redirect("/sign-in");
    }
    if (request.method.equalsIgnoreCase("get")) {
      Document dom = getTemplate("schemaUploadStatus");
      renderHtml(dom);
    } else {
      String s = (String) Cache.get(getUserID() + "addSchema");
      if (s == null) {
        s = "The Schemas have been uploaded. <span='num'></span><span='total'></span>";
      }
      renderText(s);
    }
  }

  //--------------------------------------------------------------------------
  // OPEN ID
  //--------------------------------------------------------------------------
  public static void openID(String openIdURL) {
    System.out.println("authenticate: " + OpenID.isAuthenticationResponse());
    if (OpenID.isAuthenticationResponse()) {
      OpenID.UserInfo verifiedUser = OpenID.getVerifiedID();
      if (verifiedUser == null) {
        renderText("error", "Oops. Authentication has failed");
        signIn(null);
      } else {
        System.out.println("verifiedUser.id: " + verifiedUser.id);
        User user = User.find("byOpenID", verifiedUser.id).first();
        System.out.println("user : " + user);
        if (user == null) {
          user = new User();
          user.openID = verifiedUser.id;
          user.save();
          redirect("/person/edit/" + user.id);
        } else {
          session.put(USER_ID, user.id);
          redirect("/");
        }
      }
    } else {
      OpenID.id(openIdURL).verify(); // will redirect the user
    }
  }

  //--------------------------------------------------------------------------
  // STAR
  //--------------------------------------------------------------------------
  public static void star(String url, Boolean selected) {
    String[] sa = url.split("/");
    System.out.println(sa.length);
    String type = sa[sa.length - 2];
    String id = sa[sa.length - 1];
    List<Star> stars = Star.find("byNodeID", id).fetch();
    Star fetched = null;
    for (Star star : stars) {
      if (star.type.equalsIgnoreCase(type)) {
        fetched = star;
      }
    }
    if (fetched != null) {
      if (!selected) {
        fetched.delete();
      }
    } else {
      Star star = new Star();
      star.user = getUser();
      star.nodeID = id;
      star.type = type;
      star.save();
    }
  }
  
  private static void send404() {
    Document dom = getTemplate("error");
    renderHtml(dom);
  }
}
