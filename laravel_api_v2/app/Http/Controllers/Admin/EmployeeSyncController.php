<?php

namespace App\Http\Controllers\Admin;

use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Log;

class EmployeeSyncController extends Controller
{
    /**
     * Mendapatkan Daftar Seluruh Pegawai untuk Sinkronisasi Android
     */
    public function getEmployees()
    {
        try {
            $employees = DB::table('employees')
                ->leftJoin('opds', 'opds.id', '=', 'employees.opd_id')
                ->select(
                    'employees.id',
                    'employees.nip',
                    'employees.nama',
                    'employees.email',
                    'employees.no_hp',
                    'employees.kelompok',
                    'employees.s_jabatan',
                    'employees.eselon',
                    'employees.foto',
                    'employees.shift',
                    'employees.active',
                    'employees.opd_id',
                    'employees.atasan_id1',
                    'employees.atasan_id2',
                    'employees.position_id',
                    'employees.alamat',
                    'employees.let',
                    'employees.lng',
                    'employees.awal_waktu',
                    'employees.opd_shift',
                    'opds.opd as opd'
                )
                ->get();

            return response()->json([
                'success' => true,
                'total'   => $employees->count(),
                'data'    => $employees
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error getEmployees: ' . $e->getMessage());
            return response()->json([
                'success' => false,
                'total'   => 0,
                'data'    => []
            ], 500);
        }
    }

    /**
     * Mendapatkan Daftar Unit Kerja / OPD
     */
    public function getOpdList()
    {
        try {
            $opds = DB::table('opds')
                ->select('id', 'opd', DB::raw("opd as singkatan"))
                ->orderBy('opd', 'asc')
                ->get();

            return response()->json([
                'status' => true,
                'data'   => $opds
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error getOpdList: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'data'   => []
            ], 500);
        }
    }

    /**
     * Mengubah Status Aktif/Non-Aktif Akun Pegawai
     */
    public function updateStatusPegawai(Request $request)
    {
        try {
            $id = $request->input('id');
            $active = (int) $request->input('active', 1);

            if (!$id) {
                return response()->json([
                    'status' => false,
                    'remarks' => 'Gagal: ID pegawai tidak boleh kosong.'
                ], 400);
            }

            DB::table('employees')
                ->where('id', $id)
                ->orWhere('nip', $id)
                ->update(['active' => $active]);

            return response()->json([
                'status' => true,
                'remarks' => 'Status aktif pegawai berhasil diperbarui.'
            ], 200);

        } catch (\Exception $e) {
            Log::error('Error updateStatusPegawai: ' . $e->getMessage());
            return response()->json([
                'status' => false,
                'remarks' => 'Terjadi kesalahan server: ' . $e->getMessage()
            ], 500);
        }
    }
}
