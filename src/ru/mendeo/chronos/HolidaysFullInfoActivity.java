package ru.mendeo.chronos;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class HolidaysFullInfoActivity extends Activity
{
    private TextView mFullDescView, mShortDescView, mCategoryView;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.holidays_full_info);
        mFullDescView = (TextView)findViewById(R.id.HolidaysFullDescText);
        mShortDescView = (TextView)findViewById(R.id.HolidaysShortDescText);
        mCategoryView = (TextView)findViewById(R.id.HolidaysCategoryText);
        String FullDesc = getIntent().getStringExtra(ExternalDbAdapter.HOLIDAYS_KEY_FULL_DESC);
        String ShortDesc = getIntent().getStringExtra(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC);
        int typeId = getIntent().getIntExtra(ExternalDbAdapter.HOLIDAYS_KEY_TYPE, -1);
        String CategoryText="";
        switch (typeId)
        {
        	case 1:
        		CategoryText = getString(R.string.holidays_type1);
        	break;
        	case 2:
        		CategoryText = getString(R.string.holidays_type2);
        	break;
        	case 3:
        		CategoryText = getString(R.string.holidays_type3);
        	break;
        	case 4:
        		CategoryText = getString(R.string.holidays_type4);
        	break;        
        	case 5:
        		CategoryText = getString(R.string.holidays_type5);
        	break;        	
        	case 6:
        		CategoryText = getString(R.string.holidays_type6);
        	break;        	
        	case 7:
        		CategoryText = getString(R.string.holidays_type7);
        	break; 
        	default:
        		Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No such holydays type (error in Holidays table)");
        	break;
        }
        mShortDescView.setText(ShortDesc);        
        mFullDescView.setText(FullDesc);
        mCategoryView.setText(CategoryText);
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
