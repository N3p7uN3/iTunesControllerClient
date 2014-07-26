package com.n3p7un3.itunescontroller;

public class iTunesEvent<DataType> {
	
	public volatile iTunesEventType Type;
	public volatile DataType Data;
	
	public iTunesEvent(iTunesEventType type, DataType data)
	{
		Type = type;
		Data = data;
	}
	
}
