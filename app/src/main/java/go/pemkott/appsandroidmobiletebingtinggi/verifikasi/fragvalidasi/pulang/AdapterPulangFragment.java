package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang;

import android.content.Context;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.BaseVerifikasiAdapter;

public class AdapterPulangFragment extends BaseVerifikasiAdapter<RekapPulangFragment> {

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback callback) {
        this.onItemClickCallback = callback;
    }

    public AdapterPulangFragment(List<RekapPulangFragment> rekapServers, Context context) {
        super(rekapServers);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_validasi_new;
    }

    @Override
    protected String extractId(RekapPulangFragment item) {
        return item.getId();
    }

    @Override
    protected String extractNama(RekapPulangFragment item) {
        return item.getNama();
    }

    @Override
    protected String extractTanggal(RekapPulangFragment item) {
        return item.getTanggal();
    }

    @Override
    protected String extractJam(RekapPulangFragment item) {
        return item.getJam_pulang();
    }

    @Override
    protected String extractPosisi(RekapPulangFragment item) {
        return item.getPosisi_pulang();
    }

    @Override
    protected String extractKeterangan(RekapPulangFragment item) {
        return item.getKet_pulang();
    }

    @Override
    protected int extractValidasi(RekapPulangFragment item) {
        return item.getValidasi_pulang();
    }

    @Override
    protected void dispatchItemClick(RekapPulangFragment item) {
        if (onItemClickCallback != null) {
            onItemClickCallback.onItemClicked(item);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapPulangFragment data);
    }
}
