package ru.mendeo.chronos;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

public class RateAppActivity extends Activity
{
	public static final String PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG = "ru.mendeo.timewhenshowrateourappdialog";
	private static final String REFERENCES_TO_MARKET = "market://details?id=ru.mendeo.chronos";
	public static final int TIME_AFTER_REPEAT_RATE_DIALOG = 3 * 86400 * 1000;
	private Button mOkButton, mLaterButton, mCancelButton;
	private SharedPreferences mSharedPreferences;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_app_dialog);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mOkButton = (Button)findViewById(R.id.RateAppOkButton);
        mLaterButton = (Button)findViewById(R.id.RateAppLaterButton);
        mCancelButton = (Button)findViewById(R.id.RateAppCancelButton);
        View.OnClickListener l = new View.OnClickListener()
    	{			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				SharedPreferences.Editor e = mSharedPreferences.edit();
				switch (v.getId())
				{
				case R.id.RateAppOkButton:
			    	e.putLong(PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG, -1);
			    	e.commit();
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setData(Uri.parse(REFERENCES_TO_MARKET));
					startActivity(intent);
					finish();
					break;
				case R.id.RateAppLaterButton:
			    	e.putLong(PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG, System.currentTimeMillis() + TIME_AFTER_REPEAT_RATE_DIALOG);
			    	e.commit();
			    	finish();
					break;
				case R.id.RateAppCancelButton:
			    	e.putLong(PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG, -1);
			    	e.commit();
			    	finish();
					break;
				}
			}
    	};
    	mOkButton.setOnClickListener(l);
    	mLaterButton.setOnClickListener(l);
    	mCancelButton.setOnClickListener(l);
    }
	//При нажатии кнопки "назад" срабатывает "напомнить позже".
	@Override
	public void onBackPressed()
	{
		SharedPreferences.Editor e = mSharedPreferences.edit();
		e.putLong(PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG, System.currentTimeMillis() + TIME_AFTER_REPEAT_RATE_DIALOG);
    	e.commit();
    	super.onBackPressed();
	}
}
