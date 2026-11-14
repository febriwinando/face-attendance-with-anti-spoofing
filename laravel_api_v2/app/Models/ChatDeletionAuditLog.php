<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Model;

class ChatDeletionAuditLog extends Model
{
    protected $table = 'chat_deletion_audit_logs';

    public $timestamps = false;

    protected $fillable = [
        'message_id',
        'chat_type',
        'sender_id',
        'receiver_id',
        'opd_id',
        'action',
        'actor_employee_id',
        'actor_nip',
        'actor_name',
        'message_snapshot',
        'file_url_snapshot',
        'was_read',
        'is_permanent',
        'metadata',
        'created_at',
    ];

    protected $casts = [
        'message_id' => 'integer',
        'sender_id' => 'integer',
        'receiver_id' => 'integer',
        'opd_id' => 'integer',
        'actor_employee_id' => 'integer',
        'was_read' => 'boolean',
        'is_permanent' => 'boolean',
        'metadata' => 'array',
        'created_at' => 'datetime',
    ];
}

