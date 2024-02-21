package net.lonelytransistor.commonlib.apkselect;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import net.lonelytransistor.commonlib.ProgressDrawable;
import net.lonelytransistor.commonlib.R;
import net.lonelytransistor.commonlib.Utils;
import java.util.concurrent.Executor;

public abstract class SelectorActivity extends AppCompatActivity {
    private static final String TAG = "SelectorActivity";
    private Store.SortOrder sortBy = Store.SortOrder.NONE;
    private final Store.SortOrder[] sortOrders = Store.SortOrder.values();

    private Executor executor = null;
    private Store store = null;
    private boolean storeLoaded = false;
    private ActionBar actionBar = null;
    private SelectorAdapter appsAdapter = null;

    private static ProgressDrawable actionBarBackground;
    private static Drawable backSaveIcon;
    private static Drawable tickSaveIcon;
    private static Drawable reloadIcon;
    private static Drawable[] invertIcons;
    private static Drawable[] selectIcons;
    private static Drawable[] sortIcons;

    private Menu thisMenu = null;

    protected abstract String getHeader();
    protected abstract void getStore(Store.Callback cb);


    private void setAdapter(Store store) {
        if (store == null) {
            ((ListView) findViewById(R.id.apps_selector_listview)).setAdapter(null);
            return;
        }
        this.store = store;
        appsAdapter = new SelectorAdapter(this, this.store,
                (s) -> {
                    appsAdapter.notifyDataSetChanged();
                    onUpdateOptionItems();
                });
        ((ListView) findViewById(R.id.apps_selector_listview)).setAdapter(appsAdapter);
    }
    private void finishAdapter() {
        appsAdapter.filter("");
        updateProgress(1.0f);
        appsAdapter.notifyDataSetChanged();
        storeLoaded = true;
    }
    private void updateProgress(float fraction) {
        actionBar.setBackgroundDrawable(null);
        actionBarBackground.setFraction((fraction > 0 && fraction < 1) ? fraction : 0);
        actionBar.setBackgroundDrawable(actionBarBackground);
    }

    private boolean COLOR_INITIALIZED = false;
    private int BACKGROUND_COLOR = Utils.BACKGROUND_COLOR;
    private int ACCENT_COLOR = Utils.ACCENT_COLOR;
    private int BUTTON_COLOR = Utils.FOREGROUND_COLOR;
    private void initialize() {
        Utils.initialize(this);

        if (!COLOR_INITIALIZED) {
            TypedValue typedValue = new TypedValue();
            if (actionBar.getThemedContext().getTheme().resolveAttribute(android.R.attr.actionBarStyle, typedValue, true)) {
                int[] attrs = {android.R.attr.background,
                        android.R.attr.colorFocusedHighlight,
                        android.R.attr.colorButtonNormal};
                TypedArray actionBarStyleAttrs = obtainStyledAttributes(typedValue.data, attrs);
                try {
                    BACKGROUND_COLOR = actionBarStyleAttrs.getColor(0, Utils.BACKGROUND_COLOR);
                    ACCENT_COLOR = actionBarStyleAttrs.getColor(1, Utils.ACCENT_COLOR);
                    BUTTON_COLOR = actionBarStyleAttrs.getColor(2, Utils.FOREGROUND_COLOR);
                } finally {
                    actionBarStyleAttrs.recycle();
                }
            }
            COLOR_INITIALIZED = true;
        }
        if (actionBarBackground == null)
            actionBarBackground = new ProgressDrawable(BACKGROUND_COLOR, ACCENT_COLOR, 0.0f);
        if (backSaveIcon == null)
            backSaveIcon = Utils.getDrawable(this, R.drawable.save_back, BUTTON_COLOR);
        if (tickSaveIcon == null)
            tickSaveIcon = Utils.getDrawable(this, R.drawable.save_tick, BUTTON_COLOR);
        if (reloadIcon == null)
            reloadIcon = Utils.getDrawable(this, R.drawable.refresh, BUTTON_COLOR);
        if (invertIcons == null)
            invertIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.select_invert_off, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.select_invert_on, BUTTON_COLOR)};
        if (selectIcons == null)
            selectIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.select_all_off, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.select_all_on, BUTTON_COLOR)};
        if (sortIcons == null)
            sortIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.sort_off, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_az, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_za, BUTTON_COLOR)};
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (storeLoaded)
            store.save();
    }
    private void onUpdateOptionItem(MenuItem menu) {
        if (menu.getItemId() == R.id.apps_selector_reload) {
            menu.setIcon(reloadIcon);
        } else if (menu.getItemId() == R.id.apps_selector_invert) {
            menu.setIcon(invertIcons[appsAdapter==null ? 0 :
                    appsAdapter.getCount(true) > appsAdapter.getCount(false) ? 0 : 1]);
        } else if (menu.getItemId() == R.id.apps_selector_select) {
            menu.setIcon(selectIcons[appsAdapter==null ? 0 :
                    appsAdapter.getCount(true) > 0 ? 0 : 1]);
        } else if (menu.getItemId() == R.id.apps_selector_sort) {
            menu.setIcon(sortIcons[appsAdapter==null ? 0 :
                    appsAdapter.getSort().ordinal()]);
        }
    }
    private void onUpdateOptionItems() {
        for (int ix=0; ix<thisMenu.size(); ix++) {
            onUpdateOptionItem(thisMenu.getItem(ix));
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        boolean ret = (boolean) Utils.switch_adv(item.getItemId(),
            () -> {
                onUpdateOptionItem(item);
                return true;
            },
            () -> false,
            android.R.id.home, (Runnable) () -> {
                if (isTaskRoot())
                    finish();
            },
            R.id.apps_selector_reload, (Runnable) () -> {
                reloadStore();
            },
            R.id.apps_selector_invert, (Runnable) () -> {
                if (appsAdapter != null)
                    appsAdapter.invertAllStates();
            },
            R.id.apps_selector_select, (Runnable) () -> {
                if (appsAdapter != null)
                    appsAdapter.setAllStates(appsAdapter.getCount(true) == 0);
            },
            R.id.apps_selector_sort, (Runnable) () -> {
                if (appsAdapter != null)
                    appsAdapter.sort(sortOrders[(appsAdapter.getSort().ordinal() + 1) % sortOrders.length]);
            }
        );
        return ret ? ret : super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apk_selector_toolbar_menu, menu);
        thisMenu = menu;
        return true;
    }

    private void reloadStore() {
        getStore(new Store.Callback() {
            @Override
            public void onStarted(Store apkStore) {
                executor.execute(() -> setAdapter(apkStore));
            }
            @Override
            public void onProgress(float p) {
                executor.execute(() -> updateProgress(p));
            }
            @Override
            public void onFinished(Store apkStore) {
                executor.execute(() -> finishAdapter());
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(com.google.android.material.R.style.Theme_MaterialComponents_DayNight_DarkActionBar);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.apk_selector_activity);
        executor = getMainExecutor();
        actionBar = getSupportActionBar();

        initialize();

        actionBar.setTitle(getHeader());
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(isTaskRoot() ? backSaveIcon : tickSaveIcon);
        ((SearchView) findViewById(R.id.apps_selector_search))
                .setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (appsAdapter == null)
                            return false;

                        appsAdapter.filter(query);
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (appsAdapter == null)
                            return false;

                        appsAdapter.filter(newText);
                        return false;
                    }
                });
        reloadStore();
    }
}