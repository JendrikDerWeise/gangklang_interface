package sourcecodes;

import java.util.List;
import java.util.Map;

import model.Gait;

public class GaitTranslator {
	private List<Map<String, Object>> pGaitMapList;
	private List<Gait> pGaitList;
	private int pLastGaitNumber;
	private String pInitialSwingForSteadyStep;
	private String pMidSwingForSteadyStep;
	
	public GaitTranslator(List<Map<String, Object>> gaitMapList, List<Gait> gaitList, int lastGaitNumber) {
		pGaitMapList = gaitMapList;
		pGaitList = gaitList;
		pLastGaitNumber = lastGaitNumber;
		pInitialSwingForSteadyStep = "";
		pMidSwingForSteadyStep = "";
	}
		
	
		
	public String getPanning(){
		String panning="";
		for(int i=0;i<pGaitMapList.size(); i++){
			//Gait nummer (alle sind gelistet, auch doppelt) mit den Infos aus der jeweiligen Map
			Map<String, Object> gait = (Map<String, Object>) pGaitMapList.get(i);
			int gaitNumber = i+pLastGaitNumber;
			int temp = (int) gait.get("lVol");
			float f = (float)temp / 10;
			String lVol = (String) ""+f;
			temp = (int) gait.get("rVol");
			f = (float)temp /10;
			String rVol = (String) ""+f;
			panning += "[self setPanningtoGait:" +gaitNumber+ " withR:" +rVol + " withL:" + lVol + "];\n";
			gait.put("gaitNumber", gaitNumber);
			pGaitMapList.remove(i);
			pGaitMapList.add(i,gait);
		}
		return panning;
	}
		
	public String getSonifyDelay(){
		String sonify = "";
			
		for(int i=0; i< pGaitMapList.size(); i++){
			Map<String, Object> gaitMap = (Map<String, Object>) pGaitMapList.get(i);
			int gaitNumber = i+pLastGaitNumber;
			String side = (String)gaitMap.get("foot");
			switch(side){
			case "left":
				sonify += "[self sonifyDelay:0 toGait:" +gaitNumber+ "];\n";
				break;
			case "leftfast":
				sonify += "[self sonifyDelay:0 toGait:" +gaitNumber+ "];\n";
				break;
			case "right":
				sonify += "[self sonifyDelay:1 toGait:" +gaitNumber+ "];\n";
				break;
			case "rightfast":
				sonify += "[self sonifyDelay:1 toGait:" +gaitNumber+ "];\n";
				break;
			}
		}
		return sonify;
	}
		
	public String getSonifyMode(){
		String sonify = "";
		for(int i=0;i<pGaitList.size(); i++){//mProject.getGaitList().size()
			Gait gait = (Gait)pGaitList.get(i);
			int gaitNumber = i+pLastGaitNumber;
			switch(gait.getGaitVariant()){
			case "continous":
				sonify += "[self sonifyMode:1 toGait:" +gaitNumber+ " withParameters:[NSArray arrayWithObjects:[NSNumber numberWithFloat:" +gait.getNumberOfSounds()+ "], nil]];\n";
				break;
			case "every X steps play sound #":
				sonify += "[self sonifyMode:2 toGait:" +gaitNumber+ " withParameters:[NSArray arrayWithObjects:[NSNumber numberWithFloat:"+gait.getNumberOfSteps()+"],[NSNumber numberWithFloat:"+gait.getNumberOfSoundStepTrigger()+"], nil]];\n";
				break;
			case "random play":
				sonify += "[self sonifyMode:3 toGait:" +gaitNumber+ " withParameters:[NSArray arrayWithObjects:[NSNumber numberWithFloat:" +gait.getNumberOfSounds()+ "], nil]];\n";
				break;
			}
			pLastGaitNumber = gaitNumber;
		}
			
		return sonify;
	}
	
	public void writeSteadyStepSwings(){
		for(Map<String,Object> m : pGaitMapList){
			String foot = (String)m.get("foot");
			int gaitNumber = (int)m.get("gaitNumber");
			String str = "[self.appDelegate.audioController sonifyGait:"+gaitNumber+" withPace:gaitEvent.timeInterval];";
			switch(foot){
			case "left":
				pInitialSwingForSteadyStep += str;
				break;
			case "right":
				pMidSwingForSteadyStep += str;
				break;
			}
		}
	}

	public int getLastGaitNumber(){
		return pLastGaitNumber;
	}
	
	public String getMidSwingForSteadyStep(){
		return pMidSwingForSteadyStep;
	}
	
	public String getInitialSwingForSteadyStep(){
		return pInitialSwingForSteadyStep;
	}
}
