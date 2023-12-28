package net.lonelytransistor.commonlib;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@SuppressLint("AppCompatCustomView")
public class ProgressDrawableView extends ImageView {
    private final ProgressDrawable drawable = new ProgressDrawable(0,0,0);
    public ProgressDrawableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.ProgressDrawableView);
        setFraction(attributes.getFloat(R.styleable.ProgressDrawableView_progress,1.0f));
        setFG(attributes.getColor(R.styleable.ProgressDrawableView_fgColor,0x00000000));
        setBG(attributes.getColor(R.styleable.ProgressDrawableView_bgColor,0x00000000));
        attributes.recycle();
    }
    public void setFraction(float val) {
        drawable.setFraction(val);
    }
    public void setFG(int color) {
        drawable.setFG(color);
    }
    public void setBG(int color) {
        drawable.setBG(color);
    }
}
