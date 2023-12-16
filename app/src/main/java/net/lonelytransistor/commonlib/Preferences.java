package net.lonelytransistor.commonlib;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.lonelytransistor.commonlib.pairing.DeviceData;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Preferences {
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

    public Map<String, String> getMap(String name) {
        Map<String, String> map = new HashMap<>();
        for (String rawData : getStringSet(name)) {
            String[] data = rawData.split(":", 2);
            if (data[0].matches("^[0-9]+ [0-9]+$")) {
                String[] dataBndStr = data[0].split(" ");
                int[] dataBnd = {Integer.parseInt(dataBndStr[0]), Integer.parseInt(dataBndStr[1])};
                map.put(data[1].substring(0, dataBnd[0]), data[1].substring(dataBnd[0]));
            }
        }
        return map;
    }
    public void setMap(String name, Map<String, String> data) {
        Set<String> set = new HashSet<>();
        for (String key : data.keySet()) {
            String val = data.get(key);
            if (val != null) {
                set.add(key.length() + " " + val.length() + ":" + key + val);
            }
        }
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putStringSet(name, set);
        editor.apply();
    }

    public Set<Map<String, String>> getMapSet(String name) {
        Set<Map<String, String>> ret = new HashSet<>();
        for (String subName : getStringSet(name)) {
            ret.add(getMap(subName));
        }
        return ret;
    }
    public void setMapSet(String name, Set<Map<String, String>> data) {
        remove(name);
        Set<String> names = new HashSet<>();
        for (Map<String,String> map : data) {
            String subName = name + ":" + names.size();
            names.add(subName);
            setMap(subName, map);
        }
        setStringSet(name, names);
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
