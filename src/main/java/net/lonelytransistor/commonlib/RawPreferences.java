package net.lonelytransistor.commonlib;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import kotlin.NotImplementedError;

class RawPreferences extends XmlPreferences {
    private final ByteArrayInputStream mInputStream;
    RawPreferences(byte[] data) {
        mInputStream = new ByteArrayInputStream(data);
        loadFromDiskSync();
    }

    @Override
    protected void loadFromDiskAsync() {
        loadFromDiskSync();
    }
    @Override
    protected void loadFromDiskSync() {
        try {
            loadFromDisk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected Map loadFromDisk() throws IOException, XmlPullParserException {
        if (mMap == null) {
            mMap = XmlUtils.readMapXml(mInputStream);
        }
        return mMap;
    }

    @Override
    protected void writeToDiskAsync() {
        writeToDiskSync();
    }
    @Override
    protected boolean writeToDiskSync() {
        try {
            return writeToDisk();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    protected boolean writeToDisk() throws IOException, XmlPullParserException {
        throw new UnsupportedOperationException("Read-only implementation.");
    }
    @Override
    public void registerSimpleChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("Static implementation.");
    }
    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("Static implementation.");
    }
    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        throw new UnsupportedOperationException("Static implementation.");
    }
}
