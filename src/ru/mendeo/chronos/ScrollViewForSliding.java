package ru.mendeo.chronos;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ScrollView;
import android.content.Context;
import android.util.AttributeSet;

public class ScrollViewForSliding extends ScrollView
{
	public ScrollViewForSliding (Context context)
	{
		super(context);
	}
	public ScrollViewForSliding (Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public ScrollViewForSliding (Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{		
		//Если наполнения ScrollViewForSliding недостаточно для вертикального скрула, то передаём родительскому SlidingView что скрулить нечего и можно не обрабатывать вертикальные движения пальцем как попытку проскрулить.
		if (computeVerticalScrollRange() <= getHeight()) 
		{
	 		//Делаем цикл, пока не найдём SlidingView
	 		//ВНИМАНИЕ, ЕСЛИ SlidingView ВООБЩЕ НЕТ, ТО ПОВЕДЕНИЕ БУДЕТ НЕПРЕДСКАЗУЕМО.
	 		SlidingView SV;
	 		View tmp = this;
	 		while (true)
	 		{
	 			try
	 			{
	 				SV = (SlidingView)tmp.getParent();
	 				break;
				}
	 			catch (ClassCastException e)
	 			{
					// TODO: handle exception
	 				tmp = (View)tmp.getParent();
				}
	 		}
			SV.setIAmNotaVerticalScrollableChild();
		}
		return super.onTouchEvent(ev);
	}
}
