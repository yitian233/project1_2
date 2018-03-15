package com.xl.project.project1_0;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static android.content.ContentValues.TAG;
/**
 * Created by xl on 2018/1/1.
 */

public class Diary_book extends AppCompatActivity {
    private MyApp myapp;
    private String notebook_id;
    private Model.Diary temp;

    private RecyclerView diary_list;
    private CommonAdapter<Model.Diary> mDiaryCommonAdapter ;
    private GridLayoutManager diary_list_layoutManager;
    private ResultSet rs;
    private FloatingActionButton diary_add;
    private ProgressBar progressBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode==12){
            Toast.makeText(Diary_book.this,"添加成功",Toast.LENGTH_SHORT).show();
            notebook_id=data.getExtras().getString("notebook_id");
        }
        select_diary();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_main);
        Log.i(TAG,"diary_book");
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            notebook_id=bundle.getString("id");
        }
        bindview();
        innit_recycler();
        select_diary();
    }
    public void bindview(){
        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        diary_add=(FloatingActionButton)findViewById(R.id.add_item);
        diary_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Diary_book.this,Diary_Add.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                startActivityForResult(intent,12);
//                Intent intent =new Intent(Diary_book.this,Diary_Add.class);
//                startActivity(intent);
            }
        });
    }

    public void innit_recycler(){
        diary_list=(RecyclerView)findViewById(R.id.recyleView_notebook);
        diary_list_layoutManager=new GridLayoutManager(this,2);
        diary_list_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        diary_list.setLayoutManager(diary_list_layoutManager);

        mDiaryCommonAdapter=new CommonAdapter<Model.Diary>(this,R.layout.brief_diary_title,myapp.mDiaryList) {
            @Override
            public void convert(ViewHolder holder, Model.Diary diary) {
                TextView diary_title=holder.getView(R.id.diary_title);
                diary_title.setText(diary.getDiary_title());
                TextView diary_time=holder.getView(R.id.diary_time);
                diary_time.setText(diary.getDiary_date());
            }
        };
        mDiaryCommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent To_content_intent=new Intent(Diary_book.this,Diary_detail.class);
                Bundle bundle=new Bundle();
                bundle.putString("id",myapp.mDiaryList.get(position).getDiary_id());
                To_content_intent.putExtras(bundle);
                startActivityForResult(To_content_intent,21);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        diary_list.setAdapter(mDiaryCommonAdapter);
    }

    public void select_diary(){
        myapp.mDiaryList.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                                mDiaryCommonAdapter.notifyDataSetChanged();
                                Log.i(TAG,"diary dataset changed");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                progressBar.setVisibility(View.INVISIBLE);
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
                        List<Model.Diary> diaryList=myapp.mDBOpenHelper.read_Diary(notebook_id);
                        for(int i=0;i<diaryList.size();i++){
                            myapp.mDiaryList.add(diaryList.get(i));
                        }
                        handler.obtainMessage(123).sendToTarget();

                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"加载diarylist失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
