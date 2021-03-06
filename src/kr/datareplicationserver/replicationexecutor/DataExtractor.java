package kr.datareplicationserver.replicationexecutor;
import java.sql.*;

public interface DataExtractor {
	
	/*
	 * for SYNC_DATA: select * from user.table;
	 * for CHANGE_DATA: select * from user.table_change_log where low sequence;
	 */
	public abstract ResultSet getDataResultSet( String user, String table ); 
}
