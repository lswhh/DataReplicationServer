package kr.datareplicationserver;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ConfigurationManager {

	public ConfigurationManager() {
		// TODO Auto-generated constructor stub
	}
//	{ 
//		"id":123, "name":"Pankaj Kumar", 
//		"permanent":true, 
//		"address":[{ 
//		"street":"El Camino Real", "city":"San Jose", "zipcode":"95014" }], 
//		"phoneNumbers":["9988664422", "1234567890"], "role":"Developer" 
//		}
	public boolean jsonTest()
	{
		JSONParser parser = new JSONParser();
		Object obj = null;
		try {
				obj = parser.parse(new FileReader("d:\\test.json"));
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
