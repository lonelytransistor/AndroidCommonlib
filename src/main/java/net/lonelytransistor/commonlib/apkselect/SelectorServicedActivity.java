package net.lonelytransistor.commonlib.apkselect;

import android.Manifest;
import android.app.NotificationManager;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

public abstract class SelectorServicedActivity extends SelectorActivity {
    protected abstract Class<?> getStoreService();
    Store.Callback mCb;
    @Override
    protected void getStore(Store.Callback cb) {
        mCb = cb;
        if (binder != null) {
            binder.forceGetStore(cb);
        }
    }
    private StoreService.InstanceBinder binder = null;
    private final ServiceConnection apkStoreConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            binder = ((StoreService.InstanceBinder) ibinder);
            binder.getStore(mCb);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
        @Override
        public void onBindingDied(ComponentName name) {
            binder = null;
            ServiceConnection.super.onBindingDied(name);
        }
        @Override
        public void onNullBinding(ComponentName name) {
            binder = null;
            ServiceConnection.super.onNullBinding(name);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (binder != null) {
            unbindService(apkStoreConnection);
        }
    }
    private interface Cb {
        void cb();
    }
    private void requestNotificationAccess(Cb cb) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager.isNotificationListenerAccessGranted(new ComponentName(this, getStoreService()))) {
            cb.cb();
            return;
        }

        startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        finish();
    }
    private void requestDeviceManagement(Cb cb) {
        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        if (!deviceManager.getAssociations().isEmpty()) {
            cb.cb();
            return;
        }

        ActivityResultContracts.StartIntentSenderForResult contract =
                new ActivityResultContracts.StartIntentSenderForResult();
        ActivityResultLauncher<IntentSenderRequest> launcher =
                registerForActivityResult(contract, (res) -> {
                    if (res.getResultCode() == RESULT_OK) {
                        cb.cb();
                    } else {
                        setResult(-22);
                        finish();
                    }
        });
        AssociationRequest pairingRequest = new AssociationRequest.Builder().setSingleDevice(true).build();
        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(@NonNull IntentSender chooserLauncher) {
                IntentSenderRequest request = new IntentSenderRequest.Builder(chooserLauncher)
                        .build();
                launcher.launch(request);
            }
            @Override
            public void onFailure(CharSequence errorMessage) {
                setResult(-22);
                finish();
            }
        }, null);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestDeviceManagement(() -> {
            requestNotificationAccess(() -> {
                Intent intent = new Intent(SelectorServicedActivity.this, getStoreService());
                intent.setAction(StoreService.INTENT_GET_BINDER);
                bindService(intent, apkStoreConnection, BIND_AUTO_CREATE);
            });
        });
    }
}