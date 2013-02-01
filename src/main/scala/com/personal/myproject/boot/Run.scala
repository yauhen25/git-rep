package com.personal.myproject.boot

import org.eclipse.jetty.servlet.ServletContextHandler
import cc.spray.connectors.Servlet30ConnectorServlet

import java.util.Calendar
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.nio.SelectChannelConnector
import akka.event.slf4j.Logging

object Run extends Logging {
	val GMT_TIMEZONE: String = "Greenwich Mean Time"

	def main(args: Array[String]): Unit = {
    	log.info("Starting campaign service.")

		val envVar = System.getenv()
		var env:String = System.getProperty("akka.mode")
		if (env == null){
			env = envVar.get("AKKA_MODE")
		}
		if (env == null) {
		    throw new RuntimeException("Must set environment (Example: java -Dakka.mode=dev -jar ...) or export AKKA_MODE=dev")
		}
		val timeZone: String = Calendar.getInstance().getTimeZone().getDisplayName()
		if (!(env.equals("dev") || env.equals("test")) && !timeZone.equals(GMT_TIMEZONE)){
		    log.error(String.format("System clock is set to non-GMT timezone (%s). This may cause data integrity issues.", timeZone))
		    log.error("Exiting the Campaign Service")
      		throw new RuntimeException("Must set environment (Example: java -Dakka.mode=dev -jar ...)")
		}

		val port: Int = 8010

		val server : Server = new Server()

	    //localhost connector
	    val connector = new SelectChannelConnector();
	    connector.setPort(port);
	    server.addConnector(connector);

	    val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
	    context.setContextPath("/");
	    server.setHandler(context);

	    context.addServlet(classOf[Servlet30ConnectorServlet], "/*");

			val initializer: ServiceInitializer = new ServiceInitializer();
			context.addEventListener(initializer);

			// finally, start the Jetty Server
			server.start()
	    	server.join()
		}


}