package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import model.Gait;
import model.Layer;
import model.LayerSoundtrack;
import model.Level;
import model.Project;
import model.WalkAlong;
import xmlwise.Plist;

public class PlistFactory {
	private Project<?> mProject;
	private Map<String, Object> mProjectMap;
	private Map<String, Object> mGaitsMap;
	private Map<String, Object> mLayerMap;
	private List<String> mSoundList;
	private int gaitCounter;

	public PlistFactory(Project<?> project) {
		mProject = project;
		mProjectMap = new HashMap<>();
		mGaitsMap = new HashMap<>();
		mLayerMap = new HashMap<>();
		gaitCounter = 1;
		
		writeSoundmarksToMap();
		writeGaitsToMap(mProject.getGaitList());
		writeLevelGaitsToMap();
		writeSoundcarpetsToMap();
		writePlist();
	}
	
	private void writeSoundmarksToMap(){
		List<Map<String,Object>> soundList = mProject.getSoundlist();
		mSoundList = new ArrayList<>();
		
		for(Map<String, Object> m : soundList)
			mSoundList.add((String) m.get("name"));
		
		//for(Map<String, Object> m : soundList)
		//TODO COPY_WAV_TO_FLOW_FOLDER m.get("sound"); Und umbenennen in "name"
		//wenn sound nicht gewählt, irgendwo eine 0sec wav ablegen und diese als entsprechenden sound anlegen. spart codingarbeit
		
	}
	
	private void writeGaitsToMap(List<Object> list){
		for(Object o : list){
			List<String> soundList = new ArrayList<>();
			Gait g = (Gait) o;
			for(Object obj : g.getSoundList()){
				File f = (File) obj;
				String wavName = f.getName().replace(".wav", "");
				soundList.add(wavName);
			}
			mGaitsMap.put("dimens_"+gaitCounter,soundList);
			gaitCounter++;
		}
	}
	
	private void writeLevelGaitsToMap(){
		List<Map<String,Object>> levelList = getLevelList();
		for(Object l : levelList){
			writeGaitsToMap(getSingleLevel(l).getGaitsWithoutMap());
		}
	}
	
	private void writeSoundcarpetsToMap(){
		List<Map<String,Object>> levelList = getLevelList();
		for(Object l : levelList){
			writeSingleSoundcarpet(getSingleLevel(l).getAmbientList());
		}
	}
	
	private void writeSingleSoundcarpet(List<Map<String, Object>> ambientMapList){
		List<String> allSoundcarpetNamesList = new ArrayList<>();
		for(Map<String, Object> map : ambientMapList){
			Layer<?> layer = (Layer<?>) ObjectSaver.loadObject((String)map.get("ambient").toString(),"."); //einzelnes ambient/layer
			List<Object> singleLayer = new ArrayList<>();
			for(Object o : layer.getLayerList()){ //liste von tracks
				List<String> soundList = new ArrayList<>();
				LayerSoundtrack<?> layerSoundtrack = (LayerSoundtrack<?>) o;
				for(Object s : layerSoundtrack.getSoundList()){ //soundfiles in track
					File f = (File) s;
					String wavName = f.getName().replace(".wav", "");
					soundList.add(wavName);
				}
				
				singleLayer.add(layerSoundtrack.getSilenceMin());
				singleLayer.add(layerSoundtrack.getSilenceMax());
				singleLayer.add(soundList);
				allSoundcarpetNamesList.add(layer.getName());
			}
			mLayerMap.put(layer.getName(),singleLayer);
			mProject.setLayerNameList(allSoundcarpetNamesList);
		}
	}
	
	private List<Map<String,Object>> getLevelList(){
		Map<String,Object> continMap = mProject.getContinMap();
		if(!continMap.get("movement").toString().equals("nothing selected")){
			WalkAlong wa = (WalkAlong) ObjectSaver.loadObject((String)continMap.get("movement").toString(), ".");
			return (List<Map<String,Object>>)wa.getlevelMapList();
		}
		return new ArrayList<>();
	}
	
	private Level getSingleLevel(Object l){
		Map<String,Object> levelMap = (Map<String,Object>) l;
		return (Level) ObjectSaver.loadObject(levelMap.get("level").toString(),".");
	}
	
	private void writePlist(){
		mProjectMap.put("Gaits", mGaitsMap);
		mProjectMap.put("Soundmarks", mSoundList);
		mProjectMap.put("Layers", mLayerMap);
		try {
			Plist.store(mProjectMap, "SoundList.plist");
		} catch (IOException e1) {
			System.out.println("Error while writing to plist.");
			e1.printStackTrace();
		}
		
	}

}
