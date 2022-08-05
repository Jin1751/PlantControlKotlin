package com.example.plantcontrolkotlin

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.res.ColorStateList
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class DeviceControl : AppCompatActivity(), View.OnClickListener {
    var plantId: Int = 0
    var temp: Int = 25
    private var humid: Int = 60
    var bright: Int = 5
    var cal : Calendar = Calendar.getInstance()
    var today : LocalDate = LocalDate.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DATE))
    var date : LocalDate = LocalDate.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DATE))
    var dateTxt: String = date.toString()
    var nDays: Long = date.until(today,ChronoUnit.DAYS)
    var sTime: String = ""
    var sH : Int = 0
    var sM : Int = 0
    var eTime: String = ""
    var eH : Int = 23
    var eM : Int = 59
    var isSetted: Boolean = true
    lateinit var startDate : Button
    lateinit var days: TextView
    lateinit var ledColor : Button
    lateinit var dialog: AlertDialog
    lateinit var lightStart: TextView
    private lateinit var lightEnd: Button
    lateinit var tempNowTxt: TextView
    lateinit var humidTxt: TextView
    lateinit var btnSave: Button
    lateinit var btnDelete: Button
    var color: Int = 5
    private val colorId = arrayOf(R.color.purple, R.color.green, R.color.blue, R.color.blue_green, R.color.red, R.color.yellow)
    lateinit var dbHelper : DBHelper
    private lateinit var database : SQLiteDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_control)

        val plantImg = findViewById<ImageButton>(R.id.PlantImg)
        startDate = findViewById<Button>(R.id.StartDate)
        days = findViewById<TextView>(R.id.DayPassed)
        ledColor = findViewById<Button>(R.id.LedColor)
        val ledBright = findViewById<SeekBar>(R.id.LedBright)
        val brightTxt = findViewById<TextView>(R.id.BrightTxt)
        lightStart = findViewById<TextView>(R.id.LightStart)
        lightEnd = findViewById<Button>(R.id.LightEnd)
        tempNowTxt = findViewById<TextView>(R.id.TempDayNum)
        humidTxt = findViewById<TextView>(R.id.HumidNum)
        btnSave = findViewById(R.id.BtnSave)
        btnDelete = findViewById(R.id.BtnDelete)
        val gotIntent = intent
        dbHelper = DBHelper(this, "PlantDevices.db", null, 1)
        database = dbHelper.readableDatabase
        plantId = gotIntent.getIntExtra("Device_id",0)
        val cursor : Cursor = database.rawQuery("SELECT * FROM deviceTable WHERE Device_id = ${plantId}", null)
        isSetted = (cursor.getCount() != 0)

        if(isSetted){
            cursor.moveToFirst()
            temp = cursor.getInt(7)
            dateTxt = cursor.getString(2)
            date = LocalDate.parse(dateTxt, DateTimeFormatter.ISO_DATE)
            nDays = date.until(today,ChronoUnit.DAYS)
            humid = cursor.getInt(8)
            bright = cursor.getInt(4)
            color = cursor.getInt(3)
            sTime = cursor.getString(5).toString()
            var t = sTime.split(" : ")
            sH = t[0].toInt()
            sM = t[1].toInt()
            eTime = cursor.getString(6).toString()
            t = eTime.split(" : ")
            eH = t[0].toInt()
            eM = t[1].toInt()
            cursor.close()
            database.close()
        }
        else{
            sTime = timeDigit(cal.get(Calendar.HOUR_OF_DAY), 'H') + " : " + timeDigit(cal.get(Calendar.MINUTE), 'M')
            eTime = timeDigit(24, 'H') + " : " + timeDigit(59, 'M')
        }
        ledBright.progress = bright
        ledBright.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            var txt : String = "5"
            var p : Int = 0
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                p = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                txt = "밝기:$p"
                brightTxt.text = txt
            }

        })
        tempNowTxt.text = "${temp}°C"
        humidTxt.text = "${humid}%"

        lightStart.text = sTime
        lightEnd.text = eTime
        plantImg.setOnClickListener(this)
        startDate.text = date.toString()
        days.text = "DAY: ${nDays}"
        startDate.setOnClickListener(this)
        ledColor.setOnClickListener(this)
        lightStart.setOnClickListener(this)
        lightEnd.setOnClickListener(this)
        btnSave.setOnClickListener(this)
        btnDelete.setOnClickListener(this)
        ledColor.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,colorId[color]))
        ledBright.setPadding(21,0,21,0)
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.PlantImg-> {

            }
            R.id.StartDate-> {
                val setDate = DatePickerDialog(this, { v, y, m, d ->
                    date = LocalDate.of(y,m + 1,d)
                    dateTxt = date.toString()
                    startDate.text = dateTxt
                    nDays = date.until(today,ChronoUnit.DAYS)
                    days.text = "DAY: " + nDays
                },date.year,date.monthValue - 1,date.dayOfMonth)
                setDate.datePicker.maxDate = LocalDateTime.of(today.year,today.monthValue,today.dayOfMonth, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
                setDate.show()
            }
            R.id.LedColor-> {
                val colors = arrayOf("Puple", "Green", "Blue", "Blue-Green", "Red", "Yellow")
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Select Color")
                builder.setSingleChoiceItems(colors, color) { _, i ->
                    color = i
                    dialog.dismiss()
                    ledColor.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this,colorId[color]))
                }
                dialog = builder.create()
                dialog.show()
            }
            R.id.LightStart-> {
                TimePickerDialog(this, { v, hourOfDay, minute ->
                    if(hourOfDay <= eH){
                        sH = hourOfDay
                        if((hourOfDay == eH && minute < eM) || minute <= eM){
                            sM = minute
                            sTime = timeDigit(sH, 'H') + " : " + timeDigit(sM, 'M')
                            lightStart.text = sTime
                        }
                        else{
                            Toast.makeText(this,"Start Time must be earlier than End Time", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(this,"Start Time must be earlier than End Time", Toast.LENGTH_LONG).show()
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }
            R.id.LightEnd-> {
                TimePickerDialog(this, { v, hourOfDay, minute ->
                    if(sH <= hourOfDay){
                        eH = hourOfDay
                        if((hourOfDay == sH && minute > sM) || sM <= minute){
                            eM = minute
                            eTime = timeDigit(eH, 'H') + " : " + timeDigit(eM, 'M')
                            lightEnd.text = eTime
                        }
                        else{
                            Toast.makeText(this,"End Time must be later than Start Time", Toast.LENGTH_LONG).show()
                        }
                    }
                    else{
                        Toast.makeText(this,"End Time must be later than Start Time", Toast.LENGTH_LONG).show()
                    }
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            }
            R.id.BtnSave-> {
                database = dbHelper.writableDatabase
                var values = ContentValues()
                values.put("Device_id", plantId)
                values.put("Plant_name", "PLANT ${plantId}")
                values.put("Start_date", dateTxt)
                values.put("LED_color", color)
                values.put("LED_bright", bright)
                values.put("LED_Start_time", sTime)
                values.put("LED_End_time", eTime)
                values.put("Temp_Now", temp)
                values.put("Humid", humid)
                if(!isSetted){
                    database.insert("deviceTable", null, values)
                    Toast.makeText(this,"INSERT", Toast.LENGTH_LONG).show()
                }
                else{
                    database.update("deviceTable", values, "Device_id = ${plantId}", null)
                    Toast.makeText(this,"UPDATE", Toast.LENGTH_LONG).show()
                }
                database.close()
                this.finish()
            }
            R.id.BtnDelete->{
                database = dbHelper.readableDatabase
                database.delete("deviceTable","Device_id = ${plantId}", null)
                database.close()
                this.finish()
            }
        }
    }
    private fun timeDigit(t : Int, type : Char): String{
        var digitT : String = "" + t
        if(t < 10){
            digitT = "0${t}"
        }
        else if(t > 24 && type == 'H'){
            digitT = "00"
        }
        return digitT
    }
}
