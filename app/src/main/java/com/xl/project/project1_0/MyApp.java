package com.xl.project.project1_0;

import android.app.Application;
import android.graphics.Typeface;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by xl on 2017/12/31.
 */

public class MyApp  extends Application{
    public List<Model.Notebook> mNotebookList=new ArrayList<Model.Notebook>();
    public List<Model.Note> mNote_List=new ArrayList<Model.Note>();
    public List<Model.Diary> mDiaryList=new ArrayList<Model.Diary>();
    public List<Model.Book_cover> mBook_covers_list=new ArrayList<Model.Book_cover>();
    public List<Model.Sound> mSoundList=new ArrayList<Model.Sound>();
    public List<Model.List> mList_List=new ArrayList<Model.List>();
    public List<Model.Story_picture> mStoryPictureList=new ArrayList<Model.Story_picture>();
    public DBOpenHelper mDBOpenHelper=new DBOpenHelper(this);
    public String note_content_string="";
    public static Typeface typeface;
    @Override
    public void onCreate(){
        super.onCreate();
        typeface=Typeface.createFromAsset(getAssets(),"fonts/fangzhengkatongjianti.ttf");
        try
        {
            Field field = Typeface.class.getDeclaredField("SERIF");
            field.setAccessible(true);
            field.set(null, typeface);
        }
        catch (NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
    }

    public void Innit_notebook_list(){

        for(int i=0;i<10;i++){
            Model.Notebook temp=new Model.Notebook("我的日记本","日记",i+"0","note",R.mipmap.cover1);
            mNotebookList.add(temp);
            Log.i(TAG,"cover1: "+R.mipmap.cover1);
        }
        Model.Notebook temp=new Model.Notebook("我的日记本","日记2",11+"0","note",R.mipmap.cover1);
        mNotebookList.add(temp);
    }
    public void Innit_note_list(){
        for(int i=0;i<10;i++){
            Model.Note temp=new Model.Note("今年最后一天","2017.12.31",i+"0","content!");
            mNote_List.add(temp);
        }
    }
    public void Innit_diary_list(){
        for(int i=0;i<12;i++){
            Model.Diary temp=new Model.Diary("test","test","test","test","test");
            mDiaryList.add(temp);
        }
    }
    public void Innit_book_cover_list(){
        mBook_covers_list.clear();
        int[] covers=new int[]{R.mipmap.cover11,R.mipmap.cover14,R.mipmap.cover18,R.mipmap.cover12,R.mipmap.cover10,R.mipmap.cover5,
                R.mipmap.cover16,R.mipmap.cover19,R.mipmap.cover20};
        for(int i=0;i<covers.length;i++){
            Model.Book_cover temp=new Model.Book_cover(covers[i]);
            mBook_covers_list.add(temp);
            Log.i("tag","cover:"+covers[i]);
        }
    }

    public void Innit_sound_list(){
        for(int i=0;i<5;i++){
            Model.Sound temp=new Model.Sound("1","sound","2018-1-6","30kb");
            mSoundList.add(temp);
        }
    }

}
