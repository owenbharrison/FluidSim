package flsim;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JFrame;

public class FluidSimControlPanel {
	public JFrame mainFrame;
	
	public boolean sliderChange = false;
	public JSlider slider;
	
	public boolean renderChange = false;
	public JComboBox<String> render;
	
	public boolean gradientChange = false;
	public JComboBox<String> gradient;
	
	public static final String densRenderMask = "Render Density";
	public static final String velRenderMask = "Render Velocity";
	public static final String bothRenderMask = "Render Both";
	
	public static final String stressGradMask = "Stress Gradient";
	public static final String coolGradMask = "Cool Gradient";
	public static final String tempGradMask = "Temperature Gradient";
	public static final String rainbowGradMask = "Rainbow Gradient";
	public static final String grayGradMask = "Grayscale Gradient";
	public static final String binaryGradMask = "Binary Gradient";
	
	public FluidSimControlPanel() {
		this.mainFrame = new JFrame("Fluid Sim Control Panel");
		
		JPanel sizePanel = new JPanel();
		JLabel sizeLabel = new JLabel("Sim Size");
		sizePanel.add(BorderLayout.WEST, sizeLabel);
		this.slider = new JSlider(JSlider.HORIZONTAL, 40, 100, 60);
		this.slider.setMinorTickSpacing(1);
		this.slider.setMajorTickSpacing(10);
		this.slider.setPaintTicks(true);
		this.slider.setPaintLabels(true);
		this.slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {sliderChange = true;}
		});
		sizePanel.add(BorderLayout.EAST, this.slider);
		
		JPanel renderPanel = new JPanel();
		JLabel renderLabel = new JLabel("Rendering");
		renderPanel.add(BorderLayout.WEST, renderLabel);
		this.render = new JComboBox<String>(new String[] {densRenderMask, velRenderMask, bothRenderMask});
		this.render.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {renderChange = true;}
		});
		renderPanel.add(BorderLayout.EAST, this.render);
		
		JPanel gradientPanel = new JPanel();
		JLabel gradientLabel = new JLabel("Color Gradient");
		gradientPanel.add(BorderLayout.WEST, gradientLabel);
		this.gradient = new JComboBox<String>(new String[] {stressGradMask, coolGradMask, tempGradMask, rainbowGradMask, grayGradMask, binaryGradMask});
		this.gradient.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {gradientChange = true;}
		});
		gradientPanel.add(BorderLayout.EAST, this.gradient);
		
		this.mainFrame.add(BorderLayout.NORTH, sizePanel);
		this.mainFrame.add(BorderLayout.CENTER, renderPanel);
		this.mainFrame.add(BorderLayout.SOUTH, gradientPanel);
		
		this.mainFrame.pack();
		this.mainFrame.setVisible(true);
		this.mainFrame.setLocationRelativeTo(null);
		this.mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.mainFrame.setResizable(false);
		this.mainFrame.setAlwaysOnTop(true);
	}
}
