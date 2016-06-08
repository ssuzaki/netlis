package jp.codepanic.netlis;

import android.content.SearchRecentSuggestionsProvider;

public class SuggestionsProvider extends SearchRecentSuggestionsProvider {
	
    public final static String AUTHORITY = "SuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    
	public SuggestionsProvider(){
		setupSuggestions(AUTHORITY, MODE);
	}

}
