package net.lonelytransistor.commonlib.http;

import androidx.annotation.NonNull;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class IPv4Address {
    public int networkClass;
    public int network;
    public int subnet;
    public int device;
    public int[] ip;

    public static IPv4Address[] getAll() {
        ArrayList<IPv4Address> ret = new ArrayList<>();
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(
                        intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        if (sAddr!=null && sAddr.contains(".") && !sAddr.contains(":")) {
                            ret.add(new IPv4Address(sAddr));
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
        return ret.toArray(new IPv4Address[0]);
    }
    public IPv4Address(int data) {
        constructor(data >> 8*3 & 0xFF,
                data >> 8*2 & 0xFF,
                data >> 8   & 0xFF,
                data        & 0xFF);
    }
    public IPv4Address(String data) {
        String[] ipStr = data.split("\\.");
        if (ipStr.length == 4) {
            constructor(Integer.parseInt(ipStr[0]), Integer.parseInt(ipStr[1]),
                    Integer.parseInt(ipStr[2]), Integer.parseInt(ipStr[3]));
        } else {
            IPv4Address[] ips = getAll();
            if (ips.length > 0) {
                constructor(ips[0].ip[0], ips[0].ip[1], ips[0].ip[2], ips[0].ip[3]);
            } else {
                constructor(127, 0, 0, 1);
            }
        }
    }
    public IPv4Address(int p3, int p2, int p1, int p0) {
        constructor(p3, p2, p1, p0);
    }

    public static boolean isValid(String data) {
        return data.matches("^([0-9]{1,3}\\.){4}$");
    }

    private void constructor(int p3, int p2, int p1, int p0) {
        ip = new int[]{p3, p2, p1, p0};
        networkClass = ip[0];
        network = ip[1];
        subnet = ip[2];
        device = ip[3];
    }
    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                ip[0], ip[1], ip[2], ip[3]);
    }
}