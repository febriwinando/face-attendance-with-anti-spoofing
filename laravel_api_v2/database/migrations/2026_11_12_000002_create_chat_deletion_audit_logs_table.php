<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Membuat tabel audit log penghapusan pesan chat (tanpa ON DELETE CASCADE agar riwayat tetap terjaga).
     */
    public function up(): void
    {
        if (Schema::hasTable('chat_deletion_audit_logs')) {
            return;
        }

        Schema::create('chat_deletion_audit_logs', function (Blueprint $table) {
            $table->bigIncrements('id');
            $table->unsignedBigInteger('message_id')->index();
            $table->string('chat_type', 20)->default('personal')->index();
            $table->unsignedBigInteger('sender_id')->nullable()->index();
            $table->unsignedBigInteger('receiver_id')->nullable()->index();
            $table->unsignedBigInteger('opd_id')->nullable()->index();
            $table->string('action', 50)->index();
            $table->unsignedBigInteger('actor_employee_id')->nullable()->index();
            $table->string('actor_nip', 50)->index();
            $table->string('actor_name', 150)->nullable();
            $table->longText('message_snapshot')->nullable();
            $table->string('file_url_snapshot', 500)->nullable();
            $table->boolean('was_read')->default(false);
            $table->boolean('is_permanent')->default(false)->index();
            $table->json('metadata')->nullable();
            $table->timestamp('created_at')->useCurrent()->index();
        });
    }

    /**
     * Rollback tabel audit log penghapusan chat.
     */
    public function down(): void
    {
        Schema::dropIfExists('chat_deletion_audit_logs');
    }
};

