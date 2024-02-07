package net.lonelytransistor.commonlib;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;

public class ProgressDrawable  extends AnimationDrawable {
    private Paint bg;
    private Paint fg;
    private float frac;

    public ProgressDrawable(int background, int foreground, float fraction) {
        bg = new Paint();
        bg.setColor(background);
        bg.setStyle(Paint.Style.FILL);
        fg = new Paint();
        fg.setColor(foreground);
        fg.setStyle(Paint.Style.FILL);
        frac = fraction;
    }
    public void setFraction(float fraction) {
        frac = fraction;
    }
    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.drawPaint(bg);
        //canvas.drawRect(0.0f, 0.0f,
        //        bounds.width()*(1-frac), bounds.height(), bg);
        canvas.drawRect(0.0f, 0.0f,
                bounds.width()*frac, bounds.height(), fg);
    }
    @Override
    public void setAlpha(int alpha) {
        fg.setAlpha(alpha);
        bg.setAlpha(alpha);
    }
    public void setFG(int color) {
        fg.setColor(color);
    }
    public void setBG(int color) {
        bg.setColor(color);
    }
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
