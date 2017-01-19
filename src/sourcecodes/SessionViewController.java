package sourcecodes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import controller.ObjectSaver;
import model.Gait;
import model.Layer;
import model.Level;
import model.Project;
import model.WalkAlong;

public class SessionViewController {
	private Project mProject;
	private String maxVolTurn;
	private String volStanding;
	private String initialswing;
	private String midswing;
	private String initialswingfast;
	private String midswingfast;
	private String steadyStepLevels;
	private String steadyStepCases;
	private String steadyStepInitial;
	private String steadyStepMid;
	private String stopAllLayers;
	private String steadyStepSwitcher;

	
	public SessionViewController(Project project) {
		mProject = project;
		maxVolTurn = "0.1";
		volStanding = "0.1";
		steadyStepInitial = "";
		steadyStepMid = "";
		stopAllLayers = "";
		steadyStepSwitcher = "NO";
	}
	
	public void getProjectSettings(){
		int temp = (int) mProject.getTurnMainMap().get("volume");
		float f = (float)temp / 10;
		maxVolTurn = (String) ""+f;
		temp = (int) mProject.getStandingMap().get("volume");
		f = (float)temp / 10;
		volStanding = (String) ""+f;
		initialswing = getSwing("left");
		midswing = getSwing("right");
		initialswingfast = getSwing("leftfast");
		midswingfast = getSwing("rightfast");
		steadyStepLevels = makeSteadyStepLevelsArray();
		steadyStepCases = getSteadyStepCases();
	}
	
	private String getSwing(String foot){
		int gaitNumber = 0;
		if(mProject.getGaitMapList().size() > 0){
			for(int i=0; i< mProject.getGaitMapList().size(); i++){
				Map<String, Object> gaitMap = (Map<String, Object>) mProject.getGaitMapList().get(i);
				if(gaitMap.get("foot").equals(foot)){
					gaitNumber = (int)gaitMap.get("gaitNumber");
					break;
				}
			}
			
			return "[self.appDelegate.audioController sonifyGait:"+gaitNumber+" withPace:gaitEvent.timeInterval];";
		}
		return "";
	}
	
	private String makeSteadyStepLevelsArray(){
		String steadyStepLevels = "";
		File file = (File) mProject.getContinMap().get("movement");
		if(file.exists()){
			steadyStepSwitcher = "YES";
			String fileName = file.getName();
			WalkAlong wa = (WalkAlong) ObjectSaver.loadObject(fileName,"walkalongs");
			int []sSL = wa.getStartAfter();
			for(int i : sSL)
				steadyStepLevels += "@"+i+", ";
			return steadyStepLevels.substring(0,steadyStepLevels.length()-2);
		}
		
		return steadyStepLevels;
	}
	
	private String getSteadyStepCases(){
		String steadyStepCases="";
		//TODO case 0 Layer stop
		for(Object o : mProject.getLayerNames()){
			String name = (String) o;
			stopAllLayers += "[self.appDelegate.audioController stopLayerWithName:@\""+name+"\"];\n";
		}
			
		
		int gaitNumber = mProject.getLastGaitNumber();
		int caseNumber = 1;
		File file = (File) mProject.getContinMap().get("movement");
		if(file.exists()){
			String fileName = file.getName();
			WalkAlong wa = (WalkAlong) ObjectSaver.loadObject(fileName,"walkalongs");
			for(Map<String,Object> m : wa.getlevelMapList()){
				gaitNumber++;
				File levelFile = (File) m.get("level");
				String levelFileName = levelFile.getName();
				Level level = (Level)ObjectSaver.loadObject(levelFileName,"levels");
				GaitTranslator gt = new GaitTranslator(level.getGaitList(),(List<Gait>)(Object)level.getGaitsWithoutMap(), gaitNumber);
				
				String panning = gt.getPanning().replaceAll("self", "self.appDelegate.audioController");
				String sonifyDelay = gt.getSonifyDelay().replaceAll("self", "self.appDelegate.audioController");
				String sonifyMode = gt.getSonifyMode().replaceAll("self", "self.appDelegate.audioController");
				String startLayer = "";
				//TODO if ambient != null, hier soundcarpet einfügen	
				for(Map<String,Object> layerMap : level.getAmbientList()){
					Layer<?> layer = (Layer<?>) ObjectSaver.loadObject((String)layerMap.get("ambient").toString(),".");
					String layerName = layer.getName();
					String vol = (String)layerMap.get("volume");
					startLayer += "[self.appDelegate.audioController playLayerWithName:@\""+ layerName + "\" withEnvelope:" + vol + "];";
				}
				
				steadyStepCases += "case " + caseNumber + ":\n" + panning + sonifyDelay + sonifyMode + "self.steadyStepLevel=" + caseNumber + ";\n"+startLayer+"\nbreak;\n\n";
				
				gt.writeSteadyStepSwings();
				steadyStepInitial += "if(self.steadyStepLevel > " + (caseNumber-1) + "){\n" + gt.getInitialSwingForSteadyStep() + "\n}\n";
				steadyStepMid += "if(self.steadyStepLevel > " + (caseNumber-1) + "){\n" + gt.getMidSwingForSteadyStep() + "\n}\n";
				
				caseNumber++;
			}
		}
		return steadyStepCases;
	}


	public void replaceAndWrite() throws IOException{
		File flowMachinePath = ObjectSaver.loadPath();
		String path = flowMachinePath.toString() + "/FlowMaschine2/classes/ui/SessionViewController.m";
		File file = new File("SessionViewController.m");
		FileWriter out = new FileWriter(path);

        if (!file.canRead() || !file.isFile()) //TODO File not exists hier abfangen
            System.exit(0); 

            BufferedReader in = null; 
        try { 
            in = new BufferedReader(new FileReader("SessionViewController.m")); 
            String line = null; 
            while ((line = in.readLine()) != null) { 
                line = line.replace("maxVolTurn", maxVolTurn);
                line = line.replace("volStanding", volStanding);
                line = line.replace("XinitialswingX", initialswing);
                line = line.replace("XmidswingX", midswing);
                line = line.replace("XinitialswingfastX", initialswingfast);
                line = line.replace("XmidswingfastX", midswingfast);
                line = line.replace("XsteadyStepLevelsX", steadyStepLevels);
                line = line.replace("XsteadyStepCasesX", steadyStepCases);
                line = line.replace("XsteadyStepMidSwingX", steadyStepMid);
                line = line.replace("XsteadyStepInitialSwingX", steadyStepInitial);
                line = line.replace("XstopAllLayersX", stopAllLayers);
                line = line.replace("XsteadyStepSwitcherX", steadyStepSwitcher);
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
