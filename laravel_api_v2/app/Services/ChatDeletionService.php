<?php

namespace App\Services;

use App\Models\ChatDeletionAuditLog;
use App\Models\ChatMessage;
use App\Policies\ChatMessagePolicy;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Http;
use Illuminate\Support\Facades\Schema;

/**
 * Layanan bisnis penghapusan logis, penghapusan untuk semua, audit log,
 * penghapusan permanen (khusus Administrator Monitoring), serta sinkronisasi realtime.
 */
class ChatDeletionService
{
    public function __construct(
        private readonly ChatMessagePolicy $policy = new ChatMessagePolicy()
    ) {
    }

    /**
     * Menghapus pesan secara logis hanya dari tampilan pelaku (Hapus untuk Saya).
     * Isi pesan dan record pada database tetap dipertahankan.
     */
    public function softDeleteForMe(ChatMessage $message, object $actor, string $action = 'delete_for_me'): array
    {
        if (!$this->policy->deleteForMe($actor, $message)) {
            return [
                'ok' => false,
                'status' => 403,
                'message' => 'Anda tidak memiliki izin untuk menghapus pesan ini.',
            ];
        }

        $actorId = (int) $actor->id;
        $now = date('Y-m-d H:i:s');

        DB::transaction(function () use ($message, $actor, $actorId, $action, $now) {
            $locked = ChatMessage::query()->where('id', $message->id)->lockForUpdate()->first();
            if (!$locked) {
                return;
            }

            $updates = [
                'deleted_for_users' => $this->appendUserIdToList($locked->deleted_for_users, $actorId),
                'deleted_by' => (string) ($actor->nip ?? $actorId),
                'deleted_at' => $now,
            ];

            if ((int) $locked->sender_id === $actorId) {
                $updates['deleted_for_sender'] = 1;
                $updates['is_deleted'] = 1;
            } else {
                $updates['deleted_for_receiver'] = 1;
            }

            DB::table('chat_messages')->where('id', $locked->id)->update($updates);

            $resolvedAction = $action === 'delete_for_me'
                ? ((int) $locked->sender_id === $actorId ? 'delete_for_sender' : 'delete_for_receiver')
                : $action;

            $this->recordAuditLog($locked, $actor, $resolvedAction, false, [
                'scope' => ChatMessage::DELETE_SCOPE_FOR_ME,
            ]);
        });

        $fresh = ChatMessage::query()->find($message->id);

        return [
            'ok' => true,
            'status' => 200,
            'message' => 'Pesan berhasil dihapus dari tampilan Anda.',
            'data' => $fresh,
        ];
    }

    /**
     * Menghapus pesan untuk semua pihak (Hapus untuk Semua).
     * Backend memvalidasi kepemilikan pengirim DAN bahwa pesan belum dibaca oleh penerima.
     */
    public function deleteForEveryone(ChatMessage $message, object $actor): array
    {
        $actorId = (int) ($actor->id ?? 0);

        if ((int) $message->sender_id !== $actorId) {
            return [
                'ok' => false,
                'status' => 403,
                'message' => 'Hanya pengirim pesan yang dapat menghapus pesan untuk semua pihak.',
            ];
        }

        $result = DB::transaction(function () use ($message, $actor, $actorId) {
            $locked = ChatMessage::query()->where('id', $message->id)->lockForUpdate()->first();
            if (!$locked) {
                return [
                    'ok' => false,
                    'status' => 404,
                    'message' => 'Pesan tidak ditemukan.',
                ];
            }

            if ($locked->deleted_for_everyone || $locked->is_deleted_global) {
                return [
                    'ok' => true,
                    'status' => 200,
                    'message' => 'Pesan sudah berstatus dihapus untuk semua.',
                    'data' => $locked,
                    'already_deleted' => true,
                ];
            }

            // Validasi ketat server-side terhadap kondisi dibaca
            if ($locked->is_read || !is_null($locked->read_at)) {
                return [
                    'ok' => false,
                    'status' => 422,
                    'message' => 'Pesan sudah dibaca oleh penerima sehingga tidak dapat dihapus untuk semua pihak.',
                ];
            }

            $now = date('Y-m-d H:i:s');
            DB::table('chat_messages')->where('id', $locked->id)->update([
                'deleted_for_everyone' => 1,
                'is_deleted_global' => 1,
                'deleted_for_sender' => 1,
                'is_deleted' => 1,
                'deleted_for_users' => $this->appendUserIdToList($locked->deleted_for_users, $actorId),
                'deleted_by' => (string) ($actor->nip ?? $actorId),
                'deleted_at' => $now,
            ]);

            $this->recordAuditLog($locked, $actor, 'delete_for_everyone', false, [
                'scope' => ChatMessage::DELETE_SCOPE_FOR_EVERYONE,
            ]);

            $updated = ChatMessage::query()->find($locked->id);

            return [
                'ok' => true,
                'status' => 200,
                'message' => 'Pesan berhasil dihapus untuk pengirim dan penerima.',
                'data' => $updated,
                'already_deleted' => false,
            ];
        });

        if (($result['ok'] ?? false) && empty($result['already_deleted']) && !empty($result['data'])) {
            $msg = $result['data'];
            $this->dispatchRealtimeSync([
                'type' => 'chat_sync',
                'event' => 'deleted_for_everyone',
                'message_id' => (string) $msg->id,
                'chat_type' => (string) $msg->chat_type,
                'sender_id' => (string) $msg->sender_id,
                'receiver_id' => (string) ($msg->receiver_id ?? 0),
                'opd_id' => (string) ($msg->opd_id ?? 0),
                'deleted_by' => (string) ($msg->deleted_by ?? ''),
            ], $msg);
        }

        unset($result['already_deleted']);

        return $result;
    }

    /**
     * Menghapus/membersihkan satu percakapan secara logis dengan cakupan ketat (personal pair atau OPD tertentu).
     * Tidak pernah menyentuh percakapan lain maupun menghapus record database.
     */
    public function clearConversation(object $actor, int $targetId, string $chatType, string $clearType = 'for_me'): array
    {
        $actorId = (int) ($actor->id ?? 0);
        if ($targetId <= 0 || !in_array($chatType, ['personal', 'opd'], true)) {
            return [
                'ok' => false,
                'status' => 422,
                'message' => 'Target percakapan atau tipe chat tidak valid.',
            ];
        }

        if ($chatType === 'opd' && (int) ($actor->opd_id ?? 0) !== $targetId && !ChatMessage::isMonitoringAdminNip($actor->nip ?? null)) {
            // Cek apakah actor pernah berpartisipasi dalam grup OPD ini; jika bukan anggotanya tolak
            $hasMembership = DB::table('chat_messages')
                ->where('chat_type', 'opd')
                ->where('opd_id', $targetId)
                ->where('sender_id', $actorId)
                ->exists();
            if (!$hasMembership) {
                return [
                    'ok' => false,
                    'status' => 403,
                    'message' => 'Anda bukan anggota grup OPD yang dipilih.',
                ];
            }
        }

        $affectedForEveryoneIds = [];
        $affectedForMeCount = 0;
        $now = date('Y-m-d H:i:s');

        DB::transaction(function () use (
            $actor,
            $actorId,
            $targetId,
            $chatType,
            $clearType,
            $now,
            &$affectedForEveryoneIds,
            &$affectedForMeCount
        ) {
            $query = ChatMessage::query()->visibleToUser($actorId);

            if ($chatType === 'personal') {
                $query->where('chat_type', 'personal')
                    ->where(function ($q) use ($actorId, $targetId) {
                        $q->where(function ($pair1) use ($actorId, $targetId) {
                            $pair1->where('sender_id', $actorId)->where('receiver_id', $targetId);
                        })->orWhere(function ($pair2) use ($actorId, $targetId) {
                            $pair2->where('sender_id', $targetId)->where('receiver_id', $actorId);
                        });
                    });
            } else {
                $query->where('chat_type', 'opd')
                    ->where('opd_id', $targetId);
            }

            $messages = $query->lockForUpdate()->get();

            foreach ($messages as $msg) {
                $canDeleteAll = $clearType === ChatMessage::DELETE_SCOPE_FOR_EVERYONE
                    && $msg->canBeDeletedForEveryoneBy($actorId);

                if ($canDeleteAll) {
                    DB::table('chat_messages')->where('id', $msg->id)->update([
                        'deleted_for_everyone' => 1,
                        'is_deleted_global' => 1,
                        'deleted_for_sender' => 1,
                        'is_deleted' => 1,
                        'deleted_for_users' => $this->appendUserIdToList($msg->deleted_for_users, $actorId),
                        'deleted_by' => (string) ($actor->nip ?? $actorId),
                        'deleted_at' => $now,
                    ]);
                    $this->recordAuditLog($msg, $actor, 'clear_for_everyone', false, [
                        'target_id' => $targetId,
                        'chat_type' => $chatType,
                    ]);
                    $affectedForEveryoneIds[] = (int) $msg->id;
                } else {
                    $updates = [
                        'deleted_for_users' => $this->appendUserIdToList($msg->deleted_for_users, $actorId),
                        'deleted_by' => (string) ($actor->nip ?? $actorId),
                        'deleted_at' => $now,
                    ];
                    if ((int) $msg->sender_id === $actorId) {
                        $updates['deleted_for_sender'] = 1;
                        $updates['is_deleted'] = 1;
                    } else {
                        $updates['deleted_for_receiver'] = 1;
                    }
                    DB::table('chat_messages')->where('id', $msg->id)->update($updates);
                    $this->recordAuditLog($msg, $actor, 'clear_for_me', false, [
                        'target_id' => $targetId,
                        'chat_type' => $chatType,
                    ]);
                    $affectedForMeCount++;
                }
            }
        });

        if (!empty($affectedForEveryoneIds)) {
            $this->dispatchRealtimeSync([
                'type' => 'chat_sync',
                'event' => 'conversation_deleted_for_everyone',
                'message_ids' => implode(',', $affectedForEveryoneIds),
                'chat_type' => $chatType,
                'sender_id' => (string) $actorId,
                'receiver_id' => $chatType === 'personal' ? (string) $targetId : '0',
                'opd_id' => $chatType === 'opd' ? (string) $targetId : '0',
            ], null, [
                [
                    'chat_type' => $chatType,
                    'sender_id' => $actorId,
                    'receiver_id' => $chatType === 'personal' ? $targetId : 0,
                    'opd_id' => $chatType === 'opd' ? $targetId : 0,
                ],
            ]);
        }

        return [
            'ok' => true,
            'status' => 200,
            'message' => 'Percakapan berhasil dibersihkan secara logis.',
            'deleted_for_everyone_count' => count($affectedForEveryoneIds),
            'deleted_for_me_count' => $affectedForMeCount,
            'deleted_for_everyone_ids' => $affectedForEveryoneIds,
        ];
    }

    /**
     * Menghapus permanen 1 atau beberapa pesan secara fisik (SQL DELETE) beserta pembersihan relasi/lampiran.
     * HANYA boleh dipanggil oleh Administrator Monitoring Chat (NIP 199402092020121005).
     */
    public function permanentDeleteMessages(array $messageIds, object $admin): array
    {
        if (!$this->policy->permanentDelete($admin)) {
            return [
                'ok' => false,
                'status' => 403,
                'message' => 'Akses ditolak. Penghapusan permanen hanya untuk NIP 199402092020121005.',
            ];
        }

        $ids = array_values(array_unique(array_filter(
            array_map('intval', $messageIds),
            fn (int $id) => $id > 0
        )));

        if (empty($ids)) {
            return [
                'ok' => false,
                'status' => 422,
                'message' => 'Pilih minimal satu ID pesan yang valid untuk dihapus permanen.',
            ];
        }

        $deletedRecords = [];
        $filesToUnlink = [];

        DB::transaction(function () use ($ids, $admin, &$deletedRecords, &$filesToUnlink) {
            $messages = ChatMessage::query()->whereIn('id', $ids)->lockForUpdate()->get();
            if ($messages->isEmpty()) {
                return;
            }

            foreach ($messages as $msg) {
                $this->recordAuditLog($msg, $admin, 'permanent_delete', true, [
                    'previous_delete_status' => $msg->resolveDeleteStatus(),
                    'previous_deleted_by' => $msg->deleted_by,
                    'previous_deleted_at' => $msg->deleted_at ? (string) $msg->deleted_at : null,
                ]);

                $deletedRecords[] = [
                    'id' => (int) $msg->id,
                    'chat_type' => (string) $msg->chat_type,
                    'sender_id' => (int) $msg->sender_id,
                    'receiver_id' => (int) ($msg->receiver_id ?? 0),
                    'opd_id' => (int) ($msg->opd_id ?? 0),
                ];

                if (!empty($msg->file_url)) {
                    $filesToUnlink[] = (string) $msg->file_url;
                }
            }

            // Netralisir reply_to_id pada pesan lain agar tidak meninggalkan dangling reference atau merusak thread lain
            $replyDetachPayload = ['reply_to_id' => null];
            if (Schema::hasColumn('chat_messages', 'reply_to_message')) {
                $replyDetachPayload['reply_to_message'] = '[Pesan rujukan telah dihapus permanen]';
            }
            DB::table('chat_messages')
                ->whereIn('reply_to_id', $ids)
                ->whereNotIn('id', $ids)
                ->update($replyDetachPayload);

            // Hapus data relasi lampiran/read receipt opsional jika tabel tersedia
            foreach (['chat_message_attachments', 'chat_attachments', 'chat_message_reads'] as $childTable) {
                if (Schema::hasTable($childTable) && Schema::hasColumn($childTable, 'message_id')) {
                    DB::table($childTable)->whereIn('message_id', $ids)->delete();
                }
            }

            // Hanya hapus file fisik jika tidak direferensikan pesan lain yang masih bertahan
            $uniqueFiles = array_values(array_unique(array_filter($filesToUnlink)));
            $filesToUnlink = [];
            foreach ($uniqueFiles as $fileUrl) {
                $stillReferenced = DB::table('chat_messages')
                    ->where('file_url', $fileUrl)
                    ->whereNotIn('id', $ids)
                    ->exists();
                if (!$stillReferenced) {
                    $filesToUnlink[] = $fileUrl;
                }
            }

            // Penghapusan fisik permanen dari database
            DB::table('chat_messages')->whereIn('id', $ids)->delete();
        });

        if (empty($deletedRecords)) {
            return [
                'ok' => false,
                'status' => 404,
                'message' => 'Pesan yang dipilih tidak ditemukan di database.',
            ];
        }

        // Unlink file fisik di luar transaksi DB setelah commit berhasil
        foreach ($filesToUnlink as $fileUrl) {
            $this->deleteLocalAttachmentFile($fileUrl);
        }

        $deletedIdList = array_column($deletedRecords, 'id');
        $first = $deletedRecords[0];
        $this->dispatchRealtimeSync([
            'type' => 'chat_sync',
            'event' => 'permanent_delete',
            'message_ids' => implode(',', $deletedIdList),
            'chat_type' => $first['chat_type'],
            'sender_id' => (string) $first['sender_id'],
            'receiver_id' => (string) $first['receiver_id'],
            'opd_id' => (string) $first['opd_id'],
        ], null, $deletedRecords);

        return [
            'ok' => true,
            'status' => 200,
            'message' => count($deletedIdList) . ' pesan berhasil dihapus secara permanen dari database.',
            'deleted_count' => count($deletedIdList),
            'deleted_ids' => $deletedIdList,
        ];
    }

    /**
     * Memulihkan pesan yang terhapus secara logis (khusus NIP Monitoring Admin).
     */
    public function restoreSoftDeletedMessage(ChatMessage $message, object $admin): array
    {
        if (!$this->policy->restoreMessage($admin, $message)) {
            return [
                'ok' => false,
                'status' => 403,
                'message' => 'Akses ditolak.',
            ];
        }

        DB::transaction(function () use ($message, $admin) {
            $locked = ChatMessage::query()->where('id', $message->id)->lockForUpdate()->first();
            if (!$locked) {
                return;
            }

            DB::table('chat_messages')->where('id', $locked->id)->update([
                'deleted_for_sender' => 0,
                'deleted_for_receiver' => 0,
                'deleted_for_everyone' => 0,
                'deleted_for_users' => null,
                'is_deleted' => 0,
                'is_deleted_global' => 0,
                'deleted_by' => null,
                'deleted_at' => null,
            ]);

            $this->recordAuditLog($locked, $admin, 'restore_message', false, [
                'restored_by_admin' => true,
            ]);
        });

        return [
            'ok' => true,
            'status' => 200,
            'message' => 'Pesan berhasil dipulihkan untuk percakapan.',
            'data' => ChatMessage::query()->find($message->id),
        ];
    }

    private function appendUserIdToList(?string $currentList, int $userId): string
    {
        $raw = trim((string) $currentList);
        $ids = array_values(array_unique(array_filter(
            explode(',', $raw),
            fn (string $v) => trim($v) !== '' && ctype_digit(trim($v))
        )));

        $userStr = (string) $userId;
        if (!in_array($userStr, $ids, true)) {
            $ids[] = $userStr;
        }

        return ',' . implode(',', $ids) . ',';
    }

    private function recordAuditLog(
        ChatMessage $message,
        object $actor,
        string $action,
        bool $isPermanent,
        array $metadata = []
    ): void {
        if (!Schema::hasTable('chat_deletion_audit_logs')) {
            return;
        }

        DB::table('chat_deletion_audit_logs')->insert([
            'message_id' => (int) $message->id,
            'chat_type' => (string) ($message->chat_type ?? 'personal'),
            'sender_id' => (int) $message->sender_id,
            'receiver_id' => $message->receiver_id ? (int) $message->receiver_id : null,
            'opd_id' => $message->opd_id ? (int) $message->opd_id : null,
            'action' => $action,
            'actor_employee_id' => isset($actor->id) ? (int) $actor->id : null,
            'actor_nip' => (string) ($actor->nip ?? ''),
            'actor_name' => (string) ($actor->nama ?? $actor->name ?? ''),
            'message_snapshot' => ChatMessage::decryptText($message->message),
            'file_url_snapshot' => $message->file_url,
            'was_read' => ($message->is_read || !is_null($message->read_at)) ? 1 : 0,
            'is_permanent' => $isPermanent ? 1 : 0,
            'metadata' => json_encode($metadata),
            'created_at' => date('Y-m-d H:i:s'),
        ]);
    }

    private function deleteLocalAttachmentFile(string $fileUrl): void
    {
        $pathPart = parse_url($fileUrl, PHP_URL_PATH) ?: $fileUrl;
        $relative = ltrim(str_replace('..', '', $pathPart), '/');
        if ($relative === '') {
            return;
        }

        $candidates = [];
        if (function_exists('storage_path')) {
            $candidates[] = storage_path('app/public/' . preg_replace('#^storage/#', '', $relative));
        }
        if (function_exists('public_path')) {
            $candidates[] = public_path($relative);
        }

        foreach ($candidates as $candidate) {
            if (is_string($candidate) && $candidate !== '' && is_file($candidate)) {
                @unlink($candidate);
            }
        }
    }

    /**
     * Best-effort data-only FCM dispatch agar client yang aktif langsung sinkron tanpa delay.
     */
    private function dispatchRealtimeSync(array $dataPayload, ?ChatMessage $message = null, array $fallbackTargets = []): void
    {
        try {
            $targetEmployeeIds = [];
            $targetOpdIds = [];
            if ($message !== null) {
                $targetEmployeeIds[] = (int) $message->sender_id;
                if ($message->chat_type === 'personal' && $message->receiver_id) {
                    $targetEmployeeIds[] = (int) $message->receiver_id;
                } elseif ($message->chat_type === 'opd' && $message->opd_id) {
                    $targetOpdIds[] = (int) $message->opd_id;
                }
            }

            foreach ($fallbackTargets as $target) {
                if (!empty($target['sender_id'])) {
                    $targetEmployeeIds[] = (int) $target['sender_id'];
                }
                if (($target['chat_type'] ?? 'personal') === 'personal' && !empty($target['receiver_id'])) {
                    $targetEmployeeIds[] = (int) $target['receiver_id'];
                } elseif (($target['chat_type'] ?? '') === 'opd' && !empty($target['opd_id'])) {
                    $targetOpdIds[] = (int) $target['opd_id'];
                }
            }

            $targetOpdIds = array_values(array_unique(array_filter($targetOpdIds)));
            if (!empty($targetOpdIds) && Schema::hasTable('employees')) {
                $opdMemberIds = DB::table('employees')
                    ->whereIn('opd_id', $targetOpdIds)
                    ->pluck('id')
                    ->all();
                $targetEmployeeIds = array_merge($targetEmployeeIds, $opdMemberIds);
            }

            $targetEmployeeIds = array_values(array_unique(array_filter($targetEmployeeIds)));
            if (empty($targetEmployeeIds)) {
                return;
            }

            if (class_exists(\App\Helpers\FcmV1Helper::class)) {
                $helperClass = \App\Helpers\FcmV1Helper::class;
                if (method_exists($helperClass, 'sendDataToEmployee')) {
                    foreach ($targetEmployeeIds as $empId) {
                        $helperClass::sendDataToEmployee($empId, $dataPayload);
                    }
                    return;
                }
            }

            $serverKey = function_exists('config') ? (string) config('services.fcm.key', '') : '';
            if ($serverKey === '' || !Schema::hasTable('employees')) {
                return;
            }

            $tokenColumn = Schema::hasColumn('employees', 'fcm_token')
                ? 'fcm_token'
                : (Schema::hasColumn('employees', 'device_token') ? 'device_token' : null);

            if ($tokenColumn === null) {
                return;
            }

            $tokens = DB::table('employees')
                ->whereIn('id', $targetEmployeeIds)
                ->whereNotNull($tokenColumn)
                ->pluck($tokenColumn)
                ->filter(fn ($t) => is_string($t) && trim($t) !== '')
                ->values()
                ->all();

            if (empty($tokens)) {
                return;
            }

            Http::withHeaders([
                'Authorization' => 'key=' . $serverKey,
                'Content-Type' => 'application/json',
            ])->timeout(3)->post('https://fcm.googleapis.com/fcm/send', [
                'registration_ids' => $tokens,
                'priority' => 'high',
                'data' => $dataPayload,
            ]);
        } catch (\Throwable $e) {
            // Sinkronisasi realtime bersifat best-effort; kegagalan jaringan push tidak membatalkan transaksi DB.
        }
    }
}
