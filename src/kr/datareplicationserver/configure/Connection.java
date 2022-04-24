package kr.datareplicationserver.configure;

public class Connection {
	public String name;
	public String jdbcDriverPath;
	public String jdbcURL;
	public String user;
	public String passwd;
	
	public String toString()
	{
		String result = "name: " + name + ", " + "jdbcDriverPath: " + jdbcDriverPath + ", " +
						"jdbcURL: " + jdbcURL + ", " +
						"user: " + user + ", " +
						"passwd: " + passwd;
		return result;
	}
}
