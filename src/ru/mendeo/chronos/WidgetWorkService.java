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
	//Переменная, показывающая есть ли сегодня напоминания, которые должны произойти. Эта переменная обновляется из других классов при добавлении, удалении, и в момент появления напоминаний. А так же внутри виджета при первом запуске
	public static boolean mWillToDayRemind = false;
	public static double mLongitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	public static double mLatitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	public static boolean mIsLocationSearching = false;
	//Переменная, показывающая нужно ли в главной программе при старте обновить виджет полность.
	//Это еобходимо, если программа аварийно завершилась. В этом случае AlarmManager сбрасывает все события и время на виджете перестанет обновляться.
	public static boolean mNeedUpdateAfterFirstStart = true;
	
	private static int mNumberOfWidgetUpdates = 0;
	private static int[] mHolidaysType;
	//Необходимо для получения данных из БД (получать только один раз в день)
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
		//Эта переменная нужна для того, чтобы запустить виджет после аварийной остановки программы. Как только программа запускаяется, она проверяет значение этой переменной, и если она равна true, то запускает обновление виджета. Значение этой переменной сбрасывается ниже. Поэтому даже если виджет успеет обновиться раньше, то из главного окна обновляться уже не будет.
		if (mNeedUpdateAfterFirstStart) mNeedUpdateAfterFirstStart = false;
		//Проверяем были ли из вне заданы координаты (они будут заданы, если запустить приложение), если нет, то значит телефон только запустился и координаты ещё не были записаны в переменные либо происходит поиск координат в программе.
		if (!PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
		{
			//Поэтому сначала проверяем ведётся ли поиск координат в данный момент.
			//Если да, то выводим на виджет информацию, что координаты не известны
			if (mIsLocationSearching)
			{
				mgr.updateAppWidget(wCn, buildUpdate(wId, action, true));
				return;
			}
			//Если поиск не ведётся, то попытаемся получить координаты из настроек.
			else
			{
				SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
				mLongitude = (double)sp.getFloat(getString(R.string.preferences_longitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
				mLatitude = (double)sp.getFloat(getString(R.string.preferences_latitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
				//Если и в настройках нет координат, значит они неизвестны совсем :).
				if (!PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
				{
					mgr.updateAppWidget(wCn, buildUpdate(wId, action, true));
					return;
				}
			}
		}
		mCalendar = Calendar.getInstance();
		//Получаем нужные данные из базы данных о событиях дня, но только в том случае, если они не были ранее получены для этого дня (чтобы несколько раз БД не открывать за одним и тем же).
		if (mCalendar.get(Calendar.YEAR) != mDbCurrentDate[0] || mCalendar.get(Calendar.DAY_OF_YEAR) != mDbCurrentDate[1])
		{
			mDbCurrentDate[0] = mCalendar.get(Calendar.YEAR);
			mDbCurrentDate[1] = mCalendar.get(Calendar.DAY_OF_YEAR);
			mDbAdapter = new ExternalDbAdapter(this);
			mDbAdapter.open();
			mHolidaysType = FillHollidaysData(mDbAdapter.getHolidaysForCurrentDay(mCalendar));
			mDbAdapter.close();
			//Определяем есть ли сегодня непрочтённые заметки
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
				//Выводим сообщение на виджет о том, что нужно получить координаты и устанавливаем изображение и подписи кнопок по умолчанию.
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
				//Выводим сообщение на виджет о том, что нужно получить координаты и устанавливаем изображение и подписи кнопок по умолчанию.
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
			//Делаем видимыми кнопки и невидимой надпись, если у нас узкий виджет:
			if (widgetId == PublicConstantsAndMethods.WIDGET_4x1_ID)
			{
				updateViews.setInt(R.id.WidgetMainText, "setVisibility", View.GONE);
				updateViews.setInt(R.id.SunText, "setVisibility", View.VISIBLE);
				updateViews.setInt(R.id.MoonText, "setVisibility", View.VISIBLE);
			}
			//ОТОБРАЖЕНИЕ СОЛНЦА.
			boolean IsNight;
			//Получаем текущую высоту солнца над горизонтом
			double SunHeight = AstroCalcModules.GetObjectHeightOrSouthAzimuth(mCalendar, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude, true);
			if (SunHeight < AstroCalcModules.GetSunHorisontHeightPreserve())
			{
				updateViews.setImageViewResource(R.id.SunButton, R.drawable.sun_button_state_9);
				IsNight = true;
			}
			else
			{
				//Получаем календарь с временем зенита. Он получается только в том случае, если сегодня он ещё не получался. Сохранение данных происходит в классе CalcModules.
				Calendar TransitTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_TRANSIT, mLongitude, mLatitude);
				//Получаем высоту зенита (ну и как обычно она получается только в том случае, если мы её сегодня ещё не получали).
				double SunTransitHeight = AstroCalcModules.GetObjectTransitHeightPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude);
				//Ну и зная высоту Солнца в зените и текущую высоту вычисляем какую картинку нам ставить на кнопку. (Всего 9 картинок)
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
			//Пишем время восхода или заката.
			SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
			Calendar SettingTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (IsNight)
			{
				//Если сейчас ночь, то нужно определиться наступил ли уже следующий день.
				if (SettingTime != null)
				{
					Calendar RiseTime;
					//Если следующий день не наступил (Заход был раньше текущего времени), то считаем восход для следующего дня.
					if (SettingTime.getTimeInMillis() < mCalendar.getTimeInMillis())
					{
						Calendar NextDay = (Calendar)mCalendar.clone();
						NextDay.setTimeInMillis(NextDay.getTimeInMillis() + 86400000);
						RiseTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(NextDay, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
					}
					//В противном случае - для текущего дня.
					else
					{
						RiseTime = AstroCalcModules.GetRisingTransitOrSettingPreserve(mCalendar, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
					}
					if (RiseTime != null)
					{
						String TimeStr = TimeFormat.format(RiseTime.getTime());
						updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_rise) + " " + TimeStr);
					}
					//Если не можем определить восход, то пишем, что у нас полярная ночь.
					else
					{
						updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_night));
					}
				}
				//Так же если не можем определить заход, то пишем, что у нас полярная ночь.
				else
				{
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_night));
				}
			}
			//Если сейчас день, то просто считаем заход для текущего дня.
			else
			{				
				if (SettingTime != null)
				{
					String TimeStr = TimeFormat.format(SettingTime.getTime());
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_set) + " " + TimeStr);
				}
				//И аналогично, если не можем определить заход, то пишем, что у нас полярный день.
				else
				{
					updateViews.setTextViewText(R.id.SunText, getString(R.string.widget_text_sun_polar_day));
				}
			}
			//ОТОБРАЖЕНИЕ ФАЗ ЛУНЫ.
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
			//Пишем процент видимости Луны.
			DecimalFormat decform = new DecimalFormat("##.#");
			String MoonPercent = decform.format(AstroCalcModules.GetMoonVisiblePercents(mCalendar));
			updateViews.setTextViewText(R.id.MoonText, getString(R.string.widget_text_moon_visibility) + " " + MoonPercent + "%");
			//********************
			//ИЗМЕНЕНИЕ КНОПКИ "ПРАЗДНИКИ" В ЗАВИСИМОСТИ ОТ ТИПА ДНЯ
			if (mHolidaysType != null)
			{
				switch (mHolidaysType[0])
				{
					//Тип кнопки 1. Нерабочие праздничные дни:
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
								//Тип кнопки 1_2. Совпадение Рабочего (1) и нерабочего (2) праздничных дней:
								case 2:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_2);
								break;
								//Тип кнопки 1_3. Совпадение нерабочего праздничного дня (1) и профессионального праздника (3):
								case 3:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_3);
								break;
								//Тип кнопки 1_456. Совпадение нерабочего праздничного дня и дня воинской славы или памятной даты России или дня памяти:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_456);
								break;
								//Тип кнопки 1_7. Совпадение нерабочего праздничного дня и другого неофициального праздника:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_1_7);
								break;
								//Тип кнопки 1_8 Совпадение нерабочего праздничного дня и Православного праздника:
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
					//Тип кнопки 2. Рабочие праздничные дни:
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
								//Тип кнопки 2_3. Совпадение рабочего праздничного дня (1) и профессионального праздника (3):
								case 3:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_3);
								break;
								//Тип кнопки 2_456. Совпадение рабочего праздничного дня и дня воинской славы или памятной даты России или дня памяти:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_456);
								break;
								//Тип кнопки 2_7. Совпадение рабочего праздничного дня и другого неофициального праздника:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_2_7);
								break;
								//Тип кнопки 2_8. Совпадение рабочего праздничного дня и Православного праздника:
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
					//Тип кнопки 3. Профессиональные праздники:
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
								//Тип кнопки 3_456. Совпадение профессионального праздника и дня воинской славы или памятной даты России или дня памяти:
								case 4: case 5: case 6:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3_456);
								break;
								//Тип кнопки 3_7. Совпадение профессионального праздника и другого неофициального праздника:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_3_7);
								break;
								//Тип кнопки 3_8. Совпадение профессионального праздника и другого Православного праздника:
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
					//Тип кнопки 456. Дни воинской славы или памятные даты России или дни памяти:
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
								//Тип кнопки 456_7. Совпадение дня воинской славы или памятной даты России или дня памяти и другого неофициального праздника:
								case 7:
									updateViews.setImageViewResource(R.id.HolidaysButton, R.drawable.holidays_button_state_456_7);
								break;
								//Тип кнопки 456_8. Совпадение дня воинской славы или памятной даты России или дня памяти и Православного праздника:
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
					//Тип кнопки 7. Другие неофициальные праздники:
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
					//Тип кнопки 0, но в БД желательно ставить число большее 8. 
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
			//Отображение красного карандаша на кнопки напоминаний
			if (!mWillToDayRemind)
			{
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_0);
			}
			else
			{
				updateViews.setImageViewResource(R.id.NotesButton, R.drawable.notes_button_state_1);
			}
			//УСТАНОВКА ОБНОВЛЕНИЕ ВИДЖЕТА КАЖДУЮ МИНУТУ
			
			Intent i = new Intent(context, getWidgetClass(widgetId));
			PendingIntent pi;
			//Ставим обновление независимо от action. Поэтому возможна ситуация, когда задан alarm, а потом были получены новые координаты, и alarm снова задаётся на тоже время.
			//Однако в справочнике написано: "If there is already an alarm scheduled for the same IntentSender, that previous alarm will first be canceled."
			//Что вроде как предыдущий alarm отменяется.
			AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			i.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_ALARM_UPDATE);
			pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_ONE_SHOT);
			mCalendar.set(Calendar.SECOND, 0);
			mCalendar.set(Calendar.MILLISECOND, 0);
			long when = mCalendar.getTimeInMillis() + 60000;
			alarmManager.set(AlarmManager.RTC, when, pi);
			//*******************************************
			//Пишем время на широком виджете.
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
		//*******УСТАНОВКА СЛУШАТЕЛЕЙ НА КНОПКИ (независимо от того знаем мы координаты или нет)*******
		//Слушатель на кнопку "Праздники".
		/* Используем этот код, если хотим вызвать этот класс после нажатия кнопки
		i.putExtra(WIDGET_BUTTON_ID, HOLLIDAYS_BUTTON_ID);
		i.setData(Uri.parse(i.toUri(Intent.URI_INTENT_SCHEME)));
		pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
		updateViews.setOnClickPendingIntent(R.id.HolidaysButton, pi);
		*/
		PendingIntent pi;
		Intent iact; //Используем этот интент, если при нажатии кнопки на виджете хотим сразу вызвать активити.
		iact = new Intent(context, ChronosMainActivity.class);
		iact.putExtra(ChronosMainActivity.SELECT_SCREEN_NUMBER, ChronosMainActivity.HOLIDAYS_SCREEN_NUMBER);
		iact.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Чтобы закрыть все окна, которые были открыты в активити (например подробную историю дня, когда пользователь не закрыл активити с этой информацией).
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
		//*******ПРОГРАММИРОВАНИЕ РЕАКЦИИ НА НАЖАТИЕ КНОПОК (если выше указано, что при нажатии запускается этот класс, а не другое активити)*******
		String UpdatesTxt = "Widget updates: " + Long.toString(mNumberOfWidgetUpdates);
		Log.i(PublicConstantsAndMethods.MY_LOG_TAG, UpdatesTxt);
		//Пока не нужно, т.к. для этого нужно находиться в классе виджета, а мы всё перенесли в отдельный класс.
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
					/* Вызываем активити, если оно не было вызвано ранее из лаунчера. (т.е. если мы хотим что-то отображать на виджите перед тем как вызвать активити)
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
			cur.close(); // - обратить внимание! Курсор мы закрываем!
			return HolidaysType;
		}
		else
		{
			cur.close(); // - обратить внимание! Курсор мы закрываем!
			return null;
		}
	}
	//Получаем Layout для виджета, вызвавшего этот сервис.
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
	//Получаем класс виджета, вызвавшего этот сервис.
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
	//Выводит сообщение в Toast в главном потоке приложения.
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