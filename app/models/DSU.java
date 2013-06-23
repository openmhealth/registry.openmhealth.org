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
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 *
 * @author maku
 */
@Entity
@Indexed
public class DSU extends Unit {

  public String version = "1.0";

}
