package client;

import shared.UDPMessageSocket;

import java.io.IOException;

public class HoroscopeClient extends UDPMessageSocket
{
	public HoroscopeClient(int port) throws IOException
	{
		super(port);
	}
}