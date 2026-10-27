
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
    @POST("login")
    Call<List<LoginResponse>> login(
            @Field("username") String username,
            @Field("password") String password

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
            @Field("pegawai[]") List<String> pegawai
    );

    @FormUrlEncoded
    @POST("v2/admin/insert-massal/preview")
    Call<ResponsePOJO> previewInsertMassal(
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
            @Field("keterlambatan") int keterlambatan,
            @Field("posisi_masuk") String posisi_masuk,
            @Field("status_masuk") String status_masuk,
            @Field("validasi_masuk") String validasi_masuk,
            @Field("ket_masuk") String ket_masuk,
            @Field("jam_pulang") String jam_pulang,
            @Field("posisi_pulang") String posisi_pulang,
            @Field("status_pulang") String status_pulang,
            @Field("validasi_pulang") String validasi_pulang
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
            @Part MultipartBody.Part fototaging_masuk,
            @Part MultipartBody.Part fototaging_pulang,
            @Part MultipartBody.Part lampiran_masuk,
            @Part MultipartBody.Part lampiran_pulang
    );

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
