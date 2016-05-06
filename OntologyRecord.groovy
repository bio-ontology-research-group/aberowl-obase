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
import java.nio.file.*
import java.io.File

class OntologyRecord {
  public final static String BASE_ONTOLOGY_DIRECTORY = 'onts/'
  public final static String API_KEY = '24e0413e-54e0-11e0-9d7b-005056aa3316' // TODO: Global config

  String id
  String name
  String description
  String homepage
  String source
  String status
  String purl
  String ncbi_id
  LinkedHashMap submissions
  ArrayList owners
  ArrayList species 
  ArrayList topics
  ArrayList contact
  long lastSubDate

  void addNewSubmission(data) {
    def http = new HTTPBuilder()
    http.getClient().getParams().setParameter("http.connection.timeout", new Integer(600*1000))
    http.getClient().getParams().setParameter("http.socket.timeout", new Integer(600*1000))
    def fileName = id+'_'+(submissions.size()+1)+'.ont'
    def oFile = new File(BASE_ONTOLOGY_DIRECTORY+fileName)
    def tempFile = new File('/tmp/'+fileName)

    // Get the checksum of the most recent release.
    def oldSum = 0
    try {
      def currentFile = new FileInputStream(new File(BASE_ONTOLOGY_DIRECTORY+id+"_"+submissions.size()+".ont"))
      if(currentFile) {
        oldSum = DigestUtils.md5Hex(currentFile)
      }
      currentFile.close()
    } catch (Exception E) {
      E.printStackTrace()
    }

    println "Downloading from "+data.download
    //    http.get('uri': data.download, 'contentType': ContentType.BINARY, 'query': [ 'apikey': API_KEY ] ) { resp, ontology ->

    //    FileUtils.copyURLToFile(new URL(data.download+"?apikey="+API_KEY), tempFile)
    def proc = ("curl -L "+data.download+"?apikey="+API_KEY + " -o "+tempFile.getPath()).execute()
    proc.waitFor()

    //      FileUtils.copyInputStreamToFile(ontology, tempFile)
      def newSum = DigestUtils.md5Hex(new FileInputStream(tempFile))
      
      if(oldSum != newSum) {
	//FileUtils.moveFile(tempFile, oFile)
	/* pretty dangerous: */
	Files.move(tempFile.toPath(),oFile.toPath(),StandardCopyOption.REPLACE_EXISTING)
	lastSubDate = data.released
	submissions[data.released] = fileName
      }
      //    }
  }

  Map asMap() {
    this.class.declaredFields.findAll { !it.synthetic && !Modifier.isStatic(it.modifiers) }.collectEntries {
      [ (it.name):this."$it.name" ]
    }
  }
}
