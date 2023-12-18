package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    private List<Hatirlatici> dataList; // 

    // Constructor
    public Adapter(List<Hatirlatici> dataList) {
        this.dataList = dataList;
    }

    // ViewHolder sınıfı
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTarihSaat, textViewAciklama, textViewOffset;

        public ViewHolder(View view) {
            super(view);
            textViewTarihSaat = view.findViewById(R.id.textViewTarihSaat);
            textViewAciklama = view.findViewById(R.id.textViewAciklama);
            textViewOffset = view.findViewById(R.id.textViewOffset);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hatirlatici,
                parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Hatirlatici data = dataList.get(position);

        String tarihSaatText = "Alarm Tarih ve Saati: " + data.tarih + " " + data.saat;
        holder.textViewTarihSaat.setText(tarihSaatText);

        String aciklamaText = "Açıklama: " + data.aciklama;
        holder.textViewAciklama.setText(aciklamaText);

        String offsetText = "Alarmdan Kaç Dakika Önce Hatırlatılacak: " + data.offset;
        holder.textViewOffset.setText(offsetText);
    }

    @Override
    public int getItemCount() {

        return dataList.size();
    }
}
