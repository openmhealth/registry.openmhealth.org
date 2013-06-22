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
public class DPU extends Model {
  /*
   DPU
   Requires Schema IDs it can consume and schema IDs it can produce. 
   * (version is implicit)
   Can be a library or web service
   */

  /**
   *
   */
  @Required(message = "The name is required.")
  @Field
  public String name;
  //public String URL;
  public String sourceRepository;
  @Field
  public String keyWords;
  @Lob
  @Field
  @MaxSize(10000)
  @Required(message = "The description is required.")
  public String description;
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<User> maintainers = new ArrayList<User>();
  /*
   @ManyToMany//(cascade = CascadeType.ALL)
   public List<Skeema> consumeSkeemas;
   @ManyToMany//(cascade = CascadeType.ALL)
   public List<Skeema> produceSkeemas;
   */
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<Skeema> skeemas;

  public void update(DPU anotherDpu) {
    try {
      java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();
      for (java.lang.reflect.Field field : fields) {
        if (!field.getName().equals("id")) {
          if (field.get(anotherDpu) != null) {
            field.set(this, field.get(anotherDpu));
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
