package go.pemkott.appsandroidmobiletebingtinggi;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import go.pemkott.appsandroidmobiletebingtinggi.api.RetroClient;
import go.pemkott.appsandroidmobiletebingtinggi.database.DatabaseHelper;
import go.pemkott.appsandroidmobiletebingtinggi.model.Employee;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeeResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.EmployeesData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EmployeeSyncActivity extends AppCompatActivity {
    DatabaseHelper db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_employee_sync);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);

        syncEmployee();
    }

    private void syncEmployee(){

        RetroClient.getInstance()
                .getApi2()
                .getEmployees()
                .enqueue(new Callback<EmployeeResponse>() {

                    @Override
                    public void onResponse(Call<EmployeeResponse> call,
                                           Response<EmployeeResponse> response) {
                        if(response.isSuccessful()
                                && response.body()!=null
                                && response.body().isSuccess()){
                            db.deleteAllEmployees();
                            SQLiteDatabase database =
                                    db.getWritableDatabase();
                            database.beginTransaction();

                            try{
                                for(EmployeesData e : response.body().getData()){
                                    db.insertEmployee(e);
                                }

                                database.setTransactionSuccessful();

                            }finally{
                                database.endTransaction();
                            }

                            Toast.makeText(

                                    EmployeeSyncActivity.this,
                                    "Sinkron berhasil : "

                                            + response.body().getTotal()

                                            + " pegawai",

                                    Toast.LENGTH_LONG).show();

                        }

                    }

                    @Override

                    public void onFailure(Call<EmployeeResponse> call,
                                          Throwable t) {
                        Toast.makeText(
                                EmployeeSyncActivity.this,
                                t.getMessage(),
                                Toast.LENGTH_LONG).show();

                    }

                });

    }
}