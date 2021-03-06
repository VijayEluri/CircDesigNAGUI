/*
  Part of the CircDesigNA Project - http://cssb.utexas.edu/circdesigna
  
  Copyright (c) 2010-11 Ben Braun
  
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation, version 2.1.

  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General
  Public License along with this library; if not, write to the
  Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  Boston, MA  02111-1307  USA
*/
package circdesignagui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Displays a help button, which, when clicked, opens a help window to display information about
 * the corresponding component.
 */
public class HelpButton extends JButton implements MouseListener{
	private String msg,header;
	private ThemedApplet mc;
	private Component repainter;
	public void repaint(){
		if (mc==null || mc.getModalPanel()==null){ super.repaint(); return;}
		int hasSub = mc.getModalPanel().getComponentCount();
		if (hasSub > 0){
	
			return;
		}
		super.repaint();
	}
	public static final Dimension dim = new Dimension(12,12);
	private static final Font f;
	static {
		f = Font.decode("Monospaced-12");
	}
	public HelpButton(String string2, String string, ThemedApplet mc, Component repainter){
		this.mc = mc;
		this.repainter = repainter;
		this.msg = string;
		this.header = string2;
		setOpaque(false);
		setBorder(null);
		setRolloverEnabled(false);
		addMouseListener(this);
	}
	public Dimension getPreferredSize(){
		return dim;
	}
	private boolean mouseOver = false;
	private int mouseOverAlpha = 0;
	public void paintComponent(Graphics g){
		//mouseMoved(ms.getMouse());
		g.setColor(new Color(100,140,200));
		int yoff = 0;
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
		        RenderingHints.VALUE_ANTIALIAS_ON);
		g.fillOval(0,yoff,dim.width,dim.height);
		g.setColor(Color.black);
		g.drawOval(0,yoff,dim.width,dim.height);
		g.setFont(f);
		FontMetrics f2 = g.getFontMetrics(f);
		g.drawString("?",3,0+10+yoff);
		if (mouseOver){
			mouseOverAlpha = mouseOverAlpha+5;
			if (mouseOverAlpha > 255){
				mouseOverAlpha = 255;
			} else {
				new Thread(){
					public void run(){
						try {
							Thread.sleep(1000/60);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						repaint();
					}	
				}.start();
			}
		} else {
			mouseOverAlpha = mouseOverAlpha-5;
			if (mouseOverAlpha < 0){
				mouseOverAlpha = 0;
			} else {
				new Thread(){
					public void run(){
						try {
							Thread.sleep(1000/60);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						repaint();
					}	
				}.start();
			}
		}
		g.setColor(new Color(0,0,0,mouseOverAlpha));
		int ki = 0;
		for(char k : "HELP".toCharArray()){
			ki++;
			g.drawString(k+"",3,dim.height+10*ki+yoff+2);
		}
	}
	public void mouseMoved(Point e) {
		System.out.println(getBounds()+" "+e);
		if (getBounds().contains(e)){
			mouseOver = true;
		} else {
			System.out.println("FALSE");
			mouseOver = false;
		}
	}
	public void mouseClicked(MouseEvent e) {
		if (mc.modalPanelIsOccupied()){
			return;
		}
		mc.removeAllModalScale();
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,f);
		/*
		final JTextArea comp = new JTextArea(msg){
			{
				setOpaque(false);
				setFont(Font.decode("Verdana-11"));
				setLineWrap(true);
				setWrapStyleWord(true);
			}
		};
		*/
		final JLabel comp = new JLabel("<html><p>"+msg+"</p></html>"){
			{
				setOpaque(false);
				setFont(Font.decode("Verdana-11"));
			}
		};
		final JLabel compH = new JLabel("<html>"+header, JLabel.LEFT){
			{
				setOpaque(false);
				setFont(Font.decode("Georgia-18"));
			}
		};
		openModalDialog.setLayout(new FlowLayout(FlowLayout.LEADING,0,0));
		comp.setBorder(BorderFactory.createEmptyBorder(0,5,0,5));
		compH.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		final ScaleUtils su = new ScaleUtils();
		//su.addPreferredSize(compH, 1f, 0, 0, 24);
		//su.addPreferredSize(comp, 1f, 1f, 0, -28);
		comp.setVerticalAlignment(JLabel.TOP);
		
		su.addPreferredSize(compH, 1f, 0, 0, 24);
		JScrollPane compHolder = new JScrollPane(comp,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		su.addPreferredSize(compHolder, 1f,.9f, 0, -10);
		su.addPreferredSize(comp, 1f, 8f, -10, 0);
		openModalDialog.add(compH);
		openModalDialog.add(compHolder);
		mc.addModalScale(new Runnable(){
			public void run(){
				su.pushSizes(openModalDialog.getPreferredSize().width, openModalDialog.getPreferredSize().height);
				openModalDialog.validate();
			}
		});
	}
	public void mouseEntered(MouseEvent e) {
		mouseOver = true;
		repaint();
	}
	public void mouseExited(MouseEvent e) {
		mouseOver = false;
		repaint();
	}
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
