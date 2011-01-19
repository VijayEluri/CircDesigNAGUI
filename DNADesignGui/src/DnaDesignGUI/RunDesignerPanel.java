package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;

import DnaDesign.DDSeqDesigner;
import DnaDesign.DesignerOptions;
import DnaDesign.DDSeqDesigner.SeqDesignerOption;

public class RunDesignerPanel {
	private DnaDesignGUI_ThemedApplet mc;
	public JButton skinButton(JButton jb){
		ButtonSkin.process(mc, jb);
		return jb;
	}
	public RunDesignerPanel(DnaDesignGUI_ThemedApplet mc, final DDSeqDesigner cDesign, Font monoSpaceFont, final DesignerVisualGraph showGraph){
		final JTextArea outputText = new JTextArea("No output. First begin the designer, and then press the button again to show an intermediate result.");
		final JScrollPane showText = new JScrollPane(outputText);
		final JPanel showOptions = new JPanel();
		this.mc = mc;
		final JButton actionOnRunningDesigner = new JButton();
		showOptions.setLayout(new BorderLayout());
		{
			showOptions.setBackground(mc.THEMECOL1);
			Box showOptionsBox = Box.createVerticalBox();
			DesignerOptions options = cDesign.getOptions();
			for(final SeqDesignerOption option : options.options){
				JLabel label = new JLabel();
				label.setText(option.getDescription());
				if (option instanceof SeqDesignerOption.Boolean){
					//Add a new toggle
					final JCheckBox toggle = new JCheckBox();
					toggle.setOpaque(false);
					Box horiz = Box.createHorizontalBox();
					horiz.add(toggle);
					horiz.add(label);
					horiz.add(Box.createHorizontalGlue());
					showOptionsBox.add(horiz);
					label.setLabelFor(toggle);
					final SeqDesignerOption.Boolean bOption = (SeqDesignerOption.Boolean) option;
					toggle.setSelected(bOption.getState());
					toggle.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							bOption.toggle();
							toggle.setSelected(bOption.getState());
						}
					});
					actionOnRunningDesigner.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							toggle.setEnabled(!cDesign.isRunning());
							if (cDesign.isRunning()){
								toggle.setSelected(bOption.getState());	
							}
						}
					});
				} else if (option instanceof SeqDesignerOption.Double || option instanceof SeqDesignerOption.Integer){
					final SeqDesignerOption.Double dOption;
					final SeqDesignerOption.Integer iOption;
					if (!(option instanceof SeqDesignerOption.Integer)){
						dOption = (SeqDesignerOption.Double) option;
						iOption = null;
					} else{
						iOption = (SeqDesignerOption.Integer) option;
						dOption = null;
					}
					//Add a value field
					final JTextField jta = new JTextField(12);
					jta.setMaximumSize(new Dimension(200,23));
					final String defaultDedicateText = "Go";
					final JButton dedicate = new JButton(defaultDedicateText);
					if (dOption!=null){
						jta.setText(dOption.getState()+"");
					} else {
						jta.setText(iOption.getState()+"");
					}
					dedicate.setPreferredSize(new Dimension(70,23));
					dedicate.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent f) {
							try {
								if (dOption!=null){
									dOption.setState(new Double(jta.getText()));
								} else {
									iOption.setState(new Integer(jta.getText()));
								}
							} catch (Throwable e){
								dedicate.setText("ERR.");
								new Thread(){
									public void run(){
										try {
											Thread.sleep(500);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										dedicate.setText(defaultDedicateText);
									}
								}.start();
							}
						}
					});
					actionOnRunningDesigner.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							if (cDesign.isRunning()){
								if (dOption!=null){
									jta.setText(dOption.getState()+"");
								} else {
									jta.setText(iOption.getState()+"");
								}
							}
							dedicate.setEnabled(!cDesign.isRunning());
							jta.setEditable(!cDesign.isRunning());
						}
					});
					Box horiz = Box.createHorizontalBox();
					horiz.add(jta);
					horiz.add(dedicate);
					horiz.add(label);
					horiz.add(Box.createHorizontalGlue());
					showOptionsBox.add(horiz);
				}
			}
			showOptionsBox.add(Box.createVerticalGlue());
			showOptions.add(showOptionsBox);
		}
		
		
		final JButton resumeDesigner = skinButton(new JButton("Resume Designer"){
			private boolean designerRunning = false, designerFinished1 = false;
			private void setDesignerRunning(double i) {
				String append = "Designer Running (Click to get Intermediate Result.)";
				if(cDesign.getIterationCount()>0){
					append += "Score: "+String.format("%.2f",i)+" Iterations: "+cDesign.getIterationCount();
				}
				setText(append);
			}
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(
							ActionEvent e) {
						if (!designerRunning){
							designerRunning = true;
							new Thread(){
								public void run(){
									cDesign.resume();
									for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
										al.actionPerformed(null);
									}
									while(!cDesign.isFinished() && cDesign.isRunning()){
										setDesignerRunning(cDesign.getBestScore());
										try {
											Thread.sleep(100);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
									if (cDesign.isFinished()){
										if (cDesign.isEndConditionError()){
											setText("Designer Failed. See text output.");
										} else {
											setText("Designer Finished");
										}
										outputText.setText(cDesign.getResult());
										designerFinished1 = true;
										repaint();
									}
								}
							}.start();
						} else {
							//Get intermediate result.
							outputText.setText(cDesign.getResult());
						}
						repaint();
					}
				});
			}		
		});
		
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){;
		public void run(){
			cDesign.abort();
			showGraph.setVisible(false);
		}
		});
		
		JPanel DisplayTabs = new JPanel();
		DisplayTabs.setLayout(new GridLayout(0,3));
		JButton GoToVisualDisplay,GoToGraphDisplay,GoToOptions;
		final JButton[] allThree = new JButton[3];
		DisplayTabs.add(allThree[0] = GoToVisualDisplay = skinButton(new JButton("Text Output"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						showText.setVisible(true);
						showOptions.setVisible(false);
						enableAllBut(allThree,0);
						openModalDialog.validate();
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		DisplayTabs.add(allThree[1] = GoToGraphDisplay = skinButton(new JButton("Graph View"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(true);
						enableAllBut(allThree,1);
						openModalDialog.validate();									
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		DisplayTabs.add(allThree[2] = GoToOptions = skinButton(new JButton("Options..."){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						showGraph.setVisible(false);
						showOptions.setVisible(true);
						showText.setVisible(false);
						enableAllBut(allThree,2);
						openModalDialog.validate();
						for(ActionListener al : actionOnRunningDesigner.getListeners(ActionListener.class)){
							al.actionPerformed(null);
						}
					}
				});
			}
		}));
		outputText.setEditable(false);
		
		openModalDialog.setLayout(new BorderLayout());
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(2,0));
		buttons.add(resumeDesigner);
		buttons.add(DisplayTabs);
		openModalDialog.add(buttons,BorderLayout.NORTH);
		
		final JLayeredPane jlayers = new JLayeredPane();
		jlayers.setLayout(new OverlayLayout(jlayers));
		jlayers.add(showText,JLayeredPane.DEFAULT_LAYER);
		jlayers.add(showOptions,JLayeredPane.MODAL_LAYER);
		openModalDialog.add(jlayers, BorderLayout.CENTER);
		final ScaleUtils su = new ScaleUtils();
		//su.addPreferredSize(resumeDesigner, 0, 0, 200, 40);
		//su.addPreferredSize(ToggleVisualDisplay, 0, 0, 200, 40);
		su.addPreferredSize(buttons,1f,.1f,0,0);
		su.addPreferredSize(showText,1f,.9f, 0,0);
		//su.addPreferredSize(outputText, 1f, 1f, 0, -50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				int w = openModalDialog.getPreferredSize().width;
				int h = openModalDialog.getPreferredSize().height;
				su.pushSizes(w, h);

				Component model = showText;
				Point location = model.getLocation();
				Component nextParent = model.getParent();
				for(int i = 0; i < 4; i++){
					if (nextParent!=null){
						location.translate(nextParent.getLocation().x,nextParent.getLocation().y);
						nextParent = nextParent.getParent();
						//System.out.println(location);
					}
				}			
				Rectangle newBounds = new Rectangle(location.x,location.y,model.getPreferredSize().width,model.getPreferredSize().height);
				/*
				if (showGraph.getBounds().equals(newBounds)){
					
				} else {
					showGraph.setBounds(newBounds);
				}
				*/
				if (stackOverFlowHack[0]){
					return;
				}
				stackOverFlowHack[0] = true;
				if (!(showGraph.getLocation().equals(location))){
					showGraph.setLocation(location.x,location.y);
				}
				Dimension newSize = new Dimension(model.getPreferredSize().width-1,model.getPreferredSize().height-8);
				if (!(showGraph.getPreferredSize().equals(newSize))){
					showGraph.setPreferredSize(newSize);
				}
				stackOverFlowHack[0] = false;
			}
			private boolean[] stackOverFlowHack = new boolean[]{false};
		});
		showGraph.setDesigner(cDesign);

		//Begin in graph view
		for(ActionListener q : GoToGraphDisplay.getActionListeners()){
			q.actionPerformed(null);
		}
	}
	private static void enableAllBut(Component[] array, int but){
		for(int k = 0; k < array.length; k++){
			array[k].setEnabled(k!=but);
		}
	}
}
