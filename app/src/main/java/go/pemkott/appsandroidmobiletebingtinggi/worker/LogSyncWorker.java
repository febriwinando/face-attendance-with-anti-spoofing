package go.pemkott.appsandroidmobiletebingtinggi.worker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.api.ResponsePOJO;
import go.pemkott.appsandroidmobiletebingtinggi.model.UserActivityLog;
import go.pemkott.appsandroidmobiletebingtinggi.model.UserActivityLogRequest;
import retrofit2.Response;

public class LogSyncWorker extends Worker {
    private static final String TAG = "LogSyncWorker";

    public LogSyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    @SuppressLint("Range")
    public Result doWork() {
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        Cursor cursor = dbHelper.getUnsentLogs();

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) cursor.close();
            return Result.success();
        }

        List<UserActivityLog> logs = new ArrayList<>();
        List<Integer> logIds = new ArrayList<>();

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.LOG_ID));
            String employeeId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_EMPLOYEE_ID));
            String opdId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_OPD_ID));
            String tanggal = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_TANGGAL));
            String kegiatan = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_KEGIATAN));
            String jenisAbsen = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_JENIS));
            String deviceId = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_DEVICE_ID));
            String timestamp = cursor.getString(cursor.getColumnIndex(DatabaseHelper.LOG_TIMESTAMP));

            logs.add(new UserActivityLog(id, employeeId, opdId, tanggal, kegiatan, jenisAbsen, deviceId, timestamp));
            logIds.add(id);
        }
        cursor.close();

        if (logs.isEmpty()) {
            return Result.success();
        }

        UserActivityLogRequest request = new UserActivityLogRequest(logs);
        try {
            Response<ResponsePOJO> response = RetroClient.getInstance().getApi().sendActivityLogs(request).execute();
            if (response.isSuccessful() && response.body() != null && response.body().isStatus()) {
                dbHelper.markLogsAsSent(logIds);
                Log.d(TAG, "Logs synced successfully: " + logs.size());
                return Result.success();
            } else {
                String errorMsg = response.errorBody() != null ? response.errorBody().string() : response.message();
                Log.e(TAG, "Logs sync failed: " + errorMsg);
                return Result.retry();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error syncing logs", e);
            return Result.retry();
        }
    }
}
