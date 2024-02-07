package net.lonelytransistor.commonlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class OutlinedTextView extends android.widget.TextView {
    float strokeWidth;
    int strokeColor;
    public OutlinedTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.OutlinedTextView);
        strokeWidth = attributes.getFloat(R.styleable.OutlinedTextView_stroke,1.0f);
        strokeColor = attributes.getColor(R.styleable.OutlinedTextView_strokeColor,0x00000000);
        attributes.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();
        Paint.Style oldStyle = paint.getStyle();
        int oldColor = getCurrentTextColor();

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        setTextColor(strokeColor);
        super.onDraw(canvas);

        paint.setStyle(oldStyle);
        paint.setStrokeWidth(0);
        setTextColor(oldColor);
        super.onDraw(canvas);
    }
}

