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

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Utility GUI for modifying the codon table.
 */
public class ModifyBannedPatternsListPanel {
	public ModifyBannedPatternsListPanel(final CircDesigNA_DesignView mc, Font monoSpaceFont) {
		final JPanel openModalDialog = ModalUtils.openModalDialog(mc,monoSpaceFont, new Runnable(){
			public void run() {
				//Closing action
			}
		});
		
		final JTextArea userInput = new JTextArea();
		userInput.setFont(monoSpaceFont);
		userInput.setText(mc.getCurrentBannedWordsList());
		
		JScrollPane displayScrollPane = new JScrollPane(userInput);
		
		JButton action = new JButton("Update Banned Patterns"){
			{
				addActionListener(new ActionListener(){
					public void actionPerformed(ActionEvent e) {
						try {
							mc.updateBannedWordsList(userInput.getText());
							setText("OK! Click again to update banned patterns.");
						} catch (Throwable f){
							setText("Error: "+f.getMessage());
						}
					}
				});
			}
		};
		openModalDialog.setLayout(new BorderLayout());
		openModalDialog.add(displayScrollPane,BorderLayout.NORTH);
		openModalDialog.add(action,BorderLayout.CENTER);
		
		final ScaleUtils su = new ScaleUtils();
		su.addPreferredSize(displayScrollPane, 1f, 1f, 0, -50);
		su.addPreferredSize(action, .5f, 0, 0, 50);
		
		mc.addModalScale(new Runnable(){
			public void run() {
				su.pushSizes(openModalDialog.getPreferredSize().width,
						openModalDialog.getPreferredSize().height);
				openModalDialog.validate();
			}
		});
		
	}
}
