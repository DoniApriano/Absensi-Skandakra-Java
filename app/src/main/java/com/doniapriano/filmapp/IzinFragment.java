package com.doniapriano.filmapp;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IzinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IzinFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    TableLayout tableLayout;
    TextView tvIzin;

    public IzinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IzinFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IzinFragment newInstance(String param1, String param2) {
        IzinFragment fragment = new IzinFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_izin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String username = getActivity().getIntent().getStringExtra("username");
        System.out.println(username);

        String urlDetail = DB.URL_DETAIL+username;
        tableLayout = view.findViewById(R.id.tableLayoutIzin);
        tvIzin = view.findViewById(R.id.tvIzin);

        showData(urlDetail);
    }

    public void showData(String url) {
        if (checkNetworkConnection(this.getContext())) {
            JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.length() > 0) {
                                    JSONArray cekIzin = response.getJSONArray("izin");
                                    if (cekIzin.length() > 0) {
                                        for (int i = 0; i < response.length(); i++) {
                                            JSONObject jsonObject = response.getJSONArray("izin").getJSONObject(i);
                                            ModelIzin modelIzin = new ModelIzin();
                                            String alasan = jsonObject.getString("alasan");
                                            String keterangan = jsonObject.getString("keterangan");
                                            String tanggal = jsonObject.getString("tanggal");

                                            addRowToTable(keterangan,alasan,tanggal);
                                        }
                                    } else {
                                        tvIzin.setVisibility(View.VISIBLE);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("Volley Error", error.toString());
                        }
                    }
            );
            VolleyConnection.getInstance(getActivity()).addRequestQueue(jsonArrayRequest);
        } else {
            alertConnection();
        }
    }

    private void addRowToTable(String keterangan, String alasan, String tanggal) {
        TableRow row = new TableRow(getContext());
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_table);
        row.setBackground(drawable);

        TextView tvKeterangan = new TextView(getContext());
        tvKeterangan.setText(keterangan);
        tvKeterangan.setTextSize(15);
        tvKeterangan.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvKeterangan.setTextColor(getResources().getColor(R.color.white));
        tvKeterangan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvKeterangan.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvKeterangan);

        TextView tvAlasan = new TextView(getContext());
        tvAlasan.setText(alasan);
        tvAlasan.setTextSize(15);
        tvAlasan.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvAlasan.setTextColor(getResources().getColor(R.color.white));
        tvAlasan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvAlasan.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvAlasan);

        TextView tvTanggal = new TextView(getContext());
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd MMMM", new Locale("id", "ID"));

        try {
            Date date = inputDateFormat.parse(tanggal);
            String outputDateStr = outputDateFormat.format(date);
            tvTanggal.setText(outputDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        tvTanggal.setTextSize(15);
        tvTanggal.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvTanggal.setTextColor(getResources().getColor(R.color.white));
        tvAlasan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTanggal.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvTanggal);

        tableLayout.addView(row);
    }

    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void alertConnection(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_connection, null);
        alert.setView(mView);;

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mView.findViewById(R.id.okBTN).setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
}