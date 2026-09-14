package go.pemkott.appsandroidmobiletebingtinggi.utils;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import go.pemkott.appsandroidmobiletebingtinggi.R;

public class WeatherUtil {

    private static final Locale localeID = new Locale("in", "ID");

    public static void fetchWeather(Context context, double lat, double lng, TextView tvTemp, TextView tvCond, ImageView ivIcon) {
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + lat + "&longitude=" + lng + "&current_weather=true";

        RequestQueue queue = Volley.newRequestQueue(context);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONObject current = response.getJSONObject("current_weather");
                        double temp = current.getDouble("temperature");
                        int code = current.getInt("weathercode");

                        if (tvTemp != null) tvTemp.setText(String.format(localeID, "%.0f°C", temp));
                        if (tvCond != null) tvCond.setText(getWeatherDesc(code));
                        if (ivIcon != null) updateWeatherIcon(ivIcon, code);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, Throwable::printStackTrace);
        queue.add(request);
    }

    public static String getWeatherDesc(int code) {
        if (code == 0) return "Cerah";
        if (code <= 3) return "Berawan";
        if (code <= 48) return "Kabut";
        if (code <= 55) return "Gerimis";
        if (code <= 65) return "Hujan";
        if (code <= 77) return "Salju";
        if (code <= 82) return "Hujan Deras";
        if (code <= 86) return "Salju Lebat";
        if (code <= 99) return "Badai Petir";
        return "Cerah Berawan";
    }

    public static void updateWeatherIcon(ImageView ivIcon, int code) {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        boolean isNight = hour < 6 || hour > 18;

        if (code == 0) {
            ivIcon.setImageResource(isNight ? R.drawable.ic_night_w : R.drawable.ic_sun_w);
        } else if (code <= 3) {
            ivIcon.setImageResource(isNight ? R.drawable.ic_night_w : R.drawable.ic_sun_w);
        } else {
            ivIcon.setImageResource(R.drawable.ic_morning_w);
        }
    }
}
