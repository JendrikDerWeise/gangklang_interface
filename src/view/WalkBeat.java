package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.JSpinner;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import controller.ObjectSaver;
import controller.SoundManager;
import model.Gait;
import model.WalkAlong;

import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.NumberFormat;
import java.beans.PropertyChangeEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class WalkBeat extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String RANDOM_TEXT = "<html><body>Example<br>\"random play\"<br></body></html>";
	private static final String CONTIN_TEXT = "<html><body>Example<br>\"continous\"<br></body></html>";
	private static final String EVERYX_TEXT = "<html><body>Example<br>\"play every 3 step\"<br></body></html>";
	private static final String RANDOM_GIF = "/gifs/randomWalkBeat.gif";
	private static final String CONTIN_GIF = "/gifs/continWalkBeat.gif";
	private static final String EVERYX_GIF = "/gifs/everyXWalkbeat.gif";
	private final ImageIcon RANDOM_ICON = new ImageIcon(getClass().getResource(RANDOM_GIF));
	private final ImageIcon CONTIN_ICON = new ImageIcon(getClass().getResource(CONTIN_GIF));
	private final ImageIcon EVERYX_ICON = new ImageIcon(getClass().getResource(EVERYX_GIF));

	private JPanel soundsPanel;
	private JTextField textFieldName;
	private JTextField txtEnterSteps;
	
	private JLabel lblGaitName;
	private JLabel lblNumberOfSounds;
	private JLabel lblSoundNumber;
	private JSpinner spSoundNumber;
	private JSpinner spinnerSoundCount;
	private JComboBox<Object> comboBox;
	private List<JComboBox> comboBoxList;
	
	private int soundCountLeft;
	private JButton btnLoad;

	
	/**
	 * Create the panel.
	 */
	public WalkBeat() {
		soundCountLeft = 1;
		comboBoxList = new ArrayList<>();
		
		//setting up general layout
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		JPanel northPanel = GUIFactory.getTransparentPanel();
		northPanel.setLayout(new FlowLayout());
		this.add(northPanel, BorderLayout.NORTH);
		
		//North panel for name
		lblGaitName = new JLabel("WalkBeat name:");
		northPanel.add(lblGaitName);
		
		textFieldName = new JTextField();
		northPanel.add(textFieldName);
		textFieldName.setColumns(10);
		
		//East panel for buttons
		JPanel eastPanel = GUIFactory.getBtnPanel();
		
		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(checkBeforeSave())
					saveGait();
			}
		});
		
		btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser();
			}
		});
		eastPanel.add(btnLoad);
		eastPanel.add(btnSave);
		this.add(eastPanel, BorderLayout.EAST);
		
		//west panel
		JPanel westPanel = GUIFactory.getTransparentPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.PAGE_AXIS));
		
		JLabel text = new JLabel(RANDOM_TEXT);
		text.setBorder(BorderFactory.createEmptyBorder(50, 10, 0, 0));
		westPanel.add(text, BorderLayout.NORTH);
		
		JLabel gif = new JLabel(RANDOM_ICON);
		gif.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		westPanel.add(gif, BorderLayout.CENTER);
		
		this.add(westPanel, BorderLayout.WEST);
		
		//CenterPanel
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		centerPanel.setBorder(new EmptyBorder(50,100,50,100));
		this.add(centerPanel, BorderLayout.CENTER);
		
		//WalkBeat variant panel
		JPanel wbVariantPanel = GUIFactory.getTransparentPanel();
		wbVariantPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		String gaitVariant[] = {"random play", "continous", "every X steps play sound #" };
		comboBox = new JComboBox<Object>(gaitVariant);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switch(comboBox.getSelectedItem().toString()){
				case "random play":
					text.setText(RANDOM_TEXT);
					gif.setIcon(RANDOM_ICON);
					everyXSetter(false);
					break;
				case "continous":
					text.setText(CONTIN_TEXT);
					gif.setIcon(CONTIN_ICON);
					break;
				case "every X steps play sound #":
					text.setText(EVERYX_TEXT);
					gif.setIcon(EVERYX_ICON);
					everyXSetter(true);
					break;
				}
			}
		});
		
		NumberFormat format = NumberFormat.getInstance();
	    NumberFormatter formatter = new NumberFormatter(format);
	    formatter.setValueClass(Integer.class);
	    formatter.setMinimum(0);
	    formatter.setMaximum(Integer.MAX_VALUE);
	    formatter.setAllowsInvalid(false);
	    formatter.setCommitsOnValidEdit(true);
		txtEnterSteps = new JFormattedTextField(format);
		txtEnterSteps.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(txtEnterSteps.getText().equals("enter steps")&&txtEnterSteps.isEnabled())
					txtEnterSteps.setText("");
			}
		});
		txtEnterSteps.setText("enter steps");
		txtEnterSteps.setColumns(10);
		lblSoundNumber = new JLabel("Sound number:");
		SpinnerNumberModel spinnerModelSoundNumber = new SpinnerNumberModel(1,1,soundCountLeft,1);
		spSoundNumber = new JSpinner(spinnerModelSoundNumber);
		everyXSetter(false);
		
		wbVariantPanel.add(comboBox);
		wbVariantPanel.add(txtEnterSteps);
		wbVariantPanel.add(lblSoundNumber);
		wbVariantPanel.add(spSoundNumber);
		
		
		//Number of sounds panel
		JPanel numberOfSoundsPanel = GUIFactory.getTransparentPanel();
		numberOfSoundsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		lblNumberOfSounds = new JLabel("Number of sounds:");
		numberOfSoundsPanel.add(lblNumberOfSounds);
		
		SpinnerModel spinnerModelSoundCount = new SpinnerNumberModel(1,1,5,1);
		spinnerSoundCount = new JSpinner(spinnerModelSoundCount);
		spinnerSoundCount.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				soundCountLeft = (int)spinnerSoundCount.getValue();
				addComboBoxForSound();
				spinnerModelSoundNumber.setMaximum(soundCountLeft);
				if((int)spSoundNumber.getValue() > soundCountLeft)
					spSoundNumber.setValue(1);
			}
		});
		numberOfSoundsPanel.add(spinnerSoundCount);
		
		centerPanel.add(Box.createVerticalStrut(20));
		centerPanel.add(numberOfSoundsPanel);
		centerPanel.add(Box.createVerticalStrut(-80));
		centerPanel.add(wbVariantPanel);
		centerPanel.add(Box.createVerticalStrut(-80));
		
		soundsPanel = GUIFactory.getTransparentPanel();
		soundsPanel.setLayout(new BoxLayout(soundsPanel, BoxLayout.PAGE_AXIS));
		centerPanel.add(soundsPanel);
		centerPanel.add(Box.createVerticalStrut(-150));
		centerPanel.add(Box.createGlue());
		
		addComboBoxForSound();
		this.setOpaque(false);
	}
	
	private void everyXSetter(boolean b){
		txtEnterSteps.setEnabled(b);
		spSoundNumber.setEnabled(b);
		lblSoundNumber.setEnabled(b);
	}
	
	private void addComboBoxForSound(){
		soundsPanel.removeAll();
		comboBoxList.clear();
		SoundManager sm = new SoundManager();
		for(int i=0; i<soundCountLeft; i++){
			JPanel singleSoundPanel = GUIFactory.getTransparentPanel();
			singleSoundPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			
			JLabel soundNr = new JLabel("Sound #" + (i+1));
			
			JComboBox<?> comboBox = new JComboBox(sm.getSoundList().toArray());
			comboBox.setBounds(27, 6+(i*30), 154, 27);
			comboBoxList.add(comboBox);
			singleSoundPanel.add(soundNr);
			singleSoundPanel.add(comboBoxList.get(i));
			soundsPanel.add(singleSoundPanel);
			
		}
		soundsPanel.validate();
		soundsPanel.repaint();
	}
	
	private void saveGait(){
		String name = textFieldName.getText();
		int soundCount = (int)spinnerSoundCount.getValue();
		String gaitVariant = (String)comboBox.getSelectedItem();
		Gait gait = new Gait(name, soundCount, gaitVariant);
		if(comboBox.getSelectedItem().equals("every X steps play sound #")){
			gait.setNumberOfSteps(Integer.parseInt(txtEnterSteps.getText()));
			gait.setNumberOfSoundStepTrigger((int)spSoundNumber.getValue());
		}
		for(JComboBox cb : comboBoxList)
			gait.toSoundList(cb.getSelectedItem());
		ObjectSaver.saveObject(gait, name, "gaits");
	}
	
	private void loadGait(Gait g){
		textFieldName.setText(g.getName());
		spinnerSoundCount.setValue(g.getNumberOfSounds());
		comboBox.setSelectedItem(g.getGaitVariant());
		if(comboBox.getSelectedItem().equals("every X steps play sound #")){
			txtEnterSteps.setText("" + g.getNumberOfSteps());
			spSoundNumber.setValue((int)g.getNumberOfSoundStepTrigger());
		}
			
		for(int i=0; i<g.getSoundList().size();i++){
			comboBoxList.get(i).setSelectedItem(g.getSoundList().get(i));
		}
	}
	
	public void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		File dir = new File("gaits");
		chooser.setCurrentDirectory(dir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Choose a Gait:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			Gait g = (Gait) ObjectSaver.loadObject(chooser.getSelectedFile().getName(), "gaits");
		    loadGait(g);
		}else{
		      System.out.println("No Selection ");
		}
	}
	
	private boolean checkBeforeSave(){
		
		if(textFieldName.getText().equals("")){
			JOptionPane.showMessageDialog(null, "Please type a name for the WalkBeat!", "NOT saved: Name is missing", JOptionPane.ERROR_MESSAGE, null);
			return false;
		}
		
		for(JComboBox box : comboBoxList){
			File file = (File)box.getSelectedItem();
			if(file.getName().equals("nothing selected")){
				JOptionPane.showMessageDialog(null, "Please select a sound for every number!", "NOT saved: Select sounds", JOptionPane.ERROR_MESSAGE, null);
				return false;
			}
		}
		
		if(comboBox.getSelectedItem().equals("every X steps play sound #")){
			if(txtEnterSteps.getText().equals("enter steps")){
				JOptionPane.showMessageDialog(null, "Please a number of steps!", "NOT saved: Step count is missing", JOptionPane.ERROR_MESSAGE, null);
				return false;
			}
			if(txtEnterSteps.getText().equals(""))
				return false;
		}
		
		return true;
	}
}


