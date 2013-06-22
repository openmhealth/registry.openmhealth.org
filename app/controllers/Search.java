/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.List;
import models.DPU;
import models.DSU;
import models.DVU;
import models.Skeema;
import models.User;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import play.db.jpa.GenericModel.JPAQuery;
import play.modules.search.Query;

/**
 * @author maku
 */
public class Search extends Templator {

  //--------------------------------------------------------------------------
  // Search
  //--------------------------------------------------------------------------
  public static void search(String q) {
    Document dom = getTemplate("searchResults");
    dom.select("[name=q]").val(q);
    searchUsers(q, dom);
    searchSchemas(q, dom);
    searchDSUs(q, dom);
    searchDPUs(q, dom);
    searchDVUs(q, dom);
    Element ul = dom.select("#searchResults .schemas ul").first();
    if (ul.children().size() == 0) {
      ul.parent().remove();
    }
    if (dom.select("#searchResults .units li").size() == 0) {
      dom.select("#searchResults .units").remove();
    }
    ul = dom.select("#searchResults .people ul").first();
    if (ul.children().size() == 0) {
      ul.parent().remove();
    }
    if (dom.select("#searchResults li").size() == 0) {
      dom.select(".results").first().text("No results for: " + q);
    } else {
      dom.select(".q").first().text(q);
    }
    renderHtml(dom);
  }

  private static void searchLucene(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "firstName:" + quote(q)
            + " OR lastName:" + quote(q)
            + " OR organization:" + quote(q)
            + " OR description:" + quote(q),
            User.class);
    List<User> users = query.fetch();
    if (users.size() > 0) {
      bind(dom.select("#searchResults .people ul"), users);
      dom.select(".noResults").remove();
    } else {
      dom.select("#searchResults .authors").remove();
    }
  }

  private static void searchUsers(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "firstName:" + quote(q)
            + " OR lastName:" + quote(q)
            + " OR organization:" + quote(q)
            + " OR description:" + quote(q),
            User.class);
    List<User> users = query.fetch();
    Elements ul = dom.select("#searchResults .people ul");
    bind(ul, users);

    /*
     List<User> users = User.find("select u from User u where u.firstName"
     + " like ? or u.lastName like ?"
     + " or u.organization like ? or u.description like ?", q, q, q, q).fetch();
     Elements ul = dom.select("#searchResults .people ul");
     bind(ul, users);
     */
  }

  private static void searchSchemas(String q, Document dom) {
    /*
     List<Skeema> skeemas = Skeema.find("select s from Skeema s"
     + " where s.description like ? or s.keyWords like ?"
     + " or s.skeemaID like ? ", q, q, q).fetch();
     Elements ul = dom.select("#searchResults .schemas ul");
     */
    Query query = play.modules.search.Search.search(
            "skeemaID:" + quote(q)
            + " OR keyWords:" + quote(q)
            + " OR description:" + quote(q),
            Skeema.class);
    List<Skeema> skeemas = query.fetch();
    Elements ul = dom.select("#searchResults .schemas ul");
    bind(ul, skeemas);
  }

  private static void searchDSUs(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "name:" + quote(q)
            + " OR keyWords:" + quote(q)
            + " OR description:" + quote(q),
            DSU.class);
    List<DSU> dsus = query.fetch();
    Elements ul = dom.select("#searchResults .dsus ul");
    bind(ul, dsus);
    /*
     List<DSU> dsus = User.find("select s from DSU s"
     + " where s.description like ? or s.keyWords like ?"
     + " or s.name like ? ", q, q, q).fetch();
     */
  }

  private static void searchDPUs(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "name:" + quote(q)
            + " OR keyWords:" + quote(q)
            + " OR description:" + quote(q),
            DPU.class);
    List<DPU> dpus = query.fetch();
    Elements ul = dom.select("#searchResults .dpus ul");
    bind(ul, dpus);
  }

  private static void searchDVUs(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "name:" + quote(q)
            + " OR keyWords:" + quote(q)
            + " OR description:" + quote(q),
            DVU.class);
    List<DVU> dvus = query.fetch();
    Elements ul = dom.select("#searchResults .dvus ul");
    bind(ul, dvus);
  }

  private static void searchSchemaz(String q, Document dom) {
    Query query = play.modules.search.Search.search(
            "skeemaID:" + quote(q)
            + " OR description:" + quote(q)
            + " OR keyWords:" + quote(q),
            Skeema.class)
            .page(0, 100000);
    List<Skeema> skeemas = query.fetch();
    if (skeemas.size() > 0) {
      //   bindList(dom.select("#searchResults .schemas ul").first(), skeemas);
      dom.select(".noResults").remove();
    } else {
      dom.select("#searchResults .schemas").remove();
    }
  }

  private static void searchUnits(String q, Document dom) {
    dom.select("#searchResults .units").remove();
  }

  private static String quote(String s) {
    return "\"" + s + "\"";
  }
}
