package kr.datareplicationserver.dataextractor;

import java.util.LinkedList;

import kr.datareplicationserver.configure.Configure;
import kr.datareplicationserver.database.Target;

public interface DriverInitializer {
	
//	public void setConfigure(Configure conf);
	//public void loadDrivers();
	public LinkedList<Target> loadDrivers(LinkedList<Target> aTargetList);
	public void loadDrivers(Configure conf);
}
