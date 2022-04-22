/**
 * 
 */
package kr.datareplicationserver;

import kr.datareplicationserver.configure.ConfigurationManager;
import kr.datareplicationserver.configure.Configure;

/**
 * @author ALTIBASE
 *
 */
public class StartupManager {

	/**
	 * 
	 */
	public StartupManager() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Configure conf = ConfigurationManager.start().build("conf/dataReplicationServer.conf");
		boolean isExit = false;
		
		while (isExit != true)
		{
			try {
				Thread.sleep(1000);
				System.out.println("test");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("sleep error : " + e.getMessage());
			}
			//sync data
		}
	}

}
