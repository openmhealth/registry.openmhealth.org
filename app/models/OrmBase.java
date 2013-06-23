/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import play.db.jpa.Model;

/**
 * @author maku
 */
public abstract class OrmBase extends Model {

  public OrmBase() {
    super();
  }

  public void copyFieldsFrom(OrmBase anotherOrm) {
    try {
      java.lang.reflect.Field[] fields = this.getClass().getFields();
      for (java.lang.reflect.Field field : fields) {
        if (!field.getName().equals("id")) {
          if (field.get(anotherOrm) != null) {
            field.set(this, field.get(anotherOrm));
          }
        }
      }
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(DSU.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(DSU.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
