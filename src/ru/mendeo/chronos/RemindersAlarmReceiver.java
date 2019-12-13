package ru.mendeo.chronos;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.Context;

public class RemindersAlarmReceiver extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intent)
	{
		WakeReminderIntentService.acquireStaticLock(context);
		long rowid = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);
		//Log.d(PublicConstants.MY_LOG_TAG, Long.toString(rowid));
		Intent i = new Intent(context, RemindersNotificationService.class);
		i.putExtra(RemindersDbAdapter.KEY_ROWID, rowid);
		context.startService(i);
	}
}

