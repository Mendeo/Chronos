package ru.mendeo.chronos;
/*
 * Переделано Меняйло А.Н. в апреле 2012 года.
 * Добавлена дополнительная функциональность, чтобы была возможность использовать ScrollView внутри каждого экрана.
 * При этом когда пользователь скрулит содержимое экрана (работает дочерний ScrollView), то он не может листать экраны.
 * И наоборот, когда пользователь листает экраны, то он не может скрулить.
 * Улучшена производительность. Добавлена функциональность.
 */
/* Шпаргалка:
  	Each touch event follows the pattern of (simplified example):
    Activity.dispatchTouchEvent()
    ViewGroup.dispatchTouchEvent()
    View.dispatchTouchEvent()
    View.onTouchEvent()
    ViewGroup.onTouchEvent()
    Activity.onTouchEvent()
 */
/*
 * Copyright (C) 2010 Marc Reichelt
 * 
 * Work derived from Workspace.java of the Launcher application
 *  see http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * RealViewSwitcher allows users to switch between multiple screens (layouts) in the same way as the Android home screen (Launcher application).
 * <p>
 * You can add and remove views using the normal methods {@link ViewGroup#addView(View)}, {@link ViewGroup#removeView(View)} etc. You may want to listen for updates by calling {@link RealViewSwitcher#setOnScreenSwitchListener(OnScreenSwitchListener)}
 * in order to perform operations once a new screen has been selected.
 * 
 * @author Marc Reichelt, <a href="http://www.marcreichelt.de/">http://www.marcreichelt.de/</a>
 * @version 0.1.0
 */
public class SlidingView extends ViewGroup
{

	// TODO: This class does the basic stuff right now, but it would be cool to have certain things implemented,
	// e.g. using an adapter for getting views instead of setting them directly, memory management and the
	// possibility of scrolling vertically instead of horizontally. If you have ideas or patches, please visit
	// my website and drop me a mail. :-)

	/**
	 * Listener for the event that the RealViewSwitcher switches to a new view.
	 */
	public static interface OnScreenSwitchListener
	{

		/**
		 * Notifies listeners about the new screen. Runs after the animation completed.
		 * 
		 * @param screen The new screen index.
		 */
		void onScreenSwitched(int screen);

	}

	private static final int INVALID_SCREEN = -1;
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private float mLastMotionX, mLastMotionY;
	private int mTouchSlop;
	private int mMaximumVelocity, mMinimumVelocity;
	private int mCurrentScreen = 0;
	private int mNextScreen = INVALID_SCREEN;
	private boolean mFirstLayout = true;
	private boolean mIsScrollX = false;
	private boolean mIsScrollY = false;
	private boolean mHasVerticalScrollableChilds = true;
	private boolean mStopTouchEventDispatching = false;
	private OnScreenSwitchListener mOnScreenSwitchListener = null;

	public SlidingView(Context context)
	{
		super(context);
		init();
	}
	public SlidingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}
	public SlidingView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}
	private void init()
	{
		mScroller = new Scroller(getContext());
		final ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mTouchSlop = configuration.getScaledTouchSlop();
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		//Позволяем View самой определить свои размеры. (Она их берёт из xml, но wrap_content работать не будет):
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//Получаем размеры, которая определила для себя наша View:
		final int width = getMeasuredWidth();
		final int height = getMeasuredHeight();
		//Говорим, что дети должны иметь ровно такой же размер как и наше View:
		int ChildWidth = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
		int ChildHeight = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
		final int count = getChildCount();
		//Ну и в цикле задаём размер детей. Но только тех, которые сейчас видимы, чтоб лишний раз цикл не гонять.
		for (int i = 0; i < count; i++)
		{
			final View child = getChildAt(i);
			if (child.getVisibility() == View.GONE) continue;
			child.measure(ChildWidth, ChildHeight);
		}
		if (mFirstLayout)
		{
			scrollTo(mCurrentScreen * width, 0);
			mFirstLayout = false;
		}
	}
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b)
	{
		int childLeft = 0;
		int childTop = 0;
		final int count = getChildCount();
		for (int i = 0; i < count; i++)
		{
			final View child = getChildAt(i);
			if (child.getVisibility() == View.GONE) continue;
			final int childWidth = child.getMeasuredWidth();
			final int childHeight = child.getMeasuredHeight();
			int childRight = childLeft + childWidth;
			int childBottom = childHeight; 
			child.layout(childLeft, childTop, childRight, childBottom);
			childLeft += childWidth;
		}
	}
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		if (mStopTouchEventDispatching) return false;
		final float x = ev.getX();
		final float y = ev.getY();
		int action = ev.getAction();
		if (action == MotionEvent.ACTION_CANCEL)
		{
			//Log.d("MyLogD", "dispatchTouchEvent_ACTION_CANCEL");
			return super.dispatchTouchEvent(ev);			
		}
		if (action == MotionEvent.ACTION_UP)
		{		
			//Log.d("MyLogD", "dispatchTouchEvent_ACTION_UP");
			if (!mIsScrollX && !mIsScrollY) //Отпустили палец после нажатия (скрула не было)
			{
				doScrolling(ev);
				return super.dispatchTouchEvent(ev);
			}
			else if (mIsScrollX) //Отпустили палец после скрула по горизонтали
			{
				doScrolling(ev);
				return true;
			}
			else if (mIsScrollY) //Отпустили палец после скрула по вертикали
			{
				return super.dispatchTouchEvent(ev);
			}
		}
		if (action == MotionEvent.ACTION_DOWN)
		{
			//Log.d("MyLogD", "dispatchTouchEvent_ACTION_DOWN");
			mLastMotionX = x;
			mLastMotionY = y;
			mIsScrollX = false;
			mIsScrollY = false;
			//По умолчанию считаем дочернее View способным скрулиться. Это поведение View может отменить в свойстве укп поставив false. Если дочернего View нет, то в onTouchEvent mHasScrollableChilds автоматически сброситься в false. Если дочернее View поглащает событие и до onTouchEvent оно не доходит и дочернее View не установило свойство в false, то считается, что оно может скрулиться.
			mHasVerticalScrollableChilds = true;
			doScrolling(ev);
			return super.dispatchTouchEvent(ev);
		}
		if (mIsScrollX)
		{
			doScrolling(ev);
			return true;
		}
		else if (mIsScrollY && mHasVerticalScrollableChilds)
		{
			return super.dispatchTouchEvent(ev);
		}
		if (action == MotionEvent.ACTION_MOVE)
		{
			//Log.d("MyLogD", "dispatchTouchEvent_ACTION_MOVE");
			final boolean isScrollX = (int)Math.abs(x - mLastMotionX) >= mTouchSlop;
			if (mHasVerticalScrollableChilds)
			{
				final boolean isScrollY = (int)Math.abs(y - mLastMotionY) >= mTouchSlop;
				if (isScrollX && !isScrollY)
				{
					mIsScrollX = true;
				}
				else if (isScrollY && !isScrollX)
				{
					mIsScrollY = true;
				}
				else if (isScrollX && isScrollY)
				{
					/* Здесь выбираем, что будем скролить, если пользователь ведёт пальцем по диагонали.
					 * Но лучше выбрать скролить дочерний ScrollView,
					 * т.к. если пользователь резко захочет прокрутить вниз или вверх содержимое экрана,
					 * то у него не будет такого, что экран резко неожиданно перелистнётся.
					 */					
					mIsScrollY = true;
				}
				else
				{
					return super.dispatchTouchEvent(ev);
				}
			}
			else
			{
				mIsScrollX = isScrollX;
			}
			if (mIsScrollX)
			{
				doScrolling(ev);
				//Отменяем все события, что мы передали дочернему скурулу
				ev.setAction(MotionEvent.ACTION_CANCEL);
				super.dispatchTouchEvent(ev);
				return true;
			}
			else
			{
				return super.dispatchTouchEvent(ev);
			}
		}		
		Log.e(PublicConstantsAndMethods.MY_LOG_TAG, "Error in SlidingView/dispatchTouchEvent");
		return super.dispatchTouchEvent(ev);
	}
	//Метод вызывается дочерними View, чтобы сообщить, что они не скрулятся.
	public void setIAmNotaVerticalScrollableChild()
	{
		mHasVerticalScrollableChilds = false;
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{		
		//onTouchEvent работает, только если клик не попадает на детей нашего SlidingView, поэтому говорим, что прокручивающихся детей нет и корректно отрабатываем ACTION_CANCEL.
		//Log.d("MyLogD", "onTouchEvent " + mHasScrollableChilds);
		mHasVerticalScrollableChilds = false;
		return !(ev.getAction() == MotionEvent.ACTION_CANCEL);
	}
	public void doScrolling(MotionEvent ev)
	{
		float x = ev.getX();
		if (mVelocityTracker == null) mVelocityTracker = VelocityTracker.obtain();
		mVelocityTracker.addMovement(ev);
		switch (ev.getAction())
		{
			case MotionEvent.ACTION_DOWN:
				if (!mScroller.isFinished()) mScroller.abortAnimation();
			break;
			case MotionEvent.ACTION_MOVE:
				final int deltaX = (int)(mLastMotionX - x);
				mLastMotionX = x;
				final int scrollX = getScrollX();
				//Двигаем слева направо.
				if (deltaX <= 0)
				{
					if (scrollX > 0) scrollBy(Math.max(-scrollX, deltaX), 0);
				}
				//Двигаем справа на лево.
				else if (deltaX > 0)
				{
					final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
					if (availableToScroll > 0) scrollBy(Math.min(availableToScroll, deltaX), 0);
				}
			break;
			case MotionEvent.ACTION_UP:
				mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
				int velocityX = (int)mVelocityTracker.getXVelocity();
				if (velocityX > mMinimumVelocity && mCurrentScreen > 0)
				{
					// Fling hard enough to move left
					snapToScreen(mCurrentScreen - 1, true);
				}
				else if (velocityX < -mMinimumVelocity && mCurrentScreen < getChildCount() - 1)
				{
				// Fling hard enough to move right
					snapToScreen(mCurrentScreen + 1, true);
				}
				else
				{
					snapToDestination(false);
				}
				if (mVelocityTracker != null)
				{
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				}
			break;
		}
	}
	private void snapToDestination(boolean isFling)
	{
		final int screenWidth = getWidth();
		final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
		snapToScreen(whichScreen, isFling);
	}
	private void snapToScreen(int whichScreen, boolean isFling)
	{
		if (!mScroller.isFinished()) return;
		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		mNextScreen = whichScreen;
		final int newX = whichScreen * getWidth();
		final int scrollX = getScrollX();
		final int delta = newX - scrollX;
		//Определяем максимальное время скролинга. Оно соответсвует скорости скрулинга в 10 раз быстрее, чем минимальная скорость броска
		int maxDuration = 100 * Math.abs(delta) / mMinimumVelocity;
		int duration;
		if (isFling)
		{
			//Если скорость броска больше, определяемой maxDuration, то скролим со скоростью броска, иначе со скоростью, определяемой maxDuration
			int velocityX = (mVelocityTracker != null) ? (int)mVelocityTracker.getXVelocity() : 0;
			duration = -1000 * delta / velocityX;
			duration = (duration > maxDuration) ? maxDuration : duration;
			mScroller.startScroll(scrollX, 0, delta, 0, duration);
		}
		else
		{
			//Обычный скрулинг осуществляем со скоростью, определяемой maxDuration.
			duration = maxDuration;
			mScroller.startScroll(scrollX, 0, delta, 0, duration);
		}
		invalidate();
	}
	// Как это не банально, но computeScroll() запускается после каждого invalidate() или postInvalidate(), но судя по всему в отдельном потоке.
	@Override
	public void computeScroll()
	{
		if (mScroller.computeScrollOffset())
		{
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
			postInvalidate(); //нужно вызывать postInvalidate (несмотря на scrollTo),  так как computeScroll работает в другом потоке.
		}		
		else if (mNextScreen != INVALID_SCREEN)
		{
			int oldScreen = mCurrentScreen;
			mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
			// notify observer about screen change
			if (mOnScreenSwitchListener != null && mCurrentScreen != oldScreen) mOnScreenSwitchListener.onScreenSwitched(mCurrentScreen);
			mNextScreen = INVALID_SCREEN;
		}
	}
	/**
	 * Returns the index of the currently displayed screen.
	 * 
	 * @return The index of the currently displayed screen.
	 */
	public int getCurrentScreen()
	{
		return mCurrentScreen;
	}
	/**
	 * Sets the current screen.
	 * 
	 * @param currentScreen The new screen.
	 */
	public void setCurrentScreen(int currentScreen)
	{
		int oldScreen = mCurrentScreen;
		mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
		scrollTo(mCurrentScreen * getWidth(), 0);
		if (mOnScreenSwitchListener != null && mCurrentScreen != oldScreen) mOnScreenSwitchListener.onScreenSwitched(mCurrentScreen);
		//invalidate(); - здесь это не нужно, т.к. scrollTo внутри себя вызывает этот метод. Исправил оригинал. Разве что если мы будем изменять экран из другого потока... Ну если нам такое понадобиться, то исправим, а так неча просто так экран обновлять...
	}
	/**
	 * Sets the {@link ViewSwitcher.OnScreenSwitchListener}.
	 * 
	 * @param onScreenSwitchListener The listener for switch events.
	 */
	public void setOnScreenSwitchListener(OnScreenSwitchListener onScreenSwitchListener)
	{
		mOnScreenSwitchListener = onScreenSwitchListener;
	}
	public void stopTouchEventsDispatching()
	{
		mStopTouchEventDispatching = true;
	}
	public void startTouchEventsDispatching()
	{
		mStopTouchEventDispatching = false;
	}
}