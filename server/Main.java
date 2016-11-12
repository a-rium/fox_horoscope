package server;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

import shared.UDPMessageSocket;
import shared.RequestMessage;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			// UDPMessageSocket client = new UDPMessageSocket(50000);
			HoroscopeServer server = new HoroscopeServer(args.length > 0 ? Integer.parseInt(args[0]) : 50000);
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String line;
			while((line = in.readLine()) == "");
			server.close();
			// client.connect("localhost", 50001);
			// String[] parameters = { "utente" };
			// client.send(new RequestMessage("connect", parameters).getBytes());	
			// client.send(new RequestMessage("upgrade", parameters).getBytes());
			// client.send(new RequestMessage("upgrade", parameters).getBytes());		
			// client.send(new RequestMessage("quit", null).getBytes());	

		}
		catch(IOException e)
		{
			e.printStackTrace(); 
		}
	}
}