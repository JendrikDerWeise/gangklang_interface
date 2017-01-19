package sourcecodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import controller.ObjectSaver;
import model.Project;

public class AudioController {
	private Project mProject;
	private String mPanning;
	private String mSonifyDelay;
	private String mSonifyMode;
	private String mLoadLayers;
	private String mStopAllLayers;

	public AudioController(Project project) {
		mProject = project;
		GaitTranslator gt = new GaitTranslator(mProject.getGaitMapList(), mProject.getGaitList(),1);
		mPanning = gt.getPanning();
		mSonifyDelay = gt.getSonifyDelay();
		mSonifyMode = gt.getSonifyMode();
		mProject.setLastGaitNumber(gt.getLastGaitNumber());
		mStopAllLayers = "";
		mLoadLayers = "";
		writeLayers();
	}
	
	public void writeLayers(){
		for(Object o : mProject.getLayerNames()){
			String name = (String) o;
			mLoadLayers += "[self loadLayerwithName:@\""+name+"\"];\n";
			mStopAllLayers += "[self stopLayerWithName:@\""+name+"\"];\n";
		}
	}
	
	public void replaceAndWrite() throws IOException{
		File flowMachinePath = ObjectSaver.loadPath();
		String path = flowMachinePath.toString() + "/FlowMaschine2/AudioController.m";
		File file = new File("AudioController.m"); 
		FileWriter out = new FileWriter(path);

        if (!file.canRead() || !file.isFile()) 
            System.exit(0); 

            BufferedReader in = null; 
        try { 
            in = new BufferedReader(new FileReader("AudioController.m"));
            String line = null; 
            while ((line = in.readLine()) != null) { 
                line = line.replace("XpanningX", mPanning);
                line = line.replace("XsonifyDelayX", mSonifyDelay);
                line = line.replace("XsonifyModeX", mSonifyMode);
                line = line.replace("XloadLayersX", mLoadLayers);
                line = line.replace("XstopLayersX", mStopAllLayers);
                out.write(line);
                out.write("\n");
            } 
        } catch (IOException e) { 
            e.printStackTrace(); 
        } finally { 
            if (in != null) 
                try { 
                    in.close(); 
                    out.close();
                } catch (IOException e) { 
                } 
        } 
	}
	
	

}
