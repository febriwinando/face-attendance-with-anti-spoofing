<?php

namespace App\Http\Controllers\Chat;

use App\Http\Controllers\Controller;
use App\Http\Middleware\EnsureChatMonitoringAdmin;
use App\Models\ChatDeletionAuditLog;
use App\Models\ChatMessage;
use App\Policies\ChatMessagePolicy;
use App\Services\ChatDeletionService;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

class ChatController extends Controller
{
    public function __construct(
        private readonly ChatDeletionService $deletionService = new ChatDeletionService(),
        private readonly ChatMessagePolicy $policy = new ChatMessagePolicy()
    ) {
    }

    /**
     * Mengambil pesan untuk ruang obrolan pengguna aktif (personal atau grup OPD).
     * Otomatis memfilter pesan yang dihapus untuk pengguna tersebut atau dihapus untuk semua.
     */
    public function getMessages(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $chatType = strtolower(trim((string) $request->input('chat_type', 'personal')));
        $targetId = (int) $request->input('target_id', 0);
        $afterId = max(0, (int) $request->input('after_id', 0));

        if ($targetId <= 0 || !in_array($chatType, ['personal', 'opd'], true)) {
            return response()->json([
                'success' => false,
                'message' => 'Parameter percakapan tidak valid.',
                'data' => [],
            ], 422);
        }

        if ($chatType === 'opd' && (int) ($actor->opd_id ?? 0) !== $targetId && !ChatMessage::isMonitoringAdminNip($actor->nip ?? null)) {
            return response()->json([
                'success' => false,
                'message' => 'Anda tidak memiliki akses ke grup OPD ini.',
                'data' => [],
            ], 403);
        }

        $actorId = (int) $actor->id;

        // Tandai pesan masuk sebagai sudah dibaca (hanya jika pesan belum dihapus untuk semua)
        $now = date('Y-m-d H:i:s');
        if ($chatType === 'personal') {
            DB::table('chat_messages')
                ->where('chat_type', 'personal')
                ->where('sender_id', $targetId)
                ->where('receiver_id', $actorId)
                ->where('is_read', 0)
                ->where('deleted_for_everyone', 0)
                ->where('is_deleted_global', 0)
                ->update([
                    'is_read' => 1,
                    'read_at' => DB::raw("COALESCE(read_at, '" . $now . "')"),
                ]);
        } else {
            DB::table('chat_messages')
                ->where('chat_type', 'opd')
                ->where('opd_id', $targetId)
                ->where('sender_id', '!=', $actorId)
                ->where('is_read', 0)
                ->where('deleted_for_everyone', 0)
                ->where('is_deleted_global', 0)
                ->update([
                    'is_read' => 1,
                    'read_at' => DB::raw("COALESCE(read_at, '" . $now . "')"),
                ]);
        }

        $beforeId = max(0, (int) $request->input('before_id', 0));

        if ($chatType === 'opd'
            && Schema::hasTable('employee_chat_privacies')
            && Schema::hasColumn('employee_chat_privacies', 'last_read_opd_msg_id')
        ) {
            $maxOpdId = (int) (DB::table('chat_messages')
                ->where('chat_type', 'opd')
                ->where('opd_id', $targetId)
                ->max('id') ?? 0);
            if ($maxOpdId > 0) {
                $existingPriv = DB::table('employee_chat_privacies')->where('employee_id', $actorId)->first();
                if ($existingPriv && $maxOpdId > (int) ($existingPriv->last_read_opd_msg_id ?? 0)) {
                    DB::table('employee_chat_privacies')
                        ->where('employee_id', $actorId)
                        ->update(['last_read_opd_msg_id' => $maxOpdId, 'updated_at' => $now]);
                }
            }
        }

        $query = DB::table('chat_messages as cm')
            ->leftJoin('employees as s', 's.id', '=', 'cm.sender_id')
            ->leftJoin('chat_messages as rp', 'rp.id', '=', 'cm.reply_to_id')
            ->leftJoin('employees as rps', 'rps.id', '=', 'rp.sender_id')
            ->select([
                'cm.*',
                's.nama as joined_sender_name',
                's.nip as joined_sender_nip',
                'rp.message as joined_reply_message',
                'rps.nama as joined_reply_sender_name',
            ])
            ->where('cm.chat_type', $chatType);

        if ($chatType === 'personal') {
            $query->where(function ($q) use ($actorId, $targetId) {
                $q->where(function ($p1) use ($actorId, $targetId) {
                    $p1->where('cm.sender_id', $actorId)->where('cm.receiver_id', $targetId);
                })->orWhere(function ($p2) use ($actorId, $targetId) {
                    $p2->where('cm.sender_id', $targetId)->where('cm.receiver_id', $actorId);
                });
            });
        } else {
            $query->where('cm.opd_id', $targetId);
        }

        if ($afterId > 0) {
            $query->where('cm.id', '>', $afterId);
        } elseif ($beforeId > 0) {
            $query->where('cm.id', '<', $beforeId);
        }

        // Terapkan filter visibility logis per-user dan delete_for_everyone
        $token = '%,' . $actorId . ',%';
        $query->where('cm.deleted_for_everyone', 0)
            ->where('cm.is_deleted_global', 0)
            ->whereRaw('(cm.deleted_for_users IS NULL OR cm.deleted_for_users NOT LIKE ?)', [$token])
            ->where(function ($q) use ($actorId) {
                $q->where('cm.sender_id', '!=', $actorId)
                    ->orWhere(function ($sq) {
                        $sq->where('cm.deleted_for_sender', 0)
                            ->where('cm.is_deleted', 0);
                    });
            })
            ->where(function ($q) use ($actorId) {
                $q->where('cm.chat_type', '!=', 'personal')
                    ->orWhereNull('cm.receiver_id')
                    ->orWhere('cm.receiver_id', '!=', $actorId)
                    ->orWhere('cm.deleted_for_receiver', 0);
            });

        $rows = $query->orderBy('cm.id', 'asc')->get();

        $formatted = [];
        foreach ($rows as $row) {
            $formatted[] = $this->formatRegularMessage($row, $actorId);
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => 'Daftar pesan berhasil dimuat.',
            'messages' => $formatted,
            'data' => $formatted,
        ]);
    }

    /**
     * Menandai pesan masuk sebagai sudah dibaca secara eksplisit.
     */
    public function markAsRead(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $targetId = (int) $request->input('target_id', 0);
        $chatType = strtolower(trim((string) $request->input('chat_type', 'personal')));
        if ($targetId <= 0 || !in_array($chatType, ['personal', 'opd'], true)) {
            return response()->json([
                'success' => true,
                'message' => 'Tidak ada pembaruan status baca.',
                'updated_count' => 0,
            ]);
        }

        $now = date('Y-m-d H:i:s');
        if ($chatType === 'personal') {
            $updated = DB::table('chat_messages')
                ->where('chat_type', 'personal')
                ->where('sender_id', $targetId)
                ->where('receiver_id', (int) $actor->id)
                ->where('is_read', 0)
                ->where('deleted_for_everyone', 0)
                ->where('is_deleted_global', 0)
                ->update([
                    'is_read' => 1,
                    'read_at' => DB::raw("COALESCE(read_at, '" . $now . "')"),
                ]);
        } else {
            if ((int) ($actor->opd_id ?? 0) !== $targetId && !ChatMessage::isMonitoringAdminNip($actor->nip ?? null)) {
                return response()->json([
                    'success' => false,
                    'message' => 'Anda tidak memiliki akses ke grup OPD ini.',
                ], 403);
            }
            $updated = DB::table('chat_messages')
                ->where('chat_type', 'opd')
                ->where('opd_id', $targetId)
                ->where('sender_id', '!=', (int) $actor->id)
                ->where('is_read', 0)
                ->where('deleted_for_everyone', 0)
                ->where('is_deleted_global', 0)
                ->update([
                    'is_read' => 1,
                    'read_at' => DB::raw("COALESCE(read_at, '" . $now . "')"),
                ]);
        }

        return response()->json([
            'success' => true,
            'message' => 'Status baca diperbarui.',
            'updated_count' => (int) $updated,
            'read_at' => $now,
        ]);
    }

    /**
     * Menghapus pesan secara logis dari halaman chat reguler maupun daftar chat.
     * Mendukung: 'for_me' (default) dan 'for_everyone' / 'global' (hanya jika milik pengirim & belum dibaca).
     * Tidak pernah menghapus record database dan menolak hapus permanen dari endpoint reguler.
     */
    public function deleteMessage(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $messageId = (int) $request->input('message_id', 0);
        $deleteType = strtolower(trim((string) $request->input('delete_type', ChatMessage::DELETE_SCOPE_FOR_ME)));

        if ($messageId <= 0) {
            return response()->json([
                'success' => false,
                'message' => 'ID pesan wajib diisi.',
            ], 422);
        }

        if (in_array($deleteType, ['hard_delete', 'permanent', ChatMessage::DELETE_SCOPE_PERMANENT], true)) {
            return response()->json([
                'success' => false,
                'message' => 'Penghapusan permanen tidak diperbolehkan dari halaman obrolan. Gunakan halaman Monitoring Administrator.',
            ], 403);
        }

        $message = ChatMessage::query()->find($messageId);
        if (!$message) {
            return response()->json([
                'success' => false,
                'message' => 'Pesan tidak ditemukan.',
            ], 404);
        }

        if (in_array($deleteType, [ChatMessage::DELETE_SCOPE_FOR_EVERYONE, 'global'], true)) {
            $result = $this->deletionService->deleteForEveryone($message, $actor);
        } else {
            $result = $this->deletionService->softDeleteForMe($message, $actor);
        }

        $ok = (bool) ($result['ok'] ?? false);
        $formattedData = (!empty($result['data']) && is_object($result['data']))
            ? $this->formatRegularMessage($result['data'], (int) $actor->id)
            : null;

        return response()->json([
            'status' => $ok,
            'success' => $ok,
            'message' => (string) ($result['message'] ?? ''),
            'data' => $formattedData,
        ], (int) ($result['status'] ?? 200));
    }

    /**
     * Endpoint khusus untuk menghapus pesan untuk semua pihak (Hapus untuk Semua).
     */
    public function deleteForEveryone(Request $request): JsonResponse
    {
        $request->merge(['delete_type' => ChatMessage::DELETE_SCOPE_FOR_EVERYONE]);
        return $this->deleteMessage($request);
    }

    /**
     * Membersihkan satu percakapan secara logis dengan cakupan ketat tanpa menyentuh percakapan lain.
     */
    public function clearConversation(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $targetId = (int) $request->input('target_id', 0);
        $chatType = strtolower(trim((string) $request->input('chat_type', 'personal')));
        $clearType = strtolower(trim((string) $request->input('clear_type', ChatMessage::DELETE_SCOPE_FOR_ME)));
        if ($clearType === 'global') {
            $clearType = ChatMessage::DELETE_SCOPE_FOR_EVERYONE;
        }

        $result = $this->deletionService->clearConversation($actor, $targetId, $chatType, $clearType);

        $ok = (bool) ($result['ok'] ?? false);
        return response()->json([
            'status' => $ok,
            'success' => $ok,
            'message' => (string) ($result['message'] ?? ''),
            'deleted_for_everyone_count' => $result['deleted_for_everyone_count'] ?? 0,
            'deleted_for_me_count' => $result['deleted_for_me_count'] ?? 0,
            'deleted_for_everyone_ids' => $result['deleted_for_everyone_ids'] ?? [],
        ], (int) ($result['status'] ?? 200));
    }

    /**
     * Monitoring Daftar Percakapan (Khusus Administrator NIP 199402092020121005).
     */
    public function monitoringConversations(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->viewMonitoring($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $search = trim((string) $request->input('search', ''));
        $opdId = trim((string) $request->input('opd_id', ''));
        $employeeId = trim((string) $request->input('employee_id', ''));
        $nipFilter = trim((string) $request->input('nip', ''));
        $statusFilter = strtolower(trim((string) $request->input('status', 'all')));
        $chatTypeFilter = strtolower(trim((string) $request->input('chat_type', 'all')));
        $date = trim((string) $request->input('date', ''));
        $dateFrom = trim((string) $request->input('date_from', ''));
        $dateTo = trim((string) $request->input('date_to', ''));
        $page = max(1, (int) $request->input('page', 1));
        $perPage = min(100, max(1, (int) $request->input('per_page', 30)));

        $query = $this->buildMonitoringBaseQuery($request, true);
        $rawRows = $query->orderBy('cm.id', 'desc')->get();
        $rows = $this->filterRowsBySearchTerm($rawRows, $search);

        $threads = [];
        foreach ($rows as $row) {
            $type = (string) ($row->chat_type ?? 'personal');
            if ($type === 'opd') {
                $key = 'opd_' . (int) $row->opd_id;
            } else {
                $minId = min((int) $row->sender_id, (int) $row->receiver_id);
                $maxId = max((int) $row->sender_id, (int) $row->receiver_id);
                $key = 'personal_' . $minId . '_' . $maxId;
            }

            $deleteStatus = $this->resolveDeleteStatusFromRow($row);
            $isDeletedAny = $deleteStatus !== 'active';
            $isUnread = !(bool) $row->is_read && empty($row->read_at);

            if (!isset($threads[$key])) {
                $senderOpdName = (string) ($row->sender_opd_name ?? '');
                $receiverOpdName = (string) ($row->receiver_opd_name ?? '');
                $groupOpdName = (string) ($row->group_opd_name ?? '');

                if ($type === 'opd') {
                    $senderDisplay = 'Grup OPD: ' . ($groupOpdName !== '' ? $groupOpdName : ('OPD #' . $row->opd_id));
                    $receiverDisplay = 'Seluruh Pegawai OPD';
                    $senderNip = '';
                    $receiverNip = '';
                    $opdName = $groupOpdName;
                    $resolvedOpdId = (int) $row->opd_id;
                    $participantAId = (int) $row->opd_id;
                    $participantBId = 0;
                } else {
                    $senderDisplay = (string) ($row->sender_name ?? 'Pegawai #' . $row->sender_id);
                    $receiverDisplay = (string) ($row->receiver_name ?? 'Pegawai #' . $row->receiver_id);
                    $senderNip = (string) ($row->sender_nip ?? '');
                    $receiverNip = (string) ($row->receiver_nip ?? '');
                    $opdName = $senderOpdName !== '' ? $senderOpdName : $receiverOpdName;
                    $resolvedOpdId = (int) ($row->sender_opd_id ?? $row->receiver_opd_id ?? 0);
                    $participantAId = min((int) $row->sender_id, (int) $row->receiver_id);
                    $participantBId = max((int) $row->sender_id, (int) $row->receiver_id);
                    if ($participantAId !== (int) $row->sender_id) {
                        $senderDisplay = (string) ($row->receiver_name ?? 'Pegawai #' . $row->receiver_id);
                        $receiverDisplay = (string) ($row->sender_name ?? 'Pegawai #' . $row->sender_id);
                        $senderNip = (string) ($row->receiver_nip ?? '');
                        $receiverNip = (string) ($row->sender_nip ?? '');
                    }
                }

                $preview = trim(ChatMessage::decryptText((string) ($row->message ?? '')));
                if ($preview === '' && !empty($row->file_name)) {
                    $preview = '[Lampiran: ' . $row->file_name . ']';
                } elseif ($preview === '' && !empty($row->msg_type) && $row->msg_type !== 'text') {
                    $preview = '[Media ' . strtoupper((string) $row->msg_type) . ']';
                }

                $createdAtStr = (string) ($row->created_at ?? '');
                $threads[$key] = [
                    'pair_key' => $key,
                    'thread_key' => $key,
                    'chat_type' => $type,
                    'sender_id' => $participantAId,
                    'receiver_id' => $participantBId,
                    'opd_id' => $resolvedOpdId,
                    'sender_name' => $senderDisplay,
                    'sender_nip' => $senderNip,
                    'sender_opd' => $senderOpdName !== '' ? $senderOpdName : $opdName,
                    'receiver_name' => $receiverDisplay,
                    'receiver_nip' => $receiverNip,
                    'receiver_opd' => $receiverOpdName !== '' ? $receiverOpdName : $opdName,
                    'opd_name' => $opdName,
                    'last_message' => $preview,
                    'last_message_id' => (int) $row->id,
                    'last_time' => $createdAtStr !== '' ? date('d/m H:i', strtotime($createdAtStr)) : '',
                    'last_time_ts' => $createdAtStr !== '' ? (int) strtotime($createdAtStr) : 0,
                    'last_message_at' => $createdAtStr,
                    'last_message_status' => $deleteStatus,
                    'last_message_is_deleted' => $isDeletedAny ? 1 : 0,
                    'total_messages' => 0,
                    'deleted_count' => 0,
                    'unread_count' => 0,
                    'deleted_for_everyone_count' => 0,
                ];
            }

            $threads[$key]['total_messages']++;
            if ($isDeletedAny) {
                $threads[$key]['deleted_count']++;
            }
            if ($deleteStatus === 'deleted_for_everyone') {
                $threads[$key]['deleted_for_everyone_count']++;
            }
            if ($isUnread) {
                $threads[$key]['unread_count']++;
            }
        }

        $threadList = array_values($threads);
        $total = count($threadList);
        $offset = ($page - 1) * $perPage;
        $pagedThreads = array_slice($threadList, $offset, $perPage);

        $opdList = [];
        if (Schema::hasTable('opds')) {
            $opdList = DB::table('opds')->select(['id', 'opd'])->orderBy('opd', 'asc')->get()->all();
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => 'Data monitoring percakapan berhasil dimuat.',
            'threads' => $pagedThreads,
            'data' => $pagedThreads,
            'opd_list' => $opdList,
            'pagination' => [
                'current_page' => $page,
                'per_page' => $perPage,
                'total' => $total,
                'last_page' => max(1, (int) ceil($total / $perPage)),
            ],
            'filters_applied' => [
                'search' => $search,
                'opd_id' => $opdId,
                'employee_id' => $employeeId,
                'nip' => $nipFilter,
                'status' => $statusFilter,
                'chat_type' => $chatTypeFilter,
                'date' => $date,
                'date_from' => $dateFrom,
                'date_to' => $dateTo,
            ],
        ]);
    }

    /**
     * Monitoring seluruh pesan (paginasi + filter lengkap) tanpa mengubah status read_at/is_read.
     */
    public function monitoringMessages(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->viewMonitoring($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $page = max(1, (int) $request->input('page', 1));
        $perPage = min(100, max(1, (int) $request->input('per_page', 25)));
        $search = trim((string) $request->input('search', ''));

        if ($search !== '') {
            $query = $this->buildMonitoringBaseQuery($request, true);
            $matchedAll = $this->filterRowsBySearchTerm($query->orderBy('cm.id', 'desc')->get(), $search);
            $matchedArray = is_array($matchedAll) ? $matchedAll : $matchedAll->all();
            $total = count($matchedArray);
            $rows = array_slice($matchedArray, ($page - 1) * $perPage, $perPage);
        } else {
            $query = $this->buildMonitoringBaseQuery($request, true);
            $total = (clone $query)->count();
            $rows = $query->orderBy('cm.id', 'desc')
                ->offset(($page - 1) * $perPage)
                ->limit($perPage)
                ->get()
                ->all();
        }

        $msgIds = array_map(fn ($r) => (int) $r->id, $rows);
        $auditByMsgId = $this->loadAuditHistoryMap($msgIds);

        $data = [];
        foreach ($rows as $row) {
            $data[] = $this->formatMonitoringMessage($row, $auditByMsgId[(int) $row->id] ?? []);
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => 'Data monitoring seluruh pesan berhasil dimuat.',
            'data' => $data,
            'pagination' => [
                'current_page' => $page,
                'per_page' => $perPage,
                'total' => $total,
                'last_page' => max(1, (int) ceil($total / $perPage)),
            ],
        ]);
    }

    /**
     * Detail percakapan penuh untuk Administrator Monitoring (menampilkan pesan aktif & terhapus secara utuh).
     * Tidak mengubah status is_read atau read_at pegawai.
     */
    public function monitoringConversationDetail(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->viewMonitoring($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $chatType = strtolower(trim((string) $request->input('chat_type', 'personal')));
        $participantA = (int) ($request->input('participant_a') ?? $request->input('sender_id') ?? 0);
        $participantB = (int) ($request->input('participant_b') ?? $request->input('receiver_id') ?? 0);
        $opdId = (int) ($request->input('opd_id') ?? $request->input('target_id') ?? 0);
        $page = max(1, (int) $request->input('page', 1));
        $perPage = min(200, max(1, (int) $request->input('per_page', 50)));

        $query = DB::table('chat_messages as cm')
            ->leftJoin('employees as s', 's.id', '=', 'cm.sender_id')
            ->leftJoin('employees as r', 'r.id', '=', 'cm.receiver_id')
            ->leftJoin('opds as so', 'so.id', '=', 's.opd_id')
            ->leftJoin('opds as ro', 'ro.id', '=', 'r.opd_id')
            ->leftJoin('opds as go', 'go.id', '=', 'cm.opd_id')
            ->select([
                'cm.*',
                's.nama as sender_name',
                's.nip as sender_nip',
                's.opd_id as sender_opd_id',
                'so.opd as sender_opd_name',
                'r.nama as receiver_name',
                'r.nip as receiver_nip',
                'r.opd_id as receiver_opd_id',
                'ro.opd as receiver_opd_name',
                'go.opd as group_opd_name',
            ]);

        if ($chatType === 'opd') {
            $resolvedOpd = $opdId > 0 ? $opdId : $participantA;
            if ($resolvedOpd <= 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'ID OPD tidak valid untuk detail percakapan grup.',
                ], 422);
            }
            $query->where('cm.chat_type', 'opd')->where('cm.opd_id', $resolvedOpd);
        } else {
            if ($participantA <= 0 || $participantB <= 0) {
                return response()->json([
                    'success' => false,
                    'message' => 'ID kedua peserta percakapan wajib diisi.',
                ], 422);
            }
            $query->where('cm.chat_type', 'personal')
                ->where(function ($q) use ($participantA, $participantB) {
                    $q->where(function ($p1) use ($participantA, $participantB) {
                        $p1->where('cm.sender_id', $participantA)->where('cm.receiver_id', $participantB);
                    })->orWhere(function ($p2) use ($participantA, $participantB) {
                        $p2->where('cm.sender_id', $participantB)->where('cm.receiver_id', $participantA);
                    });
                });
        }

        $statusFilter = strtolower(trim((string) $request->input('status', 'all')));
        if ($statusFilter !== '' && $statusFilter !== 'all') {
            $this->applyMonitoringStatusFilter($query, $statusFilter);
        }

        $search = trim((string) $request->input('search', ''));
        if ($search !== '') {
            $matchedAll = $this->filterRowsBySearchTerm($query->orderBy('cm.id', 'asc')->get(), $search);
            $matchedArray = is_array($matchedAll) ? $matchedAll : $matchedAll->all();
            $total = count($matchedArray);
            $rows = array_slice($matchedArray, ($page - 1) * $perPage, $perPage);
        } else {
            $total = (clone $query)->count();
            $rows = $query->orderBy('cm.id', 'asc')
                ->offset(($page - 1) * $perPage)
                ->limit($perPage)
                ->get()
                ->all();
        }

        $msgIds = array_map(fn ($r) => (int) $r->id, $rows);
        $auditByMsgId = $this->loadAuditHistoryMap($msgIds);

        $messages = [];
        foreach ($rows as $row) {
            $messages[] = $this->formatMonitoringMessage($row, $auditByMsgId[(int) $row->id] ?? []);
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => 'Detail percakapan monitoring berhasil dimuat.',
            'data' => $messages,
            'pagination' => [
                'current_page' => $page,
                'per_page' => $perPage,
                'total' => $total,
                'last_page' => max(1, (int) ceil($total / $perPage)),
            ],
        ]);
    }

    /**
     * Penghapusan permanen satu atau beberapa pesan secara fisik dari database (Khusus NIP 199402092020121005).
     */
    public function monitoringPermanentDelete(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->permanentDelete($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $ids = [];
        $rawIds = $request->input('message_ids');
        if (is_array($rawIds)) {
            $ids = $rawIds;
        } elseif (is_string($rawIds) && trim($rawIds) !== '') {
            $ids = explode(',', $rawIds);
        }

        $singleId = (int) $request->input('message_id', 0);
        if ($singleId > 0) {
            $ids[] = $singleId;
        }

        $result = $this->deletionService->permanentDeleteMessages($ids, $admin);

        $ok = (bool) ($result['ok'] ?? false);
        return response()->json([
            'status' => $ok,
            'success' => $ok,
            'message' => (string) ($result['message'] ?? ''),
            'deleted_count' => $result['deleted_count'] ?? 0,
            'deleted_ids' => $result['deleted_ids'] ?? [],
        ], (int) ($result['status'] ?? 200));
    }

    /**
     * Memulihkan pesan yang dihapus secara logis (Khusus Administrator NIP 199402092020121005).
     */
    public function restoreMessage(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->restoreMessage($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $messageId = (int) $request->input('message_id', 0);
        $message = ChatMessage::query()->find($messageId);
        if (!$message) {
            return response()->json([
                'success' => false,
                'message' => 'Pesan tidak ditemukan.',
            ], 404);
        }

        $result = $this->deletionService->restoreSoftDeletedMessage($message, $admin);

        $ok = (bool) ($result['ok'] ?? false);
        $formattedData = (!empty($result['data']) && is_object($result['data']))
            ? $this->formatRegularMessage($result['data'], (int) $admin->id)
            : null;

        return response()->json([
            'status' => $ok,
            'success' => $ok,
            'message' => (string) ($result['message'] ?? ''),
            'data' => $formattedData,
        ], (int) ($result['status'] ?? 200));
    }

    /**
     * Mengambil riwayat audit penghapusan pesan (termasuk pesan yang sudah dihapus permanen).
     */
    public function monitoringAuditLogs(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->viewMonitoring($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        if (!Schema::hasTable('chat_deletion_audit_logs')) {
            return response()->json([
                'success' => true,
                'data' => [],
                'pagination' => ['current_page' => 1, 'per_page' => 30, 'total' => 0, 'last_page' => 1],
            ]);
        }

        $page = max(1, (int) $request->input('page', 1));
        $perPage = min(100, max(1, (int) $request->input('per_page', 30)));

        $query = DB::table('chat_deletion_audit_logs');

        $messageId = (int) $request->input('message_id', 0);
        if ($messageId > 0) {
            $query->where('message_id', $messageId);
        }

        $nip = trim((string) $request->input('nip', ''));
        if ($nip !== '') {
            $query->where('actor_nip', 'like', '%' . $nip . '%');
        }

        $action = trim((string) $request->input('action', ''));
        if ($action !== '') {
            $query->where('action', $action);
        }

        $date = trim((string) $request->input('date', ''));
        if ($date !== '') {
            $query->whereDate('created_at', $date);
        }

        $total = (clone $query)->count();
        $rows = $query->orderBy('id', 'desc')
            ->offset(($page - 1) * $perPage)
            ->limit($perPage)
            ->get();

        $formattedLogs = array_values(array_map(function ($r) {
            return [
                'id' => (int) $r->id,
                'message_id' => (int) $r->message_id,
                'chat_type' => (string) ($r->chat_type ?? 'personal'),
                'sender_id' => $r->sender_id !== null ? (int) $r->sender_id : null,
                'receiver_id' => $r->receiver_id !== null ? (int) $r->receiver_id : null,
                'opd_id' => $r->opd_id !== null ? (int) $r->opd_id : null,
                'action' => (string) ($r->action ?? ''),
                'actor_employee_id' => $r->actor_employee_id !== null ? (int) $r->actor_employee_id : null,
                'actor_nip' => (string) ($r->actor_nip ?? ''),
                'actor_name' => (string) ($r->actor_name ?? ''),
                'message_snapshot' => ChatMessage::decryptText((string) ($r->message_snapshot ?? '')),
                'file_url_snapshot' => $r->file_url_snapshot ?? null,
                'was_read' => !empty($r->was_read),
                'is_permanent' => !empty($r->is_permanent),
                'metadata' => $r->metadata ?? null,
                'created_at' => (string) ($r->created_at ?? ''),
            ];
        }, $rows->all()));

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => 'Audit log penghapusan berhasil dimuat.',
            'data' => $formattedLogs,
            'pagination' => [
                'current_page' => $page,
                'per_page' => $perPage,
                'total' => $total,
                'last_page' => max(1, (int) ceil($total / $perPage)),
            ],
        ]);
    }

    /**
     * Menyimpan persetujuan Syarat & Ketentuan SAHUT pada tabel employees.
     */
    public function acceptTerms(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $accepted = (int) $request->input('accepted', 1) ? 1 : 0;
        $now = date('Y-m-d H:i:s');

        if (Schema::hasTable('employees') && Schema::hasColumn('employees', 'chat_terms_accepted')) {
            $update = ['chat_terms_accepted' => $accepted];
            if (Schema::hasColumn('employees', 'chat_terms_accepted_at')) {
                $update['chat_terms_accepted_at'] = $accepted ? $now : null;
            }
            DB::table('employees')->where('id', (int) $actor->id)->update($update);
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => $accepted ? 'Persetujuan Syarat & Ketentuan tersimpan.' : 'Status persetujuan direset.',
            'chat_terms_accepted' => $accepted,
            'chat_terms_accepted_at' => $accepted ? $now : null,
        ]);
    }

    /**
     * Mengecek status persetujuan Syarat & Ketentuan milik user login.
     */
    public function getTermsStatus(Request $request): JsonResponse
    {
        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$actor) {
            return $this->unauthenticatedResponse();
        }

        $isAccepted = false;
        $acceptedAt = null;
        if (Schema::hasTable('employees') && Schema::hasColumn('employees', 'chat_terms_accepted')) {
            $cols = ['id', 'chat_terms_accepted'];
            if (Schema::hasColumn('employees', 'chat_terms_accepted_at')) {
                $cols[] = 'chat_terms_accepted_at';
            }
            $row = DB::table('employees')
                ->select($cols)
                ->where('id', (int) $actor->id)
                ->first();
            $isAccepted = ((int) ($row->chat_terms_accepted ?? 0)) === 1;
            $acceptedAt = $row->chat_terms_accepted_at ?? null;
        }

        return response()->json([
            'status' => $isAccepted,
            'success' => $isAccepted,
            'chat_terms_accepted' => $isAccepted ? 1 : 0,
            'chat_terms_accepted_at' => $acceptedAt,
        ]);
    }

    /**
     * Status layanan global chat SAHUT.
     */
    public function getGlobalStatus(Request $request): JsonResponse
    {
        $enabled = true;
        if (Schema::hasTable('chat_settings')) {
            $setting = DB::table('chat_settings')->where('key_name', 'chat_feature_enabled')->first();
            if ($setting) {
                $enabled = ((string) ($setting->key_value ?? '1')) === '1';
            }
        } elseif (Schema::hasTable('settings')) {
            $setting = DB::table('settings')->where('key', 'chat_global_enabled')->first();
            if ($setting) {
                $enabled = ((string) ($setting->value ?? '1')) === '1';
            }
        }

        $actor = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        $isMasterUser = $actor ? ChatMessage::isMonitoringAdminNip($actor->nip ?? null) : false;

        return response()->json([
            'status' => true,
            'success' => true,
            'chat_enabled' => $enabled,
            'is_master_user' => $isMasterUser,
            'enabled' => $enabled,
            'message' => $enabled ? 'Layanan Chat Aktif' : 'Layanan Chat Dinonaktifkan',
        ]);
    }

    /**
     * Mengubah status aktif layanan global chat SAHUT (Khusus NIP 199402092020121005).
     */
    public function toggleGlobalStatus(Request $request): JsonResponse
    {
        $admin = EnsureChatMonitoringAdmin::resolveAuthenticatedEmployee($request);
        if (!$admin) {
            return $this->unauthenticatedResponse();
        }
        if (!$this->policy->viewMonitoring($admin)) {
            return $this->forbiddenMonitoringResponse();
        }

        $rawEnabled = $request->input('chat_enabled', '1');
        $value = ($rawEnabled === '1' || $rawEnabled === 1 || $rawEnabled === true || $rawEnabled === 'true') ? '1' : '0';
        $isNowEnabled = ($value === '1');
        $now = date('Y-m-d H:i:s');

        if (Schema::hasTable('chat_settings')) {
            $exists = DB::table('chat_settings')->where('key_name', 'chat_feature_enabled')->exists();
            if ($exists) {
                DB::table('chat_settings')->where('key_name', 'chat_feature_enabled')->update([
                    'key_value' => $value,
                    'updated_at' => $now,
                ]);
            } else {
                DB::table('chat_settings')->insert([
                    'key_name' => 'chat_feature_enabled',
                    'key_value' => $value,
                    'created_at' => $now,
                    'updated_at' => $now,
                ]);
            }
        } elseif (Schema::hasTable('settings')) {
            $exists = DB::table('settings')->where('key', 'chat_global_enabled')->exists();
            if ($exists) {
                DB::table('settings')->where('key', 'chat_global_enabled')->update([
                    'value' => $value,
                    'updated_at' => $now,
                ]);
            } else {
                DB::table('settings')->insert([
                    'key' => 'chat_global_enabled',
                    'value' => $value,
                    'created_at' => $now,
                    'updated_at' => $now,
                ]);
            }
        }

        return response()->json([
            'status' => true,
            'success' => true,
            'message' => $isNowEnabled
                ? 'Layanan Chat BERHASIL DIPUBLIKASIKAN (Tampil di semua perangkat).'
                : 'Layanan Chat BERHASIL DISEMBUNYIKAN dari seluruh perangkat pegawai.',
            'chat_enabled' => $isNowEnabled,
            'is_master_user' => true,
            'enabled' => $isNowEnabled,
        ]);
    }

    private function buildMonitoringBaseQuery(Request $request, bool $skipSqlSearch = false)
    {
        $query = DB::table('chat_messages as cm')
            ->leftJoin('employees as s', 's.id', '=', 'cm.sender_id')
            ->leftJoin('employees as r', 'r.id', '=', 'cm.receiver_id')
            ->leftJoin('opds as so', 'so.id', '=', 's.opd_id')
            ->leftJoin('opds as ro', 'ro.id', '=', 'r.opd_id')
            ->leftJoin('opds as go', 'go.id', '=', 'cm.opd_id')
            ->select([
                'cm.*',
                's.nama as sender_name',
                's.nip as sender_nip',
                's.opd_id as sender_opd_id',
                'so.opd as sender_opd_name',
                'r.nama as receiver_name',
                'r.nip as receiver_nip',
                'r.opd_id as receiver_opd_id',
                'ro.opd as receiver_opd_name',
                'go.opd as group_opd_name',
            ]);

        $chatTypeFilter = strtolower(trim((string) $request->input('chat_type', 'all')));
        if (in_array($chatTypeFilter, ['personal', 'opd'], true)) {
            $query->where('cm.chat_type', $chatTypeFilter);
        }

        $opdId = trim((string) $request->input('opd_id', ''));
        if ($opdId !== '' && ctype_digit($opdId) && (int) $opdId > 0) {
            $opdInt = (int) $opdId;
            $query->where(function ($q) use ($opdInt) {
                $q->where('cm.opd_id', $opdInt)
                    ->orWhere('s.opd_id', $opdInt)
                    ->orWhere('r.opd_id', $opdInt);
            });
        }

        $employeeId = trim((string) $request->input('employee_id', ''));
        if ($employeeId !== '' && ctype_digit($employeeId) && (int) $employeeId > 0) {
            $empInt = (int) $employeeId;
            $query->where(function ($q) use ($empInt) {
                $q->where('cm.sender_id', $empInt)
                    ->orWhere('cm.receiver_id', $empInt);
            });
        }

        $nip = trim((string) $request->input('nip', ''));
        if ($nip !== '') {
            $nipLike = '%' . $nip . '%';
            $query->where(function ($q) use ($nipLike) {
                $q->where('s.nip', 'like', $nipLike)
                    ->orWhere('r.nip', 'like', $nipLike)
                    ->orWhere('cm.deleted_by', 'like', $nipLike);
            });
        }

        $date = trim((string) $request->input('date', ''));
        $dateFrom = trim((string) $request->input('date_from', ''));
        $dateTo = trim((string) $request->input('date_to', ''));
        if ($date !== '') {
            $query->whereDate('cm.created_at', $date);
        } else {
            if ($dateFrom !== '') {
                $query->whereDate('cm.created_at', '>=', $dateFrom);
            }
            if ($dateTo !== '') {
                $query->whereDate('cm.created_at', '<=', $dateTo);
            }
        }

        $status = strtolower(trim((string) $request->input('status', 'all')));
        if ($status !== '' && $status !== 'all') {
            $this->applyMonitoringStatusFilter($query, $status);
        }

        $search = trim((string) $request->input('search', ''));
        if (!$skipSqlSearch && $search !== '') {
            $like = '%' . $search . '%';
            $query->where(function ($q) use ($like) {
                $q->where('cm.message', 'like', $like)
                    ->orWhere('cm.file_name', 'like', $like)
                    ->orWhere('s.nama', 'like', $like)
                    ->orWhere('s.nip', 'like', $like)
                    ->orWhere('r.nama', 'like', $like)
                    ->orWhere('r.nip', 'like', $like)
                    ->orWhere('so.opd', 'like', $like)
                    ->orWhere('ro.opd', 'like', $like)
                    ->orWhere('go.opd', 'like', $like);
            });
        }

        return $query;
    }

    private function filterRowsBySearchTerm($rows, string $search)
    {
        $needle = strtolower(trim($search));
        if ($needle === '') {
            return $rows;
        }

        return $rows->filter(function ($row) use ($needle) {
            $decryptedMessage = strtolower(ChatMessage::decryptText((string) ($row->message ?? '')));
            $haystack = [
                $decryptedMessage,
                strtolower((string) ($row->file_name ?? '')),
                strtolower((string) ($row->sender_name ?? '')),
                strtolower((string) ($row->sender_nip ?? '')),
                strtolower((string) ($row->receiver_name ?? '')),
                strtolower((string) ($row->receiver_nip ?? '')),
                strtolower((string) ($row->sender_opd_name ?? '')),
                strtolower((string) ($row->receiver_opd_name ?? '')),
                strtolower((string) ($row->group_opd_name ?? '')),
                strtolower((string) ($row->deleted_by ?? '')),
            ];
            foreach ($haystack as $value) {
                if ($value !== '' && str_contains($value, $needle)) {
                    return true;
                }
            }
            return false;
        })->values();
    }

    private function applyMonitoringStatusFilter($query, string $status): void
    {
        switch ($status) {
            case 'active':
                $query->where('cm.deleted_for_sender', 0)
                    ->where('cm.deleted_for_receiver', 0)
                    ->where('cm.deleted_for_everyone', 0)
                    ->where('cm.is_deleted', 0)
                    ->where('cm.is_deleted_global', 0)
                    ->whereRaw("(cm.deleted_for_users IS NULL OR TRIM(cm.deleted_for_users) = '')");
                break;

            case 'deleted':
                $query->where(function ($q) {
                    $q->where('cm.deleted_for_sender', 1)
                        ->orWhere('cm.deleted_for_receiver', 1)
                        ->orWhere('cm.deleted_for_everyone', 1)
                        ->orWhere('cm.is_deleted', 1)
                        ->orWhere('cm.is_deleted_global', 1)
                        ->orWhereRaw("(cm.deleted_for_users IS NOT NULL AND TRIM(cm.deleted_for_users) != '')");
                });
                break;

            case 'deleted_for_sender':
                $query->where(function ($q) {
                    $q->where('cm.deleted_for_sender', 1)->orWhere('cm.is_deleted', 1);
                });
                break;

            case 'deleted_for_receiver':
                $query->where('cm.deleted_for_receiver', 1);
                break;

            case 'deleted_for_everyone':
                $query->where(function ($q) {
                    $q->where('cm.deleted_for_everyone', 1)->orWhere('cm.is_deleted_global', 1);
                });
                break;

            case 'read':
                $query->where(function ($q) {
                    $q->where('cm.is_read', 1)->orWhereNotNull('cm.read_at');
                });
                break;

            case 'unread':
                $query->where('cm.is_read', 0)->whereNull('cm.read_at');
                break;
        }
    }

    private function loadAuditHistoryMap(array $messageIds): array
    {
        if (empty($messageIds) || !Schema::hasTable('chat_deletion_audit_logs')) {
            return [];
        }

        $logs = DB::table('chat_deletion_audit_logs')
            ->whereIn('message_id', $messageIds)
            ->orderBy('id', 'desc')
            ->get();

        $map = [];
        foreach ($logs as $log) {
            $mid = (int) $log->message_id;
            $map[$mid][] = [
                'id' => (int) $log->id,
                'action' => (string) $log->action,
                'actor_nip' => (string) $log->actor_nip,
                'actor_name' => (string) ($log->actor_name ?? ''),
                'was_read' => (bool) $log->was_read,
                'is_permanent' => (bool) $log->is_permanent,
                'created_at' => (string) ($log->created_at ?? ''),
            ];
        }

        return $map;
    }

    private function resolveDeleteStatusFromRow(object $row): string
    {
        if (!empty($row->deleted_for_everyone) || !empty($row->is_deleted_global)) {
            return 'deleted_for_everyone';
        }
        $bySender = !empty($row->deleted_for_sender) || !empty($row->is_deleted);
        $byReceiver = !empty($row->deleted_for_receiver);
        if (!$byReceiver && !empty(trim((string) ($row->deleted_for_users ?? '')))) {
            foreach (explode(',', (string) $row->deleted_for_users) as $part) {
                $t = trim($part);
                if ($t !== '' && ctype_digit($t) && (int) $t !== (int) ($row->sender_id ?? 0)) {
                    $byReceiver = true;
                    break;
                }
            }
        }

        if ($bySender && $byReceiver) {
            return 'deleted_for_both';
        }
        if ($bySender) {
            return 'deleted_for_sender';
        }
        if ($byReceiver) {
            return 'deleted_for_receiver';
        }

        return 'active';
    }

    private function formatRegularMessage(object $row, int $actorId): array
    {
        $createdAt = (string) ($row->created_at ?? '');
        $ts = $createdAt !== '' ? (int) strtotime($createdAt) : time();
        $decryptedMessage = ChatMessage::decryptText((string) ($row->message ?? ''));
        $rawReplyMessage = $row->joined_reply_message ?? $row->reply_to_message ?? '';
        $decryptedReply = ChatMessage::decryptText((string) $rawReplyMessage);
        $replySenderName = (string) ($row->joined_reply_sender_name ?? $row->reply_to_sender_name ?? '');

        return [
            'id' => (int) $row->id,
            'sender_id' => (int) $row->sender_id,
            'sender_name' => (string) ($row->joined_sender_name ?? $row->sender_name ?? ''),
            'receiver_id' => isset($row->receiver_id) && $row->receiver_id !== null ? (int) $row->receiver_id : null,
            'opd_id' => isset($row->opd_id) && $row->opd_id !== null ? (int) $row->opd_id : null,
            'chat_type' => (string) ($row->chat_type ?? 'personal'),
            'message' => $decryptedMessage,
            'msg_type' => (string) ($row->msg_type ?? 'text'),
            'file_url' => $row->file_url ?? null,
            'file_name' => $row->file_name ?? null,
            'reply_to_id' => isset($row->reply_to_id) && $row->reply_to_id !== null ? (int) $row->reply_to_id : null,
            'reply_to_sender_name' => $replySenderName,
            'reply_to_message' => $decryptedReply,
            'is_me' => ((int) $row->sender_id === $actorId),
            'is_read' => (!empty($row->is_read) || !empty($row->read_at)),
            'read_at' => !empty($row->read_at) ? (string) $row->read_at : null,
            'deleted_for_sender' => !empty($row->deleted_for_sender) ? 1 : 0,
            'deleted_for_receiver' => !empty($row->deleted_for_receiver) ? 1 : 0,
            'deleted_for_everyone' => !empty($row->deleted_for_everyone) ? 1 : 0,
            'deleted_by' => isset($row->deleted_by) && $row->deleted_by !== null ? (string) $row->deleted_by : null,
            'deleted_at' => !empty($row->deleted_at) ? (string) $row->deleted_at : null,
            'is_deleted' => !empty($row->is_deleted) ? 1 : 0,
            'deleted_for_users' => (string) ($row->deleted_for_users ?? ''),
            'is_deleted_global' => !empty($row->is_deleted_global) ? 1 : 0,
            'created_at' => $createdAt,
            'created_at_formatted' => $createdAt !== '' ? date('d/m H:i', strtotime($createdAt)) : '',
            'created_at_ts' => $ts,
        ];
    }

    private function formatMonitoringMessage(object $row, array $auditHistory): array
    {
        $deleteStatus = $this->resolveDeleteStatusFromRow($row);
        $createdAt = (string) ($row->created_at ?? '');
        $opdName = (string) ($row->chat_type === 'opd'
            ? ($row->group_opd_name ?? '')
            : ($row->sender_opd_name ?? $row->receiver_opd_name ?? ''));
        $decryptedMessage = ChatMessage::decryptText((string) ($row->message ?? ''));

        return [
            'id' => (int) $row->id,
            'chat_type' => (string) ($row->chat_type ?? 'personal'),
            'sender_id' => (int) $row->sender_id,
            'sender_name' => (string) ($row->sender_name ?? 'Pegawai #' . $row->sender_id),
            'sender_nip' => (string) ($row->sender_nip ?? ''),
            'sender_opd_name' => (string) ($row->sender_opd_name ?? ''),
            'receiver_id' => (int) ($row->receiver_id ?? 0),
            'receiver_name' => $row->chat_type === 'opd'
                ? ('Grup OPD: ' . ($row->group_opd_name ?? ('OPD #' . $row->opd_id)))
                : (string) ($row->receiver_name ?? 'Pegawai #' . $row->receiver_id),
            'receiver_nip' => (string) ($row->receiver_nip ?? ''),
            'receiver_opd_name' => (string) ($row->receiver_opd_name ?? ''),
            'opd_id' => (int) ($row->opd_id ?? $row->sender_opd_id ?? 0),
            'opd_name' => $opdName,
            'message' => $decryptedMessage,
            'msg_type' => (string) ($row->msg_type ?? 'text'),
            'file_url' => (string) ($row->file_url ?? ''),
            'file_name' => (string) ($row->file_name ?? ''),
            'is_read' => (!empty($row->is_read) || !empty($row->read_at)) ? 1 : 0,
            'read_at' => $row->read_at ?? null,
            'deleted_for_sender' => (!empty($row->deleted_for_sender) || !empty($row->is_deleted)) ? 1 : 0,
            'deleted_for_receiver' => !empty($row->deleted_for_receiver) ? 1 : 0,
            'deleted_for_everyone' => (!empty($row->deleted_for_everyone) || !empty($row->is_deleted_global)) ? 1 : 0,
            'deleted_by' => $row->deleted_by ?? null,
            'deleted_at' => $row->deleted_at ?? null,
            'delete_status' => $deleteStatus,
            'is_deleted_any' => $deleteStatus !== 'active' ? 1 : 0,
            'created_at' => $createdAt,
            'created_at_formatted' => $createdAt !== '' ? date('d/m/Y H:i', strtotime($createdAt)) : '',
            'deletion_history' => $auditHistory,
        ];
    }

    private function unauthenticatedResponse(): JsonResponse
    {
        return response()->json([
            'success' => false,
            'message' => 'Tidak terautentikasi.',
        ], 401);
    }

    private function forbiddenMonitoringResponse(): JsonResponse
    {
        return response()->json([
            'success' => false,
            'message' => 'Akses ditolak. Fitur Monitoring & Penghapusan Permanen khusus NIP ' . ChatMessage::MONITORING_ADMIN_NIP . '.',
        ], 403);
    }
}
