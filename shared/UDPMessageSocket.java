package shared;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;

import java.util.ArrayList;

public class UDPMessageSocket
{ 
	private DatagramSocket socket;
	private boolean running;
	private static int BUFFER_SIZE = 1024;
	private InetAddress addr;
	private int door;
	private ArrayList<MessageHandler> handlers;

	public UDPMessageSocket(int port) throws IOException
	{
		socket = new DatagramSocket(port);
		running = true;
		handlers = new ArrayList<MessageHandler>();
		Thread requestHandler = new Thread(() ->
		{
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			while(running)
			{
				try
				{
					socket.receive(packet);
					String message = new String(packet.getData()).trim();
					// applying the trim() method to the newly created String removes all the unused
					// characters after the terminator
					for(MessageHandler handler : handlers)
						handler.messageReceived(message);
				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		});
		requestHandler.start();
	}

	public void addMessageHandler(MessageHandler handler)
	{
		handlers.add(handler);
	}

	public void connect(String ipAddr, int door) throws IOException
	{
		// socket.connect(InetAddress.getByName(ipAddr), door);
		this.addr = InetAddress.getByName(ipAddr);
		this.door = door;
		System.out.println("H");
	}

	public boolean isConnected()
	{
		return socket.isConnected(); 
	}

	public void close() throws IOException
	{
		running = false;
		socket.close();
	}

	public void sendMessage(String message) throws IOException
	{
		// if(!isConnected())
		// 	throw new IOException("Socket isn't connected to anyone");
		// every message will be described an header block
		// bytes from 0 to 2: signature
		// bytes 3: number of segment(range from 1 to 255)
		byte[] bytes = message.getBytes();
		if(bytes.length == 0)
			throw new IOException("Given message is empty");
		else if(bytes.length > BUFFER_SIZE)
			throw new IOException("Given message is too long to be sent");
		byte[] packet = new byte[BUFFER_SIZE];
		for(int i = 0; i<bytes.length; i++)
			packet[i] = bytes[i];
		// the message gets divided into 1024 byte long segment and sent
		// since the messages are not necessarily received in order
		// a byte is appended(following a terminator byte) to the message, 
		// which will represent the segment position
		socket.send(new DatagramPacket(packet, BUFFER_SIZE, addr, door));
	}
}