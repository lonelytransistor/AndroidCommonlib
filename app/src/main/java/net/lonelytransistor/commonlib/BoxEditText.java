package net.lonelytransistor.commonlib;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;

public class BoxEditText {
    /*pairingCode.setOnBindEditTextListener(editText -> {
        BoxEditText.create(editText, PAIRING_CODE_SIZE, Constants.GraphicalResources.accentColor);
    });*/
    public static void create(EditText editText, int size, int tintId) {
        Drawable background = Utils.getDrawable(editText.getContext(), R.drawable.box_edit_text_bg, tintId);

        editText.setInputType(InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        @SuppressLint("SetTextI18n")
        InputFilter filter = (source, start, end, dest, dstart, dend) -> {
            String data = source.toString().toUpperCase();
            if (data.length() > 1)
                return data.substring(0, size);

            String curText = editText.getText().toString();
            if (curText.length() + source.length() > size) {
                int curPos = (dstart == size) ? size-1 : dstart;
                int nextPos = Math.min(curPos+1, curText.length());
                editText.setText(
                        curText.substring(0, curPos) +
                                source +
                                curText.substring(nextPos));
                editText.setSelection(nextPos);
                return "";
            }
            return data;
        };
        InputFilter[] filters = {
                filter,
        };
        editText.setFilters(filters);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) editText.getLayoutParams();
        params.width = (int) (background.getIntrinsicWidth() * (0.25 + size));
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.topMargin = 100;
        params.bottomMargin = 20;
        editText.setLayoutParams(params);

        editText.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX,50f);

        Rect bounds = new Rect();
        editText.getPaint().getTextBounds(Utils.mult("0", size), 0, size, bounds);
        editText.setLetterSpacing((1f * background.getIntrinsicWidth() * size / bounds.width() - 0.5f)/2);
        editText.setBackground(background);
    }
}
