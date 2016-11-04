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
import javax.swing.SwingConstants;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import javax.swing.border.Border;

public class LoginGUI extends JFrame
{
	public LoginGUI()
	{
		super("FoxHoroscope - Login");
		JPanel mainPanel = new JPanel(new BorderLayout());
		JPanel usernamePanel = new JPanel(new GridLayout(1, 2));
		Border etchedBorder = BorderFactory.createEtchedBorder();
		usernamePanel.setBorder(new TitledBorder(etchedBorder, "Login"));
		JTextField usernameField = new JTextField("utente");
		usernamePanel.add(new JLabel("Scelgi un username:", SwingConstants.RIGHT));
		usernamePanel.add(usernameField);
		JButton nextButton = new JButton("Avanti >>");
		nextButton.addActionListener((ActionEvent e) ->
		{
			// new HoroscopeGUI(usernameField.getText());
			try
			{
				PrintWriter writer = new PrintWriter(new FileWriter("user.conf"));
				writer.printf("%s", usernameField.getText());
				writer.close();

			} catch(IOException i) {}
			dispose();
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
			mainPanel.add(usernamePanel, BorderLayout.CENTER);
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
				mainPanel.add(new JLabel("<html><center>File di configurazione trovato.<br>Username: " + 
					usernameField.getText() + "</center></html>", SwingConstants.CENTER), BorderLayout.CENTER);
				controlPanel.add(previousButton);
			}
			catch(IOException ie)
			{
				ie.printStackTrace();
			}
		}
		else
		{
			mainPanel.add(usernamePanel, BorderLayout.CENTER);
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