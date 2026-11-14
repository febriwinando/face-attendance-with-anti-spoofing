package go.pemkott.appsandroidmobiletebingtinggi;

import com.google.gson.Gson;

import org.junit.Test;

import go.pemkott.appsandroidmobiletebingtinggi.database.ChatLocalDatabase;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.SendMessageResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit test JUnit 4 untuk aturan bisnis penghapusan pesan SAHUT dan otorisasi NIP Monitoring.
 */
public class ChatDeletionRulesTest {

    @Test
    public void superAdminNip_onlyMatchesExact18Digits() {
        assertEquals("199402092020121005", ChatLocalDatabase.MONITORING_ADMIN_NIP);
        assertTrue(ChatLocalDatabase.isSuperAdminNip("199402092020121005"));
        assertTrue(ChatLocalDatabase.isSuperAdminNip("  199402092020121005  "));

        assertFalse(ChatLocalDatabase.isSuperAdminNip("19940209202012"));
        assertFalse(ChatLocalDatabase.isSuperAdminNip("199402092020129999"));
        assertFalse(ChatLocalDatabase.isSuperAdminNip("1994020920201210050"));
        assertFalse(ChatLocalDatabase.isSuperAdminNip(""));
        assertFalse(ChatLocalDatabase.isSuperAdminNip("   "));
        assertFalse(ChatLocalDatabase.isSuperAdminNip(null));
    }

    @Test
    public void canDeleteForEveryone_requiresSenderAndUnread() {
        ChatMessageItem msg = new ChatMessageItem();
        msg.setId(101L);
        msg.setSenderId(2L);
        msg.setReceiverId(3L);
        msg.setChatType("personal");
        msg.setMessage("Halo");
        msg.setMe(true);
        msg.setRead(false);
        msg.setReadAt(null);
        msg.setIsDeleted(0);
        msg.setIsDeletedGlobal(0);
        msg.setDeletedForEveryone(0);

        assertTrue(msg.canDeleteForEveryone());

        // Whitespace-only readAt still counts as unread
        msg.setReadAt("   ");
        assertTrue(msg.canDeleteForEveryone());

        // Receiver cannot delete for everyone
        msg.setReadAt(null);
        msg.setMe(false);
        assertFalse(msg.canDeleteForEveryone());

        // Sender cannot delete for everyone once read flag is true
        msg.setMe(true);
        msg.setRead(true);
        assertFalse(msg.canDeleteForEveryone());

        // Sender cannot delete for everyone if readAt timestamp is populated
        msg.setRead(false);
        msg.setReadAt("2026-11-12 09:05:00");
        assertFalse(msg.canDeleteForEveryone());

        // Already deleted message cannot be deleted for everyone again
        msg.setReadAt(null);
        msg.setIsDeleted(1);
        assertFalse(msg.canDeleteForEveryone());

        msg.setIsDeleted(0);
        msg.setDeletedForEveryone(1);
        assertFalse(msg.canDeleteForEveryone());
    }

    @Test
    public void isDeletedForUser_respectsCommaDelimitedIdsAndSideIsolation() {
        ChatMessageItem msg = new ChatMessageItem();
        msg.setChatType("opd");
        msg.setMe(false);
        msg.setDeletedForUsers(",12,34,");

        // Exact token match: user 2 must NOT match substring of "12"
        assertFalse(msg.isDeletedForUser("2"));
        assertFalse(msg.isDeletedForUser("3"));
        assertTrue(msg.isDeletedForUser("12"));
        assertTrue(msg.isDeletedForUser("34"));

        // Personal chat: sender soft delete affects sender only, not receiver
        ChatMessageItem personal = new ChatMessageItem();
        personal.setChatType("personal");
        personal.setDeletedForSender(1);
        personal.setDeletedForReceiver(0);
        personal.setDeletedForEveryone(0);

        personal.setMe(true);
        assertTrue(personal.isDeletedForUser("2"));

        personal.setMe(false);
        assertFalse(personal.isDeletedForUser("3"));
        assertFalse(personal.isDeletedForEveryone());

        // Personal receiver soft delete hides for receiver only
        personal.setDeletedForSender(0);
        personal.setDeletedForReceiver(1);
        personal.setMe(false);
        assertTrue(personal.isDeletedForUser("3"));
        personal.setMe(true);
        assertFalse(personal.isDeletedForUser("2"));

        // Deleted for everyone hides for both sides
        personal.setDeletedForReceiver(0);
        personal.setDeletedForEveryone(1);
        assertTrue(personal.isDeletedForEveryone());
        personal.setMe(true);
        assertTrue(personal.isDeletedForUser("2"));
        personal.setMe(false);
        assertTrue(personal.isDeletedForUser("3"));
    }

    @Test
    public void buildPersonalPairClause_isolatesPairWhenMyIdPositive() {
        String pairClause = ChatLocalDatabase.buildPersonalPairClause(2L, 3L);
        assertEquals("((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?))", pairClause);

        String fallbackClause = ChatLocalDatabase.buildPersonalPairClause(0L, 3L);
        assertEquals("(sender_id = ? OR receiver_id = ?)", fallbackClause);
    }

    @Test
    public void sendMessageResponse_parsesBothBooleanAndIntegerDeletionFlags() {
        Gson gson = new Gson();

        String booleanPayload = "{"
                + "\"status\":true,"
                + "\"success\":true,"
                + "\"message\":\"Pesan berhasil dihapus untuk pengirim dan penerima.\","
                + "\"data\":{"
                + "\"id\":21,"
                + "\"sender_id\":11,"
                + "\"receiver_id\":22,"
                + "\"chat_type\":\"personal\","
                + "\"message\":\"Tes\","
                + "\"is_me\":true,"
                + "\"is_read\":false,"
                + "\"deleted_for_sender\":true,"
                + "\"deleted_for_receiver\":false,"
                + "\"deleted_for_everyone\":true,"
                + "\"deleted_for_users\":\",11,\","
                + "\"is_deleted\":true,"
                + "\"is_deleted_global\":true"
                + "}"
                + "}";

        SendMessageResponse fromBooleans = gson.fromJson(booleanPayload, SendMessageResponse.class);
        assertTrue(fromBooleans.isStatus());
        assertEquals(21L, fromBooleans.getData().getId());
        assertEquals(1, fromBooleans.getData().getDeletedForSender());
        assertEquals(0, fromBooleans.getData().getDeletedForReceiver());
        assertEquals(1, fromBooleans.getData().getDeletedForEveryone());
        assertEquals(1, fromBooleans.getData().getIsDeleted());
        assertEquals(1, fromBooleans.getData().getIsDeletedGlobal());

        String intPayload = "{"
                + "\"status\":true,"
                + "\"message\":\"OK\","
                + "\"data\":{"
                + "\"id\":22,"
                + "\"deleted_for_sender\":0,"
                + "\"deleted_for_receiver\":1,"
                + "\"deleted_for_everyone\":0,"
                + "\"is_deleted\":1,"
                + "\"is_deleted_global\":0"
                + "}"
                + "}";

        SendMessageResponse fromInts = gson.fromJson(intPayload, SendMessageResponse.class);
        assertTrue(fromInts.isStatus());
        assertEquals(0, fromInts.getData().getDeletedForSender());
        assertEquals(1, fromInts.getData().getDeletedForReceiver());
        assertEquals(0, fromInts.getData().getDeletedForEveryone());
        assertEquals(1, fromInts.getData().getIsDeleted());
        assertEquals(0, fromInts.getData().getIsDeletedGlobal());
    }
}

