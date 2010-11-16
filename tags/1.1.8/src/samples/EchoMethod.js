/**
	A sample script that returns the incoming request body.
*/

// Read the incoming SOAP request.
var connectorRequest = new XML(Cordys.getRequest());
var connectorResponse;

// Create the SOAP response.
connectorResponse =
	<Success>
		<Message>Hello {Cordys.getRequestUserDN()}</Message>
		<OriginalRequest>
		{connectorRequest}
		</OriginalRequest>
	</Success>;

// Set the SOAP response to the application connector.
if (connectorResponse != null) {
	Cordys.setResponse(connectorResponse.toXMLString());
}

