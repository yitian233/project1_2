package com.xl.project.project1_0;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class List_detail extends AppCompatActivity {
    private MyApp myapp;
    private String list_id,first_day_string,last_day_string,week_num_string;
    private TextView first_day,last_day,week_num;
    private EditText list_item_content;
    private ResultSet rs1,rs2;
    private Model.List temp;
    private List<item> todo_list=new ArrayList<item>();
    private List<item> done_list=new ArrayList<item>();
    private RecyclerView todo_recycle,done_recycle;
    private RecyclerView.LayoutManager todo_manager,done_manager;
    private CommonAdapter<item> todo_adapter,done_adapter;
    private Button add_item_btn,list_save_btn;
    private ProgressBar progressBar;
    private Cursor cursor;
    @Override
    protected void onResume(){
        super.onResume();
        select_list_item();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_list_detail);
        Bundle bundle=getIntent().getExtras();
        list_id=bundle.getString("id");
        first_day_string=bundle.getString("first_day");
        last_day_string=bundle.getString("last_day");
        week_num_string=bundle.getString("week_num");
        bindview();
        innit_recycler();
        select_list_item();
        //fillview();
    }
    public void bindview(){
        progressBar=(ProgressBar)findViewById(R.id.progress_bar_list);
        first_day=(TextView)findViewById(R.id.first_day);
        last_day=(TextView)findViewById(R.id.last_day);
        week_num=(TextView)findViewById(R.id.week_number);
        first_day.setText(first_day_string);
        last_day.setText(last_day_string);
        week_num.setText("第 "+week_num_string+"周");

        list_save_btn=(Button)findViewById(R.id.list_save_btn);
        list_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    save_list_item();

            }
        });
        add_item_btn=(Button)findViewById(R.id.add_item);
        add_item_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LayoutInflater factor= LayoutInflater.from(List_detail.this);
                View layout=factor.inflate(R.layout.list_add_item_layout,null);
                final AlertDialog builder=new AlertDialog.Builder(List_detail.this).create();
                builder.setView(layout);
                builder.show();
                EditText content=(EditText) layout.findViewById(R.id.add_item_content);
                Button cancel=(Button)layout.findViewById(R.id.cancel);
                Button confirm=(Button)layout.findViewById(R.id.confirm);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.dismiss();
                    }
                });
                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        item i=new item(content.getText().toString(), R.mipmap.uncheck);
                        todo_list.add(i);
                        todo_adapter.notifyDataSetChanged();
                        builder.dismiss();
                    }

                });

            }
        });
    }


    public void innit_recycler(){
        todo_recycle=(RecyclerView)findViewById(R.id.to_do_list);
        todo_manager=new GridLayoutManager(this,1);
        todo_recycle.setLayoutManager(todo_manager);
        todo_adapter=new CommonAdapter<item>(this, R.layout.list_item,todo_list) {
            @Override
            public void convert(ViewHolder holder, item i) {
                EditText item_content=holder.getView(R.id.item_content);
                item_content.setText(i.getItem_content());
                ImageView img=holder.getView(R.id.item_checkbox);
                img.setBackgroundResource(R.mipmap.uncheck);
            }
        };
        done_recycle=(RecyclerView)findViewById(R.id.done_list);
        done_manager=new GridLayoutManager(this,1);
        done_recycle.setLayoutManager(done_manager);
        done_adapter=new CommonAdapter<item>(this, R.layout.list_item,done_list) {
            @Override
            public void convert(ViewHolder holder, item i) {
                EditText item_content=holder.getView(R.id.item_content);
                item_content.setText(i.getItem_content());
                ImageView img=holder.getView(R.id.item_checkbox);
                img.setBackgroundResource(R.mipmap.check);
            }
        };
        todo_adapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                item i=new item(todo_list.get(position).getItem_content(), R.mipmap.check);
                todo_list.remove(position);
                todo_adapter.notifyDataSetChanged();
                done_list.add(i);
                done_adapter.notifyDataSetChanged();
            }
            @Override
            public void onLongClick(int position) {
                todo_list.remove(position);
                todo_adapter.notifyDataSetChanged();
            }
        });
        todo_recycle.setAdapter(todo_adapter);

        done_adapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                item i=new item(done_list.get(position).getItem_content(), R.mipmap.uncheck);
                done_list.remove(position);
                done_adapter.notifyDataSetChanged();
                todo_list.add(i);
                todo_adapter.notifyDataSetChanged();
            }

            @Override
            public void onLongClick(int position) {
                done_list.remove(position);
                done_adapter.notifyDataSetChanged();
            }
        });
        done_recycle.setAdapter(done_adapter);
    }
    public class item{
        private String item_content;
        private int img;
        public item(String item_content,int img){
            this.item_content=item_content;
            this.img=img;
        }
        public String getItem_content(){return item_content;}
        public int getImg(){return img;}
    }

    public void  save_list_item(){
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        Log.i("tag","update todo_list");
                        break;
                    case 234:
                        Log.i("tag","update done_list");
                        break;
                }
                Intent intent=new Intent(List_detail.this,List_book.class);
                setResult(15,intent);
                progressBar.setVisibility(View.INVISIBLE);
                finish();
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
                        Log.i("tag","interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        String sql="delete from list_item where list_id='"+list_id+"';";
                        database.execSQL(sql);

                        int insert1=0;
                        int insert2=0;
                        for(int i=0;i<todo_list.size();i++){
                            sql="insert into list_item(list_id,item_content,done) values('"+list_id+"','"+todo_list.get(i).getItem_content()+"','no');";
                            Log.i("tag","sql:"+sql);
                            database.execSQL(sql);
                        }handler.obtainMessage(123).sendToTarget();
                        for(int i=0;i<done_list.size();i++){
                            sql="insert into list_item(list_id,item_content,done) values('"+list_id+"','"+done_list.get(i).getItem_content()+"','yes');";
                            Log.i("tag","sql:"+sql);
                            database.execSQL(sql);
                        }handler.obtainMessage(234).sendToTarget();

                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i("tag","list_item保存失败！");
                    }
                }
            }
        });
        thread.start();
    }
    public void  select_list_item(){
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        Log.i("tag","onMessage");
                        try{
                            todo_list.clear();
                            done_list.clear();
                            while(cursor.moveToNext()){
                                Log.i("tag","is done"+cursor.getString(cursor.getColumnIndex("done")));
                                if(cursor.getString(cursor.getColumnIndex("done")).equals("no"))
                                    todo_list.add(new item(cursor.getString(cursor.getColumnIndex("item_content")), R.mipmap.uncheck));
                                else if(cursor.getString(cursor.getColumnIndex("done")).equals("yes"))
                                    done_list.add(new item(cursor.getString(cursor.getColumnIndex("item_content")), R.mipmap.check));
                                Log.i("tag","get item: "+ cursor.getString(cursor.getColumnIndex("item_content")));
                                Log.i("tag","todo_size:"+todo_list.size());
                                Log.i("tag","done_size:"+done_list.size());
                            }
                            todo_adapter.notifyDataSetChanged();
                            done_adapter.notifyDataSetChanged();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        break;

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
                        Log.i("tag","interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    try{
                        SQLiteDatabase database=myapp.mDBOpenHelper.getReadableDatabase();
                        String sql="select * from list_item where list_id='"+list_id+"';";
                        cursor=database.rawQuery(sql,new String[]{});
                        Log.i("tag","sql:"+sql);
                            handler.obtainMessage(123).sendToTarget();
                            Log.i("tag","sendMessage");
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i("tag","查询list_item失败！");
                    }
                }
            }
        });
        thread.start();
    }

}
