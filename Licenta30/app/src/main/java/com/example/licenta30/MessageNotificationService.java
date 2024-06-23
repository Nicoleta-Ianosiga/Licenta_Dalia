package com.example.licenta30;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class MessageNotificationService {
    private static MessageNotificationService instance;

    private Context context;
    public static final String MY_UUID = ("bluetooth_service");
    private NotificationManager notificationManager ;
    public MessageNotificationService(Context context) {
        this.context = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
    public static synchronized MessageNotificationService getInstance(Context context){
        if (instance == null) {
            instance = new MessageNotificationService(context);
        }
        return instance;
    }

    public void showNotification(String content, String title){
        Intent activityIntent = new Intent(context, MainActivity.class);
        PendingIntent activity = PendingIntent.getActivity(context, 1, activityIntent,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)? PendingIntent.FLAG_IMMUTABLE : 0);

        PendingIntent newMessageIntent = PendingIntent.getBroadcast(context,
                2,
                new Intent(context, NotificationReciver.class),
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)? PendingIntent.FLAG_IMMUTABLE : 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, MY_UUID)
                .setSmallIcon(R.drawable.baseline_notification_important_24)
                .setContentTitle(title)
                .setContentText(content)
                .setContentIntent(activity);

        notificationManager.notify(1, notification.build());
    }

}
