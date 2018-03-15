package com.xl.project.project1_0;

import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.ContentValues.TAG;

public class List_Add extends AppCompatActivity {
    private MyApp myapp;
    private String notebook_id;
    private Button list_save,add_list_item;
    private long time;
    private String list_date_string,list_day_string;
    private Date date;
    private SimpleDateFormat format;
    private TextView first_day,last_day,week_number;
    private String first_day_string,last_day_string,year_string;
    private int week_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_list_detail);
        Bundle bundle=getIntent().getExtras();
        notebook_id=bundle.getString("notebook_id");
        get_System_time();
        bindview();
    }

    public void get_System_time(){
        time=System.currentTimeMillis();
        date=new Date(time);
        format=new SimpleDateFormat("yyyy-MM-dd");
        list_date_string=format.format(date);
        format=new SimpleDateFormat("EEEE");
        list_day_string=format.format(date);
    }

    public void bindview(){
        format=new SimpleDateFormat("yyyy-MM-dd");

        first_day_string=format.format(getFirstDayOfWeek(date));
        last_day_string=format.format(getLastDayOfWeek(date));
        format=new SimpleDateFormat("yyyy");
        year_string=format.format(date).toString();
        week_num=getWeekNumber();

        first_day=(TextView)findViewById(R.id.first_day);
        last_day=(TextView)findViewById(R.id.last_day);
        week_number=(TextView)findViewById(R.id.week_number);
        list_save=(Button)findViewById(R.id.list_save_btn);


        first_day.setText(first_day_string);
        last_day.setText(last_day_string);
        week_number.setText("第 "+week_num+" 周");

        list_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                add_to_database();

                Intent intent=new Intent(List_Add.this,List_book.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                setResult(123,intent);
                finish();
            }
        });


    }
    public int getWeekNumber(){
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        java.util.Date date=new Date(System.currentTimeMillis());
        calendar.setTime(date);
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }
    public static java.util.Date getFirstDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
        return c.getTime ();
    }
    public static java.util.Date getLastDayOfWeek(Date date) {
        Calendar c = new GregorianCalendar();
        c.setFirstDayOfWeek(Calendar.MONDAY);
        c.setTime(date);
        int temp=c.getFirstDayOfWeek();
        int k=temp+6;
        c.set(Calendar.DAY_OF_WEEK, k); // Sunday
        return c.getTime();
    }

    public void add_to_database(){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
            }
        };

        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try{
                        Thread.sleep(200);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }

                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="insert into list(note_book_id,list_first_day,list_last_day,week_of_year,year) values("+notebook_id+",'"+first_day_string+"','"
                                +last_day_string+"','"+week_num+"','"+year_string+"')";
                        database.execSQL(sql);
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"list添加数据库失败！");
                    }
                }
            }
        });
        thread.start();
    }
}

