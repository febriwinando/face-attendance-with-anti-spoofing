package go.pemkott.appsandroidmobiletebingtinggi.rekap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class RekapViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public RekapViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is home rekap");
    }

    public LiveData<String> getText() {
        return mText;
    }
}