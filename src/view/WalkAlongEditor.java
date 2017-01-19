package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import controller.LevelManager;
import controller.ObjectSaver;
import model.WalkAlong;

import java.awt.GridBagLayout;

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

public class WalkAlongEditor<E> extends JPanel {

	/**
	 * 
	 */
	private static final String WEST_TEXT = "<html><body>Example<br>Level count: 4<br>Reset after: 21<br>start after: 5<br>(all levelsets)</body></html>";
	private static final String GIF_PATH = "/gifs/walkAlong.gif";
	private final ImageIcon RANDOM_ICON = new ImageIcon(getClass().getResource(GIF_PATH));
	private static final long serialVersionUID = 1L;
	private JPanel northPanel;
	private JTextField tfName;
	private JFormattedTextField tfEndAfter;
	
	private JPanel panelLevel;
	private int levelCount;
	private List<JComboBox<E>> cbLevelList;
	private List<JTextField> tfStepList;
	private JSpinner spinner;
	
	/**
	 * Create the panel.
	 */
	public WalkAlongEditor() {
		levelCount = 1;
		cbLevelList = new ArrayList<>();
		tfStepList = new ArrayList<>();
		
		//main layout
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		
		//North panel
		northPanel = GUIFactory.getTransparentPanel();
		northPanel.setLayout(new FlowLayout());
		
		JLabel lblWalkAlong = new JLabel("Walk Along name:");
		tfName = new JTextField();
		tfName.setColumns(10);
		northPanel.add(lblWalkAlong);
		northPanel.add(tfName);
		this.add(northPanel, BorderLayout.NORTH);
		
		//East/Btn panel
		JPanel eastPanel = GUIFactory.getBtnPanel();
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser();
			}
		});
	
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkBeforeSave())
					saveWalkAlong();
				else
					JOptionPane.showMessageDialog(null, "Please type name and count of steps between levels.", "Cannot save", JOptionPane.ERROR_MESSAGE, null);
			}
		});
		
		eastPanel.add(btnLoad);
		eastPanel.add(btnSave);
		this.add(eastPanel, BorderLayout.EAST);

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
		
		//Center panel
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		BorderLayout centerLayout = new BorderLayout();
		centerPanel.setLayout(centerLayout);
		centerPanel.setBorder(new EmptyBorder(50,100,50,100));
		this.add(centerPanel, BorderLayout.CENTER);
		
		//top of center panel
		JPanel leCountReset = GUIFactory.getTransparentPanel();
		leCountReset.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblLevelCount = new JLabel("Level count:");
		
		SpinnerModel spinnerModel = new SpinnerNumberModel(1,1,5,1);
		spinner = new JSpinner(spinnerModel);
		spinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				levelCount = (int)spinner.getValue();
				newLevelRow();
			}
		});
		
		JLabel lblEndAfter = new JLabel("Reset after steps:");
		
		tfEndAfter = new JFormattedTextField();
		tfEndAfter.setColumns(3);
		tfEndAfter.setValue(new Integer(0));
		
		leCountReset.add(lblLevelCount);
		leCountReset.add(spinner);
		leCountReset.add(lblEndAfter);
		leCountReset.add(tfEndAfter);
		centerPanel.add(leCountReset, BorderLayout.NORTH);
		
		//level panel
		panelLevel = GUIFactory.getTransparentPanel();
		panelLevel.setLayout(new BoxLayout(panelLevel, BoxLayout.PAGE_AXIS));
		centerPanel.add(panelLevel, BorderLayout.CENTER);
		
		newLevelRow();
	}
	
	private void newLevelRow(){
		panelLevel.removeAll();
		cbLevelList.clear();
		tfStepList.clear();
		for(int i=0; i<levelCount;i++){
			JPanel level = GUIFactory.getTransparentPanel();
			level.setLayout(new FlowLayout(FlowLayout.LEFT));
			JLabel lblLevelset = new JLabel("Levelset:");
			LevelManager lm = new LevelManager();
			JComboBox<E> comboBox = (JComboBox<E>) new JComboBox<>(lm.getSoundList().toArray());
			JLabel lblEvery = new JLabel("start after");
			JFormattedTextField tfSteps = new JFormattedTextField();
			tfSteps.setValue(new Integer(0));
			tfSteps.setColumns(3);
			JLabel lblSteps = new JLabel("steps");
			
			cbLevelList.add(comboBox);
			tfStepList.add(tfSteps);
			
			level.add(lblLevelset);
			level.add(comboBox);
			level.add(lblEvery);
			level.add(tfSteps);
			level.add(lblSteps);
			
			panelLevel.add(level);
		}
		
		panelLevel.validate();
		panelLevel.repaint();
	}

	private void saveWalkAlong(){
		String name = tfName.getText();
		WalkAlong walkAlong = new WalkAlong(name,levelCount);
		int [] steadyStepLevels = new int[levelCount+1];
		int endsAfter = Integer.parseInt(tfEndAfter.getText());
		if(endsAfter == 0)
			steadyStepLevels[levelCount] = 5000;
		else
			steadyStepLevels[levelCount] = endsAfter;
		
		for(int i=0; i<levelCount; i++){
			Map<String, Object> levelMap = new HashMap<String, Object>();
			levelMap.put("level", cbLevelList.get(i).getSelectedItem());
			int steps = Integer.parseInt(tfStepList.get(i).getText());//TODO unnötig, wird in Array gespeichert
			levelMap.put("steps", steps);
			steadyStepLevels[i] = Integer.parseInt(tfStepList.get(i).getText());
			
			walkAlong.addLevelToList(levelMap);
		}
		walkAlong.setStartAfter(steadyStepLevels);
		ObjectSaver.saveObject(walkAlong, name, "walkalongs");
	}
	
	private void loadWalkAlong(WalkAlong wa){
		levelCount = wa.getLevelCount();
		tfName.setText(wa.getName());
		tfEndAfter.setText(""+wa.getStartAfter()[levelCount]);
		spinner.setValue(levelCount);
		
		for(int i=0; i<levelCount; i++){
			int steps = (int)getMapParts(wa.getlevelMapList().get(i), "steps");
			tfStepList.get(i).setText("" + steps);
			cbLevelList.get(i).setSelectedItem(getMapParts(wa.getlevelMapList().get(i), "level"));
		}
	}
	
	private Object getMapParts(Map<String, Object> map, String name){
		Object object = map.get(name);
		return object;
	}
	
	public void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		File dir = new File("walkalongs");
		chooser.setCurrentDirectory(dir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Choose a WalkAlong:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			WalkAlong wa = (WalkAlong) ObjectSaver.loadObject(chooser.getSelectedFile().getName(), "walkalongs");
		    loadWalkAlong(wa);
		}else{
		      System.out.println("No Selection ");
		}
	}
	
	private boolean checkBeforeSave(){
		boolean nameGiven = false;
		boolean timesGiven = false;
		int counter = 0;
		
		if(!tfName.getText().equals(""))
			nameGiven = true;
		
		for(JTextField tf : tfStepList){
			if(!tf.getText().equals(""))
				counter++;
		}
		
		if(counter == tfStepList.size()){
			timesGiven = true;
		}
		
		if(nameGiven && timesGiven)
			return true;
		else
			return false;
	}
}
