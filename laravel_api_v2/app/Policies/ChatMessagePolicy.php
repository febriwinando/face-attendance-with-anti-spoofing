<?php

namespace App\Policies;

use App\Models\ChatMessage;

/**
 * Policy otorisasi pesan chat SAHUT:
 * - viewMonitoring & permanentDelete: eksklusif NIP 199402092020121005.
 * - deleteForMe: pengirim, penerima, atau anggota OPD pada grup OPD terkait.
 * - deleteForEveryone: hanya pengirim asli dan hanya jika pesan belum dibaca.
 */
class ChatMessagePolicy
{
    public function viewMonitoring(object $actor): bool
    {
        return ChatMessage::isMonitoringAdminNip($actor->nip ?? null);
    }

    public function deleteForMe(object $actor, ChatMessage $message): bool
    {
        $actorId = (int) ($actor->id ?? 0);
        if ($actorId <= 0) {
            return false;
        }

        if ($message->chat_type === 'opd') {
            $actorOpdId = (int) ($actor->opd_id ?? 0);
            if ((int) $message->sender_id === $actorId) {
                return true;
            }
            return $actorOpdId > 0 && (int) $message->opd_id === $actorOpdId;
        }

        return (int) $message->sender_id === $actorId
            || (int) $message->receiver_id === $actorId;
    }

    public function deleteForEveryone(object $actor, ChatMessage $message): bool
    {
        $actorId = (int) ($actor->id ?? 0);
        if ($actorId <= 0) {
            return false;
        }

        return $message->canBeDeletedForEveryoneBy($actorId);
    }

    public function permanentDelete(object $actor, ?ChatMessage $message = null): bool
    {
        return $this->viewMonitoring($actor);
    }

    public function restoreMessage(object $actor, ?ChatMessage $message = null): bool
    {
        return $this->viewMonitoring($actor);
    }
}

