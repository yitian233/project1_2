package com.xl.project.project1_0;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
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
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
 * Created by xl on 2018/1/6.
 */

public class Story_picture_Add extends AppCompatActivity {
    private final int CHOOSE_ALBUM=111,TAKE_PHOTO=112;
    private LinearLayout linearLayout;
    private String imagePath;
    private CosXmlServiceConfig serviceConfig;

    private String s;
    private CosXmlService cosXmlService;
    private PutObjectRequest putObjectRequest;
    private Thread thread;
    private Handler handler;
    private Handler handler2;
    private Bitmap bitmap;
    private static final String image_unspecified="image/*";
    private String text_or_picture;
    private int cnt=0;
    private String[] item_string=new String[]{"","","","","","","","","",""};
    private String identify="";//text=0   image=1 nothing=2
    private long time;
    private Date date;
    private SimpleDateFormat format;
    private String Current_time,Current_date;
    private String book_id,urlimg;
    private String title_string;
    private MyApp myapp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.story_picture);
        myapp= (MyApp) getApplication();
        get_System_time();
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            book_id=bundle.getString("book_id");
        }

        cnt=0;
        linearLayout=(LinearLayout)findViewById(R.id.story_picture_linearlayout);
        ImageView add_text=(ImageView)findViewById(R.id.add_text);
        ImageView add_picture=(ImageView)findViewById(R.id.add_picture);
        Button finish=(Button)findViewById(R.id.finish_story_picture);
        Button back=(Button)findViewById(R.id.back_story_picture);
        ImageView remove_view=(ImageView)findViewById(R.id.remove);

        remove_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"cnt "+cnt);
                linearLayout.removeViewAt(cnt+2);
                cnt=cnt-1;
                identify=identify.substring(0,identify.length()-1);
                Log.i(TAG,"identify: "+identify);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        add_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cnt>=10){
                    //升级VIP
                }else{
                    createText(cnt);
                }

            }
        });
        add_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cnt>=10){
                    //升级VIP
                }else{
                    createImageView(cnt);
                }

            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<cnt;i++){
                    if(identify.charAt(i)=='0'){
                        EditText temp=(EditText)findViewById(i);
                        item_string[i]=temp.getEditableText().toString();
                    }
                }
                EditText editText=(EditText)findViewById(R.id.story_picture_title);
                title_string=editText.getEditableText().toString();
                add_to_database();
            }
        });
    }

    private void createText(int id){
        EditText editText=new EditText(this);
        editText.setPadding(100,10,10,10);
        editText.setId(id);
        Log.i(TAG,"textView id: "+cnt);
        LinearLayout.LayoutParams params= new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(30,30,30,30);
        identify=identify+"0";
        Log.i(TAG,"identify : "+identify);
        cnt++;
        linearLayout.addView(editText,params);
    }

    private void createImageView(int id){
        ImageView imageView=new ImageView(this);
        imageView.setId(id);
        imageView.setPadding(10,10,10,10);
        Log.i(TAG,"image id: "+cnt);
        select_picture();
        handler2=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 1:
                        Log.i(TAG,"hello : "+"hello");
                        imageView.setImageBitmap(bitmap);
                        item_string[cnt]=urlimg;
                        identify=identify+"1";
                        Log.i(TAG,"identify : "+identify);
                        cnt++;
                }
            }

        };
        linearLayout.addView(imageView);
    }

    public void get_System_time(){
        time=System.currentTimeMillis();
        date=new Date(time);
        format=new SimpleDateFormat("yyyy-MM-dd");
        Current_date=format.format(date);
        format=new SimpleDateFormat("HH-mm-ss");
        Current_time=format.format(date);
    }

    public void select_picture(){
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
        cosPath="/picture/"+book_id+Current_time+cnt+".jpg";
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
                int current_progress= (int) (progress/max)*100;
                Log.w("TEST","progress =" + (long)result + "%");
            }
        });
    }

    public void upload(){
        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com/picture/1.jpg";
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com"+"/picture/"+book_id+Current_time+cnt+".jpg";
                    Log.i(TAG,"urlimg: "+urlimg);

//                    item_string[cnt]=urlimg;
//                    identify=identify+"1";

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
                        Thread.sleep(500);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        String temp_sql="insert into story_picture(story_picture_title,book_id,time,identify";
                        for(int i=1;i<=cnt;i++){
                            temp_sql=temp_sql+",";
                            temp_sql=temp_sql+"item";
                            temp_sql=temp_sql+i;
                        }
                        temp_sql=temp_sql+") values('"+title_string+"','"+book_id+"','"+Current_date+"','"+identify;
                        for(int i=0;i<cnt;i++){
                            temp_sql=temp_sql+"','";
                            temp_sql=temp_sql+item_string[i];
                        }
                        temp_sql=temp_sql+"');";

                        String sql=temp_sql;
                        Log.i(TAG,"temp sql : "+temp_sql);
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        database.execSQL(sql,new String[]{});
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"story_pic添加失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
