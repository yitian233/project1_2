package com.xl.project.project1_0;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2017/12/31.
 */

public class Note_detail extends AppCompatActivity {
    private MyApp myapp;
    private String note_id;
    private EditText note_title;
    private EditText note_content;
    private ResultSet rs;
    private TextView note_time;
    private Button back_btn,save_btn;
    private AlertDialog.Builder alertDialog;
    private boolean hasGotToken = false;
    private Cursor cursor;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("tag","onActivityResult");
        // 识别成功回调，文字识别（含位置信息）
        Log.i("tag","recognize..");
        String filepath=data.getExtras().getString("file_path");
        Log.i("tag","file_path:"+filepath);
        RecognizeService.recAccurate(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                new RecognizeService.ServiceListener() {
                    @Override
                    public void onResult(GeneralResult result) {
                        //infoPopText(result);
                        StringBuilder sb=new StringBuilder();
                        for (WordSimple wordSimple : result.getWordList()) {
                            Word word = (Word) wordSimple;
                            sb.append(word.getWords());
                            Log.i("tag","word:"+word.getWords());
                            sb.append("\n");
                        }
                        String temp=note_content.getText().toString();
                        note_content.setText(temp+sb);
                        //这里处理result
                    }
                    @Override
                    public void onError(String error){
                        //  infoPopText(error);
                    }
                });
    }
    @Override
    protected void onDestroy(){
        Intent intent=new Intent(Note_detail.this,Note_book.class);
        setResult(123,intent);
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.note1);
        Bundle bundle=getIntent().getExtras();
        note_id=bundle.getString("id");
        Log.i("tag","note_id:"+note_id);
        bindview();
        fillview();
        alertDialog = new AlertDialog.Builder(this);
        if(alertDialog==null){
            Log.i(TAG,"alertDialog:NULL");
        }
        else{
            Log.i(TAG,"alertDialog:NOT NULL");
        }
        initAccessTokenWithAkSk();


        //文字识别
        findViewById(R.id.orc_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkTokenStatus()) {
                    Log.i("tag","no token");
                    return;
                }
                Intent intent = new Intent(Note_detail.this, mCameraActivity.class);
                intent.putExtra(mCameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(mCameraActivity.KEY_CONTENT_TYPE,
                        mCameraActivity.CONTENT_TYPE_GENERAL);
                Log.i("tag","start camara");
                startActivityForResult(intent, 999);
            }
        });

    }

    public void  bindview(){
        note_title=(EditText)findViewById(R.id.note1_title);
        note_content=(EditText)findViewById(R.id.note1_content);
        note_time=(TextView)findViewById(R.id.note1_time);
        back_btn=(Button)findViewById(R.id.note1_back);
        save_btn=(Button)findViewById(R.id.note1_save);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Note_detail.this,Note_book.class);
                setResult(123,intent);
                Note_detail.this.finish();
            }
        });
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save_content();
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
                            myapp.mNote_List.clear();
                            while(cursor.moveToNext()){
                                note_content.setText(cursor.getString(cursor.getColumnIndex("note_content")));
                                note_time.setText(cursor.getString(cursor.getColumnIndex("note_time")));
                                note_title.setText(cursor.getString(cursor.getColumnIndex("note_title")));
                            }
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
                        Log.i("tag","interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        String sql="select * from note where note_id="+note_id;
                        SQLiteDatabase database=myapp.mDBOpenHelper.getReadableDatabase();
                        cursor=database.rawQuery(sql,new String[]{});
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i("tag","加载note详情失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void  save_content(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 234:
                        Toast.makeText(Note_detail.this,"保存成功",Toast.LENGTH_SHORT).show();
                        break;
                    case 345:
                        Toast.makeText(Note_detail.this,"保存失败",Toast.LENGTH_SHORT).show();
                        break;
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
                        Log.i("tag","interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        SQLiteDatabase database = myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="update note set note_content='"+note_content.getText().toString()+"' where note_id="+note_id+";";
                        database.execSQL(sql);
                        handler.obtainMessage(234).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        handler.obtainMessage(345).sendToTarget();
                        e.printStackTrace();
                        Log.i("tag","远程连接失败！");
                    }

                }
            }
        });
        thread.start();
    }
    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    private void initAccessToken() {
        OCR.getInstance().initAccessToken(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken accessToken) {
                String token = accessToken.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                //alertText("licence方式获取token失败", error.getMessage());
            }
        }, getApplicationContext());
    }

    private void initAccessTokenWithAkSk() {
        OCR.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
                //alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "vP3BGlOEmR0nucRLW4sZpvE6", "RZTjGvedPWxyxRw4YHciBKoS8UT6mjfn");
    }


//    private void alertText(final String title, final String message) {
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                alertDialog.setTitle(title)
//                        .setMessage(message)
//                        .setPositiveButton("确定", null)
//                        .show();
//            }
//        });
//    }


//    private void infoPopText(final String result) {
//        alertText("", result);
//    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initAccessToken();
        } else {
            Toast.makeText(getApplicationContext(), "需要android.permission.READ_PHONE_STATE", Toast.LENGTH_LONG).show();
        }
    }

}
