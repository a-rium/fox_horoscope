package server;

import shared.UDPMessageSocket;
import shared.RequestMessage;
import shared.InvalidMessageException;

import java.net.DatagramPacket;
import java.net.URL;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

import java.util.HashMap;

public class HoroscopeServer extends UDPMessageSocket
{
	private HashMap<String, Integer> clients;

	public HoroscopeServer(int port) throws IOException
	{
		super(port);
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
					System.out.printf("Sending info to: IP: " + packet.getAddress().getHostAddress() + " Porta: " + packet.getPort() + "...");
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
				else if(message.equals("date"))
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
					System.out.println(end - start);
					String horoscopeParagraph = "<html>" + content.substring(start, end) + "</html>";
					sendMessage(horoscopeParagraph, packet.getAddress().getHostAddress(), packet.getPort());
					System.out.println("Paragraph sent");
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
		clients = new HashMap<String, Integer>();
	}
}