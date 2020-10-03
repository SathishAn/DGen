package dgen;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LoadHelpView {
	private String moduleName;
	
	public LoadHelpView(String module) {
		this.moduleName = module;
	}
	
	public String createHelpView() {
		String helpviewHtml = null;
		StringBuilder htmlbulider = new StringBuilder();
		
		JSONParser jsonparser = new JSONParser();
		try {
			htmlbulider.append("<html><body>");
			htmlbulider.append("<p>");
			String path =this.getClass().getResource("ModuleDetails/module.json").toString().replace("file:","");
			
			FileReader reader = new FileReader(path);
					
			Object obj = jsonparser.parse(reader);
			JSONArray modList = (JSONArray) obj;
			
			modList.forEach(emp->{
				JSONObject object = (JSONObject) emp;
				String moduleId = (String) object.get("id");
				String desc = (String) object.get("description");
				if(moduleId.equals(moduleName)) {
					htmlbulider.append("<h4>" +  this.moduleName + "</h4>" + desc);	
					
				}
					
			});
			
				
			htmlbulider.append("</p>");
			htmlbulider.append("</body></html>");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
		helpviewHtml = htmlbulider.toString();
		
		
		return helpviewHtml;
		
		
	}

}
