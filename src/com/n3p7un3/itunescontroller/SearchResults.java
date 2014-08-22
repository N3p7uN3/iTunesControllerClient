package com.n3p7un3.itunescontroller;

public class SearchResults
{
	public enum SearchResultsType {
		Songs,
		Playlists
	};
	
	CharSequence[] mResults;
	SearchResultsType mType;
	
	
	public SearchResults(CharSequence[] results, SearchResultsType type)
	{
		mResults = results;
		mType = type;
	}
	
	public SearchResultsType GetSearchResultsType() { return mType; }
	public CharSequence[] GetSearchResults() { return mResults; }
}

