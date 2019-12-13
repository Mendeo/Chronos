package ru.mendeo.chronos;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.SQLException;
import android.database.Cursor;
import android.content.Context;
import android.content.ContentValues;
import java.util.Calendar;

public class RemindersDbAdapter
{
	private static final String DATABASE_NAME = "Reminders.db3";
	private static final String DATABASE_TABLE = "reminders";
	private static final int DATABASE_VERSION = 1;
	public static final String KEY_TITLE = "title";
	public static final String KEY_BODY = "body";
	public static final String KEY_YEAR = "year";
	public static final String KEY_MONTH = "month";
	public static final String KEY_DAY = "day";
	public static final String KEY_HOUR = "hour";
	public static final String KEY_MINUTE = "minute";
	public static final String KEY_SECOND = "second";
	public static final String KEY_MILLISECOND = "millisecond";
	public static final String KEY_ROWID = "_id";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_TABLE + "(" + KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + KEY_TITLE + " TEXT NOT NULL, " + KEY_BODY + " TEXT NOT NULL, " + KEY_YEAR + " INTEGER NOT NULL, " + KEY_MONTH + " INTEGER NOT NULL, " + KEY_DAY + " INTEGER NOT NULL, " + KEY_HOUR + " INTEGER NOT NULL, " + KEY_MINUTE + " INTEGER NOT NULL, " + KEY_SECOND + " INTEGER NOT NULL, " + KEY_MILLISECOND + " INTEGER NOT NULL);";
	private final Context mCtx;
	public RemindersDbAdapter(Context ctx)
	{
		this.mCtx = ctx;
	}
	public void open() throws SQLException
	{
		mDbHelper = new DatabaseHelper(mCtx);
		mDb = mDbHelper.getWritableDatabase();
	}
	public void close()
	{
		mDbHelper.close();
	}
	public long createReminder(String title, String body, Calendar reminderDateTime)
	{
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);
		initialValues.put(KEY_BODY, body);
		initialValues.put(KEY_YEAR, reminderDateTime.get(Calendar.YEAR));
		initialValues.put(KEY_MONTH, reminderDateTime.get(Calendar.MONTH));
		initialValues.put(KEY_DAY, reminderDateTime.get(Calendar.DAY_OF_MONTH));
		initialValues.put(KEY_HOUR, reminderDateTime.get(Calendar.HOUR_OF_DAY));
		initialValues.put(KEY_MINUTE, reminderDateTime.get(Calendar.MINUTE));
		initialValues.put(KEY_SECOND, reminderDateTime.get(Calendar.SECOND));
		initialValues.put(KEY_MILLISECOND, reminderDateTime.get(Calendar.MILLISECOND));
		long id = mDb.insert(DATABASE_TABLE, null, initialValues);
		return id;
	}
	public boolean deleteReminder(long rowId)
	{
		return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	public long[] deleteAllReminders()
	{
		Cursor curId = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID}, null, null, null, null, null);
		long[] ArrId = getRowIdArray(curId);
		curId.close();
		mDb.delete(DATABASE_TABLE, null, null);
		return ArrId;
	}
	public long[] deleteTodayReminders(Calendar DateTime)
	{
		String year = Integer.toString(DateTime.get(Calendar.YEAR));
		String month = Integer.toString(DateTime.get(Calendar.MONTH));
		String day = Integer.toString(DateTime.get(Calendar.DAY_OF_MONTH));
		String selection = KEY_YEAR + " = \"" + year + "\" AND " + KEY_MONTH + " = \"" + month + "\" AND " + KEY_DAY + " = \"" + day + "\"";
		Cursor curId = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID}, selection, null, null, null, null);
		long[] ArrId = getRowIdArray(curId);
		curId.close();
		mDb.delete(DATABASE_TABLE, selection, null);
		return ArrId;
	}
	private long[] getRowIdArray(Cursor cursor)
	{
		int size = cursor.getCount();
		long[] ArrId = new long[size];
		cursor.moveToFirst();
		for (int i = 0; i < size; i++)
		{
			ArrId[i] = cursor.getLong(cursor.getColumnIndex(KEY_ROWID));
			cursor.moveToNext();
		}
		return ArrId;
	}
	public Cursor fetchAllReminders() throws SQLException
	{
		String orderBy = KEY_YEAR + " ," + KEY_MONTH + ", " + KEY_DAY + ", " + KEY_HOUR + ", " + KEY_MINUTE;
		return mDb.query(DATABASE_TABLE, null, null, null, null, null, orderBy);
	}
	public Cursor fetchReminderForGivenDay(Calendar DateTime) throws SQLException
	{
		String year = Integer.toString(DateTime.get(Calendar.YEAR));
		String month = Integer.toString(DateTime.get(Calendar.MONTH));
		String day = Integer.toString(DateTime.get(Calendar.DAY_OF_MONTH));
		String selection = KEY_YEAR + " = " + year + " AND " + KEY_MONTH + " = " + month + " AND " + KEY_DAY + " = " + day;
		String orderBy = KEY_HOUR + ", " + KEY_MINUTE;
		return mDb.query(DATABASE_TABLE, null, selection, null, null, null, orderBy);
	}
	public Cursor fetchRemindersForGivenMonth(Calendar DateTime) throws SQLException
	{
		String year = Integer.toString(DateTime.get(Calendar.YEAR));
		String month = Integer.toString(DateTime.get(Calendar.MONTH));
		String selection = KEY_YEAR + " = " + year + " AND " + KEY_MONTH + " = " + month;
		return mDb.query(DATABASE_TABLE, null, selection, null, null, null, null);
	}
	//Метод, показывающий есть ли сегодня напоминания, которые должны произойти
	public boolean WillToDayReminders(Calendar DateTime)
	{
		String year = Integer.toString(DateTime.get(Calendar.YEAR));
		String month = Integer.toString(DateTime.get(Calendar.MONTH));
		String day = Integer.toString(DateTime.get(Calendar.DAY_OF_MONTH));
		String hour = Integer.toString(DateTime.get(Calendar.HOUR_OF_DAY));
		String minute = Integer.toString(DateTime.get(Calendar.MINUTE));
		String second = Integer.toString(DateTime.get(Calendar.SECOND));
		String millisecond = Integer.toString(DateTime.get(Calendar.MILLISECOND));
		String SQLWhere = KEY_YEAR + " = \"" + year + "\" AND " + KEY_MONTH + " = \"" + month + "\" AND " + KEY_DAY + " = \"" + day + "\" AND " + "(" + KEY_HOUR + " > \"" + hour + "\" OR (" + KEY_HOUR + " = \"" + hour + "\" AND " + KEY_MINUTE + " > \"" + minute + "\") OR (" + KEY_HOUR + " = \"" + hour + "\" AND " + KEY_MINUTE + " = \"" + minute + "\" AND " + KEY_SECOND + " > \"" + second + "\") OR (" + KEY_HOUR + " = \"" + hour + "\" AND " + KEY_MINUTE + " = \"" + minute + "\" AND " + KEY_SECOND + " = \"" + second + "\" AND " + KEY_MILLISECOND + " > \"" + millisecond + "\"))";
		Cursor cur = mDb.query(DATABASE_TABLE, null, SQLWhere, null, null, null, null);
		if (cur.getCount() > 0)
		{
			cur.close();
			return true;
		}
		else
		{
			cur.close();
			return false;
		}
	}
	public Cursor fetchReminder(long rowId) throws SQLException
	{
		return mDb.query(true, DATABASE_TABLE, null, KEY_ROWID + " = " + Long.toString(rowId), null, null, null, null, null);
	}
	public boolean updateReminder(long rowId, String title, String body, Calendar reminderDateTime)
	{
		ContentValues args = new ContentValues();
		args.put(KEY_TITLE, title);
		args.put(KEY_BODY, body);
		args.put(KEY_YEAR, reminderDateTime.get(Calendar.YEAR));
		args.put(KEY_MONTH, reminderDateTime.get(Calendar.MONTH));
		args.put(KEY_DAY, reminderDateTime.get(Calendar.DAY_OF_MONTH));
		args.put(KEY_HOUR, reminderDateTime.get(Calendar.HOUR_OF_DAY));
		args.put(KEY_MINUTE, reminderDateTime.get(Calendar.MINUTE));
		args.put(KEY_SECOND, reminderDateTime.get(Calendar.SECOND));
		args.put(KEY_MILLISECOND, reminderDateTime.get(Calendar.MILLISECOND));
		return mDb.update(DATABASE_TABLE, args, KEY_ROWID + " = " + rowId, null) > 0;
	}
	private static class DatabaseHelper extends SQLiteOpenHelper
	{
		DatabaseHelper(Context context)
		{
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		@Override
		public void onCreate(SQLiteDatabase db)
		{
			db.execSQL(DATABASE_CREATE);
		}
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
		{
		// Not used, but you could upgrade the database with ALTER
		// Scripts
		}
	}
}
