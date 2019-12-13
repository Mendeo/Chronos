package ru.mendeo.chronos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.view.View;
import android.widget.ListView;

public class SearchableActivity extends ListActivity 
{
	public static final String DATE_EXSTRAS_KEY = "ru.mendeo.search_result_date_exstras_key";
	private static final String DATE_TEXT = "ru.mendeo.search_result_date_in_text_format";
	private static final String DATE_CALENDAR = "ru.mendeo.search_result_date_in_Calendar_format";
	private ArrayList<HashMap<String, Object>> mArrayWithResult;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.searchable_activity);
        handleIntent(getIntent());
    }
	@Override
	protected void onNewIntent(Intent intent)
	{
	    setIntent(intent); //When the system calls onNewIntent(Intent), the activity has not been restarted, so the getIntent() method returns the same intent that was received with onCreate(). This is why you should call setIntent(Intent) inside onNewIntent(Intent) (so that the intent saved by the activity is updated in case you call getIntent() in the future).
	    handleIntent(intent);
	}
	private void handleIntent(Intent intent)
	{
        if (Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doMySearch(query);
        }
	}
	@Override
	protected void onListItemClick (ListView l, View v, int position, long id)
	{
		Calendar nedeedDate = (Calendar)mArrayWithResult.get(position).get(DATE_CALENDAR);
		Intent i = new Intent(this, ChronosMainActivity.class);
		Bundle bd = new Bundle();
		PublicConstantsAndMethods.setCalendarToBundle(nedeedDate, bd);
		i.putExtra(DATE_EXSTRAS_KEY, bd);
		i.setAction(PublicConstantsAndMethods.ACTION_AFTER_SEARCH);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); //Нужно, чтобы вызывалось onNewIntent в ChronosMainActivity (нужно вызвать уже имеющееся активити, а не создавать новое, хотя onCreate ChronosMainActivity тоже есть обработка этого нажатия на всякий случай).
		startActivity(i);
		finish();
	}
    @Override
    protected void onPause()
	{    
    	super.onPause();
    	if (isFinishing())
		{
			//Вызов диалога с просьбой поставить оценку в google play
			if (PublicConstantsAndMethods.mNeedToShowRateOurAppsDialog)
			{
				PublicConstantsAndMethods.mNeedToShowRateOurAppsDialog = false;
				Intent i = new Intent(getApplicationContext(), RateAppActivity.class);
				startActivity(i);
			}
		}
	}
    //Не знаю почему, но без этого программа не работает на андроидах больше 2.3.3. (В этом классе не проверял данное утверждение, а тупо скопировал код из ChronosMainActivity на всякий случай).
    @Override
    public void startManagingCursor(Cursor c)
    {
        // TODO Auto-generated method stub
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) super.startManagingCursor(c);
    }
	private void doMySearch(String query)
	{
		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE); //SearchSuggestionProvider - это мой класс, который наследует SearchRecentSuggestionsProvider
		suggestions.saveRecentQuery(query, null);
    	SharedPreferences.Editor e = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
    	e.putLong(PublicConstantsAndMethods.LAST_SEARCH_TIME_KEY, System.currentTimeMillis());
    	e.commit();
		ExternalDbAdapter dbAdapter = new ExternalDbAdapter(this);
		dbAdapter.open();
		Cursor cursor = dbAdapter.searchHolidays(query);
		if (cursor == null) return;
		int rows = cursor.getCount();
		HashMap<String, Object> hm;
		mArrayWithResult = new ArrayList<HashMap<String, Object>>();
    	long id;
    	String shortDesc;
    	int [] dateArr;
    	String dateStr;
    	Calendar currentDate = Calendar.getInstance(Locale.getDefault());
    	Calendar dateCal = Calendar.getInstance(Locale.getDefault());
		SimpleDateFormat timeFormat = new SimpleDateFormat(PublicConstantsAndMethods.ONLY_DATE_FORMAT, Locale.getDefault());
		String dateTime;
		String dayName;
		String dateStatus;
		String dateToShow;
    	cursor.moveToFirst();
    	for (int i = 0; i < rows; i++)
    	{
			id = cursor.getLong(cursor.getColumnIndex(ExternalDbAdapter.KEY_ROWID));
			shortDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC));
			//Пишем дату.
			dateStr = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_DAY));
			dateArr = ExternalDbAdapter.convertStringDayFromHolidaysToActualDate(dateStr, dateCal.get(Calendar.YEAR));
			dateCal.set(Calendar.DAY_OF_MONTH, dateArr[0]);
			dateCal.set(Calendar.MONTH, dateArr[1] - 1);
			dateTime = timeFormat.format(dateCal.getTime());
			dayName = PublicConstantsAndMethods.getDayOfWeek(dateCal, getResources().getStringArray(R.array.days_of_week));
			dateStatus = "(";
			if (dateCal.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR))
			{
				dateStatus += getString(R.string.current_date);
			}
			else if (dateCal.getTimeInMillis() > currentDate.getTimeInMillis())
			{
				dateStatus += getString(R.string.future_date);
			}
			else
			{
				dateStatus += getString(R.string.past_date);
			}
			dateStatus += ")";
			dateToShow = dateTime + " " + dayName + " " + dateStatus;
			//Закончили писать дату.
			hm = new HashMap<String, Object>();
			hm.put(ExternalDbAdapter.KEY_ROWID, id);
			hm.put(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC, shortDesc);
			hm.put(DATE_TEXT, dateToShow);
			hm.put(DATE_CALENDAR, dateCal);
			mArrayWithResult.add(hm);
			cursor.moveToNext();
    	}
       	cursor.close();
       	dbAdapter.close();
    	//Заполняем ListView
    	String[] from = {ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC, DATE_TEXT};
    	int[] to = {R.id.search_result_text, R.id.search_result_date};
    	SimpleAdapterWithCustomId adapter = new SimpleAdapterWithCustomId(this, mArrayWithResult, R.layout.search_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    	setListAdapter(adapter);
	}
}
