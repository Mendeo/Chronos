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

public class MoonAstroInfoActivity extends Activity
{
	private static final String FULL_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss.SSS";
	private static final String ONLY_TIME_FORMAT = "HH:mm:ss.SSS";
	public static final String LONGITUDE_KEY = "com.Chronos.Longitude";
	public static final String LATITUDE_KEY = "com.Chronos.Latitude";
	private double mLongitude;
	private double mLatitude;
	private TextView mMoonTimeZoneText, mMoonCurTimeText, mMoonVisibleText, mMoonRiseText, mMoonTransitText, mMoonSettingText, mMoonLengthOfDayText, mMoonHeightText, mMoonAzimuthText, mMoonTransitHeightText, mMoonDistanceText, mMoonPhaseText, mMoonNewMoonText, mMoonFirstQuarterText, mMoonFullMoonText, mMoonLastQuarterText;
	private Calendar mDateTime;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.moon_astro_info);
        mMoonTimeZoneText = (TextView)findViewById(R.id.MATimeZoneText);
        mMoonCurTimeText = (TextView)findViewById(R.id.MACurTimeText);
        mMoonVisibleText = (TextView)findViewById(R.id.MAVisibleText);
        mMoonRiseText = (TextView)findViewById(R.id.MARiseText);
        mMoonTransitText = (TextView)findViewById(R.id.MATransitText);
        mMoonSettingText = (TextView)findViewById(R.id.MASettingText);
        mMoonLengthOfDayText = (TextView)findViewById(R.id.MALengthOfDayText);
        mMoonHeightText = (TextView)findViewById(R.id.MAHeightText);
        mMoonAzimuthText = (TextView)findViewById(R.id.MAAzimuthText);
        mMoonTransitHeightText = (TextView)findViewById(R.id.MATransitHeightText);
        mMoonDistanceText = (TextView)findViewById(R.id.MADistanceText);
        mMoonPhaseText  = (TextView)findViewById(R.id.MAPhaseText);
        mMoonNewMoonText = (TextView)findViewById(R.id.MANewMoonText);
        mMoonFirstQuarterText = (TextView)findViewById(R.id.MAFirstQuarterText);
        mMoonFullMoonText = (TextView)findViewById(R.id.MAFullMoonText);
        mMoonLastQuarterText = (TextView)findViewById(R.id.MALastQuarterText);
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
        ShowMoonData(mDateTime);
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
    private void ShowMoonData(Calendar CalcTime)
    {
        //Пишем название временной зоны.
    	TimeZone tz = CalcTime.getTimeZone();
        float offset =  (float)tz.getOffset(System.currentTimeMillis()) / 3600000;
        String offsetStr = (offset > 0) ? "+" + Float.toString(offset) : Float.toString(offset);
        offsetStr = offsetStr + " " + getString(R.string.hours);
        mMoonTimeZoneText.setText(getString(R.string.current_time_zone) + " " + tz.getDisplayName() + " (" + offsetStr + " UTC)");
        //Пишем текущее время.
        SimpleDateFormat TotalDateFormat = new SimpleDateFormat(FULL_DATE_FORMAT, Locale.getDefault());
        mMoonCurTimeText.setText(getString(R.string.date_and_time) + " " + TotalDateFormat.format(CalcTime.getTime()));
    	if ((mLongitude - PublicConstantsAndMethods.INVALID_COORDINATES < PublicConstantsAndMethods.APPROXIMATELY_ZERO) || (mLatitude - PublicConstantsAndMethods.INVALID_COORDINATES < PublicConstantsAndMethods.APPROXIMATELY_ZERO) || mDateTime == null)
        {
    		if (PublicConstantsAndMethods.mLocationDenied)
    		{
    			mMoonVisibleText.setText(getString(R.string.location_permission_denied));
    		}
    		else
    		{
    			mMoonVisibleText.setText(getString(R.string.no_available_coordinates));
    		}
    		//Дальше костыль, убирающий надписи с заголовков.
    		((TextView)findViewById(R.id.MAMainPhasesTitle)).setText("");
    		((TextView)findViewById(R.id.MANewMoonTitle)).setText("");
    		((TextView)findViewById(R.id.MAFirstQuarterTitle)).setText("");
    		((TextView)findViewById(R.id.MAFullMoonTitle)).setText("");
    		((TextView)findViewById(R.id.MALastQuarterTitle)).setText("");
        }
        else
        {
	        SimpleDateFormat SmallDateFormat = new SimpleDateFormat(ONLY_TIME_FORMAT, Locale.getDefault());
	        DecimalFormat decFormat;
			//Пишем процент видимости Луны.
			decFormat = new DecimalFormat("##.##");
			mMoonVisibleText.setText(getString(R.string.astro_moon_visibility) + " " + decFormat.format(AstroCalcModules.GetMoonVisiblePercents(CalcTime)) + "%");
	        //Пишем время восхода Луны.		
			Calendar TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_MOON, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mMoonRiseText.setText(getString(R.string.astro_moon_rise_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mMoonRiseText.setText(getString(R.string.astro_no_moon_rise));
			}
	        //Пишем время зенита Луны.		
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_MOON, AstroCalcModules.EVENT_TRANSIT, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mMoonTransitText.setText(getString(R.string.astro_moon_transit_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mMoonTransitText.setText(getString(R.string.astro_moon_no_transit));
			}
	        //Пишем время захода Луны.
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(CalcTime, AstroCalcModules.OBJECT_MOON, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				mMoonSettingText.setText(getString(R.string.astro_moon_set_time) + " " + SmallDateFormat.format(TmpCal.getTime()));
			}
			else
			{
				mMoonSettingText.setText(getString(R.string.astro_no_moon_set));
			}
	        //Пишем длительность лунного дня.		
			long LengthOfDaylightInMillis = AstroCalcModules.GetLengthOfDaylightInMillis(CalcTime, AstroCalcModules.OBJECT_MOON, mLongitude, mLatitude);
			if (LengthOfDaylightInMillis == Long.MAX_VALUE)
			{
				mMoonLengthOfDayText.setText(getString(R.string.astro_no_moon_lenth));
			}
			else
			{
				decFormat = new DecimalFormat("00");
				LengthOfDaylightInMillis = Math.abs(LengthOfDaylightInMillis);
				int[] LengthOfDaylight = AstroCalcModules.ConvertMillisToHHMMSSsss(LengthOfDaylightInMillis);		
				mMoonLengthOfDayText.setText(getString(R.string.astro_moon_lenth) + " " + decFormat.format(LengthOfDaylight[0]) + "ч " + decFormat.format(LengthOfDaylight[1]) + "м " + decFormat.format(LengthOfDaylight[2]) + "." + decFormat.format(LengthOfDaylight[3]) + "с");
			}		
	        //Пишем высоту Луны над горизонтом.
			decFormat = new DecimalFormat("#.###");
			mMoonHeightText.setText(getString(R.string.astro_moon_height) + " " + decFormat.format(AstroCalcModules.GetObjectHeightOrSouthAzimuth(CalcTime, AstroCalcModules.OBJECT_MOON, mLongitude, mLatitude, true) / AstroCalcModules.RPD) + " град.");
	        //Пишем азимут Луны от юга.
			mMoonAzimuthText.setText(getString(R.string.astro_moon_azimuth) + " " + decFormat.format(AstroCalcModules.GetObjectHeightOrSouthAzimuth(CalcTime, AstroCalcModules.OBJECT_MOON, mLongitude, mLatitude, false) / AstroCalcModules.RPD) + " град.");
			//Пишем высоту Солнца в зените.
			double TransitHeight = AstroCalcModules.GetObjectTransitHeightPreserve(CalcTime, AstroCalcModules.OBJECT_MOON, mLongitude, mLatitude);
			if (TransitHeight != Double.MAX_VALUE)
			{
				mMoonTransitHeightText.setText(getString(R.string.astro_moon_max_height) + " " + decFormat.format(TransitHeight / AstroCalcModules.RPD) + " град.");
			}
			else
			{
				mMoonTransitHeightText.setText(getString(R.string.astro_no_moon_max_height));
			}
	        //Пишем расстояние до Луны.
			mMoonDistanceText.setText(getString(R.string.astro_moon_distance) + " " + decFormat.format(AstroCalcModules.GetMoonDistance(CalcTime)) + " км.");
			//Пишем текущую фазу луны.
	    	int moonPhaseNumber = AstroCalcModules.GetMoonPhasePreserve(mDateTime);
	    	String moonPhaseName = getResources().getStringArray(R.array.moon_phases)[moonPhaseNumber - 1];
	    	mMoonPhaseText.setText(getString(R.string.astro_moon_phase) + " " + moonPhaseName);
			//Пишем основные фазы Луны.
			Calendar[] PhasesTime;
			//Новолуния:
			PhasesTime = AstroCalcModules.GetMoonMoonMainPhases(CalcTime, AstroCalcModules.NEW_MOON);
			if (PhasesTime == null)
			{
				mMoonNewMoonText.setText(getString(R.string.astro_moon_no_such_phase));
			}
			else
			{
				mMoonNewMoonText.setText("");
				for (int i = 0; i < PhasesTime.length; i++)
				{
					if (i > 0) mMoonNewMoonText.append("\n");
					mMoonNewMoonText.append(TotalDateFormat.format(PhasesTime[i].getTime()));			
				}
			}
			//Первая четверть:
			PhasesTime = AstroCalcModules.GetMoonMoonMainPhases(CalcTime, AstroCalcModules.FIRST_QUARTER);
			if (PhasesTime == null)
			{
				mMoonFirstQuarterText.setText(getString(R.string.astro_moon_no_such_phase));
			}
			else
			{
				mMoonFirstQuarterText.setText("");
				for (int i = 0; i < PhasesTime.length; i++)
				{
					if (i > 0) mMoonFirstQuarterText.append("\n");
					mMoonFirstQuarterText.append(TotalDateFormat.format(PhasesTime[i].getTime()));			
				}
			}
			//Полнолуние:
			PhasesTime = AstroCalcModules.GetMoonMoonMainPhases(CalcTime, AstroCalcModules.FULL_MOON);
			if (PhasesTime == null)
			{
				mMoonFullMoonText.setText(getString(R.string.astro_moon_no_such_phase));
			}
			else
			{
				mMoonFullMoonText.setText("");
				for (int i = 0; i < PhasesTime.length; i++)
				{
					if (i > 0) mMoonFullMoonText.append("\n");
					mMoonFullMoonText.append(TotalDateFormat.format(PhasesTime[i].getTime()));			
				}
			}
			//Последняя четверть:
			PhasesTime = AstroCalcModules.GetMoonMoonMainPhases(CalcTime, AstroCalcModules.LAST_QUARTER);
			if (PhasesTime == null)
			{
				mMoonLastQuarterText.setText(getString(R.string.astro_moon_no_such_phase));
			}
			else
			{
				mMoonLastQuarterText.setText("");
				for (int i = 0; i < PhasesTime.length; i++)
				{
					if (i > 0) mMoonLastQuarterText.append("\n");
					mMoonLastQuarterText.append(TotalDateFormat.format(PhasesTime[i].getTime()));			
				}
			}
        }
    }
}
