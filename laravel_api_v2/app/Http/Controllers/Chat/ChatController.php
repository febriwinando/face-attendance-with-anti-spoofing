<?php

namespace App\Http\Controllers\Chat;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class ChatController extends Controller
{
    private const SUPER_ADMIN_NIP = '199402092020121005';

    private function isSuperAdmin($nip)
    {
        if (!$nip) return false;
        return str_starts_with(trim($nip), '19940209202012') || trim($nip) === self::SUPER_ADMIN_NIP;
    }

    /**
     * Mendapatkan user pegawai yang sedang login (via token Bearer atau session)
     */
    private function getAuthenticatedUser(Request $request)
    {
        $user = $request->user();
        if ($user) {
            return $user;
        }

        // Fallback jika menggunakan token kustom / session table
        $bearer = $request->bearerToken();
        if ($bearer) {
            $emp = DB::table('employees')->where('api_token', $bearer)->orWhere('remember_token', $bearer)->first();
            if ($emp) return $emp;
        }

        // Fallback jika dikirim parameter employee_id / user_id
        $empId = $request->input('user_id', $request->input('employee_id'));
        if ($empId) {
            return DB::table('employees')->where('id', $empId)->first();
        }

        return null;
    }

    /**
     * Hapus Pesan Chat (Mendukung 'for_me', 'global', dan 'hard_delete')
     */
    public function deleteMessage(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userId = $user ? $user->id : $request->input('user_id');
            $userNip = $user ? ($user->nip ?? '') : $request->input('nip', '');

            $messageId = $request->input('message_id');
            $deleteType = $request->input('delete_type', 'for_me'); // 'for_me', 'global', 'hard_delete'

            if (!$messageId) {
                return response()->json(['status' => false, 'message' => 'Message ID wajib disertakan.'], 400);
            }

            $msg = DB::table('chat_messages')->where('id', $messageId)->first();
            if (!$msg) {
                return response()->json(['status' => false, 'message' => 'Pesan tidak ditemukan.'], 404);
            }

            // Hapus Permanen dari Database (Hard Delete) - Khusus NIP 19940209202012 dari Monitoring
            if ($deleteType === 'hard_delete') {
                if (!$this->isSuperAdmin($userNip)) {
                    return response()->json(['status' => false, 'message' => 'Akses ditolak: Pegawai biasa tidak dapat menghapus secara permanen. Hanya NIP 19940209202012 dari monitoring yang berhak.'], 403);
                }
                DB::table('chat_messages')->where('id', $messageId)->delete();
                return response()->json(['status' => true, 'message' => 'Pesan berhasil dihapus secara fisik dan permanen dari database.'], 200);
            }

            // Tandai Hapus Kedinasan (Soft Delete Global) - Khusus NIP 19940209202012 dari Monitoring
            if ($deleteType === 'global') {
                if (!$this->isSuperAdmin($userNip)) {
                    return response()->json(['status' => false, 'message' => 'Akses ditolak: Hanya Super Admin Monitoring yang berhak menandai hapus global.'], 403);
                }
                DB::table('chat_messages')->where('id', $messageId)->update([
                    'is_deleted'        => 1,
                    'is_deleted_global' => 1,
                    'updated_at'        => now()
                ]);
                return response()->json(['status' => true, 'message' => 'Pesan ditandai dihapus kedinasan (Global).'], 200);
            }

            // Hapus untuk Saya (Per-User Soft Delete) - Default untuk seluruh pegawai
            $currentDeletedUsers = $msg->deleted_for_users ?? '';
            $userTag = ',' . $userId . ',';
            if (strpos($currentDeletedUsers, $userTag) === false) {
                $newDeletedUsers = ($currentDeletedUsers ? $currentDeletedUsers : ',') . $userId . ',';
                DB::table('chat_messages')->where('id', $messageId)->update([
                    'deleted_for_users' => $newDeletedUsers,
                    'updated_at'        => now()
                ]);
            }

            return response()->json(['status' => true, 'message' => 'Pesan berhasil dihapus dari perangkat Anda.'], 200);

        } catch (\Exception $e) {
            Log::error('Error deleteMessage: ' . $e->getMessage());
            return response()->json(['status' => false, 'message' => 'Terjadi kesalahan: ' . $e->getMessage()], 500);
        }
    }

    /**
     * Bersihkan Obrolan (Mendukung 'for_me', 'global', dan 'hard_delete')
     */
    public function clearConversation(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userId = $user ? $user->id : $request->input('user_id');
            $userNip = $user ? ($user->nip ?? '') : $request->input('nip', '');

            $targetId = $request->input('target_id');
            $chatType = $request->input('chat_type', 'personal'); // 'personal' or 'opd'
            $clearType = $request->input('clear_type', 'for_me'); // 'for_me', 'global', 'hard_delete'

            if (!$targetId) {
                return response()->json(['status' => false, 'message' => 'Target ID wajib disertakan.'], 400);
            }

            $query = DB::table('chat_messages');
            if ($chatType === 'opd') {
                $query->where('chat_type', 'opd')->where(function ($q) use ($targetId) {
                    $q->where('opd_id', $targetId)->orWhere('receiver_id', $targetId);
                });
            } else {
                $query->where('chat_type', 'personal')->where(function ($q) use ($targetId, $userId) {
                    $q->where(function ($sq) use ($targetId, $userId) {
                        $sq->where('sender_id', $userId)->where('receiver_id', $targetId);
                    })->orWhere(function ($sq) use ($targetId, $userId) {
                        $sq->where('sender_id', $targetId)->where('receiver_id', $userId);
                    });
                });
            }

            // Hard Clear - Khusus NIP 19940209202012 dari Monitoring
            if ($clearType === 'hard_delete') {
                if (!$this->isSuperAdmin($userNip)) {
                    return response()->json(['status' => false, 'message' => 'Akses ditolak: Pegawai biasa tidak dapat membersihkan secara permanen. Hanya NIP 19940209202012 dari monitoring yang berhak.'], 403);
                }
                $query->delete();
                return response()->json(['status' => true, 'message' => 'Seluruh riwayat obrolan telah dihapus permanen dari database.'], 200);
            }

            // Global Soft Clear - Khusus NIP 19940209202012 dari Monitoring
            if ($clearType === 'global') {
                if (!$this->isSuperAdmin($userNip)) {
                    return response()->json(['status' => false, 'message' => 'Akses ditolak: Hanya Super Admin Monitoring yang berhak menandai bersihkan global.'], 403);
                }
                $query->update([
                    'is_deleted'        => 1,
                    'is_deleted_global' => 1,
                    'updated_at'        => now()
                ]);
                return response()->json(['status' => true, 'message' => 'Riwayat obrolan ditandai dibersihkan kedinasan (Global).'], 200);
            }

            // Clear for Me (Per-User) - Default
            $messages = $query->get();
            $userTag = ',' . $userId . ',';
            foreach ($messages as $m) {
                $current = $m->deleted_for_users ?? '';
                if (strpos($current, $userTag) === false) {
                    $newDel = ($current ? $current : ',') . $userId . ',';
                    DB::table('chat_messages')->where('id', $m->id)->update(['deleted_for_users' => $newDel]);
                }
            }

            return response()->json(['status' => true, 'message' => 'Riwayat obrolan telah dibersihkan dari perangkat Anda.'], 200);

        } catch (\Exception $e) {
            Log::error('Error clearConversation: ' . $e->getMessage());
            return response()->json(['status' => false, 'message' => 'Terjadi kesalahan: ' . $e->getMessage()], 500);
        }
    }

    /**
     * Pulihkan Pesan (Restore) - Khusus NIP 19940209202012 dari Monitoring
     */
    public function restoreMessage(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userNip = $user ? ($user->nip ?? '') : $request->input('nip', '');
            if (!$this->isSuperAdmin($userNip)) {
                return response()->json(['status' => false, 'message' => 'Akses ditolak: Hanya NIP 19940209202012 dari monitoring yang berhak memulihkan pesan.'], 403);
            }

            $messageId = $request->input('message_id');
            if (!$messageId) {
                return response()->json(['status' => false, 'message' => 'Message ID wajib disertakan.'], 400);
            }

            DB::table('chat_messages')->where('id', $messageId)->update([
                'is_deleted'        => 0,
                'is_deleted_global' => 0,
                'deleted_for_users' => null,
                'updated_at'        => now()
            ]);

            return response()->json(['status' => true, 'message' => 'Pesan berhasil dipulihkan secara normal.'], 200);

        } catch (\Exception $e) {
            Log::error('Error restoreMessage: ' . $e->getMessage());
            return response()->json(['status' => false, 'message' => 'Terjadi kesalahan: ' . $e->getMessage()], 500);
        }
    }

    /**
     * Dapatkan Daftar Pesan (Messages) dengan Penyaringan Per-User & Mode Monitoring
     */
    public function getMessages(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userId = $user ? $user->id : $request->input('user_id');
            $userNip = $user ? ($user->nip ?? '') : $request->input('nip', '');

            $chatType = $request->query('chat_type', 'personal');
            $targetId = $request->query('target_id');
            $afterId = $request->query('after_id', 0);

            $isSuperAdmin = $this->isSuperAdmin($userNip);

            $query = DB::table('chat_messages')
                ->leftJoin('employees as sender', 'sender.id', '=', 'chat_messages.sender_id');

            if ($chatType === 'opd') {
                $query->where('chat_messages.chat_type', 'opd')->where(function ($q) use ($targetId) {
                    $q->where('chat_messages.opd_id', $targetId)->orWhere('chat_messages.receiver_id', $targetId);
                });
            } else {
                $query->where('chat_messages.chat_type', 'personal')->where(function ($q) use ($targetId, $userId) {
                    $q->where(function ($sq) use ($targetId, $userId) {
                        $sq->where('chat_messages.sender_id', $userId)->where('chat_messages.receiver_id', $targetId);
                    })->orWhere(function ($sq) use ($targetId, $userId) {
                        $sq->where('chat_messages.sender_id', $targetId)->where('chat_messages.receiver_id', $userId);
                    });
                });
            }

            if ($afterId > 0) {
                $query->where('chat_messages.id', '>', $afterId);
            }

            // Jika BUKAN Super Admin, saring pesan:
            // 1. Pesan yang dihapus global kedinasan (is_deleted_global = 1) tidak ditampilkan.
            // 2. Pesan yang dihapus oleh user ini (deleted_for_users contains ,userId,) tidak ditampilkan.
            if (!$isSuperAdmin) {
                $query->where(function ($q) {
                    $q->where('chat_messages.is_deleted_global', 0)->orWhereNull('chat_messages.is_deleted_global');
                });
                $userTag = ',' . $userId . ',';
                $query->where(function ($q) use ($userTag) {
                    $q->whereNull('chat_messages.deleted_for_users')
                      ->orWhere('chat_messages.deleted_for_users', 'NOT LIKE', '%' . $userTag . '%');
                });
            }

            $messages = $query->select(
                'chat_messages.id',
                'chat_messages.sender_id',
                'sender.nama as sender_name',
                'chat_messages.receiver_id',
                'chat_messages.opd_id',
                'chat_messages.chat_type',
                'chat_messages.message',
                'chat_messages.msg_type',
                'chat_messages.file_url',
                'chat_messages.file_name',
                'chat_messages.reply_to_id',
                'chat_messages.reply_to_sender_name',
                'chat_messages.reply_to_message',
                'chat_messages.is_read',
                'chat_messages.is_deleted',
                'chat_messages.is_deleted_global',
                'chat_messages.deleted_for_users',
                'chat_messages.created_at',
                DB::raw("DATE_FORMAT(chat_messages.created_at, '%H:%i') as created_at_formatted"),
                DB::raw("UNIX_TIMESTAMP(chat_messages.created_at) as created_at_ts"),
                DB::raw("CASE WHEN chat_messages.sender_id = " . intval($userId) . " THEN 1 ELSE 0 END as is_me")
            )->orderBy('chat_messages.id', 'ASC')->get();

            // Transformasikan status is_deleted bagi Super Admin
            $result = $messages->map(function ($item) use ($isSuperAdmin) {
                $item->is_me = ($item->is_me == 1);
                $item->is_read = ($item->is_read == 1);
                $item->is_deleted = ($item->is_deleted == 1 || $item->is_deleted_global == 1) ? 1 : 0;
                return $item;
            });

            return response()->json([
                'status'   => true,
                'messages' => $result
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error getMessages: ' . $e->getMessage());
            return response()->json(['status' => false, 'message' => 'Terjadi kesalahan: ' . $e->getMessage(), 'messages' => []], 500);
        }
    }

    /**
     * Simpan Persetujuan Syarat & Ketentuan Chat (Terms & Conditions)
     */
    public function acceptTerms(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userId = $user ? $user->id : $request->input('user_id');
            $accepted = $request->input('accepted', 1);

            if ($userId) {
                DB::table('employees')->where('id', $userId)->update([
                    'chat_terms_accepted' => $accepted ? 1 : 0,
                    'chat_terms_at'       => now()
                ]);
            }

            return response()->json(['status' => true, 'message' => 'Persetujuan syarat dan ketentuan berhasil disimpan.'], 200);

        } catch (\Exception $e) {
            Log::error('Error acceptTerms: ' . $e->getMessage());
            return response()->json(['status' => false, 'message' => 'Gagal menyimpan status terms.'], 500);
        }
    }

    /**
     * Dapatkan Status Persetujuan Syarat & Ketentuan Chat
     */
    public function getTermsStatus(Request $request)
    {
        try {
            $user = $this->getAuthenticatedUser($request);
            $userId = $user ? $user->id : $request->input('user_id');

            $accepted = false;
            if ($userId) {
                $emp = DB::table('employees')->where('id', $userId)->first();
                $accepted = $emp && ($emp->chat_terms_accepted == 1);
            }

            return response()->json([
                'status'         => true,
                'terms_accepted' => $accepted
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error getTermsStatus: ' . $e->getMessage());
            return response()->json(['status' => false, 'terms_accepted' => false], 500);
        }
    }
}

