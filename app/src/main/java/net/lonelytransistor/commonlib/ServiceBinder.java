package net.lonelytransistor.commonlib;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class ServiceBinder {
    public interface Request {
        void onFinished(Object binder);
    }
    private static class BinderInstance {
        Class<?> klass;
        Binder binder;
        ServiceConnection conn;
    }
    private static Map<Context, List<BinderInstance>> binders = new HashMap<>();
    public static Binder bind(Class<?> klass, Context ctx) {
        return bind(klass, ctx, (String) null);
    }
    public static Binder bind(Class<?> klass, Context ctx, String action) {
        CountDownLatch latch = new CountDownLatch(1);
        bind(klass, ctx, action, (binder) -> latch.countDown());
        try { latch.await(); } catch (InterruptedException ignored) {}
        return getBinder(klass, ctx).binder;
    }
    private static BinderInstance getBinder(Class<?> klass, Context ctx) {
        if (binders.containsKey(ctx)) {
            for (BinderInstance inst : binders.get(ctx)) {
                if (inst.klass == klass) {
                    return inst;
                }
            }
        }
        return null;
    }
    public static void bind(Class<?> klass, Context ctx, Request cb) {
        bind(klass, ctx, null, cb);
    }
    public static void bind(Class<?> klass, Context ctx, String action, Request cb) {
        if (binders.containsKey(ctx)) {
            cb.onFinished(getBinder(klass, ctx).binder);
            return;
        } else {
            binders.put(ctx, new ArrayList<>());
        }
        BinderInstance inst = new BinderInstance();
        inst.klass = klass;
        inst.conn = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder ibinder) {
                Binder binder = (Binder) ibinder;
                inst.binder = binder;
                cb.onFinished(binder);
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                binders.remove(ctx);
            }
        };
        Intent intent = new Intent(ctx, klass);
        if (action != null)
            intent.setAction(action);
        ctx.bindService(intent, inst.conn, Context.BIND_AUTO_CREATE);
        binders.get(ctx).add(inst);
    }
    private static void unbind(Context ctx, BinderInstance inst) {
        ctx.unbindService(inst.conn);
        binders.get(ctx).remove(inst);
        if (binders.get(ctx).size() == 0) {
            binders.remove(ctx);
        }
    }
    public static void unbindAll(Context ctx) {
        if (!binders.containsKey(ctx))
            return;
        for (BinderInstance inst : binders.get(ctx).toArray(new BinderInstance[]{})) {
            unbind(ctx, inst);
        }
    }
    public static void unbind(Class<?> klass, Context ctx) {
        if (!binders.containsKey(ctx))
            return;
        for (BinderInstance inst : binders.get(ctx)) {
            if (inst.klass == klass) {
                unbind(ctx, inst);
                return;
            }
        }
    }
    public static void unbind(Binder binder) {
        for (Context ctx : binders.keySet()) {
            for (BinderInstance inst : binders.get(ctx)) {
                if (inst.binder == binder) {
                    unbind(ctx, inst);
                    return;
                }
            }
        }
    }
}
