package net.lonelytransistor.commonlib.pairing;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Animatable2;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import net.lonelytransistor.commonlib.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothServer extends Activity {
    private BluetoothAdapter bluetoothAdapter = null;
    private TextView stateText;

    private final static int REQUEST_PERMISSION_BT = 10;
    private final static int REQUEST_ENABLE_BT = 11;
    private final static int REQUEST_VISIBLE_BT = 12;

    public final static int PERMISSION_DENIED = -2;
    public final static int BLUETOOTH_OFF = -3;
    public final static int BLUETOOTH_HIDDEN = -4;
    public final static int SOCKET_FAILED = -5;
    public final static int CONNECTION_FAILED = -6;
    public final static int INPUT_STREAM_FAILED = -7;
    public final static int OUTPUT_STREAM_FAILED = -8;
    public final static int EMPTY_RESPONSE = -9;

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
        } else if (requestCode == REQUEST_VISIBLE_BT) {
            if (resultCode == RESULT_OK) {
                onCreate();
            } else {
                finish(BLUETOOTH_HIDDEN);
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
            Log.i("Perm", "SCAN: " + ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN));
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_SCAN}, REQUEST_PERMISSION_BT);
                return;
            }
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, REQUEST_PERMISSION_BT);
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
        stateText.setText(R.string.visible_bt);
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivityForResult(discoverableIntent, REQUEST_VISIBLE_BT);
        }
        stateText.setText(R.string.find_bt);
        AcceptThread acceptThread = new AcceptThread(
                bluetoothAdapter,
                UUID.fromString(getIntent().getStringExtra("UUID")),
                getIntent().getStringExtra("DATA"));
        acceptThread.start();
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    private class AcceptThread extends Thread {
        private final BluetoothAdapter mAdapter;
        private final UUID mUUID;
        private final String mData;
        private BluetoothServerSocket mServerSocket;
        private BluetoothSocket mSocket;
        private OutputStream mOStream;
        private InputStream mIStream;

        public AcceptThread(BluetoothAdapter adapter, UUID uuid, String data) {
            mAdapter = adapter;
            mUUID = uuid;
            mData = data;
        }
        private boolean socketCreate() {
            try {
                mServerSocket = mAdapter.listenUsingInsecureRfcommWithServiceRecord(this.getName(), mUUID);
                return true;
            } catch (IOException | SecurityException exception) {
                Log.e("AcceptThread", "Socket could not be created", exception);
                return false;
            }
        }
        private boolean socketAccept() {
            try {
                mSocket = mServerSocket.accept();
                return true;
            } catch (IOException exception) {
                Log.e("AcceptThread", "Connection could not be accepted", exception);
                serverClose();
                return false;
            }
        }
        private boolean serverClose() {
            boolean ret = socketInputStreamClose();
            ret &= socketOutputStreamClose();
            ret &= socketClose();
            ret &= serverSocketClose();
            return  ret;
        }
        private boolean socketClose() {
            try {
                if (mSocket != null)
                    mSocket.close();
                return true;
            } catch (IOException exception) {
                Log.e("AcceptThread", "Could not close the client socket", exception);
                return false;
            }
        }
        private boolean serverSocketClose() {
            try {
                if (mServerSocket != null)
                    mServerSocket.close();
                return true;
            } catch (IOException exception) {
                Log.e("AcceptThread", "Could not close the server socket", exception);
                return false;
            }
        }
        private boolean socketOutputStreamClose() {
            try {
                if (mOStream != null)
                    mOStream.close();
                return true;
            } catch (IOException exception) {
                Log.e("AcceptThread", "Could not close the output stream", exception);
                return false;
            }
        }
        private boolean socketInputStreamClose() {
            try {
                if (mIStream != null)
                    mIStream.close();
                return true;
            } catch (IOException exception) {
                Log.e("AcceptThread", "Could not close the intput stream", exception);
                return false;
            }
        }
        private boolean socketGetOutputStream() {
            try {
                mOStream = mSocket.getOutputStream();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not get the output stream", exception);
                serverClose();
                return false;
            }
        }
        private boolean socketGetInputStream() {
            try {
                mIStream = mSocket.getInputStream();
                return true;
            } catch (IOException exception) {
                Log.e("ConnectThread", "Could not get the intput stream", exception);
                serverClose();
                return false;
            }
        }
        public void run() {
            runPriv();
        }
        private int runPriv() {
            if (!socketCreate())
                return finish(SOCKET_FAILED);
            stateText.post(() -> stateText.setText(R.string.wait_bt));
            if (!socketAccept())
                return finish(CONNECTION_FAILED);
            if (!socketGetOutputStream())
                return finish(OUTPUT_STREAM_FAILED);
            if (!socketGetInputStream())
                return finish(INPUT_STREAM_FAILED);

            stateText.post(() -> stateText.setText(R.string.send_bt));
            try {
                mOStream.write(mData.getBytes());
                mOStream.write(new byte[]{0x00});
            } catch (IOException exception) {
                Log.e("ConnectThread", "Sending failed", exception);
                serverClose();
                return finish(EMPTY_RESPONSE);
            }

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
                } catch (IOException exception) {
                    Log.d("ConnectThread", "Receiving failed", exception);
                    break;
                }
            }

            stateText.post(() -> stateText.setText(R.string.close_bt));
            serverClose();
            if (ret.length() > 0) {
                return finish(RESULT_OK, ret.toString());
            } else {
                return finish(EMPTY_RESPONSE);
            }
        }
        public void cancel() {
            serverClose();
        }
    }
}
