package net.lonelytransistor.commonlib.apkselect;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
    private ActionBar actionBar = null;
    private SelectorAdapter appsAdapter = null;

    private static ProgressDrawable actionBarBackground;
    private static Drawable backSaveIcon;
    private static Drawable tickSaveIcon;
    private static Drawable reloadIcon;
    private static Drawable[] invertIcons;
    private static Drawable[] selectIcons;
    private static Drawable[] sortIcons;

    protected abstract String getHeader();
    protected abstract void getStore(Store.Callback cb);

    private void updateActionBar() {
        if (actionBar == null || appsAdapter == null)
            return;
        View actionBarView = actionBar.getCustomView();
        if (actionBarView == null)
            return;

        ((TextView) actionBarView.findViewById(R.id.titleTextView))
                .setText(getHeader());
        actionBarView = actionBarView.findViewById(R.id.toolbar_buttons);
        if (actionBarView == null)
            return;

        ((ImageButton) actionBarView.findViewById(R.id.apps_selector_reload))
                .setImageDrawable(reloadIcon);
        ((ImageButton) actionBarView.findViewById(R.id.apps_selector_invert))
                .setImageDrawable(invertIcons[
                        appsAdapter.getCount(true) > appsAdapter.getCount(false) ? 0 : 1]);
        ((ImageButton) actionBarView.findViewById(R.id.apps_selector_select))
                .setImageDrawable(selectIcons[
                        appsAdapter.getCount(true) > 0 ? 0 : 1]);
        ((ImageButton) actionBarView.findViewById(R.id.apps_selector_sort))
                .setImageDrawable(sortIcons[
                        appsAdapter.getSort().ordinal()]);
    }

    private void setAdapter(Store store) {
        if (store == null) {
            ((ListView) findViewById(R.id.apps_selector_listview)).setAdapter(null);
            return;
        }
        this.store = store;
        appsAdapter = new SelectorAdapter(this, this.store,
                (s) -> {
                    appsAdapter.notifyDataSetChanged();
                    updateActionBar();
                });
        ((ListView) findViewById(R.id.apps_selector_listview)).setAdapter(appsAdapter);
    }
    private void finishAdapter() {
        appsAdapter.filter("");
        updateProgress(1.0f);
        appsAdapter.notifyDataSetChanged();
    }
    private void updateProgress(float fraction) {
        actionBar.setBackgroundDrawable(null);
        actionBarBackground.setFraction((fraction > 0 && fraction < 1) ? fraction : 0);
        actionBar.setBackgroundDrawable(actionBarBackground);
    }

    private void initialize() {
        Utils.initialize(this);

        if (actionBarBackground == null)
            actionBarBackground = new ProgressDrawable(Utils.BACKGROUND_COLOR, Utils.ACCENT_COLOR, 0.0f);
        if (backSaveIcon == null)
            backSaveIcon = Utils.getDrawable(this, R.drawable.save_back, Utils.FOREGROUND_COLOR);
        if (tickSaveIcon == null)
            tickSaveIcon = Utils.getDrawable(this, R.drawable.save_tick, Utils.FOREGROUND_COLOR);
        if (reloadIcon == null)
            reloadIcon = Utils.getDrawable(this, R.drawable.refresh, Utils.FOREGROUND_COLOR);
        if (invertIcons == null)
            invertIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.select_invert_off, Utils.FOREGROUND_COLOR),
                    Utils.getDrawable(this, R.drawable.select_invert_on, Utils.FOREGROUND_COLOR)};
        if (selectIcons == null)
            selectIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.select_all_off, Utils.FOREGROUND_COLOR),
                    Utils.getDrawable(this, R.drawable.select_all_on, Utils.FOREGROUND_COLOR)};
        if (sortIcons == null)
            sortIcons = new Drawable[]{
                    Utils.getDrawable(this, R.drawable.sort_off, Utils.FOREGROUND_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_az, Utils.FOREGROUND_COLOR),
                    Utils.getDrawable(this, R.drawable.sort_za, Utils.FOREGROUND_COLOR)};
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (store != null)
            store.save();
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home && isTaskRoot()) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        initialize();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        setContentView(R.layout.apk_selector_activity);
        executor = getMainExecutor();

        actionBar = getSupportActionBar();
        if (actionBar == null) {
            setSupportActionBar((Toolbar) View.inflate(this, R.layout.apk_selector_toolbar, null));
            actionBar = getSupportActionBar();
        }
        if (actionBar != null) {
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setCustomView(R.layout.apk_selector_toolbar);
            actionBar.setTitle(getHeader());

            View actionBarView = actionBar.getCustomView();
            actionBar.setHomeAsUpIndicator(
                    Utils.getDrawable(this,
                            isTaskRoot() ? R.drawable.save_tick : R.drawable.save_back,
                            Utils.FOREGROUND_COLOR));

            actionBarView.findViewById(R.id.apps_selector_reload)
                    .setOnClickListener((v) -> {
                        reloadStore();
                    });
            actionBarView.findViewById(R.id.apps_selector_invert)
                    .setOnClickListener((v) -> {
                        if (appsAdapter == null)
                            return;

                        appsAdapter.invertAllStates();
                    });
            actionBarView.findViewById(R.id.apps_selector_select)
                    .setOnClickListener((v) -> {
                        if (appsAdapter == null)
                            return;

                        appsAdapter.setAllStates(appsAdapter.getCount(true) == 0);
                    });
            actionBarView.findViewById(R.id.apps_selector_sort)
                    .setOnClickListener((v) -> {
                        if (appsAdapter == null)
                            return;

                        appsAdapter.sort(sortOrders[(appsAdapter.getSort().ordinal() + 1) % sortOrders.length]);
                    });
        }
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