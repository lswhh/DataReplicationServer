package kr.datareplicationserver;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kr.datareplicationserver.util.TraceLogger;

class testTraceLogger {

	@Test
	void test() {
		TraceLogger    mTraceLogger          = TraceLogger.getInstance();
        // initialize trace logger
        mTraceLogger.setDirPath("trc");
        mTraceLogger.setFileName("test.log");
        mTraceLogger.setLevel(TraceLogger.LogLevelTrace);
        
        assertEquals(mTraceLogger.start(),true);
        mTraceLogger.logError("test error"); 
		//fail("Not yet implemented");
		
	}

}
