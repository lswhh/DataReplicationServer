package kr.datareplicationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.datareplicationserver.configure.ConfigurationManager;
import kr.datareplicationserver.configure.Configure;
import kr.datareplicationserver.database.JdbcDriverManager;
import kr.datareplicationserver.dataextractor.DriverInitializer;

class testJdbcDriverManager {

	@Test
	void test() {
		Configure conf = ConfigurationManager.start().build("conf/dataReplicationServer.conf");
		DriverInitializer driverInitializer = new JdbcDriverManager(); 
		driverInitializer.loadDrivers(conf);
		
		fail("Not yet implemented");
	}

}
