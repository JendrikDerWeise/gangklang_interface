package view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class Startscreen extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Startscreen() {
		this.setBorder(new EmptyBorder(10,10,10,10));
		this.setLayout(new BorderLayout());
		
		//West panel
		JPanel westPanel = GUIFactory.getTransparentPanel();
		westPanel.setLayout(new BorderLayout());
		JLabel flowmachineLabel = new JLabel(new ImageIcon((new ImageIcon(getClass().getResource("/images/foot.png")).getImage().getScaledInstance(150, 150, java.awt.Image.SCALE_SMOOTH))));
		flowmachineLabel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
		
		JLabel text = new JLabel("<html><body><h1 style=\"color: #008faf; text-align: center;\">Gangklang</h1><p style=\"text-align: center;\"><span style=\"color: #008faf;\">"
				+ "Copyright (c)2014 Hochschule Bremen</span></p>"
				+"<p style=\"text-align: center;\"><span style=\"color: #008faf;\">All rights reserved.</span></p></body></html>");
		text.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		//inner west panel
		JPanel innerPanelWest = GUIFactory.getTransparentPanel();
		innerPanelWest.setBorder(BorderFactory.createEmptyBorder(140,0,0,0));
		
		innerPanelWest.setLayout(new BoxLayout(innerPanelWest, BoxLayout.Y_AXIS));
		innerPanelWest.add(flowmachineLabel);
		innerPanelWest.add(text);
		
		westPanel.add(innerPanelWest, BorderLayout.CENTER);
		
		//inner center panel
		JPanel innerCenterPanel = GUIFactory.getTransparentPanel();
		innerCenterPanel.setLayout(new BorderLayout());
		
		JLabel textLabel = new JLabel("<html><body><h1 style=\"color: #008faf; text-align: left;\">Welcome to the interface for the iOS-App Gangklang</h1>"
				+ "<p style=\"text-align: left;\">The app takes your walking-movements from the sensors of your smartphone and generates out of this different events. "
				+ "This interface helps you, to give each of these events a sound of your choice. Please keep in mind, that you must have the smartphone in your right pocket with face foreward.</p>"
				+ "<p style=\"text-align: left;\">See below graphic to understand how the single components of the app are binded together.</p></body></html>");

		JLabel graphicLabel = new JLabel(new ImageIcon((new ImageIcon(getClass().getResource("/images/gangklangoverview.png")).getImage().getScaledInstance(500, 300, java.awt.Image.SCALE_SMOOTH))));
		
		innerCenterPanel.add(textLabel, BorderLayout.NORTH);
		innerCenterPanel.add(graphicLabel, BorderLayout.CENTER);
		
		//south panel
		JPanel southPanel = GUIFactory.getTransparentPanel();
		southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		JButton btnAbout = new JButton("About");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "The App \"Gangklang\" was developed on the Hochschule Bremen \n"
						+ "in a BMBF research project for body movements and sound.\n"
						+ "The interface was developed as part of this project in an independent study by Jendrik Bulk.\n\n"
						+ "The interface program is an open source project.",
						"About: Gangklang and the interface", JOptionPane.INFORMATION_MESSAGE, null);
			}
		});
		
		southPanel.add(btnAbout);
		
		this.add(westPanel, BorderLayout.WEST);
		this.add(innerCenterPanel, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		this.setOpaque(false);
	}
}
