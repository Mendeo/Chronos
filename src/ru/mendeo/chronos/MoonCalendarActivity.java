package ru.mendeo.chronos;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

public class MoonCalendarActivity extends Activity
{
	private TextView mFullInfoText;
	private Calendar mDateTime;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.moon_calendar);
        mFullInfoText = (TextView)findViewById(R.id.moon_calendar_activity_full_info_text);
        if (savedInstanceState != null)
        {
        	mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(savedInstanceState);
        }
        else if (getIntent() != null)
        {
        	Bundle extras = getIntent().getExtras();
        	if (extras != null)
        	{
        		mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(extras);
        	}
        }
    	ExternalDbAdapter dBAdapter = new ExternalDbAdapter(this);
    	dBAdapter.open();
    	Cursor cursor = dBAdapter.getMoonCalendarForCurrentDay(mDateTime);
		if (cursor != null)
		{
			int size = cursor.getCount();
			if (size > 0)
			{
				String textToOut = "";
				cursor.moveToFirst();
				int numberOfSpaces = 2;
				for (int i = 0; i < size; i++)
				{
					textToOut = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.MOON_CALENDAR_KEY_DATA));
					for (int j = 0; j < numberOfSpaces; j++) textToOut += "\n";
					cursor.moveToNext();
				}				
				textToOut = textToOut.substring(0, textToOut.length() - numberOfSpaces);
				mFullInfoText.setText(textToOut);
			}
		}
		cursor.close();
    	dBAdapter.close();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	PublicConstantsAndMethods.setCalendarToBundle(mDateTime, outState);
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
}