package net.lonelytransistor.commonlib;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

public class TVModalPrompt extends DialogFragment {
    public enum ButtonBg {
        NORMAL,
        WHITE,
        RED,
        GREEN,
        BLUE,
        YELLOW
    }
    public interface ButtonCallback {
        boolean onClick(View v);
    }
    public static class DialogButton {
        private final String text;
        private final ButtonBg bg;
        private final ButtonCallback cb;
        DialogButton(String text, ButtonBg bg, ButtonCallback cb) {
            this.text = text;
            this.bg = bg;
            this.cb = cb;
        }
    }

    private class DialogButtonHolder {
        public final TextView view;
        DialogButtonHolder(Context context, String text, ButtonBg background, ButtonCallback cb) {
            Resources res = context.getResources();
            Resources.Theme theme = context.getTheme();

            int p = (int) res.getDimension(R.dimen.dialog_leanback_button_horiz);
            float fontSize = res.getDimension(R.dimen.text_dialog_leanback_button);
            Drawable bg;
            switch (background) {
                case RED:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.tvmodal_btn_red, theme);
                    break;
                case GREEN:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.tvmodal_btn_green, theme);
                    break;
                case BLUE:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.tvmodal_btn_blue, theme);
                    break;
                case YELLOW:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.tvmodal_btn_yellow, theme);
                    break;
                case NORMAL:
                case WHITE:
                default:
                    bg = ResourcesCompat.getDrawable(res, R.drawable.tvmodal_btn_default, theme);
                    break;
            }
            view = new TextView(context);
            view.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
            view.setPadding(p, p, p, p);
            view.setFocusable(true);
            view.setFocusableInTouchMode(true);
            view.setTextSize(fontSize);
            view.setBackground(bg);
            //view.setTextColor(textColor);
            view.setText(text);
            view.setOnClickListener((v) -> {
                if (cb.onClick(v)) {
                    mThis.dismiss();
                }
            });
        }
    }
    private final FragmentActivity mCtx;
    private final TVModalPrompt mThis;
    private final FragmentManager mManager;
    private Drawable mIcon;
    private String mTitle;
    private String mDesc;
    private DialogButton[] mButtons;
    TVModalPrompt(FragmentActivity ctx) {
        mCtx = ctx;
        mThis = this;
        mManager = mCtx.getSupportFragmentManager();
    }
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tvmodal_prompt, container);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ImageView) view.findViewById(R.id.dialog_icon)).setImageDrawable(mIcon);
        ((TextView) view.findViewById(R.id.dialog_title)).setText(mTitle);
        ((TextView) view.findViewById(R.id.dialog_desc)).setText(mDesc);

        LinearLayout buttonsContainer = view.findViewById(R.id.dialog_buttons);
        buttonsContainer.removeAllViews();
        for (DialogButton btn : mButtons) {
            buttonsContainer.addView(new DialogButtonHolder(mCtx, btn.text, btn.bg, btn.cb).view);
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            Window win = dialog.getWindow();
            win.setGravity(Gravity.BOTTOM);
            win.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            win.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    void show(int icon, String title, String desc, DialogButton[] buttons) {
        show(ResourcesCompat.getDrawable(mCtx.getResources(), icon, mCtx.getTheme()),
                title, desc, buttons);
    }
    void show(Drawable icon, String title, String desc, DialogButton[] buttons) {
        mIcon = icon;
        mTitle = title;
        mDesc = desc;
        mButtons = buttons;
        super.show(mManager, null);
    }
}