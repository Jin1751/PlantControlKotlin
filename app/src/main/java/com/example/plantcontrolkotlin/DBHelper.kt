package com.example.plantcontrolkotlin

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(
    context: Context?,
    name: String?,
    factory: SQLiteDatabase.CursorFactory?,
    version: Int
) : SQLiteOpenHelper(context, name, factory, version) {


    override fun onCreate(db: SQLiteDatabase) {
        var sql : String = "CREATE TABLE if not exists deviceTable (" +
                "Device_id INTEGER primary key autoincrement," +
                "Start_date TEXT," +
                "LED_color INTEGER," +
                "LED_bright INTERGER," +
                "LED_Start_time TEXT," +
                "LED_End_time TEXT," +
                "asd" + ");"

        db.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        val sql : String = "DROP TABLE if exists deviceTable"

        db.execSQL(sql)
        onCreate(db)
    }

}