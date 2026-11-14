package go.pemkott.appsandroidmobiletebingtinggi;

import org.junit.Test;

import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test pengujian aturan otorisasi dan kebijakan 2FA untuk user NIP 199101012010111001:
 * 1. NIP 199101012010111001 HANYA memiliki kelebihan bisa login tanpa 2FA.
 * 2. Untuk fitur Chat dan layanan lainnya, NIP 199101012010111001 TIDAK BOLEH memiliki role apa-apa,
 *    cukup seperti pegawai lainnya (tidak memiliki bypass chat atau hak istimewa).
 * 3. Yang memiliki akses penuh HANYA NIP 199402092020121005.
 */
public class User19910101RulesAndPermissionsTest {

    public static final String SPECIAL_LOGIN_NIP = "199101012010111001";
    public static final String FULL_ACCESS_SUPER_ADMIN_NIP = "199402092020121005";

    public static boolean is2faRequiredForUser(String usernameOrNip) {
        if (usernameOrNip == null) return true;
        String clean = usernameOrNip.trim();
        if (SPECIAL_LOGIN_NIP.equalsIgnoreCase(clean)) {
            return false; // HANYA kelebihannya bisa login tanpa 2FA
        }
        return true;
    }

    public static boolean hasFullAccessSuperAdmin(String nip) {
        // Yang punya akses penuh HANYA 199402092020121005
        return FULL_ACCESS_SUPER_ADMIN_NIP.equals(nip != null ? nip.trim() : null);
    }

    public static boolean canAccessChatService(String nip, boolean isGlobalChatEnabled) {
        if (nip == null) return false;
        String clean = nip.trim();
        // HANYA NIP 199402092020121005 yang punya akses penuh (dapat memantau dan membuka chat saat disabled)
        if (FULL_ACCESS_SUPER_ADMIN_NIP.equalsIgnoreCase(clean)) {
            return true;
        }
        // NIP 199101012010111001 tidak boleh memiliki role apa-apa, cukup seperti pegawai lainnya
        return isGlobalChatEnabled;
    }

    @Test
    public void test2faExemption_specialUserDoesNotRequire2fa() {
        assertFalse("User NIP 199101012010111001 tidak perlu menggunakan 2FA saat login",
                is2faRequiredForUser(SPECIAL_LOGIN_NIP));
        assertFalse("User NIP 199101012010111001 dengan spasi tetap bebas 2FA",
                is2faRequiredForUser(" 199101012010111001 "));
    }

    @Test
    public void test2faExemption_regularAndAdminUsersRequire2fa() {
        assertTrue("Pegawai umum wajib 2FA", is2faRequiredForUser("198307262015031001"));
        assertTrue("Super Admin wajib 2FA", is2faRequiredForUser(FULL_ACCESS_SUPER_ADMIN_NIP));
        assertTrue("User null wajib 2FA", is2faRequiredForUser(null));
    }

    @Test
    public void testFullAccess_only199402092020121005HasFullAccess() {
        assertTrue("Hanya NIP 199402092020121005 yang memiliki akses penuh",
                hasFullAccessSuperAdmin(FULL_ACCESS_SUPER_ADMIN_NIP));
        assertTrue("ChatLocalDatabase mengakui 199402092020121005 sebagai Super Admin",
                ChatLocalDatabase.isSuperAdminNip(FULL_ACCESS_SUPER_ADMIN_NIP));

        assertFalse("NIP 199101012010111001 TIDAK boleh memiliki akses penuh",
                hasFullAccessSuperAdmin(SPECIAL_LOGIN_NIP));
        assertFalse("NIP 199101012010111001 bukan Super Admin",
                ChatLocalDatabase.isSuperAdminNip(SPECIAL_LOGIN_NIP));
    }

    @Test
    public void testChatRole_user199101012010111001IsTreatedLikeAnyOtherPegawai() {
        // Ketika layanan chat global dinonaktifkan:
        boolean globalChatDisabled = false;

        // Pegawai biasa tidak dapat chat saat global disabled
        assertFalse("Pegawai biasa tidak dapat chat saat chat global disabled",
                canAccessChatService("198307262015031001", globalChatDisabled));

        // NIP 199101012010111001 TIDAK boleh memiliki role apa-apa, cukup seperti pegawai lainnya
        assertFalse("NIP 199101012010111001 tidak memiliki role khusus di chat, sama seperti pegawai lainnya",
                canAccessChatService(SPECIAL_LOGIN_NIP, globalChatDisabled));

        // HANYA 199402092020121005 yang memiliki akses penuh untuk bypass saat chat dinonaktifkan
        assertTrue("Hanya 199402092020121005 yang memiliki akses penuh ke chat",
                canAccessChatService(FULL_ACCESS_SUPER_ADMIN_NIP, globalChatDisabled));

        // Ketika layanan chat global diaktifkan:
        boolean globalChatEnabled = true;
        assertTrue("Saat chat aktif, NIP 199101012010111001 dapat chat normal seperti pegawai lainnya",
                canAccessChatService(SPECIAL_LOGIN_NIP, globalChatEnabled));
        assertTrue("Saat chat aktif, pegawai biasa dapat chat",
                canAccessChatService("198307262015031001", globalChatEnabled));
    }
}
