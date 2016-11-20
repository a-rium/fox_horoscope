package server;

import shared.UDPMessageSocket;
import shared.RequestMessage;
import shared.InvalidMessageException;

import java.net.DatagramPacket;
import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.HashMap;

public class HoroscopeServer extends UDPMessageSocket
{
	private HashMap<String, Integer> clients;

	public HoroscopeServer(int port) throws IOException
	{
		super(port);
		File explorer = new File("connections.data");
		if(explorer.exists())
		{
			try
			{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(explorer));
				clients = (HashMap<String, Integer>) in.readObject();
				in.close();
				System.out.println("Loading archive...");
				for(String name : clients.keySet())
					System.out.printf("\tUser: %s Occorrenze: %d\n", name, clients.get(name));
				System.out.println("Archive loaded!");
			}
			catch(IOException | ClassNotFoundException | ClassCastException i)
			{
				clients = new HashMap<String, Integer>();
			}
		}
		else
			clients = new HashMap<String, Integer>();
		this.addPacketHandler((DatagramPacket packet) ->
		{
			try
			{
				RequestMessage request = new RequestMessage(packet.getData());
				String message = request.getRequest();
				if(message.equals("connect"))
				{
					String username = request.getParameter(0);
					if(!clients.keySet().contains(username))
					{
						System.out.printf("New user: %s\n", username);
						clients.put(username, 5);
					}
					String[] parameters = { "" + clients.get(username) };
					System.out.printf("Sending info to: IP: " + packet.getAddress().getHostAddress() + 
						" Porta: " + packet.getPort() + "...\n");
					send(new RequestMessage("connect", parameters).getBytes(), 
						packet.getAddress().getHostAddress(), packet.getPort());
				}
				else if(message.equals("upgrade"))
				{
					String username = request.getParameter(0);
					if(clients.keySet().contains(username))
					{
						if(clients.get(username) < 0)
							System.out.printf("User %s already has a premium account\n", username);
						else
						{
							clients.put(username, -1);
							System.out.printf("User %s account is now premium\n", username);
						}
					}
				}
				else if(message.equals("data"))
				{
					String sign = request.getParameter(1);
					String[] date = sign.split("/");
					if(date.length == 3)
					{
						int day = Integer.parseInt(date[0]);
						int month = Integer.parseInt(date[1]);
						sign = dateToSign(day, month);
					}
					String url = "http://www.grazia.it/oroscopo/oroscopo-del-giorno/";
					URL sourceWebSite = new URL(url + sign.toLowerCase());
					BufferedReader in = new BufferedReader(new InputStreamReader(sourceWebSite.openStream()));
					String content = "";
					String line;
					while((line = in.readLine()) != null)
						content += line;
					String toFind = "<div id=\"single-article-body\" class=\"container\">";
					int start = content.indexOf(toFind);
					toFind = "<p>";
					start = content.indexOf(toFind, start) + toFind.length();
					toFind = "</p>";
					int end = content.indexOf(toFind, start);
					System.out.println("Paragraph length: " + (end - start));
					String horoscopeParagraph = "<html>" + content.substring(start, end) + "</html>";
					sendMessage(horoscopeParagraph, packet.getAddress().getHostAddress(), packet.getPort());
					System.out.println("Paragraph sent");
					int availableHoroscopes = clients.get(request.getParameter(0));
					if(availableHoroscopes > 0)
						clients.put(request.getParameter(0), availableHoroscopes - 1);

				}
				else if(message.equals("quit"))
				{
					System.out.println("Disconnecting...");
					System.exit(0);
				}
			}
			catch(IOException ie)
			{
				ie.printStackTrace();
			}
		});
	}

	public void close()
	{
		super.close();
		try
		{
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("connections.data"));
			out.writeObject(clients);
			out.close();
		}
		catch(IOException i) {}
	}

	public String dateToSign(int day, int month)
	{
		if((month == 3 && day > 20) || (month == 4 && day < 21))
			return "ariete";
		else if((month == 4 && day > 20) || (month == 5 && day < 21))
			return "toro";
		else if((month == 5 && day > 20) || (month == 6 && day < 22))
			return "gemelli";
		else if((month == 6 && day > 21) || (month == 7 && day < 23))
			return "cancro";
		else if((month == 7 && day > 22) || (month == 8 && day < 24))
			return "leone";
		else if((month == 8 && day > 23) || (month == 9 && day < 23))
			return "vergine";
		else if((month == 9 && day > 22) || (month == 10 && day < 23))
			return "bilancia";
		else if((month == 10 && day > 22) || (month == 11 && day < 23))
			return "scorpione";
		else if((month == 11 && day > 22) || (month == 12 && day < 22))
			return "sagittario";
		else if((month == 12 && day > 21) || (month == 1 && day < 21))
			return "capricorno";
		else if((month == 1 && day > 20) || (month == 2 && day < 20))
			return "acquario";
		else if((month == 2 && day > 19) || (month == 3 && day < 21))
			return "pesci";	
		return "unknown";
	}
}