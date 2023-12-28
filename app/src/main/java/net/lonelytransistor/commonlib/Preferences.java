package net.lonelytransistor.commonlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Preferences {
    private static final String TAG = "Preferences";
    private final SharedPreferences sharedPrefs;
    public Preferences(Context ctx, String key) {
        sharedPrefs = ctx.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public int getInt(String name) {
        return sharedPrefs.getInt(name, 0);
    }
    public void setInt(String name, int data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putInt(name, data);
        editor.apply();
    }
    public long getLong(String name) {
        return sharedPrefs.getLong(name, 0);
    }
    public void setLong(String name, long data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong(name, data);
        editor.apply();
    }

    public float getFloat(String name) {
        return sharedPrefs.getFloat(name, 0.0f);
    }
    public void setFloat(String name, float data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat(name, data);
        editor.apply();
    }

    public boolean getBoolean(String name) {
        return sharedPrefs.getBoolean(name, false);
    }
    public void setBoolean(String name, boolean data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(name, data);
        editor.apply();
    }

    public String getString(String name) {
        return sharedPrefs.getString(name, "");
    }
    public void setString(String name, String data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(name, data);
        editor.apply();
    }

    public Set<String> getStringSet(String name) {
        return sharedPrefs.getStringSet(name, new HashSet<>());
    }
    public void setStringSet(String name, Set<String> data) {
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, data);
        editor.apply();
    }

    private static Serializable strToObj(String s) throws IOException, ClassNotFoundException {
        byte [] data = Base64.getDecoder().decode(s);
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
        Serializable o = (Serializable) ois.readObject();
        ois.close();
        return o;
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
    public Map<?, ?> getMap(String name) {
        try {
            return getMapSer(name);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static String objToStr(Serializable o) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(o);
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
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
                    throw new ClassCastException("Not serializable.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public Set<?> getMapSet(String name) {
        Set<Map<?, ?>> ret = new HashSet<>();
        for (String subName : getStringSet(name)) {
            ret.add(getMap(subName));
        }
        return ret;
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
}
