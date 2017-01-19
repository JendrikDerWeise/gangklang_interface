package view;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import controller.ObjectSaver;
import model.Project;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.JComboBox;

public class MainFrame<E> extends JFrame implements ICallback{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8206473607677599044L;
	private JLabel contentPane;
	private JFileChooser chooser;
	private JToolBar mToolBar;
	private JPanel mContentPanel;
	
	private JPanel mStartscreen;
	private JPanel mSoundsInLibrary;
	private JPanel mSavedProjects;
	private JPanel mGaitsEditor;
	private JPanel mLayerEditor;
	private JPanel mLevelEditor;
	private JPanel mWalkAlongEditor;
	private ProjectEditor mProjectEditor;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					try {
			            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
			        } catch (ClassNotFoundException e) {
			            e.printStackTrace();
			        } catch (InstantiationException e) {
			            e.printStackTrace();
			        } catch (IllegalAccessException e) {
			            e.printStackTrace();
			        } catch (UnsupportedLookAndFeelException e) {
			            e.printStackTrace();
			        }
					
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		this.setTitle("Gangklang Interface");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1000, 640);
		contentPane = new JLabel();
		contentPane.setIcon(new ImageIcon(getClass().getResource("/images/bg.jpg")));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		
		mToolBar = new JToolBar();
		mToolBar.setOpaque(false);
		mToolBar.setPreferredSize(new Dimension(400,40));
		GridLayout tbLayout = new GridLayout(0,3,100,0);
		mToolBar.setLayout(tbLayout);
		setUpButtons();
		setUpComboBox();
		contentPane.add(mToolBar, BorderLayout.NORTH);
		
		//init all panels, to keep the stats 
		mStartscreen = new Startscreen();
		mSoundsInLibrary = new SoundsInLibrary();
		mSavedProjects = new SavedProjects(this);
		mGaitsEditor = new WalkBeat();
		mLayerEditor = new LayerEditor<>();
		mLevelEditor = new LevelSet<>();
		mWalkAlongEditor = new WalkAlongEditor<>();
		mProjectEditor = new ProjectEditor();
		
		mContentPanel = mStartscreen;
		
		contentPane.add(mContentPanel, BorderLayout.CENTER);
		
	}
	
	@Override
	public void openSavedProject(Project p){
		mProjectEditor.loadProject(p);
		openProjectEditor();
	}
	
	public void fileChooser(){
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle("Location of gangklang project-files:");
		
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { 
		      System.out.println("getCurrentDirectory(): " 
		         +  chooser.getCurrentDirectory());
		      System.out.println("getSelectedFile() : " 
		         +  chooser.getSelectedFile());
		      ObjectSaver.saveFlowMachinePath(chooser.getSelectedFile());
		      //ObjectSaver.saveFlowMachinePath(chooser.getSelectedFile().toPath());
		}else{
		      System.out.println("No Selection ");
		}
	}
	
	/**
	 * Sets the combobox for program options
	 */
	private void setUpComboBox(){
		String[] menuOptions = { "Startscreen", "Saved projects", "Sounds in libary", "WalkBeat editor", "Soundcarpet editor", "Level editor", "WalkAlong editor" };
		JComboBox comboBox = new JComboBox(menuOptions);
		comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	contentPane.remove(mContentPanel);
                switch(comboBox.getSelectedItem().toString()){
                case "Startscreen":
                	mContentPanel = mStartscreen;
                	break;
                case "Saved projects":
                	mContentPanel = mSavedProjects;
                	break;
                case "Sounds in libary":
                	mContentPanel = mSoundsInLibrary;
                	break;
                case "WalkBeat editor":
                	mContentPanel = mGaitsEditor;
                	break;
                case "Soundcarpet editor":
                	mContentPanel = mLayerEditor;
                	break;
                case "Level editor":
                	mContentPanel = mLevelEditor;
                	break;
                case "WalkAlong editor":
                	mContentPanel = mWalkAlongEditor;
                	break;
                }
                contentPane.add(mContentPanel, BorderLayout.CENTER);
                contentPane.revalidate();
                contentPane.repaint();
            }
        });
		
		mToolBar.add(comboBox);
	}
	
	private void openProjectEditor(){
		contentPane.remove(mContentPanel);
		mContentPanel = new ProjectEditor();
		contentPane.add(mContentPanel, BorderLayout.CENTER);
        contentPane.revalidate();
        contentPane.repaint();
	}
	
	/**
	 * Sets the buttons in the Toolbar
	 */
	private void setUpButtons(){
		JButton btnCreateNewProject = new JButton("Projecteditor");
		btnCreateNewProject.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openProjectEditor();
			}
		});
		
		JButton btnSetupFlowMachineFolder = null;
		ImageIcon foot = new ImageIcon(getClass().getResource("/images/foot.png"));
		ImageIcon icon = new ImageIcon(foot.getImage().getScaledInstance(30, 30, java.awt.Image.SCALE_SMOOTH));
		btnSetupFlowMachineFolder = new JButton(icon);

		btnSetupFlowMachineFolder.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fileChooser();
			}
		});
		
		mToolBar.add(btnCreateNewProject);
		mToolBar.add(btnSetupFlowMachineFolder);
	}
}
