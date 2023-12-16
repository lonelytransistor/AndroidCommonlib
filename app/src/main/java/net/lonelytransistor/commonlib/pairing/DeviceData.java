package net.lonelytransistor.commonlib.pairing;

import net.lonelytransistor.commonlib.R;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeviceData {
    public static final int FLAG_RESTRICTED = 0b0010;
    public enum Type {
        UNKNOWN,
        TABLET,
        MOBILE,
        SERVER,
        ROUTER,
        HOME,
        SMARTHOME,
        PC,
        DESKTOP,
        LAPTOP
    }

    public final Type type;
    public final String name;
    public final Date datePaired;

    private List<String> apps;
    private Date lastActive;
    public int flags;
    private boolean mRestricted;
    public final String pairingCode;

    public int getLogo() {
        switch (type) {
            case TABLET:
                return R.drawable.device_tablet;
            case MOBILE:
                return R.drawable.device_mobile;
            case DESKTOP:
            case PC:
                return R.drawable.device_desktop;
            case LAPTOP:
                return R.drawable.device_laptop;
            case SERVER:
                return R.drawable.device_server;
            case ROUTER:
                return R.drawable.device_router;
            case HOME:
            case SMARTHOME:
                return R.drawable.device_home;
            case UNKNOWN:
            default:
                return R.drawable.device_unknown;
        }
    }
    public boolean restrict(boolean state) {
        flags = state ? (flags | FLAG_RESTRICTED) : (flags & ~FLAG_RESTRICTED);
        mRestricted = state;
        return mRestricted;
    }
    public boolean isRestricted() {
        return mRestricted;
    }
    public void touch() {
        lastActive = new Date();
    }
    public Date getLastActive() {
        return lastActive;
    }
    public List<String> getEnabledApps() {
        return apps;
    }
    public void setAppEnabled(String appName, boolean enabled) {
        if (!apps.contains(appName) && enabled) {
            apps.add(appName);
        } else if (apps.contains(appName) && !enabled) {
            apps.remove(appName);
        }
    }
    public boolean isAppEnabled(String appName) {
        return apps.contains(appName);
    }
    public Map<String,String> toMap() {
        Map<String,String> ret = new HashMap<>();
        ret.put("type", String.valueOf(type.ordinal()));
        ret.put("datePaired", String.valueOf(datePaired.getTime()));
        ret.put("lastActive", String.valueOf(lastActive.getTime()));
        ret.put("pairingCode", pairingCode);
        ret.put("flags", String.valueOf(flags));

        StringBuilder appsStr = new StringBuilder();
        for (String appName : apps) {
            appsStr.append(apps).append(",");
        }
        if (appsStr.length() > 0) {
            appsStr.deleteCharAt(appsStr.length() - 1);
        }
        ret.put("apps", appsStr.toString());

        return ret;
    }

    public DeviceData(Map<String,String> data) {
        this.type = Type.values()[Integer.parseInt(data.get("type"))];
        this.name = data.get("name");
        this.datePaired = new Date(Long.parseLong(data.get("datePaired")));
        this.lastActive = new Date(Long.parseLong(data.get("lastActive")));
        this.pairingCode = data.get("pairingCode");
        this.flags = Integer.parseInt(data.get("flags"));
        this.apps = Arrays.asList(data.get("apps").split("\\s*,\\s*"));
        mRestricted = (flags & FLAG_RESTRICTED) != 0;
    }
    public DeviceData(Type type, String name, Date datePaired, Date lastActive, String pairingCode, int flags, List<String> apps) {
        this.type = type;
        this.name = name;
        this.datePaired = datePaired;
        this.lastActive = lastActive;
        this.pairingCode = pairingCode;
        this.flags = flags;
        this.apps = apps;
        mRestricted = (flags & FLAG_RESTRICTED) != 0;
    }
}