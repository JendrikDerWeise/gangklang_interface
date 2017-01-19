package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import controller.ObjectSaver;

import java.io.Serializable;

public class Level implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3872998996893580960L;
	private String pName;
	private int pGaitCount;
	private int pAmbientCount;
	
	private List<Map<String,Object>> gaitList;
	private List<Map<String,Object>> ambientList;
	private List<Object> gaitsWithoutMap;
	
	public Level(String name, int gaits, int ambients){
		setName(name);
		setGaitCount(gaits);
		setAmbientCount(ambients);
		
		gaitList = new ArrayList<>();
		ambientList = new ArrayList<>();
		gaitsWithoutMap = new ArrayList<>();
	}
	
	public void addGaitToList(Map<String,Object> gait){
		gaitList.add(gait);
		gaitsWithoutMap.add(ObjectSaver.loadObject(gait.get("gait").toString(),"."));
	}
	
	public List<Object> getGaitsWithoutMap(){
		return gaitsWithoutMap;
	}
	
	public void addAmbientToList(Map<String,Object> ambient){
		ambientList.add(ambient);
	}

	public String getName() {
		return pName;
	}

	public void setName(String pName) {
		this.pName = pName;
	}

	public int getGaitCount() {
		return pGaitCount;
	}

	public void setGaitCount(int pGaitCount) {
		this.pGaitCount = pGaitCount;
	}

	public int getAmbientCount() {
		return pAmbientCount;
	}

	public void setAmbientCount(int pAmbientCount) {
		this.pAmbientCount = pAmbientCount;
	}
	
	public List<Map<String, Object>> getGaitList() {
		return gaitList;
	}

	public List<Map<String, Object>> getAmbientList() {
		return ambientList;
	}

}
