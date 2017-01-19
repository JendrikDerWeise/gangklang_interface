package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JInternalFrame;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;

import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import controller.SoundManager;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;

@SuppressWarnings("serial")
public class SoundsInLibrary extends JPanel {
	SoundManager sm;

	/**
	 * Create the frame.
	 */
	@SuppressWarnings("unchecked")
	public SoundsInLibrary() {
		sm = new SoundManager();
		this.setBorder(new EmptyBorder(10,40,10,10));
		this.setLayout(new BorderLayout());
		
		JLabel lblSoundsInYour = new JLabel("Sounds in your library:");
		this.add(lblSoundsInYour, BorderLayout.WEST);
		
		@SuppressWarnings("rawtypes")
		JList list = new JList();
		list.setModel(sm.getListModel());
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		GridLayout layout = new GridLayout(1,0);
		centerPanel.setLayout(layout);
		centerPanel.setBorder(new EmptyBorder(100,50,100,50));
		centerPanel.add(new JScrollPane(list));
		
		this.add(centerPanel, BorderLayout.CENTER);
		
		JPanel eastPanel = GUIFactory.getBtnPanel();
		JButton btnAddSound = new JButton("Add");
		btnAddSound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fileChooser();
				list.setModel(sm.getListModel());
				list.validate();
				list.repaint();
			}
		});
		eastPanel.add(btnAddSound);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String soundName = sm.getSoundList().get(list.getSelectedIndex()).toString();
				int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + soundName + "?", "Delete?",  JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION){
					sm.deleteFile((File) list.getSelectedValue());
					list.setModel(sm.getListModel());
					list.validate();
					list.repaint();
				}
			}
		});
		eastPanel.add(btnDelete);

		this.add(eastPanel, BorderLayout.EAST);
		this.setOpaque(false);
	}
	
	public void fileChooser(){
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Wave file", "wav");

		JFileChooser chooser = new JFileChooser();
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setFileFilter(filter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogTitle("Choose a soundfile:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		      
		      try {
				sm.copySoundFile(chooser.getSelectedFile());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
		      System.out.println("No Selection ");
		}
		
	}

}
