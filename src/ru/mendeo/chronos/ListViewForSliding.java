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
		//���� ���������� ListViewForSliding ������������ ��� ������������� ������, �� ������� ������������� SlidingView ��� �������� ������ � ����� �� ������������ ������������ �������� ������� ��� ������� �����������.
		//����� ����� �� �������� ListViewForSliding ��� ������ ������ canTargetScrollVertically ������ ListViewAutoScrollHelper
	 	ListViewAutoScrollHelper lvash = new ListViewAutoScrollHelper(this);
	 	if (!(lvash.canTargetScrollVertically(-1) || lvash.canTargetScrollVertically(1)))
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
