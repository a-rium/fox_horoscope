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
					URL generatorWebSite = new URL("http://www.polygen.org/it/grammatiche/rubriche/ita/oroscopo.grm");
					BufferedReader in = new BufferedReader(new InputStreamReader(generatorWebSite.openStream()));
					String content = "";
					String line;
					while((line = in.readLine()) != null)
						content += line;
					String toFind = "<div class=\"generation\">";
					int start = content.indexOf(toFind);
					toFind = "<br>";
					start = content.indexOf(toFind, start) + toFind.length();
					start = content.indexOf(toFind, start) + toFind.length();
					toFind = "</div>";
					int end = content.indexOf(toFind, start) + toFind.length();
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
}