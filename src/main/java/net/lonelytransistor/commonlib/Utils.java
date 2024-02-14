package net.lonelytransistor.commonlib;

import android.app.Activity;
import android.companion.AssociationRequest;
import android.companion.CompanionDeviceManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;

public class Utils {
    public interface SwitchAdv {
        Object run();
    }
    public static Object switch_adv(Object var, SwitchAdv t, SwitchAdv f, Object... cases) {
        if (cases.length % 2 != 0) {
            throw new IllegalArgumentException("Arguments must be in pairs of conditions and actions");
        }
        for (int i = 0; i < cases.length; i += 2) {
            if (cases[i].equals(var)) {
                ((Runnable) cases[i + 1]).run();
                return t.run();
            }
        }
        return f.run();
    }
    public static void switch_adv(Object var, Object v0, Runnable r0, Object v1, Runnable r1, Object v2, Runnable r2) {
        if (var == v0) r0.run();
        else if (var == v1) r1.run();
        else if (var == v2) r2.run();
    }
    public static <T> List<T> findViewsByType(View parent, Class<T> type) {
        return findViewsByTypePriv(parent, type, false);
    }
    public static <T> T findViewByType(View parent, Class<T> type) {
        List<T> ret = findViewsByTypePriv(parent, type, true);
        return ret.size()==1 ? ret.get(0) : null;
    }
    private static <T> List<T> findViewsByTypePriv(View parent, Class<T> type, boolean single) {
        List<T> ret = new ArrayList<>();
        List<View> views = new ArrayList<>();
        views.add(parent);
        for (int i=0; i<views.size(); i++) {
            View v = views.get(i);
            if (type.isInstance(v) && i>0) {
                ret.add((T) v);
                if (single) {
                    return ret;
                }
            } else if (v instanceof ViewGroup) {
                ViewGroup grp = (ViewGroup) v;
                for (int i2=0; i2<grp.getChildCount(); i2++) {
                    views.add(grp.getChildAt(i2));
                }
            }
        }
        return ret;
    }
    public static class Interval {
        private Handler handler;
        private Runnable runnable;
        public Interval(Runnable r, int i) {
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    r.run();
                    handler.postDelayed(this, i);
                }
            };
        }
        public void start() {
            handler.postDelayed(runnable, 0);
        }
        public void stop() {
            handler.removeCallbacks(runnable);
        }
    }
    static private final DateFormat mDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public static void signalService(Context context, String action, Class<?> klass) {
        Intent intent = new Intent(context, klass);
        intent.setAction(action);
        context.startService(intent);
    }
    public static String bundleToString(Intent intent) {
        return bundleToString(intent.getExtras());
    }
    public static String bundleToString(Bundle bundle) {
        String ret = "{";
        if (bundle != null) {
            for (String key : bundle.keySet()) {
                Object value = bundle.get(key);
                ret += "\"" + key + "\": " + (value instanceof String ? "\""+value+"\"" : value) + ",";
            }
        }
        if (ret.endsWith(","))
            ret = ret.substring(0, ret.length()-1);
        ret += "}";
        return ret;
    }
    public static final int REQUEST_CODE_ASSOCIATE = 149;
    public static void deassociateDevice(Activity ctx) {
        CompanionDeviceManager deviceManager =
                (CompanionDeviceManager) ctx.getSystemService(Context.COMPANION_DEVICE_SERVICE);
        if (deviceManager == null || deviceManager.getAssociations().size() == 0) {
            return;
        }
        for (String assoc : deviceManager.getAssociations()) {
            deviceManager.disassociate(assoc);
        }
    }
    public static void associateDevice(Activity ctx) {
        CompanionDeviceManager deviceManager =
                (CompanionDeviceManager) ctx.getSystemService(Context.COMPANION_DEVICE_SERVICE);
        if (deviceManager == null || deviceManager.getAssociations().size() > 0) {
            return;
        }
        AssociationRequest.Builder pairingRequestBuilder = new AssociationRequest.Builder();
        pairingRequestBuilder = pairingRequestBuilder.setSingleDevice(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            pairingRequestBuilder = pairingRequestBuilder.setDeviceProfile(AssociationRequest.DEVICE_PROFILE_WATCH);
        }
        AssociationRequest pairingRequest = pairingRequestBuilder.build();

        deviceManager.associate(pairingRequest, new CompanionDeviceManager.Callback() {
            @Override
            public void onDeviceFound(@NonNull IntentSender chooserLauncher) {
                try {
                    ctx.startIntentSenderForResult(
                            chooserLauncher, REQUEST_CODE_ASSOCIATE,
                            new Intent(), 0,
                            0, 0
                    );
                } catch (IntentSender.SendIntentException ignored) {}
            }
            @Override
            public void onFailure(CharSequence errorMessage) {
                Toast.makeText(ctx, errorMessage.toString(), Toast.LENGTH_SHORT).show();
            }
        }, null);
    }

    public static String getDialerPackageName(PackageManager pkgManager) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        ComponentName componentName = intent.resolveActivity(pkgManager);
        return componentName != null ? componentName.getPackageName() : null;
    }
    public static List<String> getDialerPackageNames(PackageManager pkgManager) {
        List<String> ret = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_DIAL);
        ComponentName componentName = intent.resolveActivity(pkgManager);
        if (componentName != null)
            ret.add(componentName.getPackageName());
        intent = new Intent(Intent.ACTION_CALL);
        componentName = intent.resolveActivity(pkgManager);
        if (componentName != null)
            ret.add(componentName.getPackageName());
        intent = new Intent(Intent.ACTION_CALL_BUTTON);
        componentName = intent.resolveActivity(pkgManager);
        if (componentName != null)
            ret.add(componentName.getPackageName());
        return ret;
    }
    public static String mult(String data, int num) {
        StringBuilder ret = new StringBuilder();
        for (int n=0; n<num; n++) {
            ret.append(data);
        }
        return ret.toString();
    }
    public static String capitalize(String data) {
        return data.substring(0,1).toUpperCase() + data.substring(1);
    }

    public static int FOREGROUND_COLOR = 0;
    public static int BACKGROUND_COLOR = 0;
    public static int HIGHLIGHT_COLOR = 0;
    public static int ACCENT_COLOR = 0;
    public static int drawableToColor(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                Math.max(1, drawable.getIntrinsicWidth()),
                Math.max(1, drawable.getIntrinsicHeight()),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        bitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, false);
        int pixelColor = bitmap.getPixel(0, 0);
        int red = Color.red(pixelColor);
        int green = Color.green(pixelColor);
        int blue = Color.blue(pixelColor);
        return 0xFF000000 |
                red << 8*2 |
                green << 8 |
                blue;
    }
    public static void initialize(Activity ctx) {
        if (BACKGROUND_COLOR == 0) {
            TypedValue color = new TypedValue();
            ctx.getTheme().resolveAttribute(
                    android.R.attr.colorBackground, color, true);
            BACKGROUND_COLOR = color.data;
        }
        if (ACCENT_COLOR == 0) {
            TypedValue color = new TypedValue();
            ctx.getTheme().resolveAttribute(
                    android.R.attr.colorAccent, color, true);
            ACCENT_COLOR = color.data;
        }
        if (HIGHLIGHT_COLOR == 0) {
            TypedValue color = new TypedValue();
            ctx.getTheme().resolveAttribute(
                    android.R.attr.colorFocusedHighlight, color, true);
            HIGHLIGHT_COLOR = color.data;
        }
        if (FOREGROUND_COLOR == 0) {
            ViewGroup colorGetter = (ViewGroup) ctx.getLayoutInflater().inflate(R.layout._color_getter, null);
            FOREGROUND_COLOR = Utils.drawableToColor(colorGetter.findViewById(R.id.tintSelector).getBackground());
        }
    }
    public static Drawable getDrawable(Context ctx, int resId) {
        return getDrawable(ctx, resId, 0);
    }
    public static Drawable getDrawable(Context ctx, int resId, int tint) {
        Drawable saveButton = AppCompatResources.getDrawable(ctx, resId);
        if (tint == 0) {
            return saveButton;
        }
        TypedValue colorAttr = new TypedValue();
        if (ctx.getTheme().resolveAttribute(tint, colorAttr, true)) {
            saveButton.setTint(colorAttr.data);
            return saveButton;
        }
        try {
            saveButton.setTint(ctx.getColor(tint));
            return saveButton;
        } catch (Resources.NotFoundException ignored) {}
        if (tint < 0xFFFFFFFF) {
            saveButton.setTint(tint);
            return saveButton;
        }
        return saveButton;
    }
    private static KeyEvent lastKeyEvent = new KeyEvent(0,0);
    public static boolean isKeyEventRepeat(KeyEvent event) {
        KeyEvent lastEvent_ = lastKeyEvent;
        lastKeyEvent = event;
        return (event.getRepeatCount()>0) ||
                (event.getKeyCode() == lastEvent_.getKeyCode() &&
                event.getAction() == lastEvent_.getAction() &&
                event.getEventTime()-100 <= lastEvent_.getEventTime());
    }
    public static JSONObject mapToJSON(Map<String,String> map) {
        JSONObject data = new JSONObject();
        for (String name : map.keySet()) {
            try {
                data.put(name, map.get(name));
            } catch (JSONException ignored) {}
        }
        return data;
    }
    public static Map<String,String> jsonToMap(JSONObject data) {
        Map<String,String> map = new HashMap<>();
        for (Iterator<String> it = data.keys(); it.hasNext(); ) {
            String name = it.next();
            try {
                map.put(name, String.valueOf(data.get(name)));
            } catch (JSONException ignored) {}
        }
        return map;
    }
    public static void executeAsync(Context ctx, Runnable command) {
        Executor executor = ctx.getMainExecutor();
        Thread mainThread = ctx.getMainLooper().getThread();
        if (mainThread == Thread.currentThread()) {
            command.run();
        } else {
            executor.execute(command);
        }
    }

    public static String reverseString(String data) {
        StringBuilder ret = new StringBuilder();
        for (byte b : data.getBytes()) {
            ret.insert(0, new String(new byte[] {b}));
        }
        return ret.toString();
    }

    public static String getContactName(ContentResolver resolver, String number) {
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[] {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER};
        Cursor cursor = resolver.query(uri, projection, null, null, null);

        int nameId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int numberId = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
        String numberStr = number.replaceAll("[^0-9]", "");
        if (cursor.moveToFirst()) {
            do {
                if (cursor.getString(numberId).replaceAll("[^0-9]", "").endsWith(numberStr)) {
                    String name = cursor.getString(nameId);
                    cursor.close();
                    return name;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return number;
    }
    public static <T> boolean listContains(List<T> list, Object obj) {
        for (T el : list) {
            if (el.equals(obj)) {
                return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    public interface CallableN {
        void call() throws Exception;
    }
    @FunctionalInterface
    public interface CallableP1N<P1> {
        void call(P1 param1) throws Exception;
    }
    @FunctionalInterface
    public interface CallableP1<R,P1> {
        R call(P1 param1) throws Exception;
    }
    @FunctionalInterface
    public interface CallableP2N<P1,P2> {
        void call(P1 param1, P2 param2) throws Exception;
    }
    @FunctionalInterface
    public interface CallableP2<R,P1,P2> {
        R call(P1 param1, P2 param2) throws Exception;
    }
    @FunctionalInterface
    public interface CallableP3N<P1,P2,P3> {
        void call(P1 param1, P2 param2, P3 param3) throws Exception;
    }
    @FunctionalInterface
    public interface CallableP3<R,P1,P2,P3> {
        R call(P1 param1, P2 param2, P3 param3) throws Exception;
    }
    public static <P1,P2,P3> void executeSync(Context ctx, CallableP3N<P1,P2,P3> command, P1 param1, P2 param2, P3 param3) {
        executeSync(ctx, () -> {
            command.call(param1, param2, param3);
            return 0;
        });
    }
    public static <T,P1,P2,P3> T executeSync(Context ctx, CallableP3<T,P1,P2,P3> command, P1 param1, P2 param2, P3 param3) {
        return executeSync(ctx, () -> {
            return command.call(param1, param2, param3);
        });
    }
    public static <P1,P2> void executeSync(Context ctx, CallableP2N<P1,P2> command, P1 param1, P2 param2) {
        executeSync(ctx, () -> {
            command.call(param1, param2);
            return 0;
        });
    }
    public static <T,P1,P2> T executeSync(Context ctx, CallableP2<T,P1,P2> command, P1 param1, P2 param2) {
        return executeSync(ctx, () -> {
            return command.call(param1, param2);
        });
    }
    public static <P1> void executeSync(Context ctx, CallableP1N<P1> command, P1 param1) {
        executeSync(ctx, () -> {
            command.call(param1);
            return 0;
        });
    }
    public static <T,P1> T executeSync(Context ctx, CallableP1<T,P1> command, P1 param1) {
        return executeSync(ctx, () -> {
            return command.call(param1);
        });
    }
    public static void executeSync(Context ctx, CallableN command) {
        executeSync(ctx, () -> {
            command.call();
            return 0;
        });
    }
    public static <T> T executeSync(Context ctx, Callable<T> command) {
        Executor executor = ctx.getMainExecutor();
        Thread mainThread = ctx.getMainLooper().getThread();
        RunnableFuture<T> runnable = new FutureTask<>(command);
        if (mainThread == Thread.currentThread()) {
            runnable.run();
        } else {
            executor.execute(runnable);
        }
        try {
            return runnable.get();
        } catch (ExecutionException | InterruptedException e) {
            Log.e("executeAsync", e.toString());
            return null;
        }
    }

    public static Map<String, String> parseWwwForm(String data) {
        Map<String, String> result = new HashMap<>();
        String[] pairs = data.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0], keyValue[1]);
            }
        }
        return result;
    }
    public static JSONObject wwwFormToJSON(String data) {
        String rexNumber = "^[0-9]+$";
        String rexFloat = "^[0-9]+\\.[0-9]+$";
        String rexBoolean = "^(on|off|true|false)$";
        Map<String,String> map = parseWwwForm(data);
        JSONObject ret = new JSONObject();
        for (String var : map.keySet()) {
            String val = map.get(var);
            try {
                if (val.matches(rexNumber)) {
                    ret.put(var, Integer.parseInt(val));
                } else if (val.matches(rexFloat)) {
                    ret.put(var, Double.parseDouble(val));
                } else if (val.matches(rexBoolean)) {
                    ret.put(var,
                            val.equals("on") || val.equals("true"));
                } else {
                    ret.put(var, val);
                }
            } catch (JSONException ignored) {}
        }
        return ret;
    }

    public static String dateToString(Date date) {
        return mDateFormat.format(date);
    }
    public static String readAsset(Context ctx, String path) {
        InputStream assetStream = null;
        byte ret[] = null;
        try {
            assetStream = ctx.getAssets().open(path);
            ret = new byte[assetStream.available()];
            assetStream.read(ret);
        } catch (IOException e) {
            Log.w("Utils:readAsset", e);
        } finally {
            if (assetStream != null) {
                try {
                    assetStream.close();
                } catch (IOException e) {
                    Log.w("Utils:readAsset", e);
                }
            }
        }
        if (ret.length > 0) {
            return new String(ret);
        } else {
            return "";
        }
    }
    public static List<String> listFilesRecursively(File directory) {
        List<String> files = new ArrayList<>();
        Stack<File> stack = new Stack<>();
        stack.push(directory);
        while (!stack.isEmpty()) {
            File curDir = stack.pop();
            File[] curDirFiles = curDir.listFiles();
            if (curDirFiles != null) {
                for (File file : curDirFiles) {
                    if (file.isFile()) {
                        files.add(file.toPath().relativize(directory.toPath()).toString());
                    } else if (file.isDirectory()) {
                        stack.push(file);
                    }
                }
            }
        }
        return files;
    }
    public static List<String> listAssetsRecursively(Context ctx) {
        List<String> files = new ArrayList<>();
        AssetManager assetMgr = ctx.getAssets();
        Stack<String> dirStack = new Stack<>();
        dirStack.push("");
        while (!dirStack.isEmpty()) {
            String curPath = dirStack.pop();
            try {
                String[] curFiles = assetMgr.list(curPath);
                if (curFiles != null) {
                    for (String path : curFiles) {
                        String fullPath = (curPath.length()>0 ? curPath + "/" : "") + path;
                        if (assetMgr.list(fullPath).length > 0) {
                            dirStack.push(fullPath);
                        } else {
                            files.add(fullPath);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return files;
    }
    public static void extractAssets(Context ctx) {
        String dataPath = ctx.getDataDir().getPath();
        if (!dataPath.endsWith("/"))
            dataPath += "/";
        for (String path : listAssetsRecursively(ctx)) {
            saveAsset(ctx, path, dataPath+path);
        }
    }
    public static void saveAsset(Context ctx, String assetPath, String outputFile) {
        byte[] buff = new byte[256];
        InputStream aStream = null;
        OutputStream fStream = null;
        try {
            try {
                Files.createDirectories(Paths.get(outputFile).getParent());
            } catch (IOException ignored) {}
            File file = new File(outputFile);
            aStream = ctx.getAssets().open(assetPath);
            fStream = Files.newOutputStream(file.toPath());
            while (aStream.available() > 0) {
                int size = aStream.read(buff, 0, buff.length);
                if (size <= 0) {
                    break;
                }
                fStream.write(buff, 0, size);
            }
        } catch (IOException e) {
            Log.w("Utils:saveAsset", e);
        }
        if (aStream != null)
            try { aStream.close(); } catch (IOException ignored) {}
        if (fStream != null)
            try { fStream.close(); } catch (IOException ignored) {}
    }
}
