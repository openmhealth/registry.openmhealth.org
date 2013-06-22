/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 *
 * @author maku
 */
@Entity
@Indexed
public class Skeema extends Model {

  @Unique(message = "Schema ID must be unique")
  @Required(message = "Schema ID is required")
  @Field
  public String skeemaID;
  //
  @Field
  public String description = "";
  //
  @Field
  public String keyWords = "";
  //
  @ManyToMany(cascade = CascadeType.DETACH)
  public List<User> maintainers = new ArrayList<User>();
  //

  @Override
  public String toString() {
    return getId() + " " + skeemaID + " ::" + maintainers;
  }
}
