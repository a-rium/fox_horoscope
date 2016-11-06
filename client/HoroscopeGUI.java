package client;

import shared.RequestMessage;
import shared.UDPMessageSocket;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JOptionPane;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import java.net.DatagramPacket;

import java.io.IOException;

public class HoroscopeGUI extends JFrame
{
	private static final int DEFAULT_DOOR = 60000;
	private UDPMessageSocket client;
	private int availableHoroscopes;

	public HoroscopeGUI(String username, String ipAddr, int port)
	{
		super("FoxHoroscope - Logged as " + username);
		try
		{
			client = new UDPMessageSocket(DEFAULT_DOOR);
			String[] parameters = { username };
			client.send(new RequestMessage("connect", parameters).getBytes(), ipAddr, port);
			client.setSoTimeout(5000);
			DatagramPacket responsePacket = client.receive();
			// client.setSoTimeout(0);
			RequestMessage response = new RequestMessage(responsePacket.getData());
			if(!response.getRequest().equals("connect"))
			{
				JOptionPane.showMessageDialog(null, "An error has occurred while attempting to contact the server", "Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
			}
			availableHoroscopes = Integer.parseInt(response.getParameter(0));
			client.connect(ipAddr, port);

		}
		catch(IOException ie)
		{
			JOptionPane.showMessageDialog(null, "An error has occurred while attempting to contact the server", "Error", JOptionPane.ERROR_MESSAGE);
			ie.printStackTrace();
			System.exit(-1);
		}

		Border etchedBorder = BorderFactory.createEtchedBorder();
		JPanel mainPanel = new JPanel();
		JTextPane horoscopeArea = new JTextPane();
		horoscopeArea.setContentType("text/html");
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		inputPanel.setBorder(new TitledBorder(etchedBorder, "Dati utente"));
		JPanel dataPanel = new JPanel(new FlowLayout());
		String[] days = {"--", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
					   		 "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
					   		 "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		String[] months = {"--", "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
							   "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};
		JComboBox<String> dayBox = new JComboBox<String>(days);
		dayBox.setSelectedIndex(0);
		JComboBox<String> monthBox = new JComboBox<String>(months);
		monthBox.setSelectedIndex(0);
		JTextField yearField = new JTextField("", 4);
		dataPanel.add(dayBox);
		dataPanel.add(new JLabel("/"));
		dataPanel.add(monthBox);
		dataPanel.add(new JLabel("/"));
		dataPanel.add(yearField);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		JButton sendButton = new JButton("Richiedi");
		sendButton.addActionListener((ActionEvent e) ->
		{
			int day = dayBox.getSelectedIndex();
			int month = monthBox.getSelectedIndex();
			if(day > 0 && month > 0)
			{
				if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 11) ||
					(month == 2 && day < 29) || ((month == 4 || month == 6 || month == 9 || month == 11) && day < 31))
				{
					try
					{
						int year = Integer.parseInt(yearField.getText());
						if(year < 1900)
							JOptionPane.showMessageDialog(null, "Invalid year(number is less than 1900): " + year, 
								"Error", JOptionPane.ERROR_MESSAGE);		
						if(availableHoroscopes == 0)
							JOptionPane.showMessageDialog(null, "You cannot request an horoscope anymore." + 
								" To get more, update to a premium account!" + year, "Info", JOptionPane.INFORMATION_MESSAGE);		
						else
						{
							try
							{
								String[] parameters = { day + "/" + month + "/" + year };
								client.send(new RequestMessage("date", parameters).getBytes());
								String paragraph = client.receiveMessage();
								System.out.println(paragraph);
								horoscopeArea.setText(paragraph);
								revalidate();
								repaint();
							}
							catch(IOException i){}
							availableHoroscopes--;
						}
					}
					catch(NumberFormatException nfe)
					{
						JOptionPane.showMessageDialog(null, "Invalid year (not a number)", "Error", 
							JOptionPane.ERROR_MESSAGE);		
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Invalid date: " + days[day] + " " + months[month], 
						"Error", JOptionPane.ERROR_MESSAGE);	
			}
			else
				JOptionPane.showMessageDialog(null, "You must select a date", "Info", JOptionPane.INFORMATION_MESSAGE);
		});
		buttonPanel.add(new JLabel());
		buttonPanel.add(sendButton);
		buttonPanel.add(new JLabel());
		inputPanel.add(dataPanel);
		inputPanel.add(buttonPanel);
		// horoscopeArea.setEditable(false);
		mainPanel.add(inputPanel);
		mainPanel.add(horoscopeArea);
		add(mainPanel);
		setSize(new Dimension(360, 600));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}