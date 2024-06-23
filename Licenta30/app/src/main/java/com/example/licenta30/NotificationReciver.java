package com.example.licenta30;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReciver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        MessageNotificationService service = new MessageNotificationService(context);
        service.showNotification(Message.getMessage(), "");
    }
}
