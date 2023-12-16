package net.lonelytransistor.commonlib;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

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
    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}
    @Override
    public void setTint(int tintColor) {
        fg.setColor(tintColor);
    }
    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
