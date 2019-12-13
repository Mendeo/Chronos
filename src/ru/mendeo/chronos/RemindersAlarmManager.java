package ru.mendeo.chronos;

import android.content.Context;
import android.app.AlarmManager;
import android.content.Intent;
import android.net.Uri;
import android.app.PendingIntent;
import java.util.Calendar;

public class RemindersAlarmManager
{
	private Context mContext;
	private AlarmManager mAlarmManager;
	
	public RemindersAlarmManager(Context context)
	{
		mContext = context;
		mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}
	public void setAlarm(long taskId, Calendar when)
	{
		mAlarmManager.set(AlarmManager.RTC_WAKEUP, when.getTimeInMillis(), getAlarmPendingIntent(taskId));
	}
	public void cancelAlarm(long taskId)
	{
		mAlarmManager.cancel(getAlarmPendingIntent(taskId));
	}
	private PendingIntent getAlarmPendingIntent(long taskId)
	{
		Intent i = new Intent(mContext, RemindersAlarmReceiver.class);
		i.putExtra(RemindersDbAdapter.KEY_ROWID, taskId);
		i.setData(Uri.parse(i.toUri(Intent.URI_INTENT_SCHEME)));
		return PendingIntent.getBroadcast(mContext, 0, i, PendingIntent.FLAG_ONE_SHOT);
	}
}
