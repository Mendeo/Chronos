package ru.mendeo.chronos;

import android.content.SearchRecentSuggestionsProvider;

public class SearchSuggestionProvider extends SearchRecentSuggestionsProvider 
{
    public final static String AUTHORITY = "ru.mendeo.SearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchSuggestionProvider()
    {
        setupSuggestions(AUTHORITY, MODE);
    }
}
