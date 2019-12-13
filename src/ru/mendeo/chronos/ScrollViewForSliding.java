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
		//���� ���������� ScrollViewForSliding ������������ ��� ������������� ������, �� ������� ������������� SlidingView ��� �������� ������ � ����� �� ������������ ������������ �������� ������� ��� ������� �����������.
		if (computeVerticalScrollRange() <= getHeight()) 
		{
	 		//������ ����, ���� �� ����� SlidingView
	 		//��������, ���� SlidingView ������ ���, �� ��������� ����� ��������������.
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
