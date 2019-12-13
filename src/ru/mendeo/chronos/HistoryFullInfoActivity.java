package ru.mendeo.chronos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class HistoryFullInfoActivity extends Activity
{
    private TextView mFullDescView, mShortDescView, mYearView;
    private static final int INVALID_YEAR = -100347;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_full_info);
        mFullDescView = (TextView)findViewById(R.id.HistoryFullDescText);
        mShortDescView = (TextView)findViewById(R.id.HistoryShortDescText);
        mYearView = (TextView)findViewById(R.id.HistoryYearText);
        String FullDesc = getIntent().getStringExtra(ExternalDbAdapter.HISTORY_KEY_FULL_DESC);
        String ShortDesc = getIntent().getStringExtra(ExternalDbAdapter.HISTORY_KEY_SHORT_DESC);
        int year = getIntent().getIntExtra(ExternalDbAdapter.HISTORY_KEY_YEAR, INVALID_YEAR);
        if (year != INVALID_YEAR)
        {
	        mShortDescView.setText(ShortDesc);
	        mFullDescView.setText(FullDesc);
	        String yearStr;
    		if (year < 0)
    		{
    			yearStr = Integer.toString(-year) + " " + getString(R.string.year_abbreviation) + " " + getString(R.string.year_bc_abbreviation);	
    		}  
    		else
    		{
    			yearStr = Integer.toString(year) + " " + getString(R.string.year_abbreviation);
    		}
	        mYearView.setText(yearStr);
        }
        else
        {
        	Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No such history ID (error in History table)");
        	mShortDescView.setText("ERROR!");
        }
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