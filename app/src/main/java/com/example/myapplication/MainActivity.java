package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    private EditText editGirisEmail,editGirisPassword,editKayitEmail,editKayitPassword;
    private String  txtGirisEmail,txtGirisPassword,txtKayitEmail,txtKayitPassword;

    private FirebaseAuth kimlikDogFirebase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editGirisEmail = (EditText)findViewById(R.id.girisEmail);
        editGirisPassword = (EditText)findViewById(R.id.girisPassword);
        editKayitEmail = (EditText)findViewById(R.id.kayitEmail);
        editKayitPassword = (EditText)findViewById(R.id.kayitPassword);
        kimlikDogFirebase = FirebaseAuth.getInstance();
    }
    public void kayitOl(View v){
        txtKayitEmail = editKayitEmail.getText().toString();
        txtKayitPassword = editKayitPassword.getText().toString();

        if(!TextUtils.isEmpty(txtKayitEmail) && !TextUtils.isEmpty(txtKayitPassword)){
            kimlikDogFirebase.createUserWithEmailAndPassword(txtKayitEmail,txtKayitPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this,
                                                "Kayıt Başarılı",
                                                Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(MainActivity.this,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else{
            Toast.makeText(this,"Lütfen boş alan bırakmayınız.",Toast.LENGTH_SHORT).
                    show();
        }
    }

    public void girisYap(View v){
        txtGirisEmail = editGirisEmail.getText().toString();
        txtGirisPassword = editGirisPassword.getText().toString();

        if(!TextUtils.isEmpty(txtGirisEmail) && !TextUtils.isEmpty(txtGirisPassword)){
            kimlikDogFirebase.signInWithEmailAndPassword(txtGirisEmail,txtGirisPassword)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(MainActivity.this,
                                        "Giriş Başarılı",
                                        Toast.LENGTH_SHORT).show();
                                Intent intent  = new Intent(MainActivity.this,
                                        GirisActivity.class);
                                intent.putExtra("email",txtGirisEmail);
                                startActivity(intent);

                            }
                            else{
                                Toast.makeText(MainActivity.this,
                                        task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        }else{
            Toast.makeText(this,"Lütfen boş alan bırakmayınız.",Toast.LENGTH_SHORT).
                    show();
        }
    }
}