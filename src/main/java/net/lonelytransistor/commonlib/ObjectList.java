package net.lonelytransistor.commonlib;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class ObjectList<T> extends ArrayList<T> {
    @Override
    public boolean contains(@Nullable Object o) {
        if (o == null)
            return false;
        for (T el : this) {
            if (el.equals(o)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean remove(@Nullable Object o) {
        if (o == null)
            return false;
        for (int ix=0; ix<size(); ix++) {
            if (get(ix).equals(o)) {
                remove(ix);
                return true;
            }
        }
        return false;
    }
    private Object removeDelayedObj;
    public boolean removeDelayed(@Nullable Object o) {
        boolean ret = remove(removeDelayedObj);
        removeDelayedObj = o;
        return ret;
    }
}
