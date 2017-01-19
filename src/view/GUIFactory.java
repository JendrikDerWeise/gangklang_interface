package view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;

public class GUIFactory {

	public static JSlider getVolumeSlider(){
		JSlider slider = new JSlider(JSlider.HORIZONTAL, 0, 10, 5);
		slider.setMinorTickSpacing(1);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.setOpaque(false);
		return slider;
	}
	
	public static JPanel getTransparentPanel(){
		GridLayout layout = new GridLayout(0,3,10,5);
		JPanel panel = new JPanel();
		panel.setLayout(layout);
		panel.setOpaque(false);
		return panel;
	}
	
	public static JPanel getBtnPanel(){
		JPanel panel = getTransparentPanel();
		GridLayout eastPanelLayout = new GridLayout(2,0,0,50);
		panel.setBorder(new EmptyBorder(100,0,100,30));
		panel.setLayout(eastPanelLayout);
		
		return panel;
	}

}
