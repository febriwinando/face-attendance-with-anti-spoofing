-- ==============================================================================
-- MIGRATION SQL: REKOMENDASI SKEMA TABEL LAYANAN CHAT SAHUT (PEMKO TEBING TINGGI)
-- Tanggal: 10 November 2026
-- Fitur: Hapus Pesan Per-Pengguna ('for_me'), Soft Delete Global, dan Status Syarat Ketentuan
-- ==============================================================================

-- 1. Tambahkan kolom deleted_for_users dan is_deleted_global pada tabel chat_messages
ALTER TABLE `chat_messages`
  ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0: Aktif, 1: Ditandai Dihapus' AFTER `is_read`,
  ADD COLUMN `deleted_for_users` TEXT NULL DEFAULT NULL COMMENT 'Daftar ID pegawai yang menghapus pesan untuk dirinya sendiri (misal: ,105,108,)' AFTER `is_deleted`,
  ADD COLUMN `is_deleted_global` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1: Dihapus Global Kedinasan oleh Super Admin NIP 199402092020121005' AFTER `deleted_for_users`;

-- Indexing untuk kecepatan query performa tinggi
ALTER TABLE `chat_messages`
  ADD INDEX `idx_chat_msg_is_deleted` (`is_deleted`),
  ADD INDEX `idx_chat_msg_is_deleted_global` (`is_deleted_global`);

-- 2. Tambahkan kolom status persetujuan Syarat & Ketentuan (Terms & Conditions) pada tabel employees
ALTER TABLE `employees`
  ADD COLUMN `chat_terms_accepted` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '1: Pegawai telah menyetujui syarat & ketentuan layanan chat' AFTER `active`,
  ADD COLUMN `chat_terms_at` DATETIME NULL DEFAULT NULL COMMENT 'Waktu persetujuan syarat & ketentuan' AFTER `chat_terms_accepted`;

-- 3. Master Switch Fitur Chat (Tabel settings / configurations)
-- Memungkinkan Super Admin NIP 199402092020121005 menampilkan / menyembunyikan menu chat di seluruh perangkat
INSERT INTO `settings` (`key`, `value`, `created_at`, `updated_at`) 
VALUES ('chat_global_enabled', '1', NOW(), NOW())
ON DUPLICATE KEY UPDATE `updated_at` = NOW();

