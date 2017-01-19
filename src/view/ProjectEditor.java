package view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.LayerManager;
import controller.GaitManager;
import controller.ObjectSaver;
import controller.PlistFactory;
import controller.ProjectManager;
import controller.SoundManager;
import controller.WalkAlongManager;
import model.Gait;
import model.Project;
import sourcecodes.AudioController;
import sourcecodes.SessionViewController;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class ProjectEditor<E> extends JPanel {
	
	final static String A = "ambient";
	final static String G = "gait";
	final static String W = "walkalong";
	final static String S = "sound";
	
	private JTextField projectName;
	private JSlider tfVolumeFootL;
	private JSlider tfVolumeFootR;
	private JSlider tfVolumeStanding;
	//private JSlider tfVolumeTurnL;
	//private JSlider tfVolumeTurnR;
	private JSlider tfVolumeFootLFast;
	private JSlider tfVolumeFootRFast;
	//private JSlider tfVolumeTurnLAmbient;
	//private JSlider tfVolumeTurnRAmbient;
	private JSlider tfVolumeContin;
	
	private JComboBox<E> cbFootL;
	private JComboBox<E> cbFootLFast;
	private JComboBox<E> cbFootR;
	private JComboBox<E> cbFootRFast;
	private JComboBox<E> cbStanding;
	private JComboBox<E> cbContin;
	//private JComboBox<E> cbTurnL;
	//private JComboBox<E> cbTurnR;
	//private JComboBox<E> cbTurnLAmb;
	//private JComboBox<E> cbTurnRAmb;
	
	private LayerManager am;
	private GaitManager gm;
	private WalkAlongManager waM;
	private SoundManager sm;
	private JLabel lblSpecialPlaces;
	private JComboBox<?> cbPlaces;
	private JSlider tfVolumePlaces;

	private Project<E> project;
	private JLabel lblTurnMain;
	private JLabel lblLogo;
	private JComboBox cbTurnMain;
	private JComboBox cbLogo;
	private JSlider tfVolumeTurnMain;
	private JSlider tfVolumeLogo;
	

	/**
	 * Create the panel.
	 */
	public ProjectEditor() {
		makeManagers();
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		
		
		
		JButton btnMakeApp = new JButton("Make app!");
		btnMakeApp.setEnabled(false);
		btnMakeApp.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				makeApp();
			}
		});
		
		JButton btnSaveProject = new JButton("Save project");
		btnSaveProject.setEnabled(false);
		btnSaveProject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveProject();
				btnMakeApp.setEnabled(true);
			}
		});
		JPanel btnPanel = GUIFactory.getBtnPanel();
		
		btnPanel.add(btnSaveProject);
		btnPanel.add(btnMakeApp);
		this.add(btnPanel, BorderLayout.EAST);
		
		//Name Panel NORTH
		JPanel namePanel = GUIFactory.getTransparentPanel();
		namePanel.setLayout(new FlowLayout());
		JLabel lblNameYourProject = new JLabel("Name your project:");
		
		namePanel.add(lblNameYourProject);
		
		projectName = new JTextField();
		projectName.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
				  }
				  public void removeUpdate(DocumentEvent e) {
					  if(projectName.getText().toString().length() == 0)
					    	btnSaveProject.setEnabled(false);
				  }
				  public void insertUpdate(DocumentEvent e) {
					  if(projectName.getText().toString().length() > 0)
					    	btnSaveProject.setEnabled(true);
				  }
		});
		projectName.setColumns(10);
		namePanel.add(projectName);
		
		this.add(namePanel, BorderLayout.NORTH);
		
		//Center Panel
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		this.add(centerPanel, BorderLayout.CENTER);
		
		
		//The headline
		JPanel headline = GUIFactory.getTransparentPanel();
		
		JLabel lblChooseSoundsFor = new JLabel("Walkevents:");
		headline.add(lblChooseSoundsFor);
		
		JLabel lblSoundGait = new JLabel("Single Sound / WalkBeat / Soundcarpet:");
		headline.add(lblSoundGait);
		
		JLabel lblVolume = new JLabel("Volume:");
		headline.add(lblVolume);
		
		centerPanel.add(headline);

		
		//Foot left panel
		JPanel lfPanel = GUIFactory.getTransparentPanel();
		JLabel lblFootL = new JLabel("Foot left:");
		lfPanel.add(lblFootL);
		
		cbFootL = new JComboBox(fillUp(G));
		lfPanel.add(cbFootL);
		
		tfVolumeFootL = GUIFactory.getVolumeSlider();
		lfPanel.add(tfVolumeFootL);
		centerPanel.add(lfPanel);
		
		//Foot left fast panel
		JPanel lfFastPanel = GUIFactory.getTransparentPanel();
		JLabel lblFootLFast = new JLabel("Foot left fast:");
		lfFastPanel.add(lblFootLFast);
		
		cbFootLFast = new JComboBox(fillUp(G));
		lfFastPanel.add(cbFootLFast);
		
		tfVolumeFootLFast = GUIFactory.getVolumeSlider();
		lfFastPanel.add(tfVolumeFootLFast);
		centerPanel.add(lfFastPanel);
		
		//Foot right panel
		JPanel rfPanel = GUIFactory.getTransparentPanel();
		JLabel lblFootR = new JLabel("Foot right");
		rfPanel.add(lblFootR);
		
		cbFootR = new JComboBox(fillUp(G));
		rfPanel.add(cbFootR);
		
		tfVolumeFootR = GUIFactory.getVolumeSlider();
		rfPanel.add(tfVolumeFootR);
		centerPanel.add(rfPanel);
		
		//Foot right fast panel
		JPanel rfFastPanel = GUIFactory.getTransparentPanel();
		JLabel lblFootRFast = new JLabel("Foot right fast:");
		rfFastPanel.add(lblFootRFast);
		
		cbFootRFast = new JComboBox(fillUp(G));
		rfFastPanel.add(cbFootRFast);
		
		tfVolumeFootRFast = GUIFactory.getVolumeSlider();
		rfFastPanel.add(tfVolumeFootRFast);
		centerPanel.add(rfFastPanel);
		
		//Standing panel
		JPanel standingPanel = GUIFactory.getTransparentPanel();
		JLabel lblStop = new JLabel("Standing:");
		standingPanel.add(lblStop);
		
		cbStanding = new JComboBox(fillUp(S));
		standingPanel.add(cbStanding);
		
		tfVolumeStanding = GUIFactory.getVolumeSlider();
		standingPanel.add(tfVolumeStanding);
		centerPanel.add(standingPanel);
		
		//Continuous Walk panel
		JPanel continuousWalkPanel = GUIFactory.getTransparentPanel();
		JLabel lblContinuousWalk = new JLabel("Continuous walk:");
		continuousWalkPanel.add(lblContinuousWalk);
		
		cbContin = new JComboBox(fillUp(W));
		continuousWalkPanel.add(cbContin);
		
		tfVolumeContin = GUIFactory.getVolumeSlider();
		continuousWalkPanel.add(tfVolumeContin);
		centerPanel.add(continuousWalkPanel);
		
		//turn left panel
		/*JPanel turnLeftPanel = new JPanel();
		turnLeftPanel.setLayout(layout);
		JLabel lblTurnLeft = new JLabel("Turn left:");
		lblTurnLeft.setEnabled(false);
		turnLeftPanel.add(lblTurnLeft);
		
		cbTurnL = new JComboBox(fillUp(S));
		cbTurnL.setEnabled(false);
		turnLeftPanel.add(cbTurnL);
		
		tfVolumeTurnL = new JFormattedTextField();
		tfVolumeTurnL.setValue(new Integer(0));
		tfVolumeTurnL.setEnabled(false);
		tfVolumeTurnL.setEditable(false);
		turnLeftPanel.add(tfVolumeTurnL);
		tfVolumeTurnL.setColumns(10);
		
		centerPanel.add(turnLeftPanel);*/
		
		//turn left Ambient
		/*JPanel turnLeftAmbPanel = GUIFactory.getTransparentPanel();
		
		JLabel lblTurnLeft = new JLabel("Turn left:");
		turnLeftAmbPanel.add(lblTurnLeft);
		
		cbTurnLAmb = new JComboBox(fillUp(A));
		turnLeftAmbPanel.add(cbTurnLAmb);
		
		tfVolumeTurnLAmbient = GUIFactory.getVolumeSlider();
		turnLeftAmbPanel.add(tfVolumeTurnLAmbient);
		
		centerPanel.add(turnLeftAmbPanel);*/
		
		//turn right panel
		/*JPanel turnRightPanel = new JPanel();
		turnRightPanel.setLayout(layout);
		JLabel lblTurnRight = new JLabel("Turn right:");
		lblTurnRight.setEnabled(false);
		turnRightPanel.add(lblTurnRight);
		
		cbTurnR = new JComboBox(fillUp(S));
		cbTurnR.setEnabled(false);
		turnRightPanel.add(cbTurnR);
		
		tfVolumeTurnR = new JFormattedTextField();
		tfVolumeTurnR.setValue(new Integer(0));
		tfVolumeTurnR.setEditable(false);
		tfVolumeTurnR.setEnabled(false);
		turnRightPanel.add(tfVolumeTurnR);
		tfVolumeTurnR.setColumns(10);
		
		centerPanel.add(turnRightPanel);*/
		
		//turn right Ambient Panel
		/*JPanel turnRightAmbPanel = GUIFactory.getTransparentPanel();
		
		JLabel lblTurnRight = new JLabel("Turn right:");
		turnRightAmbPanel.add(lblTurnRight);
		
		cbTurnRAmb = new JComboBox(fillUp(A));
		turnRightAmbPanel.add(cbTurnRAmb);
		
		tfVolumeTurnRAmbient = GUIFactory.getVolumeSlider();
		turnRightAmbPanel.add(tfVolumeTurnRAmbient);
		
		centerPanel.add(turnRightAmbPanel);*/
		
		//special places panel
		JPanel specialPlacesPanel = GUIFactory.getTransparentPanel();
		
		lblSpecialPlaces = new JLabel("Special places:");
		specialPlacesPanel.add(lblSpecialPlaces);
		
		cbPlaces = new JComboBox(fillUp(A));
		specialPlacesPanel.add(cbPlaces);
		
		tfVolumePlaces = GUIFactory.getVolumeSlider();
		specialPlacesPanel.add(tfVolumePlaces);
		
		centerPanel.add(specialPlacesPanel);
		
	
		//turn main panel
		JPanel turnMainPanel = GUIFactory.getTransparentPanel();
		
		lblTurnMain = new JLabel("Turn Main:");
		turnMainPanel.add(lblTurnMain);
		
		cbTurnMain = new JComboBox(fillUp(S));
		turnMainPanel.add(cbTurnMain);
		
		tfVolumeTurnMain = GUIFactory.getVolumeSlider();
		turnMainPanel.add(tfVolumeTurnMain);
		
		centerPanel.add(turnMainPanel);
		
		//Logo panel
		JPanel logoPanel = GUIFactory.getTransparentPanel();
		
		lblLogo = new JLabel("Logo:");
		logoPanel.add(lblLogo);
		
		cbLogo = new JComboBox(fillUp(S));
		logoPanel.add(cbLogo);
		
		tfVolumeLogo = GUIFactory.getVolumeSlider();
		logoPanel.add(tfVolumeLogo);
		
		centerPanel.add(logoPanel);
		centerPanel.setOpaque(false);
		this.setOpaque(false);
	}
	

	private void makeManagers(){
		am = new LayerManager();
		gm = new GaitManager();
		waM = new WalkAlongManager();
		sm = new SoundManager();
	}
	
	private Object[] fillUp(String kindOfContent){
		List<File> list = new ArrayList<>();
		switch(kindOfContent){
		case "sound":
			list = sm.getSoundList();
			break;
		case "gait":
			list = gm.getSoundList();
			break;
		case "ambient":
			list = am.getSoundList();
			break;
		case "walkalong":
			list = waM.getSoundList();
			break;
		}
		
		
		return list.toArray();
	}
	
	private void saveProject(){
		String name = projectName.getText();
		ProjectManager<?> pm = new ProjectManager<>();
		project = new Project<>(name);
		int volume = tfVolumeContin.getValue();
		project.setContinMap(pm.makeMap(cbContin.getSelectedItem(), volume));
		volume = tfVolumeFootLFast.getValue();
		project.setFootLeftFastMap(pm.makeMap(cbFootLFast.getSelectedItem(), volume));
		volume = tfVolumeFootL.getValue();
		project.setFootLeftMap(pm.makeMap(cbFootL.getSelectedItem(),volume));
		volume = tfVolumeFootRFast.getValue();
		project.setFootRightFastMap(pm.makeMap(cbFootRFast.getSelectedItem(), volume));
		volume = tfVolumeFootR.getValue();
		project.setFootRightMap(pm.makeMap(cbFootR.getSelectedItem(), volume));
		volume = tfVolumeStanding.getValue();
		project.setStandingMap(pm.makeMap(cbStanding.getSelectedItem(), volume));
		//volume = tfVolumeTurnLAmbient.getValue();
		//project.setTurnLeftAmbMap(pm.makeMap(cbTurnLAmb.getSelectedItem(), volume));
		//volume = tfVolumeTurnRAmbient.getValue();
		//project.setTurnRightAmbMap(pm.makeMap(cbTurnRAmb.getSelectedItem(), volume));
		//volume = Integer.parseInt(tfVolumeTurnL.getText());
		//project.setTurnLeftObjectMap(pm.makeMap(cbTurnL.getSelectedItem(), volume));
		//volume = Integer.parseInt(tfVolumeTurnR.getText());
		//project.setTurnRightObjectMap(pm.makeMap(cbTurnR.getSelectedItem(), volume));
		volume = tfVolumePlaces.getValue();
		project.setPlacesMap(pm.makeMap(cbPlaces.getSelectedItem(), volume));
		volume = tfVolumeTurnMain.getValue();
		project.setTurnMainMap(pm.makeMap(cbTurnMain.getSelectedItem(), volume));
		volume = tfVolumeLogo.getValue();
		project.setLogoMap(pm.makeMap(cbLogo.getSelectedItem(), volume));
		
		ObjectSaver.saveObject(project, name, "projects");
	}
	
	public void loadProject(Project<E> project){
		final String v = "volume";
		projectName.setText(project.getName());
		tfVolumeFootL.setValue((int) project.getFootLeftMap().get(v));
		tfVolumeFootR.setValue((int) project.getFootRightMap().get(v));
		tfVolumeStanding.setValue((int) project.getStandingMap().get(v));
		//tfVolumeTurnL.setValue((int) project.getTurnLeftObjectMap().get(v));
		//tfVolumeTurnR.setValue((int) project.getTurnRightObjectMap().get(v));;
		tfVolumeFootLFast.setValue((int) project.getFootLeftFastMap().get(v));
		tfVolumeFootRFast.setValue((int) project.getFootRightFastMap().get(v));;
		//tfVolumeTurnLAmbient.setValue((int) project.getTurnLeftAmbMap().get(v));;
		//tfVolumeTurnRAmbient.setValue((int) project.getTurnRightAmbMap().get(v));
		tfVolumeContin.setValue((int) project.getContinMap().get(v));
		tfVolumePlaces.setValue((int) project.getPlacesMap().get(v));
		tfVolumeTurnMain.setValue((int) project.getTurnMainMap().get(v));
		tfVolumeLogo.setValue((int) project.getLogoMap().get(v));
		
		final String m = "movement";
		cbFootL.setSelectedItem(project.getFootLeftMap().get(m));;
		cbFootLFast.setSelectedItem(project.getFootLeftFastMap().get(m));
		cbFootR.setSelectedItem(project.getFootRightMap().get(m));
		cbFootRFast.setSelectedItem(project.getFootRightFastMap().get(m));
		cbStanding.setSelectedItem(project.getStandingMap().get(m));
		cbContin.setSelectedItem(project.getContinMap().get(m));
		//cbTurnL.setSelectedItem(project.getTurnLeftObjectMap().get(m));
		//cbTurnR.setSelectedItem(project.getTurnRightObjectMap().get(m));
		//cbTurnLAmb.setSelectedItem(project.getTurnLeftAmbMap().get(m));
		//cbTurnRAmb.setSelectedItem(project.getTurnRightAmbMap().get(m));
		cbPlaces.setSelectedItem(project.getPlacesMap().get(m));
		cbTurnMain.setSelectedItem(project.getTurnMainMap().get(m));
		cbLogo.setSelectedItem(project.getLogoMap().get(m));
		
	}
	
	private String[] singleEvents = {"Standing", "Turn_main", "Logo"};
	
	private void makeApp(){
		PlistFactory plist = new PlistFactory(project);
		File flowMachinePath = ObjectSaver.loadPath();
		if(flowMachinePath!=null){
			String path=flowMachinePath.toString();
			Path pListSource = Paths.get("./SoundList.plist");
			Path pListTarget = Paths.get(path + "/FlowMaschine2/SoundList.plist");
			try {
				Files.copy(pListSource, pListTarget, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			String wavFile;

			for(int i=0;i<3;i++){
				String sourceFile = (String) project.getSoundlist().get(i).get("sound");//wenn nothing selected, dann leere wav
				Path source;
				if(sourceFile.equals("nothing selected"))
					source = Paths.get("nosound.wav");
				else
					source = Paths.get(sourceFile);
				wavFile = path + "/FlowMaschine2/Soundfiles/" + singleEvents[i] +".wav";
				Path target = Paths.get(wavFile);
				try {
					Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			AudioController ac = new AudioController(project);
			try {
				ac.replaceAndWrite();
			} catch (IOException e1) {
				System.out.println("Error while writing AudioController.m to Project.");
				e1.printStackTrace();
			}
			
			SessionViewController svc = new SessionViewController(project);
			try {
				svc.getProjectSettings();
				svc.replaceAndWrite();
			} catch (IOException e1) {
				System.out.println("Error while writing SessionViewController.m to Project.");
				e1.printStackTrace();
			}
			
			for(Object g : project.getGaitList()){
				Gait gait = (Gait) g;
				for(Object s : gait.getSoundList()){
					String sound = s.toString();
					Path source = Paths.get(sound);
					wavFile = path + "/FlowMaschine2/Soundfiles/" + sound.substring(7);
					Path target = Paths.get(wavFile);
					try {
						Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			
			
			try{
		        Runtime terminal = Runtime.getRuntime();
		        String[] cmd = new String[]{"xcodebuild -project " +path +"/FlowMaschine2.xcodeproj"};
		        Process process = terminal.exec(cmd);
		        BufferedReader read = new BufferedReader (new InputStreamReader(process.getInputStream()));
		        String s;
		        while ((s = read.readLine()) != null)
		        {
		            System.out.println(s);
		        }
		          //DataOutputStream dOut = new DataOutputStream(process.getOutputStream());
		          //dOut.writeChars("ls -al  \n");
		              
		        } 
		         catch (IOException e1) 
		         {
		            // TODO Auto-generated catch block
		        System.out.println("Fehler starten der Console !!!" + e1.toString());
		        }
		}
	}
}

