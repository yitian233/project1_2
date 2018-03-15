package com.xl.project.project1_0;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.qcloud.core.network.QCloudProgressListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2018/1/6.
 */

public class Sound_add  extends AppCompatActivity {
    private MyApp myapp;
    private String book_id;
    private Button begin_record,finish_record,begin_play,stop_play,back,upload;
    private EditText sound_name;
    private TextView sound_time,sound_durantion;
    private String sound_name_string,sound_time_string,sound_size_string;
    private long time;
    private Date date;
    private SimpleDateFormat format;
    private String FileName,urlimg;
    private MediaRecorder mRecorder;
    private MediaPlayer mMediaPlayer;
    private CosXmlServiceConfig serviceConfig;
    private CosXmlService cosXmlService;
    private PutObjectRequest putObjectRequest;
    private Handler handler2;
    private URL url;
    private boolean record_flag=true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.sound);
        Bundle bundle=getIntent().getExtras();
        book_id=bundle.getString("book_id");

        bindview();
        get_System_time();
    }

    private void bindview() {
        begin_record=(Button)findViewById(R.id.record_begin);
        //finish_record=(Button)findViewById(R.id.record_finish);
        begin_play=(Button)findViewById(R.id.sound_play);
        stop_play=(Button)findViewById(R.id.sound_stop);
        back=(Button)findViewById(R.id.sound_back);
        upload=(Button)findViewById(R.id.sound_upload);
        sound_time=(TextView)findViewById(R.id.sound_time);
        sound_durantion=(TextView)findViewById(R.id.sound_durantion);
        sound_name=(EditText)findViewById(R.id.sound_name);

        //设置sdcard的路径
        FileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        FileName += "/audiorecordtest.3gp";

        begin_record.setOnClickListener(new View.OnClickListener() {//开始录音
            @Override
            public void onClick(View v) {
                if(record_flag){
                    Toast.makeText(Sound_add.this,"开始录音",Toast.LENGTH_SHORT).show();
                    begin_record.setBackgroundResource(R.mipmap.mic_off);
                    Log.i(TAG,"file name: "+FileName);
                    mRecorder = new MediaRecorder();
                    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mRecorder.setOutputFile(soundFile.getAbsolutePath());
                    mRecorder.setOutputFile(FileName);
                    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                    try {
                        mRecorder.prepare();
                    } catch (IOException e) {
                        Log.i(TAG, "prepare() failed");
                    }
                    try{
                        mRecorder.start();
                        Log.i(TAG,"recorder start ");
                    }catch (Exception e){
                        Log.i(TAG,"failed at start ");
                    }
                }
                else{
                    Toast.makeText(Sound_add.this,"结束录音",Toast.LENGTH_SHORT).show();
                    begin_record.setBackgroundResource(R.mipmap.mic_on);
                    mRecorder.stop();
                    mRecorder.release();
                    mRecorder=null;
                }
                record_flag=!record_flag;
            }
        });


        begin_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    mMediaPlayer=new MediaPlayer();
                    mMediaPlayer.setDataSource(FileName);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    int time=mMediaPlayer.getDuration();
                    Log.i("tag","durantion:"+time);
                    java.util.Date Duration=new java.util.Date(time);
                    SimpleDateFormat format=new SimpleDateFormat("mm:ss");
                    sound_durantion.setText(format.format(time));
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG,"play failed ");
                }
            }
        });

        stop_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.release();
                mMediaPlayer=null;
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                innit();
                upload();
                add_to_database();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
    public void get_System_time(){
        time=System.currentTimeMillis();
        date=new Date(time);
        format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sound_time_string=format.format(date);
        sound_time.setText(sound_time_string);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void innit(){
        if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "READ permission IS NOT granted...");
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 321);
        }
        String appid = "1255732607";
        String region = "ap-guangzhou";
        String secretId="AKIDmSzOfXxpDPaM1aFAB0g5tcD0hEvUueyd";
        String secretKey="fTBHF5C9vlRvEMNDNSO8iCVeosmW4RgA";
        String bucket="xl-1255732607";

        String cosPath="/sound/1.jpg";//存储在服务器上的路径
        cosPath="/sound/"+book_id+sound_time_string+".3pg";
        //cosPath="/picture/"+notebook_id+diary_date+picture1_or_picture2+".jpg";
        long keyDuration = 600; //SecretKey 的有效时间，单位秒
        long signDuration = 600; //签名的有效期，单位为秒
        serviceConfig = new CosXmlServiceConfig.Builder()
                .setAppidAndRegion(appid, region)
                .setDebuggable(true)
                .setConnectionTimeout(45000)
                .setSocketTimeout(30000)
                .build();

        //创建 CosXmlServiceConfig 对象，根据需要修改默认的配置参数
        //创建获取签名类
        LocalCredentialProvider localCredentialProvider = new LocalCredentialProvider(secretId, secretKey, keyDuration);
        //创建 CosXmlService 对象，实现对象存储服务各项操作.
        Context context = getApplicationContext();//应用的上下文
        cosXmlService = new CosXmlService(context,serviceConfig, localCredentialProvider);

        //String srcPath= Environment.getExternalStorageDirectory().getPath().toString()+"/Pictures/1.jpg"; // 如 srcPath = Environment.getExternalStorageDirectory().getPath() + "/test.txt";
        String srcPath=FileName;
        putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);
        putObjectRequest.setSign(signDuration,null,null);
     /*设置进度显示
        实现 QCloudProgressListener.onProgress(long progress, long max)方法，
        progress 已上传的大小， max 表示文件的总大小
     */
        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 100.0/max);
                Log.w("TEST","progress =" + (long)result + "%");
            }
        });
    }

    public void upload(){
        handler2=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 1:
                        Toast.makeText(Sound_add.this,"上传成功",Toast.LENGTH_SHORT).show();
                        Log.i(TAG,"hello : "+"hello");

                }
            }

        };

        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com/picture/1.jpg";
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com/sound/"+book_id+sound_time_string+".3pg";
                    //urlimg="http://xl-1255732607.cosgz.myqcloud.com"+"/picture/"+notebook_id+diary_date+picture1_or_picture2+".jpg";
                    url=new URL(urlimg);
                    InputStream is= url.openStream();
                    //
                    handler2.sendEmptyMessage(1);
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                }catch(IOException e){
                    e.printStackTrace();
                } catch (CosXmlServiceException e) {
                    e.printStackTrace();
                } catch (CosXmlClientException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
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
                        Thread.sleep(500);
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
                    sound_name_string=sound_name.getText().toString();
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="insert into sound(sound_book_id,sound_file_url,sound_name,sound_time,sound_size) values("+book_id+",'"
                                +urlimg+"','"+sound_name_string+"','"+sound_time_string+"','"+sound_durantion.getText().toString()+"')";
                        database.execSQL(sql);
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
