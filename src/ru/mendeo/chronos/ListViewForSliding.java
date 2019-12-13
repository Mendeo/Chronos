package ru.mendeo.chronos;

import android.content.Context;
import android.support.v4.widget.ListViewAutoScrollHelper;
import android.util.AttributeSet;
import android.widget.ListView;
import android.view.MotionEvent;
import android.view.View;

public class ListViewForSliding extends ListView
{	
	public ListViewForSliding (Context context)
	{
		super(context);
	}
	public ListViewForSliding (Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}
	public ListViewForSliding (Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev)
	{
		//Если наполнения ListViewForSliding недостаточно для вертикального скрула, то передаём родительскому SlidingView что скрулить нечего и можно не обрабатывать вертикальные движения пальцем как попытку проскрулить.
		//Узнаём можно ли скрулить ListViewForSliding при помощи метода canTargetScrollVertically класса ListViewAutoScrollHelper
	 	ListViewAutoScrollHelper lvash = new ListViewAutoScrollHelper(this);
	 	if (!(lvash.canTargetScrollVertically(-1) || lvash.canTargetScrollVertically(1)))
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
