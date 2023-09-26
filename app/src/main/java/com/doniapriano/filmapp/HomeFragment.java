package com.doniapriano.filmapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    FusedLocationProviderClient fusedLocationProviderClient;
    Button btnAbsent, btnCamera;
    TextView tvUsername,tvNote;
    ImageView imageView;
    private final static int REQUEST_CODE = 100;
    Calendar calendar;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swpHome;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());

        Window window = getActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.purple));

        btnAbsent = view.findViewById(R.id.btnAbsen);
        btnCamera = view.findViewById(R.id.btnCamera);
        imageView = view.findViewById(R.id.image);
        tvUsername = view.findViewById(R.id.tvUsername);
        tvNote = view.findViewById(R.id.tvNote);
        swpHome = view.findViewById(R.id.swp_home);
        progressDialog = new ProgressDialog(getActivity());

        String username = getActivity().getIntent().getStringExtra("username");
        System.out.println(username);

        String urlDetail = DB.URL_DETAIL+username;
        showData(urlDetail);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.CAMERA
            },100);
        }

        btnCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent,100);
            tvNote.setText("Silahkan Absen 😁👇");
            btnAbsent.setVisibility(View.VISIBLE);
        });

        calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour <= 14) {
            btnAbsent.setText("Absen Masuk");
        } else {
            btnAbsent.setText("Absen Pulang");
        }

        btnAbsent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hour >= 6 && hour <= 14) {
                    postAbsen(username);
                } else {
                    putAbsen(username);
                }
            }
        });

        swpHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swpHome.setRefreshing(true);
                getLastLocation();
                swpHome.setRefreshing(false);
            }
        });

        getLastLocation();
    }

    private void postAbsen(String username) {
        if (checkNetworkConnection(this.getContext())) {
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.POST, DB.URL_ABSEN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String resp = jsonObject.getString("message");
                                String status = jsonObject.getString("status");
                                if (resp.equalsIgnoreCase("Berhasil melakukan absen")) {
                                    Toast.makeText(getActivity(), "Absen Berhasil", Toast.LENGTH_SHORT).show();
                                    System.out.println(status);
                                    alertAbsen();
                                } else if (resp.equalsIgnoreCase("Sudah melakukan absen")) {
                                    System.out.println(status);
                                    alertSudahAbsen();
                                }else {
                                    alert("Yah Gagal");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            alertSudahAbsen();
                        }
                    }
            ) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    return params;
                }
            };

            VolleyConnection.getInstance(getActivity()).addRequestQueue(stringRequest);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 2000);

        } else {
            alert("Sambungkan ke Internet");
        }
    }

    private void putAbsen(String username) {
        if (checkNetworkConnection(this.getContext())) {
            progressDialog.show();
            StringRequest stringRequest = new StringRequest(Request.Method.PUT, DB.URL_ABSEN,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                String resp = jsonObject.getString("message");
                                if (resp.equalsIgnoreCase("Berhasil melakukan absen pulang")) {
                                    Toast.makeText(getActivity(), "Absen Pulang Berhasil", Toast.LENGTH_SHORT).show();
                                    alertAbsenP();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }
            ) {
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    return params;
                }
            };

            VolleyConnection.getInstance(getActivity()).addRequestQueue(stringRequest);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            }, 2000);

        } else {
            alert("Sambungkan ke Internet");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
        }
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
                                // Ambil data pertama dari array JSON
                                JSONObject jsonObject = response.getJSONArray("user").getJSONObject(0);
                                // Ambil nama dari objek JSON
                                String nama = jsonObject.getString("nama");
                                System.out.println(nama);
                                tvUsername.setText("Selamat Datang \n" + nama);
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


    public boolean checkNetworkConnection(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    private void alert(String message){
        new AlertDialog.Builder(this.getContext())
                .setTitle("Failed")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void alertMock(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(this.getContext())
                .setTitle("Failed")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                    }
                }).show();

        alertDialog.getWindow().setFlags(
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        // Menampilkan AlertDialog
        alertDialog.show();

        // Setelah dialog ditampilkan, kita hapus FLAG_NOT_FOCUSABLE
        alertDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void getLastLocation(){
        if (ContextCompat.checkSelfPermission(this.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null){
                                try {
                                    System.err.println(location.isFromMockProvider());
                                    if (location.isFromMockProvider()) {
                                        alertMock("Hayoo Kamu Pake Fake GPS Ya ");
                                        System.out.println("hayoooooooooo");
                                    }
                                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    double utara = -7.589;
                                    double selatan = -7.591;
                                    double barat = 110.9495;
                                    double timur = 110.9516;
                                    double mLattitude = addresses.get(0).getLatitude();
                                    double mLongitude = addresses.get(0).getLongitude();
                                    System.out.println(mLattitude);
                                    System.out.println(mLongitude);

                                    if ((mLattitude <= utara) && (mLattitude >= selatan) && (mLongitude >= barat) && (mLattitude <= timur)) {
                                        tvNote.setText("Anda Sudah Berada Di Lokasi \nSilahkan Ambil Gambar ");
                                        btnCamera.setVisibility(View.VISIBLE);
                                    } else {
                                        btnAbsent.setVisibility(View.INVISIBLE);
                                        tvNote.setText("Anda Belum Berada Di Lokasi ");
                                        btnCamera.setVisibility(View.INVISIBLE);
                                    }

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                tvNote.setText("GPS Belum Aktif");
                            }
                        }
                    });
        } else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @org.jetbrains.annotations.NotNull String[] permissions, @NonNull @org.jetbrains.annotations.NotNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLastLocation();
            }else {
                Toast.makeText(getActivity(),"Please provide the required permission",Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void alertAbsen(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_absen, null);
        alert.setView(mView);;

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mView.findViewById(R.id.okBTNa).setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
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

    private void alertSudahAbsen(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_sudah_absen, null);
        alert.setView(mView);;

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mView.findViewById(R.id.okBTNsdh).setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }

    private void alertAbsenP(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this.getContext());
        View mView = getLayoutInflater().inflate(R.layout.dialog_absen_p, null);
        alert.setView(mView);;

        final AlertDialog alertDialog = alert.create();
        alertDialog.setCancelable(false);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mView.findViewById(R.id.okBTNq).setOnClickListener(v -> {
            alertDialog.dismiss();
        });

        alertDialog.show();
    }
}