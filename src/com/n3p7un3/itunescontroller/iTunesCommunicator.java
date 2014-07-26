package com.n3p7un3.itunescontroller;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.Toast;

import com.n3p7un3.itunescontroller.NetworkCommunicator.NetworkComListener;

public class iTunesCommunicator {
	
	//private List<NetworkComListener> _networkEventListeners;
	private List<iTunesCommunicatorEvent> _iTunesEventListeners;
	//private NetworkComListener _lstNetworkEventMaster;
	private iTunesCommunicatorEvent _listiTunesEventMaster;
	private NetworkCommunicator _nc;
	
	private boolean _doneReceivingSearchResults;
	private ArrayList<String> _searchResults;
	
	public iTunesCommunicator()
	{
		
		//CreateMasterNetworkEventListener();
		
		//setup iTunes event listeners, including the local listener
		CreateMasteriTunesEventListener();
		AddLocaliTunesEventListener();
		
		_nc = new NetworkCommunicator();
		CreateAndAddLocalNetworkEventListener();
		
		_doneReceivingSearchResults = true;
		
		
		
		
	}
	
	/*
	private void CreateMasterNetworkEventListener()
	{
		_networkEventListeners = new ArrayList<NetworkComListener>();
		
		_lstNetworkEventMaster = new NetworkComListener() {

			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.NetworkEvent ne) {
				// TODO Auto-generated method stub
				for (NetworkComListener temp : _networkEventListeners)
				{
					temp.fireEvent(ne);
				}
				
			}
		};
	}
	*/
	
	public void AddiTunesEventListener(iTunesCommunicatorEvent itce) { _iTunesEventListeners.add(itce); }
	
	
	private void CreateMasteriTunesEventListener()
	{
		_iTunesEventListeners = new ArrayList<iTunesCommunicatorEvent>();
		
		
		_listiTunesEventMaster = new iTunesCommunicatorEvent() {

			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.iTunesEvent event) {
				// TODO Auto-generated method stub
				for (iTunesCommunicatorEvent temp : _iTunesEventListeners)
				{
					
					temp.fireEvent(event);
				}
			}
			
		};
	}
	
	private void AddLocaliTunesEventListener()
	{
		iTunesCommunicatorEvent itce = new iTunesCommunicatorEvent()
		{
			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.iTunesEvent event) {
				// TODO Auto-generated method stub
				iTunesEventMethod(event);
			}
			
			
		};
	}
	
	//private void 
	
	private void CreateAndAddLocalNetworkEventListener()
	{
		
		NetworkComListener _localListener = new NetworkComListener() {

			@Override
			public void fireEvent(com.n3p7un3.itunescontroller.NetworkEvent ne) {
				// TODO Auto-generated method stub
				NetworkEvent(ne);
				
			}
			
		};
		
		_nc.AddNetworkEventListener(_localListener);
		
	}
	
	protected void iTunesEventMethod(iTunesEvent itce)
	{
		
	
	}
	
	
	protected void NetworkEvent(NetworkEvent ne) {
		// TODO Auto-generated method stub
		switch (ne.EventType)
		{
		case PacketReceived:
			ParsePacket(ne.Data);
			break;
			
		case ComFailureDisconnecting:
			_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.ConnectionLostDisconnecting, ne.Data));
			break;
			
		case PacketSendSuccess:
			//
			break;
			
		case Disconnected:
			_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.Disconnected, ne.Data));
			break;
			
		case NeedToRestart:
			_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.ConnectionNeedsToRestart, ne.Data));
			break;
			
		case Connected:
			//
		}
		
	}
	
	private void ParsePacket(String packet)
	{
		final String[] splitBySpace = packet.split(" ");
		
		if (splitBySpace[0].equals("welcome"))
		{
			
			//EnableUiFunctionality();
			_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.WelcomeMsg, null));
//			//get the current play/pause state
//			_com.SendPacket("request playpausestate");
//			
//			//get the current volume
//			_com.SendPacket("request volume");
//			
//			//get the current rating of the selected track
//			_com.SendPacket("request rating");
			
			
		} else if (splitBySpace[0].equals("information"))
		{
			
			if (splitBySpace[1].equals("playpausestate"))
			{
				String state = splitBySpace[2];
				/*
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						Log.w("debugging", "ATTEMPTING TO CHANGE STATUS");
						
						_receivingPlaypauseStatus = true;
						

						if (state.equals("playing"))
						{
							//playing
							_tglPlaying.setChecked(true);
						}
						else
						{
							//paused
							_tglPlaying.setChecked(false);
						}
						
						_receivingPlaypauseStatus = false;
					}
					});
					*/
				
				//iTunesEvent.PlayPauseState stateObj;
				PlayPauseState stateObj = new PlayPauseState(false);
				
				if (state.equals("playing"))
					stateObj.IsPlaying = true;
				//else
					//iTunesEvent.PlayPauseState stateObj = new iTunesEvent.PlayPauseState(false);
				
				_listiTunesEventMaster.fireEvent(new iTunesEvent<PlayPauseState>(iTunesEventType.PlayPauseStateMsg, stateObj));
				
				
				
				
				
			} else if (splitBySpace[1].equals("volumestate"))
			{
				int volume = Integer.parseInt(splitBySpace[2]);
				/*
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						
						
						
						_sbVolume.setProgress(volume);
						
					}
					
				});
				*/
				
				IntValue volumeVal = new IntValue(volume);
				//volumeVal.Value = volume;
				
				_listiTunesEventMaster.fireEvent(new iTunesEvent<IntValue>(iTunesEventType.VolumeStateMsg, volumeVal));
				
				
			} else if (splitBySpace[1].equals("searchresults"))
			{
				if (_doneReceivingSearchResults)
				{
					//new search results, instantiate a new list so that the results can be added as they are received.
					_doneReceivingSearchResults = false;
					_searchResults = new ArrayList<String>();
				}
				
				ParseSearchResult(packet);
			} else if (splitBySpace[1].equals("searchresultsend"))
			{
				_doneReceivingSearchResults = true;
				
				/*
				 * 
				final AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Select a search result");
				//build the CharSequence[]
				final int listSize = _searchResults.size();
				CharSequence[] theList = new CharSequence[listSize];
				for (int i = 0; i < listSize; ++i)
				{
					theList[i] = _searchResults.get(listSize - i - 1);
				}
				builder.setItems(theList, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						_com.SendPacket("itunescommand play " + Integer.toString(listSize - which));
						
					}
					
				});
				
				runOnUiThread(new Runnable () {

					@Override
					public void run() {
						builder.show();
					}
					
				});
				
				*/
				int listSize = _searchResults.size();
				CharSequence[] theList = new CharSequence[listSize];
				for (int i = 0; i < listSize; ++i)
				{
					theList[i] = _searchResults.get(listSize - i - 1);
				}
				SearchResults results = new SearchResults(theList);
				//results.Results =  theList;
				_listiTunesEventMaster.fireEvent(new iTunesEvent<SearchResults>(iTunesEventType.SearchResultsReadyMsg, results));
				
				
			} else if (splitBySpace[1].equals("searchresult"))
			{
				if (splitBySpace[2].equals("none"))
				{
					//makeAToast("No search results.", Toast.LENGTH_SHORT);
					_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.NoSearchResultsMsg, ""));
				}
			} else if (splitBySpace[1].equals("rating"))
			{
				/*
				final int rating = Integer.parseInt(splitBySpace[2]) / 20;
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						_rtb.setRating((float) rating);
						
					}
					
				});
				*/
				IntValue value = new IntValue(Integer.parseInt(splitBySpace[2]) / 20);
				//value.Value = ;
				_listiTunesEventMaster.fireEvent(new iTunesEvent<IntValue>(iTunesEventType.NewRatingAvailableMsg, value));
				
			} else if (splitBySpace[1].equals("ptime"))
			{
				/*
				final int duration = Integer.parseInt(splitBySpace[2]);
				final String strDuration = getHHMMSS(duration);
				final int ptime = Integer.parseInt(splitBySpace[3]);
				final String strPtime = getHHMMSS(ptime);
				
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						_sbProgress.setMax(duration);
						_sbProgress.setProgress(ptime);
						
						_tvProgress.setText(strPtime + " / " + strDuration);
					}
					
				});
				*/
				int duration = Integer.parseInt(splitBySpace[2]);
				int ptime = Integer.parseInt(splitBySpace[3]);
				TwoIntValues trackTime = new TwoIntValues(ptime, duration);
				
				_listiTunesEventMaster.fireEvent(new iTunesEvent<TwoIntValues>(iTunesEventType.CurrentTrackTimeMsg, trackTime));
				
			
			} else if (splitBySpace[1].equals("trackname"))
			{
				//information trackname fasdfas
				String trackinfo = packet.substring(22);
				/*
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						_tvCurPlaying.setText(trackinfo);
						
					}
					
				});
				*/
				
				_listiTunesEventMaster.fireEvent(new iTunesEvent<String>(iTunesEventType.CurrentTrackNameMsg, trackinfo));
			}
		}
	}
	
	public void SearchCurrentPlaylist(String criteria)
	{
		_nc.SendPacket("itunescommand play playbysearch " + criteria);
	}
	
	public void SearchCurrentPlaylistLucky(String criteria)
	{	
		_nc.SendPacket("itunescommand play playbysearchlucky " + criteria);
	}
	
	public void AttemptConnect(String serverAddr, int port)
	{
		_nc.AttemptConnect(serverAddr, port);
	}
	
	public void Disconnect(String reason)
	{
		_nc.Disconnect(reason);
	}
	
	public void Play()
	{
		_nc.SendPacket("itunescommand playpause play");
	}
	
	public void Play(int num)
	{
		_nc.SendPacket("itunescommand play " + Integer.toString(num));
	}
	
	public void Pause()
	{
		_nc.SendPacket("itunescommand playpause pause");
	}
	
	public void Next()
	{
		_nc.SendPacket("itunescommand play next");
	}
	
	public void Previous()
	{
		_nc.SendPacket("itunescommand play previous");
	}
	
	public void GetWholePlaylist()
	{
		_nc.SendPacket("itunescommand play playbysearch ");
	}
	
	public void SetRatingCurrentTrack(int rating)
	{
		int corrected = rating * 20;
		_nc.SendPacket("itunescommand setrating " + Integer.toString(corrected));
		
	}
	
	public void SetVolume(int volume)
	{
		_nc.SendPacket("itunescommand setvolume " + Integer.toString(volume));
		
	}
	
	public void SetProgress(int progress)
	{
		_nc.SendPacket("itunescommand setprogress " + Integer.toString(progress));
	}
	
	public boolean IsAlreadyConnected()
	{
		return _nc.IsAlreadyConnected();
	}
	
	public void SelectedSearchResult(int which)
	{
		_nc.SendPacket("itunescommand play " + Integer.toString(_searchResults.size() - which));
	}
	
	private void ParseSearchResult(String packet)
	{
		
		String[] splitBySpace = packet.split(" ");
		
		int position = Integer.parseInt(splitBySpace[2]);
		//Build the string
		String searchresult = "";
		for (int i = 3; i < splitBySpace.length; ++i)
		{
			searchresult = searchresult +  " " + splitBySpace[i];
			
					
		}
		if (_searchResults.size() >= position)
		{
			_searchResults.add(position, searchresult);
		}
		else
		{
			_searchResults.add(searchresult);
		}
		
		
		/*
		String[] splitByTab = packet.split("\t");
		
		for (int i = 2; i < splitByTab.length; ++i)
		{
			
		}
		*/
		
		
	}

	//public void AddNetworkEventListener(NetworkComListener listener) { _networkEventListeners.add(listener); }
	
	public interface iTunesCommunicatorEvent
	{
		void fireEvent(iTunesEvent event);
	}

}



