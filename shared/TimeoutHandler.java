package shared;

import java.net.SocketTimeoutException;

public interface TimeoutHandler
{
	public void handle(SocketTimeoutException ste);
}