package go.pemkott.appsandroidmobiletebingtinggi.model;

import java.util.ArrayList;

public class ContohDataJadwalSift {


        private static String[] tanggal = {
            "2022-12-01",
            "2022-12-02",
            "2022-12-0",
            "2022-12-04",
            "2022-12-06"

        };

        private static String[] inisial = {
            "PAGI 1",
            "SIANG 1",
            "MALAM 1",
            "PAGI 1",
            "SIANG 1",
            "MALAM 1"
        };

        private static String[] employee_id = {
            "15",
            "15",
            "15",
            "15",
            "15"
        };

        public static ArrayList<JadwalSift> getListData() {
                ArrayList<JadwalSift> list = new ArrayList<>();
                for (int position = 0; position < tanggal.length; position++) {
                        JadwalSift jadwalSift = new JadwalSift();
                        jadwalSift.setTanggal(tanggal[position]);
                        jadwalSift.setShift_id(inisial[position]);
                        jadwalSift.setEmployee_id(employee_id[position]);
                        list.add(jadwalSift);
                }
                return list;
        }
}
