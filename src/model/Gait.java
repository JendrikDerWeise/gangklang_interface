package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Gait implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2854250615798172928L;
	private String mName;
	private int mNumberOfSounds;
	private String mGaitVariant;
	private int mNumberOfSteps;
	private List<Object> mSounds;
	private int mNumberOfSoundStepTrigger;

	public Gait(String name,int numberOfSounds, String gaitVariant){
		setName(name);
		setNumberOfSounds(numberOfSounds);
		setGaitVariant(gaitVariant);
		
		mSounds = new ArrayList<>();
	}
	
	public void toSoundList(Object soundName){
		mSounds.add(soundName);
	}

	public int getNumberOfSounds() {
		return mNumberOfSounds;
	}

	public void setNumberOfSounds(int mNumberOfSounds) {
		this.mNumberOfSounds = mNumberOfSounds;
	}

	public String getGaitVariant() {
		return mGaitVariant;
	}

	public void setGaitVariant(String mGaitVariant) {
		this.mGaitVariant = mGaitVariant;
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public int getNumberOfSteps() {
		return mNumberOfSteps;
	}

	public void setNumberOfSteps(int mNumberOfSteps) {
		this.mNumberOfSteps = mNumberOfSteps;
	}
	
	public List<Object> getSoundList(){
		return mSounds;
	}
	
	public void setNumberOfSoundStepTrigger(int number){
		mNumberOfSoundStepTrigger = number;
	}
	
	public int getNumberOfSoundStepTrigger(){
		return mNumberOfSoundStepTrigger;
	}
}
