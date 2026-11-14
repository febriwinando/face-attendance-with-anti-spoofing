package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk;

import android.content.Context;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukKeduaFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.BaseVerifikasiAdapter;

public class AdapterMasukKeduaFragment extends BaseVerifikasiAdapter<RekapMasukKeduaFragment> {

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback callback) {
        this.onItemClickCallback = callback;
    }

    public AdapterMasukKeduaFragment(List<RekapMasukKeduaFragment> rekapServers, Context context) {
        super(rekapServers);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_validasi_new_kedua;
    }

    @Override
    protected String extractId(RekapMasukKeduaFragment item) {
        return item.getId();
    }

    @Override
    protected String extractNama(RekapMasukKeduaFragment item) {
        return item.getNama();
    }

    @Override
    protected String extractTanggal(RekapMasukKeduaFragment item) {
        return item.getTanggal();
    }

    @Override
    protected String extractJam(RekapMasukKeduaFragment item) {
        return item.getJam_masuk();
    }

    @Override
    protected String extractPosisi(RekapMasukKeduaFragment item) {
        return item.getPosisi_masuk();
    }

    @Override
    protected String extractKeterangan(RekapMasukKeduaFragment item) {
        return item.getKet_masuk();
    }

    @Override
    protected int extractValidasi(RekapMasukKeduaFragment item) {
        return item.getValidasi_masuk();
    }

    @Override
    protected void dispatchItemClick(RekapMasukKeduaFragment item) {
        if (onItemClickCallback != null) {
            onItemClickCallback.onItemClicked(item);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapMasukKeduaFragment data);
    }
}
