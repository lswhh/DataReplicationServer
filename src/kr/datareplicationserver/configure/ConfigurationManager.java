package kr.datareplicationserver.configure;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConfigurationManager {

	private String confPath;
	private Configure conf;
	
	public ConfigurationManager() {
		conf = new Configure();
	}
	
	public static ConfigurationManager start()
	{
		return new ConfigurationManager();
	}
	
	public Configure build(String configurationPath ) //throws Exception
	{
		confPath = configurationPath;
		parseJSONFile();
//		if ( parseJSONFile() != true )
//		{
//			throw 
//		}
		return conf;
	}
	
	private boolean parseJSONFile()
	{
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
				obj = parser.parse(new FileReader(confPath));
				JSONObject jsonObject = (JSONObject) obj;
				String name = (String) jsonObject.get("name");

				conf.name = name;
				
				JSONObject connection = (JSONObject) jsonObject.get("connection");
				JSONObject connFrom = (JSONObject) connection.get("from");
				conf.from.jdbcDriverPath = (String) connFrom.get("jdbcDriverPath");
				conf.from.jdbcURL = (String) connFrom.get("jdbcURL");
				conf.from.user = (String) connFrom.get("user");
				conf.from.passwd = (String) connFrom.get("passwd");

				JSONObject connTo = (JSONObject) connection.get("to");
				conf.to.jdbcDriverPath = (String) connTo.get("jdbcDriverPath");
				conf.to.jdbcURL = (String) connTo.get("jdbcURL");
				conf.to.user = (String) connTo.get("user");
				conf.to.passwd = (String) connTo.get("passwd");

				JSONObject replicationTables = (JSONObject) jsonObject.get("replicationTables");
				ReplicationTable replicationTable = new ReplicationTable();
				replicationTable.fromUser = (String)replicationTables.get("fromUser");
				replicationTable.fromTable = (String)replicationTables.get("fromTable");
				replicationTable.toUser = (String)replicationTables.get("toUser");
				replicationTable.toTable = (String)replicationTables.get("toTable");
				
				conf.ReplicationTableList.add(replicationTable);
				
				conf.print();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	public boolean jsonTest()
	{
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
				obj = parser.parse(new FileReader("conf/data_replication_server.conf"));
			JSONObject jsonObject = (JSONObject) obj;
			String name = (String) jsonObject.get("name");
			System.out.println("name :: " + name);
			long id = (Long) jsonObject.get("id");
			System.out.println("id :: " + id);
			// Array
			JSONArray phoneNum = (JSONArray) jsonObject.get("phoneNumbers");
			Iterator<String> iterator = phoneNum.iterator();
			while (iterator.hasNext()) {
				System.out.println("phoneNumbers :: " + iterator.next());
			}
			// Object
			JSONArray array = (JSONArray) jsonObject.get("address");
			for (int i = 0; i < array.size(); i++) {
				JSONObject result = (JSONObject) array.get(i);
				System.out.println("result :: " + result.get("zipcode"));
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		e.printStackTrace();
		}
		return true;
	}
}
