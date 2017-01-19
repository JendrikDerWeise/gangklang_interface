package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

public class LayerSoundtrack<E> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Object> mSoundList;
	private float mSilenceMin;
	private float mSilenceMax;
	
	public LayerSoundtrack(float silenceMin, float silenceMax){
		mSilenceMin = silenceMin;
		mSilenceMax = silenceMax;
		mSoundList = new ArrayList<>();
	}
	
	public void setSoundList(List<JComboBox<E>> list){
		for(JComboBox<E> cb : list)
			mSoundList.add(cb.getSelectedItem());
	}

	public float getSilenceMin() {
		return mSilenceMin;
	}

	public void setSilenceMin(int mSilenceMin) {
		this.mSilenceMin = mSilenceMin;
	}
	
	public List<Object> getSoundList(){
		return mSoundList;
	}

	public float getSilenceMax() {
		return mSilenceMax;
	}

	public void setSilenceMax(int mSilenceMax) {
		this.mSilenceMax = mSilenceMax;
	}
	
}
