package model;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.ObjectSaver;

public class Project<E> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1250060866739847354L;
	private String pName;
	
	private Map<String, Object> footLeftMap;
	private Map<String, Object> footLeftFastMap;
	private Map<String, Object> footRightMap;
	private Map<String, Object> footRightFastMap;
	private Map<String, Object> standingMap;
	private Map<String, Object> continMap;
	private Map<String, Object> turnLeftGaitMap;
	private Map<String, Object> turnLeftAmbMap;
	private Map<String, Object> turnRightGaitMap;
	private Map<String, Object> turnRightAmbMap;
	private Map<String, Object> placesMap;
	private Map<String, Object> turnMainMap;
	private Map<String, Object> logoMap;
	private List<Object> gaitList;
	private List<Map<String, Object>> soundList;
	private List<Map<String, Object>> gaitMapList;
	private List<String> stringGaitList;
	private List<String> layerNames;
	private int lastGaitNumber;
	
	public Project(String name){
		setName(name);
		gaitList = new ArrayList<>();
		gaitMapList = new ArrayList<>();
		soundList = new ArrayList<>();
		stringGaitList = new ArrayList<>();
		layerNames = new ArrayList<>();
	}
	
	public void setLastGaitNumber(int n){
		lastGaitNumber = n;
	}
	
	public int getLastGaitNumber(){
		return lastGaitNumber;
	}
	
	public List<Object> getGaitList(){
		return gaitList;
	}
	public List<Map<String, Object>> getGaitMapList(){
		return gaitMapList;
	}
	
	private void addToList(List<Object> list, Map<String, Object> map, String type, List<String> stringList){
		File file = (File) map.get("movement");
		if(!file.getName().equals("nothing selected")){
			Object object = (Object)ObjectSaver.loadObject(file.getName(), type);
			//if(!stringList.contains(file.getName())){
				list.add(object);
				stringList.add(file.getName());
			//}
				if(type.equals("gaits"))
					gaitMapList.add(map);
		}
	}
	
	private void addToSoundList(String name,String sound){
		Map<String, Object> soundMap = new HashMap<>();
		soundMap.put("name",name);
		soundMap.put("sound",sound);
		soundList.add(soundMap);
	}

	public Map<String, Object> getFootLeftMap() {
		return footLeftMap;
	}

	public void setFootLeftMap(Map<String, Object> footLeftMap) {
		footLeftMap.put("lVol", footLeftMap.get("volume"));
		footLeftMap.put("rVol", 1);
		footLeftMap.put("foot","left");
		addToList(gaitList,footLeftMap,"gaits",stringGaitList);
		this.footLeftMap = footLeftMap;
	}

	public Map<String, Object> getFootLeftFastMap() {
		return footLeftFastMap;
	}

	public void setFootLeftFastMap(Map<String, Object> footLeftFastMap) {
		footLeftFastMap.put("lVol", footLeftFastMap.get("volume"));
		footLeftFastMap.put("rVol", 1);
		footLeftFastMap.put("foot","leftfast");
		addToList(gaitList,footLeftFastMap,"gaits",stringGaitList);
		this.footLeftFastMap = footLeftFastMap;
	}
	

	public Map<String, Object> getFootRightMap() {
		return footRightMap;
	}

	public void setFootRightMap(Map<String, Object> footRightMap) {
		footRightMap.put("lVol", 1);
		footRightMap.put("rVol", footRightMap.get("volume"));
		footRightMap.put("foot","right");
		addToList(gaitList,footRightMap,"gaits",stringGaitList);
		this.footRightMap = footRightMap;
	}

	public Map<String, Object> getFootRightFastMap() {
		return footRightFastMap;
	}

	public void setFootRightFastMap(Map<String, Object> footRightFastMap) {
		footRightFastMap.put("lVol", 1);
		footRightFastMap.put("rVol", footRightFastMap.get("volume"));
		footRightFastMap.put("foot","rightfast");
		addToList(gaitList,footRightFastMap,"gaits",stringGaitList);
		this.footRightFastMap = footRightFastMap;
	}

	public Map<String, Object> getStandingMap() {
		return standingMap;
	}

	public void setStandingMap(Map<String, Object> standingMap) {
		String sound = standingMap.get("movement").toString();
		addToSoundList("Standing",sound);
		this.standingMap = standingMap;
	}

	public Map<String, Object> getContinMap() {
		return continMap;
	}

	public void setContinMap(Map<String, Object> continMap) {
		this.continMap = continMap;
	}

	public Map<String, Object> getTurnLeftObjectMap() {
		return turnLeftGaitMap;
	}

	public void setTurnLeftObjectMap(Map<String, Object> turnLeftObjectMap) {
		addToList(gaitList,turnLeftObjectMap,"gaits",stringGaitList);
		this.turnLeftGaitMap = turnLeftObjectMap;
	}

	public Map<String, Object> getTurnLeftAmbMap() {
		return turnLeftAmbMap;
	}

	public void setTurnLeftAmbMap(Map<String, Object> turnLeftAmbMap) {
		this.turnLeftAmbMap = turnLeftAmbMap;
	}

	public Map<String, Object> getTurnRightObjectMap() {
		return turnRightGaitMap;
	}

	public void setTurnRightObjectMap(Map<String, Object> turnRightObjectMap) {
		addToList(gaitList,turnRightObjectMap,"gaits",stringGaitList);
		this.turnRightGaitMap = turnRightObjectMap;
	}

	public Map<String, Object> getTurnRightAmbMap() {
		return turnRightAmbMap;
	}

	public void setTurnRightAmbMap(Map<String, Object> turnRightAmbMap) {
		this.turnRightAmbMap = turnRightAmbMap;
	}

	public String getName() {
		return pName;
	}

	public void setName(String pName) {
		this.pName = pName;
	}

	public Map<String, Object> getPlacesMap() {
		return placesMap;
	}

	public void setPlacesMap(Map<String, Object> placesMap) {
		this.placesMap = placesMap;
	}

	public Map<String, Object> getTurnMainMap() {
		return turnMainMap;
	}

	public void setTurnMainMap(Map<String, Object> turnMainMap) {
		String sound = turnMainMap.get("movement").toString();
		addToSoundList("Turn_main",sound);
		this.turnMainMap = turnMainMap;
	}

	public Map<String, Object> getLogoMap() {
		return logoMap;
	}

	public void setLogoMap(Map<String, Object> logoMap) {
		String sound = logoMap.get("movement").toString();
		addToSoundList("Logo",sound);
		this.logoMap = logoMap;
	}

	public List<Map<String, Object>> getSoundlist() {
		return soundList;
	}

	public void setLayerNameList(List<String> list) {
		layerNames = list;
	}
	
	public List<String> getLayerNames(){
		return layerNames;
	}

}
