package server;

import shared.UDPMessageSocket;
import shared.RequestMessage;
import shared.InvalidMessageException;

import java.net.DatagramPacket;

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
				else if(message.equals("quit"))
				{
					System.out.println("Disconnecting...");
					System.exit(0);
				}
			}
			catch(InvalidMessageException ie)
			{
				ie.printStackTrace();
			}
		});
		clients = new HashMap<String, Integer>();
	}
}