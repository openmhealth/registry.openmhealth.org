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
import javax.persistence.ManyToMany;
import play.data.validation.Required;
import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 * @author maku
 */
@Entity
@Indexed
public class DVU extends Model {

  @Field
  @Required(message = "The name is required.")
  public String name;
  //public String URL;
  public String sourceRepository;
  @Field
  public String keyWords;
  @Field
  @Required(message = "The description is required.")
  public String description;
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<User> maintainers = new ArrayList<User>();
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<Skeema> skeemas;

  public void update(DVU anotherDvu) {
    try {
      java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        if (!field.getName().equals("id")) {
          if (field.get(anotherDvu) != null) {
            field.set(this, field.get(anotherDvu));
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
