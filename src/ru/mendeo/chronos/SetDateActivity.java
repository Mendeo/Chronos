package ru.mendeo.chronos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

@SuppressWarnings("deprecation")
public class SetDateActivity extends Activity
{
	private static final int TIME_PICKER_DIALOG = 1001;
	private static final int HELP_DIALOG = 1002;
	private Button mChangeTimeBt, mSetCurrentDateTimeBt;
	private CalendarView mCalendarView;
	private boolean mDateWasChangedDueTofillCalendarGrid;
	private Calendar mDateTime, mCurrentDateTime;
	private int mCurrentScreenId;
	private Button mOkButton, mCancelButton, mHelpButton;
	private RemindersDbAdapter mRemindersDbAdapter;
	private ExternalDbAdapter mExternalDbAdapter;
	private Context mContext;
	@Override
    public void onCreate(Bundle savedInstanceState)
    {        
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.set_date_dialog);
        mContext = this.getApplicationContext();
        mOkButton = (Button)findViewById(R.id.setDateTimeDialogOkButton);
        mCancelButton = (Button)findViewById(R.id.setDateTimeDialogCancelButton);
        mChangeTimeBt = (Button)findViewById(R.id.ChangeTimeBt);
        mHelpButton = (Button)findViewById(R.id.calendarHelpButton);
        mSetCurrentDateTimeBt = (Button)findViewById(R.id.SetCurrentDateTimeBt);
        mCalendarView = (CalendarView)findViewById(R.id.calendar_view);
        mRemindersDbAdapter = new RemindersDbAdapter(this);
        mExternalDbAdapter = new ExternalDbAdapter(this);
        mCurrentScreenId = getIntent().getIntExtra(PublicConstantsAndMethods.CURRENT_SCREEN_ID_KEY, ChronosMainActivity.INVALID_SCREEN);
        mDateTime = PublicConstantsAndMethods.getCalendarFromBundle(getIntent().getBundleExtra(PublicConstantsAndMethods.DATE_AND_TIME_CALENDAR_KEY));        
        if (mCurrentScreenId == ChronosMainActivity.INVALID_SCREEN || mDateTime.get(Calendar.YEAR) < 0) 
        {
        	Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in SetDateActivity onCreate");
        	return;
        }
        openAllDatabases();
        registerButtonsListeners();
        registerCalendarViewListener();
        updateTimeButtonText();
        fillCalendarGrid();
    }
    @Override
    protected void onResume()
    {
    	super.onResume();
    	mCurrentDateTime = Calendar.getInstance();
    	//Обновляем календарную сетку, если вдруг после резюма программы оказывается, что текущий день, указанный на календарной сетке не совпадает с реальным текущим днём (например если программу свернули, затем настал новый день, то когда программу развернут календарная сетка обновит текущий день)
    	if (mDateTime.get(Calendar.MONTH) == mCurrentDateTime.get(Calendar.MONTH) && mDateTime.get(Calendar.YEAR) == mCurrentDateTime.get(Calendar.YEAR) && (mCurrentDateTime.get(Calendar.DAY_OF_MONTH) != mCalendarView.getCurrentTimeDayNumber() || mCurrentDateTime.get(Calendar.MONTH) != mCalendarView.getCurrentTimeMonth() - 1 || mCurrentDateTime.get(Calendar.YEAR) != mCalendarView.getCurrentTimeYear())) fillCalendarGrid();
    }
    @Override
    protected void onDestroy()
    {
    	super.onDestroy();
    	closeAllDatabases();
    }
    private void openAllDatabases()
    {
		//Log.d("MyLog", "Opening");
		mRemindersDbAdapter.open();
		mExternalDbAdapter.open();
    }
    private void closeAllDatabases()
    {
		//Log.d("MyLog", "Closing");
		mRemindersDbAdapter.close();
		mExternalDbAdapter.close();
    }
    //Метод, обновляющий календарную сетку в соответствии с событиями (праздниками или заметками) не текущий в mDateTime месяц.
    private void fillCalendarGrid()
    {
    	mDateWasChangedDueTofillCalendarGrid = true;
        mCalendarView.fillCalendarGrid(mDateTime.get(Calendar.YEAR), mDateTime.get(Calendar.MONTH) + 1, mDateTime.get(Calendar.DAY_OF_MONTH));
    }
    private void registerButtonsListeners()
    {
    	View.OnClickListener l = new View.OnClickListener()
    	{			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				switch (v.getId())
				{
				//Изменить время	
				case R.id.ChangeTimeBt:
					showDialog(TIME_PICKER_DIALOG);
				break;
					//Установить текущее время
				case R.id.SetCurrentDateTimeBt:
					mDateTime = Calendar.getInstance();
					updateTimeButtonText();
					fillCalendarGrid();
				break;
				case R.id.setDateTimeDialogOkButton:
					Intent i = new Intent();
			    	Bundle dateTime = new Bundle();
			    	PublicConstantsAndMethods.setCalendarToBundle(mDateTime, dateTime);
			    	i.putExtra(PublicConstantsAndMethods.DATE_AND_TIME_CALENDAR_KEY, dateTime);
			    	setResult(RESULT_OK, i);
			    	finish();
				break;
				case R.id.setDateTimeDialogCancelButton:
					setResult(RESULT_CANCELED);
					finish();
				break;
				case R.id.calendarHelpButton:
					showDialog(HELP_DIALOG);
				default:
					Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in registerTimeButtonListenersAndSetDefaultText");
				break;
				}
			}
    	};
    	mChangeTimeBt.setOnClickListener(l);
    	mSetCurrentDateTimeBt.setOnClickListener(l); 
    	mOkButton.setOnClickListener(l);
    	mCancelButton.setOnClickListener(l);
    	mHelpButton.setOnClickListener(l);
    }
	@Override
	public void onBackPressed()
	{
		setResult(RESULT_CANCELED);
		super.onBackPressed();
	}
	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch(id)
		{
			case TIME_PICKER_DIALOG:
				return showTimePicker();
			case HELP_DIALOG:
				return showHelpDialog();
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
			case TIME_PICKER_DIALOG:
				TimePickerDialog timePicker = (TimePickerDialog)dialog;
				timePicker.updateTime(mDateTime.get(Calendar.HOUR_OF_DAY), mDateTime.get(Calendar.MINUTE));
				break;
			case HELP_DIALOG:
				View helpView = dialog.findViewById(R.id.holidaysHelpLL);
				if (mCurrentScreenId == ChronosMainActivity.HOLIDAYS_SCREEN_NUMBER)
				{
					helpView.setVisibility(View.VISIBLE);
				}
				else
				{
					if (helpView.getVisibility() != View.GONE) helpView.setVisibility(View.GONE);
				}
				helpView = dialog.findViewById(R.id.remindersHelpLL);
				if (mCurrentScreenId == ChronosMainActivity.REMINDERS_SCREEN_NUMBER)
				{
					helpView.setVisibility(View.VISIBLE);
				}
				else
				{
					if (helpView.getVisibility() != View.GONE) helpView.setVisibility(View.GONE);
				}
			break;
		}
	}
	@SuppressLint("InflateParams")
	private AlertDialog showHelpDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(true);
		builder.setTitle(getString(R.string.calendar_help_title_text));
		builder.setNegativeButton(getString(R.string.return_button_text), 
				new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		LayoutInflater inflater = this.getLayoutInflater();
		builder.setView(inflater.inflate(R.layout.calendar_help_dialog, null));
		return builder.create();
	}
	private TimePickerDialog showTimePicker()
	{
		TimePickerDialog timePicker = new TimePickerDialog(this,
		new TimePickerDialog.OnTimeSetListener()
		{
			public void onTimeSet(TimePicker view, int hourOfDay, int minute)
			{
				mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				mDateTime.set(Calendar.MINUTE, minute);
				mDateTime.set(Calendar.SECOND, 0);
				mDateTime.set(Calendar.MILLISECOND, 0);
				updateTimeButtonText();
			}
		},
		mDateTime.get(Calendar.HOUR_OF_DAY), mDateTime.get(Calendar.MINUTE), true);
		return timePicker;
	}
    private void registerCalendarViewListener()
    {
    	CalendarView.OnDateChangeListener onDateChangeListener = new CalendarView.OnDateChangeListener()
    	{
			public void onDateChanged(int year, int month, int dayOfMonth)
			{
				// TODO Auto-generated method stub
				//Календарная сетка обновляется в соответствии с текущими событиями (праздники или заметки) только если сработал метод fillCalendarGrid(), который в итоге привёл к генерации этого события или если на календаре был выбран новый месяц. Обновление событий на календарной сетке не происходит, если на календаре была выбрано новое число, а месяц остался прежним.
				boolean isNewMonth = mDateTime.get(Calendar.MONTH) + 1 != month || mDateTime.get(Calendar.YEAR) != year || mDateWasChangedDueTofillCalendarGrid;
				if (mDateWasChangedDueTofillCalendarGrid)
				{
					mDateWasChangedDueTofillCalendarGrid = false;
				}
				else
				{
					mDateTime.set(Calendar.YEAR, year);
					mDateTime.set(Calendar.MONTH, month - 1);
					mDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
				}
				//Если новый месяц или год неравен предудщему месяцу или году, или календарь обновился из вне при помощи fillCalendarGrid, то расставляем на календарной сетке праздники или заметки или что-то ещё
				if (isNewMonth)
				{
					if (mCurrentScreenId == ChronosMainActivity.HOLIDAYS_SCREEN_NUMBER)
					{
				    	//Получаем курсор с праздниками на месяц в mDateTime.
						//Данные в курсоре отсортированы по убыванию номера типа праздника, таким образом достигается, что если в один и тот же день два праздника, то приоритет имеет тот, у которого тип имеет меньший номер.
						Cursor cursor = mExternalDbAdapter.getHolidaysForCurrentMonth(mDateTime);
				    	if (cursor != null)
				    	{
				    		int rows = cursor.getCount();
				    		int[] days = new int[rows];
				    		int[] colors = new int[rows];
				    		cursor.moveToFirst();
				    		for (int i = 0; i < rows; i++)
				    		{
				    			String date = cursor.getString(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_DAY));
				    			days[i] = ExternalDbAdapter.convertStringDayFromHolidaysToActualDate(date, year)[0];
				    			int dayType = cursor.getInt(cursor.getColumnIndex(ExternalDbAdapter.HOLIDAYS_KEY_TYPE));
				    			colors[i] = getHolidaysColorByDayType(dayType);
				    			cursor.moveToNext();
				    		}
				    		cursor.close();
				    		mCalendarView.setCellsBackground(days, colors);
				    	}
					}
					else if (mCurrentScreenId == ChronosMainActivity.REMINDERS_SCREEN_NUMBER)
					{
						Cursor cursor = mRemindersDbAdapter.fetchRemindersForGivenMonth(mDateTime);
				    	if (cursor != null)
				    	{
				    		int rows = cursor.getCount();
				    		int[] days = new int[rows];
				    		int[] colors = new int[rows];
				    		cursor.moveToFirst();
				    		for (int i = 0; i < rows; i++)
				    		{
				    			days[i] = cursor.getInt(cursor.getColumnIndex(RemindersDbAdapter.KEY_DAY));
				    			colors[i] = mContext.getResources().getColor(R.color.calendar_reminder_days);
				    			cursor.moveToNext();
				    		}
				    		cursor.close();
				    		mCalendarView.setCellsBackground(days, colors);
				    	}
					}
				}
			}
    	};
    	mCalendarView.setOnDateChangeListener(onDateChangeListener);
    }
    private int getHolidaysColorByDayType(int dayType)
    {
    	switch (dayType)
    	{
    		case 1: return getResources().getColor(R.color.holidays_type1);
    		case 2: return getResources().getColor(R.color.holidays_type2);
    		case 3: return getResources().getColor(R.color.holidays_type3);
    		case 4: return getResources().getColor(R.color.holidays_type456);
    		case 5: return getResources().getColor(R.color.holidays_type456);
    		case 6: return getResources().getColor(R.color.holidays_type456);
    		case 7: return getResources().getColor(R.color.holidays_type7);
    		case 8: return getResources().getColor(R.color.holidays_type8);
    		default: return 0;
    	}
    }
	private void updateTimeButtonText()
	{
		SimpleDateFormat timeFormat = new SimpleDateFormat(PublicConstantsAndMethods.ONLY_TIME_WITHOUT_SECONDS_FORMAT, Locale.getDefault());
		String timeForButton = timeFormat.format(mDateTime.getTime());
		mChangeTimeBt.setText(timeForButton);
	}
}
