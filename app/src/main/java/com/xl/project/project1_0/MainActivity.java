package com.xl.project.project1_0;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteAbortException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.xl.project.project1_0.DBOpenHelper;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.sql.Types.NULL;

public class MainActivity extends AppCompatActivity {
    private MyApp myapp;
    private GridLayoutManager notebook_list_layoutManager;
    private RecyclerView notebook_recyclerview;
    private CommonAdapter<Model.Notebook> mNotebookCommonAdapter;
    private String user_id;
    private Handler mHandler;
    private Thread mThread;
    private String s;
    private ResultSet rs;
    private Model.Notebook temp;
    private String shared_id_string;
    private final int share_or_delete=124,selet_book=123;
    private ImageView private_book,shared_book;
    private FloatingActionButton add_book;
    private ImageView logout;
    private ProgressBar progressBar;

    private Button upload;

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data){
//        super.onActivityResult(requestCode,resultCode,data);
//        if(requestCode==123){
//            Log.i(TAG,"Resume MainActivity");
//            select_private_book();
//            Log.i(TAG,"select_private_book_again");
//        }
//
//    }
    @Override
    protected void onResume(){
        super.onResume();
        select_private_book();
        Log.i(TAG,"onResume");
    }
    @Override
    protected void onDestroy(){

        update_to_server();
        Log.i(TAG,"update to server  then destroy");
        super.onDestroy();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myapp= (MyApp) getApplication();
        setContentView(R.layout.activity_main);
        user_id=getIntent().getExtras().getString("user_id");
        myapp.mDBOpenHelper.deleteDatabase(this);
        myapp.mDBOpenHelper = new DBOpenHelper(this);
        Log.i("tag","user_id:"+user_id);
        bindview();
        //progressBar.setVisibility(View.VISIBLE);
        innit_recycler();
        update_from_server();
        select_private_book();
        //progressBar.setVisibility(View.INVISIBLE);
    }

    public void bindview(){
        progressBar=(ProgressBar)findViewById(R.id.progress_bar);
        private_book=(ImageView) findViewById(R.id.private_book);
        private_book.setVisibility(View.VISIBLE);
        shared_book=(ImageView) findViewById(R.id.shared_book);
        shared_book.setVisibility(View.VISIBLE);
        select_private_book();
        private_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_private_book();
                private_book.setImageResource(R.mipmap.private_btn);
                shared_book.setImageResource(R.mipmap.public_btn1);
            }
        });
        shared_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select_shared_book();
                shared_book.setImageResource(R.mipmap.public_btn);
                private_book.setImageResource(R.mipmap.private_btn1);
            }
        });
        add_book=(FloatingActionButton)findViewById(R.id.add_item);
        add_book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,Add_book.class);
                Bundle bundle=new Bundle();
                bundle.putString("user_id",user_id);
                intent.putExtras(bundle);
                startActivityForResult(intent,123);
            }
        });

        logout=(ImageView) findViewById(R.id.logout);
        logout.setVisibility(View.VISIBLE);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
                SharedPreferences.Editor ed = sp.edit();
                ed.clear().commit();
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                Bundle bundle=new Bundle();
                bundle.putString("flag","true");
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        SearchView search=(SearchView)findViewById(R.id.search);
        search.setSubmitButtonEnabled(true);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if(s.length()!=0){
                    Log.i(TAG,"query: "+s);
                    for(int i=0;i<myapp.mNotebookList.size();i++){
                        Log.i(TAG,"notebook name: "+myapp.mNotebookList.get(i).getNotebook_name());
                        if(myapp.mNotebookList.get(i).getNotebook_name().equals(s)){
                            Log.i(TAG,"query 2 : "+s);
                            String book_type=myapp.mNotebookList.get(i).getNotebook_type();
                            Intent To_book_intent = null;
                            if(book_type.equals("note")){
                                To_book_intent=new Intent(MainActivity.this,Note_book.class);
                            }else if(book_type.equals("diary")){
                                To_book_intent=new Intent(MainActivity.this,Diary_book.class);
                            } else if(book_type.equals("sound")){
                                To_book_intent=new Intent(MainActivity.this,Sound_book.class);
                            }else if(book_type.equals("list")){
                                To_book_intent=new Intent(MainActivity.this,List_book.class);
                            }else if(book_type.equals("story_picture")){
                                To_book_intent=new Intent(MainActivity.this,Story_picture_book.class);
                            }

                            Bundle bundle=new Bundle();
                            bundle.putString("id",myapp.mNotebookList.get(i).getNotebook_id());
                            To_book_intent.putExtras(bundle);
                            Log.i("tag","id:"+myapp.mNotebookList.get(i).getNotebook_id());
                            startActivity(To_book_intent);
                        }
                    }
                }
                else
                {
                    Toast.makeText(getApplication(),"输入不能为空",Toast.LENGTH_SHORT).show();
                }
                return  true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }
    public void select_private_book(){
        myapp.mNotebookList.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            for(int i=0;i<myapp.mNotebookList.size();i++)
                                Log.i(TAG,"read notebook "+myapp.mNotebookList.get(i).getNotebook_id());
                            mNotebookCommonAdapter.notifyDataSetChanged();
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
//                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
//                            +"?autoReconnect=true&useUnicode=true"
//                            +"&characterEncoding=UTF-8";
                    try{
//                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
//                        Log.i(TAG,"连接数据库成功!");
//                        String sql="select * from note_book where note_book_userID='"+user_id+"';";
//                        Statement stmt=conn.createStatement();
//                        Log.i(TAG,"sql:"+sql);
//                        rs=stmt.executeQuery(sql);
//                        handler.obtainMessage(123).sendToTarget();
//                        return;
                          Log.i(TAG,"赋值mNotebookList");
                          List<Model.Notebook> list=myapp.mDBOpenHelper.read_Notebook(user_id);
                          for(int i=0;i<list.size();i++){
                              myapp.mNotebookList.add(list.get(i));
                          }

                          handler.obtainMessage(123).sendToTarget();
                          return;

                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"连接SQLite失败");
                    }
                }
            }
        });
        thread.start();
    }

    public void select_shared_book(){
        myapp.mNotebookList.clear();
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                           mNotebookCommonAdapter.notifyDataSetChanged();
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
                        List<Model.book_share> temp=myapp.mDBOpenHelper.read_share(user_id);
                        for(int i=0;i<temp.size();i++){
                            Model.book_share b=temp.get(i);
                            Model.Notebook k=new Model.Notebook(b.getNotebook_name(),b.getNotebook_description(),b.getNotebook_id(),b.getNotebook_type(),b.getNotebook_color());
                                myapp.mNotebookList.add(k);
                            }
                        handler.obtainMessage(123).sendToTarget();
                        return;
                    }catch(Exception e){
                        e.printStackTrace();
                        Log.i(TAG,"加载share失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void share_or_delete(int option,String notebook){
        progressBar.setVisibility(View.VISIBLE);
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case share_or_delete:
                }
                progressBar.setVisibility(View.INVISIBLE);
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

                    try{
                        String sql;
                        if(option==1){
                            sql=String.format("delete from note_book where note_book_id=%s",notebook);
                        }else{
                            sql=String.format("insert into book_share(sharer_id,shared_id,notebook_id) values(%s,%s,%s)",user_id,shared_id_string,notebook);
                        }
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        database.execSQL(sql);
                        handler.obtainMessage(share_or_delete).sendToTarget();
                        return;
                    }catch(SQLiteAbortException e){
                        e.printStackTrace();
                        Log.i(TAG,"share or delete 失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void innit_recycler(){
//        myapp.Innit_notebook_list();
//        myapp.Innit_note_list();
//        myapp.Innit_diary_list();
          myapp.Innit_book_cover_list();
//        myapp.Innit_sound_list();
        for(int i=0;i<myapp.mNotebookList.size();i++){
            Log.i(TAG,"init : "+myapp.mNotebookList.get(i).getNotebook_name());
        }
        notebook_recyclerview=(RecyclerView)findViewById(R.id.recyleView_notebook);
        notebook_list_layoutManager=new GridLayoutManager(this,2);
        notebook_list_layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        notebook_recyclerview.setLayoutManager(notebook_list_layoutManager);
        mNotebookCommonAdapter=new CommonAdapter<Model.Notebook>(this,R.layout.item_notebook,myapp.mNotebookList) {
            @Override
            public void convert(ViewHolder holder, Model.Notebook notebook) {
                TextView notebook_name=holder.getView(R.id.notebook_name);
                notebook_name.setText(notebook.getNotebook_name());
                TextView notebook_type=holder.getView(R.id.notebook_type);
                notebook_type.setText(notebook.getNotebook_type());
                LinearLayout book=(holder.getView(R.id.book));
                book.setBackgroundResource(notebook.getNotebook_cover());
            }
        };
        mNotebookCommonAdapter.setOnItemClickListener(new CommonAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                String book_type=myapp.mNotebookList.get(position).getNotebook_type();
                Intent To_book_intent = null;
                if(book_type.equals("note")){
                    To_book_intent=new Intent(MainActivity.this,Note_book.class);
                }else if(book_type.equals("diary")){
                    To_book_intent=new Intent(MainActivity.this,Diary_book.class);
                } else if(book_type.equals("sound")){
                    To_book_intent=new Intent(MainActivity.this,Sound_book.class);
                }else if(book_type.equals("list")){
                    To_book_intent=new Intent(MainActivity.this,List_book.class);
                }else if(book_type.equals("story_picture")){
                    To_book_intent=new Intent(MainActivity.this,Story_picture_book.class);
                }

                Bundle bundle=new Bundle();
                bundle.putString("id",myapp.mNotebookList.get(position).getNotebook_id());
                To_book_intent.putExtras(bundle);
                startActivityForResult(To_book_intent,1);
            }

            @Override
            public void onLongClick(int position) {
                String notebook=myapp.mNotebookList.get(position).getNotebook_id();

                LayoutInflater factor=LayoutInflater.from(MainActivity.this);
                View layout=factor.inflate(R.layout.share,null);
                AlertDialog builder=new AlertDialog.Builder(MainActivity.this).create();
                builder.setView(layout);

                EditText shared_id=(EditText)layout.findViewById(R.id.shared_id);
                ImageView cancel =(ImageView)layout.findViewById(R.id.cancel_share);
                Button delete=(Button)layout.findViewById(R.id.delete_book);
                Button ok_share=(Button)layout.findViewById(R.id.ok_share);
                TextView user=(TextView)layout.findViewById(R.id.user_id);
                user.setText("您的ID是："+user_id);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        builder.dismiss();
                    }
                });

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        share_or_delete(1,notebook);
                        builder.dismiss();
                    }
                });

                ok_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shared_id_string=shared_id.getEditableText().toString();
                        share_or_delete(2,notebook);
                        builder.dismiss();
                    }
                });

                builder.show();
            }
        });
        notebook_recyclerview.setAdapter(mNotebookCommonAdapter);
    }
    public void update_from_server(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                }
            }
        };

        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try{
                        Thread.sleep(1000);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i(TAG,"interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    String ip="119.29.229.194";
                    int port=3306;
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
                        Log.i(TAG,"连接数据库成功!");
                        String sql="select * from book_share;";
                        Statement stmt=conn.createStatement();
                        rs=stmt.executeQuery(sql);


                        handler.obtainMessage(123).sendToTarget();


                        //
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        database.execSQL("delete from  book_share;");
                        while(rs.next()){
                            Log.i(TAG,"share_id: "+rs.getString("share_id"));
                            String sql2="insert into book_share values("+rs.getString("share_id")+","+rs.getString("sharer_id")
                                    +","+rs.getString("shared_id")+","+rs.getString("notebook_id")+")";
                            database.execSQL(sql2);
                        }
                        List<Model.book_share> list=myapp.mDBOpenHelper.read_share(user_id);
                        for(int i=0;i<list.size();i++){
                            Log.i(TAG,"read from SQLITE: "+list.get(i).getNotebook_id());

                        }
                        sql="select * from note_book where note_book_userID="+user_id+";";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from note_book;");
                        while(rs.next()){
                            Log.i(TAG,"note_book_id: "+rs.getString("note_book_id"));
                            String sql2="insert into note_book values("+rs.getString("note_book_id")+","+rs.getString("note_book_userID")
                                    +",'"+rs.getString("note_book_name")+"','"+rs.getString("note_book_description")
                                    +"','"+rs.getString("note_book_title")+"','"+rs.getString("note_book_type")+"',"+rs.getString("note_book_cover")+")";
                            database.execSQL(sql2);
                        }
                        myapp.mNotebookList.clear();
                        List<Model.Notebook> temp=myapp.mDBOpenHelper.read_Notebook(user_id);
                        for(int i=0;i<temp.size();i++){
                            myapp.mNotebookList.add(temp.get(i));
                        }
                        //
                        sql="select * from diary;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from diary;");
                        while(rs.next()){
                            Log.i(TAG,"diary: "+rs.getString("diary_id"));
                            String sql2="insert into diary values("+rs.getString("diary_id")+","+rs.getString("note_book_id")
                                    +",'"+rs.getString("diary_title")+"','"+rs.getString("diary_date")
                                    +"','"+rs.getString("diary_day")+"','"+rs.getString("diary_weather")
                                    +"','"+"','"+rs.getString("diary_mood")+rs.getString("diary_content")
                                    +"','"+rs.getString("diary_picture1")+"','"+rs.getString("diary_picture2")+"')";
                            database.execSQL(sql2);
                        }

                        //
                        //
                        sql="select * from note;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from note;");
                        while(rs.next()){
                            Log.i(TAG,"note: "+rs.getString("note_id"));
                            String sql2="insert into note values("+rs.getString("note_id")+","+rs.getString("note_book_id")
                                    +",'"+rs.getString("note_title")+"','"+rs.getString("note_content")
                                    +"','"+rs.getString("note_picture")+"','"+rs.getString("note_file")
                                    +"','"+rs.getString("note_time")+"')";
                            database.execSQL(sql2);
                        }

                        //
                        sql="select * from list;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from list;");
                        while(rs.next()){
                            Log.i(TAG,"list: "+rs.getString("list_id"));
                            String sql2="insert into list values("+rs.getString("list_id")+","+rs.getString("note_book_id")
                                    +",'"+rs.getString("list_first_day")+"','"+rs.getString("list_last_day")
                                    +"','"+rs.getString("year")+"','"+rs.getString("week_of_year")
                                    +"','"+rs.getString("all_done")+"')";
                            database.execSQL(sql2);
                        }

                        sql="select * from sound;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from sound;");
                        while(rs.next()){
                            Log.i(TAG,"sound: "+rs.getString("sound_id"));
                            String sql2="insert into sound values("+rs.getString("sound_id")+","+rs.getString("sound_book_id")
                                    +",'"+rs.getString("sound_file_url")+"','"+rs.getString("sound_name")
                                    +"','"+rs.getString("sound_time")+"','"+rs.getString("sound_size")
                                    +"','"+rs.getString("sound_duration")+"')";
                            database.execSQL(sql2);
                        }

                        sql="select * from story_picture;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from story_picture;");
                        while(rs.next()){
                            Log.i(TAG,"story_picture: "+rs.getString("story_picture_id"));
                            String sql2="insert into story_picture values("+rs.getString("story_picture_id")+",'"+rs.getString("story_picture_title")
                                    +"','"+rs.getString("book_id")+"','"+rs.getString("time")
                                    +"','"+rs.getString("identify")+"','"+rs.getString("item1")
                                    +"','"+rs.getString("item2")+"','"+rs.getString("item3")
                                    +"','"+rs.getString("item4")+"','"+rs.getString("item5")
                                    +"','"+rs.getString("item6")+"','"+rs.getString("item7")
                                    +"','"+rs.getString("item8")+"','"+rs.getString("item9")
                                    +"','"+rs.getString("item10")+"','"+rs.getString("cnt")+"')";
                            database.execSQL(sql2);
                        }
                        //
                        sql="select * from list_item;";
                        rs=stmt.executeQuery(sql);
                        database.execSQL("delete from list_item;");
                        while(rs.next()){
                            Log.i(TAG,"sound: "+rs.getString("list_item_id"));
                            String sql2="insert into list_item values("+rs.getString("list_item_id")+","+rs.getString("list_id")
                                    +",'"+rs.getString("item_content")+"','"+rs.getString("done")+"')";
                            database.execSQL(sql2);
                        }

                        rs.close();
                        stmt.close();
                        conn.close();
                        return;
                    }catch(SQLException e){
                        e.printStackTrace();
                        Log.i(TAG,"远程连接失败！");
                    }
                }
            }
        });
        thread.start();
    }

    public void update_to_server(){
        final Handler handler=new android.os.Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
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
                    String ip="119.29.229.194";
                    int port=3306;
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
                        Log.i(TAG,"连接数据库成功!");
                        Statement stmt=conn.createStatement();
                        String sql;
                        sql = "delete from book_share where sharer_id="+user_id+" or shared_id="+user_id;
                        stmt.executeUpdate(sql);
                        //
                        SQLiteDatabase database=myapp.mDBOpenHelper.getWritableDatabase();
                        List<Model.book_share> list=myapp.mDBOpenHelper.read_share(user_id);

                        for(int i=0;i<list.size();i++){
                            Log.i(TAG,"read from SQLITE: "+list.get(i).getNotebook_id());
                            sql="insert into book_share values("+list.get(i).getShare_id()+","+list.get(i).getSharer_id()+","
                                    +list.get(i).getShared_id()+","+list.get(i).getNotebook_id()+")";
                            stmt.executeUpdate(sql);
                        }

                        //
                        sql="delete from note_book where note_book_userID="+user_id;
                        stmt.executeUpdate(sql);
                        sql="select * from note_book;";
                        database=myapp.mDBOpenHelper.getReadableDatabase();
                        Cursor cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into note_book values(%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                                    cursor.getString(cursor.getColumnIndex("note_book_userID")),
                                    "'"+cursor.getString(cursor.getColumnIndex("note_book_name"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_book_description"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_book_title"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_book_type"))+"'",
                                    cursor.getString(cursor.getColumnIndex("note_book_cover"))
                            );
                            Log.i(TAG,"insert_sql: "+sql);
                            stmt.executeUpdate(sql);
                        }
                        //
                        sql="delete from note;";
                        stmt.executeUpdate(sql);
                        sql="select * from note";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into note values(%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("note_id")),
                                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("note_title"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_content"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_picture"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_file"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("note_time"))+"'"
                                    );
                            stmt.executeUpdate(sql);
                        }
                        //
                        sql="delete from diary;";
                        stmt.executeUpdate(sql);
                        sql="select * from diary";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into diary values(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("diary_id")),
                                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_title"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_date"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_day"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_weather"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_mood"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_content"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_picture1"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("diary_picture2"))+"'"
                            );
                            stmt.executeUpdate(sql);
                        }
                        //
                        sql="delete from list;";
                        stmt.executeUpdate(sql);
                        sql="select * from list";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into list values(%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                                    cursor.getString(cursor.getColumnIndex("list_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("list_first_day"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("list_last_day"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("year"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("week_of_year"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("all_done"))+"'"
                            );
                            stmt.executeUpdate(sql);
                        }
                        //
                        sql="delete from list_item;";
                        stmt.executeUpdate(sql);
                        sql="select * from list_item";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into list_item values(%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("list_item_id")),
                                    cursor.getString(cursor.getColumnIndex("list_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("list_content"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("done"))+"'"
                            );
                            stmt.executeUpdate(sql);
                        }
                        //
                        sql="delete from sound;";
                        stmt.executeUpdate(sql);
                        sql="select * from sound";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into sound values(%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("sound_id")),
                                    cursor.getString(cursor.getColumnIndex("sound_book_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("sound_file_url"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("sound_name"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("sound_time"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("sound_size"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("sound_duration"))+"'"
                            );
                            stmt.executeUpdate(sql);
                        }

                        //
                        sql="delete from story_picture;";
                        stmt.executeUpdate(sql);
                        sql="select * from story_picture";
                        cursor=database.rawQuery(sql,new String[]{});
                        while(cursor.moveToNext()){
                            sql=String.format("insert into story_picture values(%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s)",
                                    cursor.getString(cursor.getColumnIndex("story_picture_id")),
                                    "'"+cursor.getString(cursor.getColumnIndex("story_picture_title"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("book_id"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("time"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("identify"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item1"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item2"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item3"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item4"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item5"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item6"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item7"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item8"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item9"))+"'",
                                    "'"+cursor.getString(cursor.getColumnIndex("item10"))+"'",
                                    cursor.getString(cursor.getColumnIndex("cnt"))
                            );
                            stmt.executeUpdate(sql);
                        }

                        handler.obtainMessage(123).sendToTarget();
                        database.close();
                        rs.close();
                        stmt.close();
                        conn.close();
                        return;
                    }catch(SQLException e){
                        e.printStackTrace();
                        Log.i(TAG,"保存到服务器数据库失败！");
                    }
                }
            }
        });
        thread.start();
    }
}
