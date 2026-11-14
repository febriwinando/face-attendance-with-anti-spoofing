package go.pemkott.appsandroidmobiletebingtinggi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse;

public class ChatLocalDatabase extends SQLiteOpenHelper {

    public static final String MONITORING_ADMIN_NIP = "199402092020121005";
    private static final String DATABASE_NAME = "chat_local.db";
    private static final int DATABASE_VERSION = 7;

    private final Context appContext;

    // Contacts Table
    private static final String TABLE_CONTACTS = "chat_contacts";
    private static final String COL_TARGET_ID = "target_id";
    private static final String COL_OPD_ID = "opd_id";
    private static final String COL_CHAT_TYPE = "chat_type";
    private static final String COL_TITLE = "title";
    private static final String COL_SUBTITLE = "subtitle";
    private static final String COL_NIP = "nip";
    private static final String COL_AVATAR_URL = "avatar_url";
    private static final String COL_LAST_MESSAGE = "last_message";
    private static final String COL_LAST_TIME = "last_time";
    private static final String COL_UNREAD_COUNT = "unread_count";

    // Messages Table
    private static final String TABLE_MESSAGES = "chat_messages";
    private static final String COL_MSG_ID = "id";
    private static final String COL_SENDER_ID = "sender_id";
    private static final String COL_SENDER_NAME = "sender_name";
    private static final String COL_RECEIVER_ID = "receiver_id";
    private static final String COL_MSG_OPD_ID = "opd_id";
    private static final String COL_MSG_CHAT_TYPE = "chat_type";
    private static final String COL_MESSAGE = "message";
    private static final String COL_MSG_TYPE = "msg_type";
    private static final String COL_FILE_URL = "file_url";
    private static final String COL_FILE_NAME = "file_name";
    private static final String COL_REPLY_TO_ID = "reply_to_id";
    private static final String COL_REPLY_SENDER_NAME = "reply_to_sender_name";
    private static final String COL_REPLY_MESSAGE = "reply_to_message";
    private static final String COL_IS_ME = "is_me";
    private static final String COL_IS_READ = "is_read";
    private static final String COL_CREATED_AT = "created_at_formatted";
    private static final String COL_IS_DELETED = "is_deleted";
    private static final String COL_DELETED_FOR_USERS = "deleted_for_users";
    private static final String COL_IS_DELETED_GLOBAL = "is_deleted_global";
    private static final String COL_READ_AT = "read_at";
    private static final String COL_DELETED_FOR_SENDER = "deleted_for_sender";
    private static final String COL_DELETED_FOR_RECEIVER = "deleted_for_receiver";
    private static final String COL_DELETED_FOR_EVERYONE = "deleted_for_everyone";
    private static final String COL_DELETED_BY = "deleted_by";
    private static final String COL_DELETED_AT = "deleted_at";

    // Privacy Table
    private static final String TABLE_PRIVACY = "chat_privacy";
    private static final String COL_PRIVACY_ID = "id";
    private static final String COL_EMPLOYEE_ID = "employee_id";
    private static final String COL_PRIVACY_MODE = "privacy_mode";
    private static final String COL_ALLOWED_EMP_IDS = "allowed_employee_ids";
    private static final String COL_ALLOWED_OPD_IDS = "allowed_opd_ids";

    public ChatLocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.appContext = context != null ? context.getApplicationContext() : null;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createContacts = "CREATE TABLE " + TABLE_CONTACTS + " (" +
                COL_TARGET_ID + " INTEGER, " +
                COL_OPD_ID + " INTEGER, " +
                COL_CHAT_TYPE + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_SUBTITLE + " TEXT, " +
                COL_NIP + " TEXT, " +
                COL_AVATAR_URL + " TEXT, " +
                COL_LAST_MESSAGE + " TEXT, " +
                COL_LAST_TIME + " TEXT, " +
                COL_UNREAD_COUNT + " INTEGER, " +
                "PRIMARY KEY (" + COL_TARGET_ID + ", " + COL_CHAT_TYPE + "))";

        String createMessages = "CREATE TABLE " + TABLE_MESSAGES + " (" +
                COL_MSG_ID + " INTEGER PRIMARY KEY, " +
                COL_SENDER_ID + " INTEGER, " +
                COL_SENDER_NAME + " TEXT, " +
                COL_RECEIVER_ID + " INTEGER, " +
                COL_MSG_OPD_ID + " INTEGER, " +
                COL_MSG_CHAT_TYPE + " TEXT, " +
                COL_MESSAGE + " TEXT, " +
                COL_MSG_TYPE + " TEXT, " +
                COL_FILE_URL + " TEXT, " +
                COL_FILE_NAME + " TEXT, " +
                COL_REPLY_TO_ID + " INTEGER, " +
                COL_REPLY_SENDER_NAME + " TEXT, " +
                COL_REPLY_MESSAGE + " TEXT, " +
                COL_IS_ME + " INTEGER, " +
                COL_IS_READ + " INTEGER, " +
                COL_CREATED_AT + " TEXT, " +
                COL_IS_DELETED + " INTEGER DEFAULT 0, " +
                COL_DELETED_FOR_USERS + " TEXT, " +
                COL_IS_DELETED_GLOBAL + " INTEGER DEFAULT 0, " +
                COL_READ_AT + " TEXT, " +
                COL_DELETED_FOR_SENDER + " INTEGER DEFAULT 0, " +
                COL_DELETED_FOR_RECEIVER + " INTEGER DEFAULT 0, " +
                COL_DELETED_FOR_EVERYONE + " INTEGER DEFAULT 0, " +
                COL_DELETED_BY + " TEXT, " +
                COL_DELETED_AT + " TEXT)";

        String createPrivacy = "CREATE TABLE " + TABLE_PRIVACY + " (" +
                COL_PRIVACY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_EMPLOYEE_ID + " INTEGER UNIQUE, " +
                COL_PRIVACY_MODE + " TEXT, " +
                COL_ALLOWED_EMP_IDS + " TEXT, " +
                COL_ALLOWED_OPD_IDS + " TEXT)";

        db.execSQL(createContacts);
        db.execSQL(createMessages);
        db.execSQL(createPrivacy);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 4) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVACY);
            onCreate(db);
            return;
        }
        if (oldVersion < 5) {
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_IS_DELETED + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 6) {
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_FOR_USERS + " TEXT");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_IS_DELETED_GLOBAL + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_READ_AT + " TEXT");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_FOR_SENDER + " INTEGER DEFAULT 0");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_FOR_RECEIVER + " INTEGER DEFAULT 0");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_FOR_EVERYONE + " INTEGER DEFAULT 0");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_BY + " TEXT");
            execSafeAlter(db, "ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_AT + " TEXT");
        }
    }

    private void execSafeAlter(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (Exception ignored) {}
    }

    public static boolean isSuperAdminUser(Context context) {
        if (context == null) return false;
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session =
                    new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(context);
            String myNip = dbHelper.getCurrentEmployeeNip();
            return isSuperAdminNip(myNip)
                    || isSuperAdminNip(session.getUsername())
                    || isSuperAdminNip(session.getNip());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSuperAdminNip(String nip) {
        if (nip == null) return false;
        return MONITORING_ADMIN_NIP.equals(nip.trim());
    }

    public void clearAllLocalChatData() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_CONTACTS);
        db.execSQL("DELETE FROM " + TABLE_MESSAGES);
        db.execSQL("DELETE FROM " + TABLE_PRIVACY);
    }

    // ==========================================
    // PRIVACY LOCAL OPERATIONS
    // ==========================================
    public void savePrivacyLocal(long employeeId, String privacyMode, String allowedEmpJson, String allowedOpdJson) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_EMPLOYEE_ID, employeeId);
        cv.put(COL_PRIVACY_MODE, privacyMode);
        cv.put(COL_ALLOWED_EMP_IDS, allowedEmpJson);
        cv.put(COL_ALLOWED_OPD_IDS, allowedOpdJson);

        db.insertWithOnConflict(TABLE_PRIVACY, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ChatPrivacyResponse.PrivacyData getPrivacyLocal() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PRIVACY + " LIMIT 1", null);

        if (cursor != null && cursor.moveToFirst()) {
            long empId = cursor.getLong(cursor.getColumnIndexOrThrow(COL_EMPLOYEE_ID));
            String mode = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRIVACY_MODE));
            String empJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALLOWED_EMP_IDS));
            String opdJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_ALLOWED_OPD_IDS));
            cursor.close();

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Long>>(){}.getType();
            List<Long> empIds = gson.fromJson(empJson, listType);
            List<Long> opdIds = gson.fromJson(opdJson, listType);

            ChatPrivacyResponse.PrivacyData data = new ChatPrivacyResponse.PrivacyData();
            data.setEmployeeId(empId);
            data.setPrivacyMode(mode);
            data.setAllowedEmployeeIds(empIds != null ? empIds : new ArrayList<>());
            data.setAllowedOpdIds(opdIds != null ? opdIds : new ArrayList<>());
            return data;
        }

        if (cursor != null) cursor.close();
        return null;
    }

    // ==========================================
    // CONTACTS LOCAL OPERATIONS
    // ==========================================
    public void saveContacts(List<ChatContact> contacts) {
        if (contacts == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ChatContact c : contacts) {
                ContentValues cv = new ContentValues();
                cv.put(COL_TARGET_ID, c.getTargetId());
                cv.put(COL_OPD_ID, c.getOpdId());
                cv.put(COL_CHAT_TYPE, c.getChatType());
                cv.put(COL_TITLE, c.getTitle());
                cv.put(COL_SUBTITLE, c.getSubtitle());
                cv.put(COL_NIP, c.getNip());
                cv.put(COL_AVATAR_URL, c.getAvatarUrl());
                cv.put(COL_LAST_MESSAGE, c.getLastMessage());
                cv.put(COL_LAST_TIME, c.getLastTime());
                cv.put(COL_UNREAD_COUNT, c.getUnreadCount());

                db.insertWithOnConflict(TABLE_CONTACTS, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<ChatContact> getContacts() {
        List<ChatContact> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTS + " ORDER BY " + COL_TITLE + " ASC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ChatContact c = new ChatContact();
                c.setTargetId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TARGET_ID)));
                c.setOpdId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_OPD_ID)));
                c.setChatType(cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TYPE)));
                c.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                c.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBTITLE)));
                c.setNip(cursor.getString(cursor.getColumnIndexOrThrow(COL_NIP)));
                c.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_AVATAR_URL)));
                c.setLastMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_MESSAGE)));
                c.setLastTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_TIME)));
                c.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNREAD_COUNT)));
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<ChatContact> getRecentConversations() {
        return getRecentConversations(false);
    }

    public List<ChatContact> getRecentConversations(boolean includeDeleted) {
        List<ChatContact> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE " + COL_CHAT_TYPE + " = 'opd' OR (" + COL_LAST_MESSAGE + " IS NOT NULL AND " + COL_LAST_MESSAGE + " != '')" +
                " ORDER BY CASE WHEN " + COL_CHAT_TYPE + " = 'opd' THEN 0 ELSE 1 END, " + COL_TARGET_ID + " DESC", null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ChatContact c = new ChatContact();
                c.setTargetId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_TARGET_ID)));
                c.setOpdId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_OPD_ID)));
                c.setChatType(cursor.getString(cursor.getColumnIndexOrThrow(COL_CHAT_TYPE)));
                c.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TITLE)));
                c.setSubtitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_SUBTITLE)));
                c.setNip(cursor.getString(cursor.getColumnIndexOrThrow(COL_NIP)));
                c.setAvatarUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_AVATAR_URL)));
                c.setLastMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_MESSAGE)));
                c.setLastTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_TIME)));
                c.setUnreadCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_UNREAD_COUNT)));
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public void updateMessageText(long msgId, String newText) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_MESSAGE, newText);
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(msgId)});
        } catch (Exception ignored) {}
    }

    // ==========================================
    // MESSAGES LOCAL OPERATIONS
    // ==========================================
    private long getCurrentMyEmployeeId() {
        if (appContext == null) return 0L;
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            String empId = dbHelper.getCurrentEmployeeId();
            if (empId != null && !empId.trim().isEmpty()) {
                return Long.parseLong(empId.trim());
            }
            go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session =
                    new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(appContext);
            String sId = session.getEmployeeId();
            if (sId != null && !sId.trim().isEmpty()) {
                return Long.parseLong(sId.trim());
            }
        } catch (Exception ignored) {}
        return 0L;
    }

    /**
     * Klausa isolasi percakapan personal agar pembersihan/reconcile hanya menyentuh pasangan (myId, targetId).
     */
    public static String buildPersonalPairClause(long myId, long targetId) {
        if (myId > 0) {
            return "((" + COL_SENDER_ID + " = ? AND " + COL_RECEIVER_ID + " = ?) OR (" +
                    COL_SENDER_ID + " = ? AND " + COL_RECEIVER_ID + " = ?))";
        }
        return "(" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
    }

    private String buildScopeSelection(String chatType, long targetId, boolean includeDeleted, List<String> argsOut) {
        StringBuilder sb = new StringBuilder();
        if ("opd".equals(chatType)) {
            sb.append(COL_MSG_CHAT_TYPE).append(" = ? AND (")
                    .append(COL_MSG_OPD_ID).append(" = ? OR ").append(COL_RECEIVER_ID).append(" = ?)");
            argsOut.add("opd");
            argsOut.add(String.valueOf(targetId));
            argsOut.add(String.valueOf(targetId));
        } else {
            long myId = getCurrentMyEmployeeId();
            sb.append(COL_MSG_CHAT_TYPE).append(" = ? AND ").append(buildPersonalPairClause(myId, targetId));
            argsOut.add("personal");
            if (myId > 0) {
                argsOut.add(String.valueOf(myId));
                argsOut.add(String.valueOf(targetId));
                argsOut.add(String.valueOf(targetId));
                argsOut.add(String.valueOf(myId));
            } else {
                argsOut.add(String.valueOf(targetId));
                argsOut.add(String.valueOf(targetId));
            }
        }

        if (!includeDeleted) {
            sb.append(" AND (").append(COL_IS_DELETED).append(" = 0 OR ").append(COL_IS_DELETED).append(" IS NULL)")
                    .append(" AND (").append(COL_IS_DELETED_GLOBAL).append(" = 0 OR ").append(COL_IS_DELETED_GLOBAL).append(" IS NULL)")
                    .append(" AND (").append(COL_DELETED_FOR_EVERYONE).append(" = 0 OR ").append(COL_DELETED_FOR_EVERYONE).append(" IS NULL)");
        }
        return sb.toString();
    }

    private ContentValues buildMessageContentValues(SQLiteDatabase db, ChatMessageItem m) {
        ContentValues cv = new ContentValues();
        cv.put(COL_MSG_ID, m.getId());
        cv.put(COL_SENDER_ID, m.getSenderId());
        cv.put(COL_SENDER_NAME, m.getSenderName());
        cv.put(COL_RECEIVER_ID, m.getReceiverId());
        Long opdId = m.getOpdId();
        if ((opdId == null || opdId == 0) && "opd".equals(m.getChatType())) {
            opdId = m.getReceiverId();
        }
        cv.put(COL_MSG_OPD_ID, opdId);
        cv.put(COL_MSG_CHAT_TYPE, m.getChatType());
        cv.put(COL_MESSAGE, m.getMessage());
        cv.put(COL_MSG_TYPE, m.getMsgType());
        cv.put(COL_FILE_URL, m.getFileUrl());
        cv.put(COL_FILE_NAME, m.getFileName());
        cv.put(COL_REPLY_TO_ID, m.getReplyToId());
        cv.put(COL_REPLY_SENDER_NAME, m.getReplyToSenderName());
        cv.put(COL_REPLY_MESSAGE, m.getReplyToMessage());
        cv.put(COL_IS_ME, m.isMe() ? 1 : 0);
        cv.put(COL_IS_READ, m.isRead() ? 1 : 0);
        cv.put(COL_CREATED_AT, m.getCreatedAtFormatted());
        cv.put(COL_READ_AT, m.getReadAt());
        cv.put(COL_DELETED_BY, m.getDeletedBy());
        cv.put(COL_DELETED_AT, m.getDeletedAt());

        int localIsDeleted = 0;
        Cursor cDel = db.query(TABLE_MESSAGES, new String[]{COL_IS_DELETED},
                COL_MSG_ID + " = ?", new String[]{String.valueOf(m.getId())}, null, null, null);
        if (cDel != null) {
            if (cDel.moveToFirst()) {
                localIsDeleted = cDel.getInt(0);
            }
            cDel.close();
        }

        int finalDeletedForSender = (m.getDeletedForSender() == 1 || (m.isMe() && localIsDeleted == 1)) ? 1 : 0;
        int finalDeletedForReceiver = (m.getDeletedForReceiver() == 1 || (!m.isMe() && localIsDeleted == 1)) ? 1 : 0;
        int finalDeletedForEveryone = (m.getDeletedForEveryone() == 1 || m.getIsDeletedGlobal() == 1) ? 1 : 0;
        int finalDeleted = (localIsDeleted == 1 || m.isDeleted() || finalDeletedForEveryone == 1) ? 1 : 0;

        cv.put(COL_IS_DELETED, finalDeleted);
        cv.put(COL_DELETED_FOR_USERS, m.getDeletedForUsers());
        cv.put(COL_IS_DELETED_GLOBAL, finalDeletedForEveryone);
        cv.put(COL_DELETED_FOR_SENDER, finalDeletedForSender);
        cv.put(COL_DELETED_FOR_RECEIVER, finalDeletedForReceiver);
        cv.put(COL_DELETED_FOR_EVERYONE, finalDeletedForEveryone);
        return cv;
    }

    private ChatMessageItem mapCursorToMessageItem(Cursor cursor) {
        ChatMessageItem m = new ChatMessageItem();
        m.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MSG_ID)));
        m.setSenderId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_SENDER_ID)));
        m.setSenderName(cursor.getString(cursor.getColumnIndexOrThrow(COL_SENDER_NAME)));
        m.setReceiverId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECEIVER_ID)));
        m.setOpdId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_MSG_OPD_ID)));
        m.setChatType(cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_CHAT_TYPE)));
        m.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_MESSAGE)));
        String type = cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TYPE));
        String url = cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URL));
        if (url != null) {
            String l = url.toLowerCase();
            if (l.endsWith(".m4a") || l.endsWith(".mp3") || l.endsWith(".wav") || l.endsWith(".aac") || l.endsWith(".ogg") || l.endsWith(".opus") || l.contains("/chat_media/audio/") || l.contains("voice_note")) {
                type = "audio";
            }
        }
        m.setMsgType(type != null ? type : "text");
        m.setFileUrl(url);
        m.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)));
        m.setReplyToId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_REPLY_TO_ID)));
        m.setReplyToSenderName(cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_SENDER_NAME)));
        m.setReplyToMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_MESSAGE)));
        m.setMe(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ME)) == 1);
        m.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_READ)) == 1);
        m.setCreatedAtFormatted(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));

        int delColIndex = cursor.getColumnIndex(COL_IS_DELETED);
        if (delColIndex != -1) m.setIsDeleted(cursor.getInt(delColIndex));
        int globalDelIndex = cursor.getColumnIndex(COL_IS_DELETED_GLOBAL);
        if (globalDelIndex != -1) m.setIsDeletedGlobal(cursor.getInt(globalDelIndex));
        int userDelIndex = cursor.getColumnIndex(COL_DELETED_FOR_USERS);
        if (userDelIndex != -1) m.setDeletedForUsers(cursor.getString(userDelIndex));
        int readAtIndex = cursor.getColumnIndex(COL_READ_AT);
        if (readAtIndex != -1) m.setReadAt(cursor.getString(readAtIndex));
        int dfsIdx = cursor.getColumnIndex(COL_DELETED_FOR_SENDER);
        if (dfsIdx != -1) m.setDeletedForSender(cursor.getInt(dfsIdx));
        int dfrIdx = cursor.getColumnIndex(COL_DELETED_FOR_RECEIVER);
        if (dfrIdx != -1) m.setDeletedForReceiver(cursor.getInt(dfrIdx));
        int dfeIdx = cursor.getColumnIndex(COL_DELETED_FOR_EVERYONE);
        if (dfeIdx != -1) m.setDeletedForEveryone(cursor.getInt(dfeIdx));
        int dbyIdx = cursor.getColumnIndex(COL_DELETED_BY);
        if (dbyIdx != -1) m.setDeletedBy(cursor.getString(dbyIdx));
        int datIdx = cursor.getColumnIndex(COL_DELETED_AT);
        if (datIdx != -1) m.setDeletedAt(cursor.getString(datIdx));
        return m;
    }

    public void saveMessages(List<ChatMessageItem> messages) {
        if (messages == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ChatMessageItem m : messages) {
                ContentValues cv = buildMessageContentValues(db, m);
                db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Rekonsiliasi penuh daftar pesan percakapan dengan data resmi dari server
     * (menghapus cache pesan yang dihapus untuk semua pihak atau dihapus permanen oleh Admin).
     */
    public void reconcileMessagesFromServer(String chatType, long targetId, List<ChatMessageItem> serverList) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            Set<Long> activeServerIds = new HashSet<>();
            if (serverList != null) {
                for (ChatMessageItem m : serverList) {
                    activeServerIds.add(m.getId());
                    ContentValues cv = buildMessageContentValues(db, m);
                    db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
                }
            }

            List<String> args = new ArrayList<>();
            String scopeWhere = buildScopeSelection(chatType, targetId, true, args);
            Cursor cExisting = db.query(TABLE_MESSAGES, new String[]{COL_MSG_ID}, scopeWhere,
                    args.toArray(new String[0]), null, null, null);
            List<Long> toRemove = new ArrayList<>();
            if (cExisting != null) {
                while (cExisting.moveToNext()) {
                    long existingId = cExisting.getLong(0);
                    if (existingId > 0 && !activeServerIds.contains(existingId)) {
                        toRemove.add(existingId);
                    }
                }
                cExisting.close();
            }

            for (Long removeId : toRemove) {
                db.delete(TABLE_MESSAGES, COL_MSG_ID + " = ?", new String[]{String.valueOf(removeId)});
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        syncLastMessageFromLocal(chatType, targetId, false);
    }

    public void insertMessage(ChatMessageItem m) {
        if (m == null) return;
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = buildMessageContentValues(db, m);
        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ChatMessageItem> getMessages(String chatType, long targetId) {
        return getMessages(chatType, targetId, false);
    }

    public List<ChatMessageItem> getMessages(String chatType, long targetId, boolean includeDeleted) {
        List<ChatMessageItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        List<String> args = new ArrayList<>();
        String selection = buildScopeSelection(chatType, targetId, includeDeleted, args);

        Cursor cursor = db.query(TABLE_MESSAGES, null, selection,
                args.toArray(new String[0]), null, null, COL_MSG_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToMessageItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public ChatMessageItem getLatestVisibleMessageForContact(String chatType, long targetId) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> args = new ArrayList<>();
        String selection = buildScopeSelection(chatType, targetId, false, args);
        Cursor cursor = db.query(TABLE_MESSAGES, null, selection,
                args.toArray(new String[0]), null, null, COL_MSG_ID + " DESC", "1");
        ChatMessageItem latest = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                latest = mapCursorToMessageItem(cursor);
            }
            cursor.close();
        }
        return latest;
    }

    public int countUnreadSentMessages(String chatType, long targetId) {
        SQLiteDatabase db = getReadableDatabase();
        List<String> args = new ArrayList<>();
        String selection = buildScopeSelection(chatType, targetId, false, args)
                + " AND " + COL_IS_ME + " = 1 AND (" + COL_IS_READ + " = 0 OR " + COL_IS_READ + " IS NULL)";
        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{"COUNT(*)"}, selection,
                args.toArray(new String[0]), null, null, null);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public long getLastMessageId(String chatType, long targetId) {
        long lastId = 0;
        SQLiteDatabase db = getReadableDatabase();
        List<String> args = new ArrayList<>();
        String selection = buildScopeSelection(chatType, targetId, true, args);

        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{"MAX(" + COL_MSG_ID + ")"},
                selection, args.toArray(new String[0]), null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getLong(0);
            cursor.close();
        }
        return lastId;
    }

    public void softDeleteMessage(long messageId) {
        softDeleteForSenderOrReceiverLocal(messageId, true, "");
    }

    public void softDeleteForSenderOrReceiverLocal(long messageId, boolean isMe, String actorNip) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            if (isMe) {
                cv.put(COL_DELETED_FOR_SENDER, 1);
            } else {
                cv.put(COL_DELETED_FOR_RECEIVER, 1);
            }
            if (actorNip != null && !actorNip.isEmpty()) {
                cv.put(COL_DELETED_BY, actorNip);
            }
            cv.put(COL_DELETED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void softDeleteForEveryoneLocal(long messageId, String deletedByNip) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            cv.put(COL_IS_DELETED_GLOBAL, 1);
            cv.put(COL_DELETED_FOR_EVERYONE, 1);
            if (deletedByNip != null && !deletedByNip.isEmpty()) {
                cv.put(COL_DELETED_BY, deletedByNip);
            }
            cv.put(COL_DELETED_AT, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()));
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void softClearConversation(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            List<String> args = new ArrayList<>();
            String selection = buildScopeSelection(chatType, targetId, true, args);
            db.update(TABLE_MESSAGES, cv, selection, args.toArray(new String[0]));
        } catch (Exception ignored) {}
    }

    public void syncLastMessageFromLocal(String chatType, long targetId, boolean includeDeleted) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            List<String> args = new ArrayList<>();
            String selection = buildScopeSelection(chatType, targetId, includeDeleted, args);

            Cursor cursor = db.query(TABLE_MESSAGES,
                    new String[]{COL_MESSAGE, COL_MSG_TYPE, COL_CREATED_AT, COL_IS_DELETED, COL_SENDER_NAME, COL_IS_ME, COL_IS_DELETED_GLOBAL, COL_DELETED_FOR_EVERYONE},
                    selection, args.toArray(new String[0]), null, null, COL_MSG_ID + " DESC", "1");

            String lastMsg = "Tidak ada pesan";
            String lastTime = "";
            if (cursor != null && cursor.moveToFirst()) {
                lastMsg = cursor.getString(0);
                String msgType = cursor.getString(1);
                lastTime = cursor.getString(2);
                int isDel = cursor.getInt(3);
                String senderName = cursor.getString(4);
                int isMe = cursor.getInt(5);
                int isDelGlobal = cursor.getInt(6);
                int delEveryone = cursor.getInt(7);

                if ("image".equalsIgnoreCase(msgType)) {
                    if (lastMsg == null || lastMsg.trim().isEmpty()) lastMsg = "📷 Foto";
                } else if ("audio".equalsIgnoreCase(msgType)) {
                    if (lastMsg == null || lastMsg.trim().isEmpty()) lastMsg = "🎤 Pesan Suara";
                } else if ("video".equalsIgnoreCase(msgType)) {
                    if (lastMsg == null || lastMsg.trim().isEmpty()) lastMsg = "🎥 Video";
                } else if ("document".equalsIgnoreCase(msgType)) {
                    if (lastMsg == null || lastMsg.trim().isEmpty()) lastMsg = "📄 Dokumen";
                }

                if ("opd".equals(chatType)) {
                    String senderPrefix = (isMe == 1) ? "Anda: " : (!TextUtils.isEmpty(senderName) ? senderName + ": " : "");
                    lastMsg = senderPrefix + (lastMsg != null ? lastMsg : "");
                }

                if ((isDel == 1 || isDelGlobal == 1 || delEveryone == 1) && includeDeleted) {
                    String tag = (isDelGlobal == 1 || delEveryone == 1) ? "[Dihapus Semua] " : "[Dihapus] ";
                    lastMsg = tag + (lastMsg != null ? lastMsg : "");
                }

                cursor.close();
            } else if (cursor != null) {
                cursor.close();
            }

            ContentValues cv = new ContentValues();
            cv.put(COL_LAST_MESSAGE, lastMsg != null ? lastMsg : "Tidak ada pesan");
            cv.put(COL_LAST_TIME, lastTime != null ? lastTime : "");
            if ("Tidak ada pesan".equals(lastMsg)) {
                cv.put(COL_UNREAD_COUNT, 0);
            }
            db.update(TABLE_CONTACTS, cv, COL_TARGET_ID + " = ? AND " + COL_CHAT_TYPE + " = ?", new String[]{String.valueOf(targetId), chatType});
        } catch (Exception ignored) {}
    }

    public void deleteMessageLocal(long messageId) {
        softDeleteMessage(messageId);
    }

    public void clearConversationLocal(String chatType, long targetId) {
        softClearConversation(chatType, targetId);
    }

    public void hardDeleteMessage(long messageId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            db.delete(TABLE_MESSAGES, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void hardDeleteMessagesLocal(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (Long id : messageIds) {
                if (id != null && id > 0) {
                    db.delete(TABLE_MESSAGES, COL_MSG_ID + " = ?", new String[]{String.valueOf(id)});
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception ignored) {
        } finally {
            db.endTransaction();
        }
    }

    public void hardClearConversation(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            List<String> args = new ArrayList<>();
            String selection = buildScopeSelection(chatType, targetId, true, args);
            db.delete(TABLE_MESSAGES, selection, args.toArray(new String[0]));
        } catch (Exception ignored) {}
    }

    public void softDeleteGlobal(long messageId) {
        softDeleteForEveryoneLocal(messageId, "");
    }

    public void restoreMessage(long messageId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 0);
            cv.put(COL_IS_DELETED_GLOBAL, 0);
            cv.put(COL_DELETED_FOR_SENDER, 0);
            cv.put(COL_DELETED_FOR_RECEIVER, 0);
            cv.put(COL_DELETED_FOR_EVERYONE, 0);
            cv.put(COL_DELETED_FOR_USERS, "");
            cv.putNull(COL_DELETED_BY);
            cv.putNull(COL_DELETED_AT);
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void markMessagesAsRead(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_READ, 1);
            if ("opd".equals(chatType)) {
                db.update(TABLE_MESSAGES, cv, COL_MSG_CHAT_TYPE + " = ? AND " + COL_MSG_OPD_ID + " = ? AND " + COL_IS_ME + " = 0", new String[]{"opd", String.valueOf(targetId)});
            } else {
                db.update(TABLE_MESSAGES, cv, COL_MSG_CHAT_TYPE + " = ? AND " + COL_SENDER_ID + " = ? AND " + COL_IS_ME + " = 0", new String[]{"personal", String.valueOf(targetId)});
            }
        } catch (Exception ignored) {}
    }

    public void resetUnreadCount(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_UNREAD_COUNT, 0);
            db.update(TABLE_CONTACTS, cv, COL_TARGET_ID + " = ? AND " + COL_CHAT_TYPE + " = ?", new String[]{String.valueOf(targetId), chatType});
        } catch (Exception ignored) {}
    }
}
