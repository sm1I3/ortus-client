package com.sm1l3.ortus_client.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.sm1l3.ortus_client.database.entity.User

private const val DATABASE_NAME = "myDB"
private const val USER_TABLE = "USER"
private const val LOGIN_COLUMN = "LOGIN"
private const val PASSWORD_COLUMN = "PASSWORD"
private const val DATABASE_VERSION = 2

class Database(private val dbHelper: DBHelper) {

    fun getCurrentUser(): User? {
        val database = dbHelper.writableDatabase

        val cursor = database.query(USER_TABLE, null, null, null, null, null, null)

        val loginColIndex = cursor.getColumnIndex(LOGIN_COLUMN)
        val passwordColIndex = cursor.getColumnIndex(PASSWORD_COLUMN)

        if (cursor.moveToFirst()) {
            val login = cursor.getString(loginColIndex)
            val password = cursor.getString(passwordColIndex)

            cursor.close()
            return User(login, password)
        }

        cursor.close()
        return null
    }

    fun updateCurrentUser(newLogin: String, newPassword: String) {
        deleteCurrentUser()

        val contentValues = ContentValues().apply {
            put(LOGIN_COLUMN, newLogin)
            put(PASSWORD_COLUMN, newPassword)
        }

        val database = dbHelper.writableDatabase

        database.insert(USER_TABLE, null, contentValues)
    }

    private fun deleteCurrentUser() {
        val database = dbHelper.writableDatabase
        database.delete(USER_TABLE, null, null)
    }
}

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(database: SQLiteDatabase) {
        database.execSQL("create table $USER_TABLE ($LOGIN_COLUMN text, $PASSWORD_COLUMN text);")
    }

    override fun onUpgrade(database: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        database.execSQL("drop table if exists $USER_TABLE;")

        onCreate(database)
    }
}