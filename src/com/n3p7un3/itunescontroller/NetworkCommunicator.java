package com.n3p7un3.itunescontroller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;



public class NetworkCommunicator {
	
	private volatile Socket _theClient;
	private volatile SocketAddress _sa;

	private NetworkComListener _lstMaster;
	private List<NetworkComListener> _listeners;
	
	private ClientPacketSender _packetSender;
	private Thread _packetSenderThread;
	
	private ClientPacketReader _packetReader;
	private Thread _packetReceiverThread;
	
	//private volatile Queue<String> _packetSendQueue;
	Thread _clientConnectThread;
	private volatile boolean _disconnected;
	private Timer _watchdog;
	private int _watchdogInterval = 1000;
	
	public NetworkCommunicator()
	{
		_theClient = new Socket();
		//_packetSendQueue = new ;
		_listeners = new ArrayList<NetworkComListener>();
		_disconnected = true;
		
		_lstMaster = new NetworkComListener() {

			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.NetworkEvent ne) {
				// TODO Auto-generated method stub
				for (NetworkComListener temp : _listeners)
				{
					temp.fireEvent(ne);
				}
				
			}
		};
		
		//This is the listener this class will use.
		NetworkComListener _localListener = new NetworkComListener() {

			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.NetworkEvent ne) {
				// TODO Auto-generated method stub
				NetworkEvent(ne);
				
			}
			
		};
		
		//Now, add it to the list such that, when an event is fired, this is able to catch it
		_listeners.add(_localListener);
	}	
	
	public void AddNetworkEventListener(NetworkComListener listener) { _listeners.add(listener); }
	
	public boolean IsAlreadyConnected() { return !_disconnected; }
	
	public void AttemptConnect(String serverAddr, int port)
	{
		try {
			_disconnected = false;
			InetAddress addr = InetAddress.getByName(serverAddr);
			
			_sa = new InetSocketAddress(addr, port);
			
			AttemptConnect(_sa);
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void AttemptConnect(SocketAddress sa)
	{
		//CloseSockets("safety before trying to connect", false);
		
		_theClient = new Socket();
			
		_clientConnectThread = new Thread(new ClientConnectThread());
		_clientConnectThread.start();
	}
	
	private void KillThread(Thread thread)
	{
		if (thread != null)
		{
			try {
				thread.join();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		thread = null;
	}
	
	public void Disconnect(String reason)
	{
		if (_theClient.isConnected())
			SendPacket("disconnecting");
		
		//need to wait for "disconnecting" packet to send before proceeding
		
		//CloseSockets(reason, true);
		
		
	}
	
	private void CloseSockets(String reason, boolean notify)
	{
		if (_watchdog != null)
		{
			_watchdog.cancel();
			_watchdog.purge();
		}
		
		if (_packetSenderThread != null)
			_packetSenderThread.interrupt();
		
		if (_packetReceiverThread != null)
			_packetReceiverThread.interrupt();
		
		if (_clientConnectThread !=null)
			_clientConnectThread.interrupt();
		
		
//		KillThread(_clientConnectThread);
//		KillThread(_thePacketSender);
//		KillThread(_thePacketReceiver);
		
//		try {
//			Thread.sleep(600);
//		} catch (InterruptedException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		try {
			_theClient.shutdownOutput();
			_theClient.shutdownInput();
			
			_theClient.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (notify)
			NotifyDisconnected(reason);
		
		
		
		
	}
	
	private void NotifyDisconnected(String reason)
	{
		_disconnected = true;
		_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.Disconnected, reason));
	}
	public void RestartConnect(String reason)
	{
		CloseSockets(reason, true);
		
		AttemptConnect(_sa);
	}
	
	
	
	private class ClientConnectThread implements Runnable
	{
		
		public ClientConnectThread()
		{
			//nothing yet
		}
		
		public void run()
		{
			if (_theClient.isConnected())
			{
				_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.NeedToRestart, "Socket reset, reconnecting..."));
			}
			else
			{
				try {
					_theClient.connect(_sa, 1000);
					if (_theClient.isConnected())
					{
						_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.Connected, _theClient.getRemoteSocketAddress().toString()));
					}
						
					else
					{
						_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.ComFailureDisconnecting, "Could not connect, timed out"));
					}
					
				} catch (Exception e) {
					_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.ComFailureDisconnecting, "Could not connect: " + e.toString()));
				}
			
				

			}
		}
	}
	
	//This handles network events.
	private void NetworkEvent(NetworkEvent ne)
	{
		if (ne.EventType == NetworkEventType.Connected)
		{
			//We are connected, do the necessary stuff
			_disconnected = false;
			
			_packetSender = new ClientPacketSender(_theClient/*, _packetSendQueue*/);
			_packetSenderThread = new Thread(_packetSender);
			_packetSenderThread.start();
			
			_packetReader = new ClientPacketReader(_theClient);
			_packetReceiverThread = new Thread(_packetReader);
			_packetReceiverThread.start();
			
			//start a timer
			_watchdog = new Timer();
			
			_watchdog.schedule(new TimerTask() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					SendPacket("keepAlive");
				}
				
			}, _watchdogInterval, _watchdogInterval);
			
			
			Log.w("debugging", "started packet sender thread");
			
			SendPacket("hello");
			
		} else if (ne.EventType == NetworkEventType.NeedToRestart)
		{
			RestartConnect(ne.Data);
		} else if (ne.EventType == NetworkEventType.PacketReceived)
		{
			//Packet is here!
			Log.w("debugging", "packet received: " + ne.Data);
//			if (ne.Data.equals("goodbye"))
//			{
//				CloseSockets("User closed connection.");
//			}
		} else if (ne.EventType == NetworkEventType.ComFailureDisconnecting)
		{
			CloseSockets(ne.Data, true);
		} else if (ne.EventType == NetworkEventType.PacketSendSuccess)
		{
			//a packet was successfully sent.
			//if (ne.EventType == NetworkEventType.Disconnecting)
			//	_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.Disconnecting, "User has disconnected."));
			
			if (ne.Data.equals("disconnecting"))
				CloseSockets("User disconnected.", true);
		}
		
	}
	
	public void SendPacket(String packet)
	{
		_packetSender.SendPacket(packet);
	}
	
	private class ClientPacketSender implements Runnable
	{
		private volatile Socket _theClient;
		private volatile Queue<String> _packetQueue;
		private PrintWriter _out;
		private char _endOfPacketChar;
		
		private volatile Object _packetReadyWait;
		
		public ClientPacketSender(Socket theClient/*, Queue<String> theQueue*/)
		{
			_theClient = theClient;
			_packetQueue = new LinkedList<String>();
			_endOfPacketChar = '\n';
			try {
				_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(_theClient.getOutputStream())), true);
				_packetReadyWait = new Object();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.w("debugging", "Could not start output stream");
				Thread.currentThread().interrupt();
			}
			
			
			
		}
		
		public void SendPacket(String packet)
		{
			_packetQueue.add(packet);
			try {
				_packetReadyWait.notifyAll();
			} catch (java.lang.IllegalMonitorStateException e) {
				// TODO Auto-generated catch block
				//nothing
			}
		}
		
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				while (!_packetQueue.isEmpty())
				{
					String poll, packet;
					
					poll = _packetQueue.poll();
					if ((poll != null) && (!poll.equals("")))
					{
						packet = poll + _endOfPacketChar;
						
						Log.w("debugging", "attempting to send: " + packet);
						
						_out.print(packet);
						//_out.flush();
						
						//Log.w("debugging", "got past output");
						if (_out.checkError())
						{
							Log.w("debugging", "WARNING: _out error");
							//Thread.currentThread().interrupt();
							_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.ComFailureDisconnecting, "Lost connection."));
							break;
						} else {
							_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.PacketSendSuccess, poll));
						}
					}
				}
				/*
				try {
					Thread.currentThread().sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					break;
				}
				*/
				
				synchronized(_packetReadyWait) {
					try {
						_packetReadyWait.wait(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
					}
				}
			}
			
			//Done
			Log.w("debugging", "ClientPacketSender stopped");
			_out.close();
		}
		
		
		
	}
	
	private class ClientPacketReader implements Runnable
	{
		private volatile Socket _theClient;
		private BufferedReader _br;
		
		public ClientPacketReader(Socket theClient)
		{
			_theClient = theClient;
			
			try {
				//_theClient.setSoTimeout(100);
				_br = new BufferedReader(new InputStreamReader(_theClient.getInputStream()));
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.ComFailureDisconnecting, e.getMessage()));
			} catch (IOException e1) {
				_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.ComFailureDisconnecting, e1.getMessage()));
			}
			
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!Thread.currentThread().isInterrupted())
			{
				String _buff = "";
				
				try {
					_buff = _br.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					_buff = "";
				}
				
				if (_buff != null)
				{
					if (!_buff.equals(""))
					{
						_lstMaster.fireEvent(new NetworkEvent(NetworkEventType.PacketReceived, _buff));
					}
				}
			}
			
			//Done
			try {
				_br.close();
				Log.w("debugging", "ClientPacketReader closed.");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	
	
	
	
	public interface NetworkComListener
	{
		void fireEvent(NetworkEvent ne);
	}
	
	
}