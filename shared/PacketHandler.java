package shared;

import java.net.DatagramPacket;

public interface PacketHandler
{
	public void packetReceived(DatagramPacket packet);
}