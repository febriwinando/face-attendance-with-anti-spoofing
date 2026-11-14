package go.pemkott.appsandroidmobiletebingtinggi.verifikasi.fragvalidasi.pulang;

import android.content.Context;

import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.R;
import go.pemkott.appsandroidmobiletebingtinggi.model.RekapPulangKeduaFragment;
import go.pemkott.appsandroidmobiletebingtinggi.verifikasi.BaseVerifikasiAdapter;

public class AdapterPulangKeduaFragment extends BaseVerifikasiAdapter<RekapPulangKeduaFragment> {

    private OnItemClickCallback onItemClickCallback;

    public void setOnItemClickCallback(OnItemClickCallback callback) {
        this.onItemClickCallback = callback;
    }

    public AdapterPulangKeduaFragment(List<RekapPulangKeduaFragment> rekapServers, Context context) {
        super(rekapServers);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_validasi_new_kedua;
    }

    @Override
    protected String extractId(RekapPulangKeduaFragment item) {
        return item.getId();
    }

    @Override
    protected String extractNama(RekapPulangKeduaFragment item) {
        return item.getNama();
    }

    @Override
    protected String extractTanggal(RekapPulangKeduaFragment item) {
        return item.getTanggal();
    }

    @Override
    protected String extractJam(RekapPulangKeduaFragment item) {
        return item.getJam_pulang();
    }

    @Override
    protected String extractPosisi(RekapPulangKeduaFragment item) {
        return item.getPosisi_pulang();
    }

    @Override
    protected String extractKeterangan(RekapPulangKeduaFragment item) {
        return item.getKet_pulang();
    }

    @Override
    protected int extractValidasi(RekapPulangKeduaFragment item) {
        return item.getValidasi_pulang();
    }

    @Override
    protected void dispatchItemClick(RekapPulangKeduaFragment item) {
        if (onItemClickCallback != null) {
            onItemClickCallback.onItemClicked(item);
        }
    }

    public interface OnItemClickCallback {
        void onItemClicked(RekapPulangKeduaFragment data);
    }
}
