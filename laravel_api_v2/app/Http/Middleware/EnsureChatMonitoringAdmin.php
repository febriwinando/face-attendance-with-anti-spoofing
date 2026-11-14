<?php

namespace App\Http\Middleware;

use App\Models\ChatMessage;
use Closure;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Schema;
use Symfony\Component\HttpFoundation\Response;

/**
 * Middleware wajib untuk memvalidasi hak akses Administrator Monitoring Chat (NIP 199402092020121005)
 * secara eksklusif dari user terautentikasi pada server (tidak pernah mempercayai parameter frontend).
 */
class EnsureChatMonitoringAdmin
{
    public function handle(Request $request, Closure $next): Response
    {
        $employee = self::resolveAuthenticatedEmployee($request);

        if (!$employee) {
            return response()->json([
                'success' => false,
                'message' => 'Tidak terautentikasi. Token otorisasi yang valid diperlukan.',
            ], 401);
        }

        if (!ChatMessage::isMonitoringAdminNip($employee->nip ?? null)) {
            return response()->json([
                'success' => false,
                'message' => 'Akses ditolak. Fitur ini khusus untuk Administrator Monitoring Chat (NIP 199402092020121005).',
            ], 403);
        }

        $request->attributes->set('auth_employee', $employee);

        return $next($request);
    }

    /**
     * Mengambil data pegawai terautentikasi dari session/token Sanctum/Passport tanpa membaca input NIP dari request.
     */
    public static function resolveAuthenticatedEmployee(Request $request): ?object
    {
        $cached = $request->attributes->get('auth_employee');
        if (is_object($cached) && !empty($cached->id)) {
            return $cached;
        }

        $user = $request->user();
        if (!$user) {
            return null;
        }

        $nip = is_object($user) ? ($user->nip ?? $user->username ?? null) : null;
        $employeeId = is_object($user) ? ($user->employee_id ?? null) : null;

        if ($employeeId && Schema::hasTable('employees')) {
            $emp = DB::table('employees')->where('id', (int) $employeeId)->first();
            if ($emp) {
                return $emp;
            }
        }

        if (!empty($nip) && Schema::hasTable('employees')) {
            $emp = DB::table('employees')->where('nip', trim((string) $nip))->first();
            if ($emp) {
                return $emp;
            }
        }

        if (is_object($user) && !empty($user->id) && Schema::hasTable('employees')) {
            $empById = DB::table('employees')->where('id', (int) $user->id)->first();
            if ($empById) {
                // Pastikan kecocokan user_id atau email bila tabel users terpisah dari employees
                if (
                    (!isset($user->email) || !isset($empById->email) || $user->email === $empById->email)
                    && (empty($user->nip) || (string) $user->nip === (string) ($empById->nip ?? ''))
                ) {
                    return $empById;
                }
            }
            if (!empty($user->email)) {
                $empByEmail = DB::table('employees')->where('email', $user->email)->first();
                if ($empByEmail) {
                    return $empByEmail;
                }
            }
        }

        if (is_object($user) && !empty($user->id) && !empty($nip)) {
            return (object) [
                'id' => (int) $user->id,
                'nip' => trim((string) $nip),
                'nama' => (string) ($user->nama ?? $user->name ?? ''),
                'opd_id' => isset($user->opd_id) ? (int) $user->opd_id : 0,
            ];
        }

        return null;
    }
}

