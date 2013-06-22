/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import play.db.jpa.Model;

/**
 *
 * @author maku
 */
@Entity
public class Star extends Model {

  @OneToOne
  public User user;
  //
  public String type;
  public String nodeID;
}
