/**
 * The OntologyDatabase handles a bunch of ontologies and their versions. It's okay.'
 *
 */
package db;

import groovy.json.*
import redis.clients.jedis.*

class OntologyDatabase {
  public final static String DB_PREFIX = 'ontologies:'
  def db
  
  OntologyDatabase() {
    db = new JedisPool(new JedisPoolConfig(), "localhost");
  }

  /**
   * Return an ontology ID by name.
   *
   * @param id The id of the record to return.
   * @return The ontology, or null.
   */
  OntologyRecord getOntology(String id) {
    def db = db.getResource()
    id = DB_PREFIX + id 
    def item = db.get(id)
    db.close()
    if(item) {
      return new OntologyRecord(new JsonSlurper().parseText(item))
    } else {
      return null
    }
  }

  /**
   * Return an ontology ID by name.
   *
   * @param id The id of the record to return.
   * @param db A jedis resource. This is here to mitigate the problems with retrieving a lot of records in a short amount of time. Disgusting, I know. TODO: fix.
   * @return The ontology, or null.
   */
  OntologyRecord getOntology(String id, boolean noprefix) {
    def db = db.getResource()
    if(noprefix != true) {
      id = DB_PREFIX + id 
    }
    def item = db.get(id)
    db.close()
    if(item) {
      return new OntologyRecord(new JsonSlurper().parseText(item))
    } else {
      return null
    }
  }

  Set<String> allOntologies() {
    def db = db.getResource()
    def onts = []
    db.keys('ontologies:*').each { id ->
      onts.add(getOntology(id, true))
    }
    db.close()
    return onts
  }

  /**
   * Create a new ontology record, and add it to the 
   *
   * @param data An object with data about the ontology.
   */
  OntologyRecord createOntology(data) {
    // Not really the right place for this
    def db = db.getResource()
    data.lastSubDate = 0
    data.submissions = new LinkedHashMap()

    def oRecord = new OntologyRecord(data)
    db.set(DB_PREFIX + data.id, new JsonBuilder(oRecord.asMap()).toString())
    db.close()

    return oRecord
  }

  /**
   * Save an ontology to the database. Will overwrite any other OntologyRecord
   *  of the same id.
   * 
   * @param record The record to save.
   */
  void saveOntology(OntologyRecord record) {
    def db = db.getResource()
    db.set(DB_PREFIX + record.id, new JsonBuilder(record.asMap()).toString())
  }
}
