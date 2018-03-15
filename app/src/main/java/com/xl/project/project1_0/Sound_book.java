package com.xl.project.project1_0;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
 * Created by xl on 2018/1/6.
 */

public class Sound_book extends AppCompatActivity {
    private MyApp myapp;
    private String book_id;
    private FloatingActionButton add_sound;
    private RecyclerView sound_list;
    private GridLayoutManager mGridLayoutManager;
    private CommonAdapter<Model.Sound> mSoundCommonAdapter;
    private ResultSet rs;
    private Model.Sound temp;
    private ProgressBar progressBar;
    private TextView duration;
    private String duration_string;
    @Override
    protected void onResume(){
        super.onResume();
        select_sound();
        Log.i("tag","select_sound_list");

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.sound_book);
        Bundle bundle=getIntent().getExtras();
        book_id=bundle.getString("id");
        bindview();
        innit_recycler();
        select_sound();
    }

    private void innit_recycler() {
        mGridLayoutManager=new GridLayoutManager(this,1);
        sound_list=(RecyclerView)findViewById(R.id.sound_list);
        sound_list.setLayoutManager(mGridLayoutManager);
        mSoundCommonAdapter=new CommonAdapter<Model.Sound>(this,R.layout.brief_sound_title,myapp.mSoundList) {
            @Override
            public void convert(ViewHolder holder, Model.Sound sound) {
                TextView sound_name=holder.getView(R.id.sound_name_itme);
                sound_name.setText(sound.getSound_name());
                TextView sound_time=holder.getView(R.id.sound_time_item);
                sound_time.setText(sound.getSound_time());
                TextView sound_size=holder.getView(R.id.sound_size_item);
                sound_size.setText(sound.getSound_size());
            }
        };
        mSoundCommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent intent =new Intent(Sound_book.this,Sound_detail.class);
                Bundle bundle=new Bundle();
                bundle.putString("sound_id",myapp.mSoundList.get(position).getSound_id());
                intent.putExtras(bundle);
                startActivity(intent);
            }
            @Override
            public void onLongClick(int position) {
                remove_to_database(myapp.mSoundList.get(position).getSound_id());
                myapp.mSoundList.remove(position);
                mSoundCommonAdapter.notifyDataSetChanged();

            }
        });
        sound_list.setAdapter(mSoundCommonAdapter);
    }

    private void bindview() {
        progressBar=(ProgressBar)findViewById(R.id.progress_bar_sound);
        add_sound=(FloatingActionButton)findViewById(R.id.add_sound);
        sound_list=(RecyclerView)findViewById(R.id.sound_list);

        add_sound.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Sound_book.this,Sound_add.class);
                Bundle bundle=new Bundle();
                bundle.putString("book_id",book_id);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    private void select_sound() {
        myapp.mSoundList.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                                mSoundCommonAdapter.notifyDataSetChanged();
                            Log.i("tag","list_size:"+myapp.mSoundList.size());
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
                        List<Model.Sound> sounds=myapp.mDBOpenHelper.read_Sound(book_id);
                        for(int i=0;i<sounds.size();i++){
                            myapp.mSoundList.add(sounds.get(i));
                        }
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"加载soundlist失败！");
                    }
                }
            }
        });
        thread.start();
    }
    public void remove_to_database(String sound_id){
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
                        String sql="delete from sound where sound_id='"+sound_id+"';";
                        database.execSQL(sql);
                        Log.i("tag","sql:"+sql);
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"删除sound失败！");
                    }
                }
            }
        });
        thread.start();
    }
}
