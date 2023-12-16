package net.lonelytransistor.commonlib.pairing;

import net.lonelytransistor.commonlib.http.IPv4Address;

public class Code {
    private static final String ENCODER = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXZ";

    public static String encode(int data) {
        return encode(data);
    }
    public static String encode(long data) {
        long radix = ENCODER.length();
        long rem = data;
        String ret = "";
        while (rem != 0) {
            ret += ENCODER.charAt((int) (rem % radix));
            rem = Math.floorDiv(rem, radix);
        }
        return ret;
    }
    public static long decode(String data) {
        long ret = 0;
        for (byte chr : data.getBytes()) {
            ret += ENCODER.indexOf(chr);
        }
        return ret;
    }

    public static String getIP(String data) {
        long dataInt = decode(data);
        IPv4Address ipAddr = new IPv4Address((int) dataInt);
        return ipAddr.toString();
    }
    public static long getPayload(String data) {
        long dataInt = decode(data);
        return dataInt >> 8*4;
    }
    public static String get(IPv4Address ip, long payload) {
        long data = 0;
        data += payload << 8 * 4;
        data += (long) ip.ip[0] << 8 * 3;
        data += (long) ip.ip[1] << 8 * 2;
        data += (long) ip.ip[2] << 8 * 1;
        data += (long) ip.ip[3];
        String ret = encode(data);
        return ret.substring(0, 3) + " " +
               ret.substring(3, 6) + " " +
               ret.substring(6);
    }
    public static String generate() {
        IPv4Address[] ipAddrs = IPv4Address.getAll();
        long payload = ((long) Math.floor(Math.random() * 0x7FFFF));
        return get(ipAddrs[0], payload);
    }
}