package ru.mendeo.chronos;

import java.util.Calendar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

@SuppressWarnings("deprecation")
public class CalendarView extends ViewGroup
{	
	private static final int ROWS_NUMBER = 6;
	private static final int COLUMNS_NUMBER = 7;
	private static final int CELLS_NUMBER = ROWS_NUMBER * COLUMNS_NUMBER;
	//������� ����������� ����� ��� �������� ������ (160 dpi).
	//private static final int GRID_WIDTH_DIP = 291;
	//private static final int GRID_HEIGHT_DIP = 207;
	//��������� ������ (X) � ������ (Y) �������� ImageView � ���������� ����� ����. 
	private static final float X_CELL_RATIO = 12f;
	private static final float Y_CELL_RATIO = 6.5f;
	//��������� ������ (X) � ������ (Y) ����� ������ View � ������ ��� ������ ��������������� ����������� �����.
	private static final float X_GRID_RATIO = 1.02f;
	private static final float Y_GRID_RATIO = 1.43f;
	//��������� ������ ����� View � ���������� �� ���� View �� ����������� �����.
	private static final float BOTTOM_GRID_RATIO = 69f;
	//���������� ������ � ������ ����� View � ������ � ������ ��������� � ��������� ������ � ����
	private static final float X_TITLE_RATIO = 1.5f;
	private static final float Y_TITLE_RATIO = 6f;
	//��������� ����� View � ���������� �� �������� ���� View �� ��������� � ��������� ������ � ����
	private static final float TOP_TITLE_RATIO = 1000f;
	//��������� ������ View � ���������� �� ������ ������������ ������ �� ������ View
	private static final float LEFT_CHANGE_MONTH_BUTTON_RATIO = 20f;
	//��������� ������ View � ��������� �� ����� View �� ������ ������������ ������ �� ������ View
	private static final float TOP_CHANGE_MONTH_BUTTON_RATIO = 86f;
	//����� ������� �� �������� � ������ init()
	//private static final int CURRENT_DAY_COLOR = 0xFF306898;
	private static Drawable CURRENT_DAY_DRAWABLE;
	private static int SELECTED_DAY_COLOR; //= 0xFF777878;
	private static int TITLE_TEXT_COLOR; //= 0xFFFFFFFF;
	private static int SUNDAY_TEXT_COLOR; //= 0xFFF84B63;
	private static int WORKDAY_TEXT_COLOR;//= 0xFFFFFFFF;
	//��������� ������ ����� View � ������� ������ ��� ��������� � ��������� ������ � ����
	private static final float TITLE_TEXT_SIZE_RATIO = 8.28f;
	//��������� ������ ����� View � ������� ������ ��� ����������� ����� �� ����������� �����	
	private static final float CELL_TEXT_SIZE_RATIO = 12.9375f;
	private static final int PREVIOUS_MONTH_BUTTON_ID = 102043;
	private static final int NEXT_MONTH_BUTTON_ID = 3849;
	//private static final int CELL_ID_INCREMENT = 1000; //����� ��� ����, ����� id ��������� ����� ���������� �� � �������.
	private ImageView[] mCellViews;
	private View mTitleView;
	private ImageView mPreviousMonthButton, mNextMonthButton;
	private int mWidth, mHeight, mCellWidth, mCellHeight, mDx, mDy, mTitleWidth, mTitleHeight, mChangeMonthButtonWidth, mChangeMonthButtonHeight;
	//������ ����� ������, ��������������� �������� ���.
	private int mCurrentDayCellNumberSaver = -1;
	//����� � ��� � ������ ������ ������������ ���������
	private int mCurrentMonth = -1;
	private int mCurrentYear = -1;
	//����, ����� � ��� ������� �� �����.
	private int mCurrentTimeMonth = -1;
	private int mCurrentTimeYear = -1;
	private int mCurrentTimeDayNumber = -1;
	//������ ����� ������, ��������������� ������� ����� �������������� ������.
	private int mCellNumberForFirstDayOfCurrentMonth = -1;
	//������ ������������ ����� �������������� ������.
	private int mMaxDayNumberOfCurrentMonth = -1;
	//������ ����� ������, ��������������� ��������� ����� �������������� ������.
	private int mSelectedDayCellNumberSaver = -1;
	//������ ����� ���������� ��� �������������� ������.
	private int mSelectedDayNumber = -1;
	//������, �������� ������ �����, ��� ������� ��� ������ �� ���
	private int[] mExternalCellsColorNumbers = null;
	private int[] mExternalDaysNumbersColor = null;
	private int[] mExternalDaysNumbersDrawable = null;
	private int[] mExternalCellsDrawableNumbers = null;
	private int[] mExternalCellsColors = null;
	private Drawable[] mExternalCellsDrawables = null;
	//����������, ����������� ��� ��������� ����������� ����� ������ ����� ����, ��� ��� �������� ����� onMeasure, �.�. ���� View ���� ��������
	//���������� ��� �� ������� onMeasure
	private boolean mIsMeasured = false;
	//���������� ����� �� ��������� �� ������ onMeasure ��������������� ������. ��� ������ �������� ���, ��� ������� ��������� ����������� � ���������� ���������� � ��� ������ �� onMeasure ��� ���������� �� �����������
	private boolean mStartFillCalendarInOnMeasure = false;
	private boolean mStartSetCellsBackgroundColorsInOnMeasure = false;
	private boolean mStartSetCellsBackgroundDrawablesInOnMeasure = false;
	private OnDateChangeListener mOnDateChangeListener = null;
	
	public CalendarView(Context context)
	{
		super(context);
		init(context);
	}
	public CalendarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context);
	}
	public CalendarView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init(context);
	}
	private void init(Context context)
	{
		setBackgroundResource(R.drawable.calendar_grid);
		CURRENT_DAY_DRAWABLE = getResources().getDrawable(R.drawable.calendar_grid_cell_current_day_background);
		SELECTED_DAY_COLOR = getResources().getColor(R.color.calendar_selected_day);
		TITLE_TEXT_COLOR = getResources().getColor(R.color.calendar_title_text_color);
		SUNDAY_TEXT_COLOR = getResources().getColor(R.color.calendar_sunday_text_color);
		WORKDAY_TEXT_COLOR = getResources().getColor(R.color.calendar_workday_text_color);
		mPreviousMonthButton = new ImageView(context);
		mNextMonthButton  = new ImageView(context);
		mPreviousMonthButton.setImageResource(R.drawable.previous_month_button);
		mNextMonthButton.setImageResource(R.drawable.next_month_button);
		mPreviousMonthButton.setId(PREVIOUS_MONTH_BUTTON_ID);
		mNextMonthButton.setId(NEXT_MONTH_BUTTON_ID);
		mPreviousMonthButton.setClickable(true);
		mNextMonthButton.setClickable(true);
		addView(mPreviousMonthButton);
		addView(mNextMonthButton);
		mTitleView = new View(context);
		addView(mTitleView);
		mCellViews = new ImageView[CELLS_NUMBER];
		for (int i = 0; i < CELLS_NUMBER; i++)
		{
			mCellViews[i] = new ImageView(context);
			mCellViews[i].setId(i);
			mCellViews[i].setClickable(true);
			addView(mCellViews[i]);
		}
		registerCellClickListeners();
		registerChangeMonthButtonsClickListeners();
	}
	public static interface OnDateChangeListener
	{
		public void onDateChanged(int year, int month, int dayOfMonth);
	}
	public void setOnDateChangeListener(OnDateChangeListener l)
	{
		mOnDateChangeListener = l;
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//��������� View ����� ���������� ���� �������. (��� �� ���� �� xml, �� wrap_content �������� �� �����):
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//�������� �������, ������� ���������� ��� ���� ���� View:
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		//��������� ������� ������ ��������� ���� �������. ��� ������������� ������ �� LayoutParams, ����������� � ���� ������ ����.
		mChangeMonthButtonWidth = 0;
		mChangeMonthButtonHeight = 0;
		mNextMonthButton.measure(MeasureSpec.makeMeasureSpec(mChangeMonthButtonWidth, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(mChangeMonthButtonHeight, MeasureSpec.UNSPECIFIED));
		mPreviousMonthButton.measure(MeasureSpec.makeMeasureSpec(mChangeMonthButtonWidth, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(mChangeMonthButtonHeight, MeasureSpec.UNSPECIFIED));
		//�������� ����������� ������ ������ ��������� ������, ������� ��� ��� ���� ���������.
		mChangeMonthButtonWidth = mPreviousMonthButton.getMeasuredWidth();
		mChangeMonthButtonHeight = mPreviousMonthButton.getMeasuredHeight();
		//��������� ������� ��������� � ��������� ������ � ����
		mTitleWidth = (int)Math.round(((float)mWidth / X_TITLE_RATIO));
		mTitleHeight = (int)Math.round(((float)mHeight / Y_TITLE_RATIO));
		//����� ������ ��� ���������
		mTitleView.measure(MeasureSpec.makeMeasureSpec(mTitleWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(mTitleHeight, MeasureSpec.EXACTLY));
		float fTmp;
		fTmp = ((float)mWidth / X_GRID_RATIO) / ((float)COLUMNS_NUMBER * (X_CELL_RATIO + 1f) - 1f);
		mDx = (int)Math.round(fTmp);
		mCellWidth = (int)Math.round((X_CELL_RATIO * fTmp));
		fTmp = ((float)mHeight / Y_GRID_RATIO) / ((float)ROWS_NUMBER * (Y_CELL_RATIO + 1f) - 1f);
		mDy = (int)Math.round(fTmp);
		mCellHeight = (int)Math.round((Y_CELL_RATIO * fTmp));
		int cellWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mCellWidth, MeasureSpec.EXACTLY);
		int cellHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mCellHeight, MeasureSpec.EXACTLY);
		//����� ������� ��� ���� �����
		for (int i = 0; i < CELLS_NUMBER; i++) mCellViews[i].measure(cellWidthMeasureSpec, cellHeightMeasureSpec);
		//�������, ��� ��� View �������
		mIsMeasured = true;
		//���� ���������� ��������� ���� ������� �� ��� �� ��������� View, �� ����� fillGridCalendar �������������� ����� ���� ��� ������ mStartFillCalendarCoreInOnMeasure
		if (mStartFillCalendarInOnMeasure) fillCalendarGrid(mCurrentYear, mCurrentMonth, mSelectedDayNumber);
		if (mStartSetCellsBackgroundColorsInOnMeasure) setCellsBackground(mExternalDaysNumbersColor, mExternalCellsColors);
		if (mStartSetCellsBackgroundDrawablesInOnMeasure) setCellsBackground(mExternalDaysNumbersDrawable, mExternalCellsDrawables);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int left, top, right, bottom;
		//��������� ������ ��������� ������
		left = (int)((float)mWidth / LEFT_CHANGE_MONTH_BUTTON_RATIO);
		top = (int)((float)mWidth / TOP_CHANGE_MONTH_BUTTON_RATIO);
		right = left + mChangeMonthButtonWidth;
		bottom = top + mChangeMonthButtonHeight;
		mPreviousMonthButton.layout(left, top, right, bottom);
		right = mWidth - left;
		left = right - mChangeMonthButtonWidth;
		mNextMonthButton.layout(left, top, right, bottom);
		//���������� ��������� � ��������� ������ � ���� �� ����������� - �� �������� View, �� ��������� - ������ ��		
		left = (mWidth - mTitleWidth) / 2;
		top = (int)((float)mHeight / TOP_TITLE_RATIO);
		right = left + mTitleWidth;
		bottom = top + mTitleHeight;
		mTitleView.layout(left, top, right, bottom);
		//��������� ���, �� ������� ����� ���������� ������ ��� ����������
		int stepX = mDx + mCellWidth;
		int stepY = mDy + mCellHeight;
		//��������� ���������� �� ����� � �� ����� ������� �� ������ ����������� �����.
		//�������, ��� �� ���� ����� ���������� ������ �� BOTTOM_GRID_RATIO, � �� ����������� ����������
		int pX = (int)(((float)(mWidth - COLUMNS_NUMBER * mCellWidth - (COLUMNS_NUMBER - 1) * mDx)) / 2f);
		int pY = mHeight - ROWS_NUMBER * mCellHeight - (ROWS_NUMBER - 1) * mDy - (int)(Math.round((float)mHeight / BOTTOM_GRID_RATIO));
		for (int i = 0; i < COLUMNS_NUMBER; i++)
		{
			for (int j = 0; j < ROWS_NUMBER; j++)
			{
				left = pX + i * stepX;
				top = pY + j * stepY;
				right = left + mCellWidth;
				bottom = top + mCellHeight;
				mCellViews[getViewIndexByRowAndColumn(j, i)].layout(left, top, right, bottom);
			}
		}
	}
	//��������� fillCalendarGrid, �� ������ ���� ���� View ��� ���� ��������
	//���� ����� ��������� �� ����� ������� ����� ������� �����, � ��� �� ���������� ���� �����, ����� ��������� � �.�.
	public void fillCalendarGrid(int year, int month, int day)
	{		
		boolean isNewMonth = true;
		if (mStartFillCalendarInOnMeasure)
		{
			mStartFillCalendarInOnMeasure = false;
		}
		else
		{			
			isNewMonth = (mCurrentYear != year) || (mCurrentMonth != month);
			mCurrentYear = year;
			mCurrentMonth = month;
			mSelectedDayNumber = day;
		}
		if (mIsMeasured)
		{
			if (year >= 0 && month >= 1 && month <= 12)
			{
				//������� ��� �������� ���. ���� ������� ������� �����, �� ���� ��� ������������� ����, ���� ���, �� ������ ��������
				if (mCurrentDayCellNumberSaver >= 0)
				{
					mCellViews[mCurrentDayCellNumberSaver].setBackgroundColor(0);
					mCurrentDayCellNumberSaver = -1;
				}
				//������� ��� � �����, ��� ������� ��� ������ �� ���.
				if (mExternalCellsDrawableNumbers != null)
				{
					for (int i = 0; i < mExternalCellsDrawableNumbers.length; i++) mCellViews[mExternalCellsDrawableNumbers[i]].setBackgroundColor(0);
					mExternalCellsDrawableNumbers = null;
					mExternalDaysNumbersDrawable = null;
				}
				if (mExternalCellsColorNumbers != null)
				{
					for (int i = 0; i < mExternalCellsColorNumbers.length; i++) mCellViews[mExternalCellsColorNumbers[i]].setBackgroundColor(0);
					mExternalCellsColorNumbers = null;
					mExternalDaysNumbersColor = null;
				}			
				Calendar calendar = Calendar.getInstance();
				/* ������� ��� ���������� ���
				 * � ���� ��� �������������, �.�. ��� �������� � selectCell
				 * if (mSelectedDayCellNumberSaver >= 0) mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(0);
				 */			
				mCurrentTimeMonth = calendar.get(Calendar.MONTH) + 1;
				mCurrentTimeYear = calendar.get(Calendar.YEAR);
				mCurrentTimeDayNumber = calendar.get(Calendar.DAY_OF_MONTH);
				//������ ��������� � ���������� ������� � ����� � ������������� ������ ����� ������
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month - 1);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				//���������� ������� ���� � ������.
				mMaxDayNumberOfCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				//���� � ���������� ������ ��� �����, ������� ����� ��������, �� �������� ������ � ������ 1.
				if (day > mMaxDayNumberOfCurrentMonth) day = 1;
				int dayOfWeekForFirstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
				dayOfWeekForFirstDayOfMonth = dayOfWeekForFirstDayOfMonth == 1 ? 7 : dayOfWeekForFirstDayOfMonth - 1;
				//���������� ����� ImageView, ��������������� ������� ��� ������
				mCellNumberForFirstDayOfCurrentMonth = dayOfWeekForFirstDayOfMonth - 1;
				//���� ������� ������� ����� � ���, �� ���������, � ����� ��������� ����� ������, ��������������� �������� ��� � ����� �� ���� ������������ ���.
				//���� � mSelectedDayNumberSaver �����������, ��� ���������� ����������� ����, �� ���� � selectCell ������ ������������ � ���� ���������� ���.
				if (mCurrentTimeMonth == month && mCurrentTimeYear == year)
				{
					//����� getViewIndexByDayNumber ������ ���������� ������ ����� ����, ��� ���������� mCellNumberForFirstDayOfCurrentMonth � mMaxDayNumberOfCurrentMonth
					mCurrentDayCellNumberSaver = getViewIndexByDayNumber(mCurrentTimeDayNumber);
					//mCellViews[mCurrentDayCellNumberSaver].setBackgroundColor(CURRENT_DAY_COLOR);
					mCellViews[mCurrentDayCellNumberSaver].setBackgroundDrawable(CURRENT_DAY_DRAWABLE);
				}
				//��������� ����� ImageView, ��������������� ���������� ��� mSelectedDayNumber
				//����� getViewIndexByDayNumber ������ ���������� ������ ����� ����, ��� ���������� mCellNumberForFirstDayOfCurrentMonth � mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(day);				
				//����� �� �� ����������� ���������� cellNumber mSelectedDayCellNumberSaver'�, �.�. ����� selectCell �� ����� ���������� ��������� ����
				//����� selectCell ���������� ��������� ���� � ���������� ������� ��������� ����.
				//����� selectCell ������ ��������� ����� ����������� mCurrentDayCellNumberSaver
				selectCell(cellNumber, day);
				if (isNewMonth)
				{
					//���������� ��������� ����� � ����.
					String titleText = getResources().getStringArray(R.array.month_list)[month - 1] + " " + Integer.toString(year);
					TextDrawable titleDrawable = new TextDrawable(titleText, TITLE_TEXT_COLOR, mHeight / TITLE_TEXT_SIZE_RATIO);
					titleDrawable.setBounds(0, 0, mTitleWidth, mTitleHeight);
					mTitleView.setBackgroundDrawable(titleDrawable);
					//������� ����������� ����� ��� ������, ������� ���� ������ ������� ����� ������
					for (int i = 0; i < mCellNumberForFirstDayOfCurrentMonth; i++) mCellViews[i].setImageDrawable(null);
					//���������� ����� ��� ������� ����������� ������.
					int firstSunday = 8 - dayOfWeekForFirstDayOfMonth;
					int dayTextColor;
					GridCellDrawable cellDrawable;
					cellNumber = mCellNumberForFirstDayOfCurrentMonth;
					//����� ����� ��� � ������ ������.
					for (int i = 1; i <= mMaxDayNumberOfCurrentMonth; i++)
					{			
						//���������� ���� ������ ����� ������ � ����������� �� ���� �������� �� ����, ��������������� ����� ����� ������������
						dayTextColor = (i - firstSunday) % 7 == 0 ? SUNDAY_TEXT_COLOR : WORKDAY_TEXT_COLOR;
						cellDrawable = new GridCellDrawable(i, dayTextColor, mHeight / CELL_TEXT_SIZE_RATIO);
						cellDrawable.setBounds(0, 0, mCellWidth, mCellHeight);						
						mCellViews[cellNumber].setImageDrawable(cellDrawable);
						cellNumber++;
					}
					//������� ����������� ����� ��� ������, ������� ���� ����� ���������� ����� ������		
					for (int i = cellNumber; i < CELLS_NUMBER; i++) mCellViews[i].setImageDrawable(null);
				}
				//for (int i = 0; i < CELLS_NUMBER; i++) mCellViews[i].setBackgroundColor(0xFFFF0000); //������ ��� ������ �������� (��� �������)
			}
		}
		else
		{
			mStartFillCalendarInOnMeasure = true;
		}
	}	
	//����� � ��� ������������� ������� ������ ���������� ����� ����� ������ fillCalendarGrid
	//���� ����� ������ ��� ������� ���� ������� ��������� ��� � ������� ����� ��� � ������� Drawable, �� ����������� ��� �����, ������� ������������� �������� ��� ��� ���������� ���
	public void setCellsBackground(int[] days, int[] colors)
	{		
		if (mStartSetCellsBackgroundColorsInOnMeasure)
		{
			mStartSetCellsBackgroundColorsInOnMeasure = false;
		}
		else
		{
			mExternalDaysNumbersColor = days;
			mExternalCellsColors = colors;
		}
		if (mIsMeasured)
		{
			if (mCellNumberForFirstDayOfCurrentMonth < 0 || mMaxDayNumberOfCurrentMonth < 0 || days.length != colors.length) return;			
			int n = days.length;
			mExternalCellsColorNumbers = new int[n];
			for (int i = 0; i < n; i++)
			{
				//����� getViewIndexByDayNumber ������ ���������� ������ ����� ����, ��� ���������� mCellNumberForFirstDayOfCurrentMonth � mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(days[i]);
				//���� � �� ���� 29 �������, �� � ������������ ���� cellNumber ����� -1, ������� ���������� �� 29 ������� � �� ���� ��������.
				if (cellNumber < 0) return;
				mExternalCellsColorNumbers[i] = cellNumber;
				//���������� ���, ������ � ��� ������ ���� ����, �� ������� �� ������ ���������� ��� �� ����� �������� ��� � ���������� ���
				if (!(days[i] == mCurrentTimeDayNumber && mCurrentMonth == mCurrentTimeMonth && mCurrentYear == mCurrentTimeYear) && days[i] != mSelectedDayNumber) mCellViews[cellNumber].setBackgroundColor(colors[i]);
			}
		}
		else
		{
			mStartSetCellsBackgroundColorsInOnMeasure = true;
		}
	}
	public void setCellsBackground(int[] days, Drawable[] drawables)
	{
		
		if (mStartSetCellsBackgroundDrawablesInOnMeasure)
		{
			mStartSetCellsBackgroundDrawablesInOnMeasure = false;
		}
		else
		{
			mExternalDaysNumbersDrawable = days;
			mExternalCellsDrawables = drawables;
		}
		if (mIsMeasured)
		{
			if (mCellNumberForFirstDayOfCurrentMonth < 0 || mMaxDayNumberOfCurrentMonth < 0 || days.length != drawables.length) return;
			int n = days.length;
			mExternalCellsDrawableNumbers = new int[n];
			for (int i = 0; i < n; i++)
			{
				//����� getViewIndexByDayNumber ������ ���������� ������ ����� ����, ��� ���������� mCellNumberForFirstDayOfCurrentMonth � mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(days[i]);
				//���� � �� ���� 29 �������, �� � ������������ ���� cellNumber ����� -1, ������� ���������� �� 29 ������� � �� ���� ��������.
				if (cellNumber < 0) return;
				mExternalCellsDrawableNumbers[i] = cellNumber;
				//���������� ���, ������ � ��� ������ ���� ����, �� ������� �� ������ ���������� ��� �� ����� �������� ��� � ���������� ���	
				if (!(days[i] == mCurrentTimeDayNumber && mCurrentMonth == mCurrentTimeMonth && mCurrentYear == mCurrentTimeYear) && days[i] != mSelectedDayNumber) mCellViews[cellNumber].setBackgroundDrawable(drawables[i]);
			}
		}
		else
		{
			mStartSetCellsBackgroundDrawablesInOnMeasure = true;
		}
	}
	//���������� ������� ����.
	public int getCurrentTimeDayNumber()
	{
		return mCurrentTimeDayNumber;		
	}
	public int getCurrentTimeMonth()
	{
		return mCurrentTimeMonth;
	}
	public int getCurrentTimeYear()
	{
		return mCurrentTimeYear;
	}
	//������ � ������� � ���������� ����� ��������� �� ����!
	private int getViewIndexByRowAndColumn(int row, int col)
	{
		return COLUMNS_NUMBER * row + col;
	}
	private void registerCellClickListeners()
	{
		View.OnClickListener onClickListener = new View.OnClickListener()
		{		
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				int id = v.getId();
				int dayOfMonth = getDayNumberByViewIndex(id);
				selectCell(id, dayOfMonth);
			}
		};
		//������������� ��������� �� ��� ���� ImageView
		for (int i = 0; i < CELLS_NUMBER; i++) mCellViews[i].setOnClickListener(onClickListener);
	}
	private void registerChangeMonthButtonsClickListeners()
	{
		View.OnClickListener onClickListener = new View.OnClickListener()
		{			
			public void onClick(View v)
			{
				// TODO Auto-generated method stub
				int id = v.getId();
				int newMonth = -1;
				int newYear = -1;
				switch (id)
				{
					case PREVIOUS_MONTH_BUTTON_ID:
						if (mCurrentMonth == 1)
						{
							newMonth = 12;
							newYear = mCurrentYear - 1;
						}
						else
						{
							newMonth = mCurrentMonth - 1;
							newYear = mCurrentYear;
						}
					break;	
					case NEXT_MONTH_BUTTON_ID:
						if (mCurrentMonth == 12)
						{
							newMonth = 1;
							newYear = mCurrentYear + 1;
						}
						else
						{
							newMonth = mCurrentMonth + 1;
							newYear = mCurrentYear;
						}
					break;
				}
				fillCalendarGrid(newYear, newMonth, mSelectedDayNumber);
			}
		};
		mPreviousMonthButton.setOnClickListener(onClickListener);
		mNextMonthButton.setOnClickListener(onClickListener);
	}
	//������ ��������� ������ � ������� �������, � ����� ���������� ������� ������� ���� �� ���������� ����.
	private void selectCell(int number, int dayOfMonth)
	{		
		if (dayOfMonth > 0)
		{			
			//���� �� ������� �� ������ ��� ���� ������� ����� ������ ������, �� � ���� ���������� ������ ���� ������ ���������.
			if (mSelectedDayCellNumberSaver >= 0)
			{
				//���� �� ����� ������, ������� ���� ������� � ������� ��� ���� ������ � ������ �������� �� ���, �� ��������, ��� ��� �� ����
				int externalColor = findNumberInArray(mSelectedDayCellNumberSaver, mExternalCellsColorNumbers);
				int externalDrawable = findNumberInArray(mSelectedDayCellNumberSaver, mExternalCellsDrawableNumbers);
				//���� ���������� ������ ��������� � ������� ���, �� ���� ���� ������ �� ������ ������ ���������, � ������ �������������� ���� ������ �������� ���.
				if (mSelectedDayCellNumberSaver == mCurrentDayCellNumberSaver && mCurrentMonth == mCurrentTimeMonth && mCurrentYear == mCurrentTimeYear)
				{
					//mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(CURRENT_DAY_COLOR);
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundDrawable(CURRENT_DAY_DRAWABLE);
				}
				//���� �� ���������� ������ ��������� � ���, ���� �������� ������������ �� ���, �� ������ ��������� ���� ����
				//���� �� ���� � �� �� ������ ����� � color � Drawable, �� ��� ����� ��������� ����� color
				else if (externalColor >= 0)
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(mExternalCellsColors[externalColor]);
				}					
				else if (externalDrawable >= 0)
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundDrawable(mExternalCellsDrawables[externalDrawable]);
				}
				//�� ���� ��������� ������� ������ ������� ���� ���������� ������
				else
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(0);
				}
			}
			//���������� ����� ��������� ������, ����� ������� � ���� ��� ��������� ������� �� ������ ������ (���� ����� �� ��� ��, �� ���� �����������)
			mSelectedDayCellNumberSaver = number;
			//��������� ����� ���������� ���, ����� ������� ��� ��� ��� ��� �������� �� ��������� ����� � fillCalendarGrid
			mSelectedDayNumber = dayOfMonth;
			//������������� ���� ��������� ������.
			mCellViews[number].setBackgroundColor(SELECTED_DAY_COLOR);
			//���������� ������� ��������� ����
			if (mOnDateChangeListener != null) mOnDateChangeListener.onDateChanged(mCurrentYear, mCurrentMonth, dayOfMonth);
		}
	}
	//���������� ������ ������ ������� ������������� ����� number.
	//���� � ������� ���������� ��������� number, �� ������������ ��������� ������. ��� ���������� ��� ���������� ��������� ����������� ����������, �.�. ��� � ������� ������������� ����� �������, ��� ���� � ����������� type �� �� ��������� � ����� �������
	private int findNumberInArray(int number, int[] array)
	{
		if (array == null) return -1;
		int index = -1;
		for (int i = 0; i < array.length; i++)
		{
			if (number == array[i]) index = i;
		}
		return index;
	}
	//�������� ������ ImageView �� ������ ��� ������
	private int getViewIndexByDayNumber(int dayNumber)
	{
		final int INVALID_DAY_NUMBER = -1; 
		if (mCellNumberForFirstDayOfCurrentMonth >= 0 && mMaxDayNumberOfCurrentMonth > 0)
		{
			if (dayNumber > mMaxDayNumberOfCurrentMonth)
			{
				return INVALID_DAY_NUMBER;
			}
			else
			{
				return dayNumber + mCellNumberForFirstDayOfCurrentMonth - 1;
			}
		}
		else
		{
			return INVALID_DAY_NUMBER;
		}
	}
	//�������� ����� ��� ������ �� ������� ImageView
	private int getDayNumberByViewIndex(int index)
	{
		final int INVALID_DAY_NUMBER = -1; 
		if (mCellNumberForFirstDayOfCurrentMonth >= 0 && mMaxDayNumberOfCurrentMonth > 0)
		{
			int dayNumber = index - mCellNumberForFirstDayOfCurrentMonth + 1;
			if (dayNumber > mMaxDayNumberOfCurrentMonth)
			{
				return INVALID_DAY_NUMBER;
			}
			else
			{
				return dayNumber;
			}
		}
		else
		{
			return INVALID_DAY_NUMBER;
		}
	}
}