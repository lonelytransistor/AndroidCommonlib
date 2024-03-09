package net.lonelytransistor.commonlib.apkselect;

import android.annotation.SuppressLint;
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
import java.util.concurrent.locks.ReentrantLock;

public abstract class SelectorActivity extends AppCompatActivity {
    private static final String TAG = "SelectorActivity";
    private Store.SortOrder sortBy = Store.SortOrder.NONE;
    private final Store.SortOrder[] sortOrders = Store.SortOrder.values();

    private Executor executor = null;
    private Store store = null;
    private boolean storeLoaded = false;
    private ActionBar actionBar = null;
    private SelectorAdapter appsAdapter = null;
    private final ReentrantLock mutex = new ReentrantLock();

    private static ProgressDrawable actionBarBackground;
    private static Drawable backSaveIcon;
    private static Drawable tickSaveIcon;
    private static Drawable reloadIcon;
    private static Drawable[] invertIcons;

    private static Drawable[] selectIcons;
    private static Drawable[] sortIcons;
    private static Drawable[] showSystemIcons;
    private static Drawable[] showSelectedIcons;
    private static Drawable[] showDeselectedIcons;
    private static String[] selectText;
    private static String[] sortText;
    private static String[] showSystemText;
    private static String[] showSelectedText;
    private static String[] showDeselectedText;

    private float oldFraction = 0;
    private Menu thisMenu = null;

    protected abstract String getHeader();
    protected abstract void getStore(Store.Callback cb);


    private void setAdapter(Store store) {
        storeLoaded = false;
        ListView view = findViewById(R.id.apps_selector_listview);
        if (store == null) {
            view.setAdapter(null);
            return;
        }
        this.store = store;
        appsAdapter = new SelectorAdapter(this, this.store,
                (s) -> {
                    appsAdapter.notifyDataSetChanged();
                    onUpdateOptionItems();
                });
        view.setAdapter(appsAdapter);
        Log.i(TAG, "setAdapter");
    }
    private void finishAdapter() {
        oldFraction = 0;
        actionBar.setBackgroundDrawable(null);
        actionBarBackground.setFraction(0);
        actionBar.setBackgroundDrawable(actionBarBackground);
        updateProgress(1.0f);

        ListView view = findViewById(R.id.apps_selector_listview);
        view.post(() -> {
            mutex.lock();
            storeLoaded = true;
            store.resumeSize();
            appsAdapter.notifyDataSetChanged();
            mutex.unlock();
        });
    }
    private void updateProgress(float fraction) {
        if (fraction > oldFraction) {
            oldFraction = (fraction > 0 && fraction < 1) ? fraction : 0;

            actionBar.setBackgroundDrawable(null);
            actionBarBackground.setFraction(oldFraction);
            actionBar.setBackgroundDrawable(actionBarBackground);

            ListView view = findViewById(R.id.apps_selector_listview);
            int screenHeight = Math.max(5, view.getLastVisiblePosition() - view.getFirstVisiblePosition());
            view.post(() -> {
                mutex.lock();
                if (store.realSize() < view.getLastVisiblePosition() + screenHeight && !storeLoaded) {
                    store.freezeSize();
                    appsAdapter.notifyDataSetChanged();
                }
                mutex.unlock();
            });
        }
    }

    private boolean COLOR_INITIALIZED = false;
    private int BACKGROUND_COLOR = Utils.BACKGROUND_COLOR;
    private int ACCENT_COLOR = Utils.ACCENT_COLOR;
    private int BUTTON_COLOR = Utils.FOREGROUND_COLOR;
    @SuppressLint("ResourceType")
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
        if (selectText == null)
            selectText = new String[]{
                    getText(R.string.deselect_all).toString(),
                    getText(R.string.select_all).toString()
            };
        if (sortIcons == null)
            sortIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.sort_off, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_az, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_za, BUTTON_COLOR)};
        if (sortText == null)
            sortText = new String[]{
                    getText(R.string.sort_none).toString(),
                    getText(R.string.sort_alphabetically).toString(),
                    getText(R.string.sort_reverse_alphabetically).toString()
            };
        if (showSelectedIcons == null)
            showSelectedIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.hide_selected, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.show_selected, BUTTON_COLOR)};
        if (showSelectedText == null)
            showSelectedText = new String[]{
                    getText(R.string.hide_selected).toString(),
                    getText(R.string.show_selected).toString()
            };
        if (showDeselectedIcons == null)
            showDeselectedIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.hide_unselected, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.show_unselected, BUTTON_COLOR)};
        if (showDeselectedText == null)
            showDeselectedText = new String[]{
                    getText(R.string.hide_unselected).toString(),
                    getText(R.string.show_unselected).toString()
            };
        if (showSystemIcons == null)
            showSystemIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.hide_system, BUTTON_COLOR),
                    Utils.getDrawable(this, R.drawable.show_system, BUTTON_COLOR)};
        if (showSystemText == null)
            showSystemText = new String[]{
                    getText(R.string.hide_system).toString(),
                    getText(R.string.show_system).toString()
            };
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (storeLoaded) {
            store.save();
        } else {
            store.cancel();
        }
    }
    private void onUpdateOptionItem(MenuItem menu) {
        if (menu.getItemId() == R.id.apps_selector_reload) {
            menu.setIcon(reloadIcon);
        } else if (menu.getItemId() == R.id.apps_selector_invert) {
            int ix = appsAdapter==null || appsAdapter.getCount(true)>appsAdapter.getCount(false) ? 0 : 1;
            menu.setIcon(invertIcons[ix]);
        } else if (menu.getItemId() == R.id.apps_selector_select) {
            int ix = appsAdapter==null || appsAdapter.getCount(true)>0 ? 0 : 1;
            menu.setIcon(selectIcons[ix]);
            menu.setTitle(selectText[ix]);
        } else if (menu.getItemId() == R.id.apps_selector_sort) {
            int ix = appsAdapter==null ? 0 : appsAdapter.getSort().ordinal();
            menu.setIcon(sortIcons[ix]);
            menu.setTitle(sortText[ix]);
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
    protected enum Button {
        INVERT,
        SELECT_ALL,
        RELOAD,
        SORT,
        SHOW_SELECTED,
        SHOW_UNSELECTED,
        SHOW_SYSTEM
    }
    protected boolean isButtonVisible(Button btn) {
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.apk_selector_toolbar_menu, menu);
        thisMenu = menu;
        for (int ix=0; ix<thisMenu.size(); ix++) {
            MenuItem item = thisMenu.getItem(ix);
            int itemId = item.getItemId();
            if (itemId == R.id.apps_selector_invert) {
                item.setVisible(isButtonVisible(Button.INVERT));
            } else if (itemId == R.id.apps_selector_reload) {
                item.setVisible(isButtonVisible(Button.RELOAD));
            } else if (itemId == R.id.apps_selector_select) {
                item.setVisible(isButtonVisible(Button.SELECT_ALL));
            } else if (itemId == R.id.apps_selector_sort) {
                item.setVisible(isButtonVisible(Button.SORT));
            } else if (itemId == R.id.apps_selector_show_unselected) {
                item.setVisible(isButtonVisible(Button.SHOW_UNSELECTED));
            } else if (itemId == R.id.apps_selector_show_selected) {
                item.setVisible(isButtonVisible(Button.SHOW_SELECTED));
            } else if (itemId == R.id.apps_selector_show_system) {
                item.setVisible(isButtonVisible(Button.SHOW_SYSTEM));
            }
        }
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