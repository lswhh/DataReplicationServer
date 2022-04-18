package kr.datareplicationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.datareplicationserver.configure.ConfigurationManager;
import kr.datareplicationserver.configure.Configure;

class testJSON {

	@Test
	void test() {
		//ConfigurationManager cm = new ConfigurationManager();
		//assertEquals(cm.jsonTest(),true);
		Configure conf = ConfigurationManager.start().build("conf/dataReplicationServer.conf");
		conf.print();
	}

}
