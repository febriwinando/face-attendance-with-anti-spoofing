package go.pemkott.appsandroidmobiletebingtinggi.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
    private static final int DATABASE_VERSION = 3;

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
                COL_TARGET_ID + " INTEGER PRIMARY KEY, " +
                COL_OPD_ID + " INTEGER, " +
                COL_CHAT_TYPE + " TEXT, " +
                COL_TITLE + " TEXT, " +
                COL_SUBTITLE + " TEXT, " +
                COL_NIP + " TEXT, " +
                COL_AVATAR_URL + " TEXT, " +
                COL_LAST_MESSAGE + " TEXT, " +
                COL_LAST_TIME + " TEXT, " +
                COL_UNREAD_COUNT + " INTEGER)";

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
                COL_CREATED_AT + " TEXT)";

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
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRIVACY);
        onCreate(db);
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
        List<ChatContact> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_CONTACTS +
                " WHERE " + COL_LAST_MESSAGE + " IS NOT NULL AND " + COL_LAST_MESSAGE + " != ''" +
                " ORDER BY " + COL_TARGET_ID + " DESC", null);

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
                cv.put(COL_MSG_OPD_ID, m.getOpdId());
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
        cv.put(COL_MSG_OPD_ID, m.getOpdId());
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

        db.insertWithOnConflict(TABLE_MESSAGES, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public List<ChatMessageItem> getMessages(String chatType, long targetId) {
        List<ChatMessageItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String selection;
        String[] selectionArgs;

        if ("opd".equals(chatType)) {
            selection = COL_MSG_CHAT_TYPE + " = ? AND " + COL_MSG_OPD_ID + " = ?";
            selectionArgs = new String[]{"opd", String.valueOf(targetId)};
        } else {
            selection = COL_MSG_CHAT_TYPE + " = ? AND (" + COL_SENDER_ID + " = ? OR " + COL_RECEIVER_ID + " = ?)";
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
                m.setMsgType(cursor.getString(cursor.getColumnIndexOrThrow(COL_MSG_TYPE)));
                m.setFileUrl(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_URL)));
                m.setFileName(cursor.getString(cursor.getColumnIndexOrThrow(COL_FILE_NAME)));
                m.setReplyToId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_REPLY_TO_ID)));
                m.setReplyToSenderName(cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_SENDER_NAME)));
                m.setReplyToMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_REPLY_MESSAGE)));
                m.setMe(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_ME)) == 1);
                m.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(COL_IS_READ)) == 1);
                m.setCreatedAtFormatted(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
                list.add(m);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
}
