
/**
	A sample script that calls Northwind GetEmpoyees method and
	returns a new XML structure that has a different structure 
*/

// This is the organizational user DN that will be used for the SOAP call.
var userDN = "cn=mpoyhone,cn=organizational users,o=development,cn=cordys,o=vanenburg.com";

function soapCall(xmlRequest) {
	// Create the SOAP request body in soapReq variable.
   	var soapReq = 
	    <GetEmployees xmlns="http://schemas.cordys.com/1.0/demo/northwind">
	      <cursor numRows="5"/>
	      <fromEmployeeID>0</fromEmployeeID>
	      <toEmployeeID>100000</toEmployeeID>
	    </GetEmployees>;
	var soapRes;

 	// Send the request.
	soapRes = new XML(Cordys.sendSoapRequest(userDN, soapReq.toXMLString()));

	// Create the response structure.
	var res = <myemps>
		    	<names/>
		    	<homephones/>
		  	  </myemps>;

	// Fill the response with data received from GetEmployeed method call.
	for each (e in soapRes.*::tuple.*::old.*) {
	    var rec = e;
 
	    res.names.* += <name>{ rec.*::FirstName + " " + rec.*::LastName }</name>;
	    res.homephones.* += <phone>{ rec.*::HomePhone.toString() }</phone>;
	}

	return res;	
}

// This part is executed first.

var connectorRequest = new XML(Cordys.getRequest());
var connectorResponse;

connectorResponse = soapCall(connectorRequest);
if (connectorResponse != null) {
	Cordys.setResponse(connectorResponse.toXMLString());
}

