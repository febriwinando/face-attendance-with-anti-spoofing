package go.pemkott.appsandroidmobiletebingtinggi.konstanta;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TimeFormat {
    public final static Locale localeID = new Locale("in", "ID");

    public static SimpleDateFormat SIMPLE_DATE_FORMAT_TAGING = new SimpleDateFormat("yyyyMMddHHmmss", localeID);
    public static SimpleDateFormat SIMPLE_DATE_FORMAT_TAGING_PHOTO_REPORT = new SimpleDateFormat("yyyyMMddHHmmss", localeID);
    public static SimpleDateFormat SIMPLE_FORMAT_JAM_TAGING = new SimpleDateFormat("HH:mm", localeID);
    public static SimpleDateFormat SIMPLE_FORMAT_JAM_MASUK_PULANG = new SimpleDateFormat("H", localeID);
    public static SimpleDateFormat SIMPLE_FORMAT_MENIT_MASUK_PULANG = new SimpleDateFormat("m", localeID);
    //    Format Tanggal
    public static SimpleDateFormat SIMPLE_FORMAT_TANGGAL = new SimpleDateFormat("yyyy-MM-dd", localeID);
    public static SimpleDateFormat SIMPLE_FORMAT_JAM = new SimpleDateFormat("HH:mm", localeID);

    public static SimpleDateFormat TANGGAL = new SimpleDateFormat("d", localeID);
    public static SimpleDateFormat TANGGALSIFT = new SimpleDateFormat("d", localeID);
    public static SimpleDateFormat BULAN = new SimpleDateFormat("MM", localeID);
    public static SimpleDateFormat TAHUN = new SimpleDateFormat("yyyy", localeID);
    public static SimpleDateFormat HARI_TEXT = new SimpleDateFormat("EEE", localeID);

        public static String bulan(String bulan){
            String  hasil = "";
            if (bulan.equals("01") || bulan.equals("1")){
                hasil = "Januari";
            }else if (bulan.equals("02") || bulan.equals("2")){
                hasil = "Februari";
            }else if (bulan.equals("03") || bulan.equals("3")){
                hasil = "Maret";
            }else if (bulan.equals("04") || bulan.equals("4")){
                hasil = "April";
            }else if (bulan.equals("05") || bulan.equals("5")){
                hasil = "Mei";
            }else if (bulan.equals("06") || bulan.equals("6")){
                hasil = "Juni";
            }else if (bulan.equals("07") || bulan.equals("7")){
                hasil = "Juli";
            }else if (bulan.equals("08") || bulan.equals("8")){
                hasil = "Agustus";
            }else if (bulan.equals("09") || bulan.equals("9")){
                hasil = "September";
            }else if (bulan.equals("10") || bulan.equals("10")){
                hasil = "Oktober";
            }else if (bulan.equals("11") || bulan.equals("11")){
                hasil = "November";
            }else if (bulan.equals("12") || bulan.equals("12")){
                hasil = "Desember";
            }
            return hasil;
        }





        public static int hari(String hari){
            if (hari.equals("Sen") ||  hari.equals("Mon")){
                return 1;
            }else if (hari.equals("Sel") ||  hari.equals("Tue")){
                return 2;
            }else if (hari.equals("Rab") ||  hari.equals("Wed")){
                return 3;
            }else if (hari.equals("Kam") ||  hari.equals("Thu")){
                return 4;
            }else if (hari.equals("Jum") ||  hari.equals("Fri")){
                return 5;
            }else if (hari.equals("Sab") ||  hari.equals("Sat")){
                return 6;
            }else{
                return 7;
            }
        }

        public static String hariText(String hari){
            if (hari.equals("Sen") ||  hari.equals("Mon")){
                return "Senin";
            }else if (hari.equals("Sel") ||  hari.equals("Tue")){
                return "Selasa";
            }else if (hari.equals("Rab") ||  hari.equals("Wed")){
                return "Rabu";
            }else if (hari.equals("Kam") ||  hari.equals("Thu")){
                return "Kamis";
            }else if (hari.equals("Jum") ||  hari.equals("Fri")){
                return "Jumat";
            }else if (hari.equals("Sab") ||  hari.equals("Sat")){
                return "Sabtu";
            }else{
                return "Minggu";
            }
        }

        public static String texthari(int hari){
            if (hari == 1){
                return "Senin";
            }else if (hari == 2){
                return "Selasa";
            }else if (hari == 3){
                return "Rabu";
            }else if (hari == 4){
                return "Kamis";
            }else if (hari == 5){
                return "Jumat";
            }else if (hari == 6){
                return "Sabtu";
            }else{
                return "Minggu";
            }
        }

        public void printDifference(Date startDate, Date endDate) {
            //milliseconds
            long different = endDate.getTime() - startDate.getTime();

            System.out.println("startDate : " + startDate);
            System.out.println("endDate : "+ endDate);
            System.out.println("different : " + different);

            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long hoursInMilli = minutesInMilli * 60;
            long daysInMilli = hoursInMilli * 24;

            long elapsedDays = different / daysInMilli;
            different = different % daysInMilli;

            long elapsedHours = different / hoursInMilli;
            different = different % hoursInMilli;

            long elapsedMinutes = different / minutesInMilli;
            different = different % minutesInMilli;

            long elapsedSeconds = different / secondsInMilli;

            System.out.printf(
                    "%d days, %d hours, %d minutes, %d seconds%n",
                    elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);
        }


    public static String makeDateString(int tahun, int bulan){
        return formatBulan(bulan)+" "+tahun;
    }

    public static String formatBulan(int bulan){
        if (bulan == 1 ){
            return "JAN";
        }else if (bulan == 2){
            return "FEB";
        }else if (bulan == 3){
            return "MAR";
        }else if (bulan == 4){
            return "APR";
        }else if (bulan == 5){
            return "MEI";
        }else if (bulan == 6){
            return "JUN";
        }else if (bulan == 7){
            return "JUL";
        }else if (bulan == 8){
            return "AGU";
        }else if (bulan == 9){
            return "SEP";
        }else if (bulan == 10){
            return "OKT";
        }else if (bulan == 11){
            return "NOV";
        }else {
            return "DES";
        }
    }

    public static String ambilbulanjadwal(String bulan){
        String sebelum = null;
        if (bulan.equals("01")){
            sebelum =  "12";
        }
        else if(bulan.equals("02")){
            sebelum = "01";
        }        else if(bulan.equals("03")){
            sebelum = "02";
        }        else if(bulan.equals("04")){
            sebelum = "03";
        }        else if(bulan.equals("05")){
            sebelum = "04";
        }        else if(bulan.equals("06")){
            sebelum = "05";
        }        else if(bulan.equals("07")){
            sebelum = "06";
        }        else if(bulan.equals("08")){
            sebelum = "07";
        }        else if(bulan.equals("09")){
            sebelum = "08";
        }else if(bulan.equals("10")){
            sebelum = "09";
        }else if(bulan.equals("11")){
            sebelum = "10";
        }else if(bulan.equals("12")){
            sebelum = "11";
        }
        return sebelum;
    }

    }


