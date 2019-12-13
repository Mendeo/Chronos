package ru.mendeo.chronos;

import java.util.Calendar;

import android.content.Intent;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.app.*;

//@SuppressWarnings("deprecation")
public class RemindersNotificationService extends WakeReminderIntentService
{
	public RemindersNotificationService()
	{
		super("RemindersNotificationService");
	}
	@Override
	public void doReminderWork(Intent intent)
	{
		// Status bar notification Code Goes here.
		long rowId = intent.getExtras().getLong(RemindersDbAdapter.KEY_ROWID);
		NotificationManager mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(this, ReminderEditActivity.class);
		notificationIntent.putExtra(RemindersDbAdapter.KEY_ROWID, rowId);
		notificationIntent.setData(Uri.parse(notificationIntent.toUri(Intent.URI_INTENT_SCHEME)));
		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(ReminderEditActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(notificationIntent);
		PendingIntent pi = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//		PendingIntent pi = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
		//Получаем данные из базы данных.
		RemindersDbAdapter DbAdapter = new RemindersDbAdapter(getApplicationContext());
		DbAdapter.open();
		Cursor task = DbAdapter.fetchReminder(rowId);
		task.moveToFirst();
		String title = task.getString(task.getColumnIndex(RemindersDbAdapter.KEY_TITLE));
		String body = task.getString(task.getColumnIndex(RemindersDbAdapter.KEY_BODY));
		task.close();
    	//Узнаём есть ли сегодня ещё напоминания, которые должны произойти
    	boolean willToDayRemind = DbAdapter.WillToDayReminders(Calendar.getInstance());
		DbAdapter.close();
		//И если напоминания остаются, то виджет не обновляем, в противном случае обновляем виджет
		if (!willToDayRemind)
		{
			//Обновим сразу все виджеты (широкий и узкий).
			//Указываем виджету, что напоминаний сегодня нет.
			WidgetWorkService.mWillToDayRemind = false;
	    	Intent widgetIntent = null;
			AppWidgetBase appWidget = null;
	    	for (int i = 0; i < 2; i++)
	    	{
	    		switch (i)
	    		{
	    		case 0:
	    			widgetIntent = new Intent(this, AppWidget_2x2.class);
	    			appWidget = new AppWidget_2x2();
	    			break;
	    		case 1:
	    			widgetIntent = new Intent(this, AppWidget_4x1.class);
	    			appWidget = new AppWidget_4x1();
	    			break;
	    		}
	    		widgetIntent.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_INTERNAL_UPDATE);
	    		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
	    		broadcastManager.registerReceiver(appWidget, new android.content.IntentFilter(PublicConstantsAndMethods.ACTION_APPWIDGET_INTERNAL_UPDATE));
	    		broadcastManager.sendBroadcast(widgetIntent);
	    		broadcastManager.unregisterReceiver(appWidget);
	    	}
		}
		//Данные получены.
		NotificationCompat.Builder noteBuilder = new NotificationCompat.Builder(this);
		noteBuilder.setContentIntent(pi);
		noteBuilder.setSmallIcon(R.drawable.notification_icon);
		noteBuilder.setContentTitle(title);
		noteBuilder.setContentText(body);
		int id = (int)((long)rowId);
		mgr.notify(id, noteBuilder.build());
		
		/* Устаревший код */
		/*
		Notification note = new Notification(R.drawable.notification_icon, title , System.currentTimeMillis());
		note.setLatestEventInfo(this, title, body, pi);
		note.defaults |= Notification.DEFAULT_SOUND;
		note.defaults |= Notification.DEFAULT_VIBRATE;
		note.flags |= Notification.FLAG_AUTO_CANCEL;
		*/
	}
}
