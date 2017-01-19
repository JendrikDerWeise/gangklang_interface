package controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultListModel;

public class ManagerMom {
File directory;
	
	public ManagerMom(String directoryName){
		directory = new File(directoryName);
		directory.mkdirs();
	}
	
	
	public DefaultListModel<File> getListModel(){
		
		DefaultListModel<File> model = new DefaultListModel<File>();
		for(File f : getSoundList())
				model.addElement(f);

		return model;
	}
	
	public List<File> getSoundList(){
		List<File> soundList = new ArrayList<File>();
		File nothing = new File("nothing selected");
		soundList.add(nothing);
		for(File f : directory.listFiles()){
			if(!f.getName().toString().equals(".DS_Store"))
				soundList.add(f);
		}
		
		return soundList;
	}
	
	
	public void deleteFile(File file){
		file.delete();
	}
	
	

}

