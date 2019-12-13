package ru.mendeo.chronos;

import android.content.Intent;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class SystemReceiver extends BroadcastReceiver
{
	private static final String REBOOT_TIME_PREFERENCES = "com.Chronos.reboottimepreferences";
	private static final long INVALID_REBOOT_TIME = -1;
	@Override
	public void onReceive(Context context, Intent intent)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		if (intent.getAction().equals(Intent.ACTION_SHUTDOWN))
		{			
			SharedPreferences.Editor e = sp.edit();
			e.putLong(REBOOT_TIME_PREFERENCES, System.currentTimeMillis());
			e.commit();
		}
		else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
		{
			long shutdownTime = sp.getLong(REBOOT_TIME_PREFERENCES, INVALID_REBOOT_TIME);
			if (shutdownTime == INVALID_REBOOT_TIME) shutdownTime = System.currentTimeMillis();
			RemindersAlarmManager reminderMgr = new RemindersAlarmManager(context);
			RemindersDbAdapter dbHelper = new RemindersDbAdapter(context);
			dbHelper.open();
			Cursor cursor = dbHelper.fetchAllReminders();
			if(cursor != null)
			{
				cursor.moveToFirst();
				while(cursor.isAfterLast() == false)
				{
					Long rowId = cursor.getLong(cursor.getColumnIndex(RemindersDbAdapter.KEY_ROWID));
		    		int year = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_YEAR));
		    		int month = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MONTH));
		    		int day = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DAY));
		    		int hour = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_HOUR));
		    		int minute = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MINUTE));
		    		int second = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_SECOND));
		    		int millisecond = cursor.getInt(cursor.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MILLISECOND));
					Calendar cal = Calendar.getInstance();
		    		setDateToCalendar(cal, year, month, day, hour, minute, second, millisecond);
		    		//Устанавливаем только те напоминания, которые должны произойти в будущем или те, что не смогли произойти пока телефон был выключен, но не трогаем те, что уже произошли раньше.
		    		if (cal.getTimeInMillis() >= shutdownTime) reminderMgr.setAlarm(rowId, cal);
					cursor.moveToNext();
				}
				cursor.close();
			}
			dbHelper.close();
		}
	}
    private void setDateToCalendar(Calendar cal, int year, int month, int day, int hour, int minute, int second, int millisecond)
    {
    	cal.set(Calendar.YEAR, year);
    	cal.set(Calendar.MONTH, month);
    	cal.set(Calendar.DAY_OF_MONTH, day);
    	cal.set(Calendar.HOUR_OF_DAY, hour);
    	cal.set(Calendar.MINUTE, minute);
    	cal.set(Calendar.SECOND, second);
    	cal.set(Calendar.MILLISECOND, millisecond);
    }
}
