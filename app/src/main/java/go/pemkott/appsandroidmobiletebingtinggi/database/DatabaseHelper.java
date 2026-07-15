package go.pemkott.appsandroidmobiletebingtinggi.database;

import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_ALAMAT;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_BATASWAKTU;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_EMAIL;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_ESELON;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_FOTO;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_ID;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_ID1;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_ID2;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_JABATAN;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_KELOMPOK;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_LAT;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_LNG;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_NAMA;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_NAMA_OPD;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_NIP;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_NO_HP;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_OPD_ID;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_PEGAWAI_STATUS_SIFT;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_POSISI_ID;
import static go.pemkott.appsandroidmobiletebingtinggi.database.ModelDataPagawai.E_S_JABATAN;
import static go.pemkott.appsandroidmobiletebingtinggi.konstanta.TimeFormat.SIMPLE_FORMAT_TANGGAL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.Date;

public class DatabaseHelper extends SQLiteOpenHelper {

    // =========================
    // DATABASE CONFIGURATION
    // =========================
    public static final String NAMA_DATABASE = "absensitt.db";
    private static final int DATABASE_VERSION = 101;

    public DatabaseHelper(Context context) {
        super(context, NAMA_DATABASE, null, DATABASE_VERSION);
    }

    // =========================
    // TABLE: DATA PENGGUNA
    // =========================
    public static final String TABLE_USER = "data_pengguna";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "EMPLOYEID";
    public static final String COL_3 = "USERNAME";
    public static final String COL_4 = "MARKS";
    public static final String COL_5 = "ACTIVE";
    public static final String COL_6 = "TOKEN";
    public static final String COL_7 = "VERIFIKATOR";

    // =========================
    // TABLE: TEMPORARY_PD
    // =========================
    public static final String TEMPORARY_PD = "temporary_pd";
    public static final String T_ID = "ID";
    public static final String T_KEGIATAN = "KEGIATAN";
    public static final String T_TGLMULAI = "TGLMULAI";
    public static final String T_TGLSAMPAI = "TGLSAMPAI";
    public static final String T_LAT = "LAT";
    public static final String T_LNG = "LNG";

    // =========================
    // TABLE: RESOURCE KEGIATAN
    // =========================
    public static final String RESOURCE_KEGIATAN = "kegiatan_izin";
    public static final String R_ID = "ID";
    public static final String R_TIPE = "TIPE";
    public static final String R_KET = "KET";

    // =========================
    // TABLE: TIMETABLE
    // =========================
    public static final String TIMETABLE = "timetables";
    public static final String TT_ID = "ID";
    public static final String TT_EMPLOYEE_ID = "EMPLOYEEID";
    public static final String TT_TIMETABLE_ID = "TIMETABELID";
    public static final String TT_INISIAL = "INISIAL";
    public static final String TT_HARI = "HARI";
    public static final String TT_MASUK = "MASUK";
    public static final String TT_PULANG = "PULANG";

    // =========================
    // TABLE: JAMSIFT
    // =========================
    public static final String JAMSIFT = "jamsift";
    public static final String JS_ID = "ID";
    public static final String JS_OPD_ID = "OPD_ID";
    public static final String JS_TIPE = "TIPE";
    public static final String JS_INISIAL = "INISIAL";
    public static final String JS_MASUK = "MASUK";
    public static final String JS_PULANG = "PULANG";

    // =========================
    // TABLE: JADWALSIFT
    // =========================
    public static final String JADWALSIFT = "jadwalsift";
    public static final String JW_ID = "ID";
    public static final String JW_EMPLOYEE_ID = "EMPLOYEEID";
    public static final String JW_SIFT = "SIFT_ID";
    public static final String JW_TANGGAL = "TANGGAL";

    // =========================
    // TABLE: PRESENCES
    // =========================
//    public static final String PRESENCES = "presences";
//    public static final String P_ID = "ID";
//    public static final String P_EMPLOYEE_ID = "EMPLOYEEID";
//    public static final String P_TANGGAL = "TANGGAL";
//    public static final String P_JAM_MASUK = "JAM_MASUK";
//    public static final String P_JAM_PULANG = "JAM_PULANG";
//    public static final String P_POSISI_MASUK = "POSISI_MASUK";
//    public static final String P_POSISI_PULANG = "POSISI_PULANG";
//    public static final String P_STATUS_MASUK = "STATUS_MASUK";
//    public static final String P_STATUS_PULANG = "STATUS_PULANG";
//    public static final String P_LAT_MASUK = "LAT_MASUK";
//    public static final String P_LAT_PULANG = "LAT_PULANG";
//    public static final String P_LNG_MASUK = "LNG_MASUK";
//    public static final String P_LNG_PULANG = "LNG_PULANG";
//    public static final String P_KET_MASUK = "KET_MASUK";
//    public static final String P_KET_PULANG = "KET_PULANG";
//    public static final String P_VALID_MASUK = "VALID_MASUK";
//    public static final String P_VALID_PULANG = "VALID_PULANG";

    // =========================
    // TABLE: KOORDINAT
    // =========================
    public static final String KOORDINAT = "koordinat";
    public static final String K_ID = "ID";
    public static final String K_OPD_ID = "OPDID";
    public static final String K_ALAMAT = "ALAMAT";
    public static final String K_LAT = "LAT";
    public static final String K_LNG = "LNG";

    // =========================
    // TABLE: KOORDINAT EMPLOYEE
    // =========================
    public static final String KOORDINAT_E = "koordinatemployee";
    public static final String KE_ID = "ID";
    public static final String KE_EMP_ID = "EMPLOYEID";
    public static final String KE_ALAMAT = "ALAMAT";
    public static final String KE_LAT = "LAT";
    public static final String KE_LNG = "LNG";

    // =========================
    // TABLE: INFO MASUK PULANG
    // =========================
    public static final String INFO_MP = "masukpulang";
    public static final String INFO_EMPLOYEE_ID = "ID";
    public static final String INFO_TANGGAL = "TANGGAL";
    public static final String INFO_JAM = "JAM";
    public static final String INFO_STATUS = "STATUS";

    // =========================
    // TABLE: INFO LOGIN USER
    // =========================
    public static final String HAPUS_DATA_PENGGUNA = "infologinuser";
    public static final String HAPUS_ID = "ID";
    public static final String HAPUS_INFO = "KODE";

    // =========================
    // TABLE: EMPLOYEE (TIDAK ADA DI KONSTANTA AWAL)
    // =========================
    public static final String EMPLOYEE = "employee";

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // =========================
            // TABLE: DATA PENGGUNA
            // =========================
            db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                    COL_1 + " TEXT, " +
                    COL_2 + " TEXT, " +
                    COL_3 + " TEXT, " +
                    COL_4 + " TEXT, " +
                    COL_5 + " TEXT, " +
                    COL_6 + " TEXT, " +
                    COL_7 + " TEXT)");

            // =========================
            // TABLE: KOORDINAT EMPLOYEE
            // =========================
            db.execSQL("CREATE TABLE " + KOORDINAT_E + " (" +
                    KE_ID + " TEXT, " +
                    KE_EMP_ID + " TEXT, " +
                    KE_ALAMAT + " TEXT, " +
                    KE_LAT + " TEXT, " +
                    KE_LNG + " TEXT)");

            // =========================
            // TABLE: KOORDINAT
            // =========================
            db.execSQL("CREATE TABLE " + KOORDINAT + " (" +
                    K_ID + " TEXT, " +
                    K_OPD_ID + " TEXT, " +
                    K_ALAMAT + " TEXT, " +
                    K_LAT + " TEXT, " +
                    K_LNG + " TEXT)");

            // =========================
            // TABLE: INFO LOGIN USER
            // =========================
            db.execSQL("CREATE TABLE " + HAPUS_DATA_PENGGUNA + " (" +
                    HAPUS_ID + " TEXT, " +
                    HAPUS_INFO + " TEXT)");

            // =========================
            // TABLE: INFO MASUK PULANG
            // =========================
            db.execSQL("CREATE TABLE " + INFO_MP + " (" +
                    INFO_EMPLOYEE_ID + " TEXT, " +
                    INFO_TANGGAL + " TEXT, " +
                    INFO_JAM + " TEXT, " +
                    INFO_STATUS + " TEXT)");

            // =========================
            // TABLE: TEMPORARY PD
            // =========================
            db.execSQL("CREATE TABLE " + TEMPORARY_PD + " (" +
                    T_ID + " TEXT, " +
                    T_KEGIATAN + " TEXT, " +
                    T_TGLMULAI + " TEXT, " +
                    T_TGLSAMPAI + " TEXT, " +
                    T_LAT + " TEXT, " +
                    T_LNG + " TEXT)");

            // =========================
            // TABLE: RESOURCE KEGIATAN
            // =========================
            db.execSQL("CREATE TABLE " + RESOURCE_KEGIATAN + " (" +
                    R_ID + " TEXT, " +
                    R_TIPE + " TEXT, " +
                    R_KET + " TEXT)");

            // =========================
            // TABLE: TIMETABLE
            // =========================
            db.execSQL("CREATE TABLE " + TIMETABLE + " (" +
                    TT_ID + " TEXT, " +
                    TT_EMPLOYEE_ID + " TEXT, " +
                    TT_TIMETABLE_ID + " TEXT, " +
                    TT_INISIAL + " TEXT, " +
                    TT_HARI + " TEXT, " +
                    TT_MASUK + " TEXT, " +
                    TT_PULANG + " TEXT)");

            // =========================
            // TABLE: JAMSIFT
            // =========================
            db.execSQL("CREATE TABLE " + JAMSIFT + " (" +
                    JS_ID + " TEXT, " +
                    JS_OPD_ID + " TEXT, " +
                    JS_TIPE + " TEXT, " +
                    JS_INISIAL + " TEXT, " +
                    JS_MASUK + " TEXT, " +
                    JS_PULANG + " TEXT)");

            // =========================
            // TABLE: JADWALSIFT
            // =========================
            db.execSQL("CREATE TABLE " + JADWALSIFT + " (" +
                    JW_ID + " TEXT, " +
                    JW_EMPLOYEE_ID + " TEXT, " +
                    JW_SIFT + " TEXT, " +
                    JW_TANGGAL + " TEXT)");

            // =========================
            // TABLE: PRESENCES
            // =========================
//            db.execSQL("CREATE TABLE " + PRESENCES + " (" +
//                    P_ID + " TEXT, " +
//                    P_EMPLOYEE_ID + " TEXT, " +
//                    P_TANGGAL + " TEXT, " +
//                    P_JAM_MASUK + " TEXT, " +
//                    P_JAM_PULANG + " TEXT, " +
//                    P_POSISI_MASUK + " TEXT, " +
//                    P_POSISI_PULANG + " TEXT, " +
//                    P_STATUS_MASUK + " TEXT, " +
//                    P_STATUS_PULANG + " TEXT, " +
//                    P_LAT_MASUK + " TEXT, " +
//                    P_LAT_PULANG + " TEXT, " +
//                    P_LNG_MASUK + " TEXT, " +
//                    P_LNG_PULANG + " TEXT, " +
//                    P_KET_MASUK + " TEXT, " +
//                    P_KET_PULANG + " TEXT, " +
//                    P_VALID_MASUK + " TEXT, " +
//                    P_VALID_PULANG + " TEXT)");

            // =========================
            // TABLE: EMPLOYEE
            // =========================
            db.execSQL("CREATE TABLE " + EMPLOYEE + " (" +
                    "ID TEXT, IDI TEXT, IDII TEXT, " +
                    "POSISIID TEXT, OPDID TEXT, " +
                    "NIP TEXT, NAMA TEXT, EMAIL TEXT, NOHP TEXT, " +
                    "KELOMPOK TEXT, SJABATAN TEXT, ESELON TEXT, JABATAN TEXT, " +
                    "NAMA_OPD TEXT, ALAMAT TEXT, LAT TEXT, LNG TEXT, FOTO TEXT, " +
                    "BATAS_WAKTU TEXT, PEGAWAI_SIFT TEXT)");

        } catch (Exception e) {
            db.beginTransaction();
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase absensi, int oldVersion, int newVersion) {
            if(newVersion > oldVersion){

                absensi.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
                absensi.execSQL("DROP TABLE IF EXISTS " + EMPLOYEE);
                absensi.execSQL("DROP TABLE IF EXISTS " + TEMPORARY_PD);
                absensi.execSQL("DROP TABLE IF EXISTS " + RESOURCE_KEGIATAN);
                absensi.execSQL("DROP TABLE IF EXISTS " + TIMETABLE);
//                absensi.execSQL("DROP TABLE IF EXISTS " + PRESENCES);
                absensi.execSQL("DROP TABLE IF EXISTS " + KOORDINAT);
                absensi.execSQL("DROP TABLE IF EXISTS " + INFO_MP);
                absensi.execSQL("DROP TABLE IF EXISTS " + KOORDINAT_E);
                absensi.execSQL("DROP TABLE IF EXISTS " + HAPUS_DATA_PENGGUNA);
                absensi.execSQL("DROP TABLE IF EXISTS " + JAMSIFT);
                absensi.execSQL("DROP TABLE IF EXISTS " + JADWALSIFT);

            }

            onCreate(absensi);
    }


    public boolean insertDataEmployee(String id, String atasan_id1, String atasan_id2, String position_id, String opd_id,
                                      String nip, String nama, String email, String no_hp, String kelompok, String s_jabatan,
                                      String eselon, String jabatan, String nama_opd, String alamat, String lat, String lng, String foto, String bataswaktu, String sift){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(E_ID, id);
        contentValues.put(E_ID1, atasan_id1);
        contentValues.put(E_ID2, atasan_id2);
        contentValues.put(E_POSISI_ID, position_id);
        contentValues.put(E_OPD_ID, opd_id);
        contentValues.put(E_NIP, nip);
        contentValues.put(E_NAMA, nama);
        contentValues.put(E_EMAIL, email);
        contentValues.put(E_NO_HP, no_hp);
        contentValues.put(E_KELOMPOK, kelompok);
        contentValues.put(E_S_JABATAN, s_jabatan);
        contentValues.put(E_ESELON, eselon);
        contentValues.put(E_JABATAN, jabatan);
        contentValues.put(E_NAMA_OPD, nama_opd);
        contentValues.put(E_ALAMAT, alamat);
        contentValues.put(E_LAT, lat);
        contentValues.put(E_LNG, lng);
        contentValues.put(E_FOTO, foto);
        contentValues.put(E_BATASWAKTU, bataswaktu);
        contentValues.put(E_PEGAWAI_STATUS_SIFT, sift);

        long result = db.insert(EMPLOYEE, null, contentValues);
        if (result == -1 ){
            return false;
        }
        else{
            return true;
        }
    }

    public Cursor getDataEmployee(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+EMPLOYEE+" where ID = '"+id+"'", null);
        return res;
    }


    public boolean insertDataUserLogin(String id, String name, String username, String marks, String active, String token, String verifikator){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, username);
        contentValues.put(COL_4, marks);
        contentValues.put(COL_5, active);
        contentValues.put(COL_6, token);
        contentValues.put(COL_7, verifikator);
        long result = db.insert(TABLE_USER, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;
    }

    public boolean insertDataKoordinat(String id, String id_opd, String alamat, String lat, String lng){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(K_ID, id);
        contentValues.put(K_OPD_ID, id_opd);
        contentValues.put(K_ALAMAT, alamat);
        contentValues.put(K_LAT, lat);
        contentValues.put(K_LNG, lng);

        long result = db.insert(KOORDINAT, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;
    }

    public boolean insertDataKoordinatEmployee(String id, String ide, String alamat, String lat, String lng){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(KE_ID, id);
        contentValues.put(KE_EMP_ID, ide);
        contentValues.put(KE_ALAMAT, alamat);
        contentValues.put(KE_LAT, lat);
        contentValues.put(KE_LNG, lng);

        long result = db.insert(KOORDINAT_E, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;
    }


    public boolean insertDataTimeTable(String id, String employee_id, String timetable_id, String inisial, String hari, String masuk, String pulang){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(TT_ID, id);
        contentValues.put(TT_EMPLOYEE_ID, employee_id);
        contentValues.put(TT_TIMETABLE_ID, timetable_id);
        contentValues.put(TT_INISIAL, inisial);
        contentValues.put(TT_HARI, hari);
        contentValues.put(TT_MASUK, masuk);
        contentValues.put(TT_PULANG, pulang);

        long result = db.insert(TIMETABLE, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;
    }

    public boolean insertJamSift(String id, String opd_id, String tipe, String inisial, String masuk, String pulang){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(JS_ID, id);
        contentValues.put(JS_OPD_ID, opd_id);
        contentValues.put(JS_TIPE, tipe);
        contentValues.put(JS_INISIAL, inisial);
        contentValues.put(JS_MASUK, masuk);
        contentValues.put(JS_PULANG, pulang);

        long result = db.insert(JAMSIFT, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;
    }

    public boolean insertJadwalSift(String id, String employee_id, String sift_id, String tanggal){

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(JW_ID, id);
        contentValues.put(JW_EMPLOYEE_ID, employee_id);
        contentValues.put(JW_SIFT, sift_id);
        contentValues.put(JW_TANGGAL, tanggal);

        long result = db.insert(JADWALSIFT, null, contentValues);
        if (result == -1 )
            return false;
        else
            return true;

    }



    public boolean insertResourceKegiatan(String id, String tipe, String ket){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(R_ID, id);
        contentValues.put(R_TIPE, tipe);
        contentValues.put(R_KET, ket);

        long result = db.insert(RESOURCE_KEGIATAN, null, contentValues);
        if (result == -1)
            return  false;
        else
            return true;

    }


    public Cursor getAllData22(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TABLE_USER+" WHERE ID = '"+id+"'", null);
        return res;
    }

    public Cursor getDataKoordinat(String idOPD){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+KOORDINAT+" where "+K_OPD_ID+" = '"+idOPD+"'", null);
        return res;
    }

    public Cursor getDataKoordinatEmp(String idE){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+KOORDINAT_E+" where EMPLOYEID = '"+idE+"'", null);

        return res;
    }

    public Cursor getKegiatanIzin(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+RESOURCE_KEGIATAN, null);
        return res;
    }

    public Cursor getKegiatanTimeTable(String idE, String hari){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TIMETABLE+" where EMPLOYEEID = '"+idE+"' AND HARI = '"+ hari +"'" , null);
        return res;
    }

    public Cursor getJamSift(String opd_id){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+JAMSIFT+" where OPD_ID = '"+opd_id+"'" , null);
        return res;
    }

    public Cursor getDataSift(String opd_id, String idSift){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+JAMSIFT+" where OPD_ID = '"+opd_id+"' AND ID = '"+idSift+"' " , null);
        return res;
    }



    public Cursor getJadwalSifts2(String ide, String bulansebelum, String bulan, String tahunsebelum, String tahun){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID, EMPLOYEEID, SIFT_ID, date(TANGGAL), strftime('%m', TANGGAL) AS BULAN, strftime('%Y', TANGGAL) AS TAHUN from "+JADWALSIFT+" where EMPLOYEEID = '"+ide+"' and (BULAN = '"+bulan+"' or BULAN = '"+bulansebelum+"') and (TAHUN = '"+tahun+"' or TAHUN = '"+tahunsebelum+"')", null);

        return res;
    }

    public Cursor getJadwalSiftsCalendar(String ide, String bulan, String tahun){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID, EMPLOYEEID, SIFT_ID, date(TANGGAL), strftime('%m', TANGGAL) AS BULAN, strftime('%Y', TANGGAL) AS TAHUN from "+JADWALSIFT+" where EMPLOYEEID = '"+ide+"' and BULAN = '"+bulan+"' and TAHUN = '"+tahun+"'", null);
        return res;
    }


    public Cursor getInfoJadwalSiftToday(String ide, String tglCheck){
        String hariini = SIMPLE_FORMAT_TANGGAL.format(new Date());


        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID, EMPLOYEEID, SIFT_ID, date(TANGGAL) from "+JADWALSIFT+" where EMPLOYEEID = '"+ide+"' and TANGGAL = '"+tglCheck+"'" , null);
        return res;
    }

    public Cursor getKegiatanTimeTableCheck(String idE){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from "+TIMETABLE+" where EMPLOYEEID = '"+idE+"'", null);
        return res;
    }


    public Integer deleteDataUseAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( TABLE_USER, null, null );
    }

    public Integer deleteTimeTable(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( TIMETABLE, null, null);
    }

    public Integer deleteJamSift(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( JAMSIFT, null, null);
    }
    public Integer deleteJadwalSift(String ide, String bulan, String tahun){

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select ID, date(TANGGAL), strftime('%m', TANGGAL) AS BULAN, strftime('%Y', TANGGAL) AS TAHUN from "+JADWALSIFT+" where EMPLOYEEID = '"+ide+"' and BULAN = '"+bulan+"' and TAHUN = '"+tahun+"'", null);
        while (res.moveToNext()){
            db.delete( JADWALSIFT, JW_TANGGAL+" = ?", new String[] {res.getString(1)});
        }
        return res.getCount();
    }

    public Integer deleteJadwalSift2(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( JADWALSIFT, null, null);
    }

    public Integer deleteTimeTableAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( TIMETABLE, null, null);
    }

    public Integer deleteAllDataUser(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( TABLE_USER, null, null );
    }

    public Integer deleteDataTableInfoClearUser(String id){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( HAPUS_DATA_PENGGUNA, HAPUS_INFO+" = ?", new String[] {id} );
    }


    public Integer deleteDataEmployeeAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( EMPLOYEE, null, null );
    }


    public Integer deleteDataKoordinatEmployee(String idE){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( KOORDINAT_E, "EMPLOYEID = ?", new String[] {idE} );

    }

    public Integer deleteDataKoordinatEmployeeAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( KOORDINAT_E, null, null);

    }

    public Integer deleteDataKoordinatOPD(String idOPD){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( KOORDINAT, "OPDID = ?", new String[] {idOPD} );
    }

    public Integer deleteDataKoordinatOPDAll(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( KOORDINAT, null, null );
    }

    public Integer deleteKegiatanIzin(){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete( RESOURCE_KEGIATAN, null, null );
    }

}
