package server;

import shared.UDPMessageSocket;

import java.io.IOException;

public class Server
{
	private UDPMessageSocket socket;

	public Server(int port) throws IOException
	{
		socket = new UDPMessageSocket(port);
		socket.addMessageHandler((String message) ->
		{
			System.out.println(message.length());
			if(message.equals("quit"))
			{
				System.out.println("Socket " + port + " closing.");
				System.exit(0);
			}
		});
	}
}