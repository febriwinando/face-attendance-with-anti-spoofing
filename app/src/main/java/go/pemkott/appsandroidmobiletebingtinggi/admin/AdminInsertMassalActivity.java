package go.pemkott.appsandroidmobiletebingtinggi.admin;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import go.pemkott.appsandroidmobiletebingtinggi.NewDashboard.DashboardVersiOne;
import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.api.HttpService;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.dialogview.DialogView;
import go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.OpdModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.PreviewItemModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminInsertMassalActivity extends AppCompatActivity {

    private EditText etTanggal, etNips, etCustomJam;
    private CheckBox cbUpdateAl;
    private TextView tvSummaryInfo, tvTabSubtitle, tvTabMode1, tvTabMode2, tvTabMode3;
    private TextView tvAdminInsertOpdSelect, tvNipCounter;
    private CardView btnTabMode1, btnTabMode2, btnTabMode3, cvSelectOpd, cvPreviewContainer;
    private LinearLayout llContainerMode2, llContainerMode3, llCustomJamBox;
    private TextView tvPreviewCountHeader;

    private RecyclerView rvPreviewList;
    private PreviewListAdapter previewAdapter;
    private List<PreviewItemModel> previewItemList = new ArrayList<>();

    // Tab 2 Radios
    private RadioButton rbShiftNon2, rbShiftShift2;
    private RadioButton rbTargetMasuk, rbTargetPulang;

    // Tab 3 Radios
    private RadioGroup rgJamMode3;
    private RadioButton rbJenisMasuk3;
    private RadioButton rbShiftNon3, rbShiftShift3;
    private RadioButton rbJamCustom;

    private Calendar calendar = Calendar.getInstance();
    private DialogView dialogView;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    private int activeMode = 1; // 1 = Massal Lengkap, 2 = Update Timetable, 3 = Absen Khusus
    private List<OpdModel> listOpd = new ArrayList<>();
    private OpdModel selectedOpd = null;
    private String selectedOpdId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        go.pemkott.appsandroidmobiletebingtinggi.utils.WindowUtil.applyDashboardStyle(this);
        setContentView(R.layout.activity_admin_insert_massal);

        dialogView = new DialogView(this);

        ImageView ivBack = findViewById(R.id.ivAdminInsertBack);
        etTanggal = findViewById(R.id.etAdminInsertTanggal);
        etNips = findViewById(R.id.etAdminInsertNips);
        cbUpdateAl = findViewById(R.id.cbAdminUpdateAl);
        tvSummaryInfo = findViewById(R.id.tvAdminInsertSummary);
        tvTabSubtitle = findViewById(R.id.tvTabSubtitle);
        tvNipCounter = findViewById(R.id.tvNipCounter);
        TextView tvClearNips = findViewById(R.id.btnClearNips);

        cvSelectOpd = findViewById(R.id.cvSelectOpd);
        tvAdminInsertOpdSelect = findViewById(R.id.tvAdminInsertOpdSelect);
        MaterialButton btnCocokkanNama = findViewById(R.id.btnCocokkanNama);
        MaterialButton btnPopulateOpdNips = findViewById(R.id.btnPopulateOpdNips);

        cvPreviewContainer = findViewById(R.id.cvPreviewContainer);
        tvPreviewCountHeader = findViewById(R.id.tvPreviewCountHeader);
        rvPreviewList = findViewById(R.id.rvPreviewList);

        btnTabMode1 = findViewById(R.id.btnTabMode1);
        btnTabMode2 = findViewById(R.id.btnTabMode2);
        btnTabMode3 = findViewById(R.id.btnTabMode3);

        tvTabMode1 = findViewById(R.id.tvTabMode1);
        tvTabMode2 = findViewById(R.id.tvTabMode2);
        tvTabMode3 = findViewById(R.id.tvTabMode3);

        llContainerMode2 = findViewById(R.id.llContainerMode2);
        rbShiftNon2 = findViewById(R.id.rbShiftNon2);
        rbShiftShift2 = findViewById(R.id.rbShiftShift2);
        rbTargetMasuk = findViewById(R.id.rbTargetMasuk);
        rbTargetPulang = findViewById(R.id.rbTargetPulang);

        llContainerMode3 = findViewById(R.id.llContainerMode3);
        rgJamMode3 = findViewById(R.id.rgJamMode3);
        rbJenisMasuk3 = findViewById(R.id.rbJenisMasuk3);
        rbShiftNon3 = findViewById(R.id.rbShiftNon3);
        rbShiftShift3 = findViewById(R.id.rbShiftShift3);
        rbJamCustom = findViewById(R.id.rbJamCustom);
        llCustomJamBox = findViewById(R.id.llCustomJamBox);
        etCustomJam = findViewById(R.id.etCustomJam);

        MaterialButton btnPreview = findViewById(R.id.btnAdminPreviewMassal);
        MaterialButton btnStore = findViewById(R.id.btnAdminStoreMassal);

        rvPreviewList.setLayoutManager(new LinearLayoutManager(this));
        previewAdapter = new PreviewListAdapter(this, previewItemList);
        rvPreviewList.setAdapter(previewAdapter);

        ivBack.setOnClickListener(v -> navigateBack());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBack();
            }
        });

        etTanggal.setText(dateFormat.format(calendar.getTime()));
        etTanggal.setOnClickListener(v -> showDatePicker());

        cvSelectOpd.setOnClickListener(v -> showOpdSelectionDialog());
        btnCocokkanNama.setOnClickListener(v -> showNameMatchingDialog());
        btnPopulateOpdNips.setOnClickListener(v -> populateOpdNips());

        btnTabMode1.setOnClickListener(v -> switchMode(1));
        btnTabMode2.setOnClickListener(v -> switchMode(2));
        btnTabMode3.setOnClickListener(v -> switchMode(3));

        btnPreview.setOnClickListener(v -> previewMassal());
        btnStore.setOnClickListener(v -> storeMassal());

        tvClearNips.setOnClickListener(v -> {
            etNips.setText("");
            Toast.makeText(this, "Daftar NIP dikosongkan.", Toast.LENGTH_SHORT).show();
        });

        etNips.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateNipCounter();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        rgJamMode3.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbJamCustom) {
                llCustomJamBox.setVisibility(View.VISIBLE);
            } else {
                llCustomJamBox.setVisibility(View.GONE);
            }
        });

        etCustomJam.setOnClickListener(v -> showTimePicker());

        loadOpdList();
        switchMode(1);
    }

    private void navigateBack() {
        if (isTaskRoot()) {
            Intent intent = new Intent(this, DashboardVersiOne.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }
        finish();
    }

    private void updateNipCounter() {
        String text = etNips.getText().toString().trim();
        if (text.isEmpty()) {
            tvNipCounter.setText("0 NIP");
            return;
        }
        String[] tokens = text.split("[\\s,;]+");
        Set<String> uniqueNips = new HashSet<>();
        for (String t : tokens) {
            String clean = t.replaceAll("[^a-zA-Z0-9_-]", "").trim();
            if (!clean.isEmpty()) {
                uniqueNips.add(clean);
            }
        }
        tvNipCounter.setText(uniqueNips.size() + " NIP");
    }

    private void appendNipsToEditText(List<String> newNips, String sourceLabel) {
        if (newNips == null || newNips.isEmpty()) {
            Toast.makeText(this, "Tidak ada NIP untuk ditambahkan.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentText = etNips.getText().toString().trim();
        String[] tokens = currentText.split("[\\s,;]+");
        Set<String> existing = new HashSet<>();
        for (String t : tokens) {
            String clean = t.replaceAll("[^a-zA-Z0-9_-]", "").trim();
            if (!clean.isEmpty()) {
                existing.add(clean);
            }
        }

        List<String> added = new ArrayList<>();
        for (String n : newNips) {
            String clean = n.replaceAll("[^a-zA-Z0-9_-]", "").trim();
            if (!clean.isEmpty() && !existing.contains(clean)) {
                existing.add(clean);
                added.add(clean);
            }
        }

        if (added.isEmpty()) {
            Toast.makeText(this, "Semua NIP pegawai sudah ada di daftar.", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder sb = new StringBuilder(currentText);
        if (sb.length() > 0) {
            sb.append("\n");
        }
        for (int i = 0; i < added.size(); i++) {
            sb.append(added.get(i));
            if (i < added.size() - 1) {
                sb.append("\n");
            }
        }

        etNips.setText(sb.toString());
        updateNipCounter();
        Toast.makeText(this, "Berhasil menambahkan " + added.size() + " NIP (" + sourceLabel + ").", Toast.LENGTH_SHORT).show();
    }

    private void switchMode(int mode) {
        activeMode = mode;
        int primaryBrand = ContextCompat.getColor(this, R.color.primary_brand);
        int surfaceMain = ContextCompat.getColor(this, R.color.surface_main);
        int textHint = ContextCompat.getColor(this, R.color.text_hint);

        btnTabMode1.setCardBackgroundColor(Color.TRANSPARENT);
        btnTabMode1.setCardElevation(0f);
        btnTabMode2.setCardBackgroundColor(Color.TRANSPARENT);
        btnTabMode2.setCardElevation(0f);
        btnTabMode3.setCardBackgroundColor(Color.TRANSPARENT);
        btnTabMode3.setCardElevation(0f);

        tvTabMode1.setTextColor(textHint);
        tvTabMode2.setTextColor(textHint);
        tvTabMode3.setTextColor(textHint);

        cvPreviewContainer.setVisibility(View.GONE);
        previewItemList.clear();

        if (mode == 1) {
            btnTabMode1.setCardBackgroundColor(surfaceMain);
            btnTabMode1.setCardElevation(4f);
            tvTabMode1.setTextColor(primaryBrand);

            tvTabSubtitle.setText("MODE 1: INPUT PRESENSI MASSAL (MASUK & PULANG)");
            cbUpdateAl.setVisibility(View.VISIBLE);
            llContainerMode2.setVisibility(View.GONE);
            llContainerMode3.setVisibility(View.GONE);
            tvSummaryInfo.setText("Metode 1: Mengisi jam masuk & pulang lengkap. Data alpha akan disesuaikan jika centang aktif.");

        } else if (mode == 2) {
            btnTabMode2.setCardBackgroundColor(surfaceMain);
            btnTabMode2.setCardElevation(4f);
            tvTabMode2.setTextColor(primaryBrand);

            tvTabSubtitle.setText("MODE 2: UPDATE KEHADIRAN SESUAI TIMETABLE");
            cbUpdateAl.setVisibility(View.GONE);
            llContainerMode2.setVisibility(View.VISIBLE);
            llContainerMode3.setVisibility(View.GONE);
            tvSummaryInfo.setText("Metode 2: Mengkoreksi jam presensi yang sudah tercatat di DB disesuaikan dengan jadwal Timetable/Shift.");

        } else if (mode == 3) {
            btnTabMode3.setCardBackgroundColor(surfaceMain);
            btnTabMode3.setCardElevation(4f);
            tvTabMode3.setTextColor(primaryBrand);

            tvTabSubtitle.setText("MODE 3: INPUT ABSEN KHUSUS (PARSIAL)");
            cbUpdateAl.setVisibility(View.GONE);
            llContainerMode2.setVisibility(View.GONE);
            llContainerMode3.setVisibility(View.VISIBLE);
            tvSummaryInfo.setText("Metode 3: Mengisi/memperbarui HANYA jam masuk ATAU HANYA jam pulang tanpa mengubah komponen jam lainnya.");
        }
    }

    private void showDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            etTanggal.setText(dateFormat.format(calendar.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker() {
        int hour = 7;
        int minute = 30;
        try {
            String val = etCustomJam.getText().toString().trim();
            if (val.contains(":")) {
                String[] p = val.split(":");
                hour = Integer.parseInt(p[0]);
                minute = Integer.parseInt(p[1]);
            }
        } catch (Exception ignored) {}

        new TimePickerDialog(this, (view, hourOfDay, minuteOfHour) -> {
            String timeStr = String.format(Locale.US, "%02d:%02d", hourOfDay, minuteOfHour);
            etCustomJam.setText(timeStr);
        }, hour, minute, true).show();
    }

    private void loadOpdList() {
        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getAdminOpdList().enqueue(new Callback<AdminOpdListResponse>() {
            @Override
            public void onResponse(@NonNull Call<AdminOpdListResponse> call, @NonNull Response<AdminOpdListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                    listOpd = response.body().getData();
                    if (listOpd == null) listOpd = new ArrayList<>();
                }
            }

            @Override
            public void onFailure(@NonNull Call<AdminOpdListResponse> call, @NonNull Throwable t) {}
        });
    }

    private void showOpdSelectionDialog() {
        if (listOpd == null || listOpd.isEmpty()) {
            Toast.makeText(this, "Memuat daftar OPD dari server...", Toast.LENGTH_SHORT).show();
            loadOpdList();
            return;
        }

        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_search_selection);

        TextView tvTitle = dialog.findViewById(R.id.tvSearchDialogTitle);
        EditText etSearch = dialog.findViewById(R.id.etSearchDialogInput);
        ListView lvItems = dialog.findViewById(R.id.lvSearchDialogItems);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseSearchDialog);

        if (tvTitle != null) tvTitle.setText("Pilih OPD (Perangkat Daerah)");

        final List<OpdModel> filteredList = new ArrayList<>(listOpd);
        final ArrayAdapter<OpdModel> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, filteredList);
        if (lvItems != null) lvItems.setAdapter(adapter);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String query = s.toString().toLowerCase().trim();
                    filteredList.clear();
                    for (OpdModel opd : listOpd) {
                        if (opd.getOpd().toLowerCase().contains(query)) {
                            filteredList.add(opd);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        if (lvItems != null) {
            lvItems.setOnItemClickListener((parent, view, position, id) -> {
                OpdModel item = adapter.getItem(position);
                if (item != null) {
                    selectedOpd = item;
                    selectedOpdId = item.getId();
                    tvAdminInsertOpdSelect.setText(item.getOpd());
                    Toast.makeText(this, "OPD Dipilih: " + item.getOpd(), Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            });
        }

        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void populateOpdNips() {
        if (selectedOpdId == null || selectedOpdId.isEmpty()) {
            Toast.makeText(this, "Silakan pilih OPD terlebih dahulu.", Toast.LENGTH_SHORT).show();
            showOpdSelectionDialog();
            return;
        }

        String filterShift = "all";
        if (activeMode == 2) {
            filterShift = rbShiftNon2.isChecked() ? "non_shift" : (rbShiftShift2.isChecked() ? "shift" : "all");
        } else if (activeMode == 3) {
            filterShift = rbShiftNon3.isChecked() ? "non_shift" : (rbShiftShift3.isChecked() ? "shift" : "all");
        }

        Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
        dialogLoading.setContentView(R.layout.view_proses);
        dialogLoading.setCancelable(false);
        dialogLoading.show();

        HttpService api = RetroClient.getInstance(this).getApi2();
        api.getPegawaiByOpd(selectedOpdId, filterShift).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                dialogLoading.dismiss();
                if (response.isSuccessful() && response.body() != null) {
                    JsonObject body = response.body();
                    if (body.has("data") && body.get("data").isJsonArray()) {
                        JsonArray arr = body.getAsJsonArray("data");
                        List<String> nips = new ArrayList<>();
                        for (JsonElement el : arr) {
                            if (el.isJsonObject() && el.getAsJsonObject().has("nip")) {
                                String nip = el.getAsJsonObject().get("nip").getAsString();
                                if (nip != null && !nip.trim().isEmpty()) {
                                    nips.add(nip.trim());
                                }
                            }
                        }
                        appendNipsToEditText(nips, "OPD: " + (selectedOpd != null ? selectedOpd.getOpd() : selectedOpdId));
                    } else {
                        Toast.makeText(AdminInsertMassalActivity.this, "Tidak ada data pegawai aktif pada OPD ini.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AdminInsertMassalActivity.this, "Gagal memuat pegawai OPD (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                dialogLoading.dismiss();
                Toast.makeText(AdminInsertMassalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showNameMatchingDialog() {
        if (selectedOpdId == null || selectedOpdId.isEmpty()) {
            Toast.makeText(this, "Silakan pilih OPD terlebih dahulu sebelum mencocokkan nama.", Toast.LENGTH_SHORT).show();
            showOpdSelectionDialog();
            return;
        }

        Dialog dialog = new Dialog(this, R.style.DialogStyle);
        dialog.setContentView(R.layout.dialog_search_selection);

        TextView tvTitle = dialog.findViewById(R.id.tvSearchDialogTitle);
        EditText etRawNames = dialog.findViewById(R.id.etSearchDialogInput);
        ImageView ivClose = dialog.findViewById(R.id.ivCloseSearchDialog);
        ListView lvItems = dialog.findViewById(R.id.lvSearchDialogItems);

        if (tvTitle != null) tvTitle.setText("Tempel / Ketik Daftar Nama Pegawai");
        if (etRawNames != null) {
            etRawNames.setHint("JEPTA THOMAS SIJABAT, SE, SYAIFUL BAHRI, SE, M.H...");
            etRawNames.setSingleLine(false);
            etRawNames.setLines(4);
        }

        List<String> instructions = new ArrayList<>();
        instructions.add("Tekan di bawah untuk mencocokkan nama dengan OPD " + (selectedOpd != null ? selectedOpd.getOpd() : "terpilih"));
        instructions.add(">> Klik di sini untuk EKSEKUSI PENCOCOKAN NAMA <<");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, instructions);
        if (lvItems != null) lvItems.setAdapter(adapter);

        if (lvItems != null) {
            lvItems.setOnItemClickListener((parent, view, position, id) -> {
                String rawNames = (etRawNames != null) ? etRawNames.getText().toString().trim() : "";
                if (rawNames.isEmpty()) {
                    Toast.makeText(this, "Silakan ketik/tempel minimal 1 nama pegawai.", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialog.dismiss();

                Dialog dialogLoading = new Dialog(this, R.style.DialogStyle);
                dialogLoading.setContentView(R.layout.view_proses);
                dialogLoading.setCancelable(false);
                dialogLoading.show();

                HttpService api = RetroClient.getInstance(this).getApi2();
                api.cocokkanNama(selectedOpdId, rawNames).enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                        dialogLoading.dismiss();
                        if (response.isSuccessful() && response.body() != null) {
                            JsonObject body = response.body();
                            if (body.has("matched") && body.get("matched").isJsonArray()) {
                                JsonArray matchedArr = body.getAsJsonArray("matched");
                                List<String> matchedNips = new ArrayList<>();
                                for (JsonElement el : matchedArr) {
                                    if (el.isJsonObject() && el.getAsJsonObject().has("nip")) {
                                        String nip = el.getAsJsonObject().get("nip").getAsString();
                                        if (nip != null && !nip.trim().isEmpty()) {
                                            matchedNips.add(nip.trim());
                                        }
                                    }
                                }
                                int totalSesuai = body.has("total_sesuai") ? body.get("total_sesuai").getAsInt() : 0;
                                int totalMendekati = body.has("total_mendekati") ? body.get("total_mendekati").getAsInt() : 0;
                                appendNipsToEditText(matchedNips, "Pencocokan Nama (" + totalSesuai + " Sesuai, " + totalMendekati + " Mendekati)");
                            } else {
                                Toast.makeText(AdminInsertMassalActivity.this, "Tidak ada nama yang cocok pada OPD ini.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(AdminInsertMassalActivity.this, "Gagal mencocokkan nama (HTTP " + response.code() + ")", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                        dialogLoading.dismiss();
                        Toast.makeText(AdminInsertMassalActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        if (ivClose != null) ivClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void previewMassal() {
        String tanggal = etTanggal.getText().toString().trim();
        String nips = etNips.getText().toString().trim();

        if (tanggal.isEmpty() || nips.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Tanggal dan daftar NIP pegawai wajib diisi.");
            return;
        }

        if ((activeMode == 2 || activeMode == 3) && (selectedOpdId == null || selectedOpdId.isEmpty())) {
            dialogView.viewNotifKosong(this, "Peringatan OPD", "Silakan pilih OPD terlebih dahulu untuk Mode " + activeMode + ".");
            showOpdSelectionDialog();
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (activeMode == 1) {
            boolean updateAl = cbUpdateAl.isChecked();
            api.previewInsertMassal(tanggal, nips, updateAl).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    dialogProses.dismiss();
                    handlePreviewResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });

        } else if (activeMode == 2) {
            String filterShift = rbShiftNon2.isChecked() ? "non_shift" : (rbShiftShift2.isChecked() ? "shift" : "all");
            String updateTarget = rbTargetMasuk.isChecked() ? "masuk" : (rbTargetPulang.isChecked() ? "pulang" : "both");

            api.previewUpdateJadwal(selectedOpdId, tanggal, filterShift, updateTarget, nips).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    dialogProses.dismiss();
                    handlePreviewResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });

        } else if (activeMode == 3) {
            String jenisAbsen = rbJenisMasuk3.isChecked() ? "masuk" : "pulang";
            String filterShift = rbShiftNon3.isChecked() ? "non_shift" : (rbShiftShift3.isChecked() ? "shift" : "all");
            String jamMode = rbJamCustom.isChecked() ? "custom" : "timetable";
            String customJam = etCustomJam.getText().toString().trim();

            api.previewParsial(selectedOpdId, tanggal, jenisAbsen, filterShift, jamMode, customJam, nips).enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    dialogProses.dismiss();
                    handlePreviewResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        }
    }

    private void handlePreviewResponse(Response<JsonObject> response) {
        if (response.isSuccessful() && response.body() != null) {
            JsonObject body = response.body();
            String msg = "Preview berhasil diproses.";
            if (body.has("remarks") && !body.get("remarks").isJsonNull()) {
                msg = body.get("remarks").getAsString();
            } else if (body.has("message") && !body.get("message").isJsonNull()) {
                msg = body.get("message").getAsString();
            }
            tvSummaryInfo.setText("Hasil Preview (Tab " + activeMode + "): " + msg);

            previewItemList.clear();
            if (body.has("items") && body.get("items").isJsonArray()) {
                JsonArray arr = body.getAsJsonArray("items");
                for (JsonElement el : arr) {
                    if (el.isJsonObject()) {
                        JsonObject obj = el.getAsJsonObject();
                        PreviewItemModel item = new PreviewItemModel();
                        if (obj.has("nip") && !obj.get("nip").isJsonNull()) item.setNip(obj.get("nip").getAsString());
                        if (obj.has("nama") && !obj.get("nama").isJsonNull()) item.setNama(obj.get("nama").getAsString());
                        if (obj.has("opd") && !obj.get("opd").isJsonNull()) item.setOpd(obj.get("opd").getAsString());
                        if (obj.has("tipe_pegawai") && !obj.get("tipe_pegawai").isJsonNull()) item.setTipePegawai(obj.get("tipe_pegawai").getAsString());
                        if (obj.has("status") && !obj.get("status").isJsonNull()) item.setStatus(obj.get("status").getAsString());
                        if (obj.has("badge") && !obj.get("badge").isJsonNull()) item.setBadge(obj.get("badge").getAsString());
                        if (obj.has("keterangan") && !obj.get("keterangan").isJsonNull()) item.setKeterangan(obj.get("keterangan").getAsString());

                        if (obj.has("jam_masuk_display") && !obj.get("jam_masuk_display").isJsonNull()) {
                            item.setJamMasuk(obj.get("jam_masuk_display").getAsString());
                        } else if (obj.has("jam_masuk_target") && !obj.get("jam_masuk_target").isJsonNull()) {
                            item.setJamMasuk(obj.get("jam_masuk_target").getAsString());
                        } else if (obj.has("jadwal_masuk") && !obj.get("jadwal_masuk").isJsonNull()) {
                            item.setJamMasuk(obj.get("jadwal_masuk").getAsString());
                        }

                        if (obj.has("jam_pulang_display") && !obj.get("jam_pulang_display").isJsonNull()) {
                            item.setJamPulang(obj.get("jam_pulang_display").getAsString());
                        } else if (obj.has("jam_pulang_target") && !obj.get("jam_pulang_target").isJsonNull()) {
                            item.setJamPulang(obj.get("jam_pulang_target").getAsString());
                        } else if (obj.has("jadwal_pulang") && !obj.get("jadwal_pulang").isJsonNull()) {
                            item.setJamPulang(obj.get("jadwal_pulang").getAsString());
                        }

                        if (obj.has("can_execute") && !obj.get("can_execute").isJsonNull()) {
                            item.setCanExecute(obj.get("can_execute").getAsBoolean());
                        }
                        if (obj.has("action_type") && !obj.get("action_type").isJsonNull()) {
                            item.setActionType(obj.get("action_type").getAsString());
                        }
                        if (obj.has("presensi_id") && !obj.get("presensi_id").isJsonNull()) {
                            item.setPresensiId(obj.get("presensi_id").getAsString());
                        }

                        previewItemList.add(item);
                    }
                }
            }

            if (!previewItemList.isEmpty()) {
                cvPreviewContainer.setVisibility(View.VISIBLE);
                tvPreviewCountHeader.setText(previewItemList.size() + " Pegawai");
                previewAdapter.notifyDataSetChanged();
            } else {
                cvPreviewContainer.setVisibility(View.GONE);
            }

            Toast.makeText(this, "Preview: " + previewItemList.size() + " data presensi dimuat.", Toast.LENGTH_SHORT).show();
        } else {
            cvPreviewContainer.setVisibility(View.GONE);
            String errMessage = extractErrorMessageJsonObject(response);
            dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Preview", errMessage);
        }
    }

    private void storeMassal() {
        String tanggal = etTanggal.getText().toString().trim();
        String nips = etNips.getText().toString().trim();

        if (tanggal.isEmpty() || nips.isEmpty()) {
            dialogView.viewNotifKosong(this, "Peringatan", "Tanggal dan daftar NIP pegawai wajib diisi.");
            return;
        }

        if ((activeMode == 2 || activeMode == 3) && (selectedOpdId == null || selectedOpdId.isEmpty())) {
            dialogView.viewNotifKosong(this, "Peringatan OPD", "Silakan pilih OPD terlebih dahulu untuk Mode " + activeMode + ".");
            showOpdSelectionDialog();
            return;
        }

        Dialog dialogProses = new Dialog(this, R.style.DialogStyle);
        dialogProses.setContentView(R.layout.view_proses);
        dialogProses.setCancelable(false);
        dialogProses.show();

        HttpService api = RetroClient.getInstance(this).getApi2();

        if (activeMode == 1) {
            boolean updateAl = cbUpdateAl.isChecked();
            api.storeInsertMassal(tanggal, nips, updateAl).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleStoreResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });

        } else if (activeMode == 2) {
            String filterShift = rbShiftNon2.isChecked() ? "non_shift" : (rbShiftShift2.isChecked() ? "shift" : "all");
            String updateTarget = rbTargetMasuk.isChecked() ? "masuk" : (rbTargetPulang.isChecked() ? "pulang" : "both");

            api.storeUpdateJadwal(selectedOpdId, tanggal, filterShift, updateTarget, nips).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleStoreResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });

        } else if (activeMode == 3) {
            String jenisAbsen = rbJenisMasuk3.isChecked() ? "masuk" : "pulang";
            String filterShift = rbShiftNon3.isChecked() ? "non_shift" : (rbShiftShift3.isChecked() ? "shift" : "all");
            String jamMode = rbJamCustom.isChecked() ? "custom" : "timetable";
            String customJam = etCustomJam.getText().toString().trim();

            api.storeParsial(selectedOpdId, tanggal, jenisAbsen, filterShift, jamMode, customJam, nips).enqueue(new Callback<ResponsePOJO>() {
                @Override
                public void onResponse(@NonNull Call<ResponsePOJO> call, @NonNull Response<ResponsePOJO> response) {
                    dialogProses.dismiss();
                    handleStoreResponse(response);
                }

                @Override
                public void onFailure(@NonNull Call<ResponsePOJO> call, @NonNull Throwable t) {
                    dialogProses.dismiss();
                    dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Koneksi Gagal", t.getMessage());
                }
            });
        }
    }

    private void handleStoreResponse(Response<ResponsePOJO> response) {
        if (response.isSuccessful() && response.body() != null) {
            ResponsePOJO pojo = response.body();
            String msg = pojo.getRemarks() != null ? pojo.getRemarks() : "Eksekusi absensi berhasil!";
            tvSummaryInfo.setText("Hasil Eksekusi (Tab " + activeMode + "): " + msg);
            if (pojo.isStatus()) {
                dialogView.viewSuksesNonFinish(AdminInsertMassalActivity.this, msg);
            } else {
                dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Eksekusi", msg);
            }
        } else {
            String errMessage = extractErrorMessage(response);
            dialogView.viewNotifKosong(AdminInsertMassalActivity.this, "Gagal Server", errMessage);
        }
    }

    private String extractErrorMessage(Response<ResponsePOJO> response) {
        String defaultMsg = "HTTP " + response.code();
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject obj = new JSONObject(errorJson);
                if (obj.has("remarks")) {
                    return obj.getString("remarks");
                } else if (obj.has("message")) {
                    return obj.getString("message");
                }
            }
        } catch (Exception ignored) {}
        return defaultMsg;
    }

    private String extractErrorMessageJsonObject(Response<JsonObject> response) {
        String defaultMsg = "HTTP " + response.code();
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                JSONObject obj = new JSONObject(errorJson);
                if (obj.has("remarks")) {
                    return obj.getString("remarks");
                } else if (obj.has("message")) {
                    return obj.getString("message");
                }
            }
        } catch (Exception ignored) {}
        return defaultMsg;
    }
}
