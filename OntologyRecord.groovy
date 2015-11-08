/**
 * An OntologyRecord is an instance of a 
 */
package db;

@Grapes([
  @Grab(group='commons-io', module='commons-io', version='2.4'),
  @Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7' )
])

import groovyx.net.http.HTTPBuilder
import java.sql.Timestamp
import java.lang.reflect.Modifier
import groovyx.net.http.ContentType
import org.apache.commons.io.FileUtils
import org.apache.commons.codec.digest.DigestUtils

class OntologyRecord {
  public final static String BASE_ONTOLOGY_DIRECTORY = 'onts/'
  public final static String API_KEY = '24e0413e-54e0-11e0-9d7b-005056aa3316' // TODO: Global config

  String id
  String name
  String description
  String homepage
  String source
  String status
  LinkedHashMap submissions
  ArrayList owners
  ArrayList species 
  ArrayList topics
  ArrayList contact
  long lastSubDate

  void addNewSubmission(data) {
    def http = new HTTPBuilder()
    def fileName = id+'_'+(submissions.size()+1)+'.ont'
    def oFile = new File(BASE_ONTOLOGY_DIRECTORY+fileName)
    def tempFile = new File('/tmp/'+fileName)

    // Get the checksum of the most recent release.
    def currentFile = new FileInputStream(new File(BASE_ONTOLOGY_DIRECTORY+submissions[lastSubDate]))
    def oldSum = md5Hex(currentFile)
    currentFile.close()

    http.get('uri': data.download, 'contentType': ContentType.BINARY, 'query': [ 'apikey': API_KEY ] ) { 
      resp, ontology ->
        FileUtils.copyInputStreamToFile(ontology, tempFile)
        def newSum = md5Hex(new FileInputStream(tempFile))

        if(oldSum != newSum) {
          FileUtils.moveFile(tempFile, oFile)
          lastSubDate = data.released
          submissions[data.released] = fileName
        }
        oFile.close()
        tempFile.close()
    }
  }

  Map asMap() {
    this.class.declaredFields.findAll { !it.synthetic && !Modifier.isStatic(it.modifiers) }.collectEntries {
      [ (it.name):this."$it.name" ]
    }
  }
}
