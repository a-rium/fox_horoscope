package client;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import java.awt.GridLayout;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

import java.net.InetAddress;

public class LoginGUI extends JFrame
{
	public LoginGUI()
	{
		super("FoxHoroscope - Login");
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel inputPanel = new JPanel(new GridLayout(3, 2));
		Border etchedBorder = BorderFactory.createEtchedBorder();
		inputPanel.setBorder(new TitledBorder(etchedBorder, "Login"));
		JTextField usernameField = new JTextField("utente");
		JTextField ipField = new JTextField("localhost");
		JTextField portField = new JTextField("50000");
		inputPanel.add(new JLabel("Scegli un username:", SwingConstants.RIGHT));
		inputPanel.add(usernameField);
		inputPanel.add(new JLabel("Server IP:", SwingConstants.RIGHT));
		inputPanel.add(ipField);
		inputPanel.add(new JLabel("Porta server:", SwingConstants.RIGHT));
		inputPanel.add(portField);
		JButton nextButton = new JButton("Avanti >>");
		nextButton.addActionListener((ActionEvent e) ->
		{
			try
			{
				int port = Integer.parseInt(portField.getText());
				try
				{
					String ipAddr = ipField.getText();
					InetAddress.getByName(ipAddr);
					new HoroscopeGUI(usernameField.getText(), ipAddr, port);
					PrintWriter writer = new PrintWriter(new FileWriter("user.conf"));
					writer.printf("%s\n", usernameField.getText());
					writer.printf("%s\n", ipAddr);
					writer.printf("%d", port);
					writer.close();
					dispose();
				}
				catch(IOException i)
				{
					JOptionPane.showMessageDialog(null, "Indirizzo IP non valido", 
						"Errore", JOptionPane.ERROR_MESSAGE);
				}
			}
			catch(NumberFormatException nfe)
			{
				JOptionPane.showMessageDialog(null, "La porta del server deve essere un valore compreso tra 0 e 65536",
							 "Errore", JOptionPane.ERROR_MESSAGE);	
			}
		});
		JPanel controlPanel = new JPanel(new GridLayout(1, 3));
		JButton previousButton = new JButton("<< Indietro");
		previousButton.addActionListener((ActionEvent e) ->
		{
			mainPanel.removeAll();
			controlPanel.removeAll();
			controlPanel.add(new JLabel());
			controlPanel.add(new JLabel());
			controlPanel.add(nextButton);
			mainPanel.add(inputPanel, BorderLayout.CENTER);
			mainPanel.add(controlPanel, BorderLayout.SOUTH);
			revalidate();
			repaint();
		});
		File explorer = new File("./user.conf");
		if(explorer.exists())
		{
			try
			{
				BufferedReader reader = new BufferedReader(new FileReader(explorer));
				usernameField.setText(reader.readLine().trim());
				ipField.setText(reader.readLine().trim());
				portField.setText(reader.readLine().trim());
				mainPanel.add(new JLabel("<html><center>File di configurazione trovato." + 
					"<br>Username: " + usernameField.getText() + 
					"<br>Server IP: " + ipField.getText() + 
					"<br>Porta server: " + portField.getText() + 
					"</center></html>", SwingConstants.CENTER), BorderLayout.CENTER);
				controlPanel.add(previousButton);
			}
			catch(IOException ie)
			{
				ie.printStackTrace();
			}
		}
		else
		{
			mainPanel.add(inputPanel, BorderLayout.CENTER);
			controlPanel.add(new JLabel());
		}
		controlPanel.add(new JLabel());
		controlPanel.add(nextButton);	
		mainPanel.add(controlPanel, BorderLayout.SOUTH);
		add(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(new Dimension(400, 120));
		setResizable(false);
		setVisible(true);
	}

}