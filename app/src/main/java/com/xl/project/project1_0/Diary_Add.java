package com.xl.project.project1_0;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.tencent.cos.xml.CosXmlService;
import com.tencent.cos.xml.CosXmlServiceConfig;
import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.exception.CosXmlServiceException;
import com.tencent.cos.xml.model.object.PutObjectRequest;
import com.tencent.cos.xml.model.object.PutObjectResult;
import com.tencent.qcloud.core.network.QCloudProgressListener;

import java.io.File;
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
 * Created by xl on 2018/1/4.
 */

public class Diary_Add extends AppCompatActivity {
    private MyApp myapp;
    private String notebook_id;
    private EditText diary_content;
    private Button diary_back,diary_ok;
    private String diary_content_string,diary_date_string,diary_day_string;
    private TextView diary_date,diary_day;
    private ImageView diary_choose_weather,diary_choose_mood;
    private long time;
    private Date date;
    private SimpleDateFormat format;
    private String diary_weather_string="sunny",diary_mood_string="excite";
    private ImageView diary_picture1,diary_picture2;

    private String s;
    private CosXmlService cosXmlService;
    private PutObjectRequest putObjectRequest;
    private Thread thread;
    private Handler handler;
    private Handler handler2;
    private Bitmap bitmap;
    private static final String image_unspecified="image/*";
    private final int CHOOSE_ALBUM=111,TAKE_PHOTO=112;
    private String imagePath;
    private CosXmlServiceConfig serviceConfig;
    private String picture1_or_picture2="1",picture1_url,picture2_url;
    private String urlimg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.diary);
        Bundle bundle=getIntent().getExtras();
        notebook_id=bundle.getString("notebook_id");
        get_System_time();
        bindview();
    }

    public void get_System_time(){
        time=System.currentTimeMillis();
        date=new Date(time);
        format=new SimpleDateFormat("yyyy-MM-dd");
        diary_date_string=format.format(date);
        format=new SimpleDateFormat("EEEE");
        diary_day_string=format.format(date);
    }

    public void bindview(){
        diary_date=(TextView)findViewById(R.id.diary_date);
        diary_day=(TextView)findViewById(R.id.diary_day);
        diary_date.setText(diary_date_string);
        diary_day.setText(diary_day_string);
        diary_content=(EditText)findViewById(R.id.diary_content);

        diary_back=(Button)findViewById(R.id.diary_back);
        diary_ok=(Button)findViewById(R.id.diary_ok);
        diary_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Diary_Add.this,Diary_book.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                setResult(34,intent);
                finish();
            }
        });
        diary_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                diary_content_string=diary_content.getEditableText().toString();
                add_to_database();
                Intent intent=new Intent(Diary_Add.this,Diary_book.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                setResult(56,intent);
                finish();
            }
        });


        diary_choose_weather=(ImageView)findViewById(R.id.diary_weather);
        diary_choose_weather.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factor=LayoutInflater.from(Diary_Add.this);
                View layout=factor.inflate(R.layout.diary_weather_choose,null);
                AlertDialog builder=new AlertDialog.Builder(Diary_Add.this).create();
                builder.setView(layout);

                ImageView sunny=(ImageView)layout.findViewById(R.id.diary_weather_sunny);
                ImageView cloudy=(ImageView)layout.findViewById(R.id.diary_weather_cloud);
                ImageView drizzle=(ImageView)layout.findViewById(R.id.diary_weather_drizzle);
                ImageView lightning=(ImageView)layout.findViewById(R.id.diary_weather_lightning);
                ImageView rainb=(ImageView)layout.findViewById(R.id.diary_weather_rainb);
                ImageView wind=(ImageView)layout.findViewById(R.id.diary_weather_wind);
                ImageView tornado=(ImageView)layout.findViewById(R.id.diary_weather_tornado);
                ImageView snowflow=(ImageView)layout.findViewById(R.id.diary_weather_snowflow);
                ImageView sunset=(ImageView)layout.findViewById(R.id.diary_weather_sunset);

                sunny.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="sunny";
                        diary_choose_weather.setImageResource(R.mipmap.sunny);
                        builder.dismiss();
                    }
                });
                cloudy.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="cloudy";
                        diary_choose_weather.setImageResource(R.mipmap.cloudy);
                        builder.dismiss();
                    }
                });
                drizzle.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="drizzle";
                        diary_choose_weather.setImageResource(R.mipmap.drizzle);
                        builder.dismiss();
                    }
                });
                lightning.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="lightning";
                        diary_choose_weather.setImageResource(R.mipmap.lightning);
                        builder.dismiss();
                    }
                });
                rainb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="rainb";
                        diary_choose_weather.setImageResource(R.mipmap.rainb);
                        builder.dismiss();
                    }
                });
                wind.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="wind";
                        diary_choose_weather.setImageResource(R.mipmap.wind);
                        builder.dismiss();
                    }
                });
                tornado.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="tornado";
                        diary_choose_weather.setImageResource(R.mipmap.tornado);
                        builder.dismiss();
                    }
                });
                snowflow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="snowflow";
                        diary_choose_weather.setImageResource(R.mipmap.snowflow);
                        builder.dismiss();
                    }
                });
                sunset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_weather_string="sunset";
                        diary_choose_weather.setImageResource(R.mipmap.sunset);
                        builder.dismiss();

                    }
                });
               builder.show();
            }
        });


        diary_choose_mood=(ImageView)findViewById(R.id.diary_mood);
        diary_choose_mood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater factor=LayoutInflater.from(Diary_Add.this);
                View layout=factor.inflate(R.layout.diary_choose_mood,null);
                AlertDialog builder=new AlertDialog.Builder(Diary_Add.this).create();
                builder.setView(layout);

                ImageView excite=(ImageView)layout.findViewById(R.id.diary_mood_excite);
                ImageView sleeping=(ImageView)layout.findViewById(R.id.diary_mood_sleeping);
                ImageView mad=(ImageView)layout.findViewById(R.id.diary_mood_mad);
                ImageView dead=(ImageView)layout.findViewById(R.id.diary_mood_dead);
                ImageView confused=(ImageView)layout.findViewById(R.id.diary_mood_confused);
                ImageView wink=(ImageView)layout.findViewById(R.id.diary_mood_wink);
                ImageView worry=(ImageView)layout.findViewById(R.id.diary_mood_worry);
                ImageView surprised=(ImageView)layout.findViewById(R.id.diary_mood_surprised);
                ImageView speechless=(ImageView)layout.findViewById(R.id.diary_mood_speechless);


                excite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="excite";
                        diary_choose_mood.setImageResource(R.mipmap.excite);
                        builder.dismiss();
                    }
                });
                sleeping.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="sleeping";
                        diary_choose_mood.setImageResource(R.mipmap.sleeping);
                        builder.dismiss();
                    }
                });
                mad.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="mad";
                        diary_choose_mood.setImageResource(R.mipmap.mad);
                        builder.dismiss();
                    }
                });
                dead.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="dead";
                        diary_choose_mood.setImageResource(R.mipmap.dead);
                        builder.dismiss();
                    }
                });
                confused.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="confused";
                        diary_choose_mood.setImageResource(R.mipmap.confused);
                        builder.dismiss();
                    }
                });
                wink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="wink";
                        diary_choose_mood.setImageResource(R.mipmap.wink);
                        builder.dismiss();
                    }
                });
                worry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="worry";
                        diary_choose_mood.setImageResource(R.mipmap.worry);
                        builder.dismiss();
                    }
                });
                surprised.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="surprised";
                        diary_choose_mood.setImageResource(R.mipmap.surprised);
                        builder.dismiss();
                    }
                });
                speechless.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        diary_mood_string="speechless";
                        diary_choose_mood.setImageResource(R.mipmap.speechless);
                        builder.dismiss();
                    }
                });
                builder.show();
            }
        });

        diary_picture1=(ImageView)findViewById(R.id.diary_picture1);
        diary_picture2=(ImageView)findViewById(R.id.diary_picture2);
        diary_picture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Diary_Add.this);
                builder.setTitle("上传图片");
                builder.setItems(new String[]{"拍摄", "从相册中选择"}, new DialogInterface
                        .OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        switch(which){
                            case 0:
                                takePhoto();
                                picture1_or_picture2="1";
                                break;
                            case 1:
                                select_picture();
                                picture1_or_picture2="1";

                        }
                    }
                });
                builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface,int i){
                    }
                });
                AlertDialog b=builder.create();
                b.show();
//                select_picture();
            }
        });

        diary_picture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Diary_Add.this);
                builder.setTitle("上传图片");
                builder.setItems(new String[]{"拍摄", "从相册中选择"}, new DialogInterface
                        .OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog,int which){
                        switch(which){
                            case 0:
                                takePhoto();
                                picture1_or_picture2="2";
                                break;
                            case 1:
                                select_picture();
                                picture1_or_picture2="2";

                        }
                    }
                });
                builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface,int i){
                    }
                });
                AlertDialog b=builder.create();
                b.show();

            }
        });
    }

    public void takePhoto(){
        File file=new File(Environment.getExternalStorageDirectory(),"Picture");
        if(!file.exists()){
            file.delete();
        }
        Uri imageUri = Uri.fromFile(file);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }

    public void select_picture(){
//        Intent intent =new Intent(Intent.ACTION_GET_CONTENT,null);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,image_unspecified);
//        startActivityForResult(intent,image_code);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        Log.i(TAG,"here ");
        startActivityForResult(intent, CHOOSE_ALBUM);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        ContentResolver resolver=getContentResolver();
        Log.i(TAG,"onActivityResult");

        if(requestCode==CHOOSE_ALBUM){
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "READ permission IS NOT granted...");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 321);
            }
            else{
                Uri originalUri = data.getData();
                //        imagePath=originalUri.toString();
                Log.i(TAG,"original uri: "+originalUri);
                imagePath=getRealFilePath(this,originalUri);
                Log.i(TAG,"imagePath : "+imagePath);
                innit();
                upload();
            }
        }
        else if(requestCode==TAKE_PHOTO){
            File file=new File(Environment.getExternalStorageDirectory(),"Picture");
            Uri uri=Uri.fromFile(file);
            imagePath=getRealFilePath(this,uri);
            Log.i(TAG,"imagePath : "+imagePath);
            innit();
            upload();
        }
    }

    public static String getRealFilePath(final Context context, final Uri uri ) {
        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null ){
            data = uri.getPath();
            Log.i(TAG,"data path1:"+data);
        }
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
            Log.i(TAG,"data path2:"+data);
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
            Log.i(TAG,"data path3:"+data);
        }
        return data;
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

        String cosPath="/picture/1.jpg";//存储在服务器上的路径
        cosPath="/picture/"+notebook_id+diary_date_string+picture1_or_picture2+".jpg";
        Log.i(TAG,"cosPath: "+cosPath);
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
        String srcPath=imagePath;
        putObjectRequest = new PutObjectRequest(bucket, cosPath, srcPath);
        putObjectRequest.setSign(signDuration,null,null);
     /*设置进度显示
        实现 QCloudProgressListener.onProgress(long progress, long max)方法，
        progress 已上传的大小， max 表示文件的总大小
     */
        putObjectRequest.setProgressListener(new QCloudProgressListener() {
            @Override
            public void onProgress(long progress, long max) {
                float result = (float) (progress * 200.0/max);
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
                        Log.i(TAG,"hello : "+"hello");
                        if(picture1_or_picture2.equals("1")){
                            diary_picture1.setImageBitmap(bitmap);
                        }
                        else{
                            diary_picture2.setImageBitmap(bitmap);
                        }

                }
            }

        };

        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com/picture/1.jpg";
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com"+"/picture/"+notebook_id+diary_date_string+picture1_or_picture2+".jpg";
                    Log.i(TAG,"urlimg: "+urlimg);
                    if(picture1_or_picture2.equals("1")){
                        picture1_url=urlimg;
                    }
                    else{
                        picture2_url=urlimg;
                    }
                    URL url=new URL(urlimg);
                    InputStream is= url.openStream();
                    bitmap = BitmapFactory.decodeStream(is);
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
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="insert into diary(note_book_id,diary_date,diary_day,diary_weather,diary_mood,diary_content,diary_picture1,diary_picture2) values("+notebook_id+",'"
                                +diary_date_string+"','"+diary_day_string+"','"+diary_weather_string+"','"+diary_mood_string+"','"+diary_content_string+"','"+picture1_url+"','"+picture2_url+"')";
                        database.execSQL(sql);
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"dairy添加失败！");
                    }
                }
            }
        });
        thread.start();
    }
}
