package ru.mendeo.chronos;

import java.util.Calendar;
import android.os.Bundle;

public class PublicConstantsAndMethods
{
	public static final String MY_LOG_TAG = "MyLog";
	public static final String WIDGET_ID_INTENT_EXTRA = "ru.Chronos.widget_id_intent_extra";
	public static final int WIDGET_2x3_ID = 1001;
	public static final int WIDGET_4x1_ID = 1002;
	public static final String ACTION_APPWIDGET_INTERNAL_UPDATE = "ru.Chronos.appwidget_internal_update";
	public static final String ACTION_APPWIDGET_ALARM_UPDATE = "ru.Chronos.appwidget_alarm_update";
	public static final String ACTION_APPWIDGET_BUTTON_TAP = "ru.Chronos.appwidget_button_tap";
	public static final String ACTION_AFTER_SEARCH = "ru.Chronos.action_ater_search";
	public static final String LOCATION_TIME_KEY = "ru.Chronos.locationtime";
	public static final String CURRENT_SCREEN_ID_KEY = "ru.Chronos.currentscreenidkey";
	public static final String DATE_AND_TIME_CALENDAR_KEY = "ru.Chronos.dateandtimecalendarkey";
	public static final String LAST_SEARCH_TIME_KEY = "ru.mendeo.chronos.lastsearchtime";
	public static final long INVALID_LOCATION_TIME = -1;
	public static final float INVALID_LOCATION_ACCURACY = -1;
	public static final double INVALID_COORDINATES = -1000000;
	public static final String LOCATION_ACCURACY_KEY = "ru.Chronos.locationaccuracy";
	public static final String LOCATION_PROVIDER_KEY = "ru.Chronos.locationprovider";
	public static final String ONLY_TIME_WITHOUT_SECONDS_FORMAT = "HH:mm";	
	public static final String FULL_DATE_WITHOUT_SECONDS_FORMAT = "dd.MM.yyyy HH:mm";
	public static final String ONLY_DATE_FORMAT = "dd.MM";
	public static final int RESULT_GET_NEW_LOCATION = 1032;
	public static final double APPROXIMATELY_ZERO = 1e-6;
	public static boolean mNeedToShowRateOurAppsDialog = false;
	public static boolean mLocationDenied = false;
    
	public static boolean IsLocationValid(double longitude, double latitude)
	{
		return (Math.abs(longitude - INVALID_COORDINATES) > APPROXIMATELY_ZERO) && (Math.abs(latitude - INVALID_COORDINATES) > APPROXIMATELY_ZERO);
	}	
	//янпрхпнбйю люяяхбю (хяонкэгсеряъ лернд ашярпни янпрхпнбйх).
	public static void SortArray(int[] A)
    {
    	qSort(A, 0, A.length - 1);
    }
    //аШЯРПЮЪ ЯНПРХПНБЙЮ.
	private static void qSort(int[] A, int low, int high)
    {
    	int i = low;
        int j = high;
        int x = A[(low + high) / 2];
        do
        {
        	while (A[i] < x) ++i;
            while (A[j] > x) --j;
            if (i <= j)
            {
                int temp = A[i];
                A[i] = A[j];
                A[j] = temp;
                i++;
                j--;
            }
        } while (i <= j);
        if (low < j) qSort(A, low, j);
        if (i < high) qSort(A, i, high);
    }
	public static long getRandomNumber(long min, long max)
	{
		double rnd = Math.random();
		return min + (long)((double)(max - min + 1) * rnd);
	}
	public static String getDayOfWeek(Calendar calendar, String[] dayOfWeekList)
	{
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = (dayOfWeek == 1) ? 6 : dayOfWeek - 2;
		return dayOfWeekList[dayOfWeek];
	}
	public static void setCalendarToBundle(Calendar cal, Bundle extras)
    {    	
    	extras.putInt("YEAR", cal.get(Calendar.YEAR));
    	extras.putInt("DAY_OF_YEAR", cal.get(Calendar.DAY_OF_YEAR));
    	extras.putInt("HOUR_OF_DAY", cal.get(Calendar.HOUR_OF_DAY));
    	extras.putInt("MINUTE", cal.get(Calendar.MINUTE));
    	extras.putInt("SECOND", cal.get(Calendar.SECOND));
    	extras.putInt("MILLISECOND", cal.get(Calendar.MILLISECOND));
    }
    public static Calendar getCalendarFromBundle(Bundle extras)
    {
    	Calendar cal = Calendar.getInstance();
    	int tmp;
    	tmp = extras.getInt("YEAR", -1);
    	if (tmp >= 0) cal.set(Calendar.YEAR, tmp);
    	tmp = extras.getInt("DAY_OF_YEAR", -1);
    	if (tmp >= 0) cal.set(Calendar.DAY_OF_YEAR, tmp);
    	tmp = extras.getInt("HOUR_OF_DAY", -1);
    	if (tmp >= 0) cal.set(Calendar.HOUR_OF_DAY, tmp);
    	tmp = extras.getInt("MINUTE", -1);
    	if (tmp >= 0) cal.set(Calendar.MINUTE, tmp);
    	tmp = extras.getInt("SECOND", -1);
    	if (tmp >= 0) cal.set(Calendar.SECOND, tmp);
    	tmp = extras.getInt("MILLISECOND", -1);
    	if (tmp >= 0) cal.set(Calendar.MILLISECOND, tmp);
    	return cal;
    }
}
