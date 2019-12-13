package ru.mendeo.chronos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class SunAstroInfoActivity extends Activity
{
	private static final String FULL_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss.SSS";
	private static final String ONLY_TIME_FORMAT = "HH:mm:ss.SSS";
	public static final String LONGITUDE_KEY = "com.Chronos.Longitude";
	public static final String LATITUDE_KEY = "com.Chronos.Latitude";
	private double mLongitude;
	private double mLatitude;
	private TextView mSunTimeZoneText, mSunCurTimeText, mSunRiseText, mSunTransitText, mSunSettingText, mSunLengthOfDayText, mSunHeightText, mSunAzimuthText, mSunTransitHeightText;
	private Calendar mDateTime;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sun_astro_info);
        mSunTimeZoneText = (TextView)findViewById(R.id.SATimeZoneText);
        mSunCurTimeText = (TextView)findViewById(R.id.SACurTimeText);        
        mSunRiseText = (TextView)findViewById(R.id.SARiseText);
        mSunTransitText = (TextView)findViewById(R.id.SATransitText);
        mSunSettingText = (TextView)findViewById(R.id.SASettingText);
        mSunLengthOfDayText = (TextView)findViewById(R.id.SALengthOfDayText);
        mSunHeightText = (TextView)findViewById(R.id.SAHeightText);
        mSunAzimuthText = (TextView)findViewById(R.id.SAAzimuthText);
        mSunTransitHeightText = (TextView)findViewById(R.id.SATransitHeightText);
        mDateTime = null;
        if (savedInstanceState != null) 
        {
        	mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(savedInstanceState);
        	mLongitude = savedInstanceState.getDouble(LONGITUDE_KEY, PublicConstantsAndMethods.INVALID_COORDINATES);
        	mLatitude = savedInstanceState.getDouble(LATITUDE_KEY, PublicConstantsAndMethods.INVALID_COORDINATES);
        }
        else if (getIntent() != null)
        {
        	Bundle extras = getIntent().getExtras();
        	if (extras != null)
        	{
        		mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(extras);
        		mLongitude = extras.getDouble(LONGITUDE_KEY, PublicConstantsAndMethods.INVALID_COORDINATES);
        		mLatitude = extras.getDouble(LATITUDE_KEY, PublicConstantsAndMethods.INVALID_COORDINATES);
        	}
        }
        ShowSunData(mDateTime);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	PublicConstantsAndMethods.setCalendarToBundle(mDateTime, outState);
    	outState.putDouble(LONGITUDE_KEY, mLongitude);
    	outState.putDouble(LATITUDE_KEY, mLatitude);    	
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
    private void ShowSunData(Calendar CalcTime)
    {
        //Пишем название временной зоны.
    	TimeZone tz = CalcTime.getTimeZone();
        float offset =  (float)tz.getOffset(System.currentTimeMillis()) / 3600000;
        String offsetStr = (offset > 0) ? "+" + Float.toString(offset) : Float.toString(offset);
        offsetStr = offsetStr + " " + getString(R.string.hours);
        mSunTimeZoneText.setText(getString(R.string.current_time_zone) + " " + tz.getDisplayName() + " (" + offsetStr + " UTC)");
        //Пишем текущее время.
        SimpleDateFormat TotalDateFormat = new SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault());
        mSunCurTimeText.setText(getString(R.string.date_and_time) + " " + TotalDateFormat.format(CalcTime.getTime()));
    	if ((mLongitude - PublicConstantsAndMethods.INVALID_COORDINATES < PublicConstantsAndMethods.APPROXIMATELY_ZERO) || (mLatitude - PublicConstantsAndMethods.INVALID_COORDINATES < PublicConstantsAndMethods.APPROXIMATELY_ZERO) || mDateTime == null)
        {
    		if (PublicConstantsAndMethods.mLocationDenied)
    		{
    			mSunRiseText.setText(getString(R.string.location_permission_denied));
    		}
    		else
    		{
    			mSunRiseText.setText(getString(R.string.no_available_coordinates));
    		}
        }
        else
        {
	        SimpleDateFormat SmallDateFormat = new SimpleDateFormat(ONLY_TIME_FORMAT, Locale.getDefault());
	        DecimalFormat decFormat;
	        //Пишем время восхода Солнца.
			Calendar TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mSunRiseText.setText(getString(R.string.astro_sun_rise_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mSunRiseText.setText(getString(R.string.astro_no_sun_rise));
			}
	        //Пишем время зенита Солнца.
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_TRANSIT, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mSunTransitText.setText(getString(R.string.astro_sun_transit_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mSunTransitText.setText(getString(R.string.astro_sun_no_transit));
			}
	        //Пишем время захода Солнца.
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mSunSettingText.setText(getString(R.string.astro_sun_set_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mSunSettingText.setText(getString(R.string.astro_no_sun_set));
			}
	        //Пишем длительность светового дня.		
			long LengthOfDaylightInMillis = AstroCalcModules.GetLengthOfDaylightInMillis(CalcTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude);
			if (LengthOfDaylightInMillis == Long.MAX_VALUE)
			{
				mSunLengthOfDayText.setText(getString(R.string.astro_no_day_lenth));
			}
			else
			{
				decFormat = new DecimalFormat("00");
				LengthOfDaylightInMillis = Math.abs(LengthOfDaylightInMillis);
				int[] LengthOfDaylight = AstroCalcModules.ConvertMillisToHHMMSSsss(LengthOfDaylightInMillis);		
				mSunLengthOfDayText.setText(getString(R.string.astro_day_lenth) + " " + decFormat.format(LengthOfDaylight[0]) + "ч " + decFormat.format(LengthOfDaylight[1]) + "м " + decFormat.format(LengthOfDaylight[2]) + "." + decFormat.format(LengthOfDaylight[3]) + "с");
			}		
	        //Пишем высоту Солнца над горизонтом.
			decFormat = new DecimalFormat("#.###");
			mSunHeightText.setText(getString(R.string.astro_sun_height) + " " + decFormat.format(AstroCalcModules.GetObjectHeightOrSouthAzimuth(CalcTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude, true) / AstroCalcModules.RPD) + " град.");
	        //Пишем азимут Солнца от юга.
			mSunAzimuthText.setText(getString(R.string.astro_sun_azimuth) + " " + decFormat.format(AstroCalcModules.GetObjectHeightOrSouthAzimuth(CalcTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude, false) / AstroCalcModules.RPD) + " град.");
			//Пишем высоту Солнца в зените.
			double TransitHeight = AstroCalcModules.GetObjectTransitHeightPreserve(CalcTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude);
			if (TransitHeight != Double.MAX_VALUE)
			{
				mSunTransitHeightText.setText(getString(R.string.astro_sun_max_height) + " " + decFormat.format(TransitHeight / AstroCalcModules.RPD) + " град.");
			}
			else
			{
				mSunTransitHeightText.setText(getString(R.string.astro_no_sun_max_height));
			}
        }
    }
}
