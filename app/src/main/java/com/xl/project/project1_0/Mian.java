package com.xl.project.project1_0;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;
/**
 * Created by xl on 2018/1/1.
 */
public class Mian extends AppCompatActivity {
    private String s;
    private CosXmlService cosXmlService;
    private PutObjectRequest putObjectRequest;
    private Thread thread;
    private Handler handler;
    private Handler handler2;
    private Bitmap bitmap;
    private static final String image_unspecified="image/*";
    private final int CHOOSE_ALBUM=0,TAKE_PHOTO=1;
    private String imagePath;
    private CosXmlServiceConfig serviceConfig;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        ImageView imageView=(ImageView)findViewById(R.id.image) ;
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //select_picture();
                takePhoto();
            }
        });
//        innit();
//        upload();
    }


    public void select_picture(){
//        Intent intent =new Intent(Intent.ACTION_GET_CONTENT,null);
//        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,image_unspecified);
//        startActivityForResult(intent,image_code);
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, CHOOSE_ALBUM);
    }

    public void takePhoto(){
        File file=new File(Environment.getExternalStorageDirectory(),"Picture");
        if(!file.exists()){
            file.delete();
        }
        Uri imageUri = Uri.fromFile(file);
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PHOTO);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @RequiresApi(api = Build.VERSION_CODES.FROYO)
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data)
    {
        ContentResolver resolver=getContentResolver();

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

    public static String getRealFilePath( final Context context, final Uri uri ) {
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
                        Log.i(TAG,"hello : "+"hello");
                        ImageView test=(ImageView)findViewById(R.id.image);
                        test.setImageBitmap(bitmap);
                }
            }

        };

        Thread thread=new Thread(){
            @Override
            public void run(){
                try{
                    PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);
                    String urlimg="http://xl-1255732607.cosgz.myqcloud.com/picture/1.jpg";
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        Bitmap bm=null;
        ContentResolver resolver=getContentResolver();
        if(requestCode==321)
        {
            Log.i(TAG,"321 ");
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                Log.i(TAG,"321 321 ");
                if(grantResults[0]!= PackageManager.PERMISSION_GRANTED)//没有授予权限的话就去授予权限
                {
                    boolean b=shouldShowRequestPermissionRationale(permissions[0]);
                    if(!b){
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addCategory(Intent.CATEGORY_DEFAULT);
                        intent.setData(Uri.parse("package:" + getPackageName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(intent);
                    }
                    else{
                    }
                }else{
                }
            }
        }
    }


    public void sql_test(){
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        Log.i(TAG,"stu: "+s);
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
//                    String connectString = "jdbc:mysql://172.18.187.233:53306/teaching"
//                            +"?autoReconnect=true&useUnicode=true"
//                            +"&characterEncoding=UTF-8";
                    String connectString = "jdbc:mysql://119.29.229.194:3306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
                        Log.i(TAG,"连接数据库成功!");
                        String sql="select * from note_book";
                        Statement stmt=conn.createStatement();
                        ResultSet rs=stmt.executeQuery(sql);
                        while(rs.next()){
                            s=s+rs.getString("note_book_name");
                            Log.i(TAG,"result_set: "+s);
                        }
                        handler.obtainMessage(123).sendToTarget();
                        rs.close();
                        stmt.close();
                        conn.close();
                        return;
                    }catch(SQLException e){
                        e.printStackTrace();
                        Log.i(TAG,"远程连接失败！");
                    }
                }
            }
        });
        thread.start();
    }


}
