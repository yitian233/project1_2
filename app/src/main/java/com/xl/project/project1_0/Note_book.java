package com.xl.project.project1_0;

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
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2017/12/31.
 */

public class Note_book extends AppCompatActivity {
    private MyApp myapp;
    private RecyclerView note_title_list;
    private CommonAdapter<Model.Note> mNote_CommonAdapter;
    private GridLayoutManager Note_layoutManager;
    private String notebook_id;
    private ResultSet rs;
    private String s;
    private Model.Note temp;
    private FloatingActionButton FB;
    private ProgressBar progressBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(resultCode==123){
            Log.i(TAG,"onActivityResult");
            select_note();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_main);
        Bundle bundle=getIntent().getExtras();
        notebook_id=bundle.getString("id");
        bindview();
        innit_recycler();
        Log.i(TAG,"onCreate");
        select_note();
    }
    public void bindview(){

        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        FB=(FloatingActionButton)findViewById(R.id.add_item);
        FB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Note_book.this,Note_Add.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                startActivityForResult(intent,21);
            }
        });
    }

    public void innit_recycler(){
        note_title_list=(RecyclerView)findViewById(R.id.recyleView_notebook);
        Note_layoutManager=new GridLayoutManager(this,2);
        Note_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        note_title_list.setLayoutManager(Note_layoutManager);

        mNote_CommonAdapter=new CommonAdapter<Model.Note>(this,R.layout.brief_note_title,myapp.mNote_List) {
            @Override
            public void convert(ViewHolder holder, Model.Note title) {
                TextView note_title=holder.getView(R.id.note_title);
                note_title.setText(title.getNote_title());
                TextView note_time=holder.getView(R.id.note_time);
                note_time.setText(title.getNote_time());
            }
        };
        mNote_CommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent To_content_intent=new Intent(Note_book.this,Note_detail.class);
                Bundle bundle=new Bundle();
                bundle.putString("id",myapp.mNote_List.get(position).getId());
                Log.i("tag","note_id:"+myapp.mNote_List.get(position).getId());
                To_content_intent.putExtras(bundle);
                startActivityForResult(To_content_intent,2);
            }

            @Override
            public void onLongClick(int position) {
                remove_to_database(myapp.mNote_List.get(position).getId());
                mNote_CommonAdapter.notifyDataSetChanged();
                select_note();
                Toast.makeText(Note_book.this,"删除成功",Toast.LENGTH_SHORT).show();
            }
        });
        note_title_list.setAdapter(mNote_CommonAdapter);
    }

    public void select_note(){
        progressBar.setVisibility(View.VISIBLE);
        myapp.mNote_List.clear();
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                                mNote_CommonAdapter.notifyDataSetChanged();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                }
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
                    Log.i(TAG,"run before try");
                    try{
                        List<Model.Note> noteList=myapp.mDBOpenHelper.read_Note(notebook_id);
                        for(int i=0;i<noteList.size();i++){
                            myapp.mNote_List.add(noteList.get(i));
                        }
                        Log.i(TAG,"加载notelist");
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"加载notelist失败！");
                    }
                }
            }
        });
        thread.start();
    }
    public void remove_to_database(String note_id){
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
                        String sql="delete from note where note_id='"+note_id+"';";
                        Log.i("tag","sql:"+sql);
                        database.execSQL(sql);
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"SQLite 删除操作失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
