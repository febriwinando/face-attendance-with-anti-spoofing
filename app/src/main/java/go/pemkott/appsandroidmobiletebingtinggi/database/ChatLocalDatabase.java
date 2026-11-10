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
import java.util.ArrayList;
import java.util.List;

import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatContact;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatMessageItem;
import go.pemkott.appsandroidmobiletebingtinggi.model.chat.ChatPrivacyResponse;

public class ChatLocalDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "chat_local.db";
    private static final int DATABASE_VERSION = 6;

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

    // Privacy Table
    private static final String TABLE_PRIVACY = "chat_privacy";
    private static final String COL_PRIVACY_ID = "id";
    private static final String COL_EMPLOYEE_ID = "employee_id";
    private static final String COL_PRIVACY_MODE = "privacy_mode";
    private static final String COL_ALLOWED_EMP_IDS = "allowed_employee_ids";
    private static final String COL_ALLOWED_OPD_IDS = "allowed_opd_ids";

    public ChatLocalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
                COL_IS_DELETED_GLOBAL + " INTEGER DEFAULT 0)";

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
        if (oldVersion < 5) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_IS_DELETED + " INTEGER DEFAULT 0");
            } catch (Exception ignored) {}
        }
        if (oldVersion < 6) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_DELETED_FOR_USERS + " TEXT");
                db.execSQL("ALTER TABLE " + TABLE_MESSAGES + " ADD COLUMN " + COL_IS_DELETED_GLOBAL + " INTEGER DEFAULT 0");
                return;
            } catch (Exception ignored) {}
        }
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVACY);
        onCreate(db);
    }

    public static boolean isSuperAdminUser(Context context) {
        if (context == null) return false;
        try {
            DatabaseHelper dbHelper = new DatabaseHelper(context);
            go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager session =
                    new go.pemkott.appsandroidmobiletebingtinggi.login.SessionManager(context);
            String myNip = dbHelper.getCurrentEmployeeNip();
            return isSuperAdminNip(myNip)
                    || isSuperAdminNip(session.getPegawaiId())
                    || isSuperAdminNip(session.getUsername())
                    || isSuperAdminNip(session.getEmployeeId());
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isSuperAdminNip(String nip) {
        if (nip == null || nip.isEmpty()) return false;
        String clean = nip.trim();
        return clean.startsWith("19940209202012") || clean.contains("199402092020121005");
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
    public void saveMessages(List<ChatMessageItem> messages) {
        if (messages == null) return;
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (ChatMessageItem m : messages) {
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

                // Pastikan jika pesan sudah dihapus di perangkat ini (soft delete per-user),
                // sinkronisasi dari server tidak akan menghidupkan kembali pesan tersebut
                int localIsDeleted = 0;
                Cursor cDel = db.query(TABLE_MESSAGES, new String[]{COL_IS_DELETED},
                        COL_MSG_ID + " = ?", new String[]{String.valueOf(m.getId())}, null, null, null);
                if (cDel != null) {
                    if (cDel.moveToFirst()) {
                        localIsDeleted = cDel.getInt(0);
                    }
                    cDel.close();
                }
                int finalDeleted = (localIsDeleted == 1 || m.isDeleted()) ? 1 : 0;
                cv.put(COL_IS_DELETED, finalDeleted);
                cv.put(COL_DELETED_FOR_USERS, m.getDeletedForUsers());
                cv.put(COL_IS_DELETED_GLOBAL, m.getIsDeletedGlobal());

                db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void insertMessage(ChatMessageItem m) {
        if (m == null) return;
        SQLiteDatabase db = getWritableDatabase();
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

        int localIsDeleted = 0;
        Cursor cDel = db.query(TABLE_MESSAGES, new String[]{COL_IS_DELETED},
                COL_MSG_ID + " = ?", new String[]{String.valueOf(m.getId())}, null, null, null);
        if (cDel != null) {
            if (cDel.moveToFirst()) {
                localIsDeleted = cDel.getInt(0);
            }
            cDel.close();
        }
        int finalDeleted = (localIsDeleted == 1 || m.isDeleted()) ? 1 : 0;
        cv.put(COL_IS_DELETED, finalDeleted);
        cv.put(COL_DELETED_FOR_USERS, m.getDeletedForUsers());
        cv.put(COL_IS_DELETED_GLOBAL, m.getIsDeletedGlobal());

        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ChatMessageItem> getMessages(String chatType, long targetId) {
        return getMessages(chatType, targetId, false);
    }

    public List<ChatMessageItem> getMessages(String chatType, long targetId, boolean includeDeleted) {
        List<ChatMessageItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;

        if ("opd".equals(chatType)) {
            selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_MSG_OPD_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
            if (!includeDeleted) {
                selection += " AND (" + COL_IS_DELETED + " = 0 OR " + COL_IS_DELETED + " IS NULL) AND (" + COL_IS_DELETED_GLOBAL + " = 0 OR " + COL_IS_DELETED_GLOBAL + " IS NULL)";
            }
            selectionArgs = new String[]{"opd", String.valueOf(targetId), String.valueOf(targetId)};
        } else {
            selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
            if (!includeDeleted) {
                selection += " AND (" + COL_IS_DELETED + " = 0 OR " + COL_IS_DELETED + " IS NULL) AND (" + COL_IS_DELETED_GLOBAL + " = 0 OR " + COL_IS_DELETED_GLOBAL + " IS NULL)";
            }
            selectionArgs = new String[]{"personal", String.valueOf(targetId), String.valueOf(targetId)};
        }

        Cursor cursor = db.query(TABLE_MESSAGES, null, selection, selectionArgs, null, null, COL_MSG_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
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
                if (delColIndex != -1) {
                    m.setIsDeleted(cursor.getInt(delColIndex));
                }
                int globalDelIndex = cursor.getColumnIndex(COL_IS_DELETED_GLOBAL);
                if (globalDelIndex != -1) {
                    m.setIsDeletedGlobal(cursor.getInt(globalDelIndex));
                }
                int userDelIndex = cursor.getColumnIndex(COL_DELETED_FOR_USERS);
                if (userDelIndex != -1) {
                    m.setDeletedForUsers(cursor.getString(userDelIndex));
                }
                list.add(m);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public long getLastMessageId(String chatType, long targetId) {
        long lastId = 0;
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;

        if ("opd".equals(chatType)) {
            selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_MSG_OPD_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
            selectionArgs = new String[]{"opd", String.valueOf(targetId), String.valueOf(targetId)};
        } else {
            selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
            selectionArgs = new String[]{"personal", String.valueOf(targetId), String.valueOf(targetId)};
        }

        Cursor cursor = db.query(TABLE_MESSAGES, new String[]{"MAX(" + COL_MSG_ID + ")"}, selection, selectionArgs, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getLong(0);
            cursor.close();
        }
        return lastId;
    }

    public void softDeleteMessage(long messageId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void softClearConversation(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            if ("opd".equals(chatType)) {
                db.update(TABLE_MESSAGES, cv, COL_MSG_CHAT_TYPE + " = ? AND (" + COL_MSG_OPD_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)", new String[]{"opd", String.valueOf(targetId), String.valueOf(targetId)});
            } else {
                db.update(TABLE_MESSAGES, cv, COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)", new String[]{"personal", String.valueOf(targetId), String.valueOf(targetId)});
            }
        } catch (Exception ignored) {}
    }

    public void syncLastMessageFromLocal(String chatType, long targetId, boolean includeDeleted) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            String selection;
            String[] selectionArgs;
            if ("opd".equals(chatType)) {
                selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_MSG_OPD_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
                if (!includeDeleted) {
                    selection += " AND (" + COL_IS_DELETED + " = 0 OR " + COL_IS_DELETED + " IS NULL) AND (" + COL_IS_DELETED_GLOBAL + " = 0 OR " + COL_IS_DELETED_GLOBAL + " IS NULL)";
                }
                selectionArgs = new String[]{"opd", String.valueOf(targetId), String.valueOf(targetId)};
            } else {
                selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
                if (!includeDeleted) {
                    selection += " AND (" + COL_IS_DELETED + " = 0 OR " + COL_IS_DELETED + " IS NULL) AND (" + COL_IS_DELETED_GLOBAL + " = 0 OR " + COL_IS_DELETED_GLOBAL + " IS NULL)";
                }
                selectionArgs = new String[]{"personal", String.valueOf(targetId), String.valueOf(targetId)};
            }

            Cursor cursor = db.query(TABLE_MESSAGES,
                    new String[]{COL_MESSAGE, COL_MSG_TYPE, COL_CREATED_AT, COL_IS_DELETED, COL_SENDER_NAME, COL_IS_ME, COL_IS_DELETED_GLOBAL},
                    selection, selectionArgs, null, null, COL_MSG_ID + " DESC", "1");

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

                if ((isDel == 1 || isDelGlobal == 1) && includeDeleted) {
                    String tag = (isDelGlobal == 1) ? "[Dihapus Global] " : "[Dihapus] ";
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

    public void hardClearConversation(String chatType, long targetId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if ("opd".equals(chatType)) {
                db.delete(TABLE_MESSAGES, COL_MSG_CHAT_TYPE + " = ? AND (" + COL_MSG_OPD_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)", new String[]{"opd", String.valueOf(targetId), String.valueOf(targetId)});
            } else {
                db.delete(TABLE_MESSAGES, COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)", new String[]{"personal", String.valueOf(targetId), String.valueOf(targetId)});
            }
        } catch (Exception ignored) {}
    }

    public void softDeleteGlobal(long messageId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 1);
            cv.put(COL_IS_DELETED_GLOBAL, 1);
            db.update(TABLE_MESSAGES, cv, COL_MSG_ID + " = ?", new String[]{String.valueOf(messageId)});
        } catch (Exception ignored) {}
    }

    public void restoreMessage(long messageId) {
        try {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put(COL_IS_DELETED, 0);
            cv.put(COL_IS_DELETED_GLOBAL, 0);
            cv.put(COL_DELETED_FOR_USERS, "");
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
