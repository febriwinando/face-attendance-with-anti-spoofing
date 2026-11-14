<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Menambahkan kolom status penghapusan logis, waktu baca, dan indeks performa pada tabel chat_messages.
     */
    public function up(): void
    {
        if (!Schema::hasTable('chat_messages')) {
            return;
        }

        Schema::table('chat_messages', function (Blueprint $table) {
            if (!Schema::hasColumn('chat_messages', 'deleted_for_sender')) {
                $table->boolean('deleted_for_sender')->default(false)->after('is_read');
            }
            if (!Schema::hasColumn('chat_messages', 'deleted_for_receiver')) {
                $table->boolean('deleted_for_receiver')->default(false)->after('deleted_for_sender');
            }
            if (!Schema::hasColumn('chat_messages', 'deleted_for_everyone')) {
                $table->boolean('deleted_for_everyone')->default(false)->after('deleted_for_receiver');
            }
            if (!Schema::hasColumn('chat_messages', 'deleted_for_users')) {
                $table->text('deleted_for_users')->nullable()->after('deleted_for_everyone');
            }
            if (!Schema::hasColumn('chat_messages', 'is_deleted')) {
                $table->boolean('is_deleted')->default(false)->after('deleted_for_users');
            }
            if (!Schema::hasColumn('chat_messages', 'is_deleted_global')) {
                $table->boolean('is_deleted_global')->default(false)->after('is_deleted');
            }
            if (!Schema::hasColumn('chat_messages', 'deleted_by')) {
                $table->string('deleted_by', 50)->nullable()->after('is_deleted_global');
            }
            if (!Schema::hasColumn('chat_messages', 'deleted_at')) {
                $table->timestamp('deleted_at')->nullable()->after('deleted_by');
            }
            if (!Schema::hasColumn('chat_messages', 'read_at')) {
                $table->timestamp('read_at')->nullable()->after('is_read');
            }
        });

        // Backfill kompatibilitas data lama jika kolom is_deleted_global sebelumnya aktif
        if (
            Schema::hasColumn('chat_messages', 'is_deleted_global')
            && Schema::hasColumn('chat_messages', 'deleted_for_everyone')
        ) {
            DB::table('chat_messages')
                ->where('is_deleted_global', 1)
                ->where('deleted_for_everyone', 0)
                ->update(['deleted_for_everyone' => 1]);
        }

        $this->addIndexSafely('chat_messages', 'idx_cm_personal_pair', ['chat_type', 'sender_id', 'receiver_id']);
        $this->addIndexSafely('chat_messages', 'idx_cm_opd_created', ['chat_type', 'opd_id', 'created_at']);
        $this->addIndexSafely('chat_messages', 'idx_cm_delete_flags', ['deleted_for_everyone', 'deleted_for_sender', 'deleted_for_receiver']);
        $this->addIndexSafely('chat_messages', 'idx_cm_read_status', ['is_read', 'read_at']);

        if (Schema::hasTable('employees')) {
            Schema::table('employees', function (Blueprint $table) {
                if (!Schema::hasColumn('employees', 'chat_terms_accepted')) {
                    $table->boolean('chat_terms_accepted')->default(false);
                }
                if (!Schema::hasColumn('employees', 'chat_terms_accepted_at')) {
                    $table->timestamp('chat_terms_accepted_at')->nullable();
                }
            });
        }
    }

    /**
     * Rollback kolom tambahan (kecuali kolom legacy inti).
     */
    public function down(): void
    {
        if (Schema::hasTable('employees')) {
            Schema::table('employees', function (Blueprint $table) {
                $empCols = array_values(array_filter(
                    ['chat_terms_accepted', 'chat_terms_accepted_at'],
                    fn (string $col) => Schema::hasColumn('employees', $col)
                ));
                if (!empty($empCols)) {
                    $table->dropColumn($empCols);
                }
            });
        }

        if (!Schema::hasTable('chat_messages')) {
            return;
        }

        $this->dropIndexSafely('chat_messages', 'idx_cm_personal_pair');
        $this->dropIndexSafely('chat_messages', 'idx_cm_opd_created');
        $this->dropIndexSafely('chat_messages', 'idx_cm_delete_flags');
        $this->dropIndexSafely('chat_messages', 'idx_cm_read_status');

        Schema::table('chat_messages', function (Blueprint $table) {
            $columns = [
                'deleted_for_sender',
                'deleted_for_receiver',
                'deleted_for_everyone',
                'deleted_by',
                'deleted_at',
                'read_at',
            ];
            $existing = array_values(array_filter($columns, fn (string $col) => Schema::hasColumn('chat_messages', $col)));
            if (!empty($existing)) {
                $table->dropColumn($existing);
            }
        });
    }

    private function addIndexSafely(string $tableName, string $indexName, array $columns): void
    {
        try {
            Schema::table($tableName, function (Blueprint $table) use ($columns, $indexName) {
                $table->index($columns, $indexName);
            });
        } catch (\Throwable $e) {
            // Indeks mungkin sudah ada pada database target
        }
    }

    private function dropIndexSafely(string $tableName, string $indexName): void
    {
        try {
            Schema::table($tableName, function (Blueprint $table) use ($indexName) {
                $table->dropIndex($indexName);
            });
        } catch (\Throwable $e) {
            // Abaikan bila indeks tidak ditemukan
        }
    }
};

