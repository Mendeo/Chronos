package ru.mendeo.chronos;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class BackgroundLineDrawable extends Drawable
{
	private int mAlpha;
	public BackgroundLineDrawable()
	{
		mAlpha = 255;
	}
	@Override
	public void draw(Canvas canvas)
	{
		// TODO Auto-generated method stub
		Rect bounds = getBounds();
		int viewWidth = bounds.right;
		int viewHeight = bounds.bottom;
		if (viewWidth <= 0 || viewHeight <= 0) return;
		//Задаём цвета для нашего градиента фона
		int[] colors = {0xFF6A94C0, 0xFF2E659F, 0xFF6A94C0};
		GradientDrawable Background = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors);
		Background.setBounds(0, 0, viewWidth, viewHeight);
		Background.setShape(GradientDrawable.RECTANGLE);
		Background.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		Background.setAlpha(mAlpha);
		Background.draw(canvas);
		//Задаём цвет маленького квадратика
		Paint paint = new Paint();
		paint.setColor(0x26FFFFFF);
		//Задаём длину стороны квадтратика
		int side = viewHeight / 6;
		//Задаём расстояние между квадратиками по x и по y
		int dY = side;
		int dX = side;
		//По формулам сверхвысшей математики считаем сколько у нас умещается квадратиков по x и по y
		int nX = (viewWidth + dX) / (side + dX);
		int nY = (viewHeight + dY) / (side + dY);
		//Вычисляем толщину кромки сверху снизу и слева справа.
		int pX = (viewWidth - nX * side - (nX - 1) * dX) / 2;
		int pY = (viewHeight - nY * side - (nY - 1) * dY) / 2;
		//Создаём первый квадратик
		Rect smallSquare = new Rect(pX, pY, pX + side, pY + side);
		//Ну и теперь размещаем квадратики в цикле.
		int stepX = dX + side;
		int stepY = dY + side;
		for (int i = 0; i < nX; i++)
		{
			for (int j = 0; j < nY; j++)
			{
				smallSquare.offsetTo(pX + i * stepX, pY + j * stepY);
				canvas.drawRect(smallSquare, paint);
			}
		}		
	}
	@Override
	public void setAlpha(int alpha)
	{
		// TODO Auto-generated method stub
		mAlpha = alpha;
	}
	@Override
	public void setColorFilter(ColorFilter cf)
	{
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getOpacity()
	{
		// TODO Auto-generated method stub
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
			return new BackgroundLineDrawable();
		}
    	
    }
}
