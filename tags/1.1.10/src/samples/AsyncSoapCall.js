
/**
	A sample script that calls XMLStore UpdateXMLObject method after a 10 second
	delay. This call will be executed even when the script has terminated.  
*/

// Create the SOAP request body in soapReq variable.
var soapReq = 
	<UpdateXMLObject xmlns="http://schemas.cordys.com/1.0/xmlstore">
		<tuple key="/Cordys/ScriptConnector/Test/AsyncSoapCallOutput" version="organization" isFolder="false" unconditional="true">
       		<new>
      				<delaytest>
      					<data>Async Call Works</data>
      				</delaytest>
       		</new>
		</tuple>
	</UpdateXMLObject>;
var soapRes;

	// Schedule the request to be sent after 10 seconds. 
Cordys.scheduleSoapRequest(Cordys.getRequestUserDN(), soapReq.toXMLString(), 10000);
