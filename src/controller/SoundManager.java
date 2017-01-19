package controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SoundManager extends ManagerMom {
	File directory;
	
	public SoundManager(){
		super("sounds");
	}
	
	public void copySoundFile(File source) throws IOException{
		String fileName = source.getName();
		File directory = new File("sounds/" + fileName);
		Files.copy(source.toPath(), directory.toPath());
	}
}
