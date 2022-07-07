package io.theriverelder.novafactory.builtin.database

import java.sql.DriverManager
import java.sql.Statement

object DataSQLPersistence {

    lateinit var stmtPutFactoryRecord: Statement
    lateinit var stmtGetFactoryRecord: Statement
    lateinit var stmtGetFactoryRecords: Statement

    fun initializeDatabase() {
        val url = "jdbc:sqlite:nova-factory.db"
        val conn = DriverManager.getConnection(url)

        val hasTableFactoryHistory = conn.prepareStatement("SELECT 1 FROM factory_history")
        if (hasTableFactoryHistory.executeQuery().wasNull()) {
            val createTable = conn.prepareStatement("CREATE TABLE factory_history(PRIMARY KEY INT row_id, INT time, INT last_electricity)")
            createTable.execute()
        }

        stmtPutFactoryRecord = conn.prepareStatement("INSERT INTO factory_history VALUES(?, ?)")
        stmtGetFactoryRecord = conn.prepareStatement("SELECT FROM factory_history WHERE time = ?")
        stmtGetFactoryRecords = conn.prepareStatement("SELECT FROM factory_history WHERE time >= ? AND time < ?")
    }
}