/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jobs;

import java.util.ArrayList;
import models.DSU;
import models.Skeema;
import models.User;
import play.cache.Cache;
import play.jobs.Job;

/**
 *
 * @author maku
 */
public class AddSchemas extends Job {

  private final DSU dsu;
  private final User user;

  public AddSchemas(DSU dsu, User user) {
    super();
    this.dsu = dsu;
    this.user = user;
  }

  @Override
  public void doJob() throws Exception {
    int i = 0;
    for (SchemaToAdd sta : schemasToAdd) {
      Cache.set(user.id + "addSchema",
              " Saved <span class='num'>" + i + "</span> of "
              + "<span class='total'>" + schemasToAdd.size() + "</span> Schemas");
      Skeema skeema = Skeema.find("bySkeemaID", sta.schemaID).first();
      if (skeema == null) {
        skeema = new Skeema();
        skeema.skeemaID = sta.schemaID;
      }
      skeema.maintainers.add(sta.maintainer);
      skeema.save();
      dsu.skeemas.add(skeema);
      i++;
    }
    Cache.set(user.id + "addSchema", null);
    schemasToAdd.clear();
    dsu.save();
  }
  //
  //
  //
  private final ArrayList<SchemaToAdd> schemasToAdd = new ArrayList<SchemaToAdd>();

  public void addSchema(String schemaID, User maintainer) {
    schemasToAdd.add(new SchemaToAdd(schemaID, maintainer));
  }

  public class SchemaToAdd {

    public SchemaToAdd(String schemaID, User maintainer) {
      this.schemaID = schemaID;
      this.maintainer = maintainer;
    }
    String schemaID;
    User maintainer;
  }
}
