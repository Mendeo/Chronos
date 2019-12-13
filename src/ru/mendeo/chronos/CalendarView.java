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
	//Размеры календарной сетки для среднего экрана (160 dpi).
	//private static final int GRID_WIDTH_DIP = 291;
	//private static final int GRID_HEIGHT_DIP = 207;
	//Отношение ширины (X) и высоты (Y) дочерних ImageView к расстоянию между ними. 
	private static final float X_CELL_RATIO = 12f;
	private static final float Y_CELL_RATIO = 6.5f;
	//Отношение ширины (X) и высоты (Y) всего нашего View к ширине или высоте непосредственно календарной сетки.
	private static final float X_GRID_RATIO = 1.02f;
	private static final float Y_GRID_RATIO = 1.43f;
	//Отношение высоты всего View к расстоянию от низа View до календарной сетки.
	private static final float BOTTOM_GRID_RATIO = 69f;
	//Отноешение ширины и высоты всего View к ширине и высоте заголовка с названием месяца и года
	private static final float X_TITLE_RATIO = 1.5f;
	private static final float Y_TITLE_RATIO = 6f;
	//Отношение всего View к расстояния от верхнего края View до заголовка с названием месяца и года
	private static final float TOP_TITLE_RATIO = 1000f;
	//Отношение ширины View к расстоянию от кнопок переключения месяца до кромки View
	private static final float LEFT_CHANGE_MONTH_BUTTON_RATIO = 20f;
	//Отношение высоты View к расстония от верха View до кнопок переключения месяца до кромки View
	private static final float TOP_CHANGE_MONTH_BUTTON_RATIO = 86f;
	//Цвета получим из ресурсов в методе init()
	//private static final int CURRENT_DAY_COLOR = 0xFF306898;
	private static Drawable CURRENT_DAY_DRAWABLE;
	private static int SELECTED_DAY_COLOR; //= 0xFF777878;
	private static int TITLE_TEXT_COLOR; //= 0xFFFFFFFF;
	private static int SUNDAY_TEXT_COLOR; //= 0xFFF84B63;
	private static int WORKDAY_TEXT_COLOR;//= 0xFFFFFFFF;
	//Отношение высоты всего View к размеру текста для заголовка с названием месяца и года
	private static final float TITLE_TEXT_SIZE_RATIO = 8.28f;
	//Отношение высоты всего View к размеру текста для отображения чисел на календарной сетке	
	private static final float CELL_TEXT_SIZE_RATIO = 12.9375f;
	private static final int PREVIOUS_MONTH_BUTTON_ID = 102043;
	private static final int NEXT_MONTH_BUTTON_ID = 3849;
	//private static final int CELL_ID_INCREMENT = 1000; //Нужно для того, чтобы id элементов ячеек начинались не с единицы.
	private ImageView[] mCellViews;
	private View mTitleView;
	private ImageView mPreviousMonthButton, mNextMonthButton;
	private int mWidth, mHeight, mCellWidth, mCellHeight, mDx, mDy, mTitleWidth, mTitleHeight, mChangeMonthButtonWidth, mChangeMonthButtonHeight;
	//Храним номер ячейки, соответствующей текущему дню.
	private int mCurrentDayCellNumberSaver = -1;
	//Месяц и год в данный момент отображаемые каленадрём
	private int mCurrentMonth = -1;
	private int mCurrentYear = -1;
	//День, месяц и год текущие по часам.
	private int mCurrentTimeMonth = -1;
	private int mCurrentTimeYear = -1;
	private int mCurrentTimeDayNumber = -1;
	//Храним номер ячейки, соответствующей первому числу установленного месяца.
	private int mCellNumberForFirstDayOfCurrentMonth = -1;
	//Храним максимальное число установленного месяца.
	private int mMaxDayNumberOfCurrentMonth = -1;
	//Храним номер ячейки, соответствующей выбраному числу установленного месяца.
	private int mSelectedDayCellNumberSaver = -1;
	//Храним число выбранного дня установленного месяца.
	private int mSelectedDayNumber = -1;
	//Массив, хранящий номера ячеек, фон которых был изменён из вне
	private int[] mExternalCellsColorNumbers = null;
	private int[] mExternalDaysNumbersColor = null;
	private int[] mExternalDaysNumbersDrawable = null;
	private int[] mExternalCellsDrawableNumbers = null;
	private int[] mExternalCellsColors = null;
	private Drawable[] mExternalCellsDrawables = null;
	//Переменные, необходимые для отрисовки календарной сетки только после того, как был выполнен метод onMeasure, т.е. наше View себя померяла
	//Определяет был ли запущен onMeasure
	private boolean mIsMeasured = false;
	//Определяют нужно ли запускать из метода onMeasure соответствующие методы. Эти методы устроены так, что входные параметры сохраняются в глобальных переменных и при вызове из onMeasure эти переменные не обновляются
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
		//Позволяем View самой определить свои размеры. (Она их берёт из xml, но wrap_content работать не будет):
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//Получаем размеры, которая определила для себя наша View:
		mWidth = getMeasuredWidth();
		mHeight = getMeasuredHeight();
		//Позволяем кнопкам самими расчитать свои размеры. Они расчитываются исзодя из LayoutParams, переданного в наши кнопки выше.
		mChangeMonthButtonWidth = 0;
		mChangeMonthButtonHeight = 0;
		mNextMonthButton.measure(MeasureSpec.makeMeasureSpec(mChangeMonthButtonWidth, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(mChangeMonthButtonHeight, MeasureSpec.UNSPECIFIED));
		mPreviousMonthButton.measure(MeasureSpec.makeMeasureSpec(mChangeMonthButtonWidth, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(mChangeMonthButtonHeight, MeasureSpec.UNSPECIFIED));
		//Получаем фактический размер кнопок изменения месяца, которые они для себя расчитали.
		mChangeMonthButtonWidth = mPreviousMonthButton.getMeasuredWidth();
		mChangeMonthButtonHeight = mPreviousMonthButton.getMeasuredHeight();
		//Вычисляем размеры заголовка с названием месяца и года
		mTitleWidth = (int)Math.round(((float)mWidth / X_TITLE_RATIO));
		mTitleHeight = (int)Math.round(((float)mHeight / Y_TITLE_RATIO));
		//Задаём размер для заголовка
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
		//Задаём размеры для всех ячеек
		for (int i = 0; i < CELLS_NUMBER; i++) mCellViews[i].measure(cellWidthMeasureSpec, cellHeightMeasureSpec);
		//Говорим, что наш View измерен
		mIsMeasured = true;
		//Если заполнение календаря было вызвано из вне до измерения View, то метод fillGridCalendar перенапрявляет вызов сюда при помощи mStartFillCalendarCoreInOnMeasure
		if (mStartFillCalendarInOnMeasure) fillCalendarGrid(mCurrentYear, mCurrentMonth, mSelectedDayNumber);
		if (mStartSetCellsBackgroundColorsInOnMeasure) setCellsBackground(mExternalDaysNumbersColor, mExternalCellsColors);
		if (mStartSetCellsBackgroundDrawablesInOnMeasure) setCellsBackground(mExternalDaysNumbersDrawable, mExternalCellsDrawables);
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int left, top, right, bottom;
		//Размещаем кнопки изменения месяца
		left = (int)((float)mWidth / LEFT_CHANGE_MONTH_BUTTON_RATIO);
		top = (int)((float)mWidth / TOP_CHANGE_MONTH_BUTTON_RATIO);
		right = left + mChangeMonthButtonWidth;
		bottom = top + mChangeMonthButtonHeight;
		mPreviousMonthButton.layout(left, top, right, bottom);
		right = mWidth - left;
		left = right - mChangeMonthButtonWidth;
		mNextMonthButton.layout(left, top, right, bottom);
		//Раземещаем заголовок с названием месяца и года по гоотзонтали - по середине View, по вертикали - исходя из		
		left = (mWidth - mTitleWidth) / 2;
		top = (int)((float)mHeight / TOP_TITLE_RATIO);
		right = left + mTitleWidth;
		bottom = top + mTitleHeight;
		mTitleView.layout(left, top, right, bottom);
		//Вычисляем шаг, на который будут сдвигаться ячейки при размещении
		int stepX = mDx + mCellWidth;
		int stepY = mDy + mCellHeight;
		//Вычисляем расстояние от верха и от левой стороны до начала календарной сетки.
		//Считаем, что от низу будет расстояние исходя из BOTTOM_GRID_RATIO, а по горизонтали отцентруем
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
	//Запускаем fillCalendarGrid, но только если наше View уже было измерено
	//Этот метод вычисляет на каких ячейках какое ставить число, а так же определяем цвет ячеек, пишем заголовок и т.д.
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
				//Убираем фон текущего дня. Если передан текущий месяц, то этот фон восстановится ниже, если нет, то просто исчезнет
				if (mCurrentDayCellNumberSaver >= 0)
				{
					mCellViews[mCurrentDayCellNumberSaver].setBackgroundColor(0);
					mCurrentDayCellNumberSaver = -1;
				}
				//Убираем фон у ячеек, фон которых был изменён из вне.
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
				/* Убираем фон выбранного дня
				 * В этом нет необходимости, т.к. это делается в selectCell
				 * if (mSelectedDayCellNumberSaver >= 0) mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(0);
				 */			
				mCurrentTimeMonth = calendar.get(Calendar.MONTH) + 1;
				mCurrentTimeYear = calendar.get(Calendar.YEAR);
				mCurrentTimeDayNumber = calendar.get(Calendar.DAY_OF_MONTH);
				//Создаём календарь с переданным месяцем и годом и устанавливаем первое число месяца
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month - 1);
				calendar.set(Calendar.DAY_OF_MONTH, 1);
				//Определяем сколько дней в месяце.
				mMaxDayNumberOfCurrentMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
				//Если в переданном месяце нет числа, которое нужно выделить, то выделяем ячейку с числом 1.
				if (day > mMaxDayNumberOfCurrentMonth) day = 1;
				int dayOfWeekForFirstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
				dayOfWeekForFirstDayOfMonth = dayOfWeekForFirstDayOfMonth == 1 ? 7 : dayOfWeekForFirstDayOfMonth - 1;
				//Определяем номер ImageView, соответствующий первому дню месяца
				mCellNumberForFirstDayOfCurrentMonth = dayOfWeekForFirstDayOfMonth - 1;
				//Если передан текущий месяц и год, то вычисляем, а затем сохраняем номер ячейки, соответствующие текущему дню и задаём ей цвет сегодняшнего дня.
				//Если в mSelectedDayNumberSaver установлено, что выбирается сегодняшний день, то ниже в selectCell ячейка окрашивается в цвет выбранного дня.
				if (mCurrentTimeMonth == month && mCurrentTimeYear == year)
				{
					//Метод getViewIndexByDayNumber должен вызываться только после того, как определены mCellNumberForFirstDayOfCurrentMonth и mMaxDayNumberOfCurrentMonth
					mCurrentDayCellNumberSaver = getViewIndexByDayNumber(mCurrentTimeDayNumber);
					//mCellViews[mCurrentDayCellNumberSaver].setBackgroundColor(CURRENT_DAY_COLOR);
					mCellViews[mCurrentDayCellNumberSaver].setBackgroundDrawable(CURRENT_DAY_DRAWABLE);
				}
				//Вычисляем номер ImageView, соответствующей выбранному дню mSelectedDayNumber
				//Метод getViewIndexByDayNumber должен вызываться только после того, как определены mCellNumberForFirstDayOfCurrentMonth и mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(day);				
				//Здесь мы не присваиваем полученный cellNumber mSelectedDayCellNumberSaver'у, т.к. тогда selectCell не сотрёт предыдущий выбранный день
				//Метод selectCell отображает выбранный день и генерирует событие изменения даты.
				//Метод selectCell должен находится после определения mCurrentDayCellNumberSaver
				selectCell(cellNumber, day);
				if (isNewMonth)
				{
					//Отображаем заголовок месяц и дату.
					String titleText = getResources().getStringArray(R.array.month_list)[month - 1] + " " + Integer.toString(year);
					TextDrawable titleDrawable = new TextDrawable(titleText, TITLE_TEXT_COLOR, mHeight / TITLE_TEXT_SIZE_RATIO);
					titleDrawable.setBounds(0, 0, mTitleWidth, mTitleHeight);
					mTitleView.setBackgroundDrawable(titleDrawable);
					//Очищаем календарную сетку для клеток, которые идут раньше первого числа месяца
					for (int i = 0; i < mCellNumberForFirstDayOfCurrentMonth; i++) mCellViews[i].setImageDrawable(null);
					//Определяем число дня первого воскресенья месяца.
					int firstSunday = 8 - dayOfWeekForFirstDayOfMonth;
					int dayTextColor;
					GridCellDrawable cellDrawable;
					cellNumber = mCellNumberForFirstDayOfCurrentMonth;
					//Пишем номер дня в каждую ячейку.
					for (int i = 1; i <= mMaxDayNumberOfCurrentMonth; i++)
					{			
						//Выставляем цвет текста числа месяца в зависимости от того является ли день, соответствующий этому числу воскресеньем
						dayTextColor = (i - firstSunday) % 7 == 0 ? SUNDAY_TEXT_COLOR : WORKDAY_TEXT_COLOR;
						cellDrawable = new GridCellDrawable(i, dayTextColor, mHeight / CELL_TEXT_SIZE_RATIO);
						cellDrawable.setBounds(0, 0, mCellWidth, mCellHeight);						
						mCellViews[cellNumber].setImageDrawable(cellDrawable);
						cellNumber++;
					}
					//Очищаем календарную сетку для клеток, которые идут позже последнего числа месяца		
					for (int i = cellNumber; i < CELLS_NUMBER; i++) mCellViews[i].setImageDrawable(null);
				}
				//for (int i = 0; i < CELLS_NUMBER; i++) mCellViews[i].setBackgroundColor(0xFFFF0000); //Делает все ячейки красными (для отладки)
			}
		}
		else
		{
			mStartFillCalendarInOnMeasure = true;
		}
	}	
	//Метод и его перегруженный товарищ должен вызываться извне после вызова fillCalendarGrid
	//Этот метод служит для задания фона ячейкам календаря или с помощью цвета или с помощью Drawable, за исключением тех ячеек, которые соответствуют текущему дню или выбранному дню
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
				//Метод getViewIndexByDayNumber должен вызываться только после того, как определены mCellNumberForFirstDayOfCurrentMonth и mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(days[i]);
				//Если в БД есть 29 февраля, то в невисокосные годы cellNumber будет -1, поэтому праздников на 29 февраля в БД быть недолжно.
				if (cellNumber < 0) return;
				mExternalCellsColorNumbers[i] = cellNumber;
				//Отображаем фон, только в том случае если день, на котором мы должны отобразить фон не равен текущему дню и выбранному дню
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
				//Метод getViewIndexByDayNumber должен вызываться только после того, как определены mCellNumberForFirstDayOfCurrentMonth и mMaxDayNumberOfCurrentMonth
				int cellNumber = getViewIndexByDayNumber(days[i]);
				//Если в БД есть 29 февраля, то в невисокосные годы cellNumber будет -1, поэтому праздников на 29 февраля в БД быть недолжно.
				if (cellNumber < 0) return;
				mExternalCellsDrawableNumbers[i] = cellNumber;
				//Отображаем фон, только в том случае если день, на котором мы должны отобразить фон не равен текущему дню и выбранному дню	
				if (!(days[i] == mCurrentTimeDayNumber && mCurrentMonth == mCurrentTimeMonth && mCurrentYear == mCurrentTimeYear) && days[i] != mSelectedDayNumber) mCellViews[cellNumber].setBackgroundDrawable(drawables[i]);
			}
		}
		else
		{
			mStartSetCellsBackgroundDrawablesInOnMeasure = true;
		}
	}
	//Возвращает текущий день.
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
	//Строки и столбцы и порядковый номер считаются от нуля!
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
		//Устанавливаем слушателя на все наши ImageView
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
	//Делаем выбранной ячейку с заданым номером, а затем генерируем событие измения даты на переданный день.
	private void selectCell(int number, int dayOfMonth)
	{		
		if (dayOfMonth > 0)
		{			
			//Если до нажатия на ячейку уже была выбрана ранее другая ячейка, то у этой предыдущей ячейки цвет должен исчезнуть.
			if (mSelectedDayCellNumberSaver >= 0)
			{
				//Если на месте ячейки, которая была выбрана в прошлый раз была ячейка с цветом заданном из вне, то получаем, что это за цвет
				int externalColor = findNumberInArray(mSelectedDayCellNumberSaver, mExternalCellsColorNumbers);
				int externalDrawable = findNumberInArray(mSelectedDayCellNumberSaver, mExternalCellsDrawableNumbers);
				//Если предыдущая ячейка совпадала с текущем днём, то цвет этой ячейки не просто должен исчезнуть, а должен восстановиться цвет ячейки текущего дня.
				if (mSelectedDayCellNumberSaver == mCurrentDayCellNumberSaver && mCurrentMonth == mCurrentTimeMonth && mCurrentYear == mCurrentTimeYear)
				{
					//mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(CURRENT_DAY_COLOR);
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundDrawable(CURRENT_DAY_DRAWABLE);
				}
				//Если же предыдущая ячейка совпадала с днём, цвет которого определяется из вне, то должен вернуться этот цвет
				//Если на одну и ту же ячейку задан и color и Drawable, то как видно приоритет имеет color
				else if (externalColor >= 0)
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(mExternalCellsColors[externalColor]);
				}					
				else if (externalDrawable >= 0)
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundDrawable(mExternalCellsDrawables[externalDrawable]);
				}
				//Во всех остальных случаях просто стираем цвет предыдущей ячейки
				else
				{
					mCellViews[mSelectedDayCellNumberSaver].setBackgroundColor(0);
				}
			}
			//Сохранаяем номер выбранной ячейки, чтобы стереть её цвет при следующим нажатии на другую ячейку (если нажмём на эту же, то цвет перепишется)
			mSelectedDayCellNumberSaver = number;
			//Сохраняем номер выбранного дня, чтобы выбрать его ещё раз при переходе на следующий месяц в fillCalendarGrid
			mSelectedDayNumber = dayOfMonth;
			//Устанавливаем цвет выбранной ячейки.
			mCellViews[number].setBackgroundColor(SELECTED_DAY_COLOR);
			//Генерируем событие изменения даты
			if (mOnDateChangeListener != null) mOnDateChangeListener.onDateChanged(mCurrentYear, mCurrentMonth, dayOfMonth);
		}
	}
	//Возвращает индекс индекс массива принадлежащий числу number.
	//Если в массиве содеожится несколько number, то возвращается ПОСЛЕДНИЙ индекс. Это необходимо для корректной обработки отображения праздников, т.к. дни в массиве отсортированы таким образом, что день с минимальным type из БД находится в конце массива
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
	//Получаем индекс ImageView по номеру дня месяца
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
	//Получаем номер дня месяца по индексу ImageView
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