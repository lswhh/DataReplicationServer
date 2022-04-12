package kr.datareplicationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.datareplicationserver.configure.ConfigurationManager;

class testJSON {

	@Test
	void test() {
		ConfigurationManager cm = new ConfigurationManager();
		assertEquals(cm.jsonTest(),true);
	}

}
