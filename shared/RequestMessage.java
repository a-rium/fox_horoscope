package shared;

import java.util.Arrays;

public class RequestMessage
{
	private static final String signatureString = "request";
	private byte[] requestData, signature;
	private byte[][] parametersData;
	private static final int REQUEST_LENGTH 	= 20;
	private static final int PARAMETER_LENGTH 	= 20;
	private static final int MAXIMUM_PARAMETERS	= 20;

	public RequestMessage(String request, String[] parameters) throws InvalidMessageException
	{
		byte[] requestData = request.getBytes();
		if(requestData.length > REQUEST_LENGTH)
			throw new InvalidMessageException("Request is too long, " + REQUEST_LENGTH + " bytes maximum.");
		else if(parameters != null && parameters.length > MAXIMUM_PARAMETERS)
			throw new InvalidMessageException("Too many parameters, " + MAXIMUM_PARAMETERS + " is the maximum permitted.");
		signature = signatureString.getBytes();
		this.requestData = Arrays.copyOf(requestData, REQUEST_LENGTH);
		parametersData = new byte[MAXIMUM_PARAMETERS][];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
			parametersData[i] = new byte[PARAMETER_LENGTH];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
		{
			if(parameters != null && i < parameters.length)
			{
				byte[] parameterData = parameters[i].getBytes(); 
				if(parameterData.length > PARAMETER_LENGTH)
					throw new InvalidMessageException("\"" + parameters[i] + "\" is too long to be a parameter");
				else
					this.parametersData[i] = Arrays.copyOf(parameterData, PARAMETER_LENGTH);
			}
			else
				parametersData[i] = new byte[PARAMETER_LENGTH];	
		}
	}

	public RequestMessage(byte[] bytes) throws InvalidMessageException
	{
		int index = 0;
		signature = signatureString.getBytes();
		if(bytes.length < signature.length + REQUEST_LENGTH + PARAMETER_LENGTH * MAXIMUM_PARAMETERS)
			throw new InvalidMessageException("Cannot build request message with given bytes, signature does not match");
		for(int i = 0; i<signature.length; i++)
			if(bytes[index++] != signature[i])
				throw new InvalidMessageException("Cannot build request message with given bytes, signature does not match");
		requestData = new byte[REQUEST_LENGTH];
		parametersData = new byte[MAXIMUM_PARAMETERS][];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
			parametersData[i] = new byte[PARAMETER_LENGTH];
		for(int i = 0; i<REQUEST_LENGTH; i++)
			requestData[i] = bytes[index++];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
			for(int j = 0; j<PARAMETER_LENGTH; j++)
				parametersData[i][j] = bytes[index++];	
	}

	public byte[] getBytes()
	{
		byte[] data = new byte[signature.length + REQUEST_LENGTH + PARAMETER_LENGTH * MAXIMUM_PARAMETERS];
		int index = 0;
		for(int i = 0; i<signature.length; i++)
			data[index++] = signature[i];
		for(int i = 0; i<REQUEST_LENGTH; i++)
			data[index++] = requestData[i];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
			for(int j = 0; j<PARAMETER_LENGTH; j++)
				data[index++] = parametersData[i][j];	
		return data;
	}

	public String getRequest()
	{
		return new String(requestData).trim();
	}

	public String getParameter(int index)
	{
		String parameter = null;
		try
		{
			parameter = new String(parametersData[index]).trim();
		}
		catch(IndexOutOfBoundsException ie)
		{
			ie.printStackTrace();
		}
		return parameter;
	}

	public String[] getParameters()
	{
		String[] parameters = new String[MAXIMUM_PARAMETERS];
		for(int i = 0; i<MAXIMUM_PARAMETERS; i++)
			parameters[i] = new String(parametersData[i]).trim();
		return parameters;
	}
}