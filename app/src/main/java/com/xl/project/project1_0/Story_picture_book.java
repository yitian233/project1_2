package com.xl.project.project1_0;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2018/1/7.
 */

public class Story_picture_book extends AppCompatActivity {
    private MyApp myapp;
    private String book_id;
    private FloatingActionButton add_story_picture;
    private RecyclerView mRecyclerView;
    private CommonAdapter<Model.Story_picture> mStoryPictureCommonAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ResultSet rs;
    private Model.Story_picture temp;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_main);
        Log.i(TAG,"diary_book");
        Bundle bundle=getIntent().getExtras();
        if(bundle!=null){
            book_id=bundle.getString("id");
        }
        bindview();
        innit_recycler();
        select_story_picture();
    }
    private void bindview() {
        progressBar=(ProgressBar)findViewById(R.id.progress_bar) ;
        add_story_picture=(FloatingActionButton)findViewById(R.id.add_item);
        add_story_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Story_picture_book.this,Story_picture_Add.class);
                Bundle bundle=new Bundle();
                bundle.putString("book_id",book_id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void innit_recycler() {
        mRecyclerView=(RecyclerView)findViewById(R.id.recyleView_notebook);
        mGridLayoutManager=new GridLayoutManager(this,2);
        mGridLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mStoryPictureCommonAdapter=new CommonAdapter<Model.Story_picture>(this, R.layout.brief_story_picture_item,myapp.mStoryPictureList) {
            @Override
            public void convert(ViewHolder holder, Model.Story_picture picture) {
                TextView title=holder.getView(R.id.story_picture_title);
                title.setText(picture.getStory_picture_title());
                TextView time=holder.getView(R.id.story_picture_time);
                time.setText(picture.getStory_picture_time());
            }
        };
        mStoryPictureCommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent To_content_intent=new Intent(Story_picture_book.this,Story_picture_detail.class);
                Bundle bundle=new Bundle();
                bundle.putString("id",myapp.mStoryPictureList.get(position).getStory_picture_id());
                To_content_intent.putExtras(bundle);
                startActivity(To_content_intent);
            }

            @Override
            public void onLongClick(int position) {
                AlertDialog.Builder builder=new AlertDialog.Builder(Story_picture_book.this);
                builder.setTitle("确定要删除？");

                builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface,int i){
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.i(TAG,"size: "+myapp.mStoryPictureList.size());
                        delete_from_database(myapp.mStoryPictureList.get(position).getStory_picture_id());
                        myapp.mStoryPictureList.remove(position);
                        mStoryPictureCommonAdapter.notifyDataSetChanged();
                    }
                });
                AlertDialog b=builder.create();
                b.show();
            }
        });
        mRecyclerView.setAdapter(mStoryPictureCommonAdapter);
    }

    private void select_story_picture() {
        myapp.mStoryPictureList.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                                mStoryPictureCommonAdapter.notifyDataSetChanged();
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
                        List<Model.Story_picture> story_pictureList=myapp.mDBOpenHelper.read_Story_picture(book_id);
                        for(int i=0;i<story_pictureList.size();i++){
                            myapp.mStoryPictureList.add(story_pictureList.get(i));
                        }
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"加载story_p_list失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void delete_from_database(String id){
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
                        String sql="delete  from story_picture where story_picture_id='"+id+"';";
                        database.execSQL(sql);
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"story_pic_删除失败！");
                    }
                }
            }
        });
        thread.start();
    }


}
