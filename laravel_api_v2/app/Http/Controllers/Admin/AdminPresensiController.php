<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class AdminPresensiController extends Controller
{
    /**
     * Update Jam Masuk & Presensi Massal
     */
    public function updateMasukMassal(Request $request)
    {
        try {
            $tanggal      = $request->input('tanggal');
            $jamMasuk     = $request->input('masuk');
            $keterlambatan= (int) $request->input('keterlambatan', 0);
            $ketMasuk     = $request->input('ket_masuk', 'Hadir');
            $pegawaiNips  = $request->input('pegawai', []);

            if (!$tanggal || !$jamMasuk || empty($pegawaiNips)) {
                return response()->json([
                    'status' => false,
                    'remarks' => 'Gagal: Tanggal, jam masuk, dan daftar pegawai wajib diisi.'
                ], 400);
            }

            $affected = 0;
            foreach ($pegawaiNips as $nip) {
                $employee = DB::table('employees')->where('nip', $nip)->orWhere('id', $nip)->first();
                if (!$employee) continue;

                $existing = DB::table('absensi')
                    ->where('employee_id', $employee->id)
                    ->where('tanggal', $tanggal)
                    ->first();

                if ($existing) {
                    DB::table('absensi')->where('id', $existing->id)->update([
                        'jam_masuk'     => $jamMasuk,
                        'terlambat'     => $keterlambatan,
                        'ket_masuk'     => $ketMasuk,
                        'status_masuk'  => 'Hadir',
                        'valid_masuk'   => 1,
                        'updated_at'    => now()
                    ]);
                } else {
                    DB::table('absensi')->insert([
                        'employee_id'   => $employee->id,
                        'opd_id'        => $employee->opd_id,
                        'tanggal'       => $tanggal,
                        'jam_masuk'     => $jamMasuk,
                        'terlambat'     => $keterlambatan,
                        'ket_masuk'     => $ketMasuk,
                        'status_masuk'  => 'Hadir',
                        'valid_masuk'   => 1,
                        'created_at'    => now(),
                        'updated_at'    => now()
                    ]);
                }
                $affected++;
            }

            return response()->json([
                'status' => true,
                'remarks' => "Berhasil memperbarui data presensi untuk {$affected} pegawai."
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error updateMasukMassal: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'remarks' => 'Terjadi kesalahan server: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Preview Insert Massal Presensi
     */
    public function previewInsertMassal(Request $request)
    {
        try {
            $tanggal = $request->input('tanggal');
            $nipsRaw = $request->input('nips', '');

            $nips = array_filter(array_map('trim', explode(',', str_replace(["\n", "\r"], ',', $nipsRaw))));

            $employees = DB::table('employees')
                ->whereIn('nip', $nips)
                ->orWhereIn('id', $nips)
                ->get();

            return response()->json([
                'status' => true,
                'remarks' => 'Preview ditemukan ' . $employees->count() . ' pegawai dari total ' . count($nips) . ' NIP.',
                'data' => $employees
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error previewInsertMassal: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'remarks' => 'Terjadi kesalahan server: ' . $e->getMessage()
            ], 500);
        }
    }

    /**
     * Store Insert Massal Presensi
     */
    public function storeInsertMassal(Request $request)
    {
        return $this->updateMasukMassal($request);
    }

    /**
     * Store Pengolahan & Koreksi Absensi Individu
     */
    public function storePengolahanAbsensi(Request $request)
    {
        try {
            $idAbsensi = $request->input('id_absensi');

            if (!$idAbsensi) {
                return response()->json([
                    'status' => false,
                    'remarks' => 'Gagal: id_absensi wajib diisi.'
                ], 400);
            }

            $updateData = [];
            if ($request->has('jam_masuk')) $updateData['jam_masuk'] = $request->input('jam_masuk');
            if ($request->has('keterlambatan')) $updateData['terlambat'] = (int) $request->input('keterlambatan');
            if ($request->has('posisi_masuk')) $updateData['posisi_masuk'] = $request->input('posisi_masuk');
            if ($request->has('status_masuk')) $updateData['status_masuk'] = $request->input('status_masuk');
            if ($request->has('validasi_masuk')) $updateData['valid_masuk'] = $request->input('validasi_masuk');
            if ($request->has('ket_masuk')) $updateData['ket_masuk'] = $request->input('ket_masuk');

            if ($request->has('jam_pulang')) $updateData['jam_pulang'] = $request->input('jam_pulang');
            if ($request->has('posisi_pulang')) $updateData['posisi_pulang'] = $request->input('posisi_pulang');
            if ($request->has('status_pulang')) $updateData['status_pulang'] = $request->input('status_pulang');
            if ($request->has('validasi_pulang')) $updateData['valid_pulang'] = $request->input('validasi_pulang');

            $updateData['updated_at'] = now();

            DB::table('absensi')->where('id', $idAbsensi)->update($updateData);

            return response()->json([
                'status' => true,
                'remarks' => 'Koreksi data absensi berhasil disimpan!'
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error storePengolahanAbsensi: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'remarks' => 'Terjadi kesalahan server: ' . $e->getMessage()
            ], 500);
        }
    }
}
