package view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

import controller.ObjectSaver;
import controller.ProjectManager;
import model.Project;

import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.io.File;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

@SuppressWarnings("serial")
public class SavedProjects extends JPanel {

	private ICallback mCallback;
	private ProjectManager pm;

	/**
	 * Create the frame.
	 * @param <E>
	 */
	public <E> SavedProjects(ICallback callback) {
		mCallback = callback;
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		
		JLabel lblSavedProjects = new JLabel("Saved Projects:");
		lblSavedProjects.setHorizontalAlignment(SwingConstants.LEFT);
		this.add(lblSavedProjects, BorderLayout.WEST);
		
		pm = new ProjectManager<>();
		JList list = new JList(pm.getListModel());	
		GridLayout layout = new GridLayout(1,0);
		JPanel centerPanel = GUIFactory.getTransparentPanel();
		centerPanel.setLayout(layout);
		centerPanel.setBorder(new EmptyBorder(100,50,100,50));
		centerPanel.add(new JScrollPane(list));
		
		//east panel buttons
		JPanel eastPanel = GUIFactory.getBtnPanel();
		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openProject(list);
			}
		});
		
		eastPanel.add(btnOpen);
		
		JButton btnDelete = new JButton("Delete");
		btnDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String projectName = pm.getSoundList().get(list.getSelectedIndex()).toString();
				int reply = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete " + projectName + "?", "Delete?",  JOptionPane.YES_NO_OPTION);
				if (reply == JOptionPane.YES_OPTION){
					pm.deleteFile((File) list.getSelectedValue());
					list.setModel(pm.getListModel());
					list.validate();
					list.repaint();
				}
			}
		});
		eastPanel.add(btnDelete);
		
		this.add(eastPanel, BorderLayout.EAST);
		this.add(centerPanel, BorderLayout.CENTER);
		this.setOpaque(false);
	}
	
	
	private <E> void openProject(JList<?> list){
		File file = (File) list.getSelectedValue();
		Project<E> project = (Project<E>) ObjectSaver.loadObject(file.getName(), "projects");
	    mCallback.openSavedProject(project);
	}
}
