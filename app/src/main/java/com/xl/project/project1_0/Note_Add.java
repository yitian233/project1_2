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
import android.widget.EditText;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2018/1/4.
 */

public class Note_Add extends AppCompatActivity {
    private MyApp myapp;
    private EditText note_title;
    private EditText note_content;
    private Button back_button,save_btn;
    String note_title_string,note_content_string,s;
    private ResultSet rs;
    private String notebook_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.note1);
        Bundle bundle=getIntent().getExtras();
        notebook_id=bundle.getString("notebook_id");
        bindview();
    }

    public void bindview(){
        note_content=(EditText)findViewById(R.id.note1_content);
        note_title=(EditText)findViewById(R.id.note1_title);
        back_button=(Button)findViewById(R.id.note1_back);
        save_btn=(Button)findViewById(R.id.note1_save);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Note_Add.this.finish();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                note_title_string=note_title.getEditableText().toString();
                note_content_string=note_title.getEditableText().toString();
                add_to_database();
                Intent intent=new Intent(Note_Add.this,Note_book.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                setResult(123,intent);
                Note_Add.this.finish();
            }
        });
    }
    public void add_to_database(){
        final Handler handler=new android.os.Handler(){
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
                        Thread.sleep(300);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                        Date date=new Date();
                        String sql="insert into note(note_book_id,note_title,note_content,note_time) values("+notebook_id+",'"
                                +note_title_string+"','"+note_content_string+"','"+df.format(date)+"');";
                        Log.i("tag","sql:"+sql);
                        database.execSQL(sql);

                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"note添加失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
