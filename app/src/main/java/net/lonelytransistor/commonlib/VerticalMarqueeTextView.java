package net.lonelytransistor.commonlib;

import android.content.Context;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.widget.TextView;

import androidx.annotation.Nullable;

public class VerticalMarqueeTextView extends TextView {
    private void constructor() {
        mChoreographer = Choreographer.getInstance();
        setMovementMethod(ScrollingMovementMethod.getInstance());
    }
    @Override
    public void setSelected(boolean selected) {
        boolean wasSelected = isSelected();
        super.setSelected(selected);
        if (selected && !wasSelected) {
            mLastAnimationMs = 0;
            mChoreographer.postFrameCallback(mTickCallback);
        }
    }
    public VerticalMarqueeTextView(Context context) {
        super(context);
        constructor();
    }
    public VerticalMarqueeTextView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        constructor();
    }
    public VerticalMarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        constructor();
    }
    public VerticalMarqueeTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        constructor();
    }


    private long mLastAnimationMs;
    private Choreographer mChoreographer;
    private Choreographer.FrameCallback mTickCallback = frameTimeNanos -> {
        tick(frameTimeNanos/1000/1000);
    };
    private boolean inhibitTextChangedCb = false;
    private int scrollRange = 0;
    private boolean allowScrolling = true;
    private CharSequence oldText;
    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        if (inhibitTextChangedCb)
            return;
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (text == null || text.length() == 0)
            return;

        inhibitTextChangedCb = true;
        scrollRange = 0;
        oldText = text;
        setText(text +
                (text.charAt(text.length() - 1) == '\n' ? "" : "\n") +
                "\n\n" + text);
        mChoreographer.postFrameCallback(mTickCallback);
        inhibitTextChangedCb = false;
    }
    private int totalScroll = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        updateScrollRange();
    }

    private void updateScrollRange() {
        scrollRange = computeVerticalScrollRange()/2 + getLineHeight();
        allowScrolling = computeVerticalScrollRange()/2 > getMeasuredHeight();
        if (!allowScrolling) {
            inhibitTextChangedCb = true;
            setText(oldText);
            inhibitTextChangedCb = false;
        }
    }
    private void tickPriv(long curTime) {
        mLastAnimationMs = curTime;
        scrollBy(0, 1);
        totalScroll++;
        if (totalScroll > scrollRange) {
            scrollTo(0, 0);
            totalScroll = 0;
        }
    }
    private void tick(long curTime) {
        if (!isSelected() || !allowScrolling) {
            scrollTo(0,0);
            totalScroll = 0;
            return;
        }

        if (scrollRange == 0) {
            updateScrollRange();
        }
        if (totalScroll == 0) {
            if (mLastAnimationMs == 0)
                mLastAnimationMs = curTime;
            if (curTime - mLastAnimationMs > 2000)
                tickPriv(curTime);
        } else if (curTime - mLastAnimationMs > 10) {
            tickPriv(curTime);
        }
        mChoreographer.postFrameCallback(mTickCallback);
    }
}
