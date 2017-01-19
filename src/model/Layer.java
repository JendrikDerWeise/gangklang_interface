package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Layer<E> implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1739620532941469155L;
	private String pName;
	private int pSoundtrackCount;
	private int pMaxSoundsCount;
	private List<LayerSoundtrack<E>> layerSoundtrackList;
	
	public Layer(String name, int layerCount, int maxSounds){
		setName(name);
		setLayersCount(layerCount);
		setMaxSoundsCount(maxSounds);
		
		layerSoundtrackList = new ArrayList<>();
	}
	
	public void addLayerToList(LayerSoundtrack<E> layer){
		layerSoundtrackList.add(layer);
	}
	
	public List<LayerSoundtrack<E>> getLayerList(){
		return layerSoundtrackList;
	}

	public String getName() {
		return pName;
	}

	public void setName(String pName) {
		this.pName = pName;
	}

	public int getLayersCount() {
		return pSoundtrackCount;
	}

	public void setLayersCount(int pLayersCount) {
		this.pSoundtrackCount = pLayersCount;
	}

	public int getMaxSoundsCount() {
		return pMaxSoundsCount;
	}

	public void setMaxSoundsCount(int pMaxSoundsCount) {
		this.pMaxSoundsCount = pMaxSoundsCount;
	}
}
