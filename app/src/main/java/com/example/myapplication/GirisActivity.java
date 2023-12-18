package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class GirisActivity extends AppCompatActivity {

    FirebaseAuth kimlikDogFirebase = FirebaseAuth.getInstance();
    private DatabaseReference databaseRef;
    private FirebaseUser userFirebase;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        goster();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_giris);
        recyclerView = findViewById(R.id.recyclerView);

        Intent getIntent = getIntent();
        String email = getIntent.getStringExtra("email");
        TextView kullaniciEmailTextView = findViewById(R.id.kullaniciEmailText);
        // Email değerini TextView'e ata
        String metin = "Kullanıcı Email: " + email;
        kullaniciEmailTextView.setText(metin);
    }
    public void ekle(View v){
        Intent getIntent = getIntent();
        String email = getIntent.getStringExtra("email");

        Intent intent  = new Intent(GirisActivity.this,
                EkleActivitiy.class);
        intent.putExtra("email",email);
        startActivity(intent);
    }
    public void alarmiSil(View v){

        TextView textViewTarihSaat = findViewById(R.id.textViewTarihSaat);
        TextView textViewAciklama = findViewById(R.id.textViewAciklama);
        TextView textViewOffset = findViewById(R.id.textViewOffset);

        String tarihSaatText = textViewTarihSaat.getText().toString();
        String aciklamaText = textViewAciklama.getText().toString();
        String offsetText = textViewOffset.getText().toString();

        String[] ilkAyirma = tarihSaatText.split("larm Tarih ve Saati: ");
        String[] ikinciAyirma = ilkAyirma[1].split(" ");

        String[] aciklamaAyir = aciklamaText.split("çıklama: ");
        String[] offsetAyir = offsetText.split("Hatırlatılacak: ");

        // İlk parça tarih, ikinci parça saat olacak
        String tarih = ikinciAyirma[0];
        String saat = ikinciAyirma[1];
        String aciklama = aciklamaAyir[1];
        String offset = offsetAyir[1];

        userFirebase = kimlikDogFirebase.getCurrentUser();

        databaseRef = FirebaseDatabase.getInstance().getReference().child("Hatırlatmalar")
                .child(userFirebase.getUid());

        databaseRef.orderByChild("tarih").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String hatirlaticiTarih = dataSnapshot.child("tarih").getValue(String.class);
                    String hatirlaticiSaat = dataSnapshot.child("saat").getValue(String.class);
                    String hatirlaticiAciklama = dataSnapshot.child("aciklama").getValue(String.class);
                    String hatirlaticiOffset = dataSnapshot.child("offset").getValue(String.class);

                    if (hatirlaticiTarih.equals(tarih) &&
                            hatirlaticiSaat.equals(saat) &&
                            hatirlaticiAciklama.equals(aciklama) &&
                            hatirlaticiOffset.equals(offset)) {

                        dataSnapshot.getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        Intent bildirimIntent = new Intent(this, AlarmReceiver.class);
        bildirimIntent.putExtra("isNotification", true);
        bildirimIntent.putExtra("description", aciklama);
        bildirimIntent.putExtra("offsetValue", offset);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.putExtra("description", aciklama);
        //alarmIntent.putExtra("ringtone", selectedRingtoneUri.toString());
        alarmIntent.putExtra("isNotification", false);

        PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(this,
                0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent bildirimPendingIntent = PendingIntent.getBroadcast(this,
                1, bildirimIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


        alarmManager.cancel(alarmPendingIntent);
        alarmManager.cancel(bildirimPendingIntent);


    };

    public void goster(){

        userFirebase = kimlikDogFirebase.getCurrentUser();
        databaseRef = FirebaseDatabase.getInstance().getReference();
        databaseRef.child("Hatırlatmalar").child(userFirebase.getUid())
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // dataSnapshot, "Hatırlatmalar" düğümünün altındaki verileri içerir
                // Verileri alın ve RecyclerView'ınıza yükleyin
                List<Hatirlatici> dataList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Hatirlatici model = snapshot.getValue(Hatirlatici.class);
                    String tarih = model.tarih;
                    String saat = model.saat;

                    String dateTimeStr = tarih + " " + saat; // Örneğin: "10/11/2023 22:22"

                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm",
                            Locale.getDefault());

                    try {
                        Date hatirlaticiTarihSaat = sdf.parse(dateTimeStr);

                        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime guncelTarihSaat = LocalDateTime.parse(dtf.format(now), dtf);
                        Date guncelDate = Date.from(guncelTarihSaat.atZone(ZoneId.systemDefault())
                                .toInstant());
                        if (hatirlaticiTarihSaat.after(guncelDate)) {
                            dataList.add(model);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                        Toast.makeText(GirisActivity.this, "hatalı",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                // RecyclerView için adapter oluşturun ve verileri set ettik
                Adapter adapter = new Adapter(dataList);
                recyclerView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Veri okuma iptal edildiğinde yapılacak işlemler
            }
        });
    }
}

