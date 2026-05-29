package ak.neocobblewatch.persistence

import ak.neocobblewatch.Neocobblewatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.DriverManager

/**
 * Single connection serialized by [writeLock] — SQLite WAL tolerates concurrent reads from
 * separate connections, but for v1 a serialized writer is simpler and the snapshot/HTTP load
 * fits well inside one connection.
 */
internal class Database {
    private var connection: Connection? = null
    private val writeLock = Mutex()

    val isOpen: Boolean get() = connection != null

    fun open(path: Path) {
        check(connection == null) { "Database already open" }
        path.parent?.let { Files.createDirectories(it) }
        Class.forName("org.sqlite.JDBC")
        val conn = DriverManager.getConnection("jdbc:sqlite:${path.toAbsolutePath()}")
        try {
            applyPragmas(conn)
            Migrations.applyAll(conn)
            connection = conn
            Neocobblewatch.LOGGER.info("Database opened at {}", path)
        } catch (e: Exception) {
            conn.close()
            throw e
        }
    }

    fun close() {
        val conn = connection ?: return
        conn.close()
        connection = null
        Neocobblewatch.LOGGER.info("Database closed")
    }

    suspend fun <T> withConnection(block: (Connection) -> T): T = withContext(Dispatchers.IO) {
        writeLock.withLock {
            val conn = connection ?: error("Database not open")
            block(conn)
        }
    }

    suspend fun <T> transaction(block: (Connection) -> T): T = withConnection { conn ->
        try {
            conn.autoCommit = false
            val result = block(conn)
            conn.commit()
            result
        } catch (e: Exception) {
            conn.rollback()
            throw e
        } finally {
            conn.autoCommit = true
        }
    }

    private fun applyPragmas(conn: Connection) {
        conn.createStatement().use { stmt ->
            stmt.execute("PRAGMA journal_mode = WAL")
            stmt.execute("PRAGMA foreign_keys = ON")
            stmt.execute("PRAGMA wal_autocheckpoint = 1000")
        }
    }
}
