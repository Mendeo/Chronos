package ru.mendeo.chronos;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;

public class GridCellDrawable extends StateListDrawable
{
	private static int COVER_COLOR = 0xB2B2B2B2;
	private TextDrawable mDayTextDrawable;
	private LayerDrawable mPressedDrawable;
	
	public GridCellDrawable(int dayOfMonth, int textColor, float textSize)
	{
		super();
		if (dayOfMonth < 1 || dayOfMonth > 31)
		{
			throw new Error("Incorect day of month passed to GridCellDrawable");
		}
		else
		{
			String dayText = Integer.toString(dayOfMonth);
			mDayTextDrawable = new TextDrawable(dayText, textColor, textSize);
			ShapeDrawable coverSquare = new ShapeDrawable(new RectShape());
			coverSquare.getPaint().setColor(COVER_COLOR);
			//Сначала фон, а сверху текст.
			Drawable[] drawablesArray= {coverSquare, mDayTextDrawable};			
			mPressedDrawable = new LayerDrawable(drawablesArray);
			int[] stateSetNormal = {-android.R.attr.state_pressed};
			int[] stateSetPressed = {android.R.attr.state_pressed};
			addState(stateSetNormal, mDayTextDrawable);
			addState(stateSetPressed, mPressedDrawable);			
		}
	}
	public void setDayOfMonth(int dayOfMonth)
	{
		if (dayOfMonth < 1 || dayOfMonth > 31)
		{
			throw new Error("Incorect day of month passed to GridCellDrawable");
		}
		else
		{
			mDayTextDrawable.setText(Integer.toString(dayOfMonth));
		}
	}
	public void setDayTextColor(int color)
	{
		mDayTextDrawable.setTextColor(color);
	}
}