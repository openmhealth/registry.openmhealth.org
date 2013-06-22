/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 *
 * @author maku
 */
@Entity
@Indexed
public class DSU extends Model {

  @Required(message = "The name is required.")
  public String name = "";
  public String version = "1.0";
  public String URL = "";
  @Field
  public String keyWords = "dsu";
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<User> maintainers = new ArrayList<User>();
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<Skeema> skeemas = new ArrayList<Skeema>();
  //
  public String sourceRepository = "";
  @Lob
  @Field
  @MaxSize(10000)
  @Required(message = "The description is required.")
  public String description = "";

  public void update(DSU anotherDsu) {
    try {
      java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        if (!field.getName().equals("id")) {
          if (field.get(anotherDsu) != null) {
            field.set(this, field.get(anotherDsu));
          }
        }
      }
    } catch (IllegalArgumentException ex) {
      Logger.getLogger(DSU.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      Logger.getLogger(DSU.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public String toString() {
    return "DSU:" + getId() + " | " + name + " | " + URL;
  }
}
