package com.n3p7un3.itunescontroller;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements OnSeekBarChangeListener, OnClickListener, OnRatingBarChangeListener {

	//private NetworkCommunicator _com;
	
	TextView _txtServerAddr;
	TextView _txtServerPort;
	TextView _txtStatus;
	volatile ToggleButton _tglPlaying;
	Button _btnCon;
	Button _btnNext;
	Button _btnPrev;
	Button _btnPlayByText;
	Button _btnWholePlaylist;
	Button _btnChangePlaylist;
	
	volatile SeekBar _sbVolume;
	SeekBar _sbProgress;
	RatingBar _rtb;
	TextView _tvProgress;
	TextView _tvCurPlaying;
	
	//boolean _doneReceivingSearchResults;
	//List<String> _searchResults;
	
	volatile boolean _receivingPlaypauseStatus;
	
	BroadcastReceiver _screenReceiver;
	boolean _currentlyForescreen;
	
	
	
	//NetworkCommunicator.NetworkComListener _localListener;	//to do: do we still need this?
	iTunesCommunicator _iTunesRemote;
	iTunesCommunicator.iTunesCommunicatorEvent _iTunesEventListener;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*
		_com = new NetworkCommunicator();
		_localListener = new NetworkCommunicator.NetworkComListener() {
			
			@Override
			public void fireEvent(NetworkEvent ne) {
				// TODO Auto-generated method stub
				NetworkComEvent(ne);
			}
		};
		_com.AddNetworkEventListener(_localListener);
		*/
		
		_txtServerAddr = (TextView) findViewById(R.id.txtServerAddress);
		_txtServerPort = (TextView) findViewById(R.id.txtServerPort);
		_txtStatus = (TextView) findViewById(R.id.txtStatus);
		_btnCon = (Button) findViewById(R.id.btnConnect);
		_btnCon.setOnClickListener(this);
		_tglPlaying = (ToggleButton) findViewById(R.id.tglPlaying);
		_receivingPlaypauseStatus = true;
		_sbVolume = (SeekBar) findViewById(R.id.sbVolume);
		_sbVolume.setOnSeekBarChangeListener(this);
		_btnNext = (Button) findViewById(R.id.btnNext);
		_btnPrev = (Button) findViewById(R.id.btnPrev);
		_btnPlayByText = (Button) findViewById(R.id.btnPlayByText);
		_btnNext.setOnClickListener(this);
		_btnPrev.setOnClickListener(this);
		_btnPlayByText.setOnClickListener(this);
		//_doneReceivingSearchResults = true;
		_rtb = (RatingBar) findViewById(R.id.ratingBar1);
		_rtb.setOnRatingBarChangeListener(this);
		_btnWholePlaylist = (Button) findViewById(R.id.btnWholePlaylist);
		_btnWholePlaylist.setOnClickListener(this);
		_btnChangePlaylist = (Button) findViewById(R.id.btnChangePlaylist);
		_btnChangePlaylist.setOnClickListener(this);
		_sbProgress = (SeekBar) findViewById(R.id.sbProgress);
		_sbProgress.setOnSeekBarChangeListener(this);
		_tvProgress = (TextView) findViewById(R.id.tvProgress);
		_tvCurPlaying = (TextView) findViewById(R.id.tvCurPlaying);
		
		
		_sbVolume.setEnabled(false);
		_rtb.setEnabled(false);
		_sbProgress.setEnabled(false);
		
		//ChangeConnectionState();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        _screenReceiver = new ScreenDetector();
        registerReceiver(_screenReceiver, filter);
        
        _currentlyForescreen = true;
        
        _iTunesRemote = new iTunesCommunicator();
        CreateAndAddiTunesEventListener();
        
        
        
        
        
        
		
	}
	
	private void CreateAndAddiTunesEventListener()
	{
		_iTunesEventListener = new iTunesCommunicator.iTunesCommunicatorEvent() {
			
			@Override
			public void fireEvent(iTunesEvent event) {
				// TODO Auto-generated method stub
				iTunesEventMethod(event);
			}
		};
		
		_iTunesRemote.AddiTunesEventListener(_iTunesEventListener);
	}
	
	private void iTunesEventMethod(iTunesEvent event)
	{
		String msgDisconnected;
		
		switch (event.Type)
		{
		case ConnectionLostDisconnecting:
			msgDisconnected = (String) event.Data;
			DisableUiFunctionality(msgDisconnected);
			break;
			
		case ConnectionNeedsToRestart:
			String msg = (String) event.Data;
			makeAToast(msg, Toast.LENGTH_SHORT);
			
			break;
			
		case Reconnecting:
			//
			break;
			
		case WelcomeMsg:
			EnableUiFunctionality();
			break;
			
		case Disconnected:
			msgDisconnected = (String) event.Data;
			DisableUiFunctionality(msgDisconnected);
			break;
			
		case PlayPauseStateMsg:
			PlayPauseState theState = (PlayPauseState) event.Data;
			SetPlayPauseButtonState(theState.IsPlaying);
			break;
			
		case VolumeStateMsg:
			IntValue theVolume = (IntValue) event.Data;
			SetVolumeState(theVolume.Value);
			break;
			
		case SearchResultsReadyMsg:
			SearchResults results = (SearchResults) event.Data;
			PromptForSearchResultsSelection(results);
			
			
			break;
			
		case NoSearchResultsMsg:
			makeAToast("No results.", Toast.LENGTH_SHORT);
			break;
		
		case NewRatingAvailableMsg:
			IntValue theRating = (IntValue) event.Data;
			SetRating(theRating.Value);
			break;
		
		case CurrentTrackTimeMsg:
			TwoIntValues theVals = (TwoIntValues) event.Data;
			SetCurTimeDisplay(theVals.Val1, theVals.Val2);
			break;
			
		case CurrentTrackNameMsg:
			String trackInfo = (String) event.Data;
			SetCurTrackName(trackInfo);
			break;
			
			
		
		
				
				
		}
	}
	
	private void SetCurTrackName(final String trackInfo)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				_tvCurPlaying.setText(trackInfo);
				
			}
			
		});
	}
	
	private void SetCurTimeDisplay(final int ptime, final int totalTrack)
	{
		//final int duration = Integer.parseInt(splitBySpace[2]);
		final String strDuration = getHHMMSS(totalTrack);
		//final int ptime = Integer.parseInt(splitBySpace[3]);
		final String strPtime = getHHMMSS(ptime);
		
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				_sbProgress.setMax(totalTrack);
				_sbProgress.setProgress(ptime);
				
				_tvProgress.setText(strPtime + " / " + strDuration);
			}
			
		});
	}
	
	private void SetRating(final int rating)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				_rtb.setRating((float) rating);
				
			}
			
		});
	}
	
	private void SetVolumeState(final int volume)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				_sbVolume.setProgress(volume);
			}
			
		});
	}
	
	private void SetPlayPauseButtonState(final boolean playing)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
				_receivingPlaypauseStatus = true;

				if (playing)
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
		
		
	}
	
	protected void onPause()
	{
		AttemptToDisconnect("pause");
		_currentlyForescreen = false;
		
		super.onPause();
	}
	
	protected void onResume()
	{
		AttemptToConnect();
		_currentlyForescreen = true;
		
		super.onResume();
	}
	
	protected void onDestroy()
	{
		unregisterReceiver(_screenReceiver);
		
		AttemptToDisconnect("exiting");
		
		super.onDestroy();
	}
	
	private class ScreenDetector extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context arg0, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
	            AttemptToDisconnect("screenoff");
	        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
	            if (_currentlyForescreen)
	            	AttemptToConnect();
	        }
			
		}
		
		
	}
	
	private void AttemptToDisconnect(String reason)
	{
		if (_iTunesRemote.IsAlreadyConnected())
        {
        	//Was connected, let's disconnect
			_iTunesRemote.Disconnect(reason);
        }
		
		DisableUiFunctionality(reason);
	}
	
	private void AttemptToConnect()
	{
    	//currently disconnected, let's try to connect
		//Attempt to connect
		if (!_iTunesRemote.IsAlreadyConnected())
		{
			_iTunesRemote.AttemptConnect(_txtServerAddr.getText().toString(), Integer.parseInt(_txtServerPort.getText().toString()));
		
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					_txtStatus.setText("Attempting to connect...");
					_btnCon.setText("Disconnect");
				}
				
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		
		return true;
	}
	
	private void ConButtonClicked()
	{
		ChangeConnectionState();
	}
	
	private void ChangeConnectionState()
	{
		if (!_iTunesRemote.IsAlreadyConnected())
		{
			AttemptToConnect();
		} 
		else
		{
			AttemptToDisconnect("User disconnected.");
		}
		
	}
	
	private void DisableUiFunctionality(final String reason)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				_btnCon.setText("Connect");
				_txtStatus.setText(reason);
				_tglPlaying.setEnabled(false);
				_sbVolume.setEnabled(false);
				_btnNext.setEnabled(false);
				_btnPrev.setEnabled(false);
				_btnPlayByText.setEnabled(false);
				_rtb.setEnabled(false);
				_btnWholePlaylist.setEnabled(false);
				_sbProgress.setEnabled(false);
				_btnChangePlaylist.setEnabled(false);
			}
		});
	}
	
	private void EnableUiFunctionality()
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				_btnCon.setText("Disconnect");
				_txtStatus.setText("Connected");
				
				_tglPlaying.setEnabled(true);
				_sbVolume.setEnabled(true);
				_btnNext.setEnabled(true);
				_btnPrev.setEnabled(true);
				_btnPlayByText.setEnabled(true);
				_rtb.setEnabled(true);
				_btnWholePlaylist.setEnabled(true);
				_sbProgress.setEnabled(true);
				_btnChangePlaylist.setEnabled(true);
			}
		});
	}
	//to do: defunct
	/*
	private void NetworkComEvent(final NetworkEvent ne)
	{
		if (ne.EventType == NetworkEventType.NeedToRestart)
		{
			//makeAToast(ne.Data, Toast.LENGTH_SHORT);
		} else if (ne.EventType == NetworkEventType.PacketReceived)
		{
			parsePacket(ne.Data);
			
			//makeAToast("packet received: " + ne.Data, Toast.LENGTH_SHORT);
			
			
		} else if (ne.EventType == NetworkEventType.Disconnected)
		{
			//DisableUiFunctionality(ne.Data);
			
			//_receivingPlaypauseStatus = true;
			
		}
	}
	*/
	
	
	
	private String getHHMMSS(int seconds)
	{
		String val = "";
		int hours = 0, minutes = 0;
		String strHours, strMinutes, strSeconds;
		
		while (seconds >= (60*60))
		{
			seconds -= (60*60);
			++hours;
		}
		while (seconds >= 60)
		{
			seconds -= 60;
			++minutes;
		}
		
		strHours = String.format("%02d", hours);
		strMinutes = String.format("%02d", minutes);
		strSeconds = String.format("%02d", seconds);
		
		val = strHours + ":" + strMinutes + ":" + strSeconds;
		
		return val;
	}
	
	public void TogglePlayPause(View v)
	{
		
		if (!_receivingPlaypauseStatus)
		{
			//get checked state
			runOnUiThread(new Runnable() {
	
				@Override
				public void run() {
					
						if (_tglPlaying.isChecked())
							_iTunesRemote.Play();
						else
							_iTunesRemote.Pause();
				}
				
			});
		}
		
		//_tglPlaying.setChecked(_tglPlaying.isChecked());
	}
	
	private void makeAToast(final String msg, final int duration)
	{
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Toast.makeText(getApplicationContext(), msg, duration).show();
			}
			
		});
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		
		if (fromUser)
		{
			if (seekBar.getId() == R.id.sbVolume)
				_iTunesRemote.SetVolume(progress);
			else if (seekBar.getId() == R.id.sbProgress)
				_iTunesRemote.SetProgress(progress);
		}
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
		case R.id.btnNext:
			_iTunesRemote.Next();
			break;
		case R.id.btnPrev:
			_iTunesRemote.Previous();
			break;
		case R.id.btnPlayByText:
			PromptForSearch();
			break;
		case R.id.btnWholePlaylist:
			_iTunesRemote.GetWholePlaylist();
			break;
		case R.id.btnConnect:
			ConButtonClicked();
			break;
		case R.id.btnChangePlaylist:
			_iTunesRemote.GetPlaylists();
			break;
			
		}
		
	}
	
	private void PromptForSearchResultsSelection(SearchResults results)
	{
		
		final CharSequence[] theList = results.GetSearchResults();
		final SearchResults.SearchResultsType type = results.GetSearchResultsType();
		
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Select a search result");
		//build the CharSequence[]
		final int listSize = theList.length;
		CharSequence[] theListCorrected = new CharSequence[listSize];
		for (int i = 0; i < listSize; ++i)
		{
			theListCorrected[i] = theList[listSize - i - 1];
		}
		builder.setItems(theList, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (type == SearchResults.SearchResultsType.Songs)
					_iTunesRemote.Play(listSize - which);
				else
					_iTunesRemote.SetPlaylist(listSize - which);
				
			}
			
		});
		
		runOnUiThread(new Runnable () {

			@Override
			public void run() {
				builder.show();
			}
			
		});
	}
	
	private void RequestAllPlaylists()
	{
		
	}
	
	private void PromptForSearch()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Play song");
		alert.setMessage("Enter search text");
		
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setNeutralButton("Search", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Editable search = input.getText();
				_iTunesRemote.SearchCurrentPlaylist(search.toString());
				
				//Hide the keyboard after the user has clicked ok
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
			
		});
		
		alert.setPositiveButton("I'm feeling lucky", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				Editable search = input.getText();
				_iTunesRemote.SearchCurrentPlaylistLucky(search.toString());
				
				//Hide the keyboard after the user has clicked ok
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
			}
			
		});
		//input.requestFocus();
		
		alert.show();
		
		//Automatically show the keyboard for the user
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	/*
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
		
	}
	*/

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		if (fromUser)
		{
			_iTunesRemote.SetRatingCurrentTrack((int) rating);
		}
		
	}

}
