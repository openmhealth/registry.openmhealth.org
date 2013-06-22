/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.User;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tautua.markdownpapers.parser.ParseException;
import play.cache.Cache;
import play.data.validation.Error;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.mvc.Controller;
import play.vfs.VirtualFile;

/**
 * @author maku
 */
public abstract class AbstractDomController extends Controller {

  protected static String USER_ID = "USER_ID";

  protected static boolean signedIn() {
    return session.get(USER_ID) != null;
  }

  protected static User getUser() {
    User user = User.findById(Long.parseLong(session.get(USER_ID)));
    return user;
  }

  protected static void renderPage(String pageName) throws FileNotFoundException, IOException {
    renderHtml(parsePage(pageName));
  }

  protected static Document parsePage(String pageName) throws FileNotFoundException, IOException {
    Document dom = parseTemplate("Application/base.html");
    String id = session.get(USER_ID);
    if (id != null) {
      dom.select(".signin").remove();
      dom.select(".signout").addClass("user-" + id);
    } else {
      dom.select(".signout").remove();
    }
    dom.select("title").html(pageName + ": Open mHealth Schema Registry");
    dom.select("#content").html(parseTemplate().toString());
    return dom;
  }

  protected static boolean isCached(String templateName) {
    return Cache.get(templateName) != null;
  }

  protected static long indexFileMod() {
    VirtualFile vf = VirtualFile
            .fromRelativePath("app/views/Application/index.html");
    File f = vf.getRealFile();
    return f.lastModified();
  }

  protected static Document getDom(String include, String exclude) {
    Document dom = parseTemplate("Application/index.html");
    if (include != null) {
      for (Element elm : dom.select(".section")) {
        if (!include.equals(elm.attr("id"))) {
          elm.remove();
        }
      }
    }
    if (exclude != null) {
      dom.select("#" + exclude).remove();
    }
    return dom;
  }

  public static Object getSectionOld(String section) {
    boolean parse = true;
    if (session.get(USER_ID) != null) {
      parse = true;
    } else if (isCached(section) && isCached("indexMod")) {
      Long time = (Long) Cache.get("indexMod");
      if (indexFileMod() < time) {
        parse = false;
        return Cache.get(section);
      }
    }
    if (parse) {
      Document dom = getDom(section, null);
      Cache.set(section, dom.toString());
      Cache.set("indexMod", System.currentTimeMillis());
      dom.select("body").removeClass("cached");
      userHeader(dom);
      return dom;
    }
    return null;
  }

  public static Document getSection(String section) {
    Document dom = getDom(section, null);
    Cache.set(section, dom.toString());
    Cache.set("indexMod", System.currentTimeMillis());
    dom.select("body").removeClass("cached");
    userHeader(dom);
    removeComments(dom);
    return dom;
  }

  protected static void userHeader(Document dom) {
    if (session.get(USER_ID) != null) {
      User user = User.findById(
              Long.parseLong(session.get(USER_ID)));
      if (user != null) {
        dom.select(".signIn").first().remove();
        dom.select(".signOut a.username").first()
                .text(user.firstName + " " + user.lastName)
                .attr("href", "/person/" + user.id);
      } else {
        session.remove(USER_ID);
        dom.select(".welcome.signOut").remove();
      }
    } else {
      dom.select(".welcome.signOut").remove();
    }
  }

  public static Document getSectionDom(String section) {
    Object obj = getSection(section);
    if (obj instanceof Document) {
      return (Document) obj;
    } else {
      return Jsoup.parse(obj.toString());
    }
  }

  protected static String staticHtml(String templateName)
          throws FileNotFoundException, IOException {
    StringBuilder htmlContent = new StringBuilder();
    VirtualFile vf = VirtualFile.fromRelativePath("app/views/" + templateName);
    File realFile = vf.getRealFile();
    BufferedReader br = new BufferedReader(new FileReader(realFile));
    char[] buf = new char[1024];
    int numRead = 0;
    while ((numRead = br.read(buf)) != -1) {
      String readData = String.valueOf(buf, 0, numRead);
      htmlContent.append(readData);
      buf = new char[1024];
    }
    br.close();
    return htmlContent.toString();
  }

  protected static org.jsoup.nodes.Document parseTemplate() {
    try {
      return Jsoup.parse(staticHtml(template()));


    } catch (FileNotFoundException ex) {
      Logger.getLogger(AbstractDomController.class
              .getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(AbstractDomController.class
              .getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  protected static void bindElement(Element e, Model model) {
    try {
      _bindElement(e, model);
    } catch (java.text.ParseException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ParseException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected static void _bindElement(Element e, Model model)
          throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException, java.text.ParseException {
    Elements elms = e.select("[data-name]");
    for (Element elm : elms) {
      String name = elm.attr("data-name");
      if (name != null) {
        String[] sa = name.split("\\.");
        if (sa.length > 1) {
          name = sa[1];
          Field field = model.getClass().getField(name);
          if (field != null) {
            Object val = field.get(model);
            if (val != null) {
              if (elm.tagName().equals("img")) {
                Blob b = (Blob) val;
                if (elm.hasClass("thumbnail128")) {
                  elm.attr("src", "/thumbnail/128/" + b.getUUID());
                } else if (elm.hasClass("thumbnail50")) {
                  elm.attr("src", "/thumbnail/50/" + b.getUUID());
                } else {
                  elm.attr("src", "/attachment/" + b.getUUID());
                }
              } else if (elm.hasClass("markdown")) {
                val = markdown.Markdown.transformMarkdown(val.toString());
                elm.html(val.toString());
              } else {
                elm.text(val.toString());
              }
            }
          }
        }
      }
    }
  }

  protected static void bindForm(Element e, Model model) {
    try {
      _bindForm(e, model);
    } catch (ParseException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected static void _bindForm(Element e, Model model)
          throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException {
    Elements elms = e.select("[name]");
    for (Element elm : elms) {
      String name = elm.attr("name");
      if (name != null) {
        String[] sa = name.split("\\.");
        if (sa.length > 1) {
          name = sa[1];
          Field field = model.getClass().getField(name);
          if (field != null) {
            Object val = field.get(model);
            if (val != null) {
              if (elm.tagName().equals("input")
                      || elm.tagName().equals("textarea")) {
                elm.val(val.toString());
              } else {
                elm.text(val.toString());
              }
            }
          }
        }
      }
    }
  }

  protected static void bindList(Element list, List obj) {
    try {
      _bindList(list, obj);
    } catch (ParseException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected static void _bindList(Element list, List objs)
          throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException {
    Element liTemplate = list.select("li").first().clone();
    list.html("");
    for (Object obj : objs) {
      Element li = liTemplate.clone();
      list.appendChild(li);
      for (Element elm : li.children()) {
        _bind(elm, obj);
      }
    }
  }

  protected static void bind(Element el, Object obj) {
    try {
      _bind(el, obj);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void _bind(Element el, Object obj)
          throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
    if (el.hasAttr("data-name")) {
      Field field = field(el.attr("data-name"), obj.getClass());
      if (field != null) {
        el.text(field.get(obj).toString());
      }
    } else if (el.hasClass("idlink")) {
      Field field = obj.getClass().getField("id");
      if (field != null) {
        el.attr("href", el.attr("href") + field.get(obj));
      } else {
        el.attr("href", el.attr("href") + "no ID");
      }
    } else if (el.tagName().equalsIgnoreCase("img")) {
      if (el.attr("data-img") != null) {
        Field field = field(el.attr("data-img"), obj.getClass());
        if (field != null) {
          Blob b = (Blob) field.get(obj);
          if (el.hasClass("thumb")) {
            String w = el.attr("width");
            String h = el.attr("height");
            el.attr("src", "/thumbnail/"
                    + w + "x" + h
                    + "/" + b.getUUID());
          } else {
            el.attr("src", "/attachment/" + b.getUUID());
          }
        }
      }
    }
    //
    if (el.children().size() > 0) {
      for (Element c : el.children()) {
        _bind(c, obj);
      }
    }
  }

  public static long userID() {
    return Long.parseLong(session.get(USER_ID));
  }

  private static Field field(String fullName, Class clazz) throws NoSuchFieldException {
    if (fullName == null) {
      return null;
    }
    String[] sa = fullName.split("\\.");
    if (sa.length > 1) {
      return clazz.getField(sa[1]);
    }
    return null;
  }

  protected static void bindErrors(Element e, Model model) {
    try {
      _bindErrors(e, model);
    } catch (ParseException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    } catch (NoSuchFieldException ex) {
      Logger.getLogger(AbstractDomController.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  protected static void _bindErrors(Element e, Model model)
          throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, ParseException {
    Elements elms = e.select("[name]");
    Map<String, List<Error>> errors = validation.errorsMap();
    System.out.println("errMap =" + errors);
    for (Element elm : elms) {
      String name = elm.attr("name");
      if (name != null) {
        List<Error> errList = errors.get(name);
        if (errList != null) {
          for (Error err : errList) {
            elm.val("");
            elm.attr("placeholder", err.message());
            elm.addClass("error");
          }
        }
      }
    }
  }

  protected static org.jsoup.nodes.Document parseTemplate(String templateName) {
    try {
      Document dom = Jsoup.parse(staticHtml(templateName));
      //Document menu = Jsoup.parse(staticHtml("Application/mainMenu.html"));
      //dom.select(".nav-collapse").first().html("").appendChild(menu.body());
      return dom;
    } catch (FileNotFoundException ex) {
      Logger.getLogger(AbstractDomController.class
              .getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(AbstractDomController.class
              .getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  protected static org.jsoup.nodes.Document parseDOM(String html) {
    return Jsoup.parse(html);
  }

  private static void removeComments(Document dom) {
  }
}
