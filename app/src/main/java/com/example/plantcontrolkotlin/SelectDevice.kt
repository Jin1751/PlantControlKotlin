package com.example.plantcontrolkotlin

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import org.intellij.lang.annotations.JdkConstants

class SelectDevice : AppCompatActivity() {
    var deviceNum : Int = 0
    lateinit var tLayout: TableLayout
    lateinit var hLayout: LinearLayout
    var rowId : Int = 0
    lateinit var dbHelper : DBHelper
    lateinit var database : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_device)

        var addDevice : androidx.appcompat.widget.Toolbar = findViewById(R.id.AddDevice)
        setSupportActionBar(addDevice)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        addDevice.title = "Device Select"

        tLayout = findViewById(R.id.TableLayout)

        dbHelper = DBHelper(this, "PlantDevices.db", null, 1)
        database = dbHelper.writableDatabase
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        var newBtn = Button(this)
        newBtn.id = deviceNum
        newBtn.text = "Device ${deviceNum}"

        newBtn.setOnClickListener(object : View.OnClickListener{
            @RequiresApi(Build.VERSION_CODES.O)
            val intent = Intent(this@SelectDevice, MainActivity::class.java)
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onClick(v: View?) {
                startActivity(intent)
            }
        })
        if(deviceNum % 3 == 0){
            var newRow = TableRow(this)
            newRow.id = rowId++
            Log.v("LAYOUT ID", "" + newRow.id)
            hLayout = newRow
            Log.v("MADE", "BTN CREATE new Row")
            tLayout.addView(newRow,LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        hLayout.addView(newBtn, 480, 300)
        Log.v("BTN ID", "" + newBtn.id)
        deviceNum++
        return super.onOptionsItemSelected(item)
    }
}