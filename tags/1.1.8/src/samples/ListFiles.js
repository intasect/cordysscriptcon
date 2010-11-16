/**
	A sample script that returns a file list in the given directory.
	This shows how it is possible to use standard Java classes from
	a Javascript.
*/

// The main function that handles the SOAP request.
function main(xmlRequest) {
	var files = new java.io.File(xmlRequest.*::dir).listFiles();
	var reply = <files/>;

	if (files != null) {
		for (i = 0; i < files.length; i++) {
		   var file = files[i];
	
		   reply.* += <file>{file.getPath()}</file>;
		}
	}
	
	return reply;
}

// This part is executed first.

var connectorRequest = new XML(Cordys.getRequest());
var connectorResponse;

connectorResponse = main(connectorRequest);
if (connectorResponse != null) {
	Cordys.setResponse(connectorResponse.toXMLString());
}

