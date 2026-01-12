package go.pemkott.appsandroidmobiletebingtinggi.viewmodel;

import android.content.Context;

import androidx.lifecycle.ViewModel;

import go.pemkott.appsandroidmobiletebingtinggi.geolocation.model.LocationHelper;


public class LocationViewModel extends ViewModel {

    public LocationHelper getLocationHelper(Context mContext) {
       return LocationHelper.getInstance(mContext);
    }
}
