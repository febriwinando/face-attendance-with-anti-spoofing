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

// ==========================================
// Layanan Chat SAHUT (Sarana Hubungan Terpadu)
// ==========================================
Route::prefix('chat')->group(function () {
    // Sinkronisasi Pesan
    Route::get('/messages', [ChatController::class, 'getMessages']);

    // Hapus Pesan (Mendukung 'for_me', 'global', dan 'hard_delete')
    Route::post('/delete-message', [ChatController::class, 'deleteMessage']);
    Route::post('/hard-delete-message', [ChatController::class, 'deleteMessage']); // Shortcut hard delete (Khusus NIP 199402092020121005)
    Route::post('/restore-message', [ChatController::class, 'restoreMessage']);     // Pulihkan pesan (Khusus NIP 199402092020121005)

    // Bersihkan Obrolan
    Route::post('/clear-conversation', [ChatController::class, 'clearConversation']);

    // Syarat & Ketentuan (Terms & Conditions)
    Route::post('/accept-terms', [ChatController::class, 'acceptTerms']);
    Route::get('/terms-status', [ChatController::class, 'getTermsStatus']);
});
