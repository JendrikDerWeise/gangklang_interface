package controller;

import java.util.HashMap;
import java.util.Map;

public class ProjectManager<E> extends ManagerMom{

	public ProjectManager() {
		super("projects");
	}
	
	public Map<String, Object> makeMap(Object object, int volume){
		Map<String, Object> map = new HashMap<>();
		map.put("movement", object);
		map.put("volume", volume);
		
		return map;
	}
	
	/*public void makeFile(String name,Map footLeftMap,Map footLeftFastMap,Map footRightMap,Map footRightFastMap,Map turnLeftGaitMap,Map turnRightGaitMap, Map turnLeftAmbMap, Map turnRightAmbMap,File stopMap, Level continMap){
		project = new Project<>(name);
		project.setContinMap(continMap);
		project.setFootLeftFastMap(footLeftFastMap);
	}*/

}
