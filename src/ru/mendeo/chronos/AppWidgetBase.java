package ru.mendeo.chronos;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;

public abstract class AppWidgetBase extends AppWidgetProvider
{
	abstract public int getWidgetId();
	public AppWidgetBase()
	{
		super();
	}
	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
		String action = intent.getAction();
		if (action.equals(PublicConstantsAndMethods.ACTION_APPWIDGET_INTERNAL_UPDATE) || action.equals(PublicConstantsAndMethods.ACTION_APPWIDGET_ALARM_UPDATE))
		{
			startUpdate(context, action);
		}
	}	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		startUpdate(context, AppWidgetManager.ACTION_APPWIDGET_UPDATE);
	}
	private void startUpdate(Context context, String action)
	{
		Intent i = new Intent(context, WidgetWorkService.class);
		i.setAction(action);
		int widgetId = getWidgetId();
		i.putExtra(PublicConstantsAndMethods.WIDGET_ID_INTENT_EXTRA, widgetId);
		context.startService(i);
	}
}