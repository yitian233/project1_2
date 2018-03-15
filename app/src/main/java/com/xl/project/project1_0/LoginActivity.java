package com.xl.project.project1_0;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.ContentValues.TAG;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity  {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
//    private static final int REQUEST_READ_CONTACTS = 0;
//
//    /**
//     * A dummy authentication store containing known user names and passwords.
//     * TODO: remove after connecting to a real authentication system.
//     */
//    private static final String[] DUMMY_CREDENTIALS = new String[]{
//            "foo@example.com:hello", "bar@example.com:world"
//    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;

    // UI references.

    private EditText mUsernameView,mPasswordView,mPasswordView2;
    private View mProgressView;
    private View mLoginFormView;
    private String new_id;
    private ResultSet rs;
    private boolean tag;
    private TextInputLayout confirm_password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (EditText) findViewById(R.id.username);
        //populateAutoComplete();
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView2=(EditText)findViewById(R.id.password_confirm);
        confirm_password=(TextInputLayout)findViewById(R.id.text_input_password_confirm);


        readAccount();
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    return true;
                }
                return false;
            }
        });



        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=mUsernameView.getText().toString();
                String password1=mPasswordView.getText().toString();

                    SharedPreferences sp_old=getSharedPreferences("info",MODE_PRIVATE);
                    SharedPreferences.Editor ed_old = sp_old.edit();//
                    ed_old.clear().commit();

                    SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();//
                    ed.putString("username",username);
                    ed.putString("password",password1);
                    ed.putString("password2",password1);
                    ed.commit();
                    Log.i("tag","提交到本地login");
                    attemptLogin();

                //login_or_register(mUserView.getText().toString(),mPasswordView.getText().toString());
            }
        });
        Button register=(Button)findViewById(R.id.register);
        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String username=mUsernameView.getText().toString();
                String password1=mPasswordView.getText().toString();
                String password2=mPasswordView2.getText().toString();
                if(password1.equals(password2)){
                    SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
                    SharedPreferences.Editor ed = sp.edit();//
                    ed.putString("username",username);
                    ed.putString("password",password1);
                    ed.putString("password2",password2);
                    ed.commit();
                    Log.i("tag","提交到本地");
                    attemptLogin();
                }
                else{
                    Toast.makeText(LoginActivity.this,"密码不一致",Toast.LENGTH_SHORT).show();
                    Log.i("tag","密码不一致");
                }

            }
        });
        Button change=(Button)findViewById(R.id.change);
        change.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tag){
                    confirm_password.setVisibility(View.INVISIBLE);
                    tag=!tag;
                    login.setEnabled(true);
                    register.setEnabled(false);
                    change.setText("切换到注册");
                }
                else{
                    confirm_password.setVisibility(View.VISIBLE);
                    tag=!tag;
                    login.setEnabled(false);
                    register.setEnabled(true);

                    change.setText("切换到登录");
                }

            }
        });


        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

//    private void populateAutoComplete() {
//        if (!mayRequestContacts()) {
//            return;
//        }
//        getLoaderManager().initLoader(0, null, this);
//    }

//    private boolean mayRequestContacts() {
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            return true;
//        }
//        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
//            return true;
//        }
//        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
//            Snackbar.make(mUsernameView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(android.R.string.ok, new OnClickListener() {
//                        @Override
//                        @TargetApi(Build.VERSION_CODES.M)
//                        public void onClick(View v) {
//                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//                        }
//                    });
//        } else {
//            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
//        }
//        return false;
//    }

    /**
     * Callback received when a permissions request has been completed.
     */
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_READ_CONTACTS) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                populateAutoComplete();
//            }
//        }
//    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid userman, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mUsernameView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsernameView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mUsernameView.setError(getString(R.string.error_field_required));
            focusView = mUsernameView;
            cancel = true;
        } else if (!isUsernamValid(username)) {
            mUsernameView.setError(getString(R.string.error_invalid_email));
            focusView = mUsernameView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            login_or_register(username,password);
        }
    }

    private boolean isUsernamValid(String username) {
        //TODO: Replace this with your own logic
        return username.length()>4;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    //@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE +
//                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
//                .CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
//    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        List<String> emails = new ArrayList<>();
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS));
//            cursor.moveToNext();
//        }
//
//        addEmailsToAutoComplete(emails);
//    }

//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//
//    }
//
//    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(LoginActivity.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mUsernameView.setAdapter(adapter);
//    }
//
//
//    private interface ProfileQuery {
//        String[] PROJECTION = {
//                ContactsContract.CommonDataKinds.Email.ADDRESS,
//                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
//        };
//
//        int ADDRESS = 0;
//        int IS_PRIMARY = 1;
//    }



    public void login_or_register(final String username, final String password){

        final Thread thread2=new Thread(new Runnable() {
            @Override
            public void run() {
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
                        Log.i(TAG,"register连接数据库成功!");
                        String fmt="insert into user(user_name,user_password) values('%s','%s');";
                        String sql = String.format(fmt,username,password);
                        Statement stmt=conn.createStatement();
                        int cnt=stmt.executeUpdate(sql);
                        if(cnt!=0)
                            Log.i("tag","register successfully!");
                        else Log.i("tag","register failded!");
                        fmt="select * from user where user_name='%s';";
                        sql = String.format(fmt,username);
                        ResultSet rs=stmt.executeQuery(sql);
                        while(rs.next()){
                            new_id=rs.getString("user_id");
                            Log.i("tag","new_id:"+new_id);
                        }
                        return;
                    }catch(SQLException e){
                        e.printStackTrace();
                        Log.i(TAG,"register远程连接失败！");
                    }
                }

        });

        @SuppressLint("HandlerLeak")
        final Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                switch(msg.what){
                    case 123:
                        try {
                            boolean flag=true;
                            while(rs.next()){
                                if(rs.getString("user_name").equals(username)){
                                    flag=false;
                                    if(rs.getString("user_password").equals(password)){
                                        Toast.makeText(LoginActivity.this,"Login successfully",Toast.LENGTH_LONG).show();
                                        Log.i("tag","Login successfully "+rs.getString("user_name"));
                                        Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                        Bundle bundle=new Bundle();
                                        bundle.putString("user_id",rs.getString("user_id"));
                                        intent.putExtras(bundle);
                                        SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
                                        SharedPreferences.Editor ed = sp.edit();//
                                        ed.putString("user_id",rs.getString("user_id"));
                                        ed.commit();
                                        Log.i("tag","save user_id to sp: "+rs.getString("user_id"));
                                        startActivity(intent);
                                        finish();
                                    }
                                    else{
                                        Toast.makeText(LoginActivity.this,"Password is incorrect!",Toast.LENGTH_LONG).show();
                                        showProgress(false);
                                        mPasswordView.requestFocus();
                                    }
                                }
                            }
                            if(flag){
                                thread2.start();
                                while(thread2.isAlive()){}
                                Toast.makeText(LoginActivity.this,"Register successfully",Toast.LENGTH_LONG).show();
                                Intent intent=new Intent(LoginActivity.this,MainActivity.class);
                                Bundle bundle=new Bundle();
                                bundle.putString("user_id",new_id);
                                SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
                                SharedPreferences.Editor ed = sp.edit();//
                                ed.putString("user_id",new_id);
                                ed.commit();
                                Log.i("tag","save user_id to sp: "+new_id);
                                intent.putExtras(bundle);
                                startActivity(intent);
                                finish();
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        };
        final Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {

                    try{
                        Thread.sleep(1000);
                        Class.forName("com.mysql.jdbc.Driver");
                    }catch (InterruptedException e){
                        Log.i("t2","interrupt! ");
                    }
                    catch (ClassNotFoundException e){
                        e.printStackTrace();
                    }
                    String ip="119.29.229.194";
                    int port=3306;
                    String dbName="android_xl";
//                    String connectString = "jdbc:mysql://172.18.187.233:53306/teaching"
//                            +"?autoReconnect=true&useUnicode=true"
//                            +"&characterEncoding=UTF-8";
                    String connectString = "jdbc:mysql://119.29.229.194:53306/android_15352399"
                            +"?autoReconnect=true&useUnicode=true"
                            +"&characterEncoding=UTF-8";
                    try{
                        Connection conn= (Connection) DriverManager.getConnection(connectString,"root","android123");
                        Log.i(TAG,"login连接数据库成功!");
                        String sql="select * from user;";
                        Statement stmt=conn.createStatement();
                        rs=stmt.executeQuery(sql);
                        handler.obtainMessage(123).sendToTarget();
//                        rs.close();
//                        stmt.close();
//                        conn.close();
                        return;
                    }catch(SQLException e){
                        e.printStackTrace();
                        Log.i("t2","login远程连接失败！");
                    }
                }

        });
        thread.start();



    }
    public void readAccount(){
        SharedPreferences sp=getSharedPreferences("info",MODE_PRIVATE);
        String usernames=sp.getString("username","");
        String password=sp.getString("password","");
        String password2=sp.getString("password2","");
        String user_id=sp.getString("user_id","");
        mUsernameView.setText(usernames);
        mPasswordView.setText(password);
        mPasswordView2.setText(password2);
        Log.i("tag","readaccount:  username:"+usernames+"  password1:"
                +password+"  password2:"+password2+" user_id:"+user_id);
        if(!"".equals(password2)){
            confirm_password.setVisibility(View.INVISIBLE);
            tag=false;
            Intent intent=new Intent(LoginActivity.this,MainActivity.class);
            Bundle bundle=new Bundle();
            bundle.putString("user_id",user_id);
            Log.i("t2","自动登录,user_id:"+user_id);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }else{
            confirm_password.setVisibility(View.VISIBLE);
            tag=true;
        }
    }
}

