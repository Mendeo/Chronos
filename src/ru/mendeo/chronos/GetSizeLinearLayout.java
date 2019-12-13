package ru.mendeo.chronos;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class GetSizeLinearLayout extends LinearLayout
{
	private OnSizeChangedListener mOnSizeChangedListener = null;
	
	public static interface OnSizeChangedListener
	{
		void onSizeChanged(int viewId, int w, int h, int oldw, int oldh);
	}
	public GetSizeLinearLayout(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	public GetSizeLinearLayout(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		if (mOnSizeChangedListener != null) mOnSizeChangedListener.onSizeChanged(this.getId(), w, h, oldw, oldh);
	}
	public void setOnSizeChangedListener(OnSizeChangedListener l)
	{
		mOnSizeChangedListener = l;
	}
}
