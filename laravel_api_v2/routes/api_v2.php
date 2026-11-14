<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\Admin\EmployeeSyncController;
use App\Http\Controllers\Admin\AdminLokasiController;
use App\Http\Controllers\Admin\AdminPresensiController;
use App\Http\Controllers\Chat\ChatController;

/*
|--------------------------------------------------------------------------
| API V2 Routes (Terisolasi Khusus Version Baru & Super Admin)
|--------------------------------------------------------------------------
| File ini aman digunakan untuk rilis versi baru tanpa mengganggu
| aplikasi lama yang berjalan pada routes/api.php.
|
*/

// Master Data Pegawai & OPD
Route::get('/employees', [EmployeeSyncController::class, 'getEmployees']);
Route::get('/opd-list', [EmployeeSyncController::class, 'getOpdList']);
Route::get('/admin/opd-list', [EmployeeSyncController::class, 'getOpdList']);

// Layanan Super Admin
Route::prefix('admin')->group(function () {
    // Kelola Lokasi Pegawai (Peta & Koordinat)
    Route::post('/lokasi-pegawai/store', [AdminLokasiController::class, 'storeLokasiPegawai']);

    // Update Status Aktif Pegawai
    Route::post('/pegawai/status', [EmployeeSyncController::class, 'updateStatusPegawai']);

    // Update Jam Masuk Massal
    Route::post('/update-masuk', [AdminPresensiController::class, 'updateMasukMassal']);

    // Insert Massal Presensi
    Route::post('/insert-massal/preview', [AdminPresensiController::class, 'previewInsertMassal']);
    Route::post('/insert-massal/store', [AdminPresensiController::class, 'storeInsertMassal']);

    // Koreksi / Pengolahan Absensi
    Route::post('/pengolahan/store', [AdminPresensiController::class, 'storePengolahanAbsensi']);
});

use App\Http\Middleware\EnsureChatMonitoringAdmin;

// ==========================================
// Layanan Chat SAHUT (Sarana Hubungan Terpadu)
// ==========================================
Route::prefix('chat')->group(function () {
    // Sinkronisasi & Status Baca Pesan
    Route::get('/messages', [ChatController::class, 'getMessages']);
    Route::post('/mark-read', [ChatController::class, 'markAsRead']);

    // Hapus Pesan Logis (Mendukung 'for_me' dan 'for_everyone' jika belum dibaca)
    Route::post('/delete-message', [ChatController::class, 'deleteMessage']);
    Route::post('/delete-for-everyone', [ChatController::class, 'deleteForEveryone']);

    // Bersihkan Obrolan Secara Logis (Cakupan Terisolasi per Percakapan)
    Route::post('/clear-conversation', [ChatController::class, 'clearConversation']);

    // Syarat & Ketentuan (Terms & Conditions) & Status Global
    Route::post('/accept-terms', [ChatController::class, 'acceptTerms']);
    Route::get('/terms-status', [ChatController::class, 'getTermsStatus']);
    Route::get('/global-status', [ChatController::class, 'getGlobalStatus']);

    // Fitur Khusus Administrator Monitoring Chat (Hanya NIP 199402092020121005)
    Route::middleware([EnsureChatMonitoringAdmin::class])->group(function () {
        Route::get('/monitoring-conversations', [ChatController::class, 'monitoringConversations']);
        Route::get('/monitoring/messages', [ChatController::class, 'monitoringMessages']);
        Route::get('/monitoring/conversation-detail', [ChatController::class, 'monitoringConversationDetail']);
        Route::get('/monitoring/audit-logs', [ChatController::class, 'monitoringAuditLogs']);
        Route::post('/monitoring/permanent-delete', [ChatController::class, 'monitoringPermanentDelete']);
        Route::post('/hard-delete-message', [ChatController::class, 'monitoringPermanentDelete']);
        Route::post('/restore-message', [ChatController::class, 'restoreMessage']);
        Route::post('/global-status', [ChatController::class, 'toggleGlobalStatus']);
    });
});
