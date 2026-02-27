package com.lody.virtual.client.stub;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;

import java.io.File;

/**
 * @author Lody
 *
 */
public class DaemonService extends Service {

    private static final int NOTIFY_ID = 1001;
    private static final String CHANNEL_ID = "virtual_daemon_channel";

    static boolean showNotification = true;

    public static void startup(Context context) {
        File flagFile = context.getFileStreamPath(Constants.NO_NOTIFICATION_FLAG);
        if (Build.VERSION.SDK_INT >= 25 && flagFile.exists()) {
            showNotification = false;
        }

        context.startService(new Intent(context, DaemonService.class));
        if (VirtualCore.get().isServerProcess()) {
            DaemonJobService.scheduleJob(context);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startup(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (!showNotification) {
            return;
        }

        // ایجاد کانال نوتیفیکیشن برای اندروید 8+
        createNotificationChannel();

        // ساخت نوتیفیکیشن معتبر
        Notification notification = buildNotification();

        // شروع سرویس داخلی و foreground
        startService(new Intent(this, InnerService.class));
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daemon Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Channel for daemon foreground service");
            channel.setShowBadge(false);

            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Notification.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }

        return builder
                .setContentTitle("VirtualApp")
                .setContentText("سرویس‌های مجازی در حال اجرا هستند")
                .setSmallIcon(android.R.drawable.ic_menu_slideshow)
                .setPriority(Notification.PRIORITY_LOW)
                .build();
    }

    public static final class InnerService extends Service {

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel();
                Notification notification = buildNotification();
                startForeground(NOTIFY_ID, notification);
            } else {
                startForeground(NOTIFY_ID, new Notification());
            }
            stopForeground(true);
            stopSelf();
            return super.onStartCommand(intent, flags, startId);
        }

        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Daemon Service Channel",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Channel for daemon foreground service");
                channel.setShowBadge(false);

                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.createNotificationChannel(channel);
            }
        }

        private Notification buildNotification() {
            Notification.Builder builder;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                builder = new Notification.Builder(this, CHANNEL_ID);
            } else {
                builder = new Notification.Builder(this);
            }

            return builder
                    .setContentTitle("VirtualApp")
                    .setContentText("سرویس‌های مجازی در حال اجرا هستند")
                    .setSmallIcon(android.R.drawable.ic_menu_slideshow)
                    .setPriority(Notification.PRIORITY_LOW)
                    .build();
        }

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }
}
