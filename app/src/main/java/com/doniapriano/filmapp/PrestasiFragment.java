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
import java.util.Date;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrestasiFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrestasiFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    TextView tvPrestasi;
    TableLayout tableLayout;

    public PrestasiFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PrestasiFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrestasiFragment newInstance(String param1, String param2) {
        PrestasiFragment fragment = new PrestasiFragment();
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
        return inflater.inflate(R.layout.fragment_prestasi, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tableLayout = view.findViewById(R.id.tableLayoutPrestasi);
        tvPrestasi = view.findViewById(R.id.tvPrestasi);

        String username = getActivity().getIntent().getStringExtra("username");
        System.out.println(username);

        String urlDetail = DB.URL_DETAIL+username;
        showData(urlDetail);

    }

    private void showData(String url) {
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
                                    JSONArray cekIzin = response.getJSONArray("prestasi");
                                    if (cekIzin.length() > 0) {
                                        for (int i = 0; i < cekIzin.length(); i++) {
                                            JSONObject jsonObject = response.getJSONArray("prestasi").getJSONObject(i);
                                            String namaLomba = jsonObject.getString("nama");
                                            String tingkat = jsonObject.getString("keterangan");
                                            String juara = jsonObject.getString("juara");

                                            addRowToTable(namaLomba,tingkat,juara);
                                        }
                                    } else {
                                        tvPrestasi.setVisibility(View.VISIBLE);
                                        tvPrestasi.setText("Belum Ada Prestasi");
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

    private void addRowToTable(String namaLomba, String tingkat, String juara) {
        TableRow row = new TableRow(getContext());
        Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.bg_table);
        row.setBackground(drawable);

        TextView tvNamaLomba = new TextView(getContext());
        tvNamaLomba.setText(namaLomba);
        tvNamaLomba.setTextSize(12);
        tvNamaLomba.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvNamaLomba.setTextColor(getResources().getColor(R.color.white));
        tvNamaLomba.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvNamaLomba.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvNamaLomba);

        TextView tvTingkat = new TextView(getContext());
        tvTingkat.setText(tingkat);
        tvTingkat.setTextSize(12);
        tvTingkat.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvTingkat.setTextColor(getResources().getColor(R.color.white));
        tvTingkat.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvTingkat.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvTingkat);

        TextView tvJuara = new TextView(getContext());
        tvJuara.setText(juara);
        tvJuara.setTextSize(12);
        tvJuara.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
        tvJuara.setTextColor(getResources().getColor(R.color.white));
        tvTingkat.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tvJuara.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(tvJuara);

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