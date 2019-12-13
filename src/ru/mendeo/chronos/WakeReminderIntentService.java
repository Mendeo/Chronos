package ru.mendeo.chronos;

import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.app.IntentService;

public abstract class WakeReminderIntentService extends IntentService
{
	abstract public void doReminderWork(Intent intent);
	public static final String LOCK_NAME_STATIC = "com.Chronos.WakeReminderIntentService";
	private static PowerManager.WakeLock lockStatic = null;
	
	public WakeReminderIntentService(String name)
	{
		super(name);
	}
	public static void acquireStaticLock(Context context)
	{
		getLock(context).acquire();
	}
	private synchronized static PowerManager.WakeLock getLock(Context context)
	{
		if (lockStatic == null)
		{
			PowerManager mgr = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME_STATIC);
			lockStatic.setReferenceCounted(true);
		}
		return lockStatic;
	}
	@Override
	final protected void onHandleIntent(Intent intent)
	{
		try
		{
			doReminderWork(intent);
		}
		finally
		{
			getLock(this).release();
		}
	}
}
