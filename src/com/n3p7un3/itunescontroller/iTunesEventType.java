package com.n3p7un3.itunescontroller;

public enum iTunesEventType {
	ConnectionLostDisconnecting,
	Reconnecting,
	WelcomeMsg,
	Disconnected,
	ConnectionNeedsToRestart,
	
	PlayPauseStateMsg,
	VolumeStateMsg,
	SearchResultsReadyMsg,
	NoSearchResultsMsg,
	NewRatingAvailableMsg,
	CurrentTrackTimeMsg,
	CurrentTrackNameMsg,
	ShuffleStateMsg
}
