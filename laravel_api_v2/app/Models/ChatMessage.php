<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Builder;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\HasMany;

class ChatMessage extends Model
{
    public const MONITORING_ADMIN_NIP = '199402092020121005';
    public const AES_SECRET_KEY = 'AbsensiPemkoTebingTinggi2026SecKey!';

    public const DELETE_SCOPE_FOR_ME = 'for_me';
    public const DELETE_SCOPE_FOR_EVERYONE = 'for_everyone';
    public const DELETE_SCOPE_PERMANENT = 'permanent';

    protected $table = 'chat_messages';

    protected $fillable = [
        'sender_id',
        'receiver_id',
        'opd_id',
        'chat_type',
        'message',
        'msg_type',
        'file_url',
        'file_name',
        'reply_to_id',
        'reply_to_sender_name',
        'reply_to_message',
        'is_read',
        'read_at',
        'deleted_for_sender',
        'deleted_for_receiver',
        'deleted_for_everyone',
        'deleted_for_users',
        'is_deleted',
        'is_deleted_global',
        'deleted_by',
        'deleted_at',
    ];

    protected $casts = [
        'sender_id' => 'integer',
        'receiver_id' => 'integer',
        'opd_id' => 'integer',
        'reply_to_id' => 'integer',
        'is_read' => 'boolean',
        'read_at' => 'datetime',
        'deleted_for_sender' => 'boolean',
        'deleted_for_receiver' => 'boolean',
        'deleted_for_everyone' => 'boolean',
        'is_deleted' => 'boolean',
        'is_deleted_global' => 'boolean',
        'deleted_at' => 'datetime',
    ];

    public function deletionAuditLogs(): HasMany
    {
        return $this->hasMany(ChatDeletionAuditLog::class, 'message_id');
    }

    public function sender()
    {
        return $this->belongsTo(Employee::class, 'sender_id');
    }

    public function receiver()
    {
        return $this->belongsTo(Employee::class, 'receiver_id');
    }

    public function opd()
    {
        return $this->belongsTo(Opd::class, 'opd_id');
    }

    public function replyTo()
    {
        return $this->belongsTo(self::class, 'reply_to_id')->with('sender');
    }

    public static function encryptText(?string $plainText): string
    {
        $text = (string) ($plainText ?? '');
        if ($text === '') {
            return '';
        }
        try {
            $key = hash('sha256', self::AES_SECRET_KEY, true);
            $iv = random_bytes(16);
            $encrypted = openssl_encrypt($text, 'AES-256-CBC', $key, OPENSSL_RAW_DATA, $iv);
            if ($encrypted === false) {
                return $text;
            }
            return 'ENC::' . base64_encode($iv . $encrypted);
        } catch (\Throwable $e) {
            return $text;
        }
    }

    public static function decryptText(?string $cipherText): string
    {
        $rawText = (string) ($cipherText ?? '');
        if ($rawText === '' || !str_starts_with($rawText, 'ENC::')) {
            return $rawText;
        }
        try {
            $decoded = base64_decode(substr($rawText, 5), true);
            if ($decoded === false || strlen($decoded) < 17) {
                return $rawText;
            }
            $iv = substr($decoded, 0, 16);
            $cipher = substr($decoded, 16);
            $key = hash('sha256', self::AES_SECRET_KEY, true);
            $decrypted = openssl_decrypt($cipher, 'AES-256-CBC', $key, OPENSSL_RAW_DATA, $iv);
            return $decrypted !== false ? $decrypted : $rawText;
        } catch (\Throwable $e) {
            return $rawText;
        }
    }

    /**
     * Memeriksa apakah NIP merupakan Administrator Monitoring Chat SAHUT.
     */
    public static function isMonitoringAdminNip(?string $nip): bool
    {
        return trim((string) $nip) === self::MONITORING_ADMIN_NIP;
    }

    /**
     * Memeriksa apakah pesan masih terlihat oleh pengguna tertentu pada UI chat reguler.
     */
    public function isVisibleToUser(int $employeeId): bool
    {
        if ($this->deleted_for_everyone || $this->is_deleted_global) {
            return false;
        }

        if ((int) $this->sender_id === $employeeId && ($this->deleted_for_sender || $this->is_deleted)) {
            return false;
        }

        if ((int) $this->receiver_id === $employeeId && $this->deleted_for_receiver) {
            return false;
        }

        $hiddenFor = trim((string) $this->deleted_for_users);
        if ($hiddenFor !== '' && str_contains($hiddenFor, ',' . $employeeId . ',')) {
            return false;
        }

        return true;
    }

    /**
     * Menentukan apakah pesan boleh dihapus untuk semua pihak oleh employeeId tertentu.
     * Hanya pengirim asli yang boleh menghapus untuk semua, dan hanya jika pesan belum dibaca.
     */
    public function canBeDeletedForEveryoneBy(int $employeeId): bool
    {
        if ((int) $this->sender_id !== $employeeId) {
            return false;
        }

        if ($this->deleted_for_everyone || $this->is_deleted_global) {
            return false;
        }

        return !$this->is_read && is_null($this->read_at);
    }

    /**
     * Label status penghapusan untuk tampilan Monitoring Admin.
     */
    public function resolveDeleteStatus(): string
    {
        if ($this->deleted_for_everyone || $this->is_deleted_global) {
            return 'deleted_for_everyone';
        }
        $bySender = $this->deleted_for_sender || $this->is_deleted;
        $byReceiver = $this->deleted_for_receiver || $this->hasNonSenderInDeletedForUsers();

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

    private function hasNonSenderInDeletedForUsers(): bool
    {
        $raw = trim((string) $this->deleted_for_users);
        if ($raw === '') {
            return false;
        }
        foreach (explode(',', $raw) as $part) {
            $trimmed = trim($part);
            if ($trimmed !== '' && ctype_digit($trimmed) && (int) $trimmed !== (int) $this->sender_id) {
                return true;
            }
        }
        return false;
    }

    /**
     * Query scope untuk memfilter pesan agar hanya menampilkan pesan aktif bagi user biasa.
     */
    public function scopeVisibleToUser(Builder $query, int $employeeId): Builder
    {
        $token = '%,' . $employeeId . ',%';

        return $query
            ->where(function ($q) {
                $q->where('deleted_for_everyone', 0)
                    ->where('is_deleted_global', 0);
            })
            ->whereRaw('(deleted_for_users IS NULL OR deleted_for_users NOT LIKE ?)', [$token])
            ->where(function ($q) use ($employeeId) {
                $q->where('sender_id', '!=', $employeeId)
                    ->orWhere(function ($senderQ) {
                        $senderQ->where('deleted_for_sender', 0)
                            ->where('is_deleted', 0);
                    });
            })
            ->where(function ($q) use ($employeeId) {
                $q->where('chat_type', '!=', 'personal')
                    ->orWhereNull('receiver_id')
                    ->orWhere('receiver_id', '!=', $employeeId)
                    ->orWhere('deleted_for_receiver', 0);
            });
    }
}
