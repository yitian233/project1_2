package com.xl.project.project1_0;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2018/1/6.
 */

public class Sound_detail extends AppCompatActivity {
    private Button begin_record,finish_record,begin_play,stop_play,back,upload;
    private EditText sound_name;
    private MyApp myapp;
    private String sound_id;
    private TextView sound_time,sound_duration;
    private ResultSet rs;
    private URL url;
    private String sound_url;
    private MediaPlayer mMediaPlayer;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.sound);
        Bundle bundle=getIntent().getExtras();
        sound_id=bundle.getString("sound_id");
        bindview();
        download();
    }

    public void bindview(){
        sound_duration=(TextView)findViewById(R.id.sound_durantion);
        begin_record=(Button)findViewById(R.id.record_begin);
        begin_record.setVisibility(View.INVISIBLE);
        //finish_record=(Button)findViewById(R.id.record_finish);
        begin_play=(Button)findViewById(R.id.sound_play);
        stop_play=(Button)findViewById(R.id.sound_stop);
        back=(Button)findViewById(R.id.sound_back);
        upload=(Button)findViewById(R.id.sound_upload);

        sound_name=(EditText) findViewById(R.id.sound_name);
        sound_time=(TextView)findViewById(R.id.sound_time);
        begin_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mMediaPlayer=new MediaPlayer();
                    mMediaPlayer.setDataSource(sound_url);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"play failed ");
                }
            }
        });
        stop_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.stop();
                        mMediaPlayer.release();
                        mMediaPlayer=null;
                    }
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Sound_detail.this.finish();
            }
        });
    }

    public void download(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            while(cursor.moveToNext()){
                                sound_name.setText(cursor.getString(cursor.getColumnIndex("sound_name")));
                                sound_time.setText(cursor.getString(cursor.getColumnIndex("sound_time")));
                                sound_url=cursor.getString(cursor.getColumnIndex("sound_file_url"));
                                sound_duration.setText(cursor.getString(cursor.getColumnIndex("sound_size")));
                                url=new URL(sound_url);
                            }
                        } catch (CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
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
                        Thread.sleep(300);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        String sql="select * from sound where sound_id='"+sound_id+"';";
                        SQLiteDatabase database=myapp.mDBOpenHelper.getReadableDatabase();
                        cursor=database.rawQuery(sql,new String[]{});
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"加载sound内容失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
