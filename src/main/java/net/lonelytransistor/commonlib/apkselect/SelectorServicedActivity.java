package net.lonelytransistor.commonlib.apkselect;

import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
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
    }
    private final ServiceConnection apkStoreConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder ibinder) {
            StoreService.InstanceBinder binder = ((StoreService.InstanceBinder) ibinder);
            binder.getStore(mCb);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(apkStoreConnection);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityResultContracts.StartIntentSenderForResult contract =
                new ActivityResultContracts.StartIntentSenderForResult();
        ActivityResultLauncher<IntentSenderRequest> launcher =
                registerForActivityResult(contract, (res) -> {
                    if (res.getResultCode() == RESULT_OK) {
                        Intent intent = new Intent(SelectorServicedActivity.this, getStoreService());
                        intent.setAction(StoreService.INTENT_GET_BINDER);
                        bindService(intent, apkStoreConnection, BIND_AUTO_CREATE);
                    } else {
                        setResult(-22);
                        finish();
                    }
                });

        CompanionDeviceManager deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        if (deviceManager.getAssociations().isEmpty()) {
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
        } else {
            Intent intent = new Intent(SelectorServicedActivity.this, getStoreService());
            intent.setAction(StoreService.INTENT_GET_BINDER);
            bindService(intent, apkStoreConnection, BIND_AUTO_CREATE);
        }
    }
}