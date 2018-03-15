package com.xl.project.project1_0;

/**
 * Created by xl on 2017/12/31.
 */

public class Model {
    public static class Notebook{
        private String notebook_name;
        private String notebook_description;
        private String notebook_id;
        private String notebook_type;
        private int notebook_cover;
        Notebook(String notebook_name,String notebook_description,String notebook_id,String notebook_type,int notebook_cover ){
            this.notebook_name=notebook_name;
            this.notebook_description=notebook_description;
            this.notebook_id=notebook_id;
            this.notebook_type=notebook_type;
            this.notebook_cover=notebook_cover;
        }

        public int getNotebook_cover() {
            return notebook_cover;
        }

        public String getNotebook_id() {
            return notebook_id;
        }

        public String getNotebook_description() {
            return notebook_description;
        }

        public String getNotebook_name() {
            return notebook_name;
        }

        public String getNotebook_type() {
            return notebook_type;
        }
    }
    public static class Note{
        private String note_title;
        private String note_time;
        private String id;
        private String note_content;
        public Note(String title,String time,String id,String content){
            this.note_title=title;
            this.note_time=time;
            this.id=id;
            this.note_content=content;
        }

        public String getNote_time() {
            return note_time;
        }
        public String getNote_title() {
            return note_title;
        }
        public String getNote_content(){return note_content;}
        public String getId() {
            return id;
        }
    }
    public static class Diary{
        private String diary_id;
        private String diary_date;
        private String diary_day;
        private String diary_content;
        private String diary_title;
        public Diary(String id,String diary_date,String diary_day,String diary_content,String diary_title ){
            this.diary_id=id;
            this.diary_date=diary_date;
            this.diary_day=diary_day;
            this.diary_content=diary_content;
            this.diary_title=diary_title;
        }

        public String getDiary_content() {
            return diary_content;
        }

        public String getDiary_date() {
            return diary_date;
        }

        public String getDiary_day() {
            return diary_day;
        }

        public String getDiary_id() {
            return diary_id;
        }

        public String getDiary_title() {
            return diary_title;
        }
    }

    public static class Book_cover{
        int book_cover;
        public  Book_cover(int book_cover){
            this.book_cover=book_cover;
        }

        public int getBook_cover() {
            return book_cover;
        }
    }

    public static class List{
        private String list_id;
        private String first_day;
        private String last_day;
        private String week_num;
        private String year;
        public List(String list_id,String first_day,String last_day,String week_num,String year){
            this.list_id=list_id;
            this.first_day=first_day;
            this.last_day=last_day;
            this.week_num=week_num;
            this.year=year;
        }
        public String getList_id(){return list_id;}
        public String getFirst_day(){return first_day;}
        public String getLast_day(){return last_day;}
        public String getWeek_num(){return week_num;}
        public String getYear(){return year;}
    }

    public static class Sound{
        String sound_id;
        String sound_name;
        String sound_time;
        String sound_size;
        public Sound(String sound_id,String sound_name,String sound_time,String sound_size){
            this.sound_id=sound_id;
            this.sound_name=sound_name;
            this.sound_time=sound_time;
            this.sound_size=sound_size;
        }

        public String getSound_name() {
            return sound_name;
        }

        public String getSound_time() {
            return sound_time;
        }

        public String getSound_size() {
            return sound_size;
        }

        public String getSound_id() {
            return sound_id;
        }
    }


    public static  class Story_picture{
        private String Story_picture_id;
        private String Story_picture_title;
        private String Story_picture_time;
        public Story_picture(String id,String title,String time){
            this.Story_picture_id=id;
            this.Story_picture_title=title;
            this.Story_picture_time=time;
        }

        public String getStory_picture_id() {
            return Story_picture_id;
        }

        public String getStory_picture_time() {
            return Story_picture_time;
        }

        public String getStory_picture_title() {
            return Story_picture_title;
        }
    }
    public static class book_share{
        private int share_id;
        private int sharer_id;
        private int shared_id;
        private String notebook_id;
        private String notebook_description;
        private String notebook_title;
        private String notebook_name;
        private String notebook_type;
        private int notebook_color;

        public book_share(int share_id,int sharer_id,int shared_id,String notebook_id,String notebook_description,String notebook_name,String notebook_type,int notebook_color){
            this.share_id=share_id;
            this.sharer_id=sharer_id;
            this.shared_id=shared_id;
            this.notebook_id=notebook_id;
            this.notebook_description=notebook_description;
            this.notebook_name=notebook_name;
            this.notebook_type=notebook_type;
            this.notebook_color=notebook_color;
        }

        public int getShare_id() {
            return share_id;
        }

        public int getSharer_id() {
            return sharer_id;
        }

        public int getShared_id() {
            return shared_id;
        }

        public String getNotebook_id() {
            return notebook_id;
        }

        public int getNotebook_color(){return notebook_color;}
        public String getNotebook_description(){return notebook_description;}
        public String getNotebook_name(){return notebook_name;}
        public String getNotebook_type(){return notebook_type;}
    }


}
