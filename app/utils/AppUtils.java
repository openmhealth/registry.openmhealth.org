/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.Skeema;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

/**
 *
 * @author maku
 */
public class AppUtils {

    public static JsonObject parseJSON(String txt) {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(txt).getAsJsonObject();
        return obj;
    }

    public static void schemaList(Element list, List<Skeema> skeemas) {
        list.html("");
        int i = 0;
        for (Skeema s : skeemas) {
            if (i < 11) {
                Element elm = list.appendElement("li").appendElement("a")
                        .attr("href", "/schema/" + s.getId().toString())
                        .text(s.skeemaID);
            } else if (i == 11) {
                Element elm = list.appendElement("li").appendElement("a")
                        .attr("href", "#")
                        .addClass("moreSchemas")
                        .text("MORE...");
            } else {
                list.appendElement("li").appendElement("a")
                        .addClass("hiddenSchema hidden")
                        .attr("href", "/schema/" + s.getId().toString())
                        .text(s.skeemaID);
            }
            i++;
        }
    }

    public static void schemaSelect(Element select, List<Skeema> skeemas) {
        List<Skeema> schemas = Skeema.all().fetch();
        for (Skeema s : schemas) {
            System.out.println(s);
            Element elm = select.appendElement("option").attr("value", s.getId().toString())
                    .text(s.skeemaID);
            if (skeemas.contains(s)) {
                elm.attr("selected", "selected");
            }
        }
    }

    public static void model2View(Element dom, Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            System.out.println("fields " + fields);
            for (Field field : fields) {
                String name = field.getName();
                Elements elms = dom.select("." + name);
                for (Element elm : elms) {
                    Object value = field.get(obj);
                    if (value != null) {
                        elm.text(value.toString());
                        if (elm.hasClass("link")) {
                            elm.attr("href", value.toString());
                        }
                    }
                }
                // elms.remove();
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AppUtils.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AppUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void model2Form(Element dom, Object obj) {

        try {
            for (Element elm : dom.select("input, textarea")) {
                String name = elm.attr("name");
                if (name.contains(".")) {
                    System.out.println("NAME " + name.split("\\.").length);
                    Field field = obj.getClass().getField(name.split("\\.")[1]);
                    Object value = field.get(obj);
                    if (value != null) {
                        elm.val(value.toString());
                    }
                }
            }
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(AppUtils.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(AppUtils.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(AppUtils.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(AppUtils.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }
}
