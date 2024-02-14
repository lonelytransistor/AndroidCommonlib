package net.lonelytransistor.commonlib.apkselect;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.lonelytransistor.commonlib.R;
import net.lonelytransistor.commonlib.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectorAdapter extends BaseAdapter {
    private static final String TAG = "SelectorAdapter";
    private final Context context;
    private final Store store;
    private final Filter.FilterListener cb;

    public static final String FIELD_NAMES_EXTRA = ":FIELDS_NAMES_EXTRA:";

    SelectorAdapter(Context context, Store store, Filter.FilterListener cb) {
        this.context = context;
        this.store = store;
        this.cb = cb;
    }
    public Store.SortOrder getSort() {
        return store.getSort();
    }
    public void sort(Store.SortOrder s) {
        sort(s, cb);
    }
    public void sort(Store.SortOrder s, Filter.FilterListener listener) {
        if (store == null)
            return;
        collapseAllDetails();
        store.sort(s, listener);
    }
    public void filter(CharSequence constraint) {
        filter(constraint, cb);
    }
    public void filter(CharSequence constraint, Filter.FilterListener listener) {
        if (store == null)
            return;
        collapseAllDetails();
        store.filter(constraint, listener);
    }
    public void setAllStates(boolean s) {
        setAllStates(s, cb);
    }
    public void setAllStates(boolean s, Filter.FilterListener listener) {
        if (store == null)
            return;
        store.setAllStates(s, listener);
    }
    public void invertAllStates() {
        invertAllStates(cb);
    }
    public void invertAllStates(Filter.FilterListener listener) {
        if (store == null)
            return;
        store.invertAllStates(listener);
    }
    @Override
    public int getCount() {
        if (store == null)
            return 0;
        return store.size();
    }
    public int getCount(boolean s) {
        if (store == null)
            return 0;
        return store.size(s);
    }
    @Override
    public Store.ApkInfo getItem(int ix) {
        if (store == null)
            return null;
        return store.getByIndex(ix);
    }
    @Override
    public long getItemId(int position) {
        if (store == null)
            return 0;
        return store.getId(position);
    }
    private void updateSwitch(SwitchMaterial checkBox, List<Store.NotificationGroup> groups) {
        int monitoredNum = 0;
        for (Store.NotificationGroup category : groups) {
            if (category.monitored) {
                monitoredNum++;
            }
        }
        if (monitoredNum == groups.size()) {
            checkBox.refreshDrawableState();
            checkBox.setChecked(true);
        } else if (monitoredNum > 0) {
            checkBox.setChecked(true);
            checkBox.getTrackDrawable().setState(new int[]{android.R.attr.state_checked});
            checkBox.setChecked(true);
        } else {
            checkBox.refreshDrawableState();
            checkBox.setChecked(false);
        }
    }
    private View inflate(ViewGroup parent, int resId) {
        return LayoutInflater
                .from(parent!=null ? parent.getContext() : context)
                .inflate(
                        resId,
                        parent,
                        false
                );
    }
    private void addSeparator(ViewGroup v) {
        View view = inflate(v, R.layout.apk_selector_listview_element_separator);
        v.addView(view);
    }
    private Map<String, String> fieldNames = new HashMap<>();
    private void updateFieldNames(Store.ApkInfo app) {
        if (app.extra.containsKey(FIELD_NAMES_EXTRA)) {
            Object fieldNamesObj = app.extra.get(FIELD_NAMES_EXTRA);
            if (fieldNamesObj instanceof Map<?, ?>)
                fieldNames = (Map<String, String>) fieldNamesObj;
        }
    }
    private void updateInnerExtras(Map<String, Serializable> extra, LinearLayout inner) {
        if (extra == null)
            return;
        for (String key : extra.keySet()) {
            if (key.equals(FIELD_NAMES_EXTRA))
                continue;
            Object obj = extra.get(key);
            String title = fieldNames.getOrDefault(key, key);
            View viewParent = null;

            if (obj instanceof String) {
                viewParent = inflate(inner, R.layout.apk_selector_listview_element_string);
                TextInputLayout viewParentO = (TextInputLayout) viewParent;
                viewParentO.setHint(title);

                TextInputEditText input = viewParentO.findViewById(R.id.input);
                input.setText((String) obj);
                input.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        extra.put(key, ((EditText) v).getText().toString());
                    }
                });
            } else if (obj instanceof Integer) {
                viewParent = inflate(inner, R.layout.apk_selector_listview_element_integer);
                TextInputLayout view = viewParent.findViewById(R.id.inputLayout);
                view.setHint(title);

                TextInputEditText input = view.findViewById(R.id.input);
                input.setText(String.valueOf((int) obj));
                input.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        EditText i = ((View) v.getParent()).findViewById(R.id.input);
                        int val = 0;
                        try {
                            val = Integer.parseInt(String.valueOf(i.getText()));
                        } catch (NumberFormatException ignored) {}
                        extra.put(key, val);
                    }
                });

                Button spinnerBtn = viewParent.findViewById(R.id.spinner_up);
                spinnerBtn.setOnClickListener((v) -> {
                    EditText i = ((View) v.getParent()).findViewById(R.id.input);
                    int val = 0;
                    try {
                        val = Integer.parseInt(String.valueOf(i.getText()));
                    } catch (NumberFormatException ignored) {}
                    val += 1;
                    extra.put(key, val);
                    i.setText(String.valueOf(val));
                });

                spinnerBtn = viewParent.findViewById(R.id.spinner_down);
                spinnerBtn.setOnClickListener((v) -> {
                    EditText i = ((View) v.getParent()).findViewById(R.id.input);
                    int val = 0;
                    try {
                        val = Integer.parseInt(String.valueOf(i.getText()));
                    } catch (NumberFormatException ignored) {}
                    val -= 1;
                    extra.put(key, val);
                    i.setText(String.valueOf(val));
                });
            } else if (obj instanceof Float) {
                viewParent = inflate(inner, R.layout.apk_selector_listview_element_decimal);
                TextInputLayout view = viewParent.findViewById(R.id.inputLayout);
                view.setHint(title);

                TextInputEditText input = view.findViewById(R.id.input);
                input.setText(String.valueOf((int) obj));
                input.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus) {
                        EditText i = ((View) v.getParent()).findViewById(R.id.input);
                        float val = 0;
                        try {
                            val = Float.parseFloat(String.valueOf(i.getText()));
                        } catch (NumberFormatException ignored) {}
                        extra.put(key, val);
                    }
                });

                Button spinnerBtn = viewParent.findViewById(R.id.spinner_up);
                spinnerBtn.setOnClickListener((v) -> {
                    EditText i = ((View) v.getParent()).findViewById(R.id.input);
                    float val = 0;
                    try {
                        val = Float.parseFloat(String.valueOf(i.getText()));
                    } catch (NumberFormatException ignored) {}
                    val += 0.1f;
                    extra.put(key, val);
                    i.setText(String.valueOf(val));
                });

                spinnerBtn = viewParent.findViewById(R.id.spinner_down);
                spinnerBtn.setOnClickListener((v) -> {
                    EditText i = ((View) v.getParent()).findViewById(R.id.input);
                    float val = 0;
                    try {
                        val = Float.parseFloat(String.valueOf(i.getText()));
                    } catch (NumberFormatException ignored) {}
                    val -= 0.1f;
                    extra.put(key, val);
                    i.setText(String.valueOf(val));
                });
            } else if (obj instanceof Boolean) {
                viewParent = new SwitchMaterial(inner.getContext());
                SwitchMaterial viewParentO = (SwitchMaterial) viewParent;
                viewParentO.setText(title);
                viewParentO.setChecked((boolean) obj);
                viewParentO.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    extra.put(key, isChecked);
                });
            } else {
                Log.e(TAG, "Invalid extra: " + key + "=" + obj);
            }
            if (viewParent != null) {
                inner.addView(viewParent);
            }
        }
    }
    private void updateInnerCategoriesShown(Store.ApkInfo app, View elementView) {
        LinearLayout inner = elementView.findViewById(R.id.innerLayout);
        SwitchMaterial globalCheckBox = elementView.findViewById(R.id.appCheckBox);

        List<SwitchMaterial> catCheckBox = new ArrayList<>();
        List<LinearLayout> catExtras = new ArrayList<>();
        for (Store.NotificationGroup cat : app.notificationGroups) {
            LinearLayout layout = new LinearLayout(inner.getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.setLayoutParams(layoutParams);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setVisibility(cat.monitored ? View.VISIBLE : View.GONE);
            layout.setBackgroundColor(Utils.FOREGROUND_COLOR & 0x00FFFFFF | 0x20000000);
            updateInnerExtras(cat.extra, layout);
            catExtras.add(layout);

            SwitchMaterial checkBox = new SwitchMaterial(inner.getContext());
            checkBox.setText(cat.name);
            checkBox.setChecked(cat.monitored);
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                cat.monitored = isChecked;
                layout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                updateSwitch(globalCheckBox, app.notificationGroups);
            });
            catCheckBox.add(checkBox);

            inner.addView(checkBox);
            inner.addView(layout);
        }

        globalCheckBox.setOnClickListener((v)-> {
            boolean s = ((SwitchMaterial) v).isChecked();
            for (Store.NotificationGroup cat : app.notificationGroups) {
                cat.monitored = s;
            }
            for (int ix=0; ix<catCheckBox.size(); ix++) {
                catCheckBox.get(ix).setChecked(s);
                catExtras.get(ix).setVisibility(s ? View.VISIBLE : View.GONE);
            }
            cb.onFilterComplete(0);
        });
        updateSwitch(globalCheckBox, app.notificationGroups);
    }
    private void updateInnerCategoriesHidden(Store.ApkInfo app, View elementView) {
        SwitchMaterial globalCheckBox = elementView.findViewById(R.id.appCheckBox);
        globalCheckBox.setOnClickListener((v)-> {
            boolean s = ((SwitchMaterial) v).isChecked();
            for (Store.NotificationGroup cat : app.notificationGroups) {
                cat.monitored = s;
            }
            cb.onFilterComplete(0);
        });
        updateSwitch(globalCheckBox, app.notificationGroups);
    }
    private final Map<View, Store.ApkInfo> expandedDetails = new HashMap<>();
    private void collapseAllDetails() {
        Set<View> keys = new HashSet<>(expandedDetails.keySet());
        for (View elementView : keys) {
            Store.ApkInfo app = expandedDetails.get(elementView);
            collapseDetails(app, elementView);
        }
        expandedDetails.clear();
    }
    private void collapseDetails(Store.ApkInfo app, View elementView) {
        ImageView icon = elementView.findViewById(R.id.appDetailsIcon);
        LinearLayout inner = elementView.findViewById(R.id.innerLayout);
        inner.setVisibility(View.GONE);
        icon.setImageDrawable(Utils.getDrawable(context, R.drawable.chevron_down, Utils.FOREGROUND_COLOR));
        inner.removeAllViews();
        updateInnerCategoriesHidden(app, elementView);

        expandedDetails.remove(elementView);
    }
    private void expandDetails(Store.ApkInfo app, View elementView) {
        ImageView icon = elementView.findViewById(R.id.appDetailsIcon);
        LinearLayout inner = elementView.findViewById(R.id.innerLayout);
        inner.setVisibility(View.VISIBLE);
        icon.setImageDrawable(Utils.getDrawable(context, R.drawable.chevron_up, Utils.FOREGROUND_COLOR));
        updateInnerExtras(app.extra, inner);
        updateInnerCategoriesShown(app, elementView);

        expandedDetails.put(elementView, app);
    }
    private void toggleDetails(Store.ApkInfo app, View elementView) {
        LinearLayout inner = elementView.findViewById(R.id.innerLayout);
        if (inner.getVisibility() == View.VISIBLE) {
            collapseDetails(app, elementView);
        } else {
            collapseAllDetails();
            expandDetails(app, elementView);
        }
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Store.ApkInfo app = getItem(position);
        updateFieldNames(app);
        if (convertView == null) {
            convertView = inflate(parent, R.layout.apk_selector_listview_element);
        }
        View elementView = convertView;

        ImageView appIcon = elementView.findViewById(R.id.appIcon);
        appIcon.setImageDrawable(app.icon);
        TextView label = elementView.findViewById(R.id.appLabel);
        label.setText(app.label);
        ConstraintLayout appDetailsBtn = elementView.findViewById(R.id.appDetailsButton);
        appDetailsBtn.setOnClickListener((v)-> {
            toggleDetails(app, elementView);
        });

        updateInnerCategoriesHidden(app, elementView);

        return convertView;
    }
}