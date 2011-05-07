package DnaDesignGUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

/**
 * Modal windowing system, which means that a single modal window can be open
 * at any time, and is rendered on top of all other components. The overlaid window captures all input.
 */
public class ModalUtils {
	public static JPanel openModalDialog(DnaDesignGUI_ThemedApplet mc, final Font xfont) {
		return openModalDialog(mc, xfont, new Runnable(){
			public void run() {
			}
		});
	}
	public static JPanel openModalDialog(final DnaDesignGUI_ThemedApplet mc, final Font xfont, final Runnable runOnClose) {
		final JPanel modalPanel = mc.getModalPanel();
		modalPanel.removeAll();
		final ScaleUtils su = new ScaleUtils();
		JButton sub = new JButton(){
			public void paintComponent(Graphics g){
				g.setColor(Color.WHITE);
				g.fillRect(0,0,getWidth(),getHeight());
			}
		};
		sub.setOpaque(true);
		sub.setLayout(new BorderLayout());
		JButton close = new JButton(){
			private boolean mouseInsideX = false;
			{
				setBorder(null);
				setOpaque(true);
				addMouseListener(new MouseListener(){
					public void mouseClicked(MouseEvent e) {
						//EXIT MODAL
						modalPanel.removeAll();
						modalPanel.validate();
						modalPanel.repaint();
						runOnClose.run();
					}
					
					public void mouseEntered(MouseEvent e) {
						mouseInsideX = true;
					}

					public void mouseExited(MouseEvent e) {
						mouseInsideX = false;
					}

					public void mousePressed(MouseEvent e) {
					}

					public void mouseReleased(MouseEvent e) {
					}
					
				});
			}
			public void paintComponent(Graphics g){
				g.setColor(mc.THEMECOL2);
						//new Color(200,180,180));
				g.fillRect(0,0,getWidth(),getHeight());
				//Draw X at top.
				if (mouseInsideX){
					g.setColor(Color.red);
				} else {
					g.setColor(Color.black);
				}
				g.setFont(xfont.deriveFont(Font.BOLD,24.f));
				g.drawString("X", 5, 20);
			}
		};
		final int closeBarSize = 24;
		su.addPreferredSize(close, 0f, 0f, closeBarSize,closeBarSize);
		JPanel leftSide = new JPanel();
		su.addPreferredSize(leftSide, 1f, 1f, -closeBarSize-5, 0);
		sub.add(leftSide, BorderLayout.WEST);
		sub.add(close, BorderLayout.EAST);
		su.addPreferredSize(sub, 1f,1f,0,-8);
		sub.setRolloverEnabled(false);
		sub.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
		modalPanel.add(sub,BorderLayout.CENTER);
		
		mc.addModalScale(new Runnable(){
			public void run(){
				su.pushSizes(modalPanel.getPreferredSize().width, modalPanel.getPreferredSize().height);
				modalPanel.validate();
			}
		});
		return leftSide;
	}

}
