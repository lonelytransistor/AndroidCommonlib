package net.lonelytransistor.commonlib.pairing;

import android.util.Log;

import androidx.annotation.NonNull;

import net.lonelytransistor.commonlib.Utils;
import net.lonelytransistor.commonlib.http.IPv4Address;

public class DeviceCode {
    private static final String ENCODER = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXZ";
    public static String encode(long data) {
        long radix = ENCODER.length();
        long rem = data;

        StringBuilder ret = new StringBuilder();
        while (rem != 0) {
            ret.append(ENCODER.charAt((int) (rem % radix)));
            rem = Math.floorDiv(rem, radix);
        }
        return ret.toString();
    }
    public static long decode(String data) {
        long radix = ENCODER.length();
        long ret = 0;

        for (byte chr : Utils.reverseString(data).getBytes()) {
            ret *= radix;
            ret += ENCODER.indexOf(chr);
        }
        return ret;
    }

    protected IPv4Address ip;
    protected long payload;
    protected String code;

    public IPv4Address getIP() {
        return ip;
    }
    public long getPayload() {
        return payload;
    }
    public String getCode() {
        return code;
    }
    public String getCodePretty() {
        StringBuilder ret = new StringBuilder();
        for (int ix=0; ix<code.length(); ix+=3) {
            ret.append(
                    code.substring(ix,
                            Math.min(ix + 3, code.length())))
                .append(" ");
        }
        return ret.toString().replaceAll("\\s+$", "");
    }
    @NonNull
    @Override
    public String toString() {
        return getCode();
    }

    public DeviceCode(String code) {
        long data = decode(code);
        this.code = code;
        this.payload = data >> 8 * 4;
        this.ip = new IPv4Address(
                (int) ((data >> 8 * 3) & 0xFF),
                (int) ((data >> 8 * 2) & 0xFF),
                (int) ((data >> 8) & 0xFF),
                (int) ((data) & 0xFF));
    }
    public DeviceCode(IPv4Address ip, long payload) {
        long data = 0;
        data += payload << 8 * 4;
        data += (long) ip.ip[0] << 8 * 3;
        data += (long) ip.ip[1] << 8 * 2;
        data += (long) ip.ip[2] << 8;
        data += (long) ip.ip[3];
        this.code = encode(data);
        this.ip = ip;
        this.payload = payload;
    }
    public DeviceCode(long payload) {
        this(IPv4Address.getAll()[0], payload);
    }
    public DeviceCode(IPv4Address ip) {
        this(ip, ((long) Math.floor(Math.random() * 0x7FFFF)));
    }
    public DeviceCode() {
        this(IPv4Address.getAll()[0]);
    }
}