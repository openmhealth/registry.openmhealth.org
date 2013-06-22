/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.User;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.Play;
import play.db.jpa.Blob;
import play.mvc.Controller;
import play.vfs.VirtualFile;
import utils.TemplateEngine;

/**
 *
 * @author maku
 */
public class Templator extends Controller {

  protected static String USER_ID = "USER_ID";
  private static HashMap<String, Document> templates = new HashMap<String, Document>();
  private static long lastMod = 0;

  protected static Document getTemplate(String sectionName) {
    try {
      return _getTemplate(sectionName);
    } catch (IOException ex) {
      Logger.getLogger(Templator.class.getName()).log(Level.SEVERE, null, ex);
    }
    return null;
  }

  protected static String getUserID() {
    return session.get(USER_ID);
  }

  protected static User getUser() {
    User user = null;
    if (session.get(USER_ID) != null
            && isNumeric(session.get(USER_ID))) {
      user = User.findById(
              Long.parseLong(session.get(USER_ID)));
    }
    return user;
  }

  protected static Document _getTemplate(String sectionName) throws IOException {
    Document doc = null;
    VirtualFile vf = VirtualFile.fromRelativePath(
            "app/views/Application/index.html");
    File templateFile = vf.getRealFile();
    if (templateFile.lastModified() > lastMod) {
      doc = Jsoup.parse(templateFile, "UTF-8");
      templates.put("wholeApp", doc.clone());
      doc.select(".section").not("#" + sectionName).remove();
      templates.put(sectionName, doc.clone());
    } else {
      if (templates.containsKey(sectionName)) {
        doc = templates.get(sectionName);
      }
    }
    return userHeader(doc.clone());
  }

  protected static Document userHeader(Document dom) {
    if (session.get(USER_ID) != null && isNumeric(session.get(USER_ID))) {
      User user = User.findById(
              Long.parseLong(session.get(USER_ID)));
      if (user != null) {
        dom.select(".signIn").first().remove();
        dom.select(".signOut a.username").first()
                .text(user.firstName + " " + user.lastName)
                .attr("href", "/author/" + user.id);
      } else {
        session.remove(USER_ID);
        dom.select(".welcome.signOut").remove();
      }
    } else {
      dom.select(".welcome.signOut").remove();
    }
    return dom;
  }

  public static boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }

  protected static void bind(Elements elms, Object obj) {
    elms.select("[name=refURL]").val(request.url);
    try {
      //
      if (obj instanceof List) {
        bindList(elms, obj);
      } else {
        bindObject(elms, obj);
      }
      //
    } catch (ParseException ex) {
      Logger.getLogger(Templator.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(Templator.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(Templator.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private static void bindList(Elements uls, Object obj)
          throws IllegalArgumentException, IllegalAccessException, ParseException {
    for (Element ul : uls) {
      Element first = ul.child(0).clone();
      ul.html("");
      for (Object o : (List) obj) {
        Element clone = first.clone();
        ul.appendChild(clone);
        bind(new Elements(clone), o);
      }
    }
  }

  private static void bindObject(Elements elms, Object obj)
          throws IllegalArgumentException, IllegalAccessException, ParseException {
    if (obj == null) {
      return;
    }
    Field[] fields = obj.getClass().getFields();
    Map<String, List<play.data.validation.Error>> errorMap = validation.errorsMap();
    System.out.println("errors: " + errorMap);

    for (Field field : fields) {
      for (Element elm : elms.select("." + field.getName())) {
        if (elm.hasClass("toHref")) {
          toHref(elm, field.get(obj));
        } else if (elm.hasClass("thumb")) {
          thumb(elm, field.get(obj));
        } else if (elm.hasClass("markdown")) {
          markdown(elm, field.get(obj));
        } else if (elm.tagName().equalsIgnoreCase("input")) {
          input(elm, field.get(obj));
          List<play.data.validation.Error> errors = errorMap.get(elm.attr("name"));
          if (errors != null) {
            bindError(elm, errors);
          }
        } else if (elm.tagName().equalsIgnoreCase("textarea")) {
          textarea(elm, field.get(obj));
          List<play.data.validation.Error> errors = errorMap.get(elm.attr("name"));
          if (errors != null) {
            bindError(elm, errors);
          }
        } else {
          Object o = field.get(obj);
          if (o != null) {
            String val = o.toString();
            if (!val.trim().isEmpty()) {
              elm.text(val);
            }
          }
        }
        bind(elm.children(), obj);
      }
    }
  }

  private static void input(Element elm, Object value) {
    if (value != null) {
      elm.val(value.toString());
    }
  }

  private static void textarea(Element elm, Object value) {
    if (value != null) {
      elm.val(value.toString());
    }
  }

  private static void markdown(Element elm, Object value) throws ParseException {
    if (value == null) {
      return;
    }
    String val = value.toString();
    if (!val.trim().isEmpty()) {
      val = markdown.Markdown.transformMarkdown(val);
      elm.html(val.toString());
    }
  }

  private static void toHref(Element elm, Object value) {
    String href = elm.attr("href");
    if (!href.endsWith("/")) {
      String[] sa = href.split("/");
      href = "";
      for (int i = 0; i < sa.length - 1; i++) {
        href += sa[i] + "/";
      }
    }
    elm.attr("href", href + value);
  }

  private static void thumb(Element elm, Object value) {
    Blob b = (Blob) value;
    System.out.println("Blob: " + b);
    String w = elm.attr("width");
    String h = elm.attr("height");
    elm.attr("src", "/thumbnail/"
            + w + "x" + h
            + "/" + b.getUUID());
  }

  private static void bindError(Element elm, List<play.data.validation.Error> errors) {
    for (play.data.validation.Error err : errors) {
      elm.val("");
      elm.attr("placeholder", err.message());
      elm.addClass("error");
    }
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
      if (!src.exists()) {
        src = new File(Play.applicationPath, "/public/img/person.png");
      }

      Thumbnails.of(src)
              .crop(Positions.CENTER)
              .size(w, h)
              .outputFormat("png")
              .toFile(dst);
    }
    renderBinary(dst);
  }
}
