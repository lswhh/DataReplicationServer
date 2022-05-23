/**
 * 
 */
package kr.datareplicationserver.dataextractor;

import kr.datareplicationserver.configure.Configure;

/**
 * @author lsw00
 *
 */
public interface DBInteractor {
	public void setConfigure(Configure conf);
	public void setDriverInitializer(DriverInitializer driverInitializer);
}
