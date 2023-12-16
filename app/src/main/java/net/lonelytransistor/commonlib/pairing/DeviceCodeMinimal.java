package net.lonelytransistor.commonlib.pairing;

import android.util.Log;

import androidx.annotation.NonNull;

import net.lonelytransistor.commonlib.http.IPv4Address;

public class DeviceCodeMinimal extends DeviceCode {
    private final int port;
    private static int codeSize = 0;

    public static int getCodeSize() {
        if (codeSize == 0) {
            codeSize = new DeviceCodeMinimal(0xFFF, 0xFF).code.length();
        }
        return codeSize;
    }
    public static boolean isValidPort(int port) {
        return port>=9000 && port<=(9000+0xFF);
    }
    public int getPort() {
        return port;
    }
    @NonNull
    @Override
    public String toString() {
        return getCode();
    }

    public DeviceCodeMinimal(String code) {
        this(code, IPv4Address.getAll()[0]);
    }
    public DeviceCodeMinimal(String code, IPv4Address ip) {
        long data = decode(code);
        this.code = code;
        this.payload = (data >> 8 * 2) & 0xFFF;
        this.port = ((int) (data >> 8) & 0xFF) + 9000;
        this.ip = ip;
        this.ip.ip[3] = (int) ((data) & 0xFF);
        this.ip.device = this.ip.ip[3];
    }
    public DeviceCodeMinimal(IPv4Address ip, long payload, int port) {
        long data = 0;
        data += (payload & 0xFFF) << 8 * 2;
        data += (long) ((port - 9000) & 0xFF) << 8;
        data += ip.ip[3] & 0xFF;
        Log.i("data", "Data:" + data);
        this.code = encode(data);
        while (this.code.length() < 6) {
            this.code += "0";
        }
        this.ip = ip;
        this.payload = payload;
        this.port = port;
    }
    public DeviceCodeMinimal(long payload, int port) {
        this(IPv4Address.getAll()[0], payload, port);
    }
    public DeviceCodeMinimal(int port) {
        this(((long) Math.floor(Math.random() * 0xFFF)), port);
    }
}