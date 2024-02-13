package net.lonelytransistor.commonlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Preferences {
    private static final String TAG = "Preferences";
    private final SharedPreferences sharedPrefs;
    public Preferences(Context ctx, String key) {
        sharedPrefs = ctx.getSharedPreferences(key, Context.MODE_PRIVATE);
    }
    public Preferences(SharedPreferences prefs) {
        sharedPrefs = prefs;
    }
    public Preferences(String path) {
        sharedPrefs = new XmlPreferences(path);
    }
    public Preferences(File file) {
        sharedPrefs = new XmlPreferences(file);
    }
    public Preferences(byte[] data) {
        sharedPrefs = new RawPreferences(data);
    }

    public int getInt(String name) {
        return getInt(name, 0);
    }
    public int getInt(String name, int defValue) {
        return sharedPrefs.getInt(name, defValue);
    }
    public void setInt(String name, int data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(name, data);
        editor.apply();
    }
    public long getLong(String name) {
        return getLong(name, 0);
    }
    public long getLong(String name, long defValue) {
        return sharedPrefs.getLong(name, defValue);
    }
    public void setLong(String name, long data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(name, data);
        editor.apply();
    }

    public float getFloat(String name) {
        return getFloat(name, 0.0f);
    }
    public float getFloat(String name, float defValue) {
        return sharedPrefs.getFloat(name, defValue);
    }
    public void setFloat(String name, float data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat(name, data);
        editor.apply();
    }

    public boolean getBoolean(String name) {
        return getBoolean(name, false);
    }
    public boolean getBoolean(String name, boolean defValue) {
        return sharedPrefs.getBoolean(name, defValue);
    }
    public void setBoolean(String name, boolean data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(name, data);
        editor.apply();
    }

    public String getString(String name) {
        return getString(name, "");
    }
    public String getString(String name, String defValue) {
        return sharedPrefs.getString(name, defValue);
    }
    public void setString(String name, String data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(name, data);
        editor.apply();
    }

    public Set<String> getStringSet(String name) {
        return getStringSet(name, new HashSet<>());
    }
    public Set<String> getStringSet(String name, Set<String> defValue) {
        return sharedPrefs.getStringSet(name, defValue);
    }
    public void setStringSet(String name, Set<String> data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, data);
        editor.apply();
    }

    public Map<?,?> getMap(String name) {
        return getMap(name, new HashMap<>());
    }
    public Map<?,?> getMap(String name, Map<?,?> defValue) {
        try {
            Map<?,?> ret = getMapSer(name);
            return ret.isEmpty() ? defValue : ret;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void setMap(String name, Map<?,?> data) {
        if (data.isEmpty()) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putStringSet(name, new HashSet<>());
            editor.apply();
        } else {
            Object key = data.keySet().toArray()[0];
            Object value = data.get(key);
            try {
                if (key instanceof Serializable && value instanceof Serializable) {
                    setMapSer(name, (Map<Serializable, Serializable>) data);
                } else {
                    throw new ClassCastException(value.getClass().getName() + " is not serializable.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<?> getList(String name) {
        return getList(name, new ArrayList<>());
    }
    public List<?> getList(String name, List<?> defValue) {
        try {
            List<?> ret = getListSer(name);
            return ret.isEmpty() ? defValue : ret;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public void setList(String name, Collection<?> data) {
        setList(name, new ArrayList<>(data));
    }
    public void setList(String name, List<?> data) {
        if (data.isEmpty()) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putStringSet(name, new HashSet<>());
            editor.apply();
        } else {
            Object key = data.toArray()[0];
            try {
                if (key instanceof Serializable) {
                    setListSer(name, (List<Serializable>) data);
                } else {
                    throw new ClassCastException("Not serializable.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Set<?> getMapSet(String name) {
        return getMapSet(name, new HashSet<>());
    }
    public Set<?> getMapSet(String name, Set<?> defValue) {
        Set<Map<?, ?>> ret = new HashSet<>();
        for (String subName : getStringSet(name)) {
            ret.add(getMap(subName));
        }
        return ret.isEmpty() ? defValue : ret;
    }
    public void setMapSet(String name, Set<?> data) {
        if (data.isEmpty()) {
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putStringSet(name, new HashSet<>());
            editor.apply();
        } else {
            Object mapObj = data.toArray()[0];
            if (mapObj instanceof Map<?,?>) {
                Set<String> names = new HashSet<>();
                for (Map<Serializable, Serializable> map : (Set<Map<Serializable, Serializable>>) data) {
                    String subName = name + ":" + names.size();
                    names.add(subName);
                    setMap(subName, map);
                }
                setStringSet(name, names);
            } else {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putStringSet(name, new HashSet<>());
                editor.apply();
            }
        }
    }


    public void remove(String name) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        try {
            Set<String> mapSet = getStringSet(name);
            for (String subName : mapSet) {
                editor.remove(subName);
            }
        } catch (ClassCastException ignored) {}
        editor.remove(name);
        editor.apply();
    }
    public void clear() {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.clear();
        editor.apply();
    }
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPrefs.registerOnSharedPreferenceChangeListener(listener);
    }
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }
    public void registerSimpleChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        if (sharedPrefs instanceof XmlPreferences) {
            sharedPrefs.registerOnSharedPreferenceChangeListener(listener);
        } else {
            registerOnSharedPreferenceChangeListener(listener);
        }
    }
    public byte[] getXml() {
        ByteArrayOutputStream2 outputStream = new ByteArrayOutputStream2();
        try {
            XmlUtils.writeMapXml(sharedPrefs.getAll(), outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }



    private static Serializable strToObj(String s) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Serializable o = (Serializable) ois.readObject();
        ois.close();
        return o;
    }
    private static String objToStr(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private Map<Serializable, Serializable> getMapSer(String name) throws IOException, ClassNotFoundException {
        Map<Serializable, Serializable> map = new HashMap<>();
        for (String rawData : getStringSet(name)) {
            String[] data = rawData.split(":", 2);
            if (data[0].matches("^[0-9]+ [0-9]+$")) {
                String[] dataBndStr = data[0].split(" ");
                int[] dataBnd = {Integer.parseInt(dataBndStr[0]), Integer.parseInt(dataBndStr[1])};
                try {
                    map.put(strToObj(data[1].substring(0, dataBnd[0])), strToObj(data[1].substring(dataBnd[0])));
                } catch (InvalidClassException e) {
                    Log.i(TAG, "Corrupted library.");
                    break;
                }
            }
        }
        return map;
    }
    private void setMapSer(String name, Map<Serializable, Serializable> data) throws IOException {
        Set<String> set = new HashSet<>();
        for (Serializable keyS : data.keySet()) {
            String key = objToStr(keyS);
            Serializable valS = data.get(keyS);
            if (valS != null) {
                String val = objToStr(valS);
                set.add(key.length() + " " + val.length() + ":" + key + val);
            }
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, set);
        editor.apply();
    }

    private List<Serializable> getListSer(String name) throws IOException, ClassNotFoundException {
        List<Serializable> list = new ArrayList<>();
        for (String rawData : getStringSet(name)) {
            String[] data = rawData.split(":", 2);
            if (data[0].matches("^[0-9]+$")) {
                int dataBnd = Integer.parseInt(data[0]);
                try {
                    list.add(strToObj(data[1].substring(0, dataBnd)));
                } catch (InvalidClassException e) {
                    Log.i(TAG, "Corrupted library.", e);
                    break;
                }
            }
        }
        return list;
    }
    private void setListSer(String name, List<Serializable> data) throws IOException {
        Set<String> set = new HashSet<>();
        for (Serializable keyS : data) {
            String val = objToStr(keyS);
            set.add(val.length() + ":" + val);
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, set);
        editor.apply();
    }

    private static class ByteArrayOutputStream2 extends ByteArrayOutputStream {
        ByteArrayOutputStream2() {
            super(1024);
        }
        @Override
        public synchronized void write(int b) {
            buf[count] = (byte) b;
            count += 1;
        }
        @Override
        public synchronized void write(byte[] b, int off, int len) {
            Objects.checkFromIndexSize(off, len, b.length);
            ensureCapacity(count + len);
            System.arraycopy(b, off, buf, count, len);
            count += len;
        }
        public byte[] getBytes() {
            return buf;
        }
        private void ensureCapacity(int minCapacity) {
            if (minCapacity - buf.length > 0) {
                int capacity = buf.length;
                while (capacity < minCapacity) {
                    capacity += 1024;
                }
                buf = Arrays.copyOf(buf, capacity);
            }
        }
    };
}
