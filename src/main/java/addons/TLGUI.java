package addons;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gui
 */
public class TLGUI extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TlAgent agent;
	ArrayList<JButton> buttons = new ArrayList<JButton>();
	/**
	 * Creates new form NewJFrame
	 */
	public TLGUI() {
		initComponents();
	}

	public TLGUI(TlAgent tlAgent) {

		this.agent = tlAgent;
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */

	// <editor-fold defaultstate="collapsed" desc="Generated Code">                          
	private void initComponents() {

		jButton1 = new JButton();
		jPanel1 = new JPanel();

		setMaximumSize(new java.awt.Dimension(500, 200));
		setMinimumSize(new java.awt.Dimension(500, 200));
		getContentPane().setLayout(null);

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

		jButton1.setText("get lanes ids");
		jButton1.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				jButton1ActionPerformed(evt);
			}
		});
		getContentPane().add(jButton1);
		jButton1.setBounds(0, 0, 150, 24);

		jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new Color(0, 0, 0)));
		jPanel1.setLayout(new java.awt.GridLayout());
		getContentPane().add(jPanel1);
		//jPanel1.setBounds(10, 29, 442, 126);
		jPanel1.setBounds(0,30,agent.controlledLanes.size()*(35+2),35);

		jPanel1.setLayout(new java.awt.GridLayout());

		pack();
	}// </editor-fold>                        

	private void jButton1ActionPerformed(ActionEvent evt) {

		for(String s: agent.controlledLanes){

			JButton jp = new JButton("");
			jp.setBorder(null);  
			jp.setBorderPainted(false);  
			jp.setContentAreaFilled(false);  
			jp.setDebugGraphicsOptions(javax.swing.DebugGraphics.BUFFERED_OPTION);  
			jp.setDoubleBuffered(true);  
			jp.setFocusPainted(false);
			jp.setForeground(Color.red);
			buttons.add(jp);	
			System.out.println("added");
		}

		for(JButton p: buttons){
			jPanel1.add(p);
			validate();
		}
		jButton1.setEnabled(false);
	}

	// Variables declaration - do not modify                     
	private JButton jButton1;
	private JPanel jPanel1;
	// End of variables declaration                   
}