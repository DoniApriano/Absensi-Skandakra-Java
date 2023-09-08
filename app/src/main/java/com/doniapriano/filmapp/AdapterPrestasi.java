package com.doniapriano.filmapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.doniapriano.filmapp.ModelIzin;
import com.doniapriano.filmapp.R;

import java.util.List;

public class AdapterPrestasi extends RecyclerView.Adapter<AdapterPrestasi.HolderData> {

    private Context context;
    private List<ModelPrestasi> listModelPrestasi;

    AdapterPrestasi(Context context, List<ModelPrestasi> listModelPrestasi) {
        this.context = context;
        this.listModelPrestasi = listModelPrestasi;
    }

    @Override
    public HolderData onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_izin,parent,false);
        HolderData viewHolderWarDetails = new HolderData(view);

        return viewHolderWarDetails;
    }

    @Override
    public void onBindViewHolder(HolderData holder, int position) {
        holder.tvAlasan.setText(listModelPrestasi.get(position).getJuara());
        holder.tvKeterangan.setText(listModelPrestasi.get(position).getTanggal());
        holder.tvTanggal.setText(listModelPrestasi.get(position).getTanggal());
    }

    @Override
    public int getItemCount() {
        return listModelPrestasi.size();
    }

    public class HolderData extends RecyclerView.ViewHolder{
        TextView tvAlasan, tvKeterangan, tvTanggal;
        CardView layoutCard;

        HolderData(View v) {
            super(v);

            tvTanggal = v.findViewById(R.id.tvTanggal);
            layoutCard = (CardView) v.findViewById(R.id.layout_card_view_izin);
        }
    }
}