/**
	A sample script that writes a log message.
*/

if (LOG.isDebugEnabled()) {
	LOG.debug("This log message comes from a script");
}

LOG.info("The request is " + Cordys.getRequest());
