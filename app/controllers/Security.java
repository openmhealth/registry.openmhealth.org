/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

/**
 *
 * @author maku
 */
public class Security extends Secure.Security {

  static boolean authenticate(String username, String password) {
    //return user != null && user.password.equals(password);
    return username.equals("omh1") && password.equals("OMHRocks!");
  }
}
