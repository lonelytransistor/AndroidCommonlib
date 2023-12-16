package net.lonelytransistor.commonlib;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class ForegroundService extends Service {
    private static final String INTENT_START = "start";
    private static final String INTENT_STOP = "stop";
    private static final String INTENT_RESTART = "restart";
    private static final String PERSISTENT_NOTIFICATION_ID = "persistent_notification_1";
    private static final int SERVICE_ID = 1;

    private Notification notification;
    private final NotificationChannel notificationChannel = new NotificationChannel(
            PERSISTENT_NOTIFICATION_ID, "Persistent notification",
            NotificationManager.IMPORTANCE_DEFAULT);

    public static void start(Context context, Class<?> klass) {
        Utils.signalService(context, INTENT_START, klass);
    }
    public static void stop(Context context, Class<?> klass) {
        Utils.signalService(context, INTENT_STOP, klass);
    }
    public static void restart(Context context, Class<?> klass) {
        Utils.signalService(context, INTENT_RESTART, klass);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(INTENT_START)) {
            createNotification();
            startForeground(SERVICE_ID, notification);
            return START_STICKY;
        } else if (intent.getAction().equals(INTENT_STOP)) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        } else if (intent.getAction().equals(INTENT_RESTART)) {
            stopForeground(true);
            stopSelf();
            createNotification();
            startForeground(SERVICE_ID, notification);
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }
    private void createNotification() {
        NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        nm.createNotificationChannel(notificationChannel);
        notification = new Notification.Builder(this, PERSISTENT_NOTIFICATION_ID)
                .setContentTitle("Running notification service")
                .setSmallIcon(androidx.leanback.R.drawable.lb_text_dot_one_small)
                .build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
