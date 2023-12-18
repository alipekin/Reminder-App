package com.example.myapplication;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "Hatirlatici_Kanal";
    private static final int NOTIFICATION_ID = 123;
    private Ringtone ringtone;

    @Override
    public void onReceive(Context context, Intent intent) {

            String description = intent.getStringExtra("description");
            String ringtoneUriString = intent.getStringExtra("ringtone");
            int offsetValueInt = intent.getIntExtra("offsetValue", 0);
            String offsetValue = Integer.toString(offsetValueInt);
            boolean isNotification = intent.getBooleanExtra("isNotification", false);

            if (isNotification) {
                description = offsetValue + " dakika sonra " + description +
                        " isimli bir hatırlatmanız var" ;
                bildirimGonder(context, description);
                } else {
                    try {
                        Uri ringtoneUri = Uri.parse(ringtoneUriString);
                        ringtone = RingtoneManager.getRingtone(context, ringtoneUri);
                        ringtone.play();
                        RingtoneManager ringMan = new RingtoneManager(context);
                        ringMan.stopPreviousRingtone();
                        Toast.makeText(context, "alarm", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Hatırlatma açıklamasını göster
                Toast.makeText(context, description, Toast.LENGTH_SHORT).show();
        }

    private void bildirimGonder(Context context, String description) {

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Hatırlatıcı Kanal"; // Kanal adı
            String channelDescription = "Hatırlatıcı Kanal"; // Kanal açıklaması
            int importance = NotificationManager.IMPORTANCE_HIGH; // Kanal önemi

            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    channelName, importance);
            notificationChannel.setDescription(channelDescription);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("Hatırlatma")
                .setContentText(description)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}


