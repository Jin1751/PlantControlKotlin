package com.example.plantcontrolkotlin

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi

class SelectDevice : AppCompatActivity() {
    var deviceNum : Int = 0
    var buttonId : Int = 0
    lateinit var tLayout: TableLayout
    lateinit var hLayout: LinearLayout
    var rowId : Int = 0
    lateinit var dbHelper : DBHelper
    lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        val addDevice : androidx.appcompat.widget.Toolbar = findViewById(R.id.AddDevice)
        setSupportActionBar(addDevice)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        addDevice.title = "Device Select"

        tLayout = findViewById(R.id.TableLayout)

        dbHelper = DBHelper(this, "PlantDevices.db", null, 1)

    }
    override fun onResume() {
        super.onResume()
        database = dbHelper.readableDatabase
        val cursor: Cursor = database.rawQuery("Select Device_id, Plant_name from deviceTable",null)
        if(cursor.moveToFirst()){
            do {
                val deviceID :Int = cursor.getInt(0)
                val plantName : String = cursor.getString(1)
                btnCreate(deviceID, plantName)

            }while(cursor.moveToNext())
        }
        cursor.moveToLast()
        Log.v("ID", cursor.getInt(0).toString())
        buttonId = cursor.getInt(0)
        cursor.close()
        database.close()
    }

    override fun onPause() {
        super.onPause()
        tLayout.removeAllViews()
        deviceNum = 0
        rowId = 0
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        buttonId++
        btnCreate(buttonId, "Plant")
        return super.onOptionsItemSelected(item)
    }

    private fun btnCreate(btnId : Int, name : String){
        val newBtn = Button(this)
        newBtn.id = btnId
        newBtn.text = name
        newBtn.setOnClickListener(object : View.OnClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            val intent = Intent(this@SelectDevice, DeviceControl::class.java)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View?) {
                intent.putExtra("Device_id", btnId)
                startActivity(intent)
            }
        })
        if(deviceNum % 3 == 0){
            val newRow = TableRow(this)
            newRow.id = rowId++
            hLayout = newRow
            tLayout.addView(newRow,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        hLayout.addView(newBtn, 480, 300)
        deviceNum++
    }
}