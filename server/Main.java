package server;

import java.io.IOException;

import shared.UDPMessageSocket;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			UDPMessageSocket client = new UDPMessageSocket(50000);
			Server server = new Server(50001);
			client.connect("localhost", 50001);
			client.sendMessage("quit");	

		}
		catch(IOException e)
		{
			e.printStackTrace(); 
		}
		// finally
		// {
		// 	System.exit(0);
		// }
	}
}