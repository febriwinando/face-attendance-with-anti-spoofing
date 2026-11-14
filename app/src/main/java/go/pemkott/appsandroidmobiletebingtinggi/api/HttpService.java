
package go.pemkott.appsandroidmobiletebingtinggi.api;

import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.login.Logout;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanDetailResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.AduanResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.CheckUpdate;
import go.pemkott.appsandroidmobiletebingtinggi.model.DataEmployee;
import go.pemkott.appsandroidmobiletebingtinggi.model.Employee;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeeResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.FileModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.HasilValidasi;
import go.pemkott.appsandroidmobiletebingtinggi.model.InfoMP;
import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
import go.pemkott.appsandroidmobiletebingtinggi.model.LoginResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.Presensi;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukKeduaFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapServer;
import go.pemkott.appsandroidmobiletebingtinggi.model.ReturnValidasi;
import go.pemkott.appsandroidmobiletebingtinggi.model.TimeTables;
import go.pemkott.appsandroidmobiletebingtinggi.model.Updatep;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiData;
import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiModel;
import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.TimeTebleSetting;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface HttpService {
    @Multipart
    @Headers("Accept: application/json")
    @POST("helpdesk/aduan")
    Call<FileModel> kirimAduan(
            @Header("Authorization") String token,
            @Part("employee_id") RequestBody employee_id,
            @Part("nama") RequestBody nama,
            @Part("nip") RequestBody nip,
            @Part("opdid") RequestBody opdid,
            @Part("kategori") RequestBody kategori,
            @Part("prioritas") RequestBody prioritas,
            @Part("lokasi_aduan") RequestBody lokasi_aduan,
            @Part("judul") RequestBody judul,
            @Part("deskripsi") RequestBody deskripsi,
            @Part List<MultipartBody.Part> gambar
    );

    @Headers("Accept: application/json")
    @GET("helpdesk/aduan")
    Call<AduanResponse> getAduans(
            @Header("Authorization") String token,
            @Query("employee_id") int employeeId,
            @Query("status") String status,
            @Query("page") Integer page,
            @Query("per_page") Integer perPage
    );

    @Headers("Accept: application/json")
    @GET("helpdesk/aduan/{id}")
    Call<AduanDetailResponse> getAduanDetail(
            @Path("id") int aduanId,
            @Header("Authorization") String token,
            @Query("employee_id") int employeeId
    );

    @GET("v2/employees")
    Call<EmployeeResponse> getEmployees();
    @FormUrlEncoded
    @POST("validasipulangpertamaterbaru")
    Call<ValidasiModel> hasilValidasiPulangPertama(
            @Field("id") String id,
            @Field("tahap") String tahap,
            @Field("statusvalidasi") String statusvalidasi,
            @Field("ctt") String ctt,
            @Field("pegawai") String pegawai,
            @Field("ipp") String ipp
    );
    
    @FormUrlEncoded
    @POST("validasimasukpertamaterbaru")
    Call<ValidasiModel> hasilValidasiMasukPertama(
            @Field("id") String id,
            @Field("tahap") String tahap,
            @Field("statusvalidasi") String statusvalidasi,
            @Field("ctt") String ctt,
            @Field("verifikatorid") String verifikatorid,
            @Field("ipp") String ipp
    );

    @FormUrlEncoded
    @POST("verifikasicount")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.VerifikasiCountResponse> getVerifikasiCount(
            @Field("id") String id
    );

    @FormUrlEncoded
    @POST("validasibatch")
    Call<ValidasiModel> validasiBatch(
            @Field("ids[]") List<String> ids,
            @Field("posisi") String posisi,
            @Field("tahap") String tahap,
            @Field("statusvalidasi") String statusvalidasi,
            @Field("ctt") String ctt,
            @Field("ipp") String ipp
    );

    @FormUrlEncoded
    @POST("login")
    Call<List<LoginResponse>> login(
            @Field("username") String username,
            @Field("password") String password,
            @Field("app_version") String appVersion
    );

    @FormUrlEncoded
    @POST("login")
    Call<List<LoginResponse>> login2fa(
            @Field("username") String username,
            @Field("password") String password,
            @Field("otp") String otp,
            @Field("app_version") String appVersion
    );

    @FormUrlEncoded
    @POST("dataEmployee")
    Call<DataEmployee> dataEmployee(
            @Field("id") String id
    );
    // UPLOAD
    @Multipart
    @POST("upload_file/RestApi/upload_api.php")
    Call<FileModel> callUploadApi(@Part MultipartBody.Part image);

    // ==============================
    // LOGOUT
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Logout>> getUrlLogout(@Url String url, @Header("Authorization") String token);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Logout>> getUrlLogout(@Url String url, @Header("Authorization") String token, @Body RequestBody empty);

    // ==============================
    // KEGIATAN
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<KegiatanIzin>> getUrlKegiatan(@Url String url, @Header("Authorization")String token , @Body RequestBody empty );

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Koordinat>> getUrlKoordinat(@Url String url, @Header("Authorization")String token, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<KegiatanIzin>> getUrlKegiatanNew(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<KegiatanIzin>> getUrlKegiatanNew(@Url String url, @Body RequestBody empty);

    // ==============================
    // EMPLOYEE
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<Employee> getUrlEmployee(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<Employee> getUrlEmployee(@Url String url, @Body RequestBody empty);

    // ==============================
    // UPLOAD KEGIATAN
    // ==============================
    @Multipart
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<FileModel>> getUrlUploadKegiatan(
            @Url String url,
            @Part List<MultipartBody.Part> image,
            @Part List<MultipartBody.Part> lampiran,
            @Header("Authorization")String token
    );

    @Multipart
    @POST
    Call<FileModel> callMultipleUploadApi(
            @Url String url,
            @Part List<MultipartBody.Part> image,
            @Part List<MultipartBody.Part> lampiran,
            @Header("Authorization")String token
    );

    @Multipart
    @FormUrlEncoded
    @POST
    Call<FileModel> callMultipleUploadApiBase(@Url String url);

    @Multipart
    @POST
    Call<FileModel> callUpdateProfil(@Url String url, @Part List<MultipartBody.Part> image, @Header("Authorization")String token);

    @Multipart
    @POST
    Call<FileModel> callMultipleUploadApiPerjalananDinas(@Url String url, @Part List<MultipartBody.Part> image, @Part List<MultipartBody.Part> lampiran, @Header("Authorization")String token);

    // ==============================
    // TIMETABLE
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<TimeTables>> getUrlTimeTable(@Url String url, @Header("Authorization")String token );

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<TimeTables>> getUrlTimeTable(@Url String url, @Header("Authorization")String token, @Body RequestBody empty );

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<WaktuSift>> getTestSift(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<WaktuSift>> getTestSift(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<JadwalSift>> getJadwalSift(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<JadwalSift>> getJadwalSift(@Url String url, @Body RequestBody empty);

    @FormUrlEncoded
    @POST("jadwalsift")
    Call<ArrayList<JadwalSift>> getJadwalSifts(
            @Field("ide") String ide,
            @Field("bulan") String bulan,
            @Field("tahun") String tahun
    );



    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<TimeTebleSetting>> getUrlTimeTableSetting(@Url String url, @Header("Authorization")String token );

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<TimeTebleSetting>> getUrlTimeTableSetting(@Url String url, @Header("Authorization")String token, @Body RequestBody empty );

    // ==============================
    // UPDATE
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Updatep>> getUrlUpdatep(@Url String url );

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Updatep>> getUrlUpdatep(@Url String url, @Body RequestBody empty );

    // ==============================
    // KOORDINAT
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Koordinat>> getUrlKoordinat(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Koordinat>> getUrlKoordinat(@Url String url, @Body RequestBody empty);

    // ==============================
    // INFO MP
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<InfoMP>> getUrlInfoMP(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<InfoMP>> getUrlInfoMP(@Url String url, @Body RequestBody empty);

    // ==============================
    // PRESENSI
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Presensi>> getUrlPresensi(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<Presensi>> getUrlPresensi(@Url String url, @Body RequestBody empty);

    // ==============================
    // VALIDASI LIST
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ValidasiData>> getUrlListValidasi(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ValidasiData>> getUrlListValidasi(@Url String url, @Body RequestBody empty);

    // ==============================
    // REKAP SERVER
    // ==============================
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapServer>> getUrlRekapServer(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapServer>> getUrlRekapServer(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapMasukFragment>> getRekapMasukFragment(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapMasukFragment>> getRekapMasukFragment(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapMasukKeduaFragment>> getRekapMasukKeduaFragment(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapMasukKeduaFragment>> getRekapMasukKeduaFragment(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapPulangFragment>> getRekapPulangFragment(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapPulangFragment>> getRekapPulangFragment(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapPulangKeduaFragment>> getRekapPulangKeduaFragment(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<RekapPulangKeduaFragment>> getRekapPulangKeduaFragment(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ReturnValidasi>> getReturnValidasi(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ReturnValidasi>> getReturnValidasi(@Url String url, @Body RequestBody empty);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<HasilValidasi>> getUrlHasilValidasi(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<HasilValidasi>> getUrlHasilValidasi(@Url String url, @Body RequestBody empty);

    // VALIDASI MASUK PERTAMA
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ValidasiModel>> getUrlHasilValidasiMasukPertama(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<ValidasiModel>> getUrlHasilValidasiMasukPertama(@Url String url, @Body RequestBody empty);

    // CHECK UPDATE
    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<CheckUpdate>> getCheckUpdate(@Url String url);

    @Headers({"Content-Type: application/json"})
    @POST
    Call<List<CheckUpdate>> getCheckUpdate(@Url String url, @Body RequestBody empty);

    // NATIVE SUPER ADMIN APIS
    @FormUrlEncoded
    @POST("v2/admin/update-masuk")
    Call<ResponsePOJO> updateDataMasukAdmin(
            @Field("tanggal") String tanggal,
            @Field("masuk") String masuk,
            @Field("keterlambatan") int keterlambatan,
            @Field("ket_masuk") String ket_masuk,
            @Field("posisi_masuk") String posisi_masuk,
            @Field("status_masuk") String status_masuk,
            @Field("validasi_masuk") String validasi_masuk,
            @Field("pegawai[]") List<String> pegawai
    );

    // Insert Massal Presensi Endpoints (3 Tab Complete)
    @FormUrlEncoded
    @POST("v2/admin/insert-massal/preview")
    Call<com.google.gson.JsonObject> previewInsertMassal(
            @Field("tanggal") String tanggal,
            @Field("nips") String nips,
            @Field("update_posisi_al") boolean updatePosisiAl
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/store")
    Call<ResponsePOJO> storeInsertMassal(
            @Field("tanggal") String tanggal,
            @Field("nips") String nips,
            @Field("update_posisi_al") boolean updatePosisiAl
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/update-jadwal/preview")
    Call<com.google.gson.JsonObject> previewUpdateJadwal(
            @Field("opd_id") String opdId,
            @Field("tanggal") String tanggal,
            @Field("filter_shift") String filterShift,
            @Field("update_target") String updateTarget,
            @Field("nips") String nips,
            @Field("overwrite_existing") boolean overwriteExisting
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/update-jadwal/store")
    Call<ResponsePOJO> storeUpdateJadwal(
            @Field("opd_id") String opdId,
            @Field("tanggal") String tanggal,
            @Field("filter_shift") String filterShift,
            @Field("update_target") String updateTarget,
            @Field("nips") String nips,
            @Field("overwrite_existing") boolean overwriteExisting
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/parsial/preview")
    Call<com.google.gson.JsonObject> previewParsial(
            @Field("opd_id") String opdId,
            @Field("tanggal") String tanggal,
            @Field("jenis_absen") String jenisAbsen,
            @Field("filter_shift") String filterShift,
            @Field("jam_mode") String jamMode,
            @Field("custom_jam") String customJam,
            @Field("nips") String nips,
            @Field("overwrite_existing") boolean overwriteExisting
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/parsial/store")
    Call<ResponsePOJO> storeParsial(
            @Field("opd_id") String opdId,
            @Field("tanggal") String tanggal,
            @Field("jenis_absen") String jenisAbsen,
            @Field("filter_shift") String filterShift,
            @Field("jam_mode") String jamMode,
            @Field("custom_jam") String customJam,
            @Field("nips") String nips,
            @Field("overwrite_existing") boolean overwriteExisting
    );

    @GET("v2/admin/insert-massal/pegawai-by-opd")
    Call<com.google.gson.JsonObject> getPegawaiByOpd(
            @Query("opd_id") String opdId,
            @Query("filter_shift") String filterShift
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/cocokkan-nama")
    Call<com.google.gson.JsonObject> cocokkanNama(
            @Field("opd_id") String opdId,
            @Field("raw_names") String rawNames
    );

    @FormUrlEncoded
    @POST("v2/admin/pegawai/status")
    Call<ResponsePOJO> updateStatusPegawaiAdmin(
            @Field("id") String id,
            @Field("active") int active
    );

    @FormUrlEncoded
    @POST("v2/admin/lokasi-pegawai/store")
    Call<ResponsePOJO> storeLokasiPegawaiAdmin(
            @Field("employee_id") String employee_id,
            @Field("nip") String nip,
            @Field("pegawai_id") String pegawai_id,
            @Field("id_pegawai") String id_pegawai,
            @Field("opd_id") String opd_id,
            @Field("eOPD") String eOPD,
            @Field("alamat") String alamat,
            @Field("let") String let,
            @Field("lat") String lat,
            @Field("latitude") String latitude,
            @Field("lng") String lng,
            @Field("longitude") String longitude,
            @Field("status") String status
    );

    @FormUrlEncoded
    @POST("v2/admin/pengolahan/store")
    Call<ResponsePOJO> storePengolahanAbsensiAdmin(
            @Field("id_absensi") String id_absensi,
            @Field("jam_masuk") String jam_masuk,
            @Field("keterlambatan") Integer keterlambatan,
            @Field("posisi_masuk") String posisi_masuk,
            @Field("status_masuk") String status_masuk,
            @Field("validasi_masuk") String validasi_masuk,
            @Field("ket_masuk") String ket_masuk,
            @Field("jam_pulang") String jam_pulang,
            @Field("posisi_pulang") String posisi_pulang,
            @Field("status_pulang") String status_pulang,
            @Field("validasi_pulang") String validasi_pulang,
            @Field("clear_pulang") String clear_pulang
    );

    @Multipart
    @POST("admin/update-masuk")
    Call<ResponsePOJO> updateDataMasukAdminMultipart(
            @Part("tanggal") RequestBody tanggal,
            @Part("masuk") RequestBody masuk,
            @Part("keterlambatan") RequestBody keterlambatan,
            @Part("ket_masuk") RequestBody ket_masuk,
            @Part("posisi_masuk") RequestBody posisi_masuk,
            @Part("status_masuk") RequestBody status_masuk,
            @Part("validasi_masuk") RequestBody validasi_masuk,
            @Part("pegawai[]") List<RequestBody> pegawai,
            @Part MultipartBody.Part fototaging_masuk,
            @Part MultipartBody.Part lampiran_masuk
    );

    @Multipart
    @POST("admin/pengolahan/store")
    Call<ResponsePOJO> storePengolahanAbsensiAdminMultipart(
            @Part("id_absensi") RequestBody id_absensi,
            @Part("jam_masuk") RequestBody jam_masuk,
            @Part("keterlambatan") RequestBody keterlambatan,
            @Part("posisi_masuk") RequestBody posisi_masuk,
            @Part("status_masuk") RequestBody status_masuk,
            @Part("validasi_masuk") RequestBody validasi_masuk,
            @Part("ket_masuk") RequestBody ket_masuk,
            @Part("jam_pulang") RequestBody jam_pulang,
            @Part("posisi_pulang") RequestBody posisi_pulang,
            @Part("status_pulang") RequestBody status_pulang,
            @Part("validasi_pulang") RequestBody validasi_pulang,
            @Part("clear_pulang") RequestBody clear_pulang,
            @Part MultipartBody.Part fototaging_masuk,
            @Part MultipartBody.Part fototaging_pulang,
            @Part MultipartBody.Part lampiran_masuk,
            @Part MultipartBody.Part lampiran_pulang
    );

    @GET("v2/opd-list")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse> getOpdList();

    @GET("admin/opd-list")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.AdminOpdListResponse> getAdminOpdList();

    @GET("admin/pegawai/list")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.AdminEmployeeListResponse> getAdminPegawaiList(@Query("opd_id") String opdId);

    @GET("admin/pengolahan/find-presence")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.AdminPresenceFindResponse> findPresenceAdmin(@Query("employee_id") String employeeId, @Query("tanggal") String tanggal);

    @GET
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.berita.BeritaResponse> getBeritaDiskominfo(@Url String url);

    @GET
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.berita.KomentarResponse> getKomentarBerita(@Url String url);

    @POST
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.berita.KomentarResponse> postKomentarBerita(@Url String url, @Body RequestBody body);

    @GET("chat/contacts")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContactResponse> getChatContacts(
            @Query("search") String search,
            @Query("opd_id") String opdId,
            @Query("active_only") int activeOnly
    );

    @GET("chat/opd-group-profile")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.OpdGroupProfileResponse> getOpdGroupProfile(
            @Query("opd_id") long opdId
    );

    @GET("chat/messages")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageResponse> getChatMessages(@Query("chat_type") String chatType, @Query("target_id") long targetId);

    @GET("chat/messages")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageResponse> getChatMessagesIncremental(@Query("chat_type") String chatType, @Query("target_id") long targetId, @Query("after_id") long afterId);

    @FormUrlEncoded
    @POST("chat/send")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> sendChatMessage(
            @Field("chat_type") String chatType,
            @Field("target_id") long targetId,
            @Field("message") String message,
            @Field("msg_type") String msgType,
            @Field("reply_to_id") String replyToId
    );

    @FormUrlEncoded
    @POST("chat/send")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> forwardChatMessage(
            @Field("chat_type") String chatType,
            @Field("target_id") long targetId,
            @Field("message") String message,
            @Field("msg_type") String msgType,
            @Field("file_url") String fileUrl,
            @Field("file_name") String fileName
    );

    @Multipart
    @POST("chat/send")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> sendChatMessageMultipart(
            @Part("chat_type") RequestBody chatType,
            @Part("target_id") RequestBody targetId,
            @Part("message") RequestBody message,
            @Part("msg_type") RequestBody msgType,
            @Part("reply_to_id") RequestBody replyToId,
            @Part MultipartBody.Part file
    );

    @GET("chat/privacy")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> getChatPrivacySettings();

    @FormUrlEncoded
    @POST("chat/privacy")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> saveChatPrivacySettings(
            @Field("privacy_mode") String privacyMode,
            @Field("allowed_employee_ids") String allowedEmployeeIdsJson,
            @Field("allowed_opd_ids") String allowedOpdIdsJson
    );

    @FormUrlEncoded
    @POST("chat/privacy")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> saveAdvancedChatPrivacySettings(
            @Field("privacy_mode") String privacyMode,
            @Field("allowed_employee_ids") String allowedEmployeeIdsJson,
            @Field("allowed_opd_ids") String allowedOpdIdsJson,
            @Field("read_receipts_enabled") int readReceiptsEnabled,
            @Field("blocked_employee_ids") String blockedEmployeeIdsJson,
            @Field("profile_photo_privacy") String profilePhotoPrivacy
    );

    @FormUrlEncoded
    @POST("chat/block-contact")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse> toggleBlockContact(
            @Field("target_employee_id") long targetEmployeeId,
            @Field("action") String action
    );

    @FormUrlEncoded
    @POST("chat/delete-message")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> deleteChatMessage(
            @Field("message_id") long messageId
    );

    @FormUrlEncoded
    @POST("chat/delete-message")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> deleteChatMessage(
            @Field("message_id") long messageId,
            @Field("delete_type") String deleteType
    );

    @FormUrlEncoded
    @POST("chat/delete-for-everyone")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> deleteChatMessageForEveryone(
            @Field("message_id") long messageId
    );

    @FormUrlEncoded
    @POST("chat/mark-read")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> markChatMessagesRead(
            @Field("target_id") long targetId,
            @Field("chat_type") String chatType
    );

    @FormUrlEncoded
    @POST("chat/hard-delete-message")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> hardDeleteChatMessage(
            @Field("message_id") long messageId
    );

    @FormUrlEncoded
    @POST("chat/restore-message")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> restoreChatMessage(
            @Field("message_id") long messageId
    );

    @FormUrlEncoded
    @POST("chat/edit-message")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> editChatMessage(
            @Field("message_id") long messageId,
            @Field("message") String message
    );

    @FormUrlEncoded
    @POST("chat/clear-conversation")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> clearConversationHistory(
            @Field("target_id") long targetId,
            @Field("chat_type") String chatType
    );

    @FormUrlEncoded
    @POST("chat/clear-conversation")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> clearConversationHistory(
            @Field("target_id") long targetId,
            @Field("chat_type") String chatType,
            @Field("clear_type") String clearType
    );

    @FormUrlEncoded
    @POST("chat/accept-terms")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> acceptChatTerms(
            @Field("accepted") int accepted
    );

    @GET("chat/terms-status")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> getChatTermsStatus();

    @GET("chat/monitoring-conversations")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringResponse> getMonitoringConversations(
            @Query("search") String search,
            @Query("opd_id") String opdId
    );

    @GET("chat/monitoring-conversations")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringResponse> getMonitoringConversations(
            @Query("search") String search,
            @Query("opd_id") String opdId,
            @Query("employee_id") String employeeId,
            @Query("nip") String nip,
            @Query("date") String date,
            @Query("status") String status,
            @Query("chat_type") String chatType,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("chat/monitoring/messages")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessagesResponse> getMonitoringMessages(
            @Query("search") String search,
            @Query("opd_id") String opdId,
            @Query("employee_id") String employeeId,
            @Query("nip") String nip,
            @Query("date") String date,
            @Query("status") String status,
            @Query("chat_type") String chatType,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("chat/monitoring/conversation-detail")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessagesResponse> getMonitoringConversationDetail(
            @Query("chat_type") String chatType,
            @Query("participant_a") long participantA,
            @Query("participant_b") long participantB,
            @Query("opd_id") long opdId,
            @Query("status") String status,
            @Query("search") String search,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @FormUrlEncoded
    @POST("chat/monitoring/permanent-delete")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse> permanentDeleteMonitoringMessages(
            @Field("message_ids") String messageIdsCsv
    );

    @GET("chat/monitoring/audit-logs")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatAuditLogsResponse> getMonitoringAuditLogs(
            @Query("message_id") long messageId,
            @Query("nip") String nip,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("chat/global-status")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> getChatGlobalStatus();

    @FormUrlEncoded
    @POST("chat/global-status")
    Call<go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatGlobalStatusResponse> toggleChatGlobalStatus(
            @Field("chat_enabled") String chatEnabled
    );

}

//package go.pemkott.appsandroidmobiletebingtinggi.api;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import go.pemkott.appsandroidmobiletebingtinggi.login.Logout;
//import go.pemkott.appsandroidmobiletebingtinggi.model.CheckUpdate;
//import go.pemkott.appsandroidmobiletebingtinggi.model.Employee;
//import go.pemkott.appsandroidmobiletebingtinggi.model.FileModel;
//import go.pemkott.appsandroidmobiletebingtinggi.model.HasilValidasi;
//import go.pemkott.appsandroidmobiletebingtinggi.model.InfoMP;
//import go.pemkott.appsandroidmobiletebingtinggi.model.JadwalSift;
//import go.pemkott.appsandroidmobiletebingtinggi.model.KegiatanIzin;
//import go.pemkott.appsandroidmobiletebingtinggi.model.Koordinat;
//import go.pemkott.appsandroidmobiletebingtinggi.model.Presensi;
//import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukFragment;
//import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukKeduaFragment;
//import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangFragment;
//import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;
//import go.pemkott.appsandroidmobiletebingtinggi.model.RekapServer;
//import go.pemkott.appsandroidmobiletebingtinggi.model.ReturnValidasi;
//import go.pemkott.appsandroidmobiletebingtinggi.model.TimeTables;
//import go.pemkott.appsandroidmobiletebingtinggi.model.Updatep;
//import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiData;
//import go.pemkott.appsandroidmobiletebingtinggi.model.ValidasiModel;
//import go.pemkott.appsandroidmobiletebingtinggi.model.WaktuSift;
//import go.pemkott.appsandroidmobiletebingtinggi.singkronjadwal.TimeTebleSetting;
//import okhttp3.MultipartBody;
//import retrofit2.Call;
//import retrofit2.http.FormUrlEncoded;
//import retrofit2.http.Header;
//import retrofit2.http.Headers;
//import retrofit2.http.Multipart;
//import retrofit2.http.POST;
//import retrofit2.http.Part;
//import retrofit2.http.Url;
//
//public interface HttpService {
//
//    @Multipart
//    @POST("upload_file/RestApi/upload_api.php")
//    Call<FileModel> callUploadApi(@Part MultipartBody.Part image);
//
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<Logout>> getUrlLogout(@Url String url, @Header("Authorization")String token );
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<KegiatanIzin>> getUrlKegiatan(@Url String url, @Header("Authorization")String token );
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<KegiatanIzin>> getUrlKegiatanNew(@Url String url );
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<Employee> getUrlEmployee(@Url String url);
//
//    @Multipart
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<FileModel>> getUrlUploadKegiatan(@Url String url, @Part List<MultipartBody.Part> image, @Part List<MultipartBody.Part> lampiran, @Header("Authorization")String token );
//
//    @Multipart
//    @POST
//    Call<FileModel> callMultipleUploadApi(@Url String url, @Part List<MultipartBody.Part> image, @Part List<MultipartBody.Part> lampiran, @Header("Authorization")String token);
//
//    @Multipart
//    @FormUrlEncoded
//    @POST
//    Call<FileModel> callMultipleUploadApiBase(@Url String url);
//
////    @FormUrlEncoded
////    @POST("uploaditem")
////    Call<ResponsePOJO> uploadImage(
////            @Field("fileup") String encodedImage,
////            @Field("nama_p") String nama,
////            @Field("id_kategori") String kategori,
////            @Field("harga_p") String harga,
////            @Field("ket_p") String keterangan,
////            @Field("id_umkm") String id_umkm
////    );
//
//
//    @Multipart
//    @POST
//    Call<FileModel> callUpdateProfil(@Url String url, @Part List<MultipartBody.Part> image, @Header("Authorization")String token);
//
//    @Multipart
//    @POST
//    Call<FileModel> callMultipleUploadApiPerjalananDinas(@Url String url, @Part List<MultipartBody.Part> image, @Part List<MultipartBody.Part> lampiran, @Header("Authorization")String token);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<TimeTables>> getUrlTimeTable(@Url String url, @Header("Authorization")String token );
//
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<WaktuSift>> getTestSift(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<JadwalSift>> getJadwalSift(@Url String url);
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<ArrayList<JadwalSift>> getJadwalSifts(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<TimeTebleSetting>> getUrlTimeTableSetting(@Url String url, @Header("Authorization")String token );
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<Updatep>> getUrlUpdatep(@Url String url );
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<Koordinat>> getUrlKoordinat(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<InfoMP>> getUrlInfoMP(@Url String url);
//
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<Presensi>> getUrlPresensi(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<ValidasiData>> getUrlListValidasi(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<RekapServer>> getUrlRekapServer(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<RekapMasukFragment>> getRekapMasukFragment(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<RekapMasukKeduaFragment>> getRekapMasukKeduaFragment(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<RekapPulangFragment>> getRekapPulangFragment(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<RekapPulangKeduaFragment>> getRekapPulangKeduaFragment(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<ReturnValidasi>> getReturnValidasi(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<HasilValidasi>> getUrlHasilValidasi(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<ValidasiModel>> getUrlHasilValidasiMasukPertama(@Url String url);
//
//    @Headers({"Content-Type: application/json"})
//    @POST
//    Call<List<CheckUpdate>> getCheckUpdate(@Url String url);
//
//
////    @Multipart
////    @POST("absen")
////    Call<FileModel> callMultipleUploadApi(
////            @Part List<MultipartBody.Part> image,
////            @Part ("employee_id") RequestBody employee_id,
////            @Part ("tanggal") RequestBody tanggal,
////            @Part ("jam") RequestBody jam,
////            @Part ("posisi") RequestBody posisi,
////            @Part ("status") RequestBody status,
////            @Part ("lat") RequestBody lat,
////            @Part ("lng") RequestBody lng,
////            @Part ("ket") RequestBody ket,
////            @Part ("lampiran") RequestBody lampiran,
////            @Part ("valid") RequestBody valid
////
////    );
//
//}
