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

public class AdapterIzin extends RecyclerView.Adapter<AdapterIzin.ViewHolderWarDetails> {

    private Context context;
    private List<ModelIzin> listModelIzin;

    AdapterIzin(Context context, List<ModelIzin> listWarDetails) {
        this.context = context;
        this.listModelIzin = listWarDetails;
    }

    @Override
    public ViewHolderWarDetails onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_view_izin,parent,false);
        ViewHolderWarDetails viewHolderWarDetails = new ViewHolderWarDetails(view);

        return viewHolderWarDetails;
    }

    @Override
    public void onBindViewHolder(ViewHolderWarDetails holder, int position) {
        holder.tvAlasan.setText(listModelIzin.get(position).getAlasan());
        holder.tvKeterangan.setText(listModelIzin.get(position).getKeterangan());
        holder.tvTanggal.setText(listModelIzin.get(position).getTanggal());
    }

    @Override
    public int getItemCount() {
        return listModelIzin.size();
    }

    public class ViewHolderWarDetails extends RecyclerView.ViewHolder{
        TextView tvAlasan, tvKeterangan, tvTanggal;
        CardView layoutCard;

        ViewHolderWarDetails(View v) {
            super(v);
            tvTanggal = v.findViewById(R.id.tvTanggal);
            layoutCard = (CardView) v.findViewById(R.id.layout_card_view_izin);
        }
    }
}