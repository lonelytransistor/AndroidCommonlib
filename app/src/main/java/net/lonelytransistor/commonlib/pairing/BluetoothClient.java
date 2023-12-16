package net.lonelytransistor.commonlib.pairing;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import net.lonelytransistor.commonlib.R;
import net.lonelytransistor.commonlib.Utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

@RequiresApi(api = Build.VERSION_CODES.S)
public class BluetoothClient extends AppCompatActivity {
    private String macAddress = "";
    private CompanionDeviceManager deviceManager = null;

    private BluetoothAdapter bluetoothAdapter = null;
    private TextView stateText;

    private final static int REQUEST_PERMISSION_BT = 10;
    private final static int REQUEST_ENABLE_BT = 11;
    private final static int REQUEST_ASSOCIATE_BT = 12;

    public final static int PERMISSION_DENIED = -2;
    public final static int BLUETOOTH_OFF = -3;
    public final static int ASSOCIATION_FAILED = -4;
    public final static int SOCKET_FAILED = -5;
    public final static int CONNECTION_FAILED = -6;
    public final static int INPUT_STREAM_FAILED = -7;
    public final static int OUTPUT_STREAM_FAILED = -8;
    public final static int EMPTY_RESPONSE = -9;
    private BluetoothServerSocket mServerSocket;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_BT) {
            for (int ix = 0; ix < grantResults.length; ix++) {
                if (permissions[ix].equals(Manifest.permission.BLUETOOTH_CONNECT)) {
                    if (grantResults[ix] == PackageManager.PERMISSION_GRANTED) {
                        onCreate();
                        return;
                    }
                    finish(PERMISSION_DENIED);
                }
            }
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                onCreate();
            } else {
                finish(BLUETOOTH_OFF);
            }
        } else if (requestCode == REQUEST_ASSOCIATE_BT) {
            if (resultCode == RESULT_OK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    List<AssociationInfo> assocs = deviceManager.getMyAssociations();
                    if (assocs.size() == 1) {
                        macAddress = assocs.get(0).getDeviceMacAddress().toString();
                    } else {
                        finish(ASSOCIATION_FAILED);
                        return;
                    }
                } else {
                    List<String> assocs = deviceManager.getAssociations();
                    if (assocs.size() == 1) {
                        macAddress = assocs.get(0);
                    } else {
                        finish(ASSOCIATION_FAILED);
                        return;
                    }
                }
                BluetoothDevice deviceToPair = data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
                if (deviceToPair != null) {
                    //deviceToPair.createBond();
                    onCreate();
                } else {
                    finish(ASSOCIATION_FAILED);
                }
            } else {
                finish(ASSOCIATION_FAILED);
            }
        }
    }
    private void onCreate() {
        stateText.setText(R.string.request_bt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_PERMISSION_BT);
                return;
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH}, REQUEST_PERMISSION_BT);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_PERMISSION_BT);
                return;
            }
        }
        if (bluetoothAdapter == null) {
            bluetoothAdapter = getSystemService(BluetoothManager.class).getAdapter();
        }
        stateText.setText(R.string.enable_bt);
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }
        if (deviceManager == null) {
            deviceManager = (CompanionDeviceManager) getSystemService(Context.COMPANION_DEVICE_SERVICE);
        }
        stateText.setText(R.string.find_bt);
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress != null ? macAddress : "")) {
            AssociationRequest pairingRequest = new AssociationRequest.Builder()
                    .setDeviceProfile(AssociationRequest.DEVICE_PROFILE_WATCH)
                    .addDeviceFilter(new BluetoothDeviceFilter.Builder().build())
                    .build();
            deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
                @Override
                public void onDeviceFound(@NonNull IntentSender chooserLauncher) {
                    try {
                        startIntentSenderForResult(chooserLauncher, REQUEST_ASSOCIATE_BT,
                                new Intent(),0,0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e("CompanionDeviceManager", e.toString());
                        finish(ASSOCIATION_FAILED);
                    }
                }
                @Override
                public void onFailure(CharSequence errorMessage) {
                    Log.e("CompanionDeviceManager", errorMessage.toString());
                    finish(ASSOCIATION_FAILED);
                }
            }, null);
            return;
        }

        stateText.setText(R.string.connect_bt);
        ConnectThread connectThread = new ConnectThread(
                bluetoothAdapter,
                UUID.fromString(getIntent().getStringExtra("UUID")),
                getIntent().getStringExtra("DATA"),
                bluetoothAdapter.getRemoteDevice(macAddress));
        connectThread.start();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(intent.getAction())) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                    Log.i("PAIRING", device + " : " + type + " : " + Utils.bundleToString(intent.getExtras()));
                }
            }
        }, filter);

        setContentView(R.layout.bluetooth_pairing);
        stateText = findViewById(R.id.bluetooth_state);
        ImageView logo = findViewById(R.id.bluetooth_logo);
        AnimatedVectorDrawable animation = (AnimatedVectorDrawable) logo.getDrawable();
        animation.registerAnimationCallback(new Animatable2.AnimationCallback() {
            @Override
            public void onAnimationEnd(Drawable drawable) {
                super.onAnimationEnd(drawable);
                logo.post(() -> animation.start());
            }
        });
        animation.start();

        onCreate();
    }
    public int finish(int code) {
        setResult(code);
        finish();
        return code;
    }
    public int finish(int code, String data) {
        setResult(code, getIntent().putExtra("DATA", data));
        finish();
        return code;
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothAdapter mAdapter;
        private final UUID mUUID;
        private final String mData;
        private BluetoothSocket mSocket;
        private OutputStream mOStream;
        private InputStream mIStream;

        public ConnectThread(BluetoothAdapter adapter, UUID uuid, String data, BluetoothDevice device) {
            mAdapter = adapter;
            mUUID = uuid;
            mData = data;
            mDevice = device;
        }
        private boolean socketCreate() {
            try {
                mSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mUUID);
                return true;
            } catch (IOException | SecurityException exception) {
                Log.e("ConnectThread", "Socket could not be created", exception);
                return false;
            }
        }
        private boolean socketConnect() {
            try {
                mSocket.connect();
                return true;
            } catch (IOException | SecurityException exception) {
                Log.e("AcceptThread", "Connection could not be established", exception);
                clientClose();
                return false;
            }
        }
        private boolean clientClose() {
            boolean ret = socketInputStreamClose();
            ret &= socketOutputStreamClose();
            ret &= socketClose();
            return ret;
        }
        private boolean socketClose() {
            try {
                if (mSocket != null)
                    mSocket.close();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not close the client socket", exception);
                return false;
            }
        }
        private boolean socketInputStreamClose() {
            try {
                if (mIStream != null)
                    mIStream.close();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not close the input stream", exception);
                return false;
            }
        }
        private boolean socketOutputStreamClose() {
            try {
                if (mOStream != null)
                    mOStream.close();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not close the output stream", exception);
                return false;
            }
        }
        private boolean socketGetInputStream() {
            try {
                mIStream = mSocket.getInputStream();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not get the input stream", exception);
                clientClose();
                return false;
            }
        }
        private boolean socketGetOutputStream() {
            try {
                mOStream = mSocket.getOutputStream();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not get the output stream", exception);
                clientClose();
                return false;
            }
        }
        public void run() {
            runPriv();
        }
        public int runPriv() {
            //if (!serverSocketCreate())
            //    return finish(SOCKET_FAILED);
            if (!socketCreate())
                return finish(SOCKET_FAILED);
            stateText.post(() -> stateText.setText(R.string.connect_bt));
            if (!socketConnect())
                return finish(CONNECTION_FAILED);
            if (!socketGetOutputStream())
                return finish(OUTPUT_STREAM_FAILED);
            if (!socketGetInputStream())
                return finish(INPUT_STREAM_FAILED);

            stateText.post(() -> stateText.setText(R.string.recv_bt));
            StringBuilder ret = new StringBuilder();
            while (true) {
                try {
                    int data = mIStream.read();
                    if (data > 0 && data <= 0xFF) {
                        ret.append((char) data);
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    Log.d("BluetoothSocket", e.toString());
                    break;
                }
            }

            stateText.post(() -> stateText.setText(R.string.send_bt));
            try {
                mOStream.write(mData.getBytes());
                mOStream.write(new byte[]{0x00});
            } catch (IOException exception) {
                Log.e("ConnectThread", "Sending failed", exception);
                clientClose();
                return finish(EMPTY_RESPONSE);
            }

            stateText.post(() -> stateText.setText(R.string.close_bt));
            clientClose();
            if (ret.length() > 0) {
                return finish(RESULT_OK, ret.toString());
            } else {
                return finish(EMPTY_RESPONSE);
            }
        }
        public void cancel() {
            socketClose();
        }
    }
}
