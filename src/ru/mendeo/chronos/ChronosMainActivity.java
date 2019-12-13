package ru.mendeo.chronos;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.*;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.StaticLayout;
import android.text.Layout.Alignment;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

@SuppressWarnings("deprecation")
public class ChronosMainActivity extends ActionBarActivity
{
	private String WRONG_TEXT = "ru.mendeo.chronos.WrongText";
	private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
	public static final int HOLIDAYS_SCREEN_NUMBER = 0;
	public static final int SUN_SCREEN_NUMBER = 1;
	public static final int MOON_SCREEN_NUMBER = 2;
	public static final int REMINDERS_SCREEN_NUMBER = 3;
	public static final int INVALID_SCREEN = -1;
	public static final String SELECT_SCREEN_NUMBER = "screen_number";
	public static final String SHOW_ALL_REMINDERS_STATE = "show_all_reminders_state";
	public static final String UNKNOWN_CITY = "Неизвестный город";
	public static final String UNKNOWN_REGION = "Неизвестный регион";
	private static final int FUTUTRE_DATE = 1;
	private static final int PAST_DATE = -1;
	private static final int CURRENT_DATE = 0;
	private static final int CONFIRM_DELETE_ALL_REMINDERS_DIALOG = 1003;
	private static final int ACTIVITY_CREATE = 110;
	private static final int ACTIVITY_EDIT = 111;
	private static final int ACTIVITY_PRREFERENCES = 120;
	private static final int ACTIVITY_INFO = 133;
	private static final int ACTIVITY_SET_DATE_AND_TIME_DIALOG = 135;
	private static final String SUN_FACTS_ID_KEY = "ru.mendeo.chronos.sunfactsidkeymainactivity";
	private static final String MOON_FACTS_ID_KEY = "ru.mendeo.chronos.moonfactsidkeymainactivity";
	private static final String SUN_FACTS_LAST_UPDATE_TIME_KEY = "ru.mendeo.chronos.sunlastapdatetimemainactivity";
	private static final String MOON_FACTS_LAST_UPDATE_TIME_KEY = "ru.mendeo.chronos.sunlastapdatetimemainactivity";
	private static final int LOCATION_AUTO = 201;
	private static final int LOCATION_MANUAL = 202;
	private static final long MAX_GPS_WAITING_TIME = 180000;
	private static final long TIME_TO_SAVE_PREVIOUS_DATE = 6 * 3600000;
	private static final long TIME_TO_SAVE_SEARCH_HISTORY = 24 * 3600000;
	private LocationManager mLocationManager;
	private LocationListener mLocationListener = null;
	private Location mLocation = null;
	//Сделаем переменные, связанные координаты статическими. Это нужно для того, чтобы когда программа открывалась из другого источника (например виджета), то ей уже были известны координаты, полученные на первом источники (например при запуске с иконки). Т.к. иначе координаты никак не узнать, поскольку они сохраняются в onDestroy, а окно, запущенное с иконки ещё не закрывалось.
	private static double mLongitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	private static double mLatitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	private static long mLocationTime = PublicConstantsAndMethods.INVALID_LOCATION_TIME;
	private static float mLocationAccuracy = PublicConstantsAndMethods.INVALID_LOCATION_ACCURACY;
	private static String mLocationProvider = "";
	private RemindersDbAdapter mRemindersDbAdapter;
	private ExternalDbAdapter mExternalDbAdapter;
	private DisplayMetrics mDisplayMetrics = null;
	private ListView mHolidaysListType1, mHolidaysListType2, mHolidaysListType3, mHolidaysListType4, mHolidaysListType5, mHolidaysListType6, mHolidaysListType7, mHolidaysListType8;
	private ListViewForSliding mRemindersList, mHistoryListView;
	private TextView mHolidaysTitleText;
	private TextView mRemindersPresentationStateText;
	private TextView mShowSelectedDayText;
	private TextView mSunAstroInfoText, mSunFactsTitle, mSunSignsTitle;
	private EllipsizingTextView mSunFactsText, mSunSignsText;
	private TextView mMoonAstroInfoText, mMoonFactsTitle, mMoonCalendarTitle;
	private EllipsizingTextView mMoonFactsText, mMoonCalendarText;
	private LinearLayout mHolidaysLayoutType1, mHolidaysLayoutType2, mHolidaysLayoutType3, mHolidaysLayoutType4, mHolidaysLayoutType5, mHolidaysLayoutType6, mHolidaysLayoutType7, mHolidaysLayoutType8;
	private GetSizeLinearLayout mSunForGettingSize, mMoonForGettingSize;
	private int mNewDateStatus = CURRENT_DATE;
	private Calendar mDateTime, mCurrentDateTime;
	private LinearLayout mSetDateTimeBt;
	private SlidingView mMainSlidingView;
	private SharedPreferences mSharedPreferences;
	private int mCurrentScreenId;
	private boolean[] mIsScreenOptionsMenuCreated;
	private NotificationManager mgr;
	private boolean mIsAfterOnCreate;
	private boolean mIsDbsOpen = false;
	private boolean mShowAllRemindersState;
	private String mCurrentCity, mCityKey, mCurrentRegion, mRegionKey;
	private static long mCurrentSunFactId = -1;
	private static long mCurrentMoonFactId = -1;
	private long mFirstNetworkLocationTime = -1;
	//Время последнего обновления фактов о Солнце и Луне.
	private static int[] mSunFactLastUpdateTime = {-1, -1, -1};
	private static int[] mMoonFactLastUpdateTime = {-1, -1, -1};
	private int mSunVarTextHeight, mMoonVarTextHeight, mSunVarTextWidth, mMoonVarTextWidth;	
	private String mSunFactsPlainText, mSunSignsPlainText, mMoonFactsPlainText, mMoonCalendarPlainText;
	private boolean mLocationAlreadyStarted = false;	
	private ActionBar mActionBar;
	private TextView mNoRemindersText;
	private boolean mIsAfterOnNewIntent = false;
	private boolean mShouldShowRequestPermissionDialog;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main); 
        Log.d("MyLogD", "onCreate");
        
        mIsAfterOnCreate = true;
        mSunVarTextHeight = -1;
        mMoonVarTextHeight = -1;
        mSunVarTextWidth = -1;
        mMoonVarTextWidth = -1;
        mSunFactsPlainText = WRONG_TEXT;
        mSunSignsPlainText = WRONG_TEXT;
        mMoonFactsPlainText = WRONG_TEXT;
        mMoonCalendarPlainText = WRONG_TEXT;
        //Для получения размеров и плотности экрана получаем DisplayMetrics
        mDisplayMetrics = new DisplayMetrics();
        ((android.view.WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mDisplayMetrics);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mSetDateTimeBt = (LinearLayout)findViewById(R.id.setDateTimeButton);
        mMainSlidingView = (SlidingView)findViewById(R.id.MainSlidingView);
        mHolidaysLayoutType1 = (LinearLayout)findViewById(R.id.holidays_type1_layout);
        mHolidaysLayoutType2 = (LinearLayout)findViewById(R.id.holidays_type2_layout);
        mHolidaysLayoutType3 = (LinearLayout)findViewById(R.id.holidays_type3_layout);
        mHolidaysLayoutType4 = (LinearLayout)findViewById(R.id.holidays_type4_layout);
        mHolidaysLayoutType5 = (LinearLayout)findViewById(R.id.holidays_type5_layout);
        mHolidaysLayoutType6 = (LinearLayout)findViewById(R.id.holidays_type6_layout);
        mHolidaysLayoutType7 = (LinearLayout)findViewById(R.id.holidays_type7_layout);
        mHolidaysLayoutType8 = (LinearLayout)findViewById(R.id.holidays_type8_layout);
        mSunForGettingSize = (GetSizeLinearLayout)findViewById(R.id.sunForGettingSize);
        mMoonForGettingSize = (GetSizeLinearLayout)findViewById(R.id.moonForGettingSize);
        mHolidaysListType1 = (ListView)findViewById(R.id.holidays_type1_list);
        mHolidaysListType2 = (ListView)findViewById(R.id.holidays_type2_list);
        mHolidaysListType3 = (ListView)findViewById(R.id.holidays_type3_list);
        mHolidaysListType4 = (ListView)findViewById(R.id.holidays_type4_list);
        mHolidaysListType5 = (ListView)findViewById(R.id.holidays_type5_list);
        mHolidaysListType6 = (ListView)findViewById(R.id.holidays_type6_list);
        mHolidaysListType7 = (ListView)findViewById(R.id.holidays_type7_list);
        mHolidaysListType8 = (ListView)findViewById(R.id.holidays_type8_list);
        mHolidaysTitleText = (TextView)findViewById(R.id.HolidaysTitleText);
        mHistoryListView = (ListViewForSliding)findViewById(R.id.history_list);
        mRemindersList = (ListViewForSliding)findViewById(R.id.reminders_list);
        mSunAstroInfoText = (TextView)findViewById(R.id.sun_astro_info);
        mSunFactsText = (EllipsizingTextView)findViewById(R.id.sun_facts_info);
        mSunSignsText = (EllipsizingTextView)findViewById(R.id.signs_info);
        mSunFactsTitle = (TextView)findViewById(R.id.sun_facts_title);
        mSunSignsTitle = (TextView)findViewById(R.id.signs_title);
        mMoonAstroInfoText = (TextView)findViewById(R.id.moon_astro_info);
        mMoonFactsText = (EllipsizingTextView)findViewById(R.id.moon_facts_info);
        mMoonCalendarText = (EllipsizingTextView)findViewById(R.id.moon_calendar_info);  
        mMoonFactsTitle = (TextView)findViewById(R.id.moon_facts_title);
        mMoonCalendarTitle = (TextView)findViewById(R.id.moon_calendar_title);
        mRemindersPresentationStateText = (TextView)findViewById(R.id.RemindersPresentationStateText);
        mShowSelectedDayText = (TextView)findViewById(R.id.show_selected_date_text);
        mNoRemindersText = (TextView)findViewById(R.id.reminders_empty);
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mCurrentDateTime = Calendar.getInstance();
        int screenId = INVALID_SCREEN;
        //Получение номера экрана приоритетней от savedInstanceState, нежели чем от getIntent(). Так как, если номер экрана существует и там и там (такое случается, например, если перевернуть телефон), то ставить нужно последний сохранённый экран, а не тот, с которого запускалось активити.
        if (savedInstanceState != null) 
        {
        	screenId = savedInstanceState.getInt(SELECT_SCREEN_NUMBER, INVALID_SCREEN);
        	mLongitude = savedInstanceState.getDouble(getString(R.string.preferences_longitude_key));
        	mLongitude = savedInstanceState.getDouble(getString(R.string.preferences_latitude_key));
        	mCurrentSunFactId = savedInstanceState.getLong(SUN_FACTS_ID_KEY, -1);
        	mCurrentMoonFactId = savedInstanceState.getLong(MOON_FACTS_ID_KEY, -1);
        	mSunFactLastUpdateTime = savedInstanceState.getIntArray(SUN_FACTS_LAST_UPDATE_TIME_KEY);
        	mMoonFactLastUpdateTime = savedInstanceState.getIntArray(MOON_FACTS_LAST_UPDATE_TIME_KEY);
        	mShowAllRemindersState = savedInstanceState.getBoolean(SHOW_ALL_REMINDERS_STATE);
        	mLocationTime = savedInstanceState.getLong(getString(R.string.preferences_location_time_key));
        	mLocationAccuracy = savedInstanceState.getFloat(getString(R.string.preferences_location_accuracy_key));
        	mLocationProvider = savedInstanceState.getString(getString(R.string.preferences_location_provider_key));
        	mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(savedInstanceState);
        	//Тут надо оценить насколько давно получена дата, на которую отображаются все данные.
        	//Пока моим волевым решением принимаем, что если человек свернул программу более чем на TIME_TO_SAVE_PREVIOUS_DATE миллисекунд (6 часов), то вновь открыв её, дата обновиться, иначе останется той, чтобы была раньше.
        	if (mCurrentDateTime.getTimeInMillis() - mDateTime.getTimeInMillis() > TIME_TO_SAVE_PREVIOUS_DATE) mDateTime = Calendar.getInstance();
        }
        else
        {
            //Если координат нет в savedInstanceState, то они могут быть известны с предыдущего запуска программы, если она не закрывалась (как статические переменные). Иначе они должны быть в SharedPreferences, но если и там нет, то это первый запуск приложения и координаты ещё не получены, тогда приписываем им значение, которое будет говорить остальной программе, что координаты ещё не получены.
        	if (!PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
        	{
	        	mLongitude = (double)mSharedPreferences.getFloat(getString(R.string.preferences_longitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
	        	mLatitude = (double)mSharedPreferences.getFloat(getString(R.string.preferences_latitude_key), (float)PublicConstantsAndMethods.INVALID_COORDINATES);
	        	mLocationTime = mSharedPreferences.getLong(getString(R.string.preferences_location_time_key), PublicConstantsAndMethods.INVALID_LOCATION_TIME);
	        	mLocationAccuracy = mSharedPreferences.getFloat(getString(R.string.preferences_location_accuracy_key), PublicConstantsAndMethods.INVALID_LOCATION_ACCURACY);
	        	mLocationProvider = mSharedPreferences.getString(getString(R.string.preferences_location_provider_key), "");
        	}
        	mDateTime = Calendar.getInstance();
            mShowAllRemindersState = false;
        }
        if (mShowAllRemindersState)
        {
        	mRemindersPresentationStateText.setText(getString(R.string.reminders_presentation_all_text));
        }
        else
        {
        	mRemindersPresentationStateText.setText(getString(R.string.reminders_presentation_today_text));
        }
        if (screenId == INVALID_SCREEN && getIntent() != null) screenId = getIntent().getIntExtra(SELECT_SCREEN_NUMBER, INVALID_SCREEN);
		//Если номера экрана нет и в intent'е, то попробуем найти его в настройках.
        if (screenId == INVALID_SCREEN) screenId = mSharedPreferences.getInt(SELECT_SCREEN_NUMBER, INVALID_SCREEN);
        if (screenId == INVALID_SCREEN)
        {
        	String info = "Cannot define screen id. Maybe this is first start.";
        	Log.i(PublicConstantsAndMethods.MY_LOG_TAG, info);
        	screenId = HOLIDAYS_SCREEN_NUMBER;
        }
        registerDateAndTimeButtonListeners();
        registerScreenSwitchListener();
        registerHolidaysListsListeners();
        registerHistoryListListener();
        registerRemindersListener();
        registerSunAndMoonInfoTextListener();
        registerLocationListener();
        registerSizeChangeListeners();
        //fillAndRegisterListenersForScreenList();
        mRemindersDbAdapter = new RemindersDbAdapter(this);
        mExternalDbAdapter = new ExternalDbAdapter(this);
        openAllDatabases();
        mCityKey = getString(R.string.preferences_list_city_key);
        mRegionKey = getString(R.string.preferences_list_region_key);
        updateScreensForNewDate();
        //Обновляем данные по Солнцу и Луне.
        showSunData();
        showMoonData();
        registerForContextMenu(mRemindersList);
        mIsScreenOptionsMenuCreated = new boolean[mMainSlidingView.getChildCount()];
		for (int i = 0; i < mIsScreenOptionsMenuCreated.length; i++)
		{
			if (mIsScreenOptionsMenuCreated[i]) mIsScreenOptionsMenuCreated[i] = false;
		}
        mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        long timeToShowRateDialog = mSharedPreferences.getLong(RateAppActivity.PREFERENCES_TIME_WHEN_SHOW_RATE_OUR_APP_DIALOG, mCurrentDateTime.getTimeInMillis() + RateAppActivity.TIME_AFTER_REPEAT_RATE_DIALOG); //Если такой настройки нет, значит это первый запуск программы и запрос мнения пользователя будем проводить только через 3 дня.
        if (timeToShowRateDialog > 0 && mCurrentDateTime.getTimeInMillis() >= timeToShowRateDialog) PublicConstantsAndMethods.mNeedToShowRateOurAppsDialog = true;
        //Обновляем виджет так как программа аварийно завершилась, и необходимо восстановить отчёт времени на виджете
        if (WidgetWorkService.mNeedUpdateAfterFirstStart) 
        {
    		WidgetWorkService.mLongitude = mLongitude;
    		WidgetWorkService.mLatitude = mLatitude;
        	updateWidget();
        }
        //Работаем с Action Bar
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayUseLogoEnabled(false);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        String fType = "navigation_img_id"; //Содержит название блока в строке навигационного списка.
        //Здесь перечислены все типы экранов!
        int[] screensTitles = {R.drawable.holidays_title, R.drawable.sun_title, R.drawable.moon_title, R.drawable.notes_title};
        ArrayList<HashMap<String, Integer>> al = new ArrayList<HashMap<String, Integer>>(screensTitles.length);
        for (int i = 0; i < screensTitles.length; i++)
        {
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            hm.put(fType, screensTitles[i]); //
            al.add(hm);
        }
        String[] from = {fType};
        int[] to = {R.id.navigation_img};
        SimpleAdapter adapter = new SimpleAdapter(this, al, R.layout.navigation_row, from, to);
        mActionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener()
        {
			public boolean onNavigationItemSelected(int itemPosition, long itemId)
			{
				// TODO Auto-generated method stub
				mMainSlidingView.setCurrentScreen(itemPosition);
				return true;
			}
		});
        mActionBar.setBackgroundDrawable(new BackgroundLineDrawable());
        //Отображаем три точки независимо от того, есть ли физическая кнопка меню или нет.
        //Говорят, что это очень плохо, и так делать нельзя. Но как делать правильно никто не пишет, поэтому делаем так.
        //Хотя я честно признаюсь, что понимаю, что так делать плохо и работать будет не везде, а когда-то может вообще перестать работать.
        try
        {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null)
            {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        }
        catch (Exception e)
        {
            //e.printStackTrace();
        }
        mMainSlidingView.setCurrentScreen(screenId); //Установить ID экрана мы должны только после того, как создали выпадаюзий список в Action Bar.
        if (getIntent().getAction().equals(PublicConstantsAndMethods.ACTION_AFTER_SEARCH)) changeDateFromIntent(getIntent(), SearchableActivity.DATE_EXSTRAS_KEY); //Если пришли с поиска, то обновляем дату на ту, что получена в поиске. Делаем это в onCreate и onNewIntent, т.к. пока неясно как отловить закрытие активити поиска в onActivityResult.
        clearSearchHistory();
        mShouldShowRequestPermissionDialog = true;
    }
    //Этот метод срабатывает, когда из разных мест вызываем главное окно (например нажимаем кнопки на виджите).
    //В манифесте стоит launchMode="singleTop". Это значит, что в этом случае не будет создаваться новое активити, а запустится старое. И вызовится этот метод c новым intent'ом.
    //Поэтому т.к. не было onCreate мы должны запустить нужный экран.
    @Override
    protected void onNewIntent(Intent intent)
    {
    	// TODO Auto-generated method stub
    	Log.d("MyLogD", "onNewIntent");
    	setIntent(intent); //В onResume передаём новый интент.
    	mIsAfterOnNewIntent = true; //Сообщаем onResume, что оно запущено после onNewIntent
    	//Выставляем нужный номер экрана, т.к. onCreate не запускается (мы берём готовое активити) и экран нужно выставить тут.
		int screenId = intent.getIntExtra(SELECT_SCREEN_NUMBER, INVALID_SCREEN);
		if (screenId == INVALID_SCREEN)
		{
			if (mCurrentScreenId == INVALID_SCREEN)
			{
				screenId = HOLIDAYS_SCREEN_NUMBER;
			}
			else
			{
				screenId = mCurrentScreenId;
			}
		}
    	mMainSlidingView.setCurrentScreen(screenId);
    	//Обновляем отображаемое время, чтобы при вызову с виджета точно было текущее время.
		mCurrentDateTime = Calendar.getInstance();
    	if (intent.getAction().equals(PublicConstantsAndMethods.ACTION_AFTER_SEARCH))
    	{
    		openAllDatabases();
    		changeDateFromIntent(intent, SearchableActivity.DATE_EXSTRAS_KEY); //Если пришли с поиска, то обновляем дату на ту, что получена в поиске. Делаем это в onCreate и onNewIntent, т.к. пока неясно как отловить закрытие активити поиска в onActivityResult.
    	}
    	else if(mCurrentDateTime.getTimeInMillis() - mDateTime.getTimeInMillis() > TIME_TO_SAVE_PREVIOUS_DATE || intent.getAction().equals(PublicConstantsAndMethods.ACTION_APPWIDGET_BUTTON_TAP))
    	{
        	//Тут надо оценить насколько давно получена дата, на которую отображаются все данные.
        	//Пока моим волевым решением принимаем, что если человек свернул программу более чем на TIME_TO_SAVE_PREVIOUS_DATE миллисекунд (6 часов), то вновь открыв её, дата обновиться, иначе останется той, чтобы была раньше.
    		//Однако, если окно открывается с виджета, то нужно отображать актуальные данные.
    		openAllDatabases();
    		boolean isEqualDate = (mDateTime.get(Calendar.YEAR) == mCurrentDateTime.get(Calendar.YEAR)) && (mDateTime.get(Calendar.DAY_OF_YEAR) == mCurrentDateTime.get(Calendar.DAY_OF_YEAR));
    		mDateTime = (Calendar)mCurrentDateTime.clone();
    		mNewDateStatus = CURRENT_DATE;
    		//Если новая и старая даты не совпадают, то обновляем только время, а не полностью дату.
    		if (isEqualDate)
    		{
    			updateScreensForNewTime();
    		}
    		else
    		{
    			updateScreensForNewDate();
    		}
    	}
    	clearSearchHistory();
    	mShouldShowRequestPermissionDialog = true;
    	super.onNewIntent(intent);
    }
    //Очищаем историю поиска, если последний поиск был более TIME_TO_SAVE_SEARCH_HISTORY (24 часов) назад.
    public void clearSearchHistory()
    {
        long lastSearchTime = mSharedPreferences.getLong(PublicConstantsAndMethods.LAST_SEARCH_TIME_KEY, 0);
        if (mCurrentDateTime.getTimeInMillis() - lastSearchTime > TIME_TO_SAVE_SEARCH_HISTORY)
        {
    		SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE); //SearchSuggestionProvider - это мой класс, который наследует SearchRecentSuggestionsProvider
    		suggestions.clearHistory();
        }
    }
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	Log.d("MyLogD", "onDestroy");
    	stopLocation();
    	SharedPreferences.Editor e = mSharedPreferences.edit();
    	e.putInt(SELECT_SCREEN_NUMBER, mCurrentScreenId);
    	e.commit();
    	closeAllDatabases();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	Log.d("MyLogD", "onSaveInstanceState");
    	outState.putLong(SUN_FACTS_ID_KEY, mCurrentSunFactId);
    	outState.putLong(MOON_FACTS_ID_KEY, mCurrentMoonFactId);
    	outState.putIntArray(SUN_FACTS_LAST_UPDATE_TIME_KEY, mSunFactLastUpdateTime);
    	outState.putIntArray(MOON_FACTS_LAST_UPDATE_TIME_KEY, mMoonFactLastUpdateTime);
    	PublicConstantsAndMethods.setCalendarToBundle(mDateTime, outState);
    	outState.putInt(SELECT_SCREEN_NUMBER, mCurrentScreenId);
    	outState.putBoolean(SHOW_ALL_REMINDERS_STATE, mShowAllRemindersState);    
    	outState.putDouble(getString(R.string.preferences_longitude_key), mLongitude);
    	outState.putDouble(getString(R.string.preferences_latitude_key), mLatitude);
    	outState.putLong(getString(R.string.preferences_location_time_key), mLocationTime);
    	outState.putFloat(getString(R.string.preferences_location_accuracy_key), mLocationAccuracy);
    	outState.putString(getString(R.string.preferences_location_provider_key), mLocationProvider);
    }
    //Не знаю почему, но без этого программа не работает на андроидах больше 2.3.3
    @Override
    public void startManagingCursor(Cursor c)
    {
        // TODO Auto-generated method stub
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) super.startManagingCursor(c);
    }
    @Override
    protected void onResume()
    {	
    	super.onResume();
    	Log.d("MyLogD", "onResume");
    	openAllDatabases();
    	boolean isFirstAppStart = false;
    	//Проверка на то, идёт ли запуск onResume после onCreate или нет.
    	if (mIsAfterOnCreate)
    	{
    		mIsAfterOnCreate = false;
    		//Проверяем, запускается приложение в самый первый раз или нет.
            if (mSharedPreferences.getBoolean(getString(R.string.preferences_is_first_start_key), true))
            {
            	SharedPreferences.Editor e = mSharedPreferences.edit();
            	e.putBoolean(getString(R.string.preferences_is_first_start_key), false);
            	e.commit();
            	//Если у нас первый запуск приложения, то вызываем настройки, указывая, что это первый запуск.
    			Intent i = new Intent(getApplicationContext(), ChronosPreferences.class);
    			i.putExtra(getString(R.string.preferences_is_first_start_key), true);
    			startActivityForResult(i, ACTIVITY_PRREFERENCES);
    			isFirstAppStart = true;
            }
    	}
    	//Проверка на то, идёт ли запуск onResume после onNewIntent или нет.
    	else if (mIsAfterOnNewIntent)
    	{
    		mIsAfterOnNewIntent = false;
    	}
    	else
    	{
    		mCurrentDateTime = Calendar.getInstance();
    	}
        //Если не первый запуск, то смотрим нужно ли нам обновлять координаты (при условии, что они должны получаться автоматически).
        if (!isFirstAppStart && mSharedPreferences.getBoolean(getString(R.string.preferences_auto_location_key), false))
        {
        	//Если за время паузы активити были получены координаты, то мы их используем для отображения астрономической информации.
        	if (mLocation != null) acquireCoordinates(LOCATION_AUTO);
        	//Если предыдущие координаты неизвестны, то приложение закрылось до того, как успело их получить, поэтому мы в любом случае должны получить координаты.
        	if ((mLocationTime == PublicConstantsAndMethods.INVALID_LOCATION_TIME) || !PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
        	{
        		startLocation();
        	}
        	else
        	{
	        	String[] freqValues = getResources().getStringArray(R.array.auto_location_freq_pref);
	        	//Если указано в настройках обновлять координаты каждый раз при запуске приложения (когда запускается onCreate).
	        	if (mSharedPreferences.getString(getString(R.string.preferences_auto_location_freq_key), "").equals(freqValues[0]) && mIsAfterOnCreate)
	        	{
	        		startLocation();
	        	}
	        	//Если указано в настройках обновлять координаты не чаще раза в день.
	        	else if (mSharedPreferences.getString(getString(R.string.preferences_auto_location_freq_key), "").equals(freqValues[1]) && Math.abs(mCurrentDateTime.getTimeInMillis() - mLocationTime) >= 86400000)
	    		{
	        		startLocation();
	    		}
	        	//Если указано в настройках обновлять координаты не чаще раза в час.
	        	else if (mSharedPreferences.getString(getString(R.string.preferences_auto_location_freq_key), "").equals(freqValues[2]) && Math.abs(mCurrentDateTime.getTimeInMillis() - mLocationTime) >= 3600000)
	        	{
	        		startLocation();
	        	}
	        	//Если указано в настройках обновлять координаты обновить только один раз.
	        	else if(mSharedPreferences.getString(getString(R.string.preferences_auto_location_freq_key), "").equals(freqValues[3]) && mSharedPreferences.getBoolean(getString(R.string.preferences_is_location_update_key), false))
	        	{
	        		Editor e = mSharedPreferences.edit();
	        		e.putBoolean(getString(R.string.preferences_is_location_update_key), false);
	        		e.commit();
	        		startLocation();
	        	}
        	}
        }
    }
    @Override
    protected void onPause()
    {
    	super.onPause();
    	Log.d("MyLogD", "onPause");
    	closeAllDatabases();
    }
    private void openAllDatabases()
    {
		if (!mIsDbsOpen)
		{
			//Log.d("MyLog", "Opening");
			mRemindersDbAdapter.open();
			mExternalDbAdapter.open();
			mIsDbsOpen = true;
		}
    }
    private void closeAllDatabases()
    {
    	if (mIsDbsOpen)
    	{
    		//Log.d("MyLog", "Closing");
    		mRemindersDbAdapter.close();
    		mExternalDbAdapter.close();
    		mIsDbsOpen = false;
    	}
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	super.onPrepareOptionsMenu(menu);
    	//Если меню для данного экрана уже создано, то просто его показываем.
    	if (mIsScreenOptionsMenuCreated[mCurrentScreenId]) return true;
    	//Если меню для данного экрана ещё не создано, то создаём его, и потом уже показываем
		MenuInflater mi = getMenuInflater();
		menu.clear();
		for (int i = 0; i < mIsScreenOptionsMenuCreated.length; i++)
		{
			if (mIsScreenOptionsMenuCreated[i]) mIsScreenOptionsMenuCreated[i] = false;
		}
		mIsScreenOptionsMenuCreated[mCurrentScreenId] = true; //Говорим, что меню для данного экрана уже создано, и мы не будем его пересоздавать, когда пользователь нажмёт кнопку меню в следующий раз
    	switch (mCurrentScreenId)
    	{
    		case HOLIDAYS_SCREEN_NUMBER:   
    			mi.inflate(R.menu.holidays_options_menu, menu);
    			return true;
    		case SUN_SCREEN_NUMBER:
    			mi.inflate(R.menu.sun_options_menu, menu);
    			return true;
    		case MOON_SCREEN_NUMBER:
    			mi.inflate(R.menu.moon_options_menu, menu);
    			return true;
    		case REMINDERS_SCREEN_NUMBER:
    			mi.inflate(R.menu.reminders_options_menu, menu);
    			MenuItem item_switch = menu.findItem(R.id.menu_switch_reminders_presentation);
    			MenuItem item_delete = menu.findItem(R.id.menu_delete_reminders);
    			if (mShowAllRemindersState)
    			{
    				item_switch.setTitle(getString(R.string.menu_show_today_reminders));
    				item_delete.setTitle(getString(R.string.menu_delete_all_reminders));
    			}
    			else
    			{
    				item_switch.setTitle(getString(R.string.menu_show_all_reminders));
    				item_delete.setTitle(getString(R.string.menu_delete_today_reminders));
    			}    			
    			return true;
    		default:
    			Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No such screen id. Menu will not inflate.");
    			return false;
    	}
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	Intent i;
    	switch(item.getItemId())
    	{
    		case R.id.menu_search:
    			onSearchRequested();
    			return true;
    		case R.id.menu_set_date_time:
    			i = new Intent(getApplicationContext(), SetDateActivity.class);
				i.putExtra(PublicConstantsAndMethods.CURRENT_SCREEN_ID_KEY, mCurrentScreenId);
				Bundle dateTime = new Bundle();
				PublicConstantsAndMethods.setCalendarToBundle(mDateTime, dateTime);
				i.putExtra(PublicConstantsAndMethods.DATE_AND_TIME_CALENDAR_KEY, dateTime);
				startActivityForResult(i, ACTIVITY_SET_DATE_AND_TIME_DIALOG);
				return true;
    		case R.id.menu_preferences:
    			i = new Intent(getApplicationContext(), ChronosPreferences.class);
    			startActivityForResult(i, ACTIVITY_PRREFERENCES);
    			return true;
    		case R.id.menu_info:
    			i = new Intent(getApplicationContext(), InfoActivity.class);
				i.putExtra(getString(R.string.preferences_longitude_key), mLongitude);
				i.putExtra(getString(R.string.preferences_latitude_key), mLatitude);
    			if (mSharedPreferences.getBoolean(getString(R.string.preferences_auto_location_key), false))
    			{
					i.putExtra(PublicConstantsAndMethods.LOCATION_TIME_KEY, mLocationTime);
					i.putExtra(PublicConstantsAndMethods.LOCATION_ACCURACY_KEY, mLocationAccuracy);
					i.putExtra(PublicConstantsAndMethods.LOCATION_PROVIDER_KEY, mLocationProvider);
    			}
    			else
    			{
    				String tmp = getString(R.string.preferences_list_region_key);
    				i.putExtra(tmp, mSharedPreferences.getString(tmp, ""));
    				tmp = getString(R.string.preferences_list_city_key);
    				i.putExtra(tmp, mSharedPreferences.getString(tmp, ""));
    			}
    			startActivityForResult(i, ACTIVITY_INFO);
    			return true;
    		case R.id.menu_insert_reminder:
    			createReminder();
    			return true;
    		case R.id.menu_delete_reminders:
    			showDialog(CONFIRM_DELETE_ALL_REMINDERS_DIALOG);
    			return true;
    		case R.id.menu_switch_reminders_presentation:
    			mIsScreenOptionsMenuCreated[REMINDERS_SCREEN_NUMBER] = false; //Говорим, что при следующем нажатии кнопки меню пользователем мы должны пересоздать меню (ну чтобы обновить названия кнопок)
    			mShowAllRemindersState = !mShowAllRemindersState;
    			//mIsFirstStart = true;
    	        if (mShowAllRemindersState)
    	        {
    	        	mRemindersPresentationStateText.setText(getString(R.string.reminders_presentation_all_text));
    	        }
    	        else
    	        {
    	        	mRemindersPresentationStateText.setText(getString(R.string.reminders_presentation_today_text));
    	        }
    			fillReminders();
    		default:
    			return super.onOptionsItemSelected(item);
    	}    	
    }
    private void createReminder()
    {
    	Intent i = new Intent(this, ReminderEditActivity.class);
    	Bundle extras = new Bundle();
    	PublicConstantsAndMethods.setCalendarToBundle(mDateTime, extras);
    	i.putExtras(extras);
    	startActivityForResult(i, ACTIVITY_CREATE);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	super.onActivityResult(requestCode, resultCode, intent);
    	Log.d("MyLogD", "onActivityResult");
    	openAllDatabases();
    	switch (requestCode)
    	{
    		case ACTIVITY_PRREFERENCES:
    			if (mSharedPreferences.getBoolean(getString(R.string.preferences_auto_location_key), false))
    			{
    				//Обработка идёт в onResume. Там уже смотрится пришло ли время обновлять координаты по данным, указанным в настрйоках
    			}
    			else
    			{
        			stopLocation();
        			acquireCoordinates(LOCATION_MANUAL);
    			}
    		break;
    		case ACTIVITY_CREATE: case ACTIVITY_EDIT:
    			fillReminders();
        	break;
    		case ACTIVITY_INFO:
    			if (resultCode == PublicConstantsAndMethods.RESULT_GET_NEW_LOCATION) startLocation();
    		break;
    		case ACTIVITY_SET_DATE_AND_TIME_DIALOG:
    			if (resultCode == RESULT_OK)
    			{
    				changeDateFromIntent(intent, PublicConstantsAndMethods.DATE_AND_TIME_CALENDAR_KEY);
    			}
    		break;
    	}
    }
    private void changeDateFromIntent(Intent intent, String dateExtraKey)
    {
    	Bundle calBundle = intent.getBundleExtra(dateExtraKey);
		if (calBundle == null) return;
    	Calendar tmp = PublicConstantsAndMethods.getCalendarFromBundle(calBundle);
		boolean isEqualDate = (mDateTime.get(Calendar.YEAR) == tmp.get(Calendar.YEAR)) && (mDateTime.get(Calendar.DAY_OF_YEAR) == tmp.get(Calendar.DAY_OF_YEAR));
		mDateTime = tmp;
		//Проверяем статус новой даты (прошлое, настоящее или будуще)
		if ((mDateTime.get(Calendar.YEAR) == mCurrentDateTime.get(Calendar.YEAR)) && (mDateTime.get(Calendar.DAY_OF_YEAR) == mCurrentDateTime.get(Calendar.DAY_OF_YEAR)))
		{
			mNewDateStatus = CURRENT_DATE;
		}
		else if(mDateTime.getTimeInMillis() > mCurrentDateTime.getTimeInMillis())
		{
			mNewDateStatus = FUTUTRE_DATE;
		}
		else
		{
			mNewDateStatus = PAST_DATE;
		}
		//Если новая и старая даты не совпадают, то обновляем только время, а не полностью дату.
		if (isEqualDate)
		{
			updateScreensForNewTime();
		}
		else
		{
			updateScreensForNewDate();
		}
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) 
    {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	if (v.getId() == mRemindersList.getId())
    	{
    		MenuInflater mi = getMenuInflater();
    		mi.inflate(R.menu.reminders_context_menu, menu);
    	}
    }
    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
    	switch(item.getItemId())
    	{
    		case R.id.menu_delete_reminder:
    			AdapterContextMenuInfo info = (AdapterContextMenuInfo)item.getMenuInfo();
    			mRemindersDbAdapter.deleteReminder(info.id);
    			fillReminders();
    			new RemindersAlarmManager(this).cancelAlarm(info.id);
    			mgr.cancel((int)info.id);
    			return true;
    		default:
    			return super.onContextItemSelected(item);
    	}
    }
    private void acquireCoordinates(int LocationSource)
    {
    	if (LocationSource == LOCATION_AUTO)
    	{
            //Временно введём координаты Нагасаки.
            //mLongitude = 129.87327778;
            //mLatitude = 32.74552778;
    		//Если первый сигнал пришёл от сети, то записываем его время
    		if (mLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) && mFirstNetworkLocationTime < 0)
    		{
    			mFirstNetworkLocationTime = mLocation.getTime();
    		}
    		//Не останавливаем приём координат, если они получены по сети, но GPS включён. Но ждём GPS не больше минуты с момента первого прихода сигнала от сети.
    		if (!(mLocation.getProvider().equals(LocationManager.NETWORK_PROVIDER) && mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && (mLocation.getTime() - mFirstNetworkLocationTime) < MAX_GPS_WAITING_TIME))
    		{
    			mFirstNetworkLocationTime = -1;
    			stopLocation();
    		}
    		//В любом случае испльзуем полученные координаты, независимо от того ждём мы следующие или выключили приём.
			mLongitude = mLocation.getLongitude();
			mLatitude = mLocation.getLatitude();
			mLocationAccuracy = mLocation.getAccuracy();
			mLocationTime = mLocation.getTime();
			mLocationProvider = mLocation.getProvider();
    	}
    	else if (LocationSource == LOCATION_MANUAL)
    	{
			mLocation = null;
			mLongitude = PublicConstantsAndMethods.INVALID_COORDINATES;
			mLatitude = PublicConstantsAndMethods.INVALID_COORDINATES;
			mLocationTime = PublicConstantsAndMethods.INVALID_LOCATION_TIME;
			mLocationAccuracy = PublicConstantsAndMethods.INVALID_LOCATION_ACCURACY;
			mLocationProvider = "";
    		mCurrentCity = mSharedPreferences.getString(mCityKey, UNKNOWN_CITY);
    		mCurrentRegion = mSharedPreferences.getString(mRegionKey, UNKNOWN_REGION);
            if (!mCurrentCity.equals(UNKNOWN_CITY) && !mCurrentRegion.equals(UNKNOWN_REGION))
            {
                double[] coords = mExternalDbAdapter.getCoordinatesByCityAndRegion(mCurrentCity, mCurrentRegion);
                mLongitude = coords[0];
                mLatitude = coords[1];
            }
    	}
		//Помещаем полученные координаты в SharedPreferences.
    	SharedPreferences.Editor e = mSharedPreferences.edit();
    	e.putFloat(getString(R.string.preferences_longitude_key), (float)mLongitude);
    	e.putFloat(getString(R.string.preferences_latitude_key), (float)mLatitude);
    	e.putLong(getString(R.string.preferences_location_time_key), mLocationTime);
    	e.putFloat(getString(R.string.preferences_location_accuracy_key), mLocationAccuracy);
    	e.putString(getString(R.string.preferences_location_provider_key), mLocationProvider);
    	e.commit();
        showSunData();
        showMoonData();
        //Обновляем виджет так как время восхода и захода изменилось.
		WidgetWorkService.mLongitude = mLongitude;
		WidgetWorkService.mLatitude = mLatitude;
		WidgetWorkService.mIsLocationSearching = false; //Убрали это из stopLocation, т.к. при onDestroy срабатывает этот метод и виджет начинает искать координаты в настройках, хотя они ещё ищутся.
        updateWidget();
    }
    private void updateWidget()
    {
		//Обновим сразу все виджеты (широкий и узкий).
    	Intent intent = null;
		AppWidgetBase appWidget = null;
    	for (int i = 0; i < 2; i++)
    	{
    		switch (i)
    		{
    		case 0:
    			intent = new Intent(this, AppWidget_2x2.class);
    			appWidget = new AppWidget_2x2();
    			break;
    		case 1:
    			intent = new Intent(this, AppWidget_4x1.class);
    			appWidget = new AppWidget_4x1();
    			break;
    		}
        	intent.setAction(PublicConstantsAndMethods.ACTION_APPWIDGET_INTERNAL_UPDATE);
    		LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
    		broadcastManager.registerReceiver(appWidget, new android.content.IntentFilter(PublicConstantsAndMethods.ACTION_APPWIDGET_INTERNAL_UPDATE));
    		broadcastManager.sendBroadcast(intent);
    		broadcastManager.unregisterReceiver(appWidget);
    	}
    }
    private void fillReminders()
    {
    	Cursor remindersCursor;
    	int layout;
    	String[] from;
    	int[] to;
    	if (mShowAllRemindersState)
    	{
    		remindersCursor = mRemindersDbAdapter.fetchAllReminders();
    		from = new String[]{RemindersDbAdapter.KEY_TITLE, RemindersDbAdapter.KEY_YEAR, RemindersDbAdapter.KEY_MONTH, RemindersDbAdapter.KEY_DAY, RemindersDbAdapter.KEY_HOUR, RemindersDbAdapter.KEY_MINUTE};
    		to = new int[]{R.id.all_reminder_row_title, R.id.all_reminder_row_year, R.id.all_reminder_row_month, R.id.all_reminder_row_day, R.id.all_reminder_row_hour, R.id.all_reminder_row_minute};
    		layout = R.layout.all_reminders_row;
    	}
    	else
    	{
    		remindersCursor = mRemindersDbAdapter.fetchReminderForGivenDay(mDateTime);
        	from = new String[]{RemindersDbAdapter.KEY_TITLE, RemindersDbAdapter.KEY_HOUR, RemindersDbAdapter.KEY_MINUTE};
        	to = new int[]{R.id.today_reminder_row_title, R.id.today_reminder_row_hour, R.id.today_reminder_row_minute};
        	layout = R.layout.today_reminders_row;
    	}
    	ArrayList<HashMap<String, Object>> array = ConvertCursorToStringArrayList(remindersCursor);
    	int rows = remindersCursor.getCount();
    	remindersCursor.close();
    	if (rows == 0)
    	{
    		mNoRemindersText.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		mNoRemindersText.setVisibility(View.GONE);
    	}
    	//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    	SimpleAdapterWithCustomId reminders = new SimpleAdapterWithCustomId(this, array, layout, from, to, RemindersDbAdapter.KEY_ROWID);
    	mRemindersList.setAdapter(reminders);
    	//Передаём виджету есть ли сегодня напоминания, которые должны произойти, если fillReminders() сработал при добавлении или удалении напоминания на текущий день
    	Calendar CurTime = Calendar.getInstance();
    	if ((CurTime.get(Calendar.YEAR) == mDateTime.get(Calendar.YEAR)) && (CurTime.get(Calendar.DAY_OF_YEAR) == mDateTime.get(Calendar.DAY_OF_YEAR)))
    	{    		
        	//И обновляем виджет.
    		//Переменная mIsAfterOnCreate сбрасывается в false внутри updateScreensForNewDate()
    		if (!mIsAfterOnCreate)
    		{
    			WidgetWorkService.mWillToDayRemind = mRemindersDbAdapter.WillToDayReminders(CurTime);
    			updateWidget();
    		}
    	}
    }
    private ArrayList<HashMap<String, Object>> ConvertCursorToStringArrayList(Cursor cursor)
    {
    	if (cursor == null) return null;    	
    	int rows = cursor.getCount();
    	ArrayList<HashMap<String, Object>> array = new ArrayList<HashMap<String, Object>>(rows);
    	cursor.moveToFirst();
    	HashMap<String, Object> hm;
    	for (int i = 0; i < rows; i++)
    	{
    		hm = new HashMap<String, Object>();
    		//Преобразуем данные из курсора и помещаем их в HashMap
    		DecimalFormat decFormat = new DecimalFormat("00");
    		String columnName;
    		columnName = RemindersDbAdapter.KEY_ROWID;
    		hm.put(columnName, cursor.getLong(cursor.getColumnIndex(columnName)));
    		columnName = RemindersDbAdapter.KEY_TITLE;
    		hm.put(columnName, cursor.getString(cursor.getColumnIndex(columnName)));
    		columnName = RemindersDbAdapter.KEY_YEAR;
    		hm.put(columnName, decFormat.format(cursor.getInt(cursor.getColumnIndex(columnName))));
    		columnName = RemindersDbAdapter.KEY_MONTH;
    		hm.put(columnName, decFormat.format(cursor.getInt(cursor.getColumnIndex(columnName)) + 1));
    		columnName = RemindersDbAdapter.KEY_DAY;
    		hm.put(columnName, decFormat.format(cursor.getInt(cursor.getColumnIndex(columnName))));    		
    		columnName = RemindersDbAdapter.KEY_HOUR;
    		hm.put(columnName, decFormat.format(cursor.getInt(cursor.getColumnIndex(columnName))));
    		columnName = RemindersDbAdapter.KEY_MINUTE;
    		hm.put(columnName, decFormat.format(cursor.getInt(cursor.getColumnIndex(columnName))));
    		array.add(hm);
    		cursor.moveToNext();
    	}
    	return array;
    }
    private void fillHistory()
    {
    	Cursor cursor = mExternalDbAdapter.getHistoryForCurrentDay(mDateTime);
    	if (cursor == null) return;
    	int rows = cursor.getCount();
    	HashMap<String, Object> hm;
    	ArrayList<HashMap<String, Object>> al = new ArrayList<HashMap<String, Object>>(rows);
    	cursor.moveToFirst();
    	long id;
    	String shortDesc;
    	int year;
    	String yearStr;
    	for (int i = 0; i < rows; i++)
    	{
    		id = cursor.getLong(cursor.getColumnIndex(ExternalDbAdapter.KEY_ROWID));
    		shortDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HISTORY_KEY_SHORT_DESC));
    		year = cursor.getInt(cursor.getColumnIndex(ExternalDbAdapter.HISTORY_KEY_YEAR));
    		hm = new HashMap<String, Object>();
    		hm.put(ExternalDbAdapter.KEY_ROWID, id);
    		hm.put(ExternalDbAdapter.HISTORY_KEY_SHORT_DESC, shortDesc);
    		if (year < 0)
    		{
    			yearStr = Integer.toString(-year) + " " + getString(R.string.year_abbreviation) + "\n" + getString(R.string.year_bc_abbreviation);	
    		}  
    		else
    		{
    			yearStr = Integer.toString(year) + " " + getString(R.string.year_abbreviation);
    		}
    		hm.put(ExternalDbAdapter.HISTORY_KEY_YEAR, yearStr);
    		al.add(hm);
    		cursor.moveToNext();
    	}
    	cursor.close();
    	//Заполняем ListView
    	String[] from = {ExternalDbAdapter.HISTORY_KEY_YEAR, ExternalDbAdapter.HISTORY_KEY_SHORT_DESC};
    	int[] to = {R.id.history_list_year, R.id.history_list_short_desc};
    	SimpleAdapterWithCustomId adapter = new SimpleAdapterWithCustomId(this, al, R.layout.history_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    	mHistoryListView.setAdapter(adapter);
    }
    private void fillHolidays()
    {    	
    	Cursor cursor = mExternalDbAdapter.getHolidaysForCurrentDay(mDateTime);
    	if (cursor == null) return;
    	int rows = cursor.getCount();
    	HashMap<String, Object> hm;
    	ArrayList<HashMap<String, Object>> type1 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type2 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type3 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type4 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type5 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type6 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type7 = new ArrayList<HashMap<String, Object>>(1);
    	ArrayList<HashMap<String, Object>> type8 = new ArrayList<HashMap<String, Object>>(1);
    	cursor.moveToFirst();
    	long id;
    	int typeId;
    	String shortDesc;
    	for (int i = 0; i < rows; i++)
    	{
			id = cursor.getLong(cursor.getColumnIndex(ExternalDbAdapter.KEY_ROWID));
			shortDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC));
			typeId = cursor.getInt(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_TYPE));
			hm = new HashMap<String, Object>();
			hm.put(ExternalDbAdapter.KEY_ROWID, id);
			hm.put(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC, shortDesc);
    		switch (typeId)
    		{
    			case 1:
    				type1.add(hm);
    			break;
    			case 2:
    				type2.add(hm);
    			break;
    			case 3:
    				type3.add(hm);
    			break;
    			case 4:
    				type4.add(hm);
    			break;
    			case 5:
    				type5.add(hm);
    			break;
    			case 6:
    				type6.add(hm);
    			break;
    			case 7:
    				type7.add(hm);
    			break;
    			case 8:
    				type8.add(hm);
    			default:
    				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No such holydays type (error in holidays table)");
    			break;
    		}
    		cursor.moveToNext();
    	}
    	cursor.close();
    	//Заполняем ListView'ы
    	String[] from = {ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC};
    	int[] to = {R.id.holidays_list_short_desc};
    	SimpleAdapterWithCustomId adapter;
    	boolean isHolidaysToday = false;
    	if (type1.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType1.getVisibility() == View.GONE) mHolidaysLayoutType1.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type1, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID); 
    		mHolidaysListType1.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType1.getVisibility() == View.VISIBLE) mHolidaysLayoutType1.setVisibility(View.GONE);    		
    	}
    	if (type2.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType2.getVisibility() == View.GONE) mHolidaysLayoutType2.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type2, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType2.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType2.getVisibility() == View.VISIBLE) mHolidaysLayoutType2.setVisibility(View.GONE);
    	}
    	if (type3.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType3.getVisibility() == View.GONE) mHolidaysLayoutType3.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type3, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType3.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType3.getVisibility() == View.VISIBLE) mHolidaysLayoutType3.setVisibility(View.GONE);
    	}
    	if (type4.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType4.getVisibility() == View.GONE) mHolidaysLayoutType4.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type4, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType4.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType4.getVisibility() == View.VISIBLE) mHolidaysLayoutType4.setVisibility(View.GONE);
    	}
    	if (type5.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType5.getVisibility() == View.GONE) mHolidaysLayoutType5.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type5, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType5.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType5.getVisibility() == View.VISIBLE) mHolidaysLayoutType5.setVisibility(View.GONE);
    	}
    	if (type6.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType6.getVisibility() == View.GONE) mHolidaysLayoutType6.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type6, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType6.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType6.getVisibility() == View.VISIBLE) mHolidaysLayoutType6.setVisibility(View.GONE);
    	}
    	if (type7.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType7.getVisibility() == View.GONE) mHolidaysLayoutType7.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type7, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType7.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType7.getVisibility() == View.VISIBLE) mHolidaysLayoutType7.setVisibility(View.GONE);
    	}
    	if (type8.size() > 0)
    	{
    		//Создаём SimpleAdapter, в который передаём созданный ArrayList. При этом переопределяем getItemId, чтобы установить элементу ListView row Id из базы данных.
    		if (mHolidaysLayoutType8.getVisibility() == View.GONE) mHolidaysLayoutType8.setVisibility(View.VISIBLE);
    		adapter = new SimpleAdapterWithCustomId(this, type8, R.layout.holidays_list_row, from, to, ExternalDbAdapter.KEY_ROWID);
    		mHolidaysListType8.setAdapter(adapter);
    		isHolidaysToday = true;
    	}
    	else
    	{
    		if (mHolidaysLayoutType8.getVisibility() == View.VISIBLE) mHolidaysLayoutType8.setVisibility(View.GONE);
    	}
    	if (isHolidaysToday)
    	{
    		mHolidaysTitleText.setText(getString(R.string.have_holidays_today_title_text));
    	}
    	else
    	{
    		mHolidaysTitleText.setText(getString(R.string.no_holidays_today_title_text));
    	}
    }
    private void registerHistoryListListener()
    {
    	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() 
    	{
    		public void onItemClick (AdapterView<?> parent, View view, int position, long id)
    		{
    			Cursor cursor = mExternalDbAdapter.getHistoryById(id);
    			if (cursor != null) cursor.moveToFirst();
    			int year = -1;
    			String FullDesc = "";
    			String ShortDesc = "";
    			if (cursor.getCount() == 1)
    			{
    				FullDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HISTORY_KEY_FULL_DESC));
    				ShortDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HISTORY_KEY_SHORT_DESC));
    				year = cursor.getInt(cursor.getColumnIndex(ExternalDbAdapter.HISTORY_KEY_YEAR));
    			}
    			else
    			{
    				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No history with such id or it more then one.");
    			}
    			Intent i = new Intent(getApplicationContext(), HistoryFullInfoActivity.class);
    			i.putExtra(ExternalDbAdapter.HISTORY_KEY_SHORT_DESC, ShortDesc);
    			i.putExtra(ExternalDbAdapter.HISTORY_KEY_FULL_DESC, FullDesc);
    			i.putExtra(ExternalDbAdapter.HISTORY_KEY_YEAR, year);
    			cursor.close();
    			startActivity(i);
    		}
		};
		mHistoryListView.setOnItemClickListener(itemClickListener);
    }
    private void registerRemindersListener()
    {
    	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener()
    	{
    		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    		{
    			// TODO Auto-generated method stub
    	    	Intent i = new Intent(getApplicationContext(), ReminderEditActivity.class);
    	    	i.putExtra(RemindersDbAdapter.KEY_ROWID, id);  	
    	    	startActivityForResult(i, ACTIVITY_EDIT);
    		}
    	};
    	mRemindersList.setOnItemClickListener(itemClickListener);
    }
    private void registerHolidaysListsListeners()
    {
    	AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener()
    	{
    		public void onItemClick (AdapterView<?> parent, View view, int position, long id)
    		{    			
    			Cursor cursor = mExternalDbAdapter.getHolidayById(id);
    			if (cursor != null) cursor.moveToFirst();
    			String FullDesc = "";
    			String ShortDesc = "";
    			int typeId = -1;
    			if (cursor.getCount() == 1)
    			{
    				FullDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_FULL_DESC));
    				ShortDesc = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC));
    				typeId = cursor.getInt(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_TYPE));
    			}
    			else
    			{
    				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "No holidays with such id or it more then one.");
    			}
    			Intent i = new Intent(getApplicationContext(), HolidaysFullInfoActivity.class);
    			i.putExtra(ExternalDbAdapter.HOLIDAYS_KEY_SHORT_DESC, ShortDesc);
    			i.putExtra(ExternalDbAdapter.HOLIDAYS_KEY_FULL_DESC, FullDesc);
    			i.putExtra(ExternalDbAdapter.HOLIDAYS_KEY_TYPE, typeId);
    			cursor.close();
    			startActivity(i);
    		}
    	};
    	mHolidaysListType1.setOnItemClickListener(itemClickListener);
    	mHolidaysListType2.setOnItemClickListener(itemClickListener);
    	mHolidaysListType3.setOnItemClickListener(itemClickListener);
    	mHolidaysListType4.setOnItemClickListener(itemClickListener);
    	mHolidaysListType5.setOnItemClickListener(itemClickListener);
    	mHolidaysListType6.setOnItemClickListener(itemClickListener);
    	mHolidaysListType7.setOnItemClickListener(itemClickListener);
    	mHolidaysListType8.setOnItemClickListener(itemClickListener);
    }
    private void registerDateAndTimeButtonListeners()
    {
    	View.OnClickListener l = new View.OnClickListener()
    	{			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent i = new Intent(getApplicationContext(), SetDateActivity.class);
				i.putExtra(PublicConstantsAndMethods.CURRENT_SCREEN_ID_KEY, mCurrentScreenId);
				Bundle dateTime = new Bundle();
				PublicConstantsAndMethods.setCalendarToBundle(mDateTime, dateTime);
				i.putExtra(PublicConstantsAndMethods.DATE_AND_TIME_CALENDAR_KEY, dateTime);
				startActivityForResult(i, ACTIVITY_SET_DATE_AND_TIME_DIALOG);
			}
    	};	
    	mSetDateTimeBt.setOnClickListener(l);
    }
    private void registerLocationListener()
    {    	
    	mLocationListener = new LocationListener()
    	{
    		public void onLocationChanged(Location l)
    		{
    			Log.d("MyLogD", "onLocationChanged");
    			mLocation = l;
    			//Обновляем координаты на экране только когда база данных открыта, т.е. наше активити активно, т.к. при деактивации в методе onPause база данных закрывается и обновить экран не получится. Если БД закрыта, то просто сохраняем текущие координаты.
    			if (mIsDbsOpen) acquireCoordinates(LOCATION_AUTO);
    		}
		    public void onStatusChanged(String provider, int status, Bundle extras)
		    {
		    	if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && !mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) stopLocation();
		    }
		    public void onProviderEnabled(String provider) {}
		    public void onProviderDisabled(String provider) {}
    	};
    }
    private void startLocation()
    {
    	if (!mLocationAlreadyStarted) //Чтобы не запускать поиск координат, когда они уже были ранее запущены и не выключены.
		{
    		//Для андроидов версии 6.0 и старше нужно убедиться, что у нас есть разрешение на получение местоположения по GPS.
    		PublicConstantsAndMethods.mLocationDenied = Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    		mLongitude = PublicConstantsAndMethods.INVALID_COORDINATES;
			mLatitude = PublicConstantsAndMethods.INVALID_COORDINATES;
	    	WidgetWorkService.mIsLocationSearching = true;
			WidgetWorkService.mLongitude = mLongitude;
			WidgetWorkService.mLatitude = mLatitude;
			updateWidget();
			mLocationTime = PublicConstantsAndMethods.INVALID_LOCATION_TIME;
			mLocationAccuracy = PublicConstantsAndMethods.INVALID_LOCATION_ACCURACY;
			mLocationProvider = "";
			mLocation = null;
			//Убираем данные по Солнцу и Луне и пишем, что координаты ещё не получены.
			showSunData();
			showMoonData();
			if (PublicConstantsAndMethods.mLocationDenied)
			{
				if (mShouldShowRequestPermissionDialog) ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
			}
			else
			{
				//Обязательно нужно проверять доступен ли провайдер координат, т.к. на андроидах старше 4.2 вылетает ошибка если попытаться получать координаты с недоступного провайдера.
		    	if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
		    	if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);
		    	mLocationAlreadyStarted = true;
			}
		}
    }
	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
	{
	    switch (requestCode)
	    {
	        case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION:
	        {
	        	//startLocation запустится само после onResume, в этом методе его запускать не надо.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
				{}
				else
				{
					Toast.makeText(this, getResources().getString(R.string.location_permission_explanation), Toast.LENGTH_LONG).show();
					mShouldShowRequestPermissionDialog = false;
				}
				return;
	        }
	    }
	}
    private void stopLocation()
    {
    	mLocationManager.removeUpdates(mLocationListener);
    	mLocationAlreadyStarted = false;
    }
    private void registerSizeChangeListeners()
    {
    	GetSizeLinearLayout.OnSizeChangedListener l = new GetSizeLinearLayout.OnSizeChangedListener()
    	{			
			public void onSizeChanged(int viewId, int w, int h, int oldw, int oldh)
			{
				// TODO Auto-generated method stub				
				float densityFactor = mDisplayMetrics.ydpi / 160f;
				//ОБЩИЕ РАЗМЕРЫ ДЛЯ ОКОН СОЛНЦА И ЛУНЫ:
				//Добавка, которую надо вычесть от определённого размера экрана. Определяется экспериментально.
				int add = (int)(8f * densityFactor);
				//Определяем высоту разделителя:
		    	int dividerHeight = (int)(getResources().getDimension(R.dimen.DividerHeight) * densityFactor);
				switch (viewId)
				{
				case R.id.sunForGettingSize:
					//Определяем высоту заголовков:
					int sunFactsTitleHeight = mSunFactsTitle.getLineCount() * mSunFactsTitle.getLineHeight();
					int sunSignsTitleHeight = mSunSignsTitle.getLineCount() * mSunSignsTitle.getLineHeight();
					//Определеяем общую высоту для двух текстов:
					mSunVarTextHeight = h - add - sunFactsTitleHeight - sunSignsTitleHeight - dividerHeight - dividerHeight;
					mSunVarTextWidth = w;
					setTextAndShiftForUpAndDownTextViews(mSunVarTextHeight, mSunVarTextWidth, mSunFactsPlainText, mSunSignsPlainText, mSunFactsText, mSunSignsText);
				case R.id.moonForGettingSize:
					//Определяем высоту заголовков:
					int moonFactsTitleHeight = mMoonFactsTitle.getLineCount() * mMoonFactsTitle.getLineHeight();
					int moonCalendarTitleHeight = mMoonCalendarTitle.getLineCount() * mMoonCalendarTitle.getLineHeight();
					mMoonVarTextHeight = h - add - moonFactsTitleHeight - moonCalendarTitleHeight - dividerHeight - dividerHeight;
					mMoonVarTextWidth = w;
					setTextAndShiftForUpAndDownTextViews(mMoonVarTextHeight, mMoonVarTextWidth, mMoonFactsPlainText, mMoonCalendarPlainText, mMoonFactsText, mMoonCalendarText);					
				}
			}
		};
		mSunForGettingSize.setOnSizeChangedListener(l);
		mMoonForGettingSize.setOnSizeChangedListener(l);
    }
    //Компоновка размеров текстовых элементов в зависимости от количества текста в них.
    private void setTextAndShiftForUpAndDownTextViews(int height, int width, String textUp, String textDown, EllipsizingTextView textViewUp, EllipsizingTextView textViewDown)
    {
		//Если известен и текст и размер, то только в этом случае выполняем компоновку текста
    	//Как-то всплыл баг, что textUp и textDown были равны null. Сейчас я его исправил, но проверку оставлю на всяуий случай.
    	if (textUp == null || textDown == null) return;
		if ((height > 0) && !(textUp.equals(WRONG_TEXT) || textDown.equals(WRONG_TEXT)))
		{
			//Для теста
			//textDown = "какой-то текст для теста";
			//textUp = "какой-то текст для теста";			
			//Определяем количество полных строк в каждом тексте.
			float[] lineSpacing;
			lineSpacing = textViewUp.getLineSpacing();
			//Задаём эквиваленты нашим текстовым View, для получения всех параметров текста, т.к. эти параметры ещё не доступны с реальных текст View, которые используются на экране.
			StaticLayout textUpStaticLayout = new StaticLayout(textUp, textViewUp.getPaint(), width - textViewUp.getPaddingLeft() - textViewUp.getPaddingRight(), Alignment.ALIGN_NORMAL, lineSpacing[0], lineSpacing[1], false);
			lineSpacing = textViewDown.getLineSpacing();
			StaticLayout textDownStaticLayout = new StaticLayout(textDown, textViewDown.getPaint(), width - textViewDown.getPaddingLeft() - textViewDown.getPaddingRight(), Alignment.ALIGN_NORMAL, lineSpacing[0], lineSpacing[1], false);
			//Количество строк
			int textUpLineCount = textUpStaticLayout.getLineCount();
			int textDownLineCount = textDownStaticLayout.getLineCount();
			//Фактический размер полного текста в пикселях
			int textUpHeight = textUpStaticLayout.getLineBottom(textUpLineCount - 1);			
			int textDownHeight = textDownStaticLayout.getLineBottom(textDownLineCount - 1);
			//Средняя высота строки
			int textUpLineHeight = textUpHeight / textUpLineCount;
			int textDownLineHeight = textDownHeight / textDownLineCount;
			//Определеяем размер верхнего текста в пикселях, который будет непосредственно отображён на экране
			int textUpHeightScreen = 0;
			int height34 = 3 * height / 4;
			int height12 = height / 2;
			int height14 = height / 4;
			if (textUpHeight >= height && textDownHeight >= height)
			{
				textUpHeightScreen = height12;
			}
			else if (textUpHeight >= height && textDownHeight < height)
			{
				if (textDownHeight >= height34)
				{
					textUpHeightScreen = height12;
				}
				else
				{
					textUpHeightScreen = height - textDownHeight;
					if (textUpHeightScreen > height34) textUpHeightScreen = height34;
				}
			}
			else if (textUpHeight < height && textDownHeight >= height)
			{
				if (textUpHeight >= height34)
				{
					textUpHeightScreen = height12;
				}
				else
				{
					if (textUpHeight < height14)
					{
						textUpHeightScreen = height14;
					}
					else
					{
						textUpHeightScreen = textUpHeight;
					}
				}
			}
			else if (textUpHeight < height && textDownHeight < height)
			{
				if ((textUpHeight + textDownHeight) > height)
				{
					if (textUpHeight < textDownHeight)
					{
						if (textUpHeight < height12)
						{
							textUpHeightScreen = textUpHeight;
						}
						else
						{
							textUpHeightScreen = height12;
						}
					}
					else if (textUpHeight > textDownHeight)
					{
						textUpHeightScreen = height - textDownHeight;
						if (textUpHeightScreen < height12) textUpHeightScreen = height12;
					}
					else
					{
						textUpHeightScreen = height12;
					}
				}
				else
				{
					textUpHeightScreen = textUpHeight;
				}
			}
			textViewUp.setHeight(textUpHeightScreen);
			textViewUp.setMaxLines(textUpHeightScreen / textUpLineHeight);
			textViewDown.setMaxLines((height - textUpHeightScreen) / textDownLineHeight);
			textViewUp.setText(textUp);
			textViewDown.setText(textDown);
		}
    }
    //Запускаем этот метот, если меняется только время
    private void updateScreensForNewTime()
    {
    	//При старте Activity методы showSunData() и showMoonData() вызываются внутри getCoordinates, поэтому здесь они вызываться уже не должны.
    	//Переменная mIsFirstStart сбрасывается в false внутри onResume()
    	if (!mIsAfterOnCreate)
    	{
    		showSunData();
    		showMoonData();
    	}
    	//Обновляем дату и время на синем заголовке вверху.
		SimpleDateFormat timeFormat = new SimpleDateFormat(PublicConstantsAndMethods.FULL_DATE_WITHOUT_SECONDS_FORMAT, Locale.getDefault());
		String dateTime = timeFormat.format(mDateTime.getTime());
		String dayName = PublicConstantsAndMethods.getDayOfWeek(mDateTime, getResources().getStringArray(R.array.days_of_week));
		String dateStatus = " (";
		switch (mNewDateStatus)
		{
		case CURRENT_DATE:
			dateStatus += getString(R.string.current_date);
			break;
		case PAST_DATE:
			dateStatus += getString(R.string.past_date);
			break;
		case FUTUTRE_DATE:
			dateStatus += getString(R.string.future_date);
			break;
		}
		dateStatus += ")";
		mShowSelectedDayText.setText(dateTime + ", " + dayName + dateStatus);
    }
    //Запускаем этот метод, если меняется дата.
    private void updateScreensForNewDate()
    {
    	updateScreensForNewTime();
    	fillReminders();
		fillHolidays();
		fillHistory();
    }
	private void registerSunAndMoonInfoTextListener()
	{
		View.OnClickListener onClickListener = new View.OnClickListener()
		{			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				Intent i;
				Bundle extras;
				switch (v.getId())
				{
					case R.id.sun_astro_info:
						i = new Intent(getApplicationContext(), SunAstroInfoActivity.class);
						extras = new Bundle();
						extras.putDouble(SunAstroInfoActivity.LATITUDE_KEY, mLatitude);
						extras.putDouble(SunAstroInfoActivity.LONGITUDE_KEY, mLongitude);
						PublicConstantsAndMethods.setCalendarToBundle(mDateTime, extras);
						i.putExtras(extras);
						startActivity(i);
					break;
					case R.id.sun_facts_info:
						i = new Intent(getApplicationContext(), SunFactsActivity.class);
						i.putExtra(SunFactsActivity.SUN_FACTS_ID_KEY, mCurrentSunFactId);
						startActivity(i);
					break;
					case R.id.signs_info:
						i = new Intent(getApplicationContext(), SignsActivity.class);
						extras = new Bundle();
						PublicConstantsAndMethods.setCalendarToBundle(mDateTime, extras);
						i.putExtras(extras);
						startActivity(i);
					break;
					case R.id.moon_facts_info:
						i = new Intent(getApplicationContext(), MoonFactsActivity.class);
						i.putExtra(MoonFactsActivity.MOON_FACTS_ID_KEY, mCurrentMoonFactId);
						startActivity(i);
					break;
					case R.id.moon_astro_info:
						i = new Intent(getApplicationContext(), MoonAstroInfoActivity.class);
						extras = new Bundle();
						extras.putDouble(MoonAstroInfoActivity.LATITUDE_KEY, mLatitude);
						extras.putDouble(MoonAstroInfoActivity.LONGITUDE_KEY, mLongitude);
						PublicConstantsAndMethods.setCalendarToBundle(mDateTime, extras);
						i.putExtras(extras);
						startActivity(i);
					break;
					case R.id.moon_calendar_info:
						i = new Intent(getApplicationContext(), MoonCalendarActivity.class);
						extras = new Bundle();
						PublicConstantsAndMethods.setCalendarToBundle(mDateTime, extras);
						i.putExtras(extras);
						startActivity(i);
					break;
				}
			}
		};
		mSunAstroInfoText.setOnClickListener(onClickListener);
		mMoonAstroInfoText.setOnClickListener(onClickListener);
		mSunFactsText.setOnClickListener(onClickListener);
		mSunSignsText.setOnClickListener(onClickListener);
		mMoonFactsText.setOnClickListener(onClickListener);
		mMoonCalendarText.setOnClickListener(onClickListener);
	}
	private void registerScreenSwitchListener()
	{
		SlidingView.OnScreenSwitchListener ScreenSwitchListener = new SlidingView.OnScreenSwitchListener()
		{			
			public void onScreenSwitched(int screen)
			{
				// TODO Auto-generated method stub
				mCurrentScreenId = mMainSlidingView.getCurrentScreen();
				//Обновляем элементы меню. Т.к. теперь у нас action bar, то onPrepareOptionMenu нужно вызывать принудительно через supportInvalidateOptionsMenu().
				supportInvalidateOptionsMenu();
				//В выпадающем списке Action bar'а меняем заголовок.
				mActionBar.setSelectedNavigationItem(mCurrentScreenId);
			}
		};
		mMainSlidingView.setOnScreenSwitchListener(ScreenSwitchListener);
	}
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case CONFIRM_DELETE_ALL_REMINDERS_DIALOG:
				return showConfirmDialog();
			default:
				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in onCreateDialog");
			break;
		}
		return super.onCreateDialog(id);
	}
	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		switch(id)
		{
			case CONFIRM_DELETE_ALL_REMINDERS_DIALOG:
				AlertDialog alertDialog = (AlertDialog)dialog;
				if (mShowAllRemindersState)
				{
					alertDialog.setMessage(getString(R.string.confirm_dialog_delete_all_reminders_message));
				}
				else
				{
					alertDialog.setMessage(getString(R.string.confirm_dialog_delete_today_reminders_message));
				}
			break;
			default:
				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in onPrepareDialog");
			break;
		}
	}
	private AlertDialog showConfirmDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.confirm_dialog_reminders_title));
		if (mShowAllRemindersState)
		{
			builder.setMessage(getString(R.string.confirm_dialog_delete_all_reminders_message));
		}
		else
		{
			builder.setMessage(getString(R.string.confirm_dialog_delete_today_reminders_message));
		}		
		builder.setCancelable(false);
		builder.setPositiveButton(getString(R.string.confirm_dialog_positive_button_text), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
			{							
				dialog.cancel();
				long[] AllId;
				if (mShowAllRemindersState)
				{
					AllId = mRemindersDbAdapter.deleteAllReminders();
				}
				else
				{
					AllId = mRemindersDbAdapter.deleteTodayReminders(mDateTime);
				}
				RemindersAlarmManager ram = new RemindersAlarmManager(getApplicationContext());
				for (int i = 0; i < AllId.length; i++)
				{
					ram.cancelAlarm(AllId[i]);
					mgr.cancel((int)AllId[i]);
				}
				fillReminders();
				//fillCalendarGrid();
			}
		});
		builder.setNegativeButton(getString(R.string.confirm_dialog_negative_button_text), new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int id)
				{
					dialog.cancel();
				}
		});
		return builder.create();
	}
	//Метод, предназначенный для отображения информации на странице Солнца
    private void showSunData()
    {    	
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PublicConstantsAndMethods.ONLY_TIME_WITHOUT_SECONDS_FORMAT, Locale.getDefault());
    	String textToOut = "";
    	//Пишем астрономическую информацию, если координаты уже получены
    	if (PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
    	{
    		//Пишем под горизонтом или над горизонтом.
    		if (AstroCalcModules.GetObjectHeightOrSouthAzimuth(mDateTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude, true) < 0)
    		{
    			textToOut += getString(R.string.astro_sun_under_horizon);
    		}
    		else
    		{
    			textToOut += getString(R.string.astro_sun_above_horizon);
    		}
    		textToOut += "\n";
    		//Пишем время восхода Солнца.		
			Calendar TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(mDateTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				textToOut += getString(R.string.astro_sun_rise_time) + " " + simpleDateFormat.format(TmpCal.getTime());
			}
			else
			{
				textToOut += getString(R.string.astro_no_sun_rise);
			}
			textToOut += "\n";
			//Пишем время зенита Солнца.		
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(mDateTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_TRANSIT, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				textToOut += getString(R.string.astro_sun_transit_time) + " " + simpleDateFormat.format(TmpCal.getTime());
			}
			else
			{
				textToOut += getString(R.string.astro_sun_no_transit);
			}
			textToOut += "\n";
	        //Пишем время захода Солнца.
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(mDateTime, AstroCalcModules.OBJECT_SUN, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				textToOut += getString(R.string.astro_sun_set_time) + " " + simpleDateFormat.format(TmpCal.getTime());
			}
			else
			{
				textToOut += getString(R.string.astro_no_sun_set);
			}
			textToOut += "\n";
	        //Пишем длительность светового дня.		
			long LengthOfDaylightInMillis = AstroCalcModules.GetLengthOfDaylightInMillis(mDateTime, AstroCalcModules.OBJECT_SUN, mLongitude, mLatitude);
			if (LengthOfDaylightInMillis == Long.MAX_VALUE)
			{
				textToOut += getString(R.string.astro_no_day_lenth);
			}
			else
			{
				DecimalFormat decFormat = new DecimalFormat("00");
				LengthOfDaylightInMillis = Math.abs(LengthOfDaylightInMillis);
				int[] LengthOfDaylight = AstroCalcModules.ConvertMillisToHHMMSSsss(LengthOfDaylightInMillis);		
				textToOut += getString(R.string.astro_day_lenth) + " " + decFormat.format(LengthOfDaylight[0]) + "ч " + decFormat.format(LengthOfDaylight[1]) + "м";
			}
			mSunAstroInfoText.setText(textToOut);
    	}
    	else
    	{
			if (PublicConstantsAndMethods.mLocationDenied)
			{
				mSunAstroInfoText.setText(getString(R.string.location_permission_denied));
			}
			else
			{
				mSunAstroInfoText.setText(getString(R.string.no_available_coordinates));
			}
    	}
		//ПИШЕМ ФАКТЫ О СОЛНЦЕ
		//Если текущий день не равен дню, когда в последний раз обновляли факт, то обновляем факт о Солнце.		
		if (mCurrentDateTime.get(Calendar.DAY_OF_MONTH) != mSunFactLastUpdateTime[0] || mCurrentDateTime.get(Calendar.MONTH) != mSunFactLastUpdateTime[1] || mCurrentDateTime.get(Calendar.YEAR) != mSunFactLastUpdateTime[2])
		{
			mSunFactLastUpdateTime[0] = mCurrentDateTime.get(Calendar.DAY_OF_MONTH);
			mSunFactLastUpdateTime[1] = mCurrentDateTime.get(Calendar.MONTH);
			mSunFactLastUpdateTime[2] = mCurrentDateTime.get(Calendar.YEAR);
			long newSunFactId = mCurrentSunFactId;
			while (newSunFactId == mCurrentSunFactId) newSunFactId = PublicConstantsAndMethods.getRandomNumber(1, mExternalDbAdapter.getMaximumSunFacts());
			mCurrentSunFactId = newSunFactId;		
		}
		textToOut = mExternalDbAdapter.getSunFactById(mCurrentSunFactId);
		mSunFactsPlainText = textToOut;    	
		//ПИШЕМ ПРИМЕТЫ
		textToOut = "";
		Cursor cursor = mExternalDbAdapter.getSignsForCurrentDay(mDateTime);
		if (cursor != null && cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			textToOut = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.SIGNS_KEY_DATA));
			cursor.close();
		}
		mSunSignsPlainText = textToOut;
		setTextAndShiftForUpAndDownTextViews(mSunVarTextHeight, mSunVarTextWidth, mSunFactsPlainText, mSunSignsPlainText, mSunFactsText, mSunSignsText);
    }
	//Метод, предназначенный для отображения информации на странице Луны    
    private void showMoonData()
    {
    	SimpleDateFormat simpleDateFormat = new SimpleDateFormat(PublicConstantsAndMethods.ONLY_TIME_WITHOUT_SECONDS_FORMAT, Locale.getDefault());
    	String textToOut = "";
    	DecimalFormat decFormat;
    	//Пишем астрономическую информацию, если координаты уже получены
    	if (PublicConstantsAndMethods.IsLocationValid(mLongitude, mLatitude))
    	{
    		//Пишем под горизонтом или над горизонтом.
    		if (AstroCalcModules.GetObjectHeightOrSouthAzimuth(mDateTime, AstroCalcModules.OBJECT_MOON, mLongitude, mLatitude, true) < 0)
    		{
    			textToOut += getString(R.string.astro_moon_under_horizon);
    		}
    		else
    		{
    			textToOut += getString(R.string.astro_moon_above_horizon);
    		}
    		textToOut += "\n";
    		//Пишем фазу Луны.
	    	int moonPhaseNumber = AstroCalcModules.GetMoonPhasePreserve(mDateTime);
	    	String moonPhaseName = getResources().getStringArray(R.array.moon_phases)[moonPhaseNumber - 1];
			textToOut += getString(R.string.astro_moon_phase) + " " + moonPhaseName;
			textToOut += "\n";
	    	//Пишем процент видимости Луны.
	    	decFormat = new DecimalFormat("##.##");
	   		textToOut += getString(R.string.astro_moon_visibility) + " " + decFormat.format(AstroCalcModules.GetMoonVisiblePercents(mDateTime)) + "%";
	   		textToOut += "\n";
	        //Пишем время восхода Луны.		
			Calendar TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(mDateTime, AstroCalcModules.OBJECT_MOON, AstroCalcModules.EVENT_RISE, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				textToOut += getString(R.string.astro_moon_rise_time) + " " + simpleDateFormat.format(TmpCal.getTime());
			}
			else
			{
				textToOut += getString(R.string.astro_no_moon_rise);
			}
			textToOut += "\n";
	        //Пишем время захода Луны.
			TmpCal = AstroCalcModules.GetRisingTransitOrSettingPreserve(mDateTime, AstroCalcModules.OBJECT_MOON, AstroCalcModules.EVENT_SETTING, mLongitude, mLatitude);
			if (TmpCal != null)
			{
				textToOut += getString(R.string.astro_moon_set_time) + " " + simpleDateFormat.format(TmpCal.getTime());
			}
			else
			{
				textToOut += getString(R.string.astro_no_moon_set);
			}
	    	mMoonAstroInfoText.setText(textToOut);
    	}
    	else
    	{
			if (PublicConstantsAndMethods.mLocationDenied)
			{
				mMoonAstroInfoText.setText(getString(R.string.location_permission_denied));
			}
			else
			{
				mMoonAstroInfoText.setText(getString(R.string.no_available_coordinates));
			}
    	}
		//Если текущий день не равен дню, когда в последний раз обновляли факт, то обновляем факт о Луне.		
		if (mCurrentDateTime.get(Calendar.DAY_OF_MONTH) != mMoonFactLastUpdateTime[0] || mCurrentDateTime.get(Calendar.MONTH) != mMoonFactLastUpdateTime[1] || mCurrentDateTime.get(Calendar.YEAR) != mMoonFactLastUpdateTime[2])
		{
			mMoonFactLastUpdateTime[0] = mCurrentDateTime.get(Calendar.DAY_OF_MONTH);
			mMoonFactLastUpdateTime[1] = mCurrentDateTime.get(Calendar.MONTH);
			mMoonFactLastUpdateTime[2] = mCurrentDateTime.get(Calendar.YEAR);
			long newMoonFactId = mCurrentMoonFactId;
			while (newMoonFactId == mCurrentMoonFactId) newMoonFactId = PublicConstantsAndMethods.getRandomNumber(1, mExternalDbAdapter.getMaximumMoonFacts());
			mCurrentMoonFactId = newMoonFactId;		
		}
		textToOut = mExternalDbAdapter.getMoonFactById(mCurrentMoonFactId);
		mMoonFactsPlainText = textToOut;
    	textToOut = getMoonCalendarInfo(); //Используем отдельный метод, т.к. информация, которую мы будем получать может меняться в зависимости от версии программы. И быть достаточно сложной в получении.
    	mMoonCalendarPlainText = textToOut;
    	setTextAndShiftForUpAndDownTextViews(mMoonVarTextHeight, mMoonVarTextWidth, mMoonFactsPlainText, mMoonCalendarPlainText, mMoonFactsText, mMoonCalendarText);
    }
    //Делаем отдельный метод, т.к. информация, которую мы будем получать может меняться в зависимости от версии программы. И быть достаточно сложной в получении.
    private String getMoonCalendarInfo()
    {
		//ПИШЕМ ЛУННЫЙ КАЛЕНДАРЬ
		/*
		 * Пока у нас ничего не придумано, что давать в информации по лунному календарю, поэтому не будем ничего писать.
		 * Закоментированный код берёт информацию из базы данных. Но в БД пока ничего нет. Есть какие-то приметы на 2012 год и всё.
		Cursor cursor = mExternalDbAdapter.getMoonCalendarForCurrentDay(mDateTime);
		String textToOut = "";
		if (cursor != null && cursor.getCount() > 0)
		{
			cursor.moveToFirst();
			textToOut = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.MOON_CALENDAR_KEY_DATA));			
		}
		else
		{
			textToOut = getString(R.string.no_moon_calendar_text);
		}
		cursor.close();
		return textToOut;
		 */
    	return "";
    }
}