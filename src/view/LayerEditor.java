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
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeListener;

import controller.ObjectSaver;
import controller.SoundManager;
import model.Layer;
import model.LayerSoundtrack;
import model.WalkAlong;

import javax.swing.event.ChangeEvent;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.beans.PropertyChangeEvent;

public class LayerEditor<E> extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel layerPanel;
	private JSpinner spinner;
	private JSpinner spinner_1;
	private GridBagLayout gbl_contentPane;
	private int layers;
	private int soundCount;
	private JTextField textFieldName;
	private List<JFormattedTextField> tfListMin;//for silence
	private List<JFormattedTextField> tfListMax;
	private JButton btnSave;
	
	private List<List<JComboBox<E>>> comboBoxList;
	private boolean savePossible;

	/**
	 * Create the panel.
	 */
	public LayerEditor() {
		comboBoxList = new ArrayList<>();
		tfListMin = new ArrayList<>();
		tfListMax = new ArrayList<>();
		layers = 1;
		soundCount = 1;
		
		//mainPanel layout
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		this.setOpaque(false);
		
		//north panel
		JPanel northPanel = GUIFactory.getTransparentPanel();
		northPanel.setLayout(new FlowLayout());
		
		JLabel lblLayerName = new JLabel("Soundcarpet name:");
		textFieldName = new JTextField();
		textFieldName.setColumns(10);
		
		northPanel.add(lblLayerName);
		northPanel.add(textFieldName);
		this.add(northPanel, BorderLayout.NORTH);
		
		//Button panel
		JPanel eastPanel = GUIFactory.getBtnPanel();
		
		btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkBeforeSave();
				if(savePossible)
					saveAmbient();
				else
					JOptionPane.showMessageDialog(null, "Please type name, silence between sounds and choose files for every field.", "Cannot save", JOptionPane.ERROR_MESSAGE, null);
			}
		});
		btnSave.setEnabled(false);
		
		JButton btnLoad = new JButton("Load");
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser();
			}
		});
		
		eastPanel.add(btnLoad);
		eastPanel.add(btnSave);
		this.add(eastPanel, BorderLayout.EAST);
		
		//CenterPanel
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.PAGE_AXIS));
		centerPanel.setBorder(new EmptyBorder(50,100,50,100));
		this.add(centerPanel, BorderLayout.CENTER);
		
		//max sound panel
		JPanel maxSoundPanel = GUIFactory.getTransparentPanel();
		maxSoundPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JLabel lblMaxSoundsPer = new JLabel("Max. sounds per track:");
		
		SpinnerModel spinnerModel_1 = new SpinnerNumberModel(1,1,3,1);
		spinner_1 = new JSpinner(spinnerModel_1);
		spinner_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				soundCount = (int)spinnerModel_1.getValue();
				changeLayerAmount();
			}
		});
		maxSoundPanel.add(lblMaxSoundsPer);
		maxSoundPanel.add(spinner_1);
		centerPanel.add(maxSoundPanel);
		
		//LayerPanel
		layerPanel = GUIFactory.getTransparentPanel();
		layerPanel.setLayout(null);
		
		centerPanel.add(layerPanel);
		centerPanel.add(Box.createVerticalStrut(-150));
		centerPanel.add(Box.createGlue());
		
		//west panel
		JPanel westPanel = GUIFactory.getTransparentPanel();
		westPanel.setLayout(new BoxLayout(westPanel, BoxLayout.PAGE_AXIS));
		
		JLabel text = new JLabel("<html><body>Example<br>Silence min: 1000ms<br>Silence max: 2000ms<br>Sound count: 3</body></html>");
		text.setBorder(BorderFactory.createEmptyBorder(50, 10, 0, 0));
		westPanel.add(text, BorderLayout.NORTH);
		
		JLabel gif = new JLabel(new ImageIcon(getClass().getResource("/gifs/soundcarpet.gif")));
		gif.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
		westPanel.add(gif, BorderLayout.CENTER);
		
		this.add(westPanel, BorderLayout.WEST);
		
		//unused Soundtrack chooser
				/*JLabel lblSoundtrack = new JLabel("Soundtrack");
				//contentPane.add(lblSoundtrack, gbc_lblSoundtrack);
				
				SpinnerModel spinnerModel = new SpinnerNumberModel(1,1,5,1);
				spinner = new JSpinner(spinnerModel);
				spinner.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent e) {
						layers = (int)spinner.getValue();
						changeLayerAmount();
					}
				});
				//contentPane.add(spinner, gbc_spinner);
				*/
		
		changeLayerAmount();
	}
	
	private void changeLayerAmount(){
		layerPanel.removeAll();
		tfListMin.clear();
		tfListMax.clear();
		comboBoxList.clear();
		for(int i=0; i<layers; i++){
			int pos = i*60;
			JLabel lblLayer = new JLabel("Track " +(i+1) + ":");
			lblLayer.setBounds(6, pos, 61, 16);
			layerPanel.add(lblLayer);
			
			JLabel lblTimeMin = new JLabel("Silence between sounds (ms) min:");
			lblTimeMin.setBounds(100, pos, 230,16);
			layerPanel.add(lblTimeMin);
			
			JLabel lblTimeMax = new JLabel("max:");
			lblTimeMax.setBounds(380, pos, 200,16);
			layerPanel.add(lblTimeMax);
			
			JFormattedTextField tfMin = new JFormattedTextField();
			tfMin.setValue(new Integer(0));
			tfMin.setBounds(320, pos, 60, 26);
			tfMin.setColumns(10);
			tfListMin.add(tfMin);
			layerPanel.add(tfListMin.get(i));
			
			JFormattedTextField tfMax = new JFormattedTextField();
			tfMax.setValue(new Integer(0));
			tfMax.setBounds(420, pos, 60, 26);
			tfMax.setColumns(10);
			tfListMax.add(tfMax);
			layerPanel.add(tfListMax.get(i));
			tfMax.addFocusListener(new FocusListener(){
				int min;
				@Override
				public void focusGained(FocusEvent e) {
					if(!tfMin.getText().equals(""))
						min = Integer.parseInt(tfMin.getText());
					else
						min = 0;
				}

				@Override
				public void focusLost(FocusEvent e) {
					int max;
					if(!tfMax.getText().equals(""))
						max = Integer.parseInt(tfMax.getText());
					else
						max = 0;
					
					if(max<min || min==max){
						JOptionPane.showMessageDialog(null, "The value \"max\" has to be higher than \"min\".", "That´s impossible", JOptionPane.ERROR_MESSAGE, null);
						btnSave.setEnabled(false);
					}
					else
						btnSave.setEnabled(true);
				}
			});
			
			addComboBox(pos);
		}
		layerPanel.validate();
		layerPanel.repaint();
	}
	
	private void addComboBox(int pos){
		SoundManager sm = new SoundManager();
		List<JComboBox<E>> list = new ArrayList<>();
		for(int i=0; i<soundCount; i++){
			JComboBox<E> comboBox = new JComboBox<E>((E[]) sm.getSoundList().toArray());
			comboBox.setBounds(6+(i*175), pos+30, 170, 27);
			list.add(comboBox);
			layerPanel.add(list.get(i));
		}
		comboBoxList.add(list);
	}
	
	private void saveAmbient(){
		String name = textFieldName.getText();
		Layer<E> ambient = new Layer<E>(name, layers, soundCount);
		
		for(int i = 0; i<layers; i++){
			String strMS = tfListMin.get(i).getText();
			float silenceMin = Float.parseFloat(strMS);
			String strMS1 = tfListMax.get(i).getText();
			float silenceMax = Float.parseFloat(strMS1);
			LayerSoundtrack<E> layer = new LayerSoundtrack<E>(silenceMin,silenceMax);
			layer.setSoundList(comboBoxList.get(i));
			ambient.addLayerToList(layer);
		}
		
		ObjectSaver.saveObject(ambient, name, "soundcarpets");
	}
	
	private void loadAmbient(Layer<E> a){
		spinner.setValue(a.getLayersCount());
		spinner_1.setValue(a.getMaxSoundsCount());
		textFieldName.setText(a.getName());
		//layers = a.getLayersCount();
		//soundCount = a.getMaxSoundsCount();
		
		//changeLayerAmount();
		List<LayerSoundtrack<E>> layerList = a.getLayerList();
		for(int i=0; i<layers; i++){
			tfListMin.get(i).setText("" + layerList.get(i).getSilenceMin());
			tfListMax.get(i).setText("" + layerList.get(i).getSilenceMax());
			List<Object> soundList = layerList.get(i).getSoundList();
			for(int j=0; j<soundCount;j++){
				comboBoxList.get(i).get(j).setSelectedItem(soundList.get(j));
			}
		}
	}
	
	public void fileChooser(){
		JFileChooser chooser = new JFileChooser();
		File dir = new File("soundcarpets");
		chooser.setCurrentDirectory(dir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Choose a Soundcarpet:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
			Layer<E> a = (Layer<E>) ObjectSaver.loadObject(chooser.getSelectedFile().getName(), "soundcarpets");
		    loadAmbient(a);
		}else{
		      System.out.println("No Selection ");
		}
	}
	
	private void checkBeforeSave(){
		boolean nameGiven = false;
		boolean timesGiven = false;
		boolean wavsSelected = false;
		int counter = 0;
		
		if(!textFieldName.getText().equals(""))
			nameGiven = true;
		
		for(JTextField tf : tfListMin){
			if(!tf.getText().equals(""))
				counter++;
		}
		
		for(JTextField tf : tfListMax){
			if(!tf.getText().equals(""))
				counter++;
		}
		
		if(counter == tfListMin.size() + tfListMax.size()){
			timesGiven = true;
		}
		
		for(List<JComboBox<E>> list : comboBoxList){
			for(JComboBox<E> box : list){
				File file = (File)box.getSelectedItem();
				if(file.getName().equals("nothing selected")){
					wavsSelected = false;
					break;
				}else
					wavsSelected = true;
			}
		}
		
		
		if(nameGiven && timesGiven && wavsSelected)
			savePossible = true;
		else
			savePossible = false;
	}
}
