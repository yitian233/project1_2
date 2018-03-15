package com.xl.project.project1_0;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2018/1/5.
 */

public class Add_book extends AppCompatActivity {
    private MyApp myapp;
    private String user_id,book_name_string,book_type_string;
    private int book_cover_int;
    private EditText book_name;
    private Button finish;
    private Spinner book_type;
    private RecyclerView book_cover_list;
    private CommonAdapter<Model.Book_cover> mBook_coverCommonAdapter;
    private GridLayoutManager mGridLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.add_book);
        Bundle bundle=getIntent().getExtras();
        user_id=bundle.getString("user_id");

        bindview();
        innit_recyclerview();
    }

    public void bindview(){
        book_name=(EditText)findViewById(R.id.book_name);
        finish=(Button)findViewById(R.id.finish_book);
        book_type=(Spinner)findViewById(R.id.book_type);
        book_cover_list=(RecyclerView)findViewById(R.id.book_cover_list);
        book_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] book_type_option=getResources().getStringArray(R.array.note_type);
                book_type_string=book_type_option[position];
                Log.i(TAG,"book_type_string: "+book_type_string);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                book_name_string=book_name.getEditableText().toString();
                update_to_database();
                finish();
//                Intent intent=new Intent(Add_book.this,MainActivity.class);
//                Bundle bundle=new Bundle();
//                bundle.putString("user_id",user_id);
//                intent.putExtras(bundle);
//                startActivity(intent);
            }
        });
    }

    public void innit_recyclerview(){
        mGridLayoutManager=new GridLayoutManager(this,3);
        book_cover_list.setLayoutManager(mGridLayoutManager);
        mBook_coverCommonAdapter=new CommonAdapter<Model.Book_cover>(this,R.layout.book_cover_item,myapp.mBook_covers_list) {
            @Override
            public void convert(ViewHolder holder, Model.Book_cover cover) {
                LinearLayout book=holder.getView(R.id.book_cover);
                book.setBackgroundResource(cover.getBook_cover());
            }
        };
        mBook_coverCommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                book_cover_int=myapp.mBook_covers_list.get(position).getBook_cover();
                ImageView cover_preview=(ImageView)findViewById(R.id.cover_preview);
                cover_preview.setImageResource(book_cover_int);
            }

            @Override
            public void onLongClick(int position) {

            }
        });
        book_cover_list.setAdapter(mBook_coverCommonAdapter);
    }

    public void update_to_database(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                Log.i(TAG,"update sucessfully");
//                Intent intent=new Intent(Add_book.this,MainActivity.class);
//                Bundle bundle=new Bundle();
//                bundle.putString("msg","add sucessfully");
//                intent.putExtras(bundle);
//                setResult(123,intent);
//                Log.i(TAG,"return to Main");
            }
        };

        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try{
                        Thread.sleep(300);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        Log.i(TAG,"准备  加入note_book");
//                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
//                        Log.i(TAG,"连接数据库成功!");
//                        String sql=String.format("insert into note_book(note_book_userID,note_book_name," +
//                                        "note_book_type,note_book_cover) values(%s,'%s','%s',%d)",
//                                user_id,book_name_string,book_type_string,book_cover_int);
//                        Log.i(TAG,"sql :"+sql);
//                        Statement stmt=conn.createStatement();
//                        int result=stmt.executeUpdate(sql);
//                        Log.i(TAG,"insert note_book sql result: "+result);


                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                            String sql2=String.format("insert into note_book(note_book_userID,note_book_name," +
                                            "note_book_type,note_book_cover) values(%s,'%s','%s',%d)",
                                    user_id,book_name_string,book_type_string,book_cover_int);

                            database.execSQL(sql2);
                        Log.i(TAG,"sql :"+sql2+"  加入note_book");
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

}
