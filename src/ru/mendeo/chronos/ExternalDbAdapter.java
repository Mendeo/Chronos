package ru.mendeo.chronos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.preference.PreferenceManager;

public class ExternalDbAdapter 
{
	private static final String DATABASE_NAME = "FullInfo.db3";
	private static final long DATABASE_SIZE = 8173568;
	private static final int DATABASE_VERSION = 9; //���� ���������� ������ �� 22.04.2016. ������� - ���������� ������� HolidaysForSearch. ������� ������ 9.
	private static final String HISTORY_TABLE = "History";
	private static final String HOLIDAYS_TABLE = "Holidays";
	private static final String HOLIDAYS_FOR_SEARCH_TABLE = "HolidaysForSearch";
	private static final String LOCATION_TABLE = "Location";
	private static final String SUN_FACTS_TABLE = "SunFacts";
	private static final String MOON_FACTS_TABLE = "MoonFacts";
	private static final String SIGNS_TABLE = "Signs";
	private static final String MOON_CALENDAR_TABLE = "MoonCalendar";
	public static final String KEY_ROWID = "_id";
	public static final String HISTORY_KEY_DAY = "day";
	public static final String HISTORY_KEY_YEAR = "year";
	public static final String HISTORY_KEY_SHORT_DESC = "short_desc";
	public static final String HISTORY_KEY_FULL_DESC = "full_desc";
	public static final String HOLIDAYS_KEY_DAY = "day";
	public static final String HOLIDAYS_KEY_FULL_DESC = "full_desc";
	public static final String HOLIDAYS_KEY_TYPE = "type";
	public static final String HOLIDAYS_KEY_SHORT_DESC = "short_desc";
	public static final String LOCATION_KEY_REGION = "region";
	public static final String LOCATION_KEY_CITY = "city";
	public static final String LOCATION_KEY_LATITUDE = "latitude";
	public static final String LOCATION_KEY_LONGITUDE = "longitude";
	public static final String SUN_FACTS_KEY_DATA = "data";
	public static final String MOON_FACTS_KEY_DATA = "data";
	public static final String SIGNS_KEY_DAY = "day";
	public static final String SIGNS_KEY_DATA = "data";
	public static final String MOON_CALENDAR_KEY_DATA = "data";
	public static final String MOON_CALENDAR_KEY_DAY = "day";
	private static final String PREFERENCES_SUN_MAX_FACTS_KEY = "���������� ��������� ��������� ���, � �������� ���������� ������ ����������.";
	private static final String PREFERENCES_MOON_MAX_FACTS_KEY = "���� ���� ����� �������� �����-������ ����� �����, ����� ����� ���� ����� ���� � ���������...";
	private static final long NO_MAX_SUN_FACTS_IN_PREFERENCES = -1;
	private static final long NO_MAX_MOON_FACTS_IN_PREFERENCES = -1;
	private static long MaxSunFacts = -1;
	private static long MaxMoonFacts = -1;	
	//������ ������� ���� � ������� Holidays � Signs (����.�����)
	private static final String NORMAL_DATE_FORMAT = "d.M"; //"yyyy-MM-dd";
/*	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE1 + " ("
			+ KEY_ROWID + " integer primary key autoincrement, "
			+ KEY_MYTEXT1 + " text not null, "
			+ KEY_MYTEXT2 + " text not null);";*/
	private static boolean mIsDbUpgrade = false;
	private ExternalDatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mCtx;
	
	public ExternalDbAdapter(Context ctx)
	{
		this.mCtx = ctx;
	}
	public void open() throws SQLException
	{
		mDbHelper = new ExternalDatabaseHelper(mCtx);
    	mDbHelper.createExternalDataBase();
    	mDb = mDbHelper.getWritableDatabase();
    	if (mIsDbUpgrade)
    	{
    		mDbHelper.createExternalDataBase();
    		mDb = mDbHelper.getWritableDatabase();
    	}
	}	
	public void close()
	{
		mDbHelper.close();
	}
	
	public Cursor getMoonCalendarForCurrentDay(Calendar calendar)
	{
		//� ������� ���������� ���� ������ �� 2012 ���. ������� ��� �����������.
		//����������� ���� � ������� ������� ���� � ���� ������� � ������ ���������
		//� ���� ������� ��������� ��� ���� �� ������!
		String dateFormatTemplate = "d.M.yyyy";
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateFormatTemplate, Locale.getDefault());
		String normalDay = dateFormat.format(calendar.getTime());
		String[] columns = {KEY_ROWID, MOON_CALENDAR_KEY_DATA};
		String where = MOON_CALENDAR_KEY_DAY + " = " + '"' + normalDay + '"';
		try
		{
			Cursor cursor = mDb.query(MOON_CALENDAR_TABLE, columns, where, null, null, null, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public long getMaximumSunFacts()
	{
		if (MaxSunFacts >= 0) return MaxSunFacts;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		MaxSunFacts = prefs.getLong(PREFERENCES_SUN_MAX_FACTS_KEY, NO_MAX_SUN_FACTS_IN_PREFERENCES);
		if (MaxSunFacts < 0)
		{
			String[] columns = {SUN_FACTS_KEY_DATA};
			try
			{
				//�� ���� ������ �������� ��� �������
				Cursor cursor = mDb.query(SUN_FACTS_TABLE, columns, null, null, null, null, null);
				MaxSunFacts = cursor.getCount();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(PREFERENCES_SUN_MAX_FACTS_KEY, MaxSunFacts);
				editor.commit();
			}
			catch(SQLException e)
			{
				//���� ������ �� �������
			}
		}
		return MaxSunFacts;
	}
	public long getMaximumMoonFacts()
	{
		if (MaxMoonFacts >= 0) return MaxMoonFacts;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mCtx);
		MaxMoonFacts = prefs.getLong(PREFERENCES_MOON_MAX_FACTS_KEY, NO_MAX_MOON_FACTS_IN_PREFERENCES);
		if (MaxMoonFacts < 0)
		{
			String[] columns = {SUN_FACTS_KEY_DATA};
			try
			{
				//�� ���� ������ �������� ��� �������
				Cursor cursor = mDb.query(MOON_FACTS_TABLE, columns, null, null, null, null, null);
				MaxMoonFacts = cursor.getCount();
				SharedPreferences.Editor editor = prefs.edit();
				editor.putLong(PREFERENCES_MOON_MAX_FACTS_KEY, MaxMoonFacts);
				editor.commit();
			}
			catch(SQLException e)
			{
				//���� ������ �� �������
			}
		}
		return MaxMoonFacts;		
	}	
	public String getSunFactById(long id)
	{
		String[] columns = {SUN_FACTS_KEY_DATA};
		String where = KEY_ROWID + " = " + Long.toString(id);
		try
		{
			//�� ���� ������ �������� ��� �������
			Cursor cursor = mDb.query(SUN_FACTS_TABLE, columns, where, null, null, null, null);
			cursor.moveToFirst();
			String out = cursor.getString(cursor.getColumnIndex(SUN_FACTS_KEY_DATA));
			cursor.close();
			return out;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public String getMoonFactById(long id)
	{
		String[] columns = {MOON_FACTS_KEY_DATA};
		String where = KEY_ROWID + " = " + Long.toString(id);
		try
		{
			//�� ���� ������ �������� ��� �������
			Cursor cursor = mDb.query(MOON_FACTS_TABLE, columns, where, null, null, null, null);
			cursor.moveToFirst();
			String out = cursor.getString(cursor.getColumnIndex(MOON_FACTS_KEY_DATA));
			cursor.close();
			return out;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public String[] getCitysByRegion(String region)
	{
		String[] columns = {LOCATION_KEY_CITY};
		String where = LOCATION_KEY_REGION + " = " + '"' + region + '"';
		String orderBy = LOCATION_KEY_CITY;
		Cursor cursor;
		try
		{
			//�� ���� ������ �������� ��� �������
			cursor = mDb.query(LOCATION_TABLE, columns, where, null, null, null, orderBy);
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
		int size = cursor.getCount();
		if (size > 0)
		{
			String[] out = new String[size];
			cursor.moveToFirst();
			for (int i = 0; i < size; i++)
			{
				out[i] = cursor.getString(cursor.getColumnIndex(LOCATION_KEY_CITY));
				cursor.moveToNext();
			}
			cursor.close();
			return out;
		}
		else
		{
			cursor.close();
			return null;
		}
	}
	public String[] getRegions()
	{	
		Cursor cursor;
		try
		{
			//�� ���� ������ �������� ��� �������
			cursor = mDb.query(LOCATION_TABLE, new String[] {LOCATION_KEY_REGION}, null, null, LOCATION_KEY_REGION, null, LOCATION_KEY_REGION);
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
		int size = cursor.getCount();
		if (size > 0)
		{
			String[] out = new String[size];
			cursor.moveToFirst();
			for (int i = 0; i < size; i++)
			{
				out[i] = cursor.getString(cursor.getColumnIndex(LOCATION_KEY_REGION));
				cursor.moveToNext();
			}
			cursor.close();
			return out;
		}
		else
		{
			cursor.close();
			return null;
		}
	}
	public double[] getCoordinatesByCityAndRegion(String city, String region)
	{
		String[] columns = {LOCATION_KEY_LONGITUDE, LOCATION_KEY_LATITUDE};
		String where = LOCATION_KEY_CITY + " = " + '"' + city + '"' + " AND " + LOCATION_KEY_REGION + " = " + '"' + region + '"';
		Cursor cursor;
		try
		{
			//�� ���� ������ �������� ��� �������
			cursor = mDb.query(LOCATION_TABLE, columns, where, null, null, null, null);
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
		if (cursor.getCount() > 0)
		{
			double[] out = new double[2];
			cursor.moveToFirst();
			out[0] = cursor.getDouble(cursor.getColumnIndex(LOCATION_KEY_LONGITUDE));
			out[1] = cursor.getDouble(cursor.getColumnIndex(LOCATION_KEY_LATITUDE));
			cursor.close();
			return out;
		}
		else
		{
			cursor.close();
			return null;
		}
	}
	public Cursor getSignsForCurrentDay(Calendar calendar)
	{
		//����������� ���� � ������� ������� ���� � ���� ������� � ���������
		//� ���� ������� ��������� ��� ���� �� ������!
		SimpleDateFormat dateFormat = new SimpleDateFormat(NORMAL_DATE_FORMAT, Locale.getDefault());
		String normalDay = dateFormat.format(calendar.getTime());
		String[] columns = {KEY_ROWID, SIGNS_KEY_DATA};
		String where = SIGNS_KEY_DAY + " = " + '"' + normalDay + '"';
		try
		{
			Cursor cursor = mDb.query(SIGNS_TABLE, columns, where, null, null, null, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public Cursor getHolidaysForCurrentMonth(Calendar calendar)
	{
		StringBuilder where = new StringBuilder();
		where.append(HOLIDAYS_KEY_DAY);
		where.append(" LIKE \"%.");
		where.append(Integer.toString(calendar.get(Calendar.MONTH) + 1));
		where.append('"');
		//��������� ��� �� � ���� ������ ����������, ����������� � �����.
		Calendar tmpCal = (Calendar)calendar.clone();
		tmpCal.set(Calendar.DAY_OF_MONTH, 1);
		tmpCal.set(Calendar.HOUR_OF_DAY, 12);
		long firstDayOfMonth = tmpCal.getTimeInMillis();
		tmpCal = AstroCalcModules.GetOrthodoxEaster(calendar.get(Calendar.YEAR));
		long EasterDay = tmpCal.getTimeInMillis();
		long deltaEaster = firstDayOfMonth - EasterDay;
		int startDaysFromEaster = (int)Math.floor((double)deltaEaster / 86400000.0);
		for (int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++)
		{
			where.append(" OR ");
			where.append(HOLIDAYS_KEY_DAY);
			where.append(" = \"f.e.");
			where.append(Integer.toString(startDaysFromEaster + i));
			where.append('"');
		}
		String[] columns = {HOLIDAYS_KEY_DAY, HOLIDAYS_KEY_TYPE};
		//������ � ������� ��������� �� �������� ������ ���� ���������, ����� ������� �����������, ��� ���� � ���� � ��� �� ���� ��� ���������, �� ��������� ����� ���, � �������� ��� ����� ������� �����.
		String orderBy = HOLIDAYS_KEY_TYPE + " DESC";
		try
		{
			//�� ���� ������ �������� ��� ������, � ������� �������� ���� day ������������ �� .�����_������_��_calendar ��� ��� ��������� � ���� ������, ��������� � ������. 
			Cursor cursor = mDb.query(HOLIDAYS_TABLE, columns, where.toString(), null, null, null, orderBy);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	//��������������� ��������� ������ ���� � ������� "Holidays" (� ��� ����� ��������� ����) � ������ ����������� ��� �� ������� ���.
	//��� ����� ����������, ����� ������������� ��������� ����
	//���������� ����� � ���� ������� �� ���� ���������. ������ ������� ����, ������ - �����.
	public static int[] convertStringDayFromHolidaysToActualDate(String date, int year)
	{
		String[] dateArray = date.split("\\.");
		//��������� ��������� ���������, ����������� ��� �������������� ��������� ��� �� �� � �������
		Calendar tmpCal = Calendar.getInstance();
		if (tmpCal.getFirstDayOfWeek() != Calendar.MONDAY) tmpCal.setFirstDayOfWeek(Calendar.MONDAY);
		//���� ��������� ��������, �� ������ ���� ����� ��������������� �����, ������ ���� ������� ������������� ��������
		int day, month;
		if (dateArray[0].equals("f"))
		{
			//���� ���� ��������� � �����:
			if (dateArray[1].equals("e"))
			{
				int daysFromEaster = Integer.parseInt(dateArray[2]);
				tmpCal = AstroCalcModules.GetOrthodoxEaster(year);
				tmpCal.add(Calendar.DAY_OF_YEAR, daysFromEaster);
				month = tmpCal.get(Calendar.MONTH) + 1;
				day = tmpCal.get(Calendar.DAY_OF_MONTH);
			}
			else
			{
				month = Integer.parseInt(dateArray[dateArray.length - 1]);
				tmpCal.set(Calendar.YEAR, year);
				tmpCal.set(Calendar.MONTH, month - 1);
				int lastWeek = tmpCal.getActualMaximum(Calendar.WEEK_OF_MONTH);
				int maxDaysInMonth = tmpCal.getActualMaximum(Calendar.DAY_OF_MONTH);
				//�������� ����� ��� ������. (����������� - 7-�� ����)
				int dayOfWeek = Integer.parseInt(dateArray[2]);
				int weekOfMonth;
				//���� � ������ date �� �� �������, ��� �������� ���� ������ ������ ���� ��������� � ������, �� ���������� ����� ������ � ������
				//��� ����� ���������� ���� ������ ���������� ��� ������. (����������� ��� ���, ��� ����������� ��� 7-�� ���)
				//���� ���� ������ ���������� ��� ������ ������ ��������� ��� ������, �� ������� ����� ������ - ��� ��������� ������ ������, ����� - ��� ������������� ������ ������
				if (dateArray[1].equals("l"))
				{
					tmpCal.set(Calendar.DAY_OF_MONTH, maxDaysInMonth);
					int lastDayOfWeek = tmpCal.get(Calendar.DAY_OF_WEEK);
					lastDayOfWeek = lastDayOfWeek == 1 ? 7 : lastDayOfWeek - 1;
					weekOfMonth = lastDayOfWeek >= dayOfWeek ? lastWeek : lastWeek - 1;
				}
				//���� ��������� ����������� ����� ������, �� ����� ���������� ����� ��������, ��� � ������ ������ �� ����� ������� ��� � ������� ��� ������ ����� ����������.
				//��������, ���� ������ ���������� � ��������, � �� ���� �����. ����� �������� ���� ���������� ���������� �������� � ��������� ������� ������.
				else
				{
					weekOfMonth = Integer.parseInt(dateArray[1]);
					tmpCal.set(Calendar.DAY_OF_MONTH, 1);
					int firtstDayOfWeek = tmpCal.get(Calendar.DAY_OF_WEEK);
					firtstDayOfWeek = firtstDayOfWeek == 1 ? 7 : firtstDayOfWeek - 1;
					if (firtstDayOfWeek > dayOfWeek) weekOfMonth ++;
				}
				//������������ ���� ������ � ������, �������� ��������� (����������� - ������ ����)
				dayOfWeek = dayOfWeek == 7 ? 1 : dayOfWeek + 1;
				tmpCal.set(Calendar.WEEK_OF_MONTH, weekOfMonth);
				tmpCal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
				day = tmpCal.get(Calendar.DAY_OF_MONTH);
			}
		}
		else
		{
			day = Integer.parseInt(dateArray[0]);
			month = Integer.parseInt(dateArray[dateArray.length - 1]);
		}
		int[] out = {day, month};
		return out;
	}
	public Cursor getHolidaysForCurrentDay(Calendar calendar)
	{
		//����������� ���� � ������� ������� (�� ���������) ���� � ����� ������� � �����������.
		SimpleDateFormat dateFormat = new SimpleDateFormat(NORMAL_DATE_FORMAT, Locale.getDefault());
		String normalDay = dateFormat.format(calendar.getTime());
		//������ ����������� ���� � ������� ��������� ���� � ���� ������� � ����������� (��� ��� ���� ������� :))
		if (calendar.getFirstDayOfWeek() != Calendar.MONDAY) calendar.setFirstDayOfWeek(Calendar.MONDAY);
		int weekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH);
		boolean isLastDayOfWeekInMonth = (calendar.getActualMaximum(Calendar.DAY_OF_MONTH) - calendar.get(Calendar.DAY_OF_MONTH)) < 7;
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		dayOfWeek = dayOfWeek == 1 ? 7 : dayOfWeek - 1;
		int month = calendar.get(Calendar.MONTH) + 1;
		//�.�. ������ ������ ����� ���������� �� � ������������, �� ����� ������ ��� ���������� ��� ������ ������������ ��������� �������: �� ����� weekOfMonth ���� �������� ���� ������ � calendar ���������� �� ������ ������ ������ � weekOfMonth + 1 �����.
		Calendar tmpCal = (Calendar)calendar.clone();
		tmpCal.set(Calendar.DAY_OF_MONTH, 1);
		int tmpDayOfWeek = tmpCal.get(Calendar.DAY_OF_WEEK);
		tmpDayOfWeek = tmpDayOfWeek == 1 ? 7 : tmpDayOfWeek - 1;
		if (dayOfWeek < tmpDayOfWeek) weekOfMonth--;
		//��������� ������ ��� ������������ �����.
		tmpCal = AstroCalcModules.GetOrthodoxEaster(calendar.get(Calendar.YEAR));
		long deltaEaster = calendar.getTimeInMillis() - tmpCal.getTimeInMillis();
		String daysFromEaster = Integer.toString((int)Math.floor((double)deltaEaster / 86400000.0));
		String strWeekOfMonth = Integer.toString(weekOfMonth);
		String strDayOfWeek = Integer.toString(dayOfWeek);
		String strMonth = Integer.toString(month);
		String flowDay = "f." + strWeekOfMonth + "." + strDayOfWeek + "." + strMonth;
		String flowDayLastWeek = isLastDayOfWeekInMonth ? "f.l." + strDayOfWeek + "." + strMonth : "��� ����� ��������, �������� �������� ��� � ��. �����, ��������, � ���� ���������� � ����� � ���� ���� :)";
		String flowDayFromEaster = "f.e." + daysFromEaster;
		String[] columns = {KEY_ROWID, HOLIDAYS_KEY_TYPE, HOLIDAYS_KEY_SHORT_DESC};
		String where = "(" + HOLIDAYS_KEY_DAY + " = " + '"' + normalDay + '"' + ") OR (" + HOLIDAYS_KEY_DAY + " = " + '"' + flowDay + '"' + ") OR (" + HOLIDAYS_KEY_DAY + " = " + '"' + flowDayLastWeek + '"' + ") OR (" + HOLIDAYS_KEY_DAY + " = " + '"' + flowDayFromEaster + '"' + ")";
		try
		{
			Cursor cursor = mDb.query(HOLIDAYS_TABLE, columns, where, null, null, null, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public Cursor getHistoryForCurrentDay(Calendar calendar)
	{
		//����������� ���� � ������� ���� � ���� ������� � ��������
		SimpleDateFormat dateFormat = new SimpleDateFormat(NORMAL_DATE_FORMAT, Locale.getDefault());
		String normalDay = dateFormat.format(calendar.getTime());
		String[] columns = {KEY_ROWID, HISTORY_KEY_YEAR, HISTORY_KEY_SHORT_DESC};
		String where = HISTORY_KEY_DAY + " = " + '"' + normalDay + '"';
		String orderBy = HISTORY_KEY_YEAR + " ASC";
		try
		{
			Cursor cursor = mDb.query(HISTORY_TABLE, columns, where, null, null, null, orderBy);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public Cursor getHolidayById(long id)
	{
		String[] columns = {HOLIDAYS_KEY_TYPE, HOLIDAYS_KEY_SHORT_DESC, HOLIDAYS_KEY_FULL_DESC};
		try
		{
			Cursor cursor = mDb.query(HOLIDAYS_TABLE, columns, KEY_ROWID + " = " + Long.toString(id), null, null, null, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	public Cursor getHistoryById(long id)
	{
		String[] columns = {HISTORY_KEY_YEAR, HISTORY_KEY_SHORT_DESC, HISTORY_KEY_FULL_DESC};
		try
		{
			Cursor cursor = mDb.query(HISTORY_TABLE, columns, KEY_ROWID + " = " + Long.toString(id), null, null, null, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	@SuppressLint("DefaultLocale")
	public Cursor searchHolidays(String query)
	{
		query = query.toLowerCase(Locale.getDefault()).replaceAll("[\\s]{2,}", " ").trim(); //�������� � ������� �������� � ������� ��� ������ �������.
		String dbQuery = 
				"SELECT " + HOLIDAYS_TABLE + "." + KEY_ROWID + ", " + HOLIDAYS_TABLE + "." + HOLIDAYS_KEY_SHORT_DESC + ", " + HOLIDAYS_TABLE + "." + HOLIDAYS_KEY_DAY + 
				" FROM " + HOLIDAYS_TABLE + 
				" INNER JOIN " + HOLIDAYS_FOR_SEARCH_TABLE +
				" ON " + HOLIDAYS_TABLE + "." + KEY_ROWID + " = " + HOLIDAYS_FOR_SEARCH_TABLE + "." + KEY_ROWID +
				" WHERE " + HOLIDAYS_FOR_SEARCH_TABLE + "." + HOLIDAYS_KEY_SHORT_DESC + " LIKE \"%" + query + "%\"";
		Log.d("MyLogD", dbQuery);
		try
		{
			Cursor cursor = mDb.rawQuery(dbQuery, null);
			return cursor;
		}
		catch(SQLException e)
		{
			//���� ������ �� �������
			return null;
		}
	}
	private static class ExternalDatabaseHelper extends SQLiteOpenHelper
	{
    	private final String DATABASE_ABSOLUTE_PATH;
    	private final Context mContext;
    	private Boolean mIsInternalCall;
    	
    	ExternalDatabaseHelper(Context context)
		{
    		super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mContext = context;
			DATABASE_ABSOLUTE_PATH = context.getDatabasePath(DATABASE_NAME).getAbsolutePath();	//DATABASE_PATH + DATABASE_NAME;
			mIsInternalCall = false;
		}
    	public void createExternalDataBase()
    	{
    		boolean isCopyNewDb;
    		if (mIsDbUpgrade)
    		{
    			mIsDbUpgrade = false;
    			isCopyNewDb = true;
    		}
    		else
    		{
    			isCopyNewDb = !checkDataBase();
    		}
            if(isCopyNewDb)
            {
            	//������� ���� �����, ������� ������ ����, ����� ��� ����� ������������ (�����������, �� ��������� ����������� ����� �� ��������)
            	mIsInternalCall = true;
            	SQLiteDatabase dB = this.getReadableDatabase();
            	mIsInternalCall = false;
            	//���� ������ �� �������� (����� ����������� ������):
            	//SQLiteDatabase dB = SQLiteDatabase.openOrCreateDatabase(DATABASE_ABSOLUTE_PATH, null);
            	dB.close(); //������� ��, ����� �������� ������ ������.
                try
                {
                    copyExternalDataBase();
                }
                catch (IOException e)
                {     
                	throw new Error("Error copying database: " + e.getMessage());
                }
            }
        }
    	//�������� ������������� ���� ������
    	private boolean checkDataBase()
    	{
            SQLiteDatabase checkDB = null;
            long size = -1;
            try
            {
            	size = (new File(DATABASE_ABSOLUTE_PATH)).length(); //�� ������, ���� ��������� ���� � ���� ����������������.
            	checkDB = SQLiteDatabase.openDatabase(DATABASE_ABSOLUTE_PATH, null, SQLiteDatabase.OPEN_READONLY);
            }
            catch(SQLException e)
            {
                //���� ��� �� ����������
            }
            if(checkDB != null)
            {
                checkDB.close();
            }
            return (checkDB != null) && (size == DATABASE_SIZE);
        }
    	//�������� �� �� ����� assets
    	private void copyExternalDataBase() throws IOException
    	{
    		//��������� ��������� �� ��� �������� �����
            InputStream inputDb = mContext.getAssets().open(DATABASE_NAME);
    		//�������� ������� ��������� �����, ����� ���������, ��� � �������� ������, ����� ������� ��. ����� �� ����� ������������ � ������ onResume.
    		if (inputDb.available() != DATABASE_SIZE) Log.w(PublicConstantsAndMethods.MY_LOG_TAG, "������ �� �� ��������� � ��������� � ����");
            //��������� ������ ���� ������ ��� ��������� �����
            OutputStream outputDb = new FileOutputStream(DATABASE_ABSOLUTE_PATH);
            //���������� ����� �� ��������� ����� � ���������
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputDb.read(buffer)) > 0)
            {
                outputDb.write(buffer, 0, length);
            }
            //��������� ������
            outputDb.flush();
            outputDb.close();
            inputDb.close();
        }
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			if (!mIsInternalCall)
			{
				//���� �� ���������� ����� createExternalDataBase(), �� �� �������� ���:
				//db.execSQL(DATABASE_CREATE);
				//mIsNewDb = true;
			}
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
			if (!mIsInternalCall)
			{
				Log.d("MyLog", "Upgrading DB...");
				mIsDbUpgrade = true;
			}
		}
	}
}
