package kr.datareplicationserver.configure;

import java.util.List;

public class Configure {
	public String name;
	public String logPath;
	public Connection from;
	public Connection to;
	public List<ReplicationTable> ReplicationTableList ;
	
	public void print()
	{
		System.out.println("name :: " + name);
		System.out.println("logPath :: " + logPath);
		System.out.println("Connection from :: " +  from.toString());
		System.out.println("Connection to :: " +  to.toString());
		for ( int i = 0; i < ReplicationTableList.size(); i++ )
		{
			System.out.println("ReplicationTable :: " +  ReplicationTableList.get(i).toString());
			
		}
	}
}
