import org.apache.commons.fileupload.FileItem
import org.apache.commons.fileupload.FileItemFactory
import org.apache.commons.fileupload.disk.DiskFileItem
import org.apache.commons.fileupload.disk.DiskFileItemFactory
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.content.InputStreamBody
import org.apache.http.entity.mime.content.StringBody
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import java.io.File;
import groovyx.net.http.*
import java.net.URI

//---------- login

def cookies = []

def bonitasoft = new RESTClient("http://localhost:8080/")
def loginResult = bonitasoft.post(
	path: "/bonita/loginservice",
	requestContentType: "application/x-www-form-urlencoded",
	body: "username=Walter.Bates&password=bpm&redirect=true"
)

log.info loginResult.status + ""
log.info 'Headers: -----------'
loginResult.headers.each { h ->
	log.info " ${h.name} : ${h.value}"
	if(h.name.equals("Set-Cookie")) {
		jsessionID = h.value.replace("JSESSIONID=", "").substring(0, 32)
		log.info " JSESSIONID has been found: " + jsessionID
		loginResult.getHeaders('Set-Cookie').each {
			String cookie = it.value.split(';')[0]
			log.info(" Adding cookie to collection: $cookie")
			cookies.add(cookie)
		}
	}
}

//-----------------

File file = new File("C:/Users/pierrick/Documents/Contribs/bonita-api-extension-ldap-helper/target/api-ext-ldap-helper-${pom.version}-assembly.zip");
String fieldName = "file";
String contentType = "application/zip";
boolean isFormField = false;
String fileName = "api-ext-ldap-helper-${pom.version}-assembly.zip";
int sizeThreshold = 10240;
DiskFileItemFactory factory = new DiskFileItemFactory();
FileItem fi = factory.createItem(fieldName,contentType,isFormField,fileName);
DiskFileItem item = new DiskFileItem(fieldName, contentType, isFormField, fileName, sizeThreshold, file);
item.getOutputStream();
MultipartFile mf = new CommonsMultipartFile(item);

def bonitasoftUploadFile = new HTTPBuilder('http://localhost:8080/bonita/portal/pageUpload?action=edit')
bonitasoftUploadFile.handler.failure = { resp, reader ->
	[response:resp, reader:reader]
}
bonitasoftUploadFile.handler.success = { resp, reader ->
	[response:resp, reader:reader]
}
def result = bonitasoftUploadFile.request(Method.POST) { req ->
	requestContentType: "multipart/form-data"
	MultipartEntity multiPartContent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE)
	// Adding Multi-part file parameter "imageFile"
	multiPartContent.addPart("file", new InputStreamBody(mf.inputStream, mf.contentType, mf.originalFilename))
	req.setEntity(multiPartContent)
	headers['Cookie'] = cookies.join(';')
	
}

log.info "response status: " + result['response'].statusLine
log.info 'Headers: -----------'
result['response'].headers.each { h ->
	log.info " ${h.name} : ${h.value}"
}

log.info 'Response data: -----'
log.info result['reader'].getText()
log.info '--------------------'