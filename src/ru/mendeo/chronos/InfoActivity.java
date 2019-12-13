package ru.mendeo.chronos;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends Activity
{
	private TextView mFullInfoText;
	private Button mUpdateCoordinatesBt;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        mFullInfoText = (TextView)findViewById(R.id.info_text);
        mUpdateCoordinatesBt = (Button)findViewById(R.id.update_coordinates_button);
        mFullInfoText.setText("");
        float offset =  (float)TimeZone.getDefault().getOffset(System.currentTimeMillis()) / 3600000;
        String offsetStr = (offset > 0) ? "+" + Float.toString(offset) : Float.toString(offset);
        offsetStr = offsetStr + " " + getString(R.string.hours);
        mFullInfoText.append(getString(R.string.current_time_zone) + " " + TimeZone.getDefault().getDisplayName() + " (" + offsetStr + " UTC)" + "\n");
        double longitude = getIntent().getDoubleExtra(getString(R.string.preferences_longitude_key), -1000.0);
        double latitude = getIntent().getDoubleExtra(getString(R.string.preferences_latitude_key), -1000.0);
        long locationTime = getIntent().getLongExtra(PublicConstantsAndMethods.LOCATION_TIME_KEY, -1000); //По умолчанию должно стоять отрицательное число, неравное -1.
        float locationAccuracy = getIntent().getFloatExtra(PublicConstantsAndMethods.LOCATION_ACCURACY_KEY, -1000); //По умолчанию должно стоять отрицательное число, неравное -1.        
        //Если координаты получены автоматически
        if (locationTime > 0 && locationAccuracy >= 0)
        {
        	String locationProvider = getIntent().getStringExtra(PublicConstantsAndMethods.LOCATION_PROVIDER_KEY);
        	mFullInfoText.append(getString(R.string.current_location_auto) + "\n");
    		mFullInfoText.append(getString(R.string.current_latitude) + " " + Double.toString(latitude) + "\n");
    		mFullInfoText.append(getString(R.string.current_longitude) + " " + Double.toString(longitude) + "\n");
    		if (locationProvider.equals(LocationManager.GPS_PROVIDER))
    		{
    			mFullInfoText.append(getString(R.string.current_location_provider_GPS) + "\n");
    		}
    		else if (locationProvider.equals(LocationManager.NETWORK_PROVIDER))
    		{
    			mFullInfoText.append(getString(R.string.current_location_provider_network) + "\n");
    		}
        	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PublicConstantsAndMethods.FULL_DATE_WITHOUT_SECONDS_FORMAT, Locale.getDefault());
    		mFullInfoText.append(getString(R.string.current_location_time) + " " + simpleDateFormat.format(new Date(locationTime)) + "\n");
    		mFullInfoText.append(getString(R.string.current_location_accuracy) + " " + Float.toString(locationAccuracy) + "\n");

        }
    	//Если координаты на данный момент неизвестны. (Идёт получение координат)
        else if (locationTime == PublicConstantsAndMethods.INVALID_LOCATION_TIME)
    	{
			if (PublicConstantsAndMethods.mLocationDenied)
			{
				mFullInfoText.append(getString(R.string.location_permission_denied));
			}
			else
			{
	        	mFullInfoText.append(getString(R.string.no_available_coordinates));
			}
    	}
        //Если координаты указаны вручную путём выбора города
        else
        {
        	mUpdateCoordinatesBt.setVisibility(View.GONE);
        	String locationRegion = getIntent().getStringExtra(getString(R.string.preferences_list_region_key));
        	String locationCity = getIntent().getStringExtra(getString(R.string.preferences_list_city_key));
        	mFullInfoText.append(getString(R.string.current_location_manual) + "\n");
        	mFullInfoText.append(getString(R.string.preferences_list_region_title) + " " + locationRegion + "\n");
        	mFullInfoText.append(getString(R.string.preferences_list_city_title) + " " + locationCity + "\n");
    		mFullInfoText.append(getString(R.string.current_latitude) + " " + Double.toString(latitude) + "\n");
    		mFullInfoText.append(getString(R.string.current_longitude) + " " + Double.toString(longitude) + "\n");
        }
        mUpdateCoordinatesBt.setOnClickListener(new View.OnClickListener()
        {			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				setResult(PublicConstantsAndMethods.RESULT_GET_NEW_LOCATION);
				finish();
			}
		});
    }
}
