import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileItemFactory
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.http.entity.FileEntity
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.ByteArrayBody
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.StringBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.io.File
import groovy.json.JsonSlurper
import groovyx.net.http.*
import java.net.URI

//////////////////////
///// Parameters ---------- START
//////////////////////

// the host where you want to automatically deploy
def host = "localhost"
// the port where you want to automatically deploy
def port = 24718;
// the username of someone who has access to the whole Page management REST API
def username = "Walter.Bates"
// the password of a Bonitasoft user who has access to the whole Page management REST API
def password = "bpm"
// the filename of the archive to be automatically deployed
def fileName = "api-ext-ldap-helper-7.1.0-assembly.zip"
// the path of the archive file to be automatically deployed
def filepath = "./target/" + fileName

//////////////////////
///// Parameters ----------- END
//////////////////////

def setcookies

def sluper = new JsonSlurper()

//---------- login

def bonitasoft = new RESTClient("http://" + host + ":" + port + "/")

def loginResult = bonitasoft.post(
	path: "/bonita/loginservice",
	requestContentType: "application/x-www-form-urlencoded",
	body: "username=" + username + "&password=" + password + "&redirect=true"
)

log.info "Login Status: " + loginResult.status
log.info 'Login Headers: -----------'
loginResult.headers.each { h ->
	log.info " ${h.name} : ${h.value}"
	if(h.name.equals("Set-Cookie")) {
		setcookies = h.value;
	}
}

bonitasoft.defaultRequestHeaders.'Cookie' = setcookies
log.info bonitasoft.defaultRequestHeaders.toString()

//----------------- upload the file

File file = new File(filepath);
byte[] data = file.getBytes();

def bonitasoftUploadFile = new HTTPBuilder('http://' + host + ':' + port + '/bonita/portal/pageUpload')
def bonitasoftUploadFileResult = bonitasoftUploadFile.request(Method.POST) { req ->
	requestContentType: "multipart/form-data"
	MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
	multiPartContent.addPart("file", new ByteArrayBody(data, fileName))
	req.setEntity(multiPartContent)
	headers['Cookie'] = setcookies
}

def resultString = "${bonitasoftUploadFileResult}".toString()
def pageZip = resultString.substring(0, resultString.indexOf("[]"));

log.info "Upload File Status: " + pageZip

//------------- get the id of the page if it already exist

def getPageResult = bonitasoft.get(
	path: "/bonita/API/portal/page",
	requestContentType: "application/json",
	query: [p: "0", c: "10000"]
)

log.info "Get Page Status: " + getPageResult.status
log.info 'Get Page Headers: -----------'
getPageResult.headers.each { h ->
	log.info " ${h.name} : ${h.value}"
}

def getPageResultString = getPageResult.data.toString()
def getPageResultJSON = sluper.parseText(getPageResultString)
log.info 'Get Page Content: ' + getPageResultJSON.toString()

def existingPageId = -1

getPageResultJSON.each { h ->
	if(h.contentName == "api-ext-ldap-helper-7.1.0-assembly.zip") {
		existingPageId = h.id
	}
}

log.info 'Existing Page Id: ' + existingPageId

if(existingPageId == -1) {
//------------- add the page using this new uploaded file (currently in tmp folder of server)
	def addPageResult = bonitasoft.post(
		path: "/bonita/API/portal/page",
		requestContentType: "application/json",
		body: "{\"pageZip\":\"" + pageZip + "\",\"formError\":\"\"}"
	)
	
	log.info "Page Add Status: " + addPageResult.status
	log.info 'Page Add Headers: -----------'
	addPageResult.headers.each { h ->
		log.info " ${h.name} : ${h.value}"
	}
} else {
//------------- edit the page using this new uploaded file (currently in tmp folder of server)
	def editPageResult = bonitasoft.put(
		path: "/bonita/API/portal/page/" + existingPageId,
		requestContentType: "application/json",
		body: "{\"pageZip\":\"" + pageZip + "\",\"formError\":\"\"}"
	)
	
	log.info "Page Edit Status: " + editPageResult.status
	log.info 'Page Edit Headers: -----------'
	editPageResult.headers.each { h ->
		log.info " ${h.name} : ${h.value}"
	}
}
