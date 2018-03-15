package com.xl.project.project1_0;

import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
 * Created by xl on 2018/1/7.
 */

public class Story_picture_detail extends AppCompatActivity {
    private MyApp myapp;
    private String story_picture_id;
    private EditText story_picture_title;
    private TextView story_picture_time;
    private String title_string,time_string;
    private ResultSet rs;
    private String identify;
    private String[] item_string=new String[]{"","","","","","","","","",""};
    private LinearLayout linearLayout;
    private Handler handler2;
    private Bitmap bitmap;
    private Bitmap[] mBitmaps;
    private String[] item_content=new String[]{"","","","","","","","","",""};
    private ProgressBar progressBar;
    private Cursor cursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.story_picture);
        Bundle bundle=getIntent().getExtras();
        story_picture_id=bundle.getString("id");
        linearLayout=(LinearLayout)findViewById(R.id.story_picture_linearlayout);
        innit_bitmap();
        bindview();
        fillview();

//        download_picture();

        ImageView test=(ImageView)findViewById(R.id.add_picture);
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // createImage("http://xl-1255732607.cosgz.myqcloud.com/picture/1022-54-26.jpg");
            }
        });


    }

    public void innit_bitmap()
    {
        mBitmaps=new Bitmap[]{BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),
        BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),
        BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),
        BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),
                BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1),BitmapFactory.decodeResource(this.getResources(),R.mipmap.cover1)};
    }
    public void bindview(){
        progressBar=(ProgressBar)findViewById(R.id.login_progress_story);
        story_picture_title=(EditText)findViewById(R.id.story_picture_title);
        story_picture_title.setFocusable(false);
        story_picture_time=(TextView)findViewById(R.id.story_picture_time);
        Button back=(Button)findViewById(R.id.back_story_picture);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    public void createText(String content){
        EditText editText=new EditText(this);
        editText.setPadding(100,10,10,10);
        editText.setText(content);
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30,30,30,30);
        linearLayout.addView(editText,params);
    }


    private void createImageView(){
        ImageView imageView=new ImageView(this);
        imageView.setPadding(10,10,10,10);
        imageView.setImageResource(R.mipmap.cloudy);
        linearLayout.addView(imageView);
    }


    public void createImage(int index){
        ImageView imageView=new ImageView(this);
        imageView.setImageBitmap(mBitmaps[index]);
        linearLayout.addView(imageView);

    }

    public void download_image(String image_url){


        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    URL url=new URL(image_url);
                    InputStream is= url.openStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    Log.i(TAG,"thread test ");
                    handler2.sendEmptyMessage(1);
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }


    private void downLoad(){
        progressBar.setVisibility(View.VISIBLE);
        Handler handler3=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what) {
                    case 1234:
                        int length=identify.length();
                        for(int i=1;i<=length;i++){
                            if(identify.charAt(i-1)=='0'){
                                createText(item_string[i-1]);
                            }else if(identify.charAt(i-1)=='1'){
                                createImage(i-1);
                            }
                        }

                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        };


        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                int length=identify.length();
                for(int i=1;i<=length;i++){
                    if(identify.charAt(i-1)=='0'){
                        item_string[i-1]=item_content[i-1];
                        Log.i(TAG,"text item: "+i);
                    }
                    else if(identify.charAt(i-1)=='1'){
                        try {
                            URL url=new URL(item_content[i-1]);
                            InputStream is= url.openStream();
                            mBitmaps[i-1] = BitmapFactory.decodeStream(is);
                            Log.i(TAG,"thread test ");

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.i(TAG,"image item: "+i);
                    }
                }
                handler3.sendEmptyMessage(1234);
            }
        });
        thread.start();
    }


    public void fillview(){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            while(cursor.moveToNext()){
                                title_string=cursor.getString(cursor.getColumnIndex("story_picture_title"));
                                time_string=cursor.getString(cursor.getColumnIndex("time"));
                                identify=cursor.getString(cursor.getColumnIndex("identify"));
                                story_picture_title.setText(title_string);
                                for(int i=1;i<=identify.length();i++){
                                    item_content[i-1]=cursor.getString(cursor.getColumnIndex("item"+i));
                                    Log.i(TAG,"item content "+item_content[i-1]);
                                }
                                downLoad();
                                Log.i(TAG,"download ");
                            }
                        } catch (CursorIndexOutOfBoundsException e) {
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
                        Thread.sleep(1000);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    String ip="119.29.229.194";
                    int port=3306;
                    String dbName="android_xl";
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getReadableDatabase();
                        String sql="select * from story_picture where story_picture_id="+story_picture_id;
                        cursor=database.rawQuery(sql,new String[]{});
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"远程连接失败！");
                    }
                }
            }
        });
        thread.start();
    }
}
