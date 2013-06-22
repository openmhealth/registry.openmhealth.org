/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.Lob;
import play.data.validation.MaxSize;
import play.data.validation.Required;
import play.data.validation.Unique;
import play.db.jpa.Blob;
import play.db.jpa.Model;
import play.modules.search.Field;
import play.modules.search.Indexed;

/**
 *
 * @author maku
 */
@Entity
@Indexed
public class User extends Model {

  @Unique
  public String openID;
  @Required(message = "Your first name is required")
  @Field
  public String firstName;
  @Field
  public String lastName;
  public Blob avatar;
  @Required(message = "Your email is required")
  @Unique(message = "This email is already registered.")
  public String email;
  @Field
  public String organization;
  @Field
  @Lob
  @MaxSize(10000)
  public String description;
  public String password;
  public Boolean isAdmin;

  public void update(User anotherUser) {
    if (anotherUser.password != null) {
      password = anotherUser.password;
    }
    if (anotherUser.openID != null) {
      openID = anotherUser.openID;
    }
    if (anotherUser.email != null) {
      email = anotherUser.email;
    }
    if (anotherUser.avatar != null) {
      avatar = anotherUser.avatar;
    }
    if (anotherUser.lastName != null) {
      lastName = anotherUser.lastName;
    }
    if (anotherUser.firstName != null) {
      firstName = anotherUser.firstName;
    }
    if (anotherUser.description != null) {
      description = anotherUser.description.toString();
    }
    if (anotherUser.organization != null) {
      organization = anotherUser.organization;
    }
  }

  @Override
  public String toString() {
    Gson gson = new Gson();
    return gson.toJson(this).toString();
  }
}
