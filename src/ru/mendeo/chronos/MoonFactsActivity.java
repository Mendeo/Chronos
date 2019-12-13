package ru.mendeo.chronos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MoonFactsActivity extends Activity
{
	public static final String MOON_FACTS_ID_KEY = "com.Chronos.MOONfactsidkey";
	private static final long INVALID_MOON_FACT_ID = -1;
	private TextView mFullInfoText;
	private long mMoonFactId;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.moon_facts);
        mFullInfoText = (TextView)findViewById(R.id.moon_facts_activity_full_info_text);
        if (savedInstanceState != null) 
        {
        	mMoonFactId = savedInstanceState.getLong(MOON_FACTS_ID_KEY, INVALID_MOON_FACT_ID);
        }
        else if (getIntent() != null)
        {
        	mMoonFactId = getIntent().getLongExtra(MOON_FACTS_ID_KEY, INVALID_MOON_FACT_ID);
        }
        if (mMoonFactId != INVALID_MOON_FACT_ID)
        {
        	ExternalDbAdapter dBAdapter = new ExternalDbAdapter(this);
        	dBAdapter.open();
        	String fact = dBAdapter.getMoonFactById(mMoonFactId);
        	dBAdapter.close();
        	mFullInfoText.setText(fact);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	outState.putLong(MOON_FACTS_ID_KEY, mMoonFactId);
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