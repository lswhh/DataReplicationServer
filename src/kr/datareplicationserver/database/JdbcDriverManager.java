package kr.datareplicationserver.database;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.sql.XAConnection;
import javax.sql.XADataSource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;

import kr.datareplicationserver.configure.Configure;
import kr.datareplicationserver.dataextractor.DriverInitializer;
import kr.datareplicationserver.util.TraceLogger;

/**
 * JDBC driver manager to manage JDBC drivers
 */
public class JdbcDriverManager implements DriverInitializer
{
	private static Configure mConf = null;
    private static TraceLogger mTraceLogger = TraceLogger.getInstance();
    
    private static HashMap    mDriverMap         = new HashMap();
    private static HashMap    mXADataSrcMap      = new HashMap();
    private static LinkedList<Target> mInvalidTargetList = null;
    private static int        mLoginTimeout      = -1;

    private static int RetrySleepTime = 200; // 200ms
    
    /**
     * Get login timeout
     * 
     * @return  Login timeout
     */
    public static int getLoginTimeout()
    {
        return mLoginTimeout;
    }
    
    /**
     * Set login timeout
     * 
     * @param aSecond   Login timeout as second unit
     */
    public static void setLoginTimeout(int aSecond)
    {
        mLoginTimeout = aSecond;
        
        DriverManager.setLoginTimeout(aSecond);
    }
    
    @Override
	public void loadDrivers(Configure conf)
	{
		LinkedList<Target> targetList = new LinkedList<Target>();
		setConfigure(conf);
		targetList.add(getTarget(conf.from.name));
		targetList.add(getTarget(conf.to.name));
		LinkedList<Target> validTargetList = loadDrivers(targetList);
		if ( validTargetList.isEmpty() == true )
		{
			throw new UnsupportedOperationException("no valid target from conf");
		}
	}

    /**
     * Load JDBC drivers from target list
     * 
     * @param aTargetList       Target list to load JDBC drivers
     * @return                  Invalid target list
     */
	@Override
    public LinkedList<Target> loadDrivers(LinkedList<Target> aTargetList)
    {
        if (aTargetList == null)
            return null;
        
        LinkedList<Target> sValidTargetList = new LinkedList<Target>();
        HashSet    sTargetNameSet   = new HashSet();
        Iterator<Target>   sIterator;
        Target     sTarget;
        Driver     sDriver;
        boolean    sInvalidTarget;
        XADataSource sXADataSrc = null;
        
        clearInvalidTargetList();

        sIterator = aTargetList.iterator();
        while (sIterator.hasNext() == true)
        {
            sTarget = (Target)sIterator.next();

            sDriver = null;
            sInvalidTarget = false;
            sXADataSrc = null;
            
            if (sTarget.validate() == true)
            {
                if (sTargetNameSet.contains(sTarget.mName) == false)
                {
                    if (mDriverMap.containsKey( sTarget.mDriver.toString() ) == false)
                    {
                        sDriver    = newDriver(sTarget.mDriver);
                        
                        if (sDriver == null)
                        {
                            mTraceLogger.logError(
                                    "JDBC driver could not load. (Target Name = {0}, JDBC driver = {1})",
                                    new Object[] { sTarget.mName, sTarget.mDriver.toString() });

                            sInvalidTarget = true;
                        }
                        else
                        {
                            // successfully loaded JDBC driver from JAR file
                        }
                    }
                    else
                    {
                        // already loaded JDBC driver
                    }

//                    if (mXADataSrcMap.containsKey( sTarget.mName ) == false)
//                    {
//                        sXADataSrc = newXADataSource( sTarget );
//                    }
//                    else
//                    {
//                     // already loaded XADataSource
//                    }
                }
                else
                {
                    // already loaded target
                }
            }
            else
            {
                sInvalidTarget = true;
            }

            if ( sInvalidTarget == true )
            {
                // invalid target
                addInvalidTarget(sTarget);
            }
            else
            {
                // valid target

                sValidTargetList.add(sTarget);

                sTargetNameSet.add(sTarget.mName);
    
                if ( sDriver != null )
                {
                    mDriverMap.put(sTarget.mDriver.toString() , sDriver);
                }
                
                if ( sXADataSrc != null )
                {
                    mXADataSrcMap.put( sTarget.mName, sXADataSrc );
                }
            }
        }
        
        return sValidTargetList;
    }
    
    /**
     * Add invalid target list
     * 
     * @param aTarget   Target object
     */
    private static void addInvalidTarget(Target aTarget)
    {
        if (mInvalidTargetList == null)
        {
            mInvalidTargetList = new LinkedList<Target>();
        }

        mInvalidTargetList.add(aTarget);
    }
    
    /**
     * Get invalid target list
     * 
     * @return  Invalid target list
     */
    public static LinkedList<Target> getInvalidTargetList()
    {
        LinkedList<Target> sInvalidTargetList = null;
        
        if (mInvalidTargetList != null)
        {
            sInvalidTargetList = (LinkedList)mInvalidTargetList.clone();
        }
        
        return sInvalidTargetList;
    }
    
    /**
     * Clear invalid target list
     */
    public static void clearInvalidTargetList()
    {
        if (mInvalidTargetList != null)
        {
            mInvalidTargetList.clear();
            mInvalidTargetList = null;
        }
    }
    
    /**
     * Get java.sql.Connect object after remote server connect successfully. 
     * 
     * @param aTarget           Target object to connect to the remote server
     * @return                  java.sql.Connect object, if success. null, if fail.
     * @throws SQLException     java.sql.Exception for remote server connection
     */
    public static Connection getConnection(Target aTarget) throws SQLException
    {
        if (aTarget == null)
        {
            return null;
        }
        
        if ( aTarget.mDriver.mDriverPath == null ||
             aTarget.mConnectionUrl      == null ||
             aTarget.mUser               == null ||
             aTarget.mPassword           == null )
        {
            return null;
        }
        
        Properties sProperties = new Properties();
        sProperties.put("user",     aTarget.mUser);
        sProperties.put("password", aTarget.mPassword);

        return getConnection(aTarget.mDriver,
                             aTarget.mConnectionUrl,
                             sProperties);
    }
    
    public static XAConnection getXAConnection(Target aTarget) throws SQLException
    {
        if (aTarget == null)
        {
            return null;
        }
        
        if ( aTarget.mDriver.mDriverPath == null ||
             aTarget.mConnectionUrl      == null ||
             aTarget.mUser               == null ||
             aTarget.mPassword           == null )
        {
            return null;
        }
        
        return getXAConnection( aTarget.mName,
                                aTarget.mUser,
                                aTarget.mPassword );
    }
    
    /**
     * Get java.sql.Connect object after remote server connect successfully.
     * 
     * @param aDriverJarFilePath        JDBC driver file path
     * @param aUrl                      Connection url for remote server
     * @param aUser                     User to log-in to remote server
     * @param aPassword                 Password to log-in to remote server
     * @return                          java.sql.Connect object, if success. null, if fail.
     * @throws SQLException             java.sql.Exception for remote server connection
     */
    public static Connection getConnection( JdbcDriver aDriver,
                                            String aUrl,
                                            String aUser,
                                            String aPassword ) throws SQLException
    { 
        Properties sProperties = new Properties();
        sProperties.put("user",     aUser);
        sProperties.put("password", aPassword);

        return getConnection( aDriver, aUrl, sProperties );
    }
    
    /**
     * Get java.sql.Connect object after remote server connect successfully.
     * 
     * @param aDriverJarFilePath        JDBC driver file path
     * @param aUrl                      Connection url for remote server
     * @param aProperties               java.util.Properties object which contains user/password to log-in to remote server
     * @return                          java.sql.Connect object, if success. null, if fail.
     * @throws SQLException             java.sql.Exception for remote server connection
     */
    public static Connection getConnection( JdbcDriver aDriver,
                                            String     aUrl,
                                            Properties aProperties ) throws SQLException
    {
        Connection      sConnection     = null;
        Driver          sDriver         = null;
        
        sDriver = (Driver)mDriverMap.get( aDriver.toString() );

        if (sDriver == null)
        {
            mTraceLogger.logError(
                    "Unknown JDBC driver. ({0})",
                    new Object[] { aDriver.toString() } );
            
            return null;
        }
        
        try 
        {
            sConnection = sDriver.connect(aUrl, aProperties);
        }
        catch ( SQLException e )
        {
            mTraceLogger.logError(
                    "JDBC Driver = {0}, Connection Url = {1}, User = {2}",
                    new Object[] { sDriver.toString(), 
                                   aUrl,
                                   aProperties.get( "user" ) } );

            throw e;
        }

        return sConnection;
    }
    
    public static Connection getConnection( XAConnection aXaConnection ) throws SQLException
    {
        return aXaConnection.getConnection();
    }
    
    public static XAResource getXAResource( XAConnection aXaConnection ) throws SQLException
    {
        return aXaConnection.getXAResource();
    }
    
    public static XAConnection getXAConnection( String     aName,
                                                String     aUser,
                                                String     aPassword ) throws SQLException
    {
        XADataSource sXAds = (XADataSource)mXADataSrcMap.get( aName );

        if (sXAds == null)
        {
            mTraceLogger.logError( "Check the XADATASOURCE_CLASS_NAME of ({0})",
                                   new Object[] { aName } );

            return null;
        }

        return sXAds.getXAConnection( aUser, aPassword );
    }
    
    /**
     * Create new java.sql.Driver object from JDBC driver file
     * 
     * @param aDriverJarFilePath JDBC driver file path
     * @return Loaded java.sql.Driver object, if success. null, if fail.
     */
    private static Driver newDriver( JdbcDriver aDriver )
    {
    	String sDriverJarFilePath = aDriver.mDriverPath;
    	
        if ( sDriverJarFilePath == null )
        {
            return null;
        }
        
        Driver sDriver = null;

        try
        {
            String sJarUrlPath = "file:" + sDriverJarFilePath + "!/";
            URL sJarUrl = new URL("jar", "", sJarUrlPath);
            
            URLClassLoader sUrlClassLoader = 
                    new URLClassLoader(new URL[] { sJarUrl },
                                       sJarUrlPath.getClass().getClassLoader());
    
            JarInputStream sJarInputStream = new JarInputStream(
                        new BufferedInputStream(
                                new FileInputStream( sDriverJarFilePath ) ) );
   
            String sDriverClassName = aDriver.mDriverClassName;

            JarEntry sJarEntry;
            while ( ( sDriverClassName == null ) && 
                    ( sJarEntry = sJarInputStream.getNextJarEntry() ) != null )
            {
                // make sure we have only slashes, we prefer unix, not windows
                String sClassName = sJarEntry.getName().replace('\\', '/')
                        .replace('/', '.').replaceFirst(".class", "");
                
                Class sClass = null;
                
                try
                {
                    sClass = Class.forName(sClassName, false, sUrlClassLoader);
                }
                catch (ClassNotFoundException e)
                {
                    e.toString();
                }
                catch (NoClassDefFoundError e)
                {
                    e.toString();
                }
                catch (Error e)
                {
                    e.toString();
                }
                catch (Exception e)
                {
                    e.toString();
                }
    
                if (sClass == null)
                {
                    continue;
                }

                int i;
                Class[] sInterfaceClasses = sClass.getInterfaces();
                
                for (i = 0; i < sInterfaceClasses.length; ++i)
                {
                    if (sInterfaceClasses[i].getName().equals("java.sql.Driver") == true)
                    {
                        sDriverClassName = sClassName;
                        break;
                    }
                }
            }

            sJarInputStream.close();

            if ( sDriverClassName == null )
            {
                sDriver = null;
            }
            else
            {
                Class sDriverClass = sUrlClassLoader.loadClass( sDriverClassName );
        
                sDriver = (Driver)sDriverClass.newInstance();
            }
    
        }
        catch (MalformedURLException e)
        {
            mTraceLogger.log(e);
        }
        catch (FileNotFoundException e)
        {
            mTraceLogger.log(e);
        }
        catch (IOException e)
        {
            mTraceLogger.log(e);
        }
        catch (InstantiationException e)
        {
            mTraceLogger.log(e);
        }
        catch (IllegalAccessException e)
        {
            mTraceLogger.log(e);
        }
        catch (ClassNotFoundException e)
        {
            mTraceLogger.log(e);
        }
        catch (Exception e)
        {
            mTraceLogger.log(e);
        }
        
        return sDriver;
    }

//    private static XADataSource newXADataSource( Target aTarget )
//    {
//        XADataSource sXADataSrc          = null;
//        Class        sXADataSrcClass     = null;
//        URLClassLoader sUrlClassLoader   = null;
//        Method         sSetUrlMethod     = null;
//        
//        if ( aTarget == null )
//        {
//            return null;
//        }
//        
//        if ( ( aTarget.mXADataSourceClassName     == null ) ||
//             ( aTarget.mXADataSourceUrlSetterName == null ) )
//        {
//            return null;
//        }
//        
//        try
//        {
//            String sJarUrlPath = "file:" + aTarget.mDriver + "!/";
//            URL sJarUrl = new URL("jar", "", sJarUrlPath);
//    
//            sUrlClassLoader = 
//                    new URLClassLoader(new URL[] { sJarUrl },
//                                       sJarUrlPath.getClass().getClassLoader());
// 
//            sXADataSrcClass = sUrlClassLoader.loadClass( aTarget.mXADataSourceClassName );
//            
//            sXADataSrc = (XADataSource)sXADataSrcClass.newInstance();
//            
//            sSetUrlMethod = sXADataSrcClass.getMethod( aTarget.mXADataSourceUrlSetterName,
//                                                              new Class[]{ String.class } );
//            sSetUrlMethod.invoke( sXADataSrc, 
//                                  new Object[]{ aTarget.mConnectionUrl } );
//        }
//        catch (Exception e)
//        {
//            mTraceLogger.log(e);
//            return null;
//        }
//
//        return sXADataSrc;
//    }
    /**
     * Get Target object by connection name
     * 
     * @param connectionName A connection object name included from, to
     * @return selected Target object
     */
	public static Target getTarget(String connectionName) {
		Target target = null;
		if ( mConf.from.name == connectionName ) {
			target = new Target();
			target.initializeFromConf(mConf.from);
		}
		
		if ( mConf.to.name ==  connectionName ) {
			target = new Target();
			target.initializeFromConf(mConf.to);
		}
		return target;
	}    
    /**
     * Get Target object by target name, user and password
     * 
     * @param aTargetName     Target name
     * @param aTargetUser     Target user
     * @param aTargetPassword Target password
     * @return Target object
     */
    public static Target getTarget(String aTargetName,
                                   String aTargetUser,
                                   String aTargetPassword)
    {
        Target sTarget = null;

        Target sTargetByProperty = getTarget(aTargetName);
        if (sTargetByProperty == null)
        {
            // invalid target
            mTraceLogger.logError(
                    "Invalid target (target name = {0})", new Object[] { aTargetName } );
        }
        else
        {
            sTarget = (Target)sTargetByProperty.clone();
            
            if ( aTargetUser != null )
            {
                sTarget.setUser( aTargetUser );
            }
            
            if ( aTargetPassword != null )
            {
                sTarget.setPassword( aTargetPassword );
            }

            if (sTarget.validateWithUserInfo() == false)
            {
                // user or password is null
                sTarget = null;

                mTraceLogger.logError("Invalid target.");
            }
        }

        return sTarget;
    }

	public void setConfigure(Configure conf) {
		mConf = conf;		
	}

    /**
     * Connect to remote server
     * 
     * @param aLinkerDataSession Linker data session
     * @param aRemoteNodeSession Remote node session
     * @return Result code
     */

//    public static byte connectToRemoteServer( LinkerDataSession aLinkerDataSession,
//                                              RemoteNodeSession aRemoteNodeSession,
//                                              boolean aIsXA )
//    {
//        byte         sResultCode       = ResultCode.None;
//        int          sRetryCount       = 0;
//        ElapsedTimer sElapsedTimer     = ElapsedTimer.getInstance();
//        final int    sRemoteNodeReceiveTimeout =
//                         PropertyManager.getInstance().getRemoteNodeReceiveTimeout() * 1000;
//        boolean      sAutoCommitMode   = aLinkerDataSession.getAutoCommitMode();
//        SQLException sLastSQLException = null;
//        long         sTime1            = sElapsedTimer.time();
//
//        Connection   sConnection       = null;
//        XAConnection sXAConnection     = null;
//        XAResource   sXAResource       = null;
//
//        aRemoteNodeSession.executing(RemoteNodeSession.TaskType.Connection);
//
//        do
//        {
//            try
//            {
//            	if ( aIsXA == true )
//            	{
//                    sXAConnection = JdbcDriverManager
//                        .getXAConnection(aRemoteNodeSession.getTarget());
//
//		            if ( sXAConnection == null )
//		            {
//		                sResultCode = ResultCode.RemoteServerConnectionFail;
//		                break;
//		            }
//
//		            sXAResource = sXAConnection.getXAResource();
//
//		            sConnection = sXAConnection.getConnection();
//		            		            
//                    if ( sXAResource == null || sConnection == null )
//                    {
//                        sResultCode = ResultCode.RemoteServerConnectionFail;
//                        break;
//                    }
//            	}
//            	else
//            	{
//                    sConnection = JdbcDriverManager
//                        .getConnection(aRemoteNodeSession.getTarget());
//
//                    if ( sConnection == null )
//                    {
//                        sResultCode = ResultCode.RemoteServerConnectionFail;
//                        break;
//                    }
//            	}
//
//                DatabaseMetaData sDatabaseMetaData = sConnection.getMetaData();
//
//                String sDatabaseProductName    = sDatabaseMetaData.getDatabaseProductName();
//                String sDatabaseProductVersion = sDatabaseMetaData.getDatabaseProductVersion();
//                String sDriverName             = sDatabaseMetaData.getDriverName();
//                String sDriverVersion          = sDatabaseMetaData.getDriverVersion();
//                int    sMaxConnections         = sDatabaseMetaData.getMaxConnections();
//                int    sMaxStatements          = sDatabaseMetaData.getMaxStatements();
//                
//                mTraceLogger.logInfo(
//                                "Connected remote server. (Product Name = {0}, Product Version = {1}, Driver Name = {2}, Driver Version = {3}, Maximum Connections = {4}, Maximum Statements = {5})",
//                                new Object[] {
//                                        sDatabaseProductName,
//                                        sDatabaseProductVersion,
//                                        sDriverName,
//                                        sDriverVersion,
//                                        new Integer(sMaxConnections),
//                                        new Integer(sMaxStatements) });
//
//                // set auto-commit mode
//                if ( aIsXA == true )
//                {
//                    try
//                    {
//                        if ( sXAResource.setTransactionTimeout( 
//                                PropertyManager.getInstance().getRemoteNodeReceiveTimeout() ) == false )
//                        {
//                            mTraceLogger.logError( "XAResource setTransactionTimeout failed." );
//                        }
//                    }
//                    catch ( XAException e )
//                    {
//                        mTraceLogger.log( e );
//                    }
//                    
//                	sConnection.setAutoCommit(false);
//                    aRemoteNodeSession.setXAConnection( sXAConnection );
//                    aRemoteNodeSession.setXAResource( sXAResource );
//                }
//                else
//                {
//                    sConnection.setAutoCommit(sAutoCommitMode);
//                }
//
//                aRemoteNodeSession.setSessionState(
//                        RemoteNodeSession.SessionState.Connected);
//                aRemoteNodeSession.setJdbcConnectionObject(sConnection);
//
//                sLastSQLException = null;
//
//                break;
//            }
//            catch (SQLException e)
//            {
//                mTraceLogger.log(e);
//
//                sLastSQLException = e;
//            }
//
//            long sTime2 = sElapsedTimer.time();
//            if ((sTime2 - sTime1) >= sRemoteNodeReceiveTimeout)
//            {
//                break;
//            }
//
//            try
//            {
//                Thread.sleep(RetrySleepTime);
//            }
//            catch (InterruptedException e)
//            {
//                mTraceLogger.log(e);
//            }
//
//            ++sRetryCount;
//
//            mTraceLogger.logDebug(
//                    "Remote server retry to connect. (Retry Count = {0}",
//                    new Object[] { new Integer(sRetryCount) } );
//
//        } while (true);
//
//        if (sResultCode == ResultCode.None)
//        {
//            if (sLastSQLException != null)
//            {
//                aRemoteNodeSession.executed(new RemoteServerError(
//                        sLastSQLException));
//
//                sResultCode = ResultCode.RemoteServerConnectionFail;
//            }
//            else
//            {
//                aRemoteNodeSession.executed();
//
//                sResultCode = ResultCode.Success;
//            }
//        }
//        else
//        {
//            aRemoteNodeSession.executed(TaskResult.Failed);
//        }
//
//        return sResultCode;
//    }
}

