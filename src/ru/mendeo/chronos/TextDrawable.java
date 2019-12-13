package ru.mendeo.chronos;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable
{
    private String mText;
    private float mTextSize;
    private Paint mPaint;
    private static final float RATIO_TEXT_SIZE_TO_BLUR_RADIUS = 15f;
    private static final float RATIO_TEXT_SIZE_TO_SHADOW_OFFSET_X = 15f;
    private static final int SHADOW_COLOR = 0xFF0E2546;
    public TextDrawable(String text, int color, float size)
    {
        mText = text;
        mTextSize = size;
        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setTextSize(mTextSize);
        mPaint.setAntiAlias(true);
        //mPaint.setFakeBoldText(true);
        mPaint.setShadowLayer(mTextSize / RATIO_TEXT_SIZE_TO_BLUR_RADIUS, mTextSize / RATIO_TEXT_SIZE_TO_SHADOW_OFFSET_X, 0, SHADOW_COLOR);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextAlign(Paint.Align.CENTER);
    }
    public void setText(String text)
    {
    	mText = text;
    }
    public void setTextColor(int color)
    {
    	mPaint.setColor(color);
    }
    public void setTextSize(float size)
    {
    	mPaint.setTextSize(size);
    }
    @Override
    public void draw(Canvas canvas)
    {        
    	Rect bounds = getBounds();
    	//Располагаем наш текст посредине View, на котором отображается наша TextDrawable
    	float x = (float)(bounds.right - bounds.left) / 2f;
    	float y = (float)(bounds.bottom - bounds.top) / 2f + mTextSize / 2.5f;
    	canvas.drawText(mText, x, y, mPaint);
    }
    @Override
    public void setAlpha(int alpha)
    {
    	mPaint.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter cf)
    {
    	mPaint.setColorFilter(cf);
    }
    @Override
    public int getOpacity()
    {
    	return PixelFormat.OPAQUE;
    }
    //Для того, чтобы этот класс работал на андроидах сташе 4.1 мы должны реализовать класс ConstantState, и вернуть его экземпляр в getConstantState. Так же в методе newDrawable должны вернуть этот же класс
    @Override
	public Drawable.ConstantState getConstantState()
    {
    	ConstantStateRealization cs = new ConstantStateRealization();
    	return cs;
    }
    public class ConstantStateRealization extends Drawable.ConstantState
    {

		@Override
		public int getChangingConfigurations()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Drawable newDrawable()
		{
			// TODO Auto-generated method stub
			return new TextDrawable(mText, mPaint.getColor(), mTextSize);
		}
    	
    }
}