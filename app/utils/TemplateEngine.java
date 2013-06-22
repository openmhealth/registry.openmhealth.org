/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import utils.*;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import play.Play;
import play.mvc.Controller;

/**
 *
 * @author maku
 */
public class TemplateEngine extends Controller {

  private HashMap<String, Document> templates = new HashMap<String, Document>();
  private final File templateFile;
  private final File templateDir;

  public TemplateEngine(Class controllerClass, String fileName) {
    templateDir = new File(Play.applicationPath,
            "app/views/" + controllerClass.getName());
    templateFile = new File(templateDir, fileName);
    System.out.println("");
  }

  public Document getTemplate(String sectionName) {
    Document doc = templates.get(sectionName);
    if (doc == null) {
      doc = templates.get("wholeApp");
      if (doc == null) {
        try {
          doc = Jsoup.parse(templateFile, "UTF-8");
          templates.put("wholeApp", doc.clone());
        } catch (IOException ex) {
          Logger.getLogger(TemplateEngine.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
      doc.select(".section").not("#" + sectionName).remove();
      templates.put(sectionName, doc.clone());
    }
    return doc;
  }
}
