package com.xl.project.project1_0;

/**
 * Created by xl on 2018/1/3.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
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

public class test extends AppCompatActivity {
    private ImageView imageView;
    private CosXmlServiceConfig serviceConfig;
    private CosXmlService cosXmlService;
    private PutObjectRequest putObjectRequest;
    private String urlimg;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);
        imageView=(ImageView)findViewById(R.id.image);
        init();
        Thread thread=new Thread(){
            @Override
            public void run(){
                try {
                    PutObjectResult putObjectResult = cosXmlService.putObject(putObjectRequest);
                    urlimg="http://xl-1255732607.cosgz.myqcloud.com/picture/1.jpg";
                    URL url=new URL(urlimg);
                    InputStream is= url.openStream();
                    bitmap = BitmapFactory.decodeStream(is);
                    handler.sendEmptyMessage(1);
                } catch (CosXmlClientException e) {
                    //抛出异常
                    Log.w("TEST","CosXmlClientException =" + e.toString());
                } catch (CosXmlServiceException e) {
                    //抛出异常
                    Log.w("TEST","CosXmlServiceException =" + e.toString());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };thread.start();
    }

    public void init(){
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
        String srcPath= Environment.getExternalStorageDirectory().getPath().toString()+"/Pictures/1.jpg"; // 如 srcPath = Environment.getExternalStorageDirectory().getPath() + "/test.txt";
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
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==1) {
                imageView.setImageBitmap(bitmap);
            }
        }
    };
}
