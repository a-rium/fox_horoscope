package client;

import shared.RequestMessage;
import shared.UDPMessageSocket;
import shared.InvalidMessageException;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JSeparator;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;

import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

import javax.swing.ImageIcon;

import java.net.DatagramPacket;
import java.net.URL;

import java.io.IOException;

import java.util.ArrayList;

public class HoroscopeGUI extends JFrame
{
	private static final int DEFAULT_DOOR = 60000;
	private UDPMessageSocket client;
	private JLabel availableHoroscopeLabel;
	private int availableHoroscopes;

	public HoroscopeGUI(String username, String ipAddr, int port)
	{
		super("FoxHoroscope - Accesso come " + username);
		JTextPane horoscopeArea = new JTextPane();
		horoscopeArea.setEditable(false);
		try
		{
			client = new UDPMessageSocket(DEFAULT_DOOR);
			String[] parameters = { username };
			client.addPacketHandler((DatagramPacket packet) ->
			{
				try
				{
					RequestMessage response = new RequestMessage(packet.getData());
					if(response.getRequest().equals("connect"))
					{
						availableHoroscopes = Integer.parseInt(response.getParameter(0));
						client.connect(ipAddr, port);
						if(availableHoroscopes > -1)
							availableHoroscopeLabel.setText("Hai ancora " + availableHoroscopes + " oroscopi a disposizione");
						else
							availableHoroscopeLabel.setText("Utente premium: numero illimitato di oroscopi");
					}
					else if(!client.isConnected())
					{
						JOptionPane.showMessageDialog(null, "E' accaduto un errore durante la connessione al server", "Error", JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
					}
				}
				catch(InvalidMessageException ime)
				{
					String paragraph = new String(packet.getData()).trim();
					horoscopeArea.setText(paragraph);
					if(availableHoroscopes > 0)
					{
						availableHoroscopes--;
						availableHoroscopeLabel.setText("Hai ancora " + availableHoroscopes + " oroscopi a disposizione");
					}
				}
				catch(IOException ie) {}
			});
			// client.setSoTimeout(5000);
			client.send(new RequestMessage("connect", parameters).getBytes(), ipAddr, port);
		}
		catch(IOException ie)
		{
			JOptionPane.showMessageDialog(null, "E' accaduto un errore durante la connessione al server", 
				"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}

		Border etchedBorder = BorderFactory.createEtchedBorder();
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));

		availableHoroscopeLabel = new JLabel("Hai ancora " + availableHoroscopes + " oroscopi a disposizione");
		horoscopeArea.setContentType("text/html");
		JPanel inputPanel = new JPanel(new GridLayout(2, 1));
		inputPanel.setBorder(new TitledBorder(etchedBorder, "Dati utente"));
		JPanel dataPanel = new JPanel(new FlowLayout());
		String[] days = {"--", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10",
					   		 "11", "12", "13", "14", "15", "16", "17", "18", "19", "20",
					   		 "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};
		String[] months = {"--", "Gennaio", "Febbraio", "Marzo", "Aprile", "Maggio", "Giugno",
							   "Luglio", "Agosto", "Settembre", "Ottobre", "Novembre", "Dicembre"};
		ArrayList<String> yearsList = new ArrayList<String>();
		yearsList.add("--");
		for(int year = 2016; year >= 1900; year--)
			yearsList.add(Integer.toString(year));
		String[] years = yearsList.toArray(days);
		JComboBox<String> dayBox = new JComboBox<String>(days);
		dayBox.setSelectedIndex(0);
		JComboBox<String> monthBox = new JComboBox<String>(months);
		monthBox.setSelectedIndex(0);
		JComboBox yearBox= new JComboBox<String>(years);
		yearBox.setSelectedIndex(0);
		dataPanel.add(dayBox);
		dataPanel.add(new JLabel("/"));
		dataPanel.add(monthBox);
		dataPanel.add(new JLabel("/"));
		dataPanel.add(yearBox);
		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
		JButton sendButton = new JButton("Richiedi");
		sendButton.addActionListener((ActionEvent e) ->
		{
			int day = dayBox.getSelectedIndex();
			int month = monthBox.getSelectedIndex();
			int year = yearBox.getSelectedIndex();
			if(day > 0 && month > 0 && year > 0)
			{
				if((month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 11) ||
					(month == 2 && day < 29) || ((month == 4 || month == 6 || month == 9 || month == 11) && day < 31))
				{
					try
					{
						if(availableHoroscopes == 0)
							JOptionPane.showMessageDialog(null, "Non puoi piu' richiedere oroscopi." + 
								" Diventa utente premium!", "Info", JOptionPane.INFORMATION_MESSAGE);		
						else
						{
							try
							{
								String[] parameters = { username, day + "/" + month + "/" + year };
								client.send(new RequestMessage("date", parameters).getBytes());
							}
							catch(IOException i) {}
						}
					}
					catch(NumberFormatException nfe)
					{
						JOptionPane.showMessageDialog(null, "Anno non valido(non e' un numero)", "Error", 
							JOptionPane.ERROR_MESSAGE);		
					}
				}
				else
					JOptionPane.showMessageDialog(null, "Data non valida: " + days[day] + " " + months[month], 
						"Errore", JOptionPane.ERROR_MESSAGE);	
			}
			else
				JOptionPane.showMessageDialog(null, "Devi selezionare una data", "Info", JOptionPane.INFORMATION_MESSAGE);
		});
		buttonPanel.add(new JLabel());
		buttonPanel.add(sendButton);
		buttonPanel.add(new JLabel());
		inputPanel.add(dataPanel);
		inputPanel.add(buttonPanel);
		JPanel infoPanel = new JPanel();
		infoPanel.add(availableHoroscopeLabel);
		JPanel premiumPanel = new JPanel(new GridLayout(1, 2));
		premiumPanel.setBorder(new TitledBorder(etchedBorder, "Opzioni Premium", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER));
		URL logoPath = getClass().getResource("../logo.png");
		JButton premiumButton = new JButton("Diventa premium!");
		if(availableHoroscopes == -1)
		{
			availableHoroscopeLabel = new JLabel("Utente premium: numero illimitato di oroscopi");
			premiumButton.setEnabled(false);
		}
		else
			availableHoroscopeLabel = new JLabel("Hai ancora " + availableHoroscopes + " oroscopi a disposizione");
		// premiumButton.setIcon(new ImageIcon(logoPath));
		JLabel logoLabel = new JLabel();
		logoLabel.setIcon(new ImageIcon(logoPath));
		premiumButton.addActionListener((ActionEvent e) ->
		{
			String email = JOptionPane.showInputDialog(null, "Inserisci l'email legata al tuo account PayPal: ", "PAGAH", JOptionPane.QUESTION_MESSAGE);
			if(email.matches("\\w+@\\w+.\\w+"))
			{
				JOptionPane.showMessageDialog(null, "Congratulazioni! Il tuo account e' adesso premium. Potrai richiedere un numero illimjitato di oroscopi.", "Pagamento effetuato", JOptionPane.INFORMATION_MESSAGE);
				availableHoroscopes = -1;
				availableHoroscopeLabel.setText("Utente premium: numero illimitato di oroscopi");
				premiumButton.setEnabled(false);
				try
				{
					String[] parameters = { username };
					client.send(new RequestMessage("upgrade", parameters).getBytes());
				}
				catch(IOException i) {}
			}
			else
				JOptionPane.showMessageDialog(null, "ERRORE! L'email inserita non e' valida.", "Errore pagamento", JOptionPane.ERROR_MESSAGE);
		});
		premiumPanel.add(premiumButton);
		premiumPanel.add(logoLabel);
		mainPanel.add(inputPanel);
		mainPanel.add(infoPanel);
		mainPanel.add(premiumPanel);
		mainPanel.add(horoscopeArea);
		add(mainPanel);
		setSize(new Dimension(360, 600));
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}
}