package kr.datareplicationserver.configure;

public class ReplicationTable {
	public String fromUser;
	public String fromTable;
	public String toUser;
	public String toTable;
	public String toString()
	{
		String result;
		result = "fromUser: " + fromUser + ", " +
				"fromTable: " + fromTable + ", " +
				"toUser: " + toUser + ", " +
				"toTable: " + toTable + ", " ;
		return result;
	}
}
