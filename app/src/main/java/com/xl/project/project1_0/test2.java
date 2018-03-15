package com.xl.project.project1_0;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.Word;
import com.baidu.ocr.sdk.model.WordSimple;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
/**
 * Created by xl on 2018/1/4.
 */

public class test2 extends AppCompatActivity {

    private static final int REQUEST_CODE_GENERAL = 105;
    private static final int REQUEST_CODE_GENERAL_BASIC = 106;
    private boolean hasGotToken = false;
    private AlertDialog.Builder alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test2);
        alertDialog = new AlertDialog.Builder(this);
        if(alertDialog==null){
            Log.i(TAG,"alertDialog:NULL");
        }
        else{
            Log.i(TAG,"alertDialog:NOT NULL");
        }
        initAccessTokenWithAkSk();


        //文字识别
        findViewById(R.id.general_basic_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkTokenStatus()) {
                    Log.i("tag","no token");
                    return;
                }
                Intent intent = new Intent(test2.this, mCameraActivity.class);
                intent.putExtra(mCameraActivity.KEY_OUTPUT_FILE_PATH,
                        FileUtil.getSaveFile(getApplication()).getAbsolutePath());
                intent.putExtra(mCameraActivity.KEY_CONTENT_TYPE,
                        mCameraActivity.CONTENT_TYPE_GENERAL);
                Log.i("tag","start camara");
                startActivityForResult(intent, 999);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("tag","onActivityResult");
        // 识别成功回调，通用文字识别（含位置信息）

            Log.i("tag","recognize..");
            RecognizeService.recAccurate(FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath(),
                    new RecognizeService.ServiceListener() {
                        @Override
                        public void onResult(GeneralResult result) {
                            //infoPopText(result);
                            for (WordSimple wordSimple : result.getWordList()) {
                                Word word = (Word) wordSimple;
                                //sb.append(word.getWords());
                                Log.i("tag","word:"+word.getWords());
                                //sb.append("\n");
                            }
                            //这里处理result
                        }
                        @Override
                        public void onError(String error){
                            infoPopText(error);
                        }
                    });



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
                alertText("licence方式获取token失败", error.getMessage());
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
                alertText("AK，SK方式获取token失败", error.getMessage());
            }
        }, getApplicationContext(), "vP3BGlOEmR0nucRLW4sZpvE6", "RZTjGvedPWxyxRw4YHciBKoS8UT6mjfn");
    }


    private void alertText(final String title, final String message) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }


    private void infoPopText(final String result) {
        alertText("", result);
    }

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
