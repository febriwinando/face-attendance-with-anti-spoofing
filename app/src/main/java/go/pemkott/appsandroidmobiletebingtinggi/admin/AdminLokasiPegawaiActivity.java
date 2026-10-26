package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeeResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeesData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminLokasiPegawaiActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSION_LOCATION_REQUEST_CODE = 2001;

    private EditText etSearchLocation;
    private EditText etEmployeeId, etAlamat, etLat, etLng;
    private DialogView dialogView;

    private GoogleMap googleMap;
    private Marker locationMarker;
    private FusedLocationProviderClient fusedLocationClient;

    private final LatLng defaultTebingTinggi = new LatLng(3.3280661, 99.1129267);

    private List<EmployeesData> masterEmployees = new ArrayList<>();
    private EmployeesData selectedEmployee = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_lokasi_pegawai);

        dialogView = new DialogView(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        ImageView ivBack = findViewById(R.id.ivAdminLokasiBack);
        etSearchLocation = findViewById(R.id.etAdminSearchLocation);
        MaterialButton btnSearchLocation = findViewById(R.id.btnAdminSearchLocation);

        etEmployeeId = findViewById(R.id.etAdminLokasiEmployeeId);
        etAlamat = findViewById(R.id.etAdminLokasiAlamat);
        etLat = findViewById(R.id.etAdminLokasiLat);
        etLng = findViewById(R.id.etAdminLokasiLng);

        MaterialButton btnPickEmployee = findViewById(R.id.btnAdminPickEmployee);
        MaterialButton btnGetGpsLocation = findViewById(R.id.btnAdminGetGpsLocation);
        MaterialButton btnSubmit = findViewById(R.id.btnAdminLokasiSubmit);

        ivBack.setOnClickListener(v -> finish());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapAdminLokasi);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        btnSearchLocation.setOnClickListener(v -> searchLocationOnMap(etSearchLocation.getText().toString()));

        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocationOnMap(etSearchLocation.getText().toString());
                return true;
            }
            return false;
        });

        if (btnPickEmployee != null) {
            btnPickEmployee.setOnClickListener(v -> showEmployeePickerDialog());
        }

        btnGetGpsLocation.setOnClickListener(v -> fetchHighAccuracyGpsLocation());
        btnSubmit.setOnClickListener(v -> submitLokasi());

        // Load employees list from server
        loadMasterEmployees();
    }

    private void loadMasterEmployees() {
        RetroClient.getInstance(this).getApi2().getEmployees().enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    masterEmployees = response.body().getData();
                    if (masterEmployees == null) masterEmployees = new ArrayList<>();
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                Log.e("ADMIN_LOKASI", "Gagal memuat master pegawai", t);
            }
        });
    }

    private void showEmployeePickerDialog() {
        if (masterEmployees == null || masterEmployees.isEmpty()) {
            Toast.makeText(this, "Sedang memuat data pegawai...", Toast.LENGTH_SHORT).show();
            loadMasterEmployees();
            return;
        }

        List<String> empLabels = new ArrayList<>();
        for (EmployeesData emp : masterEmployees) {
            if (emp == null) continue;
            String nama = emp.getNama() != null ? emp.getNama() : "Tanpa Nama";
            String nip = emp.getNip() != null ? emp.getNip() : "-";
            empLabels.add(nama + "\nNIP: " + nip + " • ID #" + emp.getId());
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("👤 Pilih Pegawai");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, empLabels);
        builder.setAdapter(adapter, (dialog, which) -> {
            if (which >= 0 && which < masterEmployees.size()) {
                selectedEmployee = masterEmployees.get(which);
                if (selectedEmployee != null) {
                    String label = selectedEmployee.getNama() + " (" + (selectedEmployee.getNip() != null ? selectedEmployee.getNip() : "ID #" + selectedEmployee.getId()) + ")";
                    etEmployeeId.setText(label);
                }
            }
        });
        builder.setNegativeButton("Batal", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Set default position
        locationMarker = googleMap.addMarker(new MarkerOptions()
                .position(defaultTebingTinggi)
                .title("Lokasi Pegawai")
                .snippet("Geser atau ketuk peta untuk memperbarui koordinat")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .draggable(true));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultTebingTinggi, 15f));
        updateSelectedLocation(defaultTebingTinggi, false);

        // Click Map Listener
        googleMap.setOnMapClickListener(latLng -> updateSelectedLocation(latLng, true));

        // Drag Marker Listener
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {}

            @Override
            public void onMarkerDrag(@NonNull Marker marker) {}

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                updateSelectedLocation(marker.getPosition(), true);
            }
        });
    }

    private void searchLocationOnMap(String query) {
        if (query == null || query.trim().isEmpty()) {
            Toast.makeText(this, "Masukkan nama jalan, kantor, atau lokasi yang ingin dicari", Toast.LENGTH_SHORT).show();
            return;
        }

        String searchLocation = query.trim();
        if (!searchLocation.toLowerCase().contains("tebing tinggi")) {
            searchLocation += ", Tebing Tinggi, Sumatera Utara";
        }

        final String finalSearch = searchLocation;

        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(AdminLokasiPegawaiActivity.this, new Locale("id", "ID"));
                List<Address> addresses = geocoder.getFromLocationName(finalSearch, 5);
                runOnUiThread(() -> {
                    dialogLoading.dismiss();
                    if (addresses != null && !addresses.isEmpty()) {
                        Address first = addresses.get(0);
                        LatLng searchLatLng = new LatLng(first.getLatitude(), first.getLongitude());

                        if (googleMap != null) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng, 17f));
                        }
                        updateSelectedLocation(searchLatLng, false);
                        Toast.makeText(AdminLokasiPegawaiActivity.this, "Lokasi ditemukan!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminLokasiPegawaiActivity.this, "Lokasi '" + query + "' tidak ditemukan. Coba ketik nama jalan secara spesifik.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    dialogLoading.dismiss();
                    Log.e("ADMIN_LOKASI", "Error searchLocationOnMap", e);
                    Toast.makeText(AdminLokasiPegawaiActivity.this, "Gagal mencari lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void updateSelectedLocation(LatLng latLng, boolean animateCamera) {
        if (latLng == null) return;

        if (locationMarker != null) {
            locationMarker.setPosition(latLng);
        }

        if (animateCamera && googleMap != null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }

        String latStr = String.format(Locale.US, "%.7f", latLng.latitude);
        String lngStr = String.format(Locale.US, "%.7f", latLng.longitude);

        etLat.setText(latStr);
        etLng.setText(lngStr);

        // Reverse Geocoding for Alamat
        reverseGeocodeAddress(latLng);
    }

    private void fetchHighAccuracyGpsLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
            return;
        }

        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(true);
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    dialogLoading.dismiss();
                    if (location != null) {
                        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        if (googleMap != null) {
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 17f));
                        }
                        updateSelectedLocation(userLatLng, false);
                        Toast.makeText(AdminLokasiPegawaiActivity.this, "Koordinat presisi GPS berhasil didapatkan!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(AdminLokasiPegawaiActivity.this, "Gagal mendapatkan lokasi GPS. Pastikan GPS aktif.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(this, e -> {
                    dialogLoading.dismiss();
                    Log.e("ADMIN_LOKASI", "Error getCurrentLocation", e);
                    Toast.makeText(AdminLokasiPegawaiActivity.this, "Gagal GPS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void reverseGeocodeAddress(LatLng latLng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(AdminLokasiPegawaiActivity.this, new Locale("id", "ID"));
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                        sb.append(addr.getAddressLine(i)).append(i < addr.getMaxAddressLineIndex() ? ", " : "");
                    }
                    String fullAddress = sb.toString();
                    runOnUiThread(() -> {
                        String currentAddr = etAlamat.getText().toString().trim();
                        if (currentAddr.isEmpty() || currentAddr.startsWith("Kantor") || currentAddr.startsWith("Jl.") || currentAddr.startsWith("Jalan")) {
                            etAlamat.setText(fullAddress);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ADMIN_LOKASI", "Error reverseGeocodeAddress", e);
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchHighAccuracyGpsLocation();
            } else {
                Toast.makeText(this, "Izin lokasi diperlukan untuk mendapatkan GPS presisi.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitLokasi() {
        String inputEmp = etEmployeeId.getText().toString().trim();
        String alamat = etAlamat.getText().toString().trim();
        String lat = etLat.getText().toString().trim();
        String lng = etLng.getText().toString().trim();

        if (inputEmp.isEmpty() || alamat.isEmpty() || lat.isEmpty() || lng.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Semua field (Pegawai, Alamat, Lat, Lng) wajib diisi.");
            return;
        }

        // Auto-resolve employee ID, NIP, and OPD ID
        String targetEmpId = null;
        String targetNip = null;
        String targetOpdId = "0";

        if (selectedEmployee != null) {
            targetEmpId = String.valueOf(selectedEmployee.getId());
            targetNip = selectedEmployee.getNip();
            targetOpdId = String.valueOf(selectedEmployee.getOpdId());
        } else {
            // Search in masterEmployees
            if (masterEmployees != null) {
                for (EmployeesData emp : masterEmployees) {
                    if (emp == null) continue;
                    String empIdStr = String.valueOf(emp.getId());
                    String nipStr = emp.getNip() != null ? emp.getNip() : "";
                    String namaStr = emp.getNama() != null ? emp.getNama() : "";

                    if (inputEmp.equalsIgnoreCase(empIdStr) || inputEmp.equalsIgnoreCase(nipStr) || inputEmp.contains(namaStr) || inputEmp.contains(nipStr)) {
                        targetEmpId = empIdStr;
                        targetNip = nipStr;
                        targetOpdId = String.valueOf(emp.getOpdId());
                        break;
                    }
                }
            }
        }

        if (targetEmpId == null) targetEmpId = inputEmp;
        if (targetNip == null) targetNip = inputEmp;

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.storeLokasiPegawaiAdmin(
                targetEmpId,  // employee_id (Primary Key integer)
                targetNip,    // nip
                targetEmpId,  // pegawai_id
                targetEmpId,  // id_pegawai
                targetOpdId,  // opd_id
                targetOpdId,  // eOPD
                alamat,       // alamat
                lat,          // let
                lat,          // lat
                lat,          // latitude
                lng,          // lng
                lng,          // longitude
                "1"           // status
        ).enqueue(new Callback<ResponsePOJO>() {
            @Override
            public void onResponse(Call<ResponsePOJO> call, Response<ResponsePOJO> response) {
                dialogProses.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    ResponsePOJO pojo = response.body();
                    if (pojo.isStatus()) {
                        dialogView.viewSuksesNonFinish(AdminLokasiPegawaiActivity.this, pojo.getRemarks() != null ? pojo.getRemarks() : "Lokasi pegawai berhasil disimpan!");
                    } else {
                        dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Gagal Simpan", pojo.getRemarks() != null ? pojo.getRemarks() : "Gagal menyimpan lokasi.");
                    }
                } else {
                    dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Gagal Server", "HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponsePOJO> call, Throwable t) {
                dialogProses.dismiss();
                dialogView.viewNotifKosong(AdminLokasiPegawaiActivity.this, "Koneksi Gagal", t.getMessage());
            }
        });
    }
}
