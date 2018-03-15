package com.xl.project.project1_0;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;
/**
 * Created by xl on 2018/1/1.
 */

public class Diary_detail extends AppCompatActivity {
    private MyApp myapp;
    private String diary_id;
    private TextView diary_date,diary_day;
    private EditText diary_content;
    private ResultSet rs;
    private Model.Diary temp;
    private ImageView diary_weather,diary_mood;
    private String diary_weather_string,diary_mood_string;
    private ImageView picture1,picture2;
    private String picture1_url,picture2_url;
    private Bitmap bitmap1,bitmap2;
    private Button back;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.diary);
        Bundle bundle=getIntent().getExtras();
        diary_id=bundle.getString("id");
        bindview();
        fillview();
        download_picture();
    }

    public void bindview(){
        diary_date=(TextView)findViewById(R.id.diary_date);
        diary_day=(TextView)findViewById(R.id.diary_day);
        diary_content=(EditText)findViewById(R.id.diary_content);
        diary_content.setFocusable(false);
        diary_mood=(ImageView)findViewById(R.id.diary_mood);
        diary_weather=(ImageView)findViewById(R.id.diary_weather);
        picture1=(ImageView)findViewById(R.id.diary_picture1);
        picture2=(ImageView)findViewById(R.id.diary_picture2);
        back=(Button)findViewById(R.id.diary_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void  fillview(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            while(cursor.moveToNext()){
                                diary_date.setText(cursor.getString(cursor.getColumnIndex("diary_date")));
                                diary_day.setText(cursor.getString(cursor.getColumnIndex("diary_day")));
                                diary_content.setText(cursor.getString(cursor.getColumnIndex("diary_content")));

                                diary_weather_string=cursor.getString(cursor.getColumnIndex("diary_weather"));
                                switch (diary_weather_string){
                                    case "sunny": diary_weather.setImageResource(R.mipmap.sunny);
                                    case "cloudy": diary_weather.setImageResource(R.mipmap.cloudy);
                                    case "drizzle": diary_weather.setImageResource(R.mipmap.drizzle);
                                }

                                diary_mood_string=cursor.getString(cursor.getColumnIndex("diary_mood"));
                                switch (diary_mood_string){
                                    case "excite": diary_mood.setImageResource(R.mipmap.excite);
                                    case "mad": diary_mood.setImageResource(R.mipmap.mad);
                                    case "sleeping": diary_mood.setImageResource(R.mipmap.sleeping);
                                }

                                picture1_url=cursor.getString(cursor.getColumnIndex("diary_picture1"));
                                picture2_url=cursor.getString(cursor.getColumnIndex("diary_picture2"));
                                Log.i(TAG,"picture1_url test in handle1 "+picture1_url);
                            }
//                            picture1.setImageBitmap(bitmap1);
//                            picture2.setImageBitmap(bitmap2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
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
                        String sql="select * from diary where diary_id="+diary_id;
                        SQLiteDatabase database=myapp.mDBOpenHelper.getReadableDatabase();
                        cursor=database.rawQuery(sql,new String[]{});
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"SQLite查询diary详情失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void download_picture(){
        Handler handler2=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 1:
                        Log.i(TAG,"hello : "+"hello");
                        picture1.setImageBitmap(bitmap1);
                        picture2.setImageBitmap(bitmap2);
                }
            }

        };

        Thread thread2=new Thread(){
            @Override
            public void run(){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try{
                    Log.i(TAG,"picture1_url test in handle2 : "+picture1_url);

                    URL url=new URL(picture1_url);
                    InputStream is= url.openStream();
                    bitmap1 = BitmapFactory.decodeStream(is);

                    url=new URL(picture2_url);
                    is= url.openStream();
                    bitmap2 = BitmapFactory.decodeStream(is);
                    handler2.sendEmptyMessage(1);
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };
        thread2.start();
    }
}
