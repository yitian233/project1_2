package com.xl.project.project1_0;

import android.content.Intent;
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
import java.util.List;

import static android.content.ContentValues.TAG;

public class List_book extends AppCompatActivity {
    private MyApp myapp;
    private RecyclerView list_title_list;
    private CommonAdapter<Model.List> mList_CommonAdapter;
    private GridLayoutManager List_layoutManager;
    private String notebook_id;
    private ResultSet rs;
    private String s;
    private Model.List temp;
    private FloatingActionButton FB;
    private ProgressBar progressBar;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
      if(requestCode==21&&resultCode==123){
            Toast.makeText(List_book.this,"保存成功",Toast.LENGTH_SHORT).show();
            notebook_id=data.getExtras().getString("notebook_id");
        }
        Log.i("tag","node_id:"+notebook_id);
        select_list();
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
        select_list();
    }
    public void bindview(){
        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        FB=(FloatingActionButton)findViewById(R.id.add_item);
        FB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(List_book.this,List_Add.class);
                Bundle bundle=new Bundle();
                bundle.putString("notebook_id",notebook_id);
                intent.putExtras(bundle);
                startActivityForResult(intent,21);
            }
        });
    }
    public void innit_recycler(){
        list_title_list=(RecyclerView)findViewById(R.id.recyleView_notebook);
        List_layoutManager=new GridLayoutManager(this,2);
        List_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        list_title_list.setLayoutManager(List_layoutManager);

        mList_CommonAdapter=new CommonAdapter<Model.List>(this, R.layout.brief_list_title,myapp.mList_List) {
            @Override
            public void convert(ViewHolder holder, Model.List title) {
                TextView list_title=holder.getView(R.id.week_text);
                list_title.setText("第 "+title.getWeek_num()+" 周");
                TextView first_day =holder.getView(R.id.first_day_text);
                first_day.setText(title.getFirst_day());
                TextView last_day =holder.getView(R.id.last_day_text);
                last_day.setText(title.getLast_day());

            }
        };
        mList_CommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                Intent To_content_intent=new Intent(List_book.this,List_detail.class);
                Bundle bundle=new Bundle();
                bundle.putString("id",myapp.mList_List.get(position).getList_id());
                bundle.putString("week_num",myapp.mList_List.get(position).getWeek_num());
                bundle.putString("first_day",myapp.mList_List.get(position).getFirst_day());
                bundle.putString("last_day",myapp.mList_List.get(position).getLast_day());
                Log.i("tag","list_id:"+myapp.mList_List.get(position).getList_id());
                To_content_intent.putExtras(bundle);
                startActivityForResult(To_content_intent,2);
            }

            @Override
            public void onLongClick(int position) {
                remove_to_database(myapp.mList_List.get(position).getList_id());
                myapp.mList_List.remove(position);
                mList_CommonAdapter.notifyDataSetChanged();
                //select_list();
                Toast.makeText(List_book.this,"删除成功",Toast.LENGTH_SHORT).show();
            }
        });
        list_title_list.setAdapter(mList_CommonAdapter);
    }
    public void select_list(){
        myapp.mList_List.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            mList_CommonAdapter.notifyDataSetChanged();
                            Log.i("tag","item_count:"+mList_CommonAdapter.getItemCount());
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
                        List<Model.List> listList=myapp.mDBOpenHelper.read_List(notebook_id);
                        for(int i=0;i<listList.size();i++){
                            myapp.mList_List.add(listList.get(i));
                        }
                        Log.i(TAG,"加载list列表");
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"list_book加载失败！");
                    }
                }
            }
        });
        thread.start();
    }
    public void remove_to_database(String list_id){
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
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
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="delete from list where list_id='"+list_id+"';";
                        database.execSQL(sql);
                        Log.i("tag","sql:"+sql);
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"list删除失败！");
                    }
                }
            }
        });
        thread.start();
    }
}
