package ru.mendeo.chronos;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

public class WidgetWorkService extends IntentService
{
	//����������, ������������ ���� �� ������� �����������, ������� ������ ���������. ��� ���������� ����������� �� ������ ������� ��� ����������, ��������, � � ������ ��������� �����������. � ��� �� ������ ������� ��� ������ �������
	public static boolean mWillToDayRemind = false;
	public static double mLongitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	public static double mLatitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	public static boolean mIsLocationSearching = false;
	//����������, ������������ ����� �� � ������� ��������� ��� ������ �������� ������ ��������.
	//��� ���������, ���� ��������� �������� �����������. � ���� ������ AlarmManager ���������� ��� ������� � ����� �� ������� ���������� �����������.
	public static boolean mNeedUpdateAfterFirstStart = true;
	
	private static int mNumberOfWidgetUpdates = 0;
	private static int[] mHolidaysType;
	//���������� ��� ��������� ������ �� �� (�������� ������ ���� ��� � ����)
	private static int[] mDbCurrentDate = {-1, -1};
	
	private ExternalDbAdapter mDbAdapter;
	private Calendar mCalendar;	
	
	public WidgetWorkService()
	{
		super("WidgetWorkService");
	}
	@Override
	protected void onHandleIntent(Intent intent)
	{
		mNumberOfWidgetUpdates++;
		int wId = intent.getIntExtra(PublicConstantsAndMethods.WIDGET_ID_INTENT_EXTRA, -1);
		String action = intent.getAction();
		AppWidgetManager mgr = AppWidgetManager.getInstance(this);
		ComponentName wCn = new ComponentName(getApplicationContext(), getWidgetClass(wId));
		//��� ���������� ����� ��� ����, ����� ��������� ������ ����� ��������� ��������� ���������. ��� ������ ��������� ������������, ��� ��������� �������� ���� ����������, � ���� ��� ����� true, �� ��������� ���������� �������. �������� ���� ���������� ������������ ����. ������� ���� ���� ������ ������ ���������� ������, �� �� �������� ���� ����������� ��� �� �����.
		if (mNeedUpdateAfterFirstStart) mNeedUpdateAfterFirstStart = false;
		//��������� ���� �� �� ��� ������ ���������� (��� ����� ������, ���� ��������� ����������), ���� ���, �� ������ ������� ������ ���������� � ���������� ��� �� ���� �������� � ���������� ���� ���������� ����� ��������� � ���������.
		if (!PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
		{
			//������� ������� ��������� ������ �� ����� ��������� � ������ ������.
			//���� ��, �� ������� �� ������ ����������, ��� ���������� �� ��������
			if (mIsLocationSearching)
			{
				mgr.updateAppWidget(wCn, buildUpdate(wId, action, true));
				return;
			}
			//���� ����� �� ������, �� ���������� �������� ���������� �� ��������.
			else
			{
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				mLongitude = (double)sp.getFloat(getString(R.string.preferences_longitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
				mLatitude = (double)sp.getFloat(getString(R.string.preferences_latitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
				//���� � � ���������� ��� ���������, ������ ��� ���������� ������ :).
				if (!PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
				{
					mgr.updateAppWidget(wCn, buildUpdate(wId, action, true));
					return;
				}
			}
		}
		mCalendar = Calendar.getInstance();
		//�������� ������ ������ �� ���� ������ � �������� ���, �� ������ � ��� ������, ���� ��� �� ���� ����� �������� ��� ����� ��� (����� ��������� ��� �� �� ��������� �� ����� � ��� ��).
		if (mCalendar.get(Calendar.YEAR) != mDbCurrentDate[0] || mCalendar.get(Calendar.DAY_OF_YEAR) != mDbCurrentDate[1])
		{
			mDbCurrentDate[0] = mCalendar.get(Calendar.YEAR);
			mDbCurrentDate[1] = mCalendar.get(Calendar.DAY_OF_YEAR);
			mDbAdapter = new ExternalDbAdapter(this);
			mDbAdapter.open();
			mHolidaysType = FillHollidaysData(mDbAdapter.getHolidaysForCurrentDay(mCalendar));
			mDbAdapter.close();
			//���������� ���� �� ������� ����������� �������
			RemindersDbAdapter reminders = new RemindersDbAdapter(getApplicationContext());
			reminders.open();
			mWillToDayRemind = reminders.WillToDayReminders(mCalendar);
			reminders.close();
		}
		mgr.updateAppWidget(wCn, buildUpdate(wId, action, false));
	}
	private RemoteViews buildUpdate(int widgetId, String action, boolean setNoLocation)
	{
		Context context = getApplicationContext();		
		RemoteViews updateViews = new RemoteViews(context.getPackageName(), getWidgetLayout(widgetId));
		if (setNoLocation)
		{
			if (widgetId == PublicConstantsAndMethods.WIDGET_2x3_ID)
			{
				//������� ��������� �� ������ � ���, ��� ����� �������� ���������� � ������������� ����������� � ������� ������ �� ���������.
				updateViews.setTextViewText(R.id.WidgetMainText, getString(R.string.widget_no_location_message));
				updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_0);
				updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_0);
				updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_default_sun));
				updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_1);	
				updateViews.setTextViewText(R.id.MoonText, getString(R.string.widget_default_moon));
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_0);
			}
			else if (widgetId == PublicConstantsAndMethods.WIDGET_4x1_ID)
			{
				//������� ��������� �� ������ � ���, ��� ����� �������� ���������� � ������������� ����������� � ������� ������ �� ���������.
				updateViews.setInt(R.id.WidgetMainText, "setVisibility", View.VISIBLE);
				updateViews.setTextViewText(R.id.WidgetMainText, getString(R.string.widget_no_location_message));
				updateViews.setInt(R.id.SunText, "setVisibility", View.GONE);
				updateViews.setInt(R.id.MoonText, "setVisibility", View.GONE);
				updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_0);
				updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_0);
				updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_default_sun));
				updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_1);	
				updateViews.setTextViewText(R.id.MoonText, getString(R.string.widget_default_moon));
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_0);
			}
		}
		else
		{
			//������ �������� ������ � ��������� �������, ���� � ��� ����� ������:
			if (widgetId == PublicConstantsAndMethods.WIDGET_4x1_ID)
			{
				updateViews.setInt(R.id.WidgetMainText, "setVisibility", View.GONE);
				updateViews.setInt(R.id.SunText, "setVisibility", View.VISIBLE);
				updateViews.setInt(R.id.MoonText, "setVisibility", View.VISIBLE);
			}
			//����������� ������.
			boolean IsNight;
			//�������� ������� ������ ������ ��� ����������
			double SunHeight = AstroCalcModules.GetObjectHeightOrSouthAzimuth(mCalendar, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude, true);
			if (SunHeight < AstroCalcModules.GetSunHorisontHeightPreserve())
			{
				updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_9);
				IsNight = true;
			}
			else
			{
				//�������� ��������� � �������� ������. �� ���������� ������ � ��� ������, ���� ������� �� ��� �� ���������. ���������� ������ ���������� � ������ CalcModules.
				Calendar TransitTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_TRANSIT, mLongitude, mLatitude);
				//�������� ������ ������ (�� � ��� ������ ��� ���������� ������ � ��� ������, ���� �� � ������� ��� �� ��������).
				double SunTransitHeight = AstroCalcModules.GetObjectTransitHeightPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude);
				//�� � ���� ������ ������ � ������ � ������� ������ ��������� ����� �������� ��� ������� �� ������. (����� 9 ��������)
				double Angle = SunTransitHeight / 4.5;
				int PicNum = (int)Math.floor(Math.abs(SunHeight / Angle));
				if (mCalendar.getTimeInMillis() > TransitTime.getTimeInMillis()) PicNum = 8 - PicNum;
				switch (PicNum)
				{
					case 0:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_0);
					break;
					case 1:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_1);
					break;
					case 2:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_2);
					break;
					case 3:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_3);
					break;
					case 4:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_4);
					break;
					case 5:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_5);
					break;
					case 6:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_6);
					break;
					case 7:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_7);
					break;
					case 8:
						updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_8);
					break;
				}
				IsNight = false;
			}
			//����� ����� ������� ��� ������.
			SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
			Calendar SettingTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (IsNight)
			{
				//���� ������ ����, �� ����� ������������ �������� �� ��� ��������� ����.
				if (SettingTime != null)
				{
					Calendar RiseTime;
					//���� ��������� ���� �� �������� (����� ��� ������ �������� �������), �� ������� ������ ��� ���������� ���.
					if (SettingTime.getTimeInMillis() < mCalendar.getTimeInMillis())
					{
						Calendar NextDay = (Calendar)mCalendar.clone();
						NextDay.setTimeInMillis(NextDay.getTimeInMillis() + 86400000);
						RiseTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(NextDay, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
					}
					//� ��������� ������ - ��� �������� ���.
					else
					{
						RiseTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
					}
					if (RiseTime != null)
					{
						String TimeStr = TimeFormat.format(RiseTime.getTime());
						updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_rise) + " " + TimeStr);
					}
					//���� �� ����� ���������� ������, �� �����, ��� � ��� �������� ����.
					else
					{
						updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_night));
					}
				}
				//��� �� ���� �� ����� ���������� �����, �� �����, ��� � ��� �������� ����.
				else
				{
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_night));
				}
			}
			//���� ������ ����, �� ������ ������� ����� ��� �������� ���.
			else
			{				
				if (SettingTime != null)
				{
					String TimeStr = TimeFormat.format(SettingTime.getTime());
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_set) + " " + TimeStr);
				}
				//� ����������, ���� �� ����� ���������� �����, �� �����, ��� � ��� �������� ����.
				else
				{
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_day));
				}
			}
			//����������� ��� ����.
			int MoonPhase = AstroCalcModules.GetMoonPhasePreserve(mCalendar);
			switch (MoonPhase)
			{
				case 1:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_1);
				break;
				case 2:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_2);
				break;
				case 3:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_3);
				break;
				case 4:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_4);
				break;
				case 5:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_5);
				break;
				case 6:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_6);
				break;
				case 7:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_7);
				break;
				case 8:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_8);
				break;
				default:
					updateViews.setImageViewResource(R.id.MoonButton, R.drawable.moon_button_state_1);
					Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in moon button image.");
					throw new Error("Error in determing moon phase.");
			}
			//����� ������� ��������� ����.
			DecimalFormat decform = new DecimalFormat("##.#");
			String MoonPercent = decform.format(AstroCalcModules.GetMoonVisiblePercents(mCalendar));
			updateViews.setTextViewText(R.id.MoonText, getString(R.string.widget_text_moon_visibility) + " " + MoonPercent + "%");
			//********************
			//��������� ������ "���������" � ����������� �� ���� ���
			if (mHolidaysType != null)
			{
				switch (mHolidaysType[0])
				{
					//��� ������ 1. ��������� ����������� ���:
					case 1:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 1:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1);
								break;
								//��� ������ 1_2. ���������� �������� (1) � ���������� (2) ����������� ����:
								case 2:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_2);
								break;
								//��� ������ 1_3. ���������� ���������� ������������ ��� (1) � ����������������� ��������� (3):
								case 3:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_3);
								break;
								//��� ������ 1_456. ���������� ���������� ������������ ��� � ��� �������� ����� ��� �������� ���� ������ ��� ��� ������:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_456);
								break;
								//��� ������ 1_7. ���������� ���������� ������������ ��� � ������� �������������� ���������:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_7);
								break;
								//��� ������ 1_8 ���������� ���������� ������������ ��� � ������������� ���������:
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1);
								break;
							}
						}
					break;
					//��� ������ 2. ������� ����������� ���:
					case 2:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 2:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2);
								break;
								//��� ������ 2_3. ���������� �������� ������������ ��� (1) � ����������������� ��������� (3):
								case 3:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_3);
								break;
								//��� ������ 2_456. ���������� �������� ������������ ��� � ��� �������� ����� ��� �������� ���� ������ ��� ��� ������:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_456);
								break;
								//��� ������ 2_7. ���������� �������� ������������ ��� � ������� �������������� ���������:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_7);
								break;
								//��� ������ 2_8. ���������� �������� ������������ ��� � ������������� ���������:
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2);
								break;
							}
						}
					break;
					//��� ������ 3. ���������������� ���������:
					case 3:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 3:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3);
								break;
								//��� ������ 3_456. ���������� ����������������� ��������� � ��� �������� ����� ��� �������� ���� ������ ��� ��� ������:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3_456);
								break;
								//��� ������ 3_7. ���������� ����������������� ��������� � ������� �������������� ���������:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3_7);
								break;
								//��� ������ 3_8. ���������� ����������������� ��������� � ������� ������������� ���������:
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3);
								break;
							}
						}
					break;
					//��� ������ 456. ��� �������� ����� ��� �������� ���� ������ ��� ��� ������:
					case 4: case 5: case 6:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456);
								break;
								//��� ������ 456_7. ���������� ��� �������� ����� ��� �������� ���� ������ ��� ��� ������ � ������� �������������� ���������:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456_7);
								break;
								//��� ������ 456_8. ���������� ��� �������� ����� ��� �������� ���� ������ ��� ��� ������ � ������������� ���������:
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456);
								break;
							}
						}
					break;
					//��� ������ 7. ������ ������������� ���������:
					case 7:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_7);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_7);
								break;
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_7_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_7);
								break;
							}
						}
					break;
					case 8:
						if (mHolidaysType.length == 1)
						{
							updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_8);
						}
						else
						{
							switch (mHolidaysType[1])
							{
								case 8:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_8);
								break;
								default:
									Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Warning error in holidays types array.");
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_8);
								break;
							}
						}
					break;
					//��� ������ 0, �� � �� ���������� ������� ����� ������� 8. 
					default:
						updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_0);
					break;
				}
			}
			else
			{
				updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_0);
				Log.i(PublicConstantsAndMethods.MY_LOG_TAG, "No such day in DB.");
			}
			//******************************************************
			//����������� �������� ��������� �� ������ �����������
			if (!mWillToDayRemind)
			{
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_0);
			}
			else
			{
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_1);
			}
			//��������� ���������� ������� ������ ������
			
			Intent i = new Intent(context, getWidgetClass(widgetId));
			PendingIntent pi;
			//������ ���������� ���������� �� action. ������� �������� ��������, ����� ����� alarm, � ����� ���� �������� ����� ����������, � alarm ����� ������� �� ���� �����.
			//������ � ����������� ��������: "If there is already an alarm scheduled for the same IntentSender, that previous alarm will first be canceled."
			//��� ����� ��� ���������� alarm ����������.
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			i.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_ALARM_UPDATE);
			pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
			mCalendar.set(Calendar.SECOND, 0);
			mCalendar.set(Calendar.MILLISECOND, 0);
			long when = mCalendar.getTimeInMillis() + 60000;
			alarmManager.set(AlarmManager.RTC, when, pi);
			//*******************************************
			//����� ����� �� ������� �������.
			if (widgetId == PublicConstantsAndMethods.WIDGET_2x3_ID)
			{
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
				String time = simpleDateFormat.format(mCalendar.getTime());
				simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy",Locale.getDefault());
				String date = simpleDateFormat.format(mCalendar.getTime());
				String dayName = PublicConstantsAndMethods.getDayOfWeek(mCalendar, getResources().getStringArray(R.array.days_of_week));
				String out = time + "   " + date + "\n" + dayName;
				updateViews.setTextViewText(R.id.WidgetMainText, out);
			}
		}
		//*******��������� ���������� �� ������ (���������� �� ���� ����� �� ���������� ��� ���)*******
		//��������� �� ������ "���������".
		/* ���������� ���� ���, ���� ����� ������� ���� ����� ����� ������� ������
		i.putExtra(WIDGET_BUTTON_ID, HOLLIDAYS_BUTTON_ID);
		i.setData(Uri.parse(i.toUri(Intent.URI_INTENT_SCHEME)));
		pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.HolidaysButton, pi);
		*/
		PendingIntent pi;
		Intent iact; //���������� ���� ������, ���� ��� ������� ������ �� ������� ����� ����� ������� ��������.
		iact = new Intent(context, ChronosMainActivity.class);
		iact.putExtra(ChronosMainActivity.SELECT_SCREEN_NUMBER, ChronosMainActivity.HOLIDAYS_SCREEN_NUMBER);
		iact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //����� ������� ��� ����, ������� ���� ������� � �������� (�������� ��������� ������� ���, ����� ������������ �� ������ �������� � ���� �����������).
		iact.setData(Uri.parse(iact.toUri(Intent.URI_INTENT_SCHEME)));
		iact.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_BUTTON_TAP);
		pi = PendingIntent.getActivity(context, 0, iact, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.HolidaysButton, pi);
		iact = new Intent(context, ChronosMainActivity.class);
		iact.putExtra(ChronosMainActivity.SELECT_SCREEN_NUMBER, ChronosMainActivity.SUN_SCREEN_NUMBER);
		iact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		iact.setData(Uri.parse(iact.toUri(Intent.URI_INTENT_SCHEME)));
		iact.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_BUTTON_TAP);
		pi = PendingIntent.getActivity(context, 0, iact, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.SunButton, pi);
		iact = new Intent(context, ChronosMainActivity.class);
		iact.putExtra(ChronosMainActivity.SELECT_SCREEN_NUMBER, ChronosMainActivity.MOON_SCREEN_NUMBER);
		iact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		iact.setData(Uri.parse(iact.toUri(Intent.URI_INTENT_SCHEME)));
		iact.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_BUTTON_TAP);
		pi = PendingIntent.getActivity(context, 0, iact, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.MoonButton, pi);
		iact = new Intent(context, ChronosMainActivity.class);
		iact.putExtra(ChronosMainActivity.SELECT_SCREEN_NUMBER, ChronosMainActivity.REMINDERS_SCREEN_NUMBER);
		iact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		iact.setData(Uri.parse(iact.toUri(Intent.URI_INTENT_SCHEME)));
		iact.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_BUTTON_TAP);
		pi = PendingIntent.getActivity(context, 0, iact, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.NotesButton, pi);
		//******************************************		
		//*******���������������� ������� �� ������� ������ (���� ���� �������, ��� ��� ������� ����������� ���� �����, � �� ������ ��������)*******
		String UpdatesTxt = "Widget updates: " + Long.toString(mNumberOfWidgetUpdates);
		Log.i(PublicConstantsAndMethods.MY_LOG_TAG, UpdatesTxt);
		//���� �� �����, �.�. ��� ����� ����� ���������� � ������ �������, � �� �� ��������� � ��������� �����.
		//if (action.equals(BUTTON_ACTION))
		//{
		//	switch (intent.getIntExtra(WIDGET_BUTTON_ID, NO_WIDGET_BUTTON_ID)) 
		//	{
		//		case HOLLIDAYS_BUTTON_ID:
					//updateViews.setTextViewText(R.id.WidgetMainText, UpdatesTxt + " Hollidays");
		//		break;
		//		case SUN_BUTTON_ID:
					//updateViews.setTextViewText(R.id.WidgetMainText, UpdatesTxt + " Sun");
		//		break;
		//		case MOON_BUTTON_ID:
					/* �������� ��������, ���� ��� �� ���� ������� ����� �� ��������. (�.�. ���� �� ����� ���-�� ���������� �� ������� ����� ��� ��� ������� ��������)
					updateViews.setTextViewText(R.id.WidgetMainText, UpdatesTxt + " Moon: ");
					i = new Intent(context, MoonActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(i);
					*/
		//		break;
		//		case NOTES_BUTTON_ID:
					//updateViews.setTextViewText(R.id.WidgetMainText, UpdatesTxt + " Notes");
		//		break;
		//		case HISTORY_BUTTON_ID:
					//updateViews.setTextViewText(R.id.WidgetMainText, UpdatesTxt + " History");
		//		break;
		//		default:																
		//		break;
		//	}
		//} 
		//********************************************************
		return updateViews; 
	}
	private int[] FillHollidaysData(Cursor cur)
	{
		if (cur == null)
		{
			Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Cursor is null");
			return null;
		}
		int size = cur.getCount(); 
		if (size > 0)
		{
			int[] HolidaysType = new int[size];
			cur.moveToFirst();
			for (int i = 0; i < size; i++)
			{
				HolidaysType[i] = cur.getInt(cur.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_TYPE));
				cur.moveToNext();
			}
			PublicConstantsAndMethods.SortArray(HolidaysType);
			cur.close(); // - �������� ��������! ������ �� ���������!
			return HolidaysType;
		}
		else
		{
			cur.close(); // - �������� ��������! ������ �� ���������!
			return null;
		}
	}
	//�������� Layout ��� �������, ���������� ���� ������.
	public int getWidgetLayout(int widgetId)
	{
		switch (widgetId)
		{
		case PublicConstantsAndMethods.WIDGET_2x3_ID:
			return R.layout.widget_2x3;
		case PublicConstantsAndMethods.WIDGET_4x1_ID:
			return R.layout.widget_4x1;			
		}
		return -1;
	}
	//�������� ����� �������, ���������� ���� ������.
	public Class<?> getWidgetClass(int widgetId)
	{
		switch (widgetId)
		{
		case PublicConstantsAndMethods.WIDGET_2x3_ID:
			return AppWidget_2x2.class;
		case PublicConstantsAndMethods.WIDGET_4x1_ID:
			return AppWidget_4x1.class;			
		}
		return null;
	}
	//������� ��������� � Toast � ������� ������ ����������.
	/*
	private void showErrorMesage()
	{
		mHandler.post(new Runnable()
		{
			public void run()
			{
				//Place some action here:
				String message = "Sample of error message.";
				Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
			}
		});
	}
	*/
}