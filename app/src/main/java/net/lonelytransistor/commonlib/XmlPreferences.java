package net.lonelytransistor.commonlib;

import android.content.SharedPreferences;
import android.os.FileObserver;
import android.util.Log;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

class XmlPreferences implements SharedPreferences {
    private static final String TAG = "XmlPreferences";

    protected ReentrantLock mutex = new ReentrantLock();
    private File mFile;
    protected Map<String, Object> mMap;
    private long mLastModified;
    private final Map<OnSharedPreferenceChangeListener, FileObserver> mFileObservers = new HashMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    class Editor implements SharedPreferences.Editor {
        @Override
        public SharedPreferences.Editor putString(String key, @Nullable String value) {
            mMap.put(key, value);
            return this;
        }
        @Override
        public SharedPreferences.Editor putStringSet(String key, @Nullable Set<String> values) {
            mMap.put(key, values);
            return this;
        }
        @Override
        public SharedPreferences.Editor putInt(String key, int value) {
            mMap.put(key, value);
            return this;
        }
        @Override
        public SharedPreferences.Editor putLong(String key, long value) {
            mMap.put(key, value);
            return this;
        }
        @Override
        public SharedPreferences.Editor putFloat(String key, float value) {
            mMap.put(key, value);
            return this;
        }
        @Override
        public SharedPreferences.Editor putBoolean(String key, boolean value) {
            mMap.put(key, value);
            return this;
        }
        @Override
        public SharedPreferences.Editor remove(String key) {
            mMap.remove(key);
            return this;
        }
        @Override
        public SharedPreferences.Editor clear() {
            mMap.clear();
            return this;
        }
        @Override
        public boolean commit() {
            return writeToDiskSync();
        }
        @Override
        public void apply() {
            writeToDiskAsync();
        }
    }

    XmlPreferences() {}
    XmlPreferences(String path) {
        this(path, false);
    }
    XmlPreferences(String path, boolean async) {
        this(new File(path), async);
    }
    XmlPreferences(File file) {
        this(file, false);
    }
    XmlPreferences(File file, boolean async) {
        mFile = file;
        mLastModified = mFile.lastModified();
        if (async)
            loadFromDiskAsync();
        else
            loadFromDiskSync();
    }
    protected void loadFromDiskAsync() {
        executor.execute(() -> loadFromDiskSync());
    }
    protected void loadFromDiskSync() {
        try {
            mutex.lock();
            if (mFile.exists()) {
                mMap = loadFromDisk();
            } else {
                mMap = new HashMap<>();
            }
            mutex.unlock();
        } catch (Exception e) {
            Log.e(TAG, "Data corrupted!", e);
            mMap = new HashMap<>();
        }
    }
    protected Map loadFromDisk() throws IOException, XmlPullParserException {
        InputStream inputStream = new FileInputStream(mFile);
        Map map = XmlUtils.readMapXml(inputStream);
        inputStream.close();
        return map;
    }

    protected void writeToDiskAsync() {
        executor.execute(() -> writeToDiskSync());
    }
    protected boolean writeToDiskSync() {
        try {
            return writeToDisk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    protected boolean writeToDisk() throws IOException, XmlPullParserException {
        if (!mFile.exists()) {
            File parent = mFile.getParentFile();
            if (!parent.exists() && !parent.mkdirs())
                return false;
            if (!mFile.createNewFile())
                return false;
        }
        OutputStream outputStream = new FileOutputStream(mFile);
        mutex.lock();
        XmlUtils.writeMapXml(mMap, outputStream);
        mutex.unlock();
        outputStream.flush();
        outputStream.close();
        if (mFile.lastModified() != mLastModified) {
            mLastModified = mFile.lastModified();
            return true;
        }
        return false;
    }
    @Override
    public Map<String, ?> getAll() {
        return mMap;
    }
    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        String v = (String) mMap.get(key);
        return v != null ? v : defValue;
    }
    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        Set<String> v = (Set<String>) mMap.get(key);
        return v != null ? v : defValues;
    }
    @Override
    public int getInt(String key, int defValue) {
        Integer v = (Integer) mMap.get(key);
        return v != null ? v : defValue;
    }
    @Override
    public long getLong(String key, long defValue) {
        Long v = (Long) mMap.get(key);
        return v != null ? v : defValue;
    }
    @Override
    public float getFloat(String key, float defValue) {
        Float v = (Float) mMap.get(key);
        return v != null ? v : defValue;
    }
    @Override
    public boolean getBoolean(String key, boolean defValue) {
        Boolean v = (Boolean)mMap.get(key);
        return v != null ? v : defValue;
    }
    @Override
    public boolean contains(String key) {
        return mMap.containsKey(key);
    }

    @Override
    public Editor edit() {
        return new Editor();
    }

    public void registerSimpleChangeListener(OnSharedPreferenceChangeListener listener) {
        FileObserver observer = new FileObserver(mFile, FileObserver.MODIFY | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                executor.execute(() -> {
                    Map<String, Object> map;
                    try {
                        map = loadFromDisk();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    mutex.lock();
                    mMap = map;
                    mutex.unlock();
                    listener.onSharedPreferenceChanged(XmlPreferences.this, null);
                });
            }
        };
        mFileObservers.put(listener, observer);
        observer.startWatching();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        FileObserver observer = new FileObserver(mFile, FileObserver.CLOSE_WRITE | FileObserver.DELETE) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                executor.execute(() -> {
                    Map<String, Object> map;
                    try {
                        map = loadFromDisk();
                    } catch (Exception e) {
                        Log.e(TAG, "Reload has failed!", e);
                        return;
                    }
                    Set<String> changed = new HashSet<>();
                    mutex.lock();
                    for (String key : map.keySet()) {
                        if (!Objects.equals(map.get(key), mMap.get(key))) {
                            changed.add(key);
                        }
                    }
                    mMap = map;
                    mutex.unlock();
                    for (String key : changed) {
                        listener.onSharedPreferenceChanged(XmlPreferences.this, key);
                    }
                });
            }
        };
        mFileObservers.put(listener, observer);
        observer.startWatching();
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        if (!mFileObservers.containsKey(listener))
            return;
        mFileObservers.get(listener).stopWatching();
        mFileObservers.remove(listener);
    }
}
