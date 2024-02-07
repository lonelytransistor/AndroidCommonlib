package net.lonelytransistor.commonlib.apkselect;

import android.app.NotificationChannel;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;
import java.util.Set;

public abstract class StoreService extends NotificationListenerService {
    public StoreService() {}
    private static final String TAG = "StoreService";
    public static final String INTENT_GET_BINDER = "INTENT_GET_BINDER";
    public static final String FIELD_NAMES_EXTRA = SelectorAdapter.FIELD_NAMES_EXTRA;
    private UserHandle userHandle;

    public class InstanceBinder extends Binder {
        public void getStore(Store.Callback cb) {
            if (apkStore == null) {
                apkStore = new Store(StoreService.this, new Store.Callback() {
                    @Override
                    public void onStarted(Store apkStore) {}
                    @Override
                    public void onProgress(float p) {
                        if (cb == null || apkStore == null)
                            return;
                        if (p <= 0) {
                            cb.onStarted(apkStore);
                        } else if (p >= 1) {
                            cb.onFinished(apkStore);
                        } else {
                            cb.onProgress(p);
                        }
                    }
                    @Override
                    public void onFinished(Store apkStore) {}
                }) {
                    @Override
                    public String loadRegex(ApkInfo info) {
                        return StoreService.this.loadRegex(info);
                    }
                    @Override
                    public Set<String> loadCategories(ApkInfo info) {
                        return StoreService.this.loadCategories(info);
                    }
                    @Override
                    protected Bundle loadExtraSettings(Store.ApkInfo info) {
                        return StoreService.this.loadExtraSettings(info);
                    }
                    @Override
                    public void save() {
                        StoreService.this.save();
                        super.save();
                    }
                    @Override
                    public void save(ApkInfo info, String notificationRegex, Set<String> monitoredGroups, Bundle extra) {
                        StoreService.this.save(info, notificationRegex, monitoredGroups, extra);
                    }
                    @Override
                    public void save(Set<String> monitoredPackages) {
                        StoreService.this.save(monitoredPackages);
                    }
                    @Override
                    protected List<NotificationChannel> getNotificationChannels(String pkgName) {
                        return StoreService.this.getNotificationChannels(pkgName, userHandle);
                    }
                };
            } else {
                cb.onStarted(apkStore);
                cb.onProgress(0.5f);
                cb.onFinished(apkStore);
            }
        }
    }
    @Override
    public IBinder onBind(Intent mIntent) {
        userHandle = Process.myUserHandle();
        return mIntent.getAction().equals(INTENT_GET_BINDER) ?
                binder : super.onBind(mIntent);
    }
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    private final InstanceBinder binder = new InstanceBinder();

    public abstract String loadRegex(Store.ApkInfo info);
    public abstract Set<String> loadCategories(Store.ApkInfo info);
    public abstract Bundle loadExtraSettings(Store.ApkInfo info);
    public void save() {}
    public abstract void save(Store.ApkInfo info, String notificationRegex, Set<String> monitoredGroups, Bundle extra);
    public abstract void save(Set<String> monitoredPackages);

    private Store apkStore = null;
    @Override
    public void onCreate() {
        super.onCreate();
    }
}