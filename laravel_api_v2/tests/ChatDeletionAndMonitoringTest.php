<?php

/**
 * Automated Verification Suite for Laravel Chat Deletion, Policy, Middleware,
 * Audit Logging, Monitoring Filters, and Two-Stage Permanent Delete.
 *
 * Uses a real in-memory SQLite PDO database plus minimal Illuminate framework shims
 * so migrations, models, middleware, policies, services, and controller execute end-to-end.
 */

declare(strict_types=1);

namespace Illuminate\Database\Schema {
    class ColumnDefinition
    {
        public string $name;
        public string $type;
        public bool $nullable = false;
        public mixed $default = null;
        public bool $hasDefault = false;
        public bool $useCurrent = false;
        public ?string $after = null;

        public function __construct(string $name, string $type)
        {
            $this->name = $name;
            $this->type = $type;
        }

        public function nullable(): self { $this->nullable = true; return $this; }
        public function default(mixed $val): self { $this->default = $val; $this->hasDefault = true; return $this; }
        public function after(string $col): self { $this->after = $col; return $this; }
        public function useCurrent(): self { $this->useCurrent = true; return $this; }
        public function index(): self { return $this; }
    }

    class Blueprint
    {
        public string $table;
        public array $columns = [];
        public array $indexes = [];
        public array $droppedColumns = [];
        public array $droppedIndexes = [];

        public function __construct(string $table) { $this->table = $table; }
        public function bigIncrements(string $col): ColumnDefinition { return $this->add($col, 'INTEGER PRIMARY KEY AUTOINCREMENT'); }
        public function unsignedBigInteger(string $col): ColumnDefinition { return $this->add($col, 'INTEGER'); }
        public function string(string $col, int $len = 255): ColumnDefinition { return $this->add($col, 'TEXT'); }
        public function text(string $col): ColumnDefinition { return $this->add($col, 'TEXT'); }
        public function longText(string $col): ColumnDefinition { return $this->add($col, 'TEXT'); }
        public function boolean(string $col): ColumnDefinition { return $this->add($col, 'INTEGER'); }
        public function timestamp(string $col): ColumnDefinition { return $this->add($col, 'TEXT'); }
        public function json(string $col): ColumnDefinition { return $this->add($col, 'TEXT'); }
        public function index(array|string $cols, ?string $name = null): void { $this->indexes[] = [$name ?? ('idx_' . uniqid()), (array) $cols]; }
        public function dropColumn(array|string $cols): void { $this->droppedColumns = array_merge($this->droppedColumns, (array) $cols); }
        public function dropIndex(string $name): void { $this->droppedIndexes[] = $name; }

        private function add(string $col, string $type): ColumnDefinition
        {
            $def = new ColumnDefinition($col, $type);
            $this->columns[] = $def;
            return $def;
        }
    }

    class Schema
    {
        public static function hasTable(string $table): bool
        {
            $pdo = \Illuminate\Support\Facades\DB::pdo();
            $stmt = $pdo->prepare("SELECT name FROM sqlite_master WHERE type='table' AND name=?");
            $stmt->execute([$table]);
            return (bool) $stmt->fetchColumn();
        }

        public static function hasColumn(string $table, string $column): bool
        {
            $pdo = \Illuminate\Support\Facades\DB::pdo();
            $stmt = $pdo->query("PRAGMA table_info(\"{$table}\")");
            foreach ($stmt->fetchAll(\PDO::FETCH_ASSOC) as $row) {
                if ($row['name'] === $column) {
                    return true;
                }
            }
            return false;
        }

        public static function create(string $table, callable $callback): void
        {
            $bp = new Blueprint($table);
            $callback($bp);
            $parts = [];
            foreach ($bp->columns as $col) {
                if (str_contains($col->type, 'PRIMARY KEY')) {
                    $parts[] = "\"{$col->name}\" {$col->type}";
                    continue;
                }
                $line = "\"{$col->name}\" {$col->type}";
                if ($col->hasDefault) {
                    $def = is_bool($col->default) ? ($col->default ? '1' : '0') : (is_numeric($col->default) ? (string) $col->default : "'{$col->default}'");
                    $line .= " DEFAULT {$def}";
                } elseif ($col->useCurrent) {
                    $line .= " DEFAULT CURRENT_TIMESTAMP";
                } elseif ($col->nullable) {
                    $line .= " DEFAULT NULL";
                }
                $parts[] = $line;
            }
            \Illuminate\Support\Facades\DB::pdo()->exec("CREATE TABLE \"{$table}\" (" . implode(', ', $parts) . ")");
        }

        public static function table(string $table, callable $callback): void
        {
            $bp = new Blueprint($table);
            $callback($bp);
            $pdo = \Illuminate\Support\Facades\DB::pdo();
            foreach ($bp->columns as $col) {
                if (self::hasColumn($table, $col->name)) {
                    continue;
                }
                $line = "\"{$col->name}\" {$col->type}";
                if ($col->hasDefault) {
                    $def = is_bool($col->default) ? ($col->default ? '1' : '0') : (is_numeric($col->default) ? (string) $col->default : "'{$col->default}'");
                    $line .= " DEFAULT {$def}";
                } else {
                    $line .= " DEFAULT NULL";
                }
                $pdo->exec("ALTER TABLE \"{$table}\" ADD COLUMN {$line}");
            }
            foreach ($bp->indexes as [$idxName, $cols]) {
                $colsSql = implode(', ', array_map(fn ($c) => "\"{$c}\"", $cols));
                $pdo->exec("CREATE INDEX IF NOT EXISTS \"{$idxName}\" ON \"{$table}\" ({$colsSql})");
            }
        }

        public static function dropIfExists(string $table): void
        {
            \Illuminate\Support\Facades\DB::pdo()->exec("DROP TABLE IF EXISTS \"{$table}\"");
        }
    }
}

namespace Illuminate\Database\Migrations {
    abstract class Migration {}
}

namespace Illuminate\Database\Eloquent {
    class Collection implements \Countable, \IteratorAggregate
    {
        public function __construct(private array $items = []) {}
        public function isEmpty(): bool { return empty($this->items); }
        public function count(): int { return count($this->items); }
        public function all(): array { return $this->items; }
        public function pluck(string $key): self
        {
            return new self(array_map(fn ($item) => is_array($item) ? ($item[$key] ?? null) : ($item->{$key} ?? null), $this->items));
        }
        public function getIterator(): \Traversable { return new \ArrayIterator($this->items); }
        public function filter(callable $cb): self { return new self(array_values(array_filter($this->items, $cb))); }
        public function values(): self { return new self(array_values($this->items)); }
    }

    class Builder extends \Illuminate\Database\Query\Builder
    {
        private string $modelClass;

        public function __construct(\PDO $pdo, string $table, string $modelClass)
        {
            parent::__construct($pdo, $table);
            $this->modelClass = $modelClass;
        }

        public function find(int $id): ?Model
        {
            return $this->where('id', $id)->first();
        }

        public function first(): mixed
        {
            $clone = clone $this;
            $clone->limitVal = 1;
            $items = self::getRawRows($clone);
            if (empty($items)) {
                return null;
            }
            return ($this->modelClass)::hydrateRow((array) $items[0]);
        }

        private static function getRawRows(\Illuminate\Database\Query\Builder $b): array
        {
            return \Illuminate\Database\Query\Builder::getRaw($b);
        }

        public function get(): Collection
        {
            $rows = \Illuminate\Database\Query\Builder::getRaw($this);
            $models = [];
            foreach ($rows as $row) {
                $models[] = ($this->modelClass)::hydrateRow((array) $row);
            }
            return new Collection($models);
        }

        public function __call(string $method, array $args): mixed
        {
            $scopeMethod = 'scope' . ucfirst($method);
            $instance = new $this->modelClass();
            if (method_exists($instance, $scopeMethod)) {
                return $instance->{$scopeMethod}($this, ...$args) ?? $this;
            }
            throw new \BadMethodCallException("Method {$method} does not exist on " . static::class);
        }
    }

    abstract class Model
    {
        protected $table;
        protected $fillable = [];
        protected $casts = [];
        public $timestamps = true;
        public array $attributes = [];

        public function __construct(array $attrs = [])
        {
            $this->attributes = $attrs;
        }

        public static function hydrateRow(array $row): static
        {
            $m = new static();
            foreach ($row as $k => $v) {
                $m->attributes[$k] = $v;
            }
            return $m;
        }

        public function __get(string $key): mixed
        {
            $val = $this->attributes[$key] ?? null;
            if (isset($this->casts[$key]) && $val !== null) {
                return match ($this->casts[$key]) {
                    'integer' => (int) $val,
                    'boolean' => (bool) (int) $val,
                    'array' => is_string($val) ? json_decode($val, true) : $val,
                    default => $val,
                };
            }
            return $val;
        }

        public function __isset(string $key): bool
        {
            return array_key_exists($key, $this->attributes) && $this->attributes[$key] !== null;
        }

        public function __set(string $key, mixed $val): void
        {
            $this->attributes[$key] = $val;
        }

        public static function query(): Builder
        {
            $inst = new static();
            return new Builder(\Illuminate\Support\Facades\DB::pdo(), $inst->table, static::class);
        }
    }
}

namespace Illuminate\Database\Eloquent\Relations {
    class HasMany {}
}

namespace Illuminate\Database\Query {
    class RawExpr {
        public function __construct(public string $sql) {}
    }

    class Builder
    {
        protected \PDO $pdo;
        protected string $table;
        protected array $joins = [];
        protected array $selects = ['*'];
        protected array $wheres = [];
        protected array $bindings = [];
        protected array $orders = [];
        protected ?int $limitVal = null;
        protected ?int $offsetVal = null;

        public function __construct(\PDO $pdo, string $table)
        {
            $this->pdo = $pdo;
            $this->table = $table;
        }

        public function select(array $cols): self { $this->selects = $cols; return $this; }

        public function leftJoin(string $table, string $first, string $op, string $second): self
        {
            $this->joins[] = "LEFT JOIN {$this->formatTable($table)} ON {$this->col($first)} {$op} {$this->col($second)}";
            return $this;
        }

        public function where(string|\Closure $col, mixed $op = null, mixed $val = null, string $boolean = 'AND'): self
        {
            if ($col instanceof \Closure) {
                $nested = new self($this->pdo, $this->table);
                $col($nested);
                if (!empty($nested->wheres)) {
                    $sql = '(' . $nested->compileWheresSql() . ')';
                    $this->wheres[] = [$boolean, $sql];
                    $this->bindings = array_merge($this->bindings, $nested->bindings);
                }
                return $this;
            }
            if (func_num_args() === 2) {
                $val = $op;
                $op = '=';
            }
            $this->wheres[] = [$boolean, "{$this->col($col)} {$op} ?"];
            $this->bindings[] = $val;
            return $this;
        }

        public function orWhere(string|\Closure $col, mixed $op = null, mixed $val = null): self
        {
            if ($col instanceof \Closure) {
                return $this->where($col, null, null, 'OR');
            }
            if (func_num_args() === 2) {
                $val = $op;
                $op = '=';
            }
            return $this->where($col, $op, $val, 'OR');
        }

        public function whereIn(string $col, array $vals, string $boolean = 'AND', bool $not = false): self
        {
            if (empty($vals)) {
                $this->wheres[] = [$boolean, $not ? '1=1' : '1=0'];
                return $this;
            }
            $placeholders = implode(', ', array_fill(0, count($vals), '?'));
            $kw = $not ? 'NOT IN' : 'IN';
            $this->wheres[] = [$boolean, "{$this->col($col)} {$kw} ({$placeholders})"];
            foreach ($vals as $v) {
                $this->bindings[] = $v;
            }
            return $this;
        }

        public function whereNotIn(string $col, array $vals): self
        {
            return $this->whereIn($col, $vals, 'AND', true);
        }

        public function whereNotNull(string $col): self
        {
            $this->wheres[] = ['AND', "{$this->col($col)} IS NOT NULL"];
            return $this;
        }

        public function whereNull(string $col): self
        {
            $this->wheres[] = ['AND', "{$this->col($col)} IS NULL"];
            return $this;
        }

        public function orWhereNull(string $col): self
        {
            $this->wheres[] = ['OR', "{$this->col($col)} IS NULL"];
            return $this;
        }

        public function whereDate(string $col, string $op, ?string $val = null): self
        {
            if ($val === null) {
                $val = $op;
                $op = '=';
            }
            $this->wheres[] = ['AND', "DATE({$this->col($col)}) {$op} ?"];
            $this->bindings[] = $val;
            return $this;
        }

        public function whereRaw(string $sql, array $bindings = []): self
        {
            $this->wheres[] = ['AND', $sql];
            foreach ($bindings as $b) {
                $this->bindings[] = $b;
            }
            return $this;
        }

        public function orWhereRaw(string $sql, array $bindings = []): self
        {
            $this->wheres[] = ['OR', $sql];
            foreach ($bindings as $b) {
                $this->bindings[] = $b;
            }
            return $this;
        }

        public function lockForUpdate(): self { return $this; }
        public function orderBy(string $col, string $dir = 'asc'): self { $this->orders[] = "{$this->col($col)} " . strtoupper($dir); return $this; }
        public function offset(int $n): self { $this->offsetVal = $n; return $this; }
        public function limit(int $n): self { $this->limitVal = $n; return $this; }

        public function count(): int
        {
            $sql = "SELECT COUNT(*) FROM {$this->formatTable($this->table)} " . implode(' ', $this->joins);
            if (!empty($this->wheres)) {
                $sql .= ' WHERE ' . $this->compileWheresSql();
            }
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute($this->bindings);
            return (int) $stmt->fetchColumn();
        }

        public function exists(): bool { return $this->count() > 0; }

        public function first(): mixed
        {
            $clone = clone $this;
            $clone->limitVal = 1;
            $items = self::getRaw($clone);
            return $items[0] ?? null;
        }

        public static function getRaw(self $b): array
        {
            $cols = implode(', ', array_map(fn ($s) => $b->formatSelect($s), $b->selects));
            $sql = "SELECT {$cols} FROM {$b->formatTable($b->table)} " . implode(' ', $b->joins);
            if (!empty($b->wheres)) {
                $sql .= ' WHERE ' . $b->compileWheresSql();
            }
            if (!empty($b->orders)) {
                $sql .= ' ORDER BY ' . implode(', ', $b->orders);
            }
            if ($b->limitVal !== null) {
                $sql .= ' LIMIT ' . $b->limitVal;
            }
            if ($b->offsetVal !== null) {
                $sql .= ' OFFSET ' . $b->offsetVal;
            }
            $stmt = $b->pdo->prepare($sql);
            $stmt->execute($b->bindings);
            return array_map(fn ($r) => (object) $r, $stmt->fetchAll(\PDO::FETCH_ASSOC));
        }

        public function get(): \Illuminate\Database\Eloquent\Collection
        {
            return new \Illuminate\Database\Eloquent\Collection(self::getRaw($this));
        }

        public function pluck(string $col): \Illuminate\Database\Eloquent\Collection
        {
            return $this->get()->pluck($col);
        }

        public function insert(array $values): bool
        {
            $cols = array_keys($values);
            $placeholders = implode(', ', array_fill(0, count($cols), '?'));
            $colsSql = implode(', ', array_map(fn ($c) => "\"{$c}\"", $cols));
            $stmt = $this->pdo->prepare("INSERT INTO \"{$this->pureTable()}\" ({$colsSql}) VALUES ({$placeholders})");
            return $stmt->execute(array_values($values));
        }

        public function update(array $values): int
        {
            $sets = [];
            $params = [];
            foreach ($values as $k => $v) {
                if ($v instanceof RawExpr) {
                    $sets[] = "\"{$k}\" = {$v->sql}";
                } else {
                    $sets[] = "\"{$k}\" = ?";
                    $params[] = $v;
                }
            }
            $sql = "UPDATE \"{$this->pureTable()}\" SET " . implode(', ', $sets);
            if (!empty($this->wheres)) {
                $sql .= ' WHERE ' . $this->compileWheresSql();
                $params = array_merge($params, $this->bindings);
            }
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute($params);
            return $stmt->rowCount();
        }

        public function delete(): int
        {
            $sql = "DELETE FROM \"{$this->pureTable()}\"";
            if (!empty($this->wheres)) {
                $sql .= ' WHERE ' . $this->compileWheresSql();
            }
            $stmt = $this->pdo->prepare($sql);
            $stmt->execute($this->bindings);
            return $stmt->rowCount();
        }

        public function compileWheresSql(): string
        {
            $out = '';
            foreach ($this->wheres as $i => [$bool, $sql]) {
                $out .= ($i === 0 ? '' : " {$bool} ") . $sql;
            }
            return $out;
        }

        private function pureTable(): string { return preg_split('/\s+as\s+/i', $this->table)[0]; }

        private function formatTable(string $table): string
        {
            if (preg_match('/^(\S+)\s+as\s+(\S+)$/i', $table, $m)) {
                return "\"{$m[1]}\" AS \"{$m[2]}\"";
            }
            return "\"{$table}\"";
        }

        private function col(string $c): string
        {
            if (str_contains($c, '.')) {
                [$t, $f] = explode('.', $c, 2);
                return $f === '*' ? "\"{$t}\".*" : "\"{$t}\".\"{$f}\"";
            }
            return "\"{$c}\"";
        }

        private function formatSelect(string $s): string
        {
            if (preg_match('/^(.+)\s+as\s+(\S+)$/i', $s, $m)) {
                return $this->col(trim($m[1])) . " AS \"{$m[2]}\"";
            }
            return $s === '*' ? '*' : $this->col($s);
        }
    }
}

namespace Illuminate\Support\Facades {
    class Schema extends \Illuminate\Database\Schema\Schema {}

    class DB
    {
        private static ?\PDO $connection = null;
        private static int $txDepth = 0;

        public static function setPdo(\PDO $pdo): void { self::$connection = $pdo; }
        public static function pdo(): \PDO { return self::$connection; }

        public static function table(string $table): \Illuminate\Database\Query\Builder
        {
            return new \Illuminate\Database\Query\Builder(self::$connection, $table);
        }

        public static function raw(string $sql): \Illuminate\Database\Query\RawExpr
        {
            return new \Illuminate\Database\Query\RawExpr($sql);
        }

        public static function transaction(callable $cb): mixed
        {
            $startedHere = false;
            if (self::$txDepth === 0) {
                self::$connection->beginTransaction();
                $startedHere = true;
            }
            self::$txDepth++;
            try {
                $res = $cb();
                if ($startedHere) {
                    self::$connection->commit();
                }
                return $res;
            } catch (\Throwable $e) {
                if ($startedHere && self::$connection->inTransaction()) {
                    self::$connection->rollBack();
                }
                throw $e;
            } finally {
                self::$txDepth--;
            }
        }
    }

    class Http
    {
        public static function withHeaders(array $h): object
        {
            return new class {
                public function timeout(int $t): self { return $this; }
                public function post(string $u, array $p): array { return []; }
            };
        }
    }
}

namespace Illuminate\Http {
    class AttributeBag {
        private array $items = [];
        public function get(string $k, mixed $d = null): mixed { return $this->items[$k] ?? $d; }
        public function set(string $k, mixed $v): void { $this->items[$k] = $v; }
    }

    class Request
    {
        public AttributeBag $attributes;
        private mixed $authUser;
        private array $inputs;

        public function __construct(array $inputs = [], mixed $authUser = null)
        {
            $this->attributes = new AttributeBag();
            $this->inputs = $inputs;
            $this->authUser = $authUser;
        }

        public function user(): mixed { return $this->authUser; }
        public function input(string $k, mixed $d = null): mixed { return $this->inputs[$k] ?? $d; }
        public function merge(array $extra): void { $this->inputs = array_merge($this->inputs, $extra); }
    }

    class JsonResponse extends \Symfony\Component\HttpFoundation\Response
    {
        public array $data;
        public function __construct(array $data = [], int $status = 200)
        {
            parent::__construct(json_encode($data), $status);
            $this->data = $data;
        }
    }
}

namespace Symfony\Component\HttpFoundation {
    class Response
    {
        public function __construct(public string $content = '', public int $status = 200) {}
        public function getStatusCode(): int { return $this->status; }
    }
}

namespace App\Http\Controllers {
    abstract class Controller {}
}

namespace {
    function response(): object
    {
        return new class {
            public function json(array $data = [], int $status = 200): \Illuminate\Http\JsonResponse
            {
                return new \Illuminate\Http\JsonResponse($data, $status);
            }
        };
    }

    // Load actual application files under test
    $base = dirname(__DIR__);
    require_once $base . '/app/Models/ChatMessage.php';
    require_once $base . '/app/Models/ChatDeletionAuditLog.php';
    require_once $base . '/app/Http/Middleware/EnsureChatMonitoringAdmin.php';
    require_once $base . '/app/Policies/ChatMessagePolicy.php';
    require_once $base . '/app/Services/ChatDeletionService.php';
    require_once $base . '/app/Http/Controllers/Chat/ChatController.php';

    // Boot in-memory SQLite schema mimicking existing tables prior to migrations
    $pdo = new PDO('sqlite::memory:');
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    \Illuminate\Support\Facades\DB::setPdo($pdo);

    $pdo->exec('CREATE TABLE opds (id INTEGER PRIMARY KEY AUTOINCREMENT, opd TEXT)');
    $pdo->exec('CREATE TABLE employees (id INTEGER PRIMARY KEY AUTOINCREMENT, nip TEXT, nama TEXT, email TEXT, opd_id INTEGER, active INTEGER DEFAULT 1, chat_terms_accepted INTEGER DEFAULT 0, chat_terms_accepted_at TEXT DEFAULT NULL)');
    $pdo->exec('CREATE TABLE settings (id INTEGER PRIMARY KEY AUTOINCREMENT, key TEXT, value TEXT, created_at TEXT, updated_at TEXT)');
    $pdo->exec('CREATE TABLE chat_messages (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        sender_id INTEGER NOT NULL,
        receiver_id INTEGER DEFAULT NULL,
        opd_id INTEGER DEFAULT NULL,
        chat_type TEXT NOT NULL DEFAULT "personal",
        message TEXT DEFAULT NULL,
        msg_type TEXT NOT NULL DEFAULT "text",
        file_url TEXT DEFAULT NULL,
        file_name TEXT DEFAULT NULL,
        reply_to_id INTEGER DEFAULT NULL,
        reply_to_sender_name TEXT DEFAULT NULL,
        reply_to_message TEXT DEFAULT NULL,
        is_read INTEGER NOT NULL DEFAULT 0,
        created_at TEXT DEFAULT NULL,
        updated_at TEXT DEFAULT NULL
    )');

    // Execute both PHP migrations
    $mig1 = require $base . '/database/migrations/2026_11_12_000001_update_chat_messages_deletion_and_read_columns.php';
    $mig2 = require $base . '/database/migrations/2026_11_12_000002_create_chat_deletion_audit_logs_table.php';
    $mig1->up();
    $mig2->up();

    // Seed OPDs and Employees
    $pdo->exec("INSERT INTO opds (id, opd) VALUES (10, 'Dinas Komunikasi dan Informatika'), (20, 'Badan Kepegawaian Daerah')");
    $pdo->exec("INSERT INTO employees (id, nip, nama, email, opd_id) VALUES
        (1, '199402092020121005', 'SAHUT Admin Monitoring', 'admin@tebingtinggi.go.id', 10),
        (2, '199001012015011001', 'Budi Santoso', 'budi@tebingtinggi.go.id', 10),
        (3, '199202022018022002', 'Siti Rahma', 'siti@tebingtinggi.go.id', 20),
        (4, '199402092020129999', 'Bukan Admin Prefix Sama', 'prefix@tebingtinggi.go.id', 10),
        (5, '199505052020031005', 'Andi Diskominfo', 'andi@tebingtinggi.go.id', 10),
        (6, '199606062021042006', 'Dewi Diskominfo', 'dewi@tebingtinggi.go.id', 10)
    ");

    $userAdmin = (object) ['id' => 1, 'nip' => '199402092020121005', 'nama' => 'SAHUT Admin Monitoring', 'opd_id' => 10];
    $userBudi = (object) ['id' => 2, 'nip' => '199001012015011001', 'nama' => 'Budi Santoso', 'opd_id' => 10];
    $userSiti = (object) ['id' => 3, 'nip' => '199202022018022002', 'nama' => 'Siti Rahma', 'opd_id' => 20];
    $userFakePrefix = (object) ['id' => 4, 'nip' => '199402092020129999', 'nama' => 'Bukan Admin Prefix Sama', 'opd_id' => 10];
    $userAndi = (object) ['id' => 5, 'nip' => '199505052020031005', 'nama' => 'Andi Diskominfo', 'opd_id' => 10];
    $userDewi = (object) ['id' => 6, 'nip' => '199606062021042006', 'nama' => 'Dewi Diskominfo', 'opd_id' => 10];

    $controller = new \App\Http\Controllers\Chat\ChatController();
    $middleware = new \App\Http\Middleware\EnsureChatMonitoringAdmin();

    $passCount = 0;
    function assertTrue(bool $cond, string $label): void {
        global $passCount;
        if (!$cond) {
            fwrite(STDERR, "FAIL: {$label}\n");
            exit(1);
        }
        $passCount++;
    }
    function assertSame(mixed $expected, mixed $actual, string $label): void {
        global $passCount;
        if ($expected !== $actual) {
            fwrite(STDERR, "FAIL: {$label} | expected=" . var_export($expected, true) . " actual=" . var_export($actual, true) . "\n");
            exit(1);
        }
        $passCount++;
    }

    // Check migration columns and audit table created
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'deleted_for_sender'), 'Migration added deleted_for_sender');
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'deleted_for_receiver'), 'Migration added deleted_for_receiver');
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'deleted_for_everyone'), 'Migration added deleted_for_everyone');
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'deleted_by'), 'Migration added deleted_by');
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'deleted_at'), 'Migration added deleted_at');
    assertTrue(\Illuminate\Database\Schema\Schema::hasColumn('chat_messages', 'read_at'), 'Migration added read_at');
    assertTrue(\Illuminate\Database\Schema\Schema::hasTable('chat_deletion_audit_logs'), 'Migration created chat_deletion_audit_logs');

    // Test 1 & 9: Exclusive NIP Middleware + Anti-spoofing
    $resUnauth = $middleware->handle(new \Illuminate\Http\Request(), fn ($r) => response()->json(['ok' => true]));
    assertSame(401, $resUnauth->getStatusCode(), 'Unauthenticated blocked with 401');

    $spoofReq = new \Illuminate\Http\Request(['nip' => '199402092020121005', 'user_id' => 1, 'is_admin' => 1], $userBudi);
    $resSpoof = $middleware->handle($spoofReq, fn ($r) => response()->json(['ok' => true]));
    assertSame(403, $resSpoof->getStatusCode(), 'Non-admin spoofing NIP blocked with 403');

    $prefixReq = new \Illuminate\Http\Request([], $userFakePrefix);
    $resPrefix = $middleware->handle($prefixReq, fn ($r) => response()->json(['ok' => true]));
    assertSame(403, $resPrefix->getStatusCode(), 'Prefix-only NIP 199402092020129999 rejected with 403');

    $adminReq = new \Illuminate\Http\Request([], $userAdmin);
    $resAdmin = $middleware->handle($adminReq, fn ($r) => response()->json(['ok' => true]));
    assertSame(200, $resAdmin->getStatusCode(), 'Exact Admin NIP 199402092020121005 allowed');

    // Seed Personal Messages:
    // Msg 1: Budi -> Siti (already READ)
    // Msg 2: Budi -> Siti (UNREAD)
    // Msg 3: Budi -> Andi (personal, already READ - for isolation check)
    // Msg 4: Budi -> OPD 10 (UNREAD)
    // Msg 5: Budi -> OPD 10 (will be read by Andi -> READ)
    $pdo->exec("INSERT INTO chat_messages (id, sender_id, receiver_id, opd_id, chat_type, message, msg_type, is_read, read_at, created_at) VALUES
        (1, 2, 3, NULL, 'personal', 'Halo Siti sudah dibaca', 'text', 1, '2026-11-12 08:00:00', '2026-11-12 07:55:00'),
        (2, 2, 3, NULL, 'personal', 'Pesan belum dibaca Budi', 'text', 0, NULL, '2026-11-12 08:05:00'),
        (3, 2, 5, NULL, 'personal', 'Percakapan lain dengan Andi', 'text', 1, '2026-11-12 08:10:00', '2026-11-12 08:09:00'),
        (4, 2, NULL, 10, 'opd', 'Pesan grup OPD belum dibaca', 'text', 0, NULL, '2026-11-12 08:15:00'),
        (5, 2, NULL, 10, 'opd', 'Pesan grup OPD kedua', 'text', 0, NULL, '2026-11-12 08:16:00')
    ");

    // Test 2: Message READ -> deleteForEveryone denied (422), deleteForMe succeeds and keeps visible to receiver
    $respReadAll = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 1, 'delete_type' => 'for_everyone'], $userBudi));
    assertSame(422, $respReadAll->getStatusCode(), 'Already read message cannot be deleted for everyone');

    $respReadMe = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 1, 'delete_type' => 'for_me'], $userBudi));
    assertSame(200, $respReadMe->getStatusCode(), 'Sender soft delete for self succeeds');
    $readMeData = $respReadMe->data['data'];
    assertTrue(is_array($readMeData), 'deleteMessage returns formatted array data');
    assertTrue(
        is_int($readMeData['deleted_for_sender'])
        && is_int($readMeData['deleted_for_receiver'])
        && is_int($readMeData['deleted_for_everyone'])
        && is_int($readMeData['is_deleted'])
        && is_int($readMeData['is_deleted_global']),
        'deleteMessage returns int flags for Gson ChatMessageItem parity'
    );
    assertTrue(is_bool($readMeData['is_me']) && is_bool($readMeData['is_read']), 'deleteMessage returns boolean is_me/is_read for Gson');

    $row1 = $pdo->query('SELECT * FROM chat_messages WHERE id = 1')->fetch(PDO::FETCH_ASSOC);
    assertSame(1, (int) $row1['deleted_for_sender'], 'DB deleted_for_sender set to 1');
    assertSame(0, (int) $row1['deleted_for_everyone'], 'DB deleted_for_everyone remains 0');
    assertSame('199001012015011001', $row1['deleted_by'], 'DB deleted_by records sender NIP');
    assertSame('Halo Siti sudah dibaca', $row1['message'], 'Original message content preserved in DB');

    // Sender no longer sees msg 1 in chat/messages
    $budiView = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'personal', 'target_id' => 3], $userBudi));
    $budiIds = array_column($budiView->data['data'], 'id');
    assertTrue(!in_array(1, $budiIds, true), 'Msg 1 hidden for Budi');
    assertTrue(in_array(2, $budiIds, true), 'Msg 2 still visible for Budi');

    // Note: Check Siti sees Msg 1 WITHOUT calling getMessages before testing Msg 2 unread delete-for-everyone!
    // Check Monitoring BEFORE Siti opens chat so Msg 2 stays unread:
    $monBefore = $controller->monitoringConversationDetail(new \Illuminate\Http\Request([
        'chat_type' => 'personal', 'participant_a' => 2, 'participant_b' => 3
    ], $userAdmin));
    assertSame(2, count($monBefore->data['data']), 'Monitoring sees both deleted and active messages');
    assertSame('deleted_for_sender', $monBefore->data['data'][0]['delete_status'], 'Monitoring shows deleted_for_sender status');
    assertSame(0, $monBefore->data['data'][1]['is_read'], 'Viewing Monitoring MUST NOT mark unread employee message as read');
    assertSame(null, $monBefore->data['data'][1]['read_at'], 'Monitoring read_at remains null');

    // Test 4: Non-sender Siti tries delete_for_everyone on unread Msg 2 -> 403
    $sitiForEveryone = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 2, 'delete_type' => 'for_everyone'], $userSiti));
    assertSame(403, $sitiForEveryone->getStatusCode(), 'Receiver cannot delete_for_everyone');

    // Test 3: Sender Budi deletes unread Msg 2 for everyone -> 200, hidden for both Budi & Siti, preserved for Admin
    $budiForEveryone = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 2, 'delete_type' => 'for_everyone'], $userBudi));
    assertSame(200, $budiForEveryone->getStatusCode(), 'Sender deletes unread Msg 2 for everyone');
    $feData = $budiForEveryone->data['data'];
    assertSame(1, $feData['deleted_for_everyone'], 'delete_for_everyone payload flag is int 1');
    assertSame(1, $feData['is_deleted_global'], 'is_deleted_global payload flag is int 1');
    assertTrue(is_int($feData['deleted_for_sender']) && is_int($feData['deleted_for_receiver']) && is_int($feData['is_deleted']), 'delete_for_everyone deletion flags are int for Gson');
    assertTrue(is_bool($feData['is_me']) && is_bool($feData['is_read']), 'delete_for_everyone is_me/is_read are bool for Gson');
    $row2 = $pdo->query('SELECT * FROM chat_messages WHERE id = 2')->fetch(PDO::FETCH_ASSOC);
    assertSame(1, (int) $row2['deleted_for_everyone'], 'Msg 2 deleted_for_everyone=1');
    assertSame('Pesan belum dibaca Budi', $row2['message'], 'Msg 2 text stays intact in DB');

    // Now Siti opens personal chat with Budi -> gets Msg 1 (visible to Siti!), does NOT get Msg 2 (deleted_for_everyone)
    $sitiView = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'personal', 'target_id' => 2], $userSiti));
    $sitiIds = array_column($sitiView->data['data'], 'id');
    assertTrue(in_array(1, $sitiIds, true), 'Siti still sees Msg 1 deleted only by sender');
    assertTrue(!in_array(2, $sitiIds, true), 'Siti does not see Msg 2 deleted_for_everyone');

    // Receiver Siti now deletes Msg 1 for herself
    $sitiDelete1 = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 1, 'delete_type' => 'for_me'], $userSiti));
    assertSame(200, $sitiDelete1->getStatusCode(), 'Siti deletes Msg 1 for self');
    $row1AfterSiti = $pdo->query('SELECT * FROM chat_messages WHERE id = 1')->fetch(PDO::FETCH_ASSOC);
    assertSame(1, (int) $row1AfterSiti['deleted_for_receiver'], 'Msg 1 deleted_for_receiver=1');

    // Test 5: OPD Group chat deleteForEveryone when unread, and blocked after read by another OPD member
    $opdDelUnread = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 4, 'delete_type' => 'for_everyone'], $userBudi));
    assertSame(200, $opdDelUnread->getStatusCode(), 'Unread OPD message deleted for everyone');

    // Andi (OPD 10) opens OPD group -> sees Msg 5 (not Msg 4), and marks Msg 5 read
    $andiOpdView = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'opd', 'target_id' => 10], $userAndi));
    assertSame([5], array_column($andiOpdView->data['data'], 'id'), 'Andi sees only active OPD Msg 5');
    $row5AfterAndi = $pdo->query('SELECT * FROM chat_messages WHERE id = 5')->fetch(PDO::FETCH_ASSOC);
    assertSame(1, (int) $row5AfterAndi['is_read'], 'OPD Msg 5 marked read when opened by another OPD member');

    // Budi tries delete_for_everyone on OPD Msg 5 after Andi read it -> rejected 422
    $budiOpdDelAllAfterRead = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 5, 'delete_type' => 'for_everyone'], $userBudi));
    assertSame(422, $budiOpdDelAllAfterRead->getStatusCode(), 'OPD message read by peer cannot be deleted for everyone');

    // Budi deletes OPD Msg 5 for self -> Budi hides it, Andi still sees it
    $budiOpdDelMe = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 5, 'delete_type' => 'for_me'], $userBudi));
    assertSame(200, $budiOpdDelMe->getStatusCode(), 'OPD soft delete for self succeeds');
    $budiOpdAfter = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'opd', 'target_id' => 10], $userBudi));
    assertSame([], $budiOpdAfter->data['data'], 'Budi sees empty OPD messages after deleting');
    $andiOpdStill = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'opd', 'target_id' => 10], $userAndi));
    assertSame([5], array_column($andiOpdStill->data['data'], 'id'), 'Andi still reads OPD Msg 5');

    // Peer Andi deletes OPD Msg 5 for self -> Andi hides it, Peer Dewi still sees OPD Msg 5!
    $andiDelOpdMe = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 5, 'delete_type' => 'for_me'], $userAndi));
    assertSame(200, $andiDelOpdMe->getStatusCode(), 'Peer Andi deletes OPD Msg 5 for self');
    $andiOpdHidden = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'opd', 'target_id' => 10], $userAndi));
    assertSame([], $andiOpdHidden->data['data'], 'OPD Msg 5 hidden for Andi');
    $dewiOpdVisible = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'opd', 'target_id' => 10], $userDewi));
    assertSame([5], array_column($dewiOpdVisible->data['data'], 'id'), 'Peer Dewi STILL sees OPD Msg 5 after Andi deleted it');

    // Test 6 & 8: Conversation clear isolation (clearing Budi<->Siti does NOT affect Budi<->Andi)
    $pdo->exec("INSERT INTO chat_messages (id, sender_id, receiver_id, opd_id, chat_type, message, is_read, read_at, created_at) VALUES
        (6, 2, 3, NULL, 'personal', 'Unread baru ke Siti', 0, NULL, '2026-11-12 09:00:00')
    ");
    $clearRes = $controller->clearConversation(new \Illuminate\Http\Request([
        'target_id' => 3, 'chat_type' => 'personal', 'clear_type' => 'for_everyone'
    ], $userBudi));
    assertSame(200, $clearRes->getStatusCode(), 'Clear conversation succeeds');
    assertSame(1, $clearRes->data['deleted_for_everyone_count'], 'Unread Msg 6 cleared for everyone');
    // Msg 3 (Budi<->Andi) must remain completely untouched
    $budiAndiView = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'personal', 'target_id' => 5], $userBudi));
    assertSame([3], array_column($budiAndiView->data['data'], 'id'), 'Other conversation Budi<->Andi unaffected by clearing Budi<->Siti');

    // Regular employee blocked from hard_delete on delete-message AND from permanent-delete
    $regHard = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 3, 'delete_type' => 'hard_delete'], $userBudi));
    assertSame(403, $regHard->getStatusCode(), 'Regular employee blocked from hard_delete on chat endpoint');
    $regPerm = $controller->monitoringPermanentDelete(new \Illuminate\Http\Request(['message_ids' => [3]], $userBudi));
    assertSame(403, $regPerm->getStatusCode(), 'Regular employee blocked from monitoring permanent delete');
    $adminChatEndpointHard = $controller->deleteMessage(new \Illuminate\Http\Request(['message_id' => 3, 'delete_type' => 'permanent'], $userAdmin));
    assertSame(403, $adminChatEndpointHard->getStatusCode(), 'Regular chat endpoint rejects permanent delete even for Admin NIP');

    // Test 7: Admin Monitoring Filters (OPD, NIP, status, search, pagination)
    $monOpd20 = $controller->monitoringMessages(new \Illuminate\Http\Request(['opd_id' => '20'], $userAdmin));
    assertTrue(count($monOpd20->data['data']) >= 2, 'Monitoring filter opd_id=20 finds Siti messages');
    $monDeletedEveryone = $controller->monitoringMessages(new \Illuminate\Http\Request(['status' => 'deleted_for_everyone'], $userAdmin));
    assertSame(3, count($monDeletedEveryone->data['data']), 'Status filter deleted_for_everyone returns exact 3 messages (ids 2,4,6)');
    $monPaged = $controller->monitoringMessages(new \Illuminate\Http\Request(['page' => 1, 'per_page' => 2], $userAdmin));
    assertTrue($monPaged->data['status'] === true, 'monitoringMessages includes status=true parity key');
    assertSame(2, count($monPaged->data['data']), 'Pagination per_page=2 returns 2 rows');
    assertSame(6, $monPaged->data['pagination']['total'], 'Pagination reports total=6');

    $monConv = $controller->monitoringConversations(new \Illuminate\Http\Request(['page' => 1, 'per_page' => 10], $userAdmin));
    assertSame(200, $monConv->getStatusCode(), 'monitoringConversations succeeds for Admin NIP');
    assertTrue($monConv->data['status'] === true && isset($monConv->data['threads']) && isset($monConv->data['opd_list']), 'monitoringConversations returns status, threads, and opd_list');

    // Test 10 & 11: Permanent delete cleans references & shared vs unshared files while preserving audit logs
    $sharedFile = sys_get_temp_dir() . '/sahut_shared_' . uniqid() . '.jpg';
    $uniqueFile = sys_get_temp_dir() . '/sahut_unique_' . uniqid() . '.jpg';
    file_put_contents($sharedFile, 'shared');
    file_put_contents($uniqueFile, 'unique');

    // Msg 7 has uniqueFile and reply referencing Msg 2; Msg 8 forwards same file_url as Msg 9
    $pdo->exec("INSERT INTO chat_messages (id, sender_id, receiver_id, chat_type, message, file_url, reply_to_id, created_at) VALUES
        (7, 3, 2, 'personal', 'Reply ke pesan 2', '" . $uniqueFile . "', 2, '2026-11-12 09:30:00'),
        (8, 2, 3, 'personal', 'Lampiran bersama 1', '" . $sharedFile . "', NULL, '2026-11-12 09:31:00'),
        (9, 2, 5, 'personal', 'Lampiran forward', '" . $sharedFile . "', NULL, '2026-11-12 09:32:00')
    ");
    // Ensure disk unlink checks public_path / storage_path fallback cleanly (define public_path pointing to '/')
    if (!function_exists('public_path')) {
        function public_path(string $p = ''): string { return '/' . ltrim($p, '/'); }
    }

    $permRes = $controller->monitoringPermanentDelete(new \Illuminate\Http\Request([
        'message_ids' => [2, 8]
    ], $userAdmin));
    assertSame(200, $permRes->getStatusCode(), 'Admin batch permanent delete succeeds');
    assertSame(2, $permRes->data['deleted_count'], 'Deleted 2 records permanently');

    // Verify physical deletion of 2 & 8, survival of 7 & 9, reply_to_id detached safely on 7, and shared file preserved
    assertSame(0, (int) $pdo->query('SELECT COUNT(*) FROM chat_messages WHERE id IN (2,8)')->fetchColumn(), 'Msg 2 & 8 physically deleted from DB');
    $msg7After = $pdo->query('SELECT * FROM chat_messages WHERE id = 7')->fetch(PDO::FETCH_ASSOC);
    assertSame(null, $msg7After['reply_to_id'], 'Child reply_to_id safely detached on permanent parent delete');
    assertSame('[Pesan rujukan telah dihapus permanen]', $msg7After['reply_to_message'], 'Child reply placeholder updated safely');
    assertTrue(is_file($sharedFile), 'Shared attachment file preserved while Msg 9 still references it');

    // Audit log must STILL hold permanent delete records for deleted msg 2 & 8
    $auditRes = $controller->monitoringAuditLogs(new \Illuminate\Http\Request(['message_id' => 2], $userAdmin));
    $auditRows = array_map(fn ($r) => (array) $r, $auditRes->data['data']);
    $actions = array_column($auditRows, 'action');
    assertTrue(in_array('permanent_delete', $actions, true), 'Audit log retains permanent_delete record after SQL DELETE');
    assertTrue(!empty($auditRows) && is_bool($auditRows[0]['was_read']) && is_bool($auditRows[0]['is_permanent']), 'Audit log returns boolean was_read/is_permanent for Gson ChatAuditLogItem');

    // Permanently delete Msg 7 (which holds unshared uniqueFile) -> file on disk must be removed
    $permUniqueRes = $controller->monitoringPermanentDelete(new \Illuminate\Http\Request(['message_id' => 7], $userAdmin));
    assertSame(200, $permUniqueRes->getStatusCode(), 'Permanent delete of unshared attachment succeeds');
    assertTrue(!is_file($uniqueFile), 'Unshared attachment file removed from disk on permanent delete');

    // Restore soft-deleted Msg 1 by Admin works, blocked for regular user
    $restoreReg = $controller->restoreMessage(new \Illuminate\Http\Request(['message_id' => 1], $userBudi));
    assertSame(403, $restoreReg->getStatusCode(), 'Regular employee cannot restore soft-deleted message');
    $restoreAdmin = $controller->restoreMessage(new \Illuminate\Http\Request(['message_id' => 1], $userAdmin));
    assertSame(200, $restoreAdmin->getStatusCode(), 'Admin can restore soft-deleted message');
    $restoreData = $restoreAdmin->data['data'];
    assertTrue(
        is_array($restoreData)
        && is_int($restoreData['deleted_for_sender'])
        && $restoreData['deleted_for_sender'] === 0
        && is_int($restoreData['deleted_for_everyone'])
        && is_int($restoreData['is_deleted']),
        'restoreMessage returns formatted int flags for Gson parity'
    );
    $row1Restored = $pdo->query('SELECT * FROM chat_messages WHERE id = 1')->fetch(PDO::FETCH_ASSOC);
    assertSame(0, (int) $row1Restored['deleted_for_sender'], 'Restored message resets deleted_for_sender=0');

    // Encrypted AES-256-CBC message decryption & Android JSON contract parity (messages key + boolean is_me/is_read)
    $cipher = \App\Models\ChatMessage::encryptText('Pesan Rahasia Terenkripsi');
    $stmtEnc = $pdo->prepare("INSERT INTO chat_messages (id, sender_id, receiver_id, chat_type, message, is_read, created_at) VALUES (10, 2, 5, 'personal', ?, 0, '2026-11-12 09:40:00')");
    $stmtEnc->execute([$cipher]);

    $budiAndiAfterEnc = $controller->getMessages(new \Illuminate\Http\Request(['chat_type' => 'personal', 'target_id' => 5], $userBudi));
    assertTrue(isset($budiAndiAfterEnc->data['messages']) && is_array($budiAndiAfterEnc->data['messages']), 'getMessages includes messages key for Android ChatMessageResponse');
    $msg10Row = array_values(array_filter($budiAndiAfterEnc->data['messages'], fn ($m) => (int) $m['id'] === 10))[0] ?? null;
    assertTrue($msg10Row !== null && $msg10Row['message'] === 'Pesan Rahasia Terenkripsi', 'getMessages decrypts AES ENC:: message');
    assertTrue(is_bool($msg10Row['is_me']) && is_bool($msg10Row['is_read']), 'getMessages exposes boolean is_me and is_read for Gson');

    $monSearchEnc = $controller->monitoringMessages(new \Illuminate\Http\Request(['search' => 'Rahasia Terenkripsi'], $userAdmin));
    assertSame(1, count($monSearchEnc->data['data']), 'Monitoring search matches decrypted text inside encrypted messages');

    // Test deleteForEveryone endpoint directly on encrypted unread Msg 10 + verify decrypted message & Gson int/bool types
    $directFe = $controller->deleteForEveryone(new \Illuminate\Http\Request(['message_id' => 10], $userBudi));
    assertSame(200, $directFe->getStatusCode(), 'deleteForEveryone endpoint succeeds on unread encrypted message');
    $directFeData = $directFe->data['data'];
    assertSame('Pesan Rahasia Terenkripsi', $directFeData['message'], 'deleteForEveryone decrypts AES ENC:: message in response payload');
    assertSame(1, $directFeData['deleted_for_everyone'], 'deleteForEveryone returns int 1 deleted_for_everyone');
    assertTrue(
        is_int($directFeData['deleted_for_sender'])
        && is_int($directFeData['deleted_for_receiver'])
        && is_int($directFeData['is_deleted'])
        && is_int($directFeData['is_deleted_global']),
        'deleteForEveryone response flags are strict PHP ints for Gson'
    );
    assertTrue($directFeData['is_me'] === true && $directFeData['is_read'] === false, 'deleteForEveryone returns bool is_me/is_read for Gson');

    $globalStatusRes = $controller->getGlobalStatus(new \Illuminate\Http\Request([], $userAdmin));
    assertTrue($globalStatusRes->data['status'] === true && is_bool($globalStatusRes->data['chat_enabled']) && $globalStatusRes->data['is_master_user'] === true, 'getGlobalStatus returns Android ChatGlobalStatusResponse contract');

    $termsBefore = $controller->getTermsStatus(new \Illuminate\Http\Request([], $userBudi));
    assertSame(false, $termsBefore->data['status'], 'Terms status starts false before acceptance');
    $acceptRes = $controller->acceptTerms(new \Illuminate\Http\Request(['accepted' => 1], $userBudi));
    assertSame(true, $acceptRes->data['status'], 'Accept terms returns status=true');
    $termsAfter = $controller->getTermsStatus(new \Illuminate\Http\Request([], $userBudi));
    assertSame(true, $termsAfter->data['status'], 'Terms status returns status=true after acceptance');

    @unlink($sharedFile);

    echo "ALL {$passCount} PHP BACKEND ASSERTIONS PASSED.\n";
}
