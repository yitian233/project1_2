package com.xl.project.project1_0;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xl on 2018/3/8.
 */

public class DBOpenHelper extends SQLiteOpenHelper{
    private static String name="mydb.db";
    private static int version=2;
    public DBOpenHelper(Context context)
    {
        super(context,name,null,version);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        String sql="create table user(user_id integer primary key AUTOINCREMENT,user_name varchar,user_password varchar);";
        db.execSQL(sql);
        sql="create table note_book(note_book_id integer primary key AUTOINCREMENT," +
                "note_book_userID integer," +
                "note_book_name varchar," +
                "note_book_description varchar," +
                "note_book_title varchar," +
                "note_book_type varchar," +
                "note_book_cover varchar" +
                ");";
        db.execSQL(sql);
        sql="create table note(note_id integer primary key AUTOINCREMENT,note_book_id integer,note_title varchar,note_content varchar," +
                "note_picture varchar,note_file varchar,note_time varchar);";
        db.execSQL(sql);
        sql="create table diary(diary_id integer primary key AUTOINCREMENT,note_book_id integer,diary_title varchar,diary_date varvhar," +
                "diary_day varchar,diary_weather varchar,diary_mood varchar,diary_content varchar,diary_picture1 varchar,diary_picture2 varchar)";
        db.execSQL(sql);
        sql="create table sound(sound_id integer primary key AUTOINCREMENT,sound_book_id integer,sound_file_url varchar,sound_name varchar," +
                "sound_time varchar,sound_size varchar,sound_duration varchar);";
        db.execSQL(sql);
        sql="create table list(list_id integer primary key AUTOINCREMENT,note_book_id integer,list_first_day varchar,list_last_day varchar,year varchar," +
                "week_of_year varchar,all_done varchar);";
        db.execSQL(sql);
        sql="create table list_item(list_item_id integer primary key AUTOINCREMENT,list_id integer,item_content varchar,done varchar);";
        db.execSQL(sql);
        sql="create table book_share(share_id integer primary key AUTOINCREMENT,sharer_id integer,shared_id integer,notebook_id varchar);";
        db.execSQL(sql);
        sql="create table story_picture(story_picture_id integer primary key AUTOINCREMENT,story_picture_title varchar,book_id integer,time varchar,identify varchar," +
                "item1 varchar,item2 varchar,item3 varchar,item4 varchar, item5 varchar,item6 varchar,item7 varchar,item8 varchar,item9 varchar,item10 varchar,cnt varchar);";
        db.execSQL(sql);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db,int oldVersion, int newVersion){
        String sql="alter table user add sex varchar(8);";
        db.execSQL(sql);
    }

    public List<Model.book_share> read_share(String user_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from book_share S,note_book B where S.notebook_id=B.note_book_id and (S.sharer_id=%s or S.shared_id=%s)",user_id,user_id),new String[]{});
        List<Model.book_share> list=new ArrayList<Model.book_share>();
        while(cursor.moveToNext()){
            Model.book_share book_share=new Model.book_share(
                    cursor.getInt(cursor.getColumnIndex("share_id")),
                    cursor.getInt(cursor.getColumnIndex("sharer_id")),
                    cursor.getInt(cursor.getColumnIndex("shared_id")),
                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                    cursor.getString(cursor.getColumnIndex("note_book_description")),
                    cursor.getString(cursor.getColumnIndex("note_book_name")),
                    cursor.getString(cursor.getColumnIndex("note_book_type")),
                    cursor.getInt(cursor.getColumnIndex("note_book_cover"))
            );
            list.add(book_share);
        }
        cursor.close();
        return list;
    }

    public List<Model.Notebook> read_Notebook(String user_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from note_book where note_book_userID=%s",user_id),new String[]{});
        List<Model.Notebook> list=new ArrayList<Model.Notebook>();
        while(cursor.moveToNext()){
            Model.Notebook notebook=new Model.Notebook(
                    cursor.getString(cursor.getColumnIndex("note_book_name")),
                    cursor.getString(cursor.getColumnIndex("note_book_description")),
                    cursor.getString(cursor.getColumnIndex("note_book_id")),
                    cursor.getString(cursor.getColumnIndex("note_book_type")),
                    cursor.getInt(cursor.getColumnIndex("note_book_cover"))
            );
            list.add(notebook);
        }
        cursor.close();
        return list;
    }

    public List<Model.Diary> read_Diary(String note_book_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from diary where note_book_id=%s",note_book_id),new String[]{});
        List<Model.Diary> list=new ArrayList<Model.Diary>();
        while(cursor.moveToNext()){
            Model.Diary diary=new Model.Diary(
                    cursor.getString(cursor.getColumnIndex("diary_id")),
                    cursor.getString(cursor.getColumnIndex("diary_date")),
                    cursor.getString(cursor.getColumnIndex("diary_day")),
                    cursor.getString(cursor.getColumnIndex("diary_content")),
                    cursor.getString(cursor.getColumnIndex("diary_title"))
            );
            list.add(diary);
        }
        cursor.close();
        return list;
    }



    public List<Model.Note> read_Note(String note_book_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from note where note_book_id=%s",note_book_id),new String[]{});
        List<Model.Note> list=new ArrayList<Model.Note>();
        while(cursor.moveToNext()){
            Model.Note note=new Model.Note(
                    cursor.getString(cursor.getColumnIndex("note_title")),
                    cursor.getString(cursor.getColumnIndex("note_time")),
                    cursor.getString(cursor.getColumnIndex("note_id")),
                    cursor.getString(cursor.getColumnIndex("note_content"))
            );
            list.add(note);
        }
        cursor.close();
        return list;
    }

    public List<Model.List> read_List(String note_book_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from list where note_book_id=%s",note_book_id),new String[]{});
        List<Model.List> list=new ArrayList<Model.List>();
        while(cursor.moveToNext()){
            Model.List mlist=new Model.List(
                    cursor.getString(cursor.getColumnIndex("list_id")),
                    cursor.getString(cursor.getColumnIndex("list_first_day")),
                    cursor.getString(cursor.getColumnIndex("list_last_day")),
                    cursor.getString(cursor.getColumnIndex("year")),
                    cursor.getString(cursor.getColumnIndex("week_of_year"))
            );
            list.add(mlist);
        }
        cursor.close();
        return list;
    }

    public List<Model.Sound> read_Sound(String note_book_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from sound where sound_book_id=%s",note_book_id),new String[]{});
        List<Model.Sound> list=new ArrayList<Model.Sound>();
        while(cursor.moveToNext()){
            Model.Sound sound=new Model.Sound(
                    cursor.getString(cursor.getColumnIndex("sound_id")),
                    cursor.getString(cursor.getColumnIndex("sound_name")),
                    cursor.getString(cursor.getColumnIndex("sound_time")),
                    cursor.getString(cursor.getColumnIndex("sound_size"))
            );
            list.add(sound);
        }
        cursor.close();
        return list;
    }

    public List<Model.Story_picture> read_Story_picture(String note_book_id){
        SQLiteDatabase database=getReadableDatabase();
        Cursor cursor=database.rawQuery(String.format("select * from story_picture where book_id=%s",note_book_id),new String[]{});
        List<Model.Story_picture> list=new ArrayList<Model.Story_picture>();
        while(cursor.moveToNext()){
            Model.Story_picture story_picture=new Model.Story_picture(
                    cursor.getString(cursor.getColumnIndex("story_picture_id")),
                    cursor.getString(cursor.getColumnIndex("story_picture_title")),
                    cursor.getString(cursor.getColumnIndex("time"))
            );
            list.add(story_picture);
        }
        cursor.close();
        return list;
    }



    public boolean deleteDatabase(Context context) {
        return context.deleteDatabase(name);
    }
}
