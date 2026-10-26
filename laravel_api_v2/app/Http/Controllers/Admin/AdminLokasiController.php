<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class AdminLokasiController extends Controller
{
    /**
     * Menyimpan / Meng-update Lokasi Pegawai Khusus (Koordinat Employee Exception)
     */
    public function storeLokasiPegawai(Request $request)
    {
        try {
            // Resolusi Parameter Fleksibel dari Android & Website
            $empIdInput = $request->input('employee_id') ?? $request->input('pegawai_id') ?? $request->input('id_pegawai');
            $nipInput   = $request->input('nip');
            $alamat     = $request->input('alamat', '-');
            $lat        = $request->input('lat') ?? $request->input('let') ?? $request->input('latitude');
            $lng        = $request->input('lng') ?? $request->input('longitude');
            $opdId      = $request->input('opd_id') ?? $request->input('eOPD');

            if (!$empIdInput || !$lat || !$lng) {
                return response()->json([
                    'status' => false,
                    'remarks' => 'Gagal: Parameter employee_id/NIP, lat, dan lng wajib diisi.'
                ], 400);
            }

            // Cari ID Primary Key Pegawai jika input berupa NIP
            $employee = DB::table('employees')
                ->where('id', $empIdInput)
                ->orWhere('nip', $empIdInput)
                ->orWhere('nip', $nipInput)
                ->first();

            $finalEmployeeId = $employee ? $employee->id : $empIdInput;
            $finalOpdId      = $employee ? $employee->opd_id : ($opdId ?? 0);

            // Cek apakah lokasi pegawai sudah ada di tabel koordinat_employees / lokasi_pegawais
            $existing = DB::table('koordinat_employees')
                ->where('employee_id', $finalEmployeeId)
                ->first();

            if ($existing) {
                // Update lokasi yang sudah ada
                DB::table('koordinat_employees')
                    ->where('id', $existing->id)
                    ->update([
                        'alamat'     => $alamat,
                        'let'        => $lat,
                        'lng'        => $lng,
                        'status'     => 1,
                        'updated_at' => now(),
                    ]);
            } else {
                // Insert lokasi baru
                DB::table('koordinat_employees')->insert([
                    'employee_id' => $finalEmployeeId,
                    'alamat'      => $alamat,
                    'let'         => $lat,
                    'lng'         => $lng,
                    'status'      => 1,
                    'created_at'  => now(),
                    'updated_at'  => now(),
                ]);
            }

            return response()->json([
                'status'  => true,
                'remarks' => 'Lokasi pegawai berhasil disimpan dan langsung aktif di website!'
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error storeLokasiPegawai: ' . $e->getMessage());
            return response()->json([
                'status'  => false,
                'remarks' => 'Terjadi kesalahan server: ' . $e->getMessage()
            ], 500);
        }
    }
}
