package ru.mendeo.chronos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SunFactsActivity extends Activity
{
	public static final String SUN_FACTS_ID_KEY = "com.Chronos.sunfactsidkey";
	private static final long INVALID_SUN_FACT_ID = -1;
	private TextView mFullInfoText;
	private long mSunFactId;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.sun_facts);
        mFullInfoText = (TextView)findViewById(R.id.sun_facts_activity_full_info_text);
        if (savedInstanceState != null) 
        {
        	mSunFactId = savedInstanceState.getLong(SUN_FACTS_ID_KEY, INVALID_SUN_FACT_ID);
        }
        else if (getIntent() != null)
        {
        	mSunFactId = getIntent().getLongExtra(SUN_FACTS_ID_KEY, INVALID_SUN_FACT_ID);
        }
        if (mSunFactId != INVALID_SUN_FACT_ID)
        {
        	ExternalDbAdapter dBAdapter = new ExternalDbAdapter(this);
        	dBAdapter.open();
        	String fact = dBAdapter.getSunFactById(mSunFactId);
        	dBAdapter.close();
        	mFullInfoText.setText(fact);
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	outState.putLong(SUN_FACTS_ID_KEY, mSunFactId);
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
