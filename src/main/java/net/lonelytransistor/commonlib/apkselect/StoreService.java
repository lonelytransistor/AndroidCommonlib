package net.lonelytransistor.commonlib.apkselect;

import android.app.NotificationChannel;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.UserHandle;
import android.service.notification.NotificationListenerService;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class StoreService extends NotificationListenerService {
    private static final String TAG = "StoreService";
    public StoreService() {}
    public static final String INTENT_GET_BINDER = "INTENT_GET_BINDER";
    public static final String FIELD_NAMES_EXTRA = SelectorAdapter.FIELD_NAMES_EXTRA;
    private UserHandle userHandle;

    public class InstanceBinder extends Binder {
        public void forceGetStore(Store.Callback cb) {
            apkStore = null;
            getStore(cb);
        }
        public void getStore(Store.Callback cb) {
            if (apkStore == null) {
                Log.i(TAG, "Store empty");
                apkStore = new Store(StoreService.this, new Store.Callback() {
                    private boolean started = false;
                    @Override
                    public void onStarted(Store apkStore) {}
                    @Override
                    public void onProgress(float p) {
                        if (cb == null || apkStore == null)
                            return;
                        if (!started) {
                            cb.onStarted(apkStore);
                            started = true;
                        }
                        if (p >= 1) {
                            cb.onFinished(apkStore);
                            started = false;
                        } else {
                            cb.onProgress(p);
                        }
                    }
                    @Override
                    public void onFinished(Store apkStore) {}
                }) {
                    @Override
                    public void load(Callback progressCb) {
                        StoreService.this.onBeforeLoad();
                        super.load(progressCb);
                        StoreService.this.onAfterLoad();
                    }
                    @Override
                    protected Data load(ApkInfo info) {
                        return StoreService.this.load(info);
                    }
                    @Override
                    public void save() {
                        if (isEmpty())
                            return;
                        StoreService.this.onBeforeSave();
                        super.save();
                    }
                    @Override
                    public void save(ApkInfo info, Data data) {
                        StoreService.this.save(info, data);
                    }
                    @Override
                    public void save(Set<String> monitoredPackages) {
                        StoreService.this.onAfterSave(monitoredPackages);
                    }
                    @Override
                    public void cancel() {
                        StoreService.this.cancel();
                    }
                    @Override
                    protected void getNotificationChannels(String pkgName, CallbackChannels cb) {
                        Map<String,String> channels = new HashMap<>();
                        for (NotificationChannel channel : StoreService.this.getNotificationChannels(pkgName, userHandle)) {
                            channels.put(channel.getId(), channel.getName().toString());
                        }
                        cb.onDone(channels);
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

    protected abstract Store.Data load(Store.ApkInfo info);
    protected abstract void save(Store.ApkInfo info, Store.Data data);
    protected void cancel() {}
    protected void onBeforeLoad() {}
    protected void onAfterLoad() {}
    protected void onBeforeSave() {}
    protected void onAfterSave(Set<String> monitoredPackages) {}

    private Store apkStore = null;
    @Override
    public void onCreate() {
        super.onCreate();
    }
}