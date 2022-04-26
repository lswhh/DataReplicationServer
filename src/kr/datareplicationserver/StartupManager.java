/**
 * 
 */
package kr.datareplicationserver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.LinkedList;

import kr.datareplicationserver.configure.ConfigurationManager;
import kr.datareplicationserver.configure.Configure;
import kr.datareplicationserver.database.JdbcDriverManager;
import kr.datareplicationserver.dataextractor.DriverInitializer;
import kr.datareplicationserver.util.TraceLogger;

/**
 * @author ALTIBASE
 *
 */
public class StartupManager {

	private static TraceLogger mTraceLogger = TraceLogger.getInstance();
	private final static String traceDir = "trc";
	
    private static String         mJavaVersion          = null;
    private static String         mJavaVendor           = null;
    private static String         mJavaSelectorProvider = null;
    private static String         mCPUArchitecture      = null;
    private static String         mOSName               = null;
    private static String         mOSVersion            = null;
    private static String         mDefaultCharset       = null;
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
		
		getSystemEnvironments();
		
		Configure conf = ConfigurationManager.start().build("conf/dataReplicationServer.conf");
		boolean isExit = false;

        mTraceLogger.setDirPath(traceDir);
        mTraceLogger.setFileName(conf.logPath);
        mTraceLogger.setLevel(TraceLogger.LogLevelTrace);
        
        if ( mTraceLogger.start() != true )
        {
        	throw new IllegalArgumentException("no valid path: " + conf.logPath);
        }

        logSystemEnvironments();

		DriverInitializer driverInitializer = new JdbcDriverManager(); 
		driverInitializer.loadDrivers(conf);

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
	
    /**
     * Trace logging for system environments
     */
    private static void logSystemEnvironments()
    {
        String sSystemEnvironments = getSystemEnvironmentsString();
        
        mTraceLogger.logInfo(sSystemEnvironments);
    }
    
    /**
     * Get system environments
     * 
     * @return  System environments
     */
    private static String getSystemEnvironmentsString()
    {
        String sEnvironment = "System environments (";
        
        LinkedList<String> sLinkedList = new LinkedList<String>();
        sLinkedList.add("Java Version = "           + mJavaVersion);
        sLinkedList.add("Java Vendor = "            + mJavaVendor);
        sLinkedList.add("Java Selector Provider = " + mJavaSelectorProvider);
        sLinkedList.add("CPU Architecture = "       + mCPUArchitecture);
        sLinkedList.add("OS Name = "                + mOSName);
        sLinkedList.add("OS Version = "             + mOSVersion);
        sLinkedList.add("Default Character Set = "  + mDefaultCharset);

        Iterator<String> sIterator = sLinkedList.iterator();
        if (sIterator.hasNext() == true)
        {
            sEnvironment += ((String)sIterator.next());
        }

        while (sIterator.hasNext() == true)
        {
            sEnvironment += ", " + ((String)sIterator.next());
        }
        
        sEnvironment += ")";
        
        return sEnvironment;
    }
    /**
     * Get system environments
     */
    private static void getSystemEnvironments()
    {
        mJavaVersion          = System.getProperty("java.version");
        mJavaVendor           = System.getProperty("java.vendor");
        mJavaSelectorProvider = System.getProperty("java.nio.channels.spi.SelectorProvider");
        mOSName               = System.getProperty("os.name");
        mCPUArchitecture      = System.getProperty("os.arch");
        mOSVersion            = System.getProperty("os.version");
        mDefaultCharset       = getDefaultCharset().toString();
    }

    /**
     * Get default character set
     * 
     * @return  Charset object
     */
    private static Charset getDefaultCharset()
    {
        Charset sDefaultCharset = null;

        String sFileEncoding = System.getProperty("file.encoding");

        sDefaultCharset = Charset.forName(sFileEncoding);
        if (sDefaultCharset == null)
        {
            sDefaultCharset = Charset.forName("UTF-8");
        }

        return sDefaultCharset;
    }
    /**
     * Set JAVA system properties
     */
    private static void setJavaSystemProperties()
    {
        // 'IOException: Invalid argument' exception of Selector.select method
        // call on Solaris 10 (on sparcv9 and x64)
        // (reference: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6782668)

        String sOSName    = System.getProperty("os.name");

        final String sPollClassName = "sun.nio.ch.PollSelectorProvider";

        if ("SunOS".equals(sOSName) == true)
        {
            if (canCreateClassName(sPollClassName) == true)
            {
                System.setProperty("java.nio.channels.spi.SelectorProvider", sPollClassName);
            }
        }
        else if ("Linux".equals(sOSName) == true)
        {
            if (canCreateClassName(sPollClassName) == true)
            {
                System.setProperty("java.nio.channels.spi.SelectorProvider", sPollClassName);
            }
        }
    }

    /**
     * Whether create or fail the class name
     * 
     * @param   aClassName  Class name to create
     * @return  true, if create. false, if fail.
     */
    private static boolean canCreateClassName(String aClassName)
    {
        Class sClass = null;

        try
        {
            sClass = Class.forName(aClassName);
        }
        catch (Exception e)
        {
            e.toString();
        }
        
        return (sClass != null) ? true : false;
    }
}
