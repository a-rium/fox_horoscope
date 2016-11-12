package shared;

import java.io.IOException;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import java.util.Arrays;
import java.util.ArrayList;

public class UDPMessageSocket extends DatagramSocket
{ 
	private boolean running;
	private static int BUFFER_SIZE = 1024;
	private String ipAddr;
	private int port;
	private ArrayList<MessageHandler> messageHandlers;
	private ArrayList<PacketHandler> packetHandlers;

	public UDPMessageSocket(int port) throws IOException
	{
		super(port);
		running = true;
		messageHandlers = new ArrayList<MessageHandler>();
		packetHandlers = new ArrayList<PacketHandler>();
		Thread requestHandler = new Thread(() ->
		{
			DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
			while(running)
			{
				try
				{
					this.receive(packet);
					String message = new String(packet.getData()).trim();
					// applying the trim() method to the newly created String removes all the unused
					// characters after the terminator
					for(MessageHandler handler : messageHandlers)
						handler.messageReceived(message);
					for(PacketHandler handler : packetHandlers)
						handler.packetReceived(packet);
				}
				catch(SocketTimeoutException ste) { }
				catch(SocketException ste) { }
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
		messageHandlers.add(handler);
	}

	public void addPacketHandler(PacketHandler handler)
	{
		packetHandlers.add(handler);
	}	

	public void connect(String ipAddr, int port) throws IOException
	{
		this.ipAddr = ipAddr;
		this.port = port;
	}

	public void close()
	{
		running = false;
		super.close();
	}

	public void send(byte[] bytes, String ipAddr, int port) throws IOException
	{
		if(bytes != null && bytes.length < BUFFER_SIZE)
			super.send(new DatagramPacket(Arrays.copyOf(bytes, BUFFER_SIZE), BUFFER_SIZE, 
				InetAddress.getByName(ipAddr), port));
		else
			throw new IOException("Invalid message, bigger than the buffer");
	}

	public void send(byte[] bytes) throws IOException
	{
		send(bytes, this.ipAddr, this.port);
	}

	public void sendMessage(String message, String ipAddr, int port) throws IOException
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
		super.send(new DatagramPacket(packet, BUFFER_SIZE, InetAddress.getByName(ipAddr), port));
	}

	public void sendMessage(String message) throws IOException
	{
		sendMessage(message, this.ipAddr, this.port);
	}

	public DatagramPacket receive() throws IOException
	{
		DatagramPacket response = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
		super.receive(response);
		return response;
	}

	public String receiveMessage() throws IOException
	{
		DatagramPacket response = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
		super.receive(response);
		return new String(response.getData()).trim();
	}

	public void disableAutoReceive()
	{
		running = false;
	}
}