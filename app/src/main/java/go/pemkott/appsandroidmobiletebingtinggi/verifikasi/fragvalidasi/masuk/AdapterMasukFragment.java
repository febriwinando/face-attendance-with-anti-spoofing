package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.masuk;

import android.content.Context;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapMasukFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.BaseVerifikasiAdapter;

public class AdapterMasukFragment extends BaseVerifikasiAdapter<RekapMasukFragment> {

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback callback) {
        this.onItemClickCallback = callback;
    }

    public AdapterMasukFragment(List<RekapMasukFragment> rekapServers, Context context) {
        super(rekapServers);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_validasi_new;
    }

    @Override
    protected String extractId(RekapMasukFragment item) {
        return item.getId();
    }

    @Override
    protected String extractNama(RekapMasukFragment item) {
        return item.getNama();
    }

    @Override
    protected String extractTanggal(RekapMasukFragment item) {
        return item.getTanggal();
    }

    @Override
    protected String extractJam(RekapMasukFragment item) {
        return item.getJam_masuk();
    }

    @Override
    protected String extractPosisi(RekapMasukFragment item) {
        return item.getPosisi_masuk();
    }

    @Override
    protected String extractKeterangan(RekapMasukFragment item) {
        return item.getKet_masuk();
    }

    @Override
    protected int extractValidasi(RekapMasukFragment item) {
        return item.getValidasi_masuk();
    }

    @Override
    protected void dispatchItemClick(RekapMasukFragment item) {
        if (onItemClickCallback != null) {
            onItemClickCallback.onItemClicked(item);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapMasukFragment data);
    }
}
