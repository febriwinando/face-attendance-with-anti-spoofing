package go.pemkott.appsandroidmobiletebingtinggi;

import com.google.gson.Gson;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import go.pemkott.appsandroidmobiletebingtinggi.api.ServerConfig;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatAuditLogItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMonitoringMessage;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.OpdGroupProfileResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Memastikan seluruh URL API, media, avatar, dokumen PDF, dan lampiran pada modul
 * Login Akun, Download Data Absensi, Absensi, Helpdesk, Verifikasi/Validasi, Administrator,
 * serta Chating mengarah ke https://absensi.tebingtinggikota.go.id/, KECUALI portal berita Diskominfo.
 */
public class ServerConfigUrlNormalizationTest {

    private static final String TARGET_BASE = "https://absensi.tebingtinggikota.go.id/";

    @Before
    public void setUp() {
        ServerConfig.setCustomBaseHost(ServerConfig.DEFAULT_HOST);
    }

    @After
    public void tearDown() {
        ServerConfig.setCustomBaseHost(ServerConfig.DEFAULT_HOST);
    }

    @Test
    public void serverConfigEndpoints_allUseTargetAbsensiDomain() {
        assertEquals(TARGET_BASE, ServerConfig.DEFAULT_HOST);
        assertEquals(TARGET_BASE, ServerConfig.getBaseUrl());
        assertEquals(TARGET_BASE + "api/v22/", ServerConfig.getApiUrl());
        assertEquals(TARGET_BASE + "api/v22/", ServerConfig.getApiV2Url());
        assertEquals(TARGET_BASE + "storage/", ServerConfig.getStorageUrl());
        assertEquals(TARGET_BASE + "uploads-img-absensi/", ServerConfig.getUploadsAbsensiUrl());
        assertEquals(TARGET_BASE + "uploads-img-lampiran/", ServerConfig.getUploadsLampiranUrl());
    }

    @Test
    public void normalizeServerUrl_rewritesLegacyAndLocalHostsWithoutBreakingExternalOrLocalUris() {
        assertEquals(
                TARGET_BASE + "storage/chat/dokumen.pdf",
                ServerConfig.normalizeServerUrl("http://127.0.0.1:8000/storage/chat/dokumen.pdf", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/helpdesk/bukti.jpg",
                ServerConfig.normalizeServerUrl("http://localhost:8000/storage/helpdesk/bukti.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-absensi/foto.jpg",
                ServerConfig.normalizeServerUrl("http://172.15.1.35:8000/uploads-img-absensi/foto.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-lampiran/surat.pdf",
                ServerConfig.normalizeServerUrl("https://absensi.tebingtinggikota.go.id/uploads-img-lampiran/surat.pdf", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/chat/video.mp4",
                ServerConfig.normalizeServerUrl("https://svr-absensi.tebingtinggikota.go.id:8443/storage/chat/video.mp4", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/helpdesk/lampiran.png",
                ServerConfig.normalizeServerUrl("/storage/helpdesk/lampiran.png", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-absensi/foto2.jpg",
                ServerConfig.normalizeServerUrl("uploads-img-absensi/foto2.jpg", TARGET_BASE)
        );

        // External portal berita & URI lokal Android tidak boleh berubah
        assertEquals(
                "https://diskominfo.tebingtinggikota.go.id/uploads/news.jpg",
                ServerConfig.normalizeServerUrl("https://diskominfo.tebingtinggikota.go.id/uploads/news.jpg", TARGET_BASE)
        );
        assertEquals(
                "https://diskominfo.tebingtinggikota.go.id/api/berita",
                ServerConfig.normalizeServerUrl("https://diskominfo.tebingtinggikota.go.id/api/berita", TARGET_BASE)
        );
        assertEquals(
                "https://diskominfo.tebingtinggikota.go.id/uploads/news.jpg",
                ServerConfig.resolveStorageUrl("https://diskominfo.tebingtinggikota.go.id/uploads/news.jpg", TARGET_BASE)
        );
        assertEquals(
                "content://media/external/images/media/42",
                ServerConfig.normalizeServerUrl("content://media/external/images/media/42", TARGET_BASE)
        );
        assertEquals(
                "file:///storage/emulated/0/Download/a.pdf",
                ServerConfig.normalizeServerUrl("file:///storage/emulated/0/Download/a.pdf", TARGET_BASE)
        );
    }

    @Test
    public void moduleResolvers_handleBareFilenamesPrefixedPathsAndFullUrls() {
        // Helpdesk & Administrator (/storage/...)
        assertEquals(
                TARGET_BASE + "storage/aduan/1.jpg",
                ServerConfig.resolveStorageUrl("aduan/1.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/aduan/1.jpg",
                ServerConfig.resolveStorageUrl("/storage/aduan/1.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/foto-pegawai/nip.jpg",
                ServerConfig.resolveStorageUrl("http://127.0.0.1:8000/storage/foto-pegawai/nip.jpg", TARGET_BASE)
        );

        // Profil & Avatar Chat (/storage/foto-pegawai/...)
        assertEquals(
                TARGET_BASE + "storage/foto-pegawai/123.jpg",
                ServerConfig.resolveFotoPegawaiUrl("123.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/foto-pegawai/123.jpg",
                ServerConfig.resolveFotoPegawaiUrl("foto-pegawai/123.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "storage/foto-pegawai/123.jpg",
                ServerConfig.resolveFotoPegawaiUrl("https://absensi.tebingtinggikota.go.id/storage/foto-pegawai/123.jpg", TARGET_BASE)
        );

        // Absensi & Verifikasi/Validasi (/uploads-img-absensi/... & /uploads-img-lampiran/...)
        assertEquals(
                TARGET_BASE + "uploads-img-absensi/selfie.jpg",
                ServerConfig.resolveUploadsAbsensiUrl("selfie.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-absensi/selfie.jpg",
                ServerConfig.resolveUploadsAbsensiUrl("uploads-img-absensi/selfie.jpg", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-lampiran/spt.pdf",
                ServerConfig.resolveUploadsLampiranUrl("spt.pdf", TARGET_BASE)
        );
        assertEquals(
                TARGET_BASE + "uploads-img-lampiran/spt.pdf",
                ServerConfig.resolveUploadsLampiranUrl("http://localhost:8000/uploads-img-lampiran/spt.pdf", TARGET_BASE)
        );
    }

    @Test
    public void chatModels_normalizeFileAndAvatarUrlsToActiveServer() {
        ChatMessageItem msg = new ChatMessageItem();
        msg.setFileUrl("http://127.0.0.1:8000/storage/chat/audio.m4a");
        assertEquals(TARGET_BASE + "storage/chat/audio.m4a", msg.getFileUrl());

        ChatContact contact = new ChatContact();
        contact.setAvatarUrl("https://absensi.tebingtinggikota.go.id/storage/foto-pegawai/pegawai1.jpg");
        assertEquals(TARGET_BASE + "storage/foto-pegawai/pegawai1.jpg", contact.getAvatarUrl());
        contact.setAvatarUrl(null);
        assertNull(contact.getAvatarUrl());

        OpdGroupProfileResponse.OpdMemberItem member = new OpdGroupProfileResponse.OpdMemberItem();
        member.setAvatarUrl("foto-pegawai/anggota.jpg");
        assertEquals(TARGET_BASE + "storage/foto-pegawai/anggota.jpg", member.getAvatarUrl());

        ChatPrivacyResponse.BlockedEmployeeItem blocked = new ChatPrivacyResponse.BlockedEmployeeItem();
        blocked.setAvatarUrl("http://localhost:8000/storage/foto-pegawai/blokir.jpg");
        assertEquals(TARGET_BASE + "storage/foto-pegawai/blokir.jpg", blocked.getAvatarUrl());

        Gson gson = new Gson();
        ChatMonitoringMessage monMsg = gson.fromJson(
                "{\"file_url\":\"http://172.15.1.35:8000/storage/chat/lampiran.pdf\"}",
                ChatMonitoringMessage.class
        );
        assertEquals(TARGET_BASE + "storage/chat/lampiran.pdf", monMsg.getFileUrl());

        ChatAuditLogItem audit = gson.fromJson(
                "{\"file_url_snapshot\":\"https://svr-absensi.tebingtinggikota.go.id/storage/chat/snap.png\"}",
                ChatAuditLogItem.class
        );
        assertEquals(TARGET_BASE + "storage/chat/snap.png", audit.getFileUrlSnapshot());
    }

    @Test
    public void setCustomBaseHost_prependsSchemeWhenMissing() {
        ServerConfig.setCustomBaseHost("absensi.tebingtinggikota.go.id");
        assertEquals(TARGET_BASE, ServerConfig.getBaseUrl());
        assertEquals(TARGET_BASE + "api/v22/", ServerConfig.getApiUrl());
    }
}

