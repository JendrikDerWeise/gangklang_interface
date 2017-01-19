package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controller.LayerManager;
import controller.GaitManager;
import controller.ObjectSaver;
import model.Level;
import model.WalkAlong;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;

public class LevelSet<E> extends JPanel {

	/**
	 * 
	 */
	private static final String WEST_TEXT = "<html><body>Example<br>WalkBeats: 3<br>Soundcarpets: 2<br>WalkBeat 1: left<br>WalkBeat 2: right<br>WalkBeat 3: right</body></html>";
	private static final String GIF_PATH = "/gifs/levelset.gif";
	private final ImageIcon RANDOM_ICON = new ImageIcon(getClass().getResource(GIF_PATH));
	private static final long serialVersionUID = 1L;
	private JPanel northPanel;
	private JPanel panelGaits;
	private JPanel panelAmbients;
	private JTextField tfName;
	private JSpinner spinnerGaits;
	private JSpinner spinnerAmbients;
	
	private GridBagLayout gbl_panelGaits;
	
	private int gaitCount;
	private int ambientCount;
	
	private List<JComboBox<E>> cbGaitList;
	private List<JComboBox<String>> cbFootList;
	private List<JSlider> tfVolumeList;
	
	private List<JComboBox<E>> cbAmbienttList;
	private List<JSlider> tfAmbientVolumeList;
	private JButton btnLoad;


	/**
	 * Create the panel.
	 */
	public LevelSet() {
		gaitCount = 1;
		ambientCount = 1;
		cbGaitList = new ArrayList<>();
		cbFootList = new ArrayList<>();
		tfVolumeList = new ArrayList<>();
		cbAmbienttList = new ArrayList<>();
		tfAmbientVolumeList = new ArrayList<>();
		
		//set main layout
		this.setLayout(new BorderLayout());
		this.setBorder(new EmptyBorder(10,10,10,10));
		
		//set north panel
		northPanel = GUIFactory.getTransparentPanel();
		northPanel.setLayout(new FlowLayout());
		
		JLabel lblLevelName = new JLabel("Level name:");
		tfName = new JTextField();
		tfName.setColumns(10);
		
		northPanel.add(lblLevelName);
		northPanel.add(tfName);
		this.add(northPanel, BorderLayout.NORTH);

		//west panel
		JPanel westPanel = GUIFactory.getTransparentPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.PAGE_AXIS));

		JLabel text = new JLabel(WEST_TEXT);
		text.setBorder(BorderFactory.createEmptyBorder(50, 10, 0, 0));
		westPanel.add(text, BorderLayout.NORTH);

		JLabel gif = new JLabel(RANDOM_ICON);
		gif.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		westPanel.add(gif, BorderLayout.CENTER);

		this.add(westPanel, BorderLayout.WEST);
	
		//Center Panel
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		BorderLayout centerLayout = new BorderLayout();
		centerPanel.setLayout(centerLayout);
		centerPanel.setBorder(new EmptyBorder(50,100,50,100));
		
		//Gait & Ambient count
		JPanel numberOfStuff = GUIFactory.getTransparentPanel();
		numberOfStuff.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblGaits = new JLabel("WalkBeats:");
		
		SpinnerModel spinnerModelGaits = new SpinnerNumberModel(1,0,5,1);
		spinnerGaits = new JSpinner(spinnerModelGaits);
		spinnerGaits.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				gaitCount = (int)spinnerGaits.getValue();
				newGaitsRow();
			}
		});
		
		JLabel lblAmbients = new JLabel("Soundcarpets:");
		
		SpinnerModel spinnerModelAmbients = new SpinnerNumberModel(1,0,5,1);
		spinnerAmbients = new JSpinner(spinnerModelAmbients);
		spinnerAmbients.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				ambientCount = (int)spinnerAmbients.getValue();
				newAmbientRow();
			}
		});
		
		numberOfStuff.add(lblGaits);
		numberOfStuff.add(spinnerGaits);
		numberOfStuff.add(lblAmbients);
		numberOfStuff.add(spinnerAmbients);
		centerPanel.add(numberOfStuff, BorderLayout.NORTH);
		
		//GridLayout im Borderlayout im Center Layout
		JPanel wbAndScPanel = GUIFactory.getTransparentPanel();
		wbAndScPanel.setLayout(new GridLayout(2,1));
		centerPanel.add(wbAndScPanel, BorderLayout.CENTER);
		
		//WalkBeats Panel
		panelGaits = GUIFactory.getTransparentPanel();
		panelGaits.setLayout(new BoxLayout(panelGaits, BoxLayout.PAGE_AXIS));
		wbAndScPanel.add(panelGaits);
		
		
		//Soundcarpet panel
		panelAmbients = GUIFactory.getTransparentPanel();
		panelAmbients.setLayout(new BoxLayout(panelAmbients, BoxLayout.PAGE_AXIS));
		wbAndScPanel.add(panelAmbients);
		
		//button panel
		JPanel eastPanel = GUIFactory.getBtnPanel();
		
		btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser();
			}
		});
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkBeforeSave())
					saveLevel();
				else
					JOptionPane.showMessageDialog(null, "Please type name and volume of Gaits and Ambients.", "Cannot save", JOptionPane.ERROR_MESSAGE, null);
			}
		});
		
		eastPanel.add(btnLoad);
		eastPanel.add(btnSave);
		
		this.add(centerPanel, BorderLayout.CENTER);
		
		this.add(eastPanel, BorderLayout.EAST);
		
		newGaitsRow();
		newAmbientRow();
		this.setOpaque(false);
	}
	
	private void newGaitsRow(){
		panelGaits.removeAll();
		cbGaitList.clear();
		cbFootList.clear();
		tfVolumeList.clear();
		GaitManager gm = new GaitManager();
		for(int i = 0; i<gaitCount; i++){
			JPanel gaitPanel = GUIFactory.getTransparentPanel();
			gaitPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel lblGaitNumber = new JLabel("WalkBeat " + (i+1));
			JComboBox<E> cbGaits = new JComboBox<E>((E[]) gm.getSoundList().toArray());
			JLabel lblFoot = new JLabel("Foot");
			String footVariant[] = {"left", "right" };
			JComboBox<String> cbFoot = new JComboBox<>(footVariant);
			JLabel lblVolume = new JLabel("Volume");
			JSlider tfVolume = GUIFactory.getVolumeSlider();
			
			cbGaitList.add(cbGaits);
			cbFootList.add(cbFoot);
			tfVolumeList.add(tfVolume);
			
			gaitPanel.add(lblGaitNumber);
			gaitPanel.add(cbGaits);
			gaitPanel.add(lblFoot);
			gaitPanel.add(cbFoot);
			gaitPanel.add(lblVolume);
			gaitPanel.add(tfVolume);
			
			panelGaits.add(gaitPanel);
		}
	
		panelGaits.validate();
		panelGaits.repaint();
	}
	
	private void newAmbientRow(){
		panelAmbients.removeAll();
		cbAmbienttList.clear();
		tfAmbientVolumeList.clear();
		LayerManager am = new LayerManager();
		for(int i = 0; i<ambientCount; i++){
			JPanel ambientPanel = GUIFactory.getTransparentPanel();
			ambientPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JLabel lblAmbientNumber = new JLabel("Soundcarpet " + (i+1));
			JComboBox<E> cbAmbient = new JComboBox<E>((E[]) am.getSoundList().toArray());
			JLabel lblVolume = new JLabel("Volume");
			JSlider tfVolume = GUIFactory.getVolumeSlider();
			
			cbAmbienttList.add(cbAmbient);
			tfAmbientVolumeList.add(tfVolume);
			
			ambientPanel.add(lblAmbientNumber);
			ambientPanel.add(cbAmbient);
			ambientPanel.add(lblVolume);
			ambientPanel.add(tfVolume);
			
			panelAmbients.add(ambientPanel);
		}
		
		panelAmbients.validate();
		panelAmbients.repaint();
	}
	
	private void saveLevel(){
		String name = tfName.getText();
		Level level = new Level(name, gaitCount, ambientCount);
	
		for(int i=0; i<gaitCount;i++){
			Map<String,Object> gaitMap = new HashMap<>();
			gaitMap.put("gait", cbGaitList.get(i).getSelectedItem());
			gaitMap.put("foot", cbFootList.get(i).getSelectedItem());
			gaitMap.put("volume", tfVolumeList.get(i).getValue());
			switch((String)cbFootList.get(i).getSelectedItem()){
				case "left":
					gaitMap.put("lVol", "0.1");
					gaitMap.put("rVol", tfVolumeList.get(i).getValue());
					break;
				case "right":
					gaitMap.put("rVol", "0.1");
					gaitMap.put("lVol", tfVolumeList.get(i).getValue());
			}
			
			level.addGaitToList(gaitMap);
		}
		
		for(int i=0; i<ambientCount;i++){
			Map<String,Object> ambientMap = new HashMap<>();
			ambientMap.put("ambient", cbAmbienttList.get(i).getSelectedItem());
			ambientMap.put("volume", tfAmbientVolumeList.get(i).getValue());
			
			level.addAmbientToList(ambientMap);
		}
		
		ObjectSaver.saveObject(level, name, "levels");
	}
	
	private void loadLevel(Level l){
		spinnerGaits.setValue(l.getGaitCount());
		spinnerAmbients.setValue(l.getAmbientCount());
		tfName.setText(l.getName());
		
		for(int i=0;i<gaitCount;i++){
			cbGaitList.get(i).setSelectedItem(l.getGaitList().get(i).get("gait"));
			cbFootList.get(i).setSelectedItem(l.getGaitList().get(i).get("foot"));
			tfVolumeList.get(i).setValue((int)l.getGaitList().get(i).get("volume"));
		}
		
		for(int i=0;i<ambientCount;i++){
			cbAmbienttList.get(i).setSelectedItem(l.getAmbientList().get(i).get("ambient"));
			tfAmbientVolumeList.get(i).setValue((int)l.getAmbientList().get(i).get("volume"));
		}
	}
	
	private boolean checkBeforeSave(){
		boolean nameGiven = false;
		
		if(!tfName.getText().equals(""))
			nameGiven = true;
		
		
		if(gaitCount>0){
			for(JComboBox<E> gaitBox : cbGaitList){
				File file = (File)gaitBox.getSelectedItem();
				if(file.getName().equals("nothing selected"))
					return false;
			}
		}
		
		if(ambientCount>0){
			for(JComboBox<E> ambientBox : cbAmbienttList){
				File file = (File)ambientBox.getSelectedItem();
				if(file.getName().equals("nothing selected"))
					return false;
			}
		}
		
		if(nameGiven){
			if(gaitCount == 0 && ambientCount == 0)
				return false;
			return true;
		}else
			return false;
	}
	
	public void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		File dir = new File("levels");
		chooser.setCurrentDirectory(dir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Choose a LevelSet:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			Level l = (Level) ObjectSaver.loadObject(chooser.getSelectedFile().getName(), "levels");
		    loadLevel(l);
		}else{
		      System.out.println("No Selection ");
		}
	}

}
