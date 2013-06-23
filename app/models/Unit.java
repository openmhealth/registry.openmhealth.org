/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.modules.search.Field;
import play.modules.search.Indexed;

/*
 * @author maku
 */
@Entity
@Indexed
public abstract class Unit extends OrmBase {

  @Required(message = "The name is required.")
  @Field
  public String name;
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
  @ManyToMany//(cascade = CascadeType.ALL)
  public List<Skeema> skeemas = new ArrayList<Skeema>();
  public String URL = "";

  @Override
  public String toString() {
    return ":" + getId() + " | " + name + " | " + URL;
  }
}
