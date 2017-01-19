package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WalkAlong implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3910060777805675852L;
	private String pName;
	private int pLevelCount;
	private int [] pSteadyStepLevels;
	private List<Map<String, Object>> levelMapList;
	
	public WalkAlong(String name, int levelCount){
		setName(name);
		setLevelCount(levelCount);
		
		levelMapList = new ArrayList<>();
	}
	
	public void addLevelToList(Map<String, Object> levelMap){
		levelMapList.add(levelMap);
	}

	public int getLevelCount() {
		return pLevelCount;
	}

	public void setLevelCount(int pLevelCount) {
		this.pLevelCount = pLevelCount;
	}

	public String getName() {
		return pName;
	}

	public void setName(String pName) {
		this.pName = pName;
	}

	public List<Map<String, Object>> getlevelMapList(){
		return levelMapList;
	}

	public int[] getStartAfter() {
		return pSteadyStepLevels;
	}

	public void setStartAfter(int[] steadyStepLevels) {
		this.pSteadyStepLevels = steadyStepLevels;
	}
}
