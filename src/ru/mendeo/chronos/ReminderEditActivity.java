package ru.mendeo.chronos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.app.Dialog;
import android.app.DatePickerDialog;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.EditText;
import android.database.Cursor;

//import android.content.SharedPreferences;
//import android.preference.PreferenceManager;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

@SuppressWarnings("deprecation")
public class ReminderEditActivity extends Activity
{
	private static final int DATE_PICKER_DIALOG = 0;
	private static final int TIME_PICKER_DIALOG = 1;
	private static final String DATE_FORMAT = "dd.MM.yyyy";
	private static final String TIME_FORMAT = "HH:mm";
	private static final int NO_ROW_ID = -10;
	private long mRowId;
	private Button mDateButton, mTimeButton;
	private ImageView mSetCurrentTimeButton, mSetCurrentDateButton, mSaveButton;
	private EditText mTitleText;
	private EditText mBodyText;
	private View.OnClickListener mButtonListener;
	private Calendar mCalendar;
	private RemindersDbAdapter mDbHelper;
	private NotificationManager mgr;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reminders_edit);
        mgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mDbHelper = new RemindersDbAdapter(this);
        mDateButton = (Button)findViewById(R.id.reminder_edit_date);
        mTimeButton = (Button)findViewById(R.id.reminder_edit_time);
        mSaveButton = (ImageView)findViewById(R.id.save_edit_reminder);
        mSetCurrentTimeButton = (ImageView)findViewById(R.id.reminders_updateTimeBt);
        mSetCurrentDateButton = (ImageView)findViewById(R.id.reminders_updateDateBt);
        mTitleText = (EditText)findViewById(R.id.reminder_edit_title);
        mBodyText = (EditText)findViewById(R.id.reminder_edit_body);
        if (savedInstanceState != null)
        {
        	mRowId = savedInstanceState.getLong(RemindersDbAdapter.KEY_ROWID, NO_ROW_ID);
        	mCalendar = PublicConstantsAndMethods.getCalendarFromBundle(savedInstanceState);
        }
        else
        {
        	mRowId = NO_ROW_ID;
        }
        registerButtonListeners();
    }
    @Override
    protected void onPause()
    {
    	super.onPause();
    	mDbHelper.close();
    }
    @Override
    protected void onResume()
    {
    	super.onResume();
    	mDbHelper.open();
    	setRowIdAndCalendarFromIntent();
    	populateFields();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
    	super.onSaveInstanceState(outState);
    	PublicConstantsAndMethods.setCalendarToBundle(mCalendar, outState);
    	outState.putLong(RemindersDbAdapter.KEY_ROWID, mRowId);
    }
    private void setRowIdAndCalendarFromIntent()
    {
    	if (mRowId == NO_ROW_ID)
    	{
    		Bundle extras = getIntent().getExtras();
    		if (extras != null)
    		{
    			mRowId = extras.getLong(RemindersDbAdapter.KEY_ROWID, NO_ROW_ID);
    			if (mRowId != NO_ROW_ID)
    			{
    				int id = (int)(mRowId);
    				mgr.cancel(id);
    			}
    			mCalendar = PublicConstantsAndMethods.getCalendarFromBundle(extras);
    		}
    	}
    }
    private void SetDateToCalendar(Calendar cal, int year, int month, int day, int hour, int minute, int second, int millisecond)
    {
    	cal.set(Calendar.YEAR, year);
    	cal.set(Calendar.MONTH, month);
    	cal.set(Calendar.DAY_OF_MONTH, day);
    	cal.set(Calendar.HOUR_OF_DAY, hour);
    	cal.set(Calendar.MINUTE, minute);
    	cal.set(Calendar.SECOND, second);
    	cal.set(Calendar.MILLISECOND, millisecond);
    }
    private void populateFields()
    {
    	if (mRowId != NO_ROW_ID)
    	{
    		Cursor reminder = mDbHelper.fetchReminder(mRowId);    		
    		startManagingCursor(reminder);
    		reminder.moveToFirst();
    		mTitleText.setText(reminder.getString(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_TITLE)));
    		mBodyText.setText(reminder.getString(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_BODY)));
    		int year = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_YEAR));
    		int month = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MONTH));
    		int day = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DAY));
    		int hour = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_HOUR));
    		int minute = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MINUTE));
    		int second = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_SECOND));
    		int millisecond = reminder.getInt(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_MILLISECOND));
    		if (mCalendar == null) mCalendar = Calendar.getInstance();
    		SetDateToCalendar(mCalendar, year, month, day, hour, minute, second, millisecond);
    	}
    	else
    	{
    		/* Получение данных из настроек
    		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
	    	String defaultTitleKey = getString(R.string.pref_task_title_key);
	    	String defaultTitle = prefs.getString(defaultTitleKey, "");
	    	if("".equals(defaultTitle) == false) mTitleText.setText(defaultTitle);
	    	*/ 
    		mTitleText.setText("");
    		mBodyText.setText("");
    	}
    	updateDateButtonText();
    	updateTimeButtonText();
    }
	private void registerButtonListeners()
	{
		// TODO Auto-generated method stub
        mButtonListener = new View.OnClickListener()
        {			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				switch (v.getId())
				{
					case R.id.reminder_edit_date:
						showDialog(DATE_PICKER_DIALOG);
					break;
					case R.id.reminder_edit_time:
						showDialog(TIME_PICKER_DIALOG);
					break;
					case R.id.save_edit_reminder:
						saveState();
						setResult(RESULT_OK);
						finish();
					break;
					case R.id.reminders_updateTimeBt:
						Calendar tmp1 = Calendar.getInstance();
						mCalendar.set(Calendar.HOUR_OF_DAY, tmp1.get(Calendar.HOUR_OF_DAY));
						mCalendar.set(Calendar.MINUTE, tmp1.get(Calendar.MINUTE));
						mCalendar.set(Calendar.SECOND, tmp1.get(Calendar.SECOND));
						mCalendar.set(Calendar.MILLISECOND, tmp1.get(Calendar.MILLISECOND));
				    	updateDateButtonText();
				    	updateTimeButtonText();
					break;
					case R.id.reminders_updateDateBt:
						Calendar tmp2 = Calendar.getInstance();
						mCalendar.set(Calendar.YEAR, tmp2.get(Calendar.YEAR));
						mCalendar.set(Calendar.DAY_OF_YEAR, tmp2.get(Calendar.DAY_OF_YEAR));
				    	updateDateButtonText();
				    	updateTimeButtonText();
					break;
					default:
						Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in registerButtonListenersAndSetDefaultText");
					break;
				}
			}
		};
		mDateButton.setOnClickListener(mButtonListener);
		mTimeButton.setOnClickListener(mButtonListener);
		mSaveButton.setOnClickListener(mButtonListener);
		mSetCurrentTimeButton.setOnClickListener(mButtonListener);
		mSetCurrentDateButton.setOnClickListener(mButtonListener);
	}
	//Создание диалогового окна.
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case DATE_PICKER_DIALOG:
				return showDatePicker();
			case TIME_PICKER_DIALOG:
				return showTimePicker();
			default:
				Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in onCreateDialog");
			break;
		}
		return super.onCreateDialog(id);
	}
	private DatePickerDialog showDatePicker()
	{
		DatePickerDialog datePicker = new DatePickerDialog(this,
		new DatePickerDialog.OnDateSetListener()
		{
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				mCalendar.set(Calendar.YEAR, year);
				mCalendar.set(Calendar.MONTH, monthOfYear);
				mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				updateDateButtonText();
			}
		},
		mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
		return datePicker;
	}
	private TimePickerDialog showTimePicker()
	{
		TimePickerDialog timePicker = new TimePickerDialog(this,
		new TimePickerDialog.OnTimeSetListener()
		{
			public void onTimeSet(TimePicker view, int hourOfDay, int minute)
			{
				mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mCalendar.set(Calendar.MINUTE, minute);
				mCalendar.set(Calendar.SECOND, 0);
				mCalendar.set(Calendar.MILLISECOND, 0);
				updateTimeButtonText();
			}
		},
		mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);
		return timePicker;
	}
	private void updateDateButtonText()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
		String dateForButton = dateFormat.format(mCalendar.getTime());
		mDateButton.setText(dateForButton);
	}
	private void updateTimeButtonText()
	{
		SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
		String timeForButton = timeFormat.format(mCalendar.getTime());
		mTimeButton.setText(timeForButton);
	}
	private void saveState()
	{
		String title = mTitleText.getText().toString();
		String body = mBodyText.getText().toString();
		if (mRowId == NO_ROW_ID)
		{
			long id = mDbHelper.createReminder(title, body, mCalendar);
			if (id > 0)
			{
				mRowId = id;
			}
		}
		else
		{
			mDbHelper.updateReminder(mRowId, title, body, mCalendar);
		}
		//Log.d(PublicConstants.MY_LOG_TAG, Long.toString(mRowId));
		//Создаём напоминальщика AlarmManager'а
		new RemindersAlarmManager(this).setAlarm(mRowId, mCalendar);
	}
}
