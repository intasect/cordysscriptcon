/**
	A sample script that calls XMLStore UpdateXMLObject method to
	store data from this SOAP request
*/

// This is the organizational user DN that will be used for the SOAP call.
var userDN = Cordys.getRequestUserDN();

// Data is store under this key in XMLStore
var baseKey = "/scriptsample";
 
/**
 * Escapes the string so that it can be safely used as a XMLStore key.
 */
function escapeString(s)
{
	var res = "";
	
	// If the string comes from XML, this is needed.
	s = new String(s);
	
	for (var i = 0; i < s.length; i++) {
		var ch = s.charAt(i);
	
		if ((ch >= 'A' && ch <= 'Z') ||	
			(ch >= 'a' && ch <= 'z') ||
			(ch >= '0' && ch <= '9')) {
			res += ch;
		} else {
			res += "#";
		}
	}
	
	return res;
}

function soapCall(xmlRequest) {
	// Create a key from the request.
	var key = baseKey + "/" +
			  escapeString(xmlRequest.*::CITY) + "/" + 
			  escapeString(xmlRequest.*::LAST_NAME) + "/" + 
			  escapeString(xmlRequest.*::FIRST_NAME); 

	// Create the SOAP request body in soapReq variable.
   	var soapReq = 
		<UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
			<tuple key={key} version="organization" isFolder="false" unconditional="true">
        		<new>
       				{ xmlRequest.*::DATA }
        		</new>
			</tuple>
		</UpdateXMLObject>   	
	var soapRes;

 	// Send the request.
	soapRes = new XML(Cordys.sendSoapRequest(userDN, soapReq.toXMLString()));

	// Create the response structure.
	var res = <result>OK</result>;

	return res;	
}

// This part is executed first.
var connectorRequest = new XML(Cordys.getRequest());
var connectorResponse;

connectorResponse = soapCall(connectorRequest);
if (connectorResponse != null) {
	Cordys.setResponse(connectorResponse.toXMLString());
}

