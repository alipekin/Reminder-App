package com.example.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.Manifest;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;

public class EkleActivitiy extends AppCompatActivity {


    private static final int DEPOLAMA_IZIN_KODU = 100;

    private DatabaseReference databaseRef;
    private HashMap<String,Object> mData;
    private FirebaseAuth kimlikDogFirebase;
    private FirebaseUser userFirebase;
    private ActivityResultLauncher<String> selectRingtoneLauncher;
    private Uri selectedRingtoneUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ekle_activitiy);

        kimlikDogFirebase = FirebaseAuth.getInstance();

        TimePicker timePicker = findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        selectRingtoneLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                uri -> {
            if (uri != null) {
                selectedRingtoneUri = uri;
                Cursor cursor = null;
                String displayName = "";
                try {
                    cursor = getContentResolver().query(uri,
                            null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        if (displayNameIndex != -1) {
                            displayName = cursor.getString(displayNameIndex);
                            TextView selectedRingtoneText = findViewById(R.id.selectedRingtoneText);
                            selectedRingtoneText.setVisibility(View.VISIBLE);
                            selectedRingtoneText.setText("Seçilen zil sesi: " + displayName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        });
    }


    private void depolamaErisimKontrol() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // İzin verilmemişse, kullanıcıdan izin iste
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, DEPOLAMA_IZIN_KODU);
        }
    }
    public void zil(View v) {
        // Dış depolama iznini kontrol et
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // İzin verildiyse, dosya seçiciyi başlat
            String mimeType = "audio/*";
            selectRingtoneLauncher.launch(mimeType);

        } else {
            depolamaErisimKontrol();
        }
    }
    public void hatırlatmaKaydet(View v){

        DatePicker datePicker = findViewById(R.id.datePicker);
        int day = datePicker.getDayOfMonth();
        int month = datePicker.getMonth();
        int year = datePicker.getYear();
        month += 1;

        TimePicker timePicker = findViewById(R.id.timePicker);
        int hour = timePicker.getHour();
        int minute = timePicker.getMinute();

        // Hatırlatma açıklamasını al
        EditText reminderDescriptionEditText = findViewById(R.id.reminderDescriptionEditText);
        String reminderDescription = reminderDescriptionEditText.getText().toString();

        EditText offsetEditText = findViewById(R.id.offsetEditText);
        String offsetValueStr = offsetEditText.getText().toString();
        int offsetValue;
        offsetValue = Integer.parseInt(offsetValueStr);
        // Alarm oluştur
        createAlarm(day, month, year, hour, minute, reminderDescription, selectedRingtoneUri,offsetValue);

        // Firebase Realtime Database'e bilgileri kaydet
        mData = new HashMap<>();

        String tarih = day + "/" + month + "/" + year;
        String stringMinute = (minute < 10) ? "0" + minute : String.valueOf(minute);
        String saat = hour + ":" + stringMinute;
        userFirebase = kimlikDogFirebase.getCurrentUser();
        mData.put("tarih",tarih);
        mData.put("saat",saat);
        mData.put("aciklama",reminderDescription);
        mData.put("offset",offsetValueStr);

        databaseRef = FirebaseDatabase.getInstance().getReference();
        String yeniHatirlaticiKey = databaseRef.push().getKey();
        databaseRef.child("Hatırlatmalar")
                .child(userFirebase.getUid())
                .child(yeniHatirlaticiKey).setValue(mData)
                .addOnCompleteListener(EkleActivitiy.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(EkleActivitiy.this, "Kayıt Başarılı",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EkleActivitiy.this, task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        Intent getIntent = getIntent();
        String email = getIntent.getStringExtra("email");

        Intent intent  = new Intent(EkleActivitiy.this,
                GirisActivity.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }

    private void createAlarm(int day, int month, int year, int hour, int minute,
                             String reminderDescription, Uri selectedRingtoneUri,int  offsetValue) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        month -= 1;

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("description", reminderDescription);
        alarmIntent.putExtra("ringtone", selectedRingtoneUri.toString());
        alarmIntent.putExtra("isNotification", false);

        java.util.Calendar alarmTarih = java.util.Calendar.getInstance();
        alarmTarih.set(java.util.Calendar.YEAR, year);
        alarmTarih.set(java.util.Calendar.MONTH, month);
        alarmTarih.set(java.util.Calendar.DAY_OF_MONTH, day);
        alarmTarih.set(java.util.Calendar.HOUR_OF_DAY, hour);
        alarmTarih.set(java.util.Calendar.MINUTE, minute);
        alarmTarih.set(java.util.Calendar.SECOND, 0);

        Intent bildirimIntent = new Intent(this, AlarmReceiver.class);
        bildirimIntent.putExtra("isNotification", true);
        bildirimIntent.putExtra("description", reminderDescription);
        bildirimIntent.putExtra("offsetValue", offsetValue);

        java.util.Calendar bildirimTarih = (java.util.Calendar) alarmTarih.clone();
        bildirimTarih.add(java.util.Calendar.MINUTE, -offsetValue);


        PendingIntent alarmPendingIntent= PendingIntent.getBroadcast(this,
                0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                alarmTarih.getTimeInMillis(), alarmPendingIntent);

        PendingIntent bildirimPendingIntent = PendingIntent.getBroadcast(this,
                1, bildirimIntent
                , PendingIntent.FLAG_UPDATE_CURRENT);


        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
                bildirimTarih.getTimeInMillis(), bildirimPendingIntent);



    }
}

