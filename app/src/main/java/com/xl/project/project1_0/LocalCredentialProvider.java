package com.xl.project.project1_0;


import android.util.Log;

import com.tencent.cos.xml.exception.CosXmlClientException;
import com.tencent.cos.xml.utils.StringUtils;
import com.tencent.qcloud.core.network.auth.BasicLifecycleCredentialProvider;
import com.tencent.qcloud.core.network.auth.BasicQCloudCredentials;
import com.tencent.qcloud.core.network.auth.QCloudLifecycleCredentials;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by ER on 2017/12/25.
 */

public class LocalCredentialProvider extends BasicLifecycleCredentialProvider {
    private String secretKey;
    private long keyDuration;
    private String secretId;

    public LocalCredentialProvider(String secretId, String secretKey, long keyDuration) {
        this.secretId = secretId;
        this.secretKey = secretKey;
        this.keyDuration = keyDuration;
    }

    /**
     返回 BasicQCloudCredentials
     */
    @Override
    public QCloudLifecycleCredentials fetchNewCredentials() throws CosXmlClientException {
        long current = System.currentTimeMillis() / 1000L;
        Log.v("current",String.valueOf(current));
        long expired = current + keyDuration;
        String keyTime = current+";"+expired;
        return new BasicQCloudCredentials(secretId, secretKeyToSignKey(secretKey, keyTime), keyTime);
    }

    private String secretKeyToSignKey(String secretKey, String keyTime) {
        String signKey = null;
        try {
            if (secretKey == null) {
                throw new IllegalArgumentException("secretKey is null");
            }
            if (keyTime == null) {
                throw new IllegalArgumentException("qKeyTime is null");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        try {
            byte[] byteKey = secretKey.getBytes("utf-8");
            SecretKey hmacKey = new SecretKeySpec(byteKey, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(hmacKey);
            signKey = StringUtils.toHexString(mac.doFinal(keyTime.getBytes("utf-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return signKey;
    }
}



//import android.util.Log;
//
//import com.tencent.cos.xml.exception.CosXmlClientException;
//import com.tencent.cos.xml.utils.StringUtils;
//import com.tencent.qcloud.core.network.auth.BasicLifecycleCredentialProvider;
//import com.tencent.qcloud.core.network.auth.BasicQCloudCredentials;
//import com.tencent.qcloud.core.network.auth.QCloudLifecycleCredentials;
//
//import java.io.UnsupportedEncodingException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//
//import javax.crypto.Mac;
//import javax.crypto.SecretKey;
//import javax.crypto.spec.SecretKeySpec;
//
///**
// * Created by xl on 2018/1/3.
// */
//
//public class LocalCredentialProvider extends BasicLifecycleCredentialProvider {
//    private String secretKey;
//    private long keyDuration;
//    private String secretId;
//
//    public LocalCredentialProvider(String secretId,String secretKey,long keyDuration){
//        this.secretId=secretId;
//        this.secretKey=secretKey;
//        this.keyDuration=keyDuration;
//    }
//
//    @Override
//    public QCloudLifecycleCredentials fetchNewCredentials() throws CosXmlClientException{
//        long current =System.currentTimeMillis()/1000L;
//        Log.v("current time: ", String.valueOf(current));
//        long exprired=current+keyDuration;
//        String keyTime=current+":"+exprired;
//        Log.v("keyDuration time: ",String.valueOf(keyDuration));
//        Log.v("keyTime time: ",keyTime);
//        return new BasicQCloudCredentials(secretId,secretKeyToSignKey(secretKey,keyTime),keyTime);
//    }
//
//    private String secretKeyToSignKey(String secretKey,String keyTime){
//        String signKey=null;
//        try{
//            if(secretKey==null){
//                throw new IllegalArgumentException("secretKey is null");
//            }
//            if(keyTime==null){
//                throw new IllegalArgumentException("qKeyTime is null");
//            }
//        }catch(IllegalStateException e){
//            e.printStackTrace();
//        }
//        try{
//            byte[] byteKey=secretKey.getBytes("utf-8");
//            SecretKey hmacKey =new SecretKeySpec(byteKey,"HmacSHA1");
//            Mac mac=Mac.getInstance("HmacSHA1");
//            mac.init(hmacKey);
//            signKey= StringUtils.toHexString(mac.doFinal(keyTime.getBytes("utf-8")));
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        } catch (InvalidKeyException e) {
//            e.printStackTrace();
//        }
//        return signKey;
//    }
//}
