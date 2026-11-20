package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiModel;
import go.pemkott.appsandroidmobiletebingtinggi.pdf.ReadPdfActivity;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtil;
import go.pemkott.appsandroidmobiletebingtinggi.utils.NetworkUtils;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.ValidasiNewActivity;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.VerificationBatchHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class PulangFragment extends Fragment {

    RecyclerView rvRekapPulangFragment, rvVerivikatorKedua;
    HttpService holderAPI;
    AdapterPulangFragment adapterPulangFragment;
    AdapterPulangKeduaFragment adapterPulangKeduaFragment;
    DatabaseHelper databaseHelper;
    String sEmployee_id, sToken, sVerifikator;
    TextView tvv1, tvv2, tvKosongValidasi;
    ImageView ivBackValidasi;
    View llBatchV1, llBatchV2;
    CheckBox cbSelectAllV1, cbSelectAllV2;
    MaterialButton btnBatchTerimaV1, btnBatchTolakV1, btnBatchTerimaV2, btnBatchTolakV2;
    private int v1Count = 0;
    private int v2Count = 0;
    private boolean v1Loaded = false;
    private boolean v2Loaded = false;

    SessionManager session;
    String userId;
    String ipPerangkat = "-";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        EdgeToEdge.enable(getActivity());

        View view =  inflater.inflate(R.layout.fragment_pulang, container, false);
        session = new SessionManager(getContext());
        userId = session.getPegawaiId();


        rvRekapPulangFragment = view.findViewById(R.id.rvRekapPulangragment);
        rvVerivikatorKedua = view.findViewById(R.id.rvVerivikatorKedua);
        tvv1 = view.findViewById(R.id.tvv1);
        tvv2 = view.findViewById(R.id.tvv2);
        tvKosongValidasi = view.findViewById(R.id.tvKosongValidasi);
        ivBackValidasi = view.findViewById(R.id.ivBackValidasi);
        llBatchV1 = view.findViewById(R.id.llBatchV1);
        cbSelectAllV1 = view.findViewById(R.id.cbSelectAllV1);
        btnBatchTerimaV1 = view.findViewById(R.id.btnBatchTerimaV1);
        btnBatchTolakV1 = view.findViewById(R.id.btnBatchTolakV1);
        llBatchV2 = view.findViewById(R.id.llBatchV2);
        cbSelectAllV2 = view.findViewById(R.id.cbSelectAllV2);
        btnBatchTerimaV2 = view.findViewById(R.id.btnBatchTerimaV2);
        btnBatchTolakV2 = view.findViewById(R.id.btnBatchTolakV2);

        holderAPI = RetroClient.getInstance().getApi2();
        databaseHelper = new DatabaseHelper(getActivity());

        dataUser();

        dataRekapServerV1(sEmployee_id);
        dataRekapServerV2(sEmployee_id);

        ivBackValidasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidasiNewActivity.validasi.finish();
            }
        });

        return view;
    }

    private void dataUser() {
        sEmployee_id = session.getEmployeeId();
        if (sEmployee_id == null || sEmployee_id.trim().isEmpty() || "0".equals(sEmployee_id)) {
            sEmployee_id = session.getPegawaiId();
        }

        Cursor res = databaseHelper.getAllData22(session.getPegawaiId());
        if (res == null || res.getCount() == 0) {
            res = databaseHelper.getAllData22(session.getEmployeeId());
        }

        if (res != null && res.getCount() > 0) {
            if (res.moveToFirst()) {
                int empIdIndex = res.getColumnIndex("EMPLOYEID");
                int tokenIndex = res.getColumnIndex("TOKEN");
                int verifIndex = res.getColumnIndex("VERIFIKATOR");

                String empFromDb = (empIdIndex != -1) ? res.getString(empIdIndex) : res.getString(1);
                if (empFromDb != null && !empFromDb.isEmpty() && !"0".equals(empFromDb)) {
                    sEmployee_id = empFromDb;
                }
                sToken = (tokenIndex != -1) ? res.getString(tokenIndex) : res.getString(5);
                sVerifikator = (verifIndex != -1) ? res.getString(verifIndex) : res.getString(6);
            }
            res.close();
        }

        if (sEmployee_id == null || sEmployee_id.trim().isEmpty() || "0".equals(sEmployee_id)) {
            sEmployee_id = session.getEmployeeId();
        }
        if (sEmployee_id == null || sEmployee_id.trim().isEmpty() || "0".equals(sEmployee_id)) {
            sEmployee_id = session.getPegawaiId();
        }
    }

    public void dataRekapServerV1(String idE){
        if (idE == null || idE.trim().isEmpty() || "0".equals(idE)) {
            idE = session.getPegawaiId();
        }

        Call<List<RekapPulangFragment>> callKegiatan = holderAPI.getRekapPulangFragment(ServerConfig.getApiUrl() + "verifikatorpertamapulang?id="+idE);
        callKegiatan.enqueue(new Callback<List<RekapPulangFragment>>() {
            @Override
            public void onResponse(Call<List<RekapPulangFragment>> call, Response<List<RekapPulangFragment>> response) {
                if (!response.isSuccessful()){
                    return;
                }

                List<RekapPulangFragment> rekapServers = response.body() != null ? response.body() : new ArrayList<>();
                v1Loaded = true;
                v1Count = rekapServers.size();
                if (rekapServers.isEmpty()) {
                    tvv1.setVisibility(View.GONE);
                } else {
                    tvv1.setText("VERIFIKATOR PERTAMA (" + v1Count + ")");
                    tvv1.setVisibility(View.VISIBLE);
                }
                syncSectionCounters();

                rvRekapPulangFragment.setLayoutManager(new LinearLayoutManager(getActivity()));
                adapterPulangFragment = new AdapterPulangFragment(rekapServers, getActivity());
                rvRekapPulangFragment.setAdapter(adapterPulangFragment);
                adapterPulangFragment.setOnItemClickCallback(new AdapterPulangFragment.OnItemClickCallback() {
                    @Override
                    public void onItemClicked(RekapPulangFragment data) {
                        viewDialogValidasi(data);
                    }
                });
                VerificationBatchHelper.bindSectionToolbar(
                        getActivity(), llBatchV1, cbSelectAllV1, btnBatchTerimaV1, btnBatchTolakV1,
                        adapterPulangFragment, holderAPI, "pulang", "1", createBatchCallback()
                );

            }

            @Override
            public void onFailure(Call<List<RekapPulangFragment>> call, Throwable t) {
                v1Loaded = true;
                syncSectionCounters();
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void dataRekapServerV2(String idE){
        if (idE == null || idE.trim().isEmpty() || "0".equals(idE)) {
            idE = session.getPegawaiId();
        }
        Dialog dialogproses = new Dialog(getActivity(), R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);

        Call<List<RekapPulangKeduaFragment>> callKegiatanKedua = holderAPI.getRekapPulangKeduaFragment(ServerConfig.getApiUrl() + "verifikatorkeduapulang?id="+idE);
        callKegiatanKedua.enqueue(new Callback<List<RekapPulangKeduaFragment>>() {
            @Override
            public void onResponse(Call<List<RekapPulangKeduaFragment>> call, Response<List<RekapPulangKeduaFragment>> response) {
                if (!response.isSuccessful()){
                    dialogproses.dismiss();
                    dialogView.viewNotifKosong(getActivity(), "Gagal menampilkan data verifikasi, dicoba kembali.", "");
                    return;
                }

                List<RekapPulangKeduaFragment> AdapterPulangKeduaFragments = response.body() != null ? response.body() : new ArrayList<>();
                v2Loaded = true;
                v2Count = AdapterPulangKeduaFragments.size();
                if (AdapterPulangKeduaFragments.isEmpty()) {
                    tvv2.setVisibility(View.GONE);
                } else {
                    tvv2.setText("VERIFIKATOR KEDUA (" + v2Count + ")");
                    tvv2.setVisibility(View.VISIBLE);
                }
                syncSectionCounters();

                rvVerivikatorKedua.setLayoutManager(new LinearLayoutManager(getActivity()));
                adapterPulangKeduaFragment = new AdapterPulangKeduaFragment(AdapterPulangKeduaFragments, getActivity());
                rvVerivikatorKedua.setAdapter(adapterPulangKeduaFragment);
                adapterPulangKeduaFragment.setOnItemClickCallback(new AdapterPulangKeduaFragment.OnItemClickCallback() {
                    @Override
                    public void onItemClicked(RekapPulangKeduaFragment data) {
                        viewDialogValidasiKedua(data);
                    }
                });
                VerificationBatchHelper.bindSectionToolbar(
                        getActivity(), llBatchV2, cbSelectAllV2, btnBatchTerimaV2, btnBatchTolakV2,
                        adapterPulangKeduaFragment, holderAPI, "pulang", "2", createBatchCallback()
                );

                dialogproses.dismiss();
            }

            @Override
            public void onFailure(Call<List<RekapPulangKeduaFragment>> call, Throwable t) {
                v2Loaded = true;
                syncSectionCounters();
                dialogproses.dismiss();
                dialogView.viewNotifKosong(getActivity(), "Gagal terhubung ke server, mohon dicoba kembali", "");
            }
        });

        dialogproses.show();

    }

    private void syncSectionCounters() {
        if (tvKosongValidasi != null) {
            tvKosongValidasi.setVisibility(v1Loaded && v2Loaded && v1Count == 0 && v2Count == 0 ? View.VISIBLE : View.GONE);
        }
        if (getActivity() instanceof ValidasiNewActivity) {
            ((ValidasiNewActivity) getActivity()).updateTabCounts(true, v1Count + v2Count);
        }
    }

    private VerificationBatchHelper.BatchCallback createBatchCallback() {
        return new VerificationBatchHelper.BatchCallback() {
            @Override
            public void onSuccess(String message) {
                dataRekapServerV1(sEmployee_id);
                dataRekapServerV2(sEmployee_id);
                viewSuksesValidasi(message);
            }

            @Override
            public void onError(String message, boolean fromServerSuccessFalse) {
                if (fromServerSuccessFalse) {
                    viewGagalValidasi(message);
                } else {
                    dialogView.viewNotifKosong(getActivity(), "Gagal melakukan verifikasi", message);
                }
            }
        };
    }


    Dialog viewDataValidasi;

    public void viewDialogValidasi(RekapPulangFragment data) {
        viewDataValidasi = new Dialog(getActivity(), R.style.DialogStyle);
        viewDataValidasi.setContentView(R.layout.view_rekap_validasi);
        viewDataValidasi.setCancelable(true);

        ImageView tutupDialog = viewDataValidasi.findViewById(R.id.ivTutupViewListValidasi);
        if (tutupDialog != null) {
            tutupDialog.setOnClickListener(v -> viewDataValidasi.dismiss());
        }

        // Profile Info
        TextView nama = viewDataValidasi.findViewById(R.id.tvNamaRekapValid);
        TextView tvNip = viewDataValidasi.findViewById(R.id.tvNipRekapValid);
        TextView tanggal = viewDataValidasi.findViewById(R.id.tvTanggalRekapValid);

        if (nama != null) nama.setText(data.getNama() != null ? data.getNama() : "-");
        if (tvNip != null) tvNip.setText("NIP: " + (data.getNip() != null && !data.getNip().trim().isEmpty() ? data.getNip().trim() : "-"));
        if (tanggal != null) tanggal.setText("📅 Tanggal: " + (data.getTanggal() != null ? data.getTanggal() : "-"));

        // Task 1: Ensure ONLY Absensi Pulang section is visible on PulangFragment
        View cvSectionMasuk = viewDataValidasi.findViewById(R.id.cvSectionMasuk);
        View cvSectionPulang = viewDataValidasi.findViewById(R.id.cvSectionPulang);
        if (cvSectionMasuk != null) cvSectionMasuk.setVisibility(View.GONE);
        if (cvSectionPulang != null) cvSectionPulang.setVisibility(View.VISIBLE);

        // Pulang Section
        TextView tvJamPulang = viewDataValidasi.findViewById(R.id.tvJamRekapPulang);
        TextView tvKehadiranPulang = viewDataValidasi.findViewById(R.id.tvKehadiranRekapPulang);
        TextView tvKeteranganPulang = viewDataValidasi.findViewById(R.id.tvKeteranganRekapPulang);
        TextView tvStatusPulang = viewDataValidasi.findViewById(R.id.tvStatusRekapPulang);
        TextView tvLokasiPulang = viewDataValidasi.findViewById(R.id.tvLokasiAlamatLuarKantorPulang);
        TextView tvTagFotoPulang = viewDataValidasi.findViewById(R.id.tvTagFotoRekapPulang);
        ShapeableImageView ivFotoPulang = viewDataValidasi.findViewById(R.id.ivAktivitasRekapPulang);
        TextView tvTagLampiranPulang = viewDataValidasi.findViewById(R.id.tvTagLampiranRekapPulang);
        ShapeableImageView ivLampiranPulang = viewDataValidasi.findViewById(R.id.ivLampiranRekapPulang);
        MaterialButton btnPdfPulang = viewDataValidasi.findViewById(R.id.txtOpenPdfPulang);

        if (tvJamPulang != null) tvJamPulang.setText(data.getJam_pulang() != null && !data.getJam_pulang().trim().isEmpty() ? data.getJam_pulang() : "-");
        if (tvKehadiranPulang != null) tvKehadiranPulang.setText(getKatName(data.getPosisi_pulang()));
        if (tvKeteranganPulang != null) tvKeteranganPulang.setText(data.getKet_pulang() != null && !data.getKet_pulang().trim().isEmpty() ? data.getKet_pulang().trim() : "-");
        if (tvStatusPulang != null) tvStatusPulang.setText(getStatusText(data.getValidasi_pulang()));

        bindMedia(data.getPhoto_tagging_pulang(), tvTagFotoPulang, ivFotoPulang, ServerConfig.getUploadsAbsensiUrl());
        bindLampiran(data.getLampiran_pulang(), data.getEkstensi_pulang(), tvTagLampiranPulang, ivLampiranPulang, btnPdfPulang);
        bindAddress(data.getLat_pulang(), data.getLng_pulang(), tvLokasiPulang);

        // Action Buttons
        View tolak = viewDataValidasi.findViewById(R.id.btnTolak);
        View terima = viewDataValidasi.findViewById(R.id.btnTerima);

        if (tolak != null) {
            tolak.setOnClickListener(v -> {
                viewDataValidasi.dismiss();
                validasi("3", data.getId(), "1");
            });
        }
        if (terima != null) {
            terima.setOnClickListener(v -> {
                viewDataValidasi.dismiss();
                validasi("1", data.getId(), "1");
            });
        }

        viewDataValidasi.show();
    }

    public void viewDialogValidasiKedua(RekapPulangKeduaFragment data) {
        viewDataValidasi = new Dialog(getActivity(), R.style.DialogStyle);
        viewDataValidasi.setContentView(R.layout.view_rekap_validasi_kedua);
        viewDataValidasi.setCancelable(true);

        ImageView tutupDialog = viewDataValidasi.findViewById(R.id.ivTutupViewListValidasiKedua);
        if (tutupDialog != null) {
            tutupDialog.setOnClickListener(v -> viewDataValidasi.dismiss());
        }

        // Profile Info
        TextView nama = viewDataValidasi.findViewById(R.id.tvNamaRekapValidKedua);
        TextView tvNip = viewDataValidasi.findViewById(R.id.tvNipRekapValidKedua);
        TextView tanggal = viewDataValidasi.findViewById(R.id.tvTanggalRekapValidKedua);

        if (nama != null) nama.setText(data.getNama() != null ? data.getNama() : "-");
        if (tvNip != null) tvNip.setText("NIP: " + (data.getNip() != null && !data.getNip().trim().isEmpty() ? data.getNip().trim() : "-"));
        if (tanggal != null) tanggal.setText("📅 Tanggal: " + (data.getTanggal() != null ? data.getTanggal() : "-"));

        // Task 1: Ensure ONLY Absensi Pulang section is visible on PulangFragment
        View cvSectionMasuk = viewDataValidasi.findViewById(R.id.cvSectionMasukKedua);
        View cvSectionPulang = viewDataValidasi.findViewById(R.id.cvSectionPulangKedua);
        if (cvSectionMasuk != null) cvSectionMasuk.setVisibility(View.GONE);
        if (cvSectionPulang != null) cvSectionPulang.setVisibility(View.VISIBLE);

        // Pulang Section
        TextView tvJamPulang = viewDataValidasi.findViewById(R.id.tvJamRekapPulangKedua);
        TextView tvKehadiranPulang = viewDataValidasi.findViewById(R.id.tvKehadiranRekapPulangKedua);
        TextView tvKeteranganPulang = viewDataValidasi.findViewById(R.id.tvKeteranganRekapPulangKedua);
        TextView tvStatusPulang = viewDataValidasi.findViewById(R.id.tvStatusRekapPulangKedua);
        TextView tvLokasiPulang = viewDataValidasi.findViewById(R.id.tvLokasiAlamatLuarKantorPulangKedua);
        TextView tvTagFotoPulang = viewDataValidasi.findViewById(R.id.tvTagFotoRekapPulangKedua);
        ShapeableImageView ivFotoPulang = viewDataValidasi.findViewById(R.id.ivAktivitasRekapPulangKedua);
        TextView tvTagLampiranPulang = viewDataValidasi.findViewById(R.id.tvTagLampiranRekapPulangKedua);
        ShapeableImageView ivLampiranPulang = viewDataValidasi.findViewById(R.id.ivLampiranRekapPulangKedua);
        MaterialButton btnPdfPulang = viewDataValidasi.findViewById(R.id.txtOpenPdfPulangKedua);

        if (tvJamPulang != null) tvJamPulang.setText(data.getJam_pulang() != null && !data.getJam_pulang().trim().isEmpty() ? data.getJam_pulang() : "-");
        if (tvKehadiranPulang != null) tvKehadiranPulang.setText(getKatName(data.getPosisi_pulang()));
        if (tvKeteranganPulang != null) tvKeteranganPulang.setText(data.getKet_pulang() != null && !data.getKet_pulang().trim().isEmpty() ? data.getKet_pulang().trim() : "-");
        if (tvStatusPulang != null) tvStatusPulang.setText(getStatusText(data.getValidasi_pulang()));

        bindMedia(data.getPhoto_tagging_pulang(), tvTagFotoPulang, ivFotoPulang, ServerConfig.getUploadsAbsensiUrl());
        bindLampiran(data.getLampiran_pulang(), data.getEkstensi_pulang(), tvTagLampiranPulang, ivLampiranPulang, btnPdfPulang);
        bindAddress(data.getLat_pulang(), data.getLng_pulang(), tvLokasiPulang);

        // Action Buttons
        View tolak = viewDataValidasi.findViewById(R.id.btnTolakKedua);
        View terima = viewDataValidasi.findViewById(R.id.btnTerimaKedua);

        if (tolak != null) {
            tolak.setOnClickListener(v -> {
                viewDataValidasi.dismiss();
                validasi("4", data.getId(), "2");
            });
        }
        if (terima != null) {
            terima.setOnClickListener(v -> {
                viewDataValidasi.dismiss();
                validasi("2", data.getId(), "2");
            });
        }

        viewDataValidasi.show();
    }

    private String getStatusText(int validasi) {
        switch (validasi) {
            case 0: return "Butuh Verifikasi Tahap 1 (Verifikator 1)";
            case 1: return "Butuh Verifikasi Tahap 2 (Verifikator 2)";
            case 2: return "Sudah Diverifikasi Verifikator 2";
            case 3: return "Ditolak Verifikator 1";
            case 4: return "Ditolak Verifikator 2";
            default: return "Proses Verifikasi";
        }
    }

    private String getKatName(String posisi) {
        if (posisi == null || posisi.trim().isEmpty()) return "-";
        posisi = posisi.trim();
        if ("tl-masuk".equals(posisi) || "tl-pulang".equals(posisi) || "tl".equals(posisi) || "luar".equals(posisi)) {
            return "Tugas Lapangan";
        } else if ("sk".equals(posisi)) {
            return "Sakit";
        } else if ("pd".equals(posisi)) {
            return "Perjalanan Dinas";
        } else if ("kp".equals(posisi)) {
            return "Keperluan Pribadi";
        } else if ("cuti".equals(posisi) || "ct".equals(posisi)) {
            return "Cuti";
        } else if ("masuk".equals(posisi) || "pulang".equals(posisi)) {
            return "Presensi Kantor";
        }
        return posisi;
    }

    private void bindMedia(String photoPath, TextView tagView, ShapeableImageView imageView, String baseUrl) {
        if (imageView == null) return;
        if (photoPath == null || photoPath.trim().isEmpty() || "-".equals(photoPath.trim())) {
            imageView.setVisibility(View.GONE);
            if (tagView != null) tagView.setVisibility(View.GONE);
        } else {
            String fullPhotoUrl = ServerConfig.resolveUploadsAbsensiUrl(photoPath);
            imageView.setVisibility(View.VISIBLE);
            if (tagView != null) tagView.setVisibility(View.VISIBLE);
            Glide.with(this).load(fullPhotoUrl).placeholder(R.drawable.profil_pic).error(R.drawable.profil_pic).into(imageView);
        }
    }

    private void bindLampiran(String lampiranPath, String ekstensi, TextView tagView, ShapeableImageView imageView, MaterialButton pdfButton) {
        if (lampiranPath == null || lampiranPath.trim().isEmpty() || "-".equals(lampiranPath.trim())) {
            if (imageView != null) imageView.setVisibility(View.GONE);
            if (tagView != null) tagView.setVisibility(View.GONE);
            if (pdfButton != null) pdfButton.setVisibility(View.GONE);
        } else {
            String fullLampiranUrl = ServerConfig.resolveUploadsLampiranUrl(lampiranPath);

            boolean isPdf = fullLampiranUrl.toLowerCase().endsWith(".pdf") || "pdf".equalsIgnoreCase(ekstensi);
            if (isPdf) {
                if (imageView != null) imageView.setVisibility(View.GONE);
                if (tagView != null) tagView.setVisibility(View.VISIBLE);
                if (pdfButton != null) {
                    pdfButton.setVisibility(View.VISIBLE);
                    String finalPdfUrl = fullLampiranUrl;
                    pdfButton.setOnClickListener(v -> {
                        Intent intent = new Intent(getActivity(), ReadPdfActivity.class);
                        intent.putExtra("PDF_URL", finalPdfUrl);
                        startActivity(intent);
                    });
                }
            } else {
                if (pdfButton != null) pdfButton.setVisibility(View.GONE);
                if (imageView != null) imageView.setVisibility(View.VISIBLE);
                if (tagView != null) tagView.setVisibility(View.VISIBLE);
                if (imageView != null) {
                    Glide.with(this).load(fullLampiranUrl).placeholder(R.drawable.profil_pic).error(R.drawable.profil_pic).into(imageView);
                }
            }
        }
    }

    private void bindAddress(String lat, String lng, TextView addressView) {
        if (addressView == null) return;
        if (lat == null || lat.trim().isEmpty() || "-".equals(lat.trim()) || "0".equals(lat.trim())) {
            addressView.setText("Lokasi Presensi Utama / Dalam Kantor");
        } else {
            try {
                double dLat = Double.parseDouble(lat.trim());
                double dLng = Double.parseDouble(lng.trim());
                String addressText = getAddress(getActivity(), dLat, dLng);
                addressView.setText(addressText);
            } catch (Exception e) {
                addressView.setText("Lat: " + lat + ", Lng: " + lng);
            }
        }
    }

    public String getAddress(Context context, double lat, double lng) {
        if (context == null || lat == 0 || lng == 0) {
            return "-";
        }
        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address obj = addresses.get(0);
                String fullAddress = obj.getAddressLine(0);
                if (fullAddress != null && !fullAddress.trim().isEmpty()) {
                    return fullAddress;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Lat: " + lat + ", Lng: " + lng;
    }

    public void validasi(String hasil, String idabsen, String validator){
        Dialog dataKosong = new Dialog(getContext(), R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_kirm_validasi);
        dataKosong.setCancelable(false);

        TextView tvWarning1 = dataKosong.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);
        TextView title = dataKosong.findViewById(R.id.tvTitleNotif);
        EditText cttValidator = dataKosong.findViewById(R.id.catatanvalidator);
        Button btnkirimvalidasi = dataKosong.findViewById(R.id.btnkirimvalidasi);

        title.setText("Peringatan!");

        if ("3".equals(hasil) || "4".equals(hasil) || "0".equals(hasil)){
            tvWarning1.setText("Apakah anda yakin tidak akan memvalidasi?");
        }else{
            tvWarning1.setText("Apakah anda yakin akan memvalidasi?");
        }


        btnkirimvalidasi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hasilValidasi(hasil, idabsen, cttValidator.getText().toString(), validator);
                dataKosong.dismiss();

            }
        });

        dataKosong.setCancelable(true);
        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataKosong.dismiss();
            }
        });
        dataKosong.show();
    }

    DialogView dialogView = new DialogView(getActivity());

    public void hasilValidasi(String hasil,
                              String idabsen,
                              String cttValidator,
                              String validator) {

        Dialog dialogproses = new Dialog(getActivity(), R.style.DialogStyle);
        dialogproses.setContentView(R.layout.view_proses);
        dialogproses.setCancelable(false);
        dialogproses.show();

        if ("-".equals(ipPerangkat)) {
            ipPerangkat = NetworkUtil.getDeviceIpFallback();
        }

        Call<ValidasiModel> call = holderAPI.hasilValidasiPulangPertama(
                idabsen,
                validator,
                hasil,
                cttValidator,
                sEmployee_id,
                ipPerangkat
        );

        call.enqueue(new Callback<ValidasiModel>() {

            @Override
            public void onResponse(@NonNull Call<ValidasiModel> call,
                                   @NonNull Response<ValidasiModel> response) {

                dialogproses.dismiss();

//                if (!response.isSuccessful()) {
//                    dialogView.viewNotifKosong(
//                            getActivity(),
//                            "Gagal melakukan verifikasi.",
//                            "Silakan coba kembali."
//                    );
//                    return;
//                }

                if (!response.isSuccessful()) {

                    String pesan = "Silakan coba kembali.";

                    try {

                        if (response.errorBody() != null) {

                            String error = response.errorBody().string();

                            Log.e("API_ERROR", error);

                            JSONObject json = new JSONObject(error);

                            if (json.has("message")) {

                                pesan = json.getString("message");

                            }

                        }

                    } catch (Exception e) {

                        e.printStackTrace();

                    }

                    dialogView.viewNotifKosong(

                            getActivity(),

                            "Gagal melakukan verifikasi",

                            pesan

                    );

                    return;

                }

                ValidasiModel hasil = response.body();

                if (hasil == null) {
                    dialogView.viewNotifKosong(
                            getActivity(),
                            "Response server kosong.",
                            ""
                    );
                    return;
                }

                if (hasil.isStatus()) {

                    dataRekapServerV1(sEmployee_id);
                    dataRekapServerV2(sEmployee_id);

                    viewSuksesValidasi(
                            hasil.getMessage()
                    );

                } else {

                    viewGagalValidasi(
                            hasil.getMessage()
                    );

                }

            }

            @Override
            public void onFailure(@NonNull Call<ValidasiModel> call,
                                  @NonNull Throwable t) {

                dialogproses.dismiss();

                Log.e(
                        "VALIDASI",
                        "Error : " + t.getMessage(),
                        t
                );

                dialogView.viewNotifKosong(
                        getActivity(),
                        "Gagal terhubung ke server.",
                        "Silakan coba kembali."
                );

            }

        });

    }
//    public void hasilValidasi(String hasil, String idabsen, String cttValidator, String validator){
//
//        Dialog dialogproses = new Dialog(getActivity(), R.style.DialogStyle);
//        dialogproses.setContentView(R.layout.view_proses);
//        dialogproses.setCancelable(false);
//
//        String statusvalidasi;
//        if (hasil.equals("1")){
//            statusvalidasi = "1";
//        }else if(hasil.equals("2")){
//            statusvalidasi = "2";
//        }else if(hasil.equals("3")){
//            statusvalidasi = "3";
//        }else{
//            statusvalidasi = "4";
//        }
//
//
//
//        if ("-".equals(ipPerangkat)) {
//            ipPerangkat = NetworkUtil.getDeviceIpFallback();
//        }
//
//
//        Call<List<ValidasiModel>> callKegiatan = holderAPI.getUrlHasilValidasiMasukPertama("https://absensi.tebingtinggikota.go.id/api/validasipulangpertama?id="+idabsen+"&tahap="+validator+"&statusvalidasi="+statusvalidasi+"&ctt="+cttValidator+"&ipp="+ipPerangkat);
//        callKegiatan.enqueue(new Callback<List<ValidasiModel>>() {
//            @Override
//            public void onResponse(Call<List<ValidasiModel>> call, Response<List<ValidasiModel>> response) {
//                if (!response.isSuccessful()){
//                    dialogView.viewNotifKosong(getActivity(), "Gagal, mohon lakukan verifikasi kembali.", "");
//                    return;
//                }
//
//                List<ValidasiModel> validasiDatas = response.body();
//                dialogproses.dismiss();
//
//                for (ValidasiModel hasilValidasi : validasiDatas){
//                    if (hasilValidasi.isStatus() == true){
//                        dataRekapServerV1(sEmployee_id);
//                        dataRekapServerV2(sEmployee_id);
//                        viewSuksesValidasi(hasilValidasi.getMessage());
//                    }else{
//                        viewGagalValidasi(hasilValidasi.getMessage());
//                    }
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<List<ValidasiModel>> call, Throwable t) {
//                dialogproses.dismiss();
//                dialogView.viewNotifKosong(getActivity(), "Gagal terhubung ke server, mohon dicoba kembali", "");
//
//            }
//        });
//
//        dialogproses.show();
//    }

    public void viewSuksesValidasi(String info1){
        Dialog dialogSukes = new Dialog(getContext(), R.style.DialogStyle);
        dialogSukes.setContentView(R.layout.view_sukses_validasi);
        dialogSukes.setCancelable(false);
        ImageView tvTutupDialog = dialogSukes.findViewById(R.id.tvTutupDialog);


        TextView tvInfo1 = dialogSukes.findViewById(R.id.tvInfo1);

        tvInfo1.setText(info1);

        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogSukes.dismiss();
            }
        });

        dialogSukes.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                //your code here
                dialogSukes.dismiss();
            }
        }, 3000);

    }

    public void viewGagalValidasi(String w1){
        Dialog dataKosong = new Dialog(getContext(), R.style.DialogStyle);
        dataKosong.setContentView(R.layout.view_failed_validasi);
        TextView tvWarning1 = dataKosong.findViewById(R.id.tvWarning1);
        ImageView tvTutupDialog = dataKosong.findViewById(R.id.tvTutupDialog);
        tvWarning1.setText(w1);

        dataKosong.setCancelable(true);
        tvTutupDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataKosong.dismiss();
            }
        });
        dataKosong.show();
    }


}