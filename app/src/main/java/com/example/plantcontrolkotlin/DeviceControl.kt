package com.example.plantcontrolkotlin

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
class DeviceControl : AppCompatActivity(), View.OnClickListener {
    var plantId: Int = 0
    var temp: Array<Int> = arrayOf(25,18)
    private var humid: Int = 60
    var bright: Int = 5
    var cal : Calendar = Calendar.getInstance()
    var today : LocalDate = LocalDate.of(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1,cal.get(Calendar.DATE))
    var date : LocalDate = LocalDate.of(2022,6,30)
    var dateTxt: String = date.toString()
    var nDays: Long = date.until(today,ChronoUnit.DAYS)
    var sTime: String = ""
    var sH : Int = 0
    var sM : Int = 0
    var eTime: String = ""
    var eH : Int = 23
    var eM : Int = 59
    lateinit var startDate : Button
    lateinit var days: TextView
    lateinit var ledColor : Button
    lateinit var dialog: AlertDialog
    lateinit var lightStart: TextView
    private lateinit var lightEnd: Button
    lateinit var tempDayTxt: TextView
    lateinit var tempNightTxt: TextView
    lateinit var humidTxt: TextView
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
        tempDayTxt = findViewById<TextView>(R.id.TempDayNum)
        tempNightTxt = findViewById<TextView>(R.id.TempNightNum)
        humidTxt = findViewById<TextView>(R.id.HumidNum)
        val gotIntent = intent
        val id = gotIntent.getIntExtra("Device_id", -1)
        val isFirst = gotIntent.getBooleanExtra("isFirst", true)
        if(!isFirst){
            dbHelper = DBHelper(this, "PlantDevices.db", null, 1)
            database = dbHelper.readableDatabase
            val cursor : Cursor = database.rawQuery("SELECT * FROM DeviceTable WHERE Device_id = $id", null)
            temp[0] = cursor.getInt(cursor.getColumnIndexOrThrow("Temp_day"))
            temp[1] = cursor.getInt(cursor.getColumnIndexOrThrow("Temp_night"))
            val string_date = cursor.getString(cursor.getColumnIndexOrThrow("Start_date"))
            humid = cursor.getInt(cursor.getColumnIndexOrThrow("Humid"))
            bright = cursor.getInt(cursor.getColumnIndexOrThrow("LED_bright"))
            color = cursor.getInt(cursor.getColumnIndexOrThrow("LED_color"))
            val start_time = cursor.getString(cursor.getColumnIndexOrThrow("LED_Start_time"))
            val end_time = cursor.getString(cursor.getColumnIndexOrThrow("LED_End_time"))
            cursor.close()
            database.close()
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
        days.text = dateTxt
        tempDayTxt.text = "${temp[0]}°C"
        tempNightTxt.text = "${temp[1]}°C"
        humidTxt.text = "${humid}%"
        sTime = timeDigit(cal.get(Calendar.HOUR_OF_DAY), 'H') + " : " + timeDigit(cal.get(Calendar.MINUTE), 'M')
        lightStart.text = sTime
        lightEnd.text = sTime
        plantImg.setOnClickListener(this)
        startDate.text = date.toString()
        days.text = "DAY: ${nDays}"
        startDate.setOnClickListener(this)
        ledColor.setOnClickListener(this)
        lightStart.setOnClickListener(this)
        lightEnd.setOnClickListener(this)

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
                    startDate.text = date.toString()
                    nDays = date.until(today,ChronoUnit.DAYS)
                    days.text = "DAY: " + nDays
                },date.year,date.monthValue - 1,date.dayOfMonth)
                setDate.datePicker.maxDate = LocalDateTime.of(date.year,date.monthValue,date.dayOfMonth, 0, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
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
