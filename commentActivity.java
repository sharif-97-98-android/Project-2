package com.example.myapplication;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class commentActivity extends AppCompatActivity {
    String id;
    String sign;
    String sign2;
    SQLiteDatabase commentDatabase;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comment_layout);
        commentDatabase = ((SubApp)getApplication()).commentDatabase;
        Intent intent = getIntent();
        id = intent.getStringExtra("key");
        sign =  intent.getStringExtra("serverordb");
        int s = Integer.parseInt(sign);
        if(s == 0){
            reqtoserver(id);
        }
        else if(s == 1){
            reqtodb(id);
        }

    }
    public void reqtodb(String id){
            Cursor result = (Cursor) commentDatabase.rawQuery("Select * from COMMENT where postid = '" + id + "'", null);
            result.moveToFirst();
            ArrayList<comment> comments = new ArrayList<>();
            int postid_index = result.getColumnIndex("postid");
            int id_index = result.getColumnIndex("id");
            int name_index = result.getColumnIndex("name");
            int email_index = result.getColumnIndex("email");
            int body_index = result.getColumnIndex("body");
            do {
                comments.add(new comment(result.getString(postid_index), result.getString(id_index),
                        result.getString(name_index), result.getString(email_index), result.getString(body_index)));
            } while (result.moveToNext());
            createcommentsfromdb(comments);
        }


    public void reqtoserver(String id){

            String URL = "https://jsonplaceholder.typicode.com/comments?postId=" + id;
            RequestQueue queue = Volley.newRequestQueue(this);
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {

                        createcommentsfromserver(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                            error.printStackTrace();
                        }
                    });
            queue.add(jsonArrayRequest);
    }
    public void dialog(View v) {
        final Context c = this;
        new dialog(c).run();
    }
    public void createcommentsfromdb(ArrayList <comment> comments){
        Log.d("comments", "comments restored from database");
        int length = comments.size();
        TextView textView = (TextView) findViewById(R.id.postinfo);
        textView.setText("post" + id + "," + length + "comments");
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for(int i = 0; i < length; ++i){
            HashMap<String, String> item = new HashMap<String,String>();
            item.put("line1", "postId: " + comments.get(i).postId);
            item.put("line2", "id: " + comments.get(i).id);
            item.put("line3", "name: " +comments.get(i).name);
            item.put("line4", "email: " +comments.get(i).email);
            item.put("line5", "body: " +comments.get(i).body);
            list.add(item);
        }
        SimpleAdapter sa = new SimpleAdapter(this, list, R.layout.comment_list, new String[]{"line1", "line2",
                "line3", "line4", "line5"}, new int[]{R.id.postId, R.id.id, R.id.name, R.id.email, R.id.body});
        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setAdapter(sa);
    }
    public void createcommentsfromserver(JSONArray jsonArray) throws JSONException {
        Log.d("comments", "comments restored from server");
        int length = jsonArray.length();
        TextView textView = (TextView) findViewById(R.id.postinfo);
        textView.setText("post" + id + "," + length + "comments");
        ArrayList<comment> comments = new ArrayList<>();
        Cursor cc = commentDatabase.rawQuery("select * from COMMENT where postid = '"+id+"'",null );
        for(int i = 0; i < length; ++i){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String postid1 = jsonObject.getString("postId");
            String postid = "postId: " + postid1;
            String id1 = jsonObject.getString("id");
            String id = "id: " + id1;
            String name1 = jsonObject.getString("name");
            String name = "name: " + name1;
            String email1 = jsonObject.getString("email");
            String email = "email: " + email1;
            String body1 = jsonObject.getString("body");
            String body = "body: " + body1;
            comments.add(new comment(postid, id, name, email, body));
            if(cc.getCount() == 0)
                commentDatabase.execSQL("Insert into COMMENT values('"+postid1+"', '"+id1+"', '"+name1+"', '"+email1+"', '"+body1+"');");
        }
        Cursor c = commentDatabase.rawQuery("select * from COMMENT", null);
        System.out.println(c.getCount());
        ArrayList<HashMap<String, String>> list = new ArrayList<>();
        for(int i = 0; i < length; ++i){
            HashMap<String, String> item = new HashMap<String,String>();
            item.put("line1", comments.get(i).postId);
            item.put("line2", comments.get(i).id);
            item.put("line3", comments.get(i).name);
            item.put("line4", comments.get(i).email);
            item.put("line5", comments.get(i).body);
            list.add(item);
        }
       SimpleAdapter sa = new SimpleAdapter(this, list, R.layout.comment_list, new String[]{"line1", "line2",
                "line3", "line4", "line5"}, new int[]{R.id.postId, R.id.id, R.id.name, R.id.email, R.id.body});
        ListView listView = (ListView) findViewById(R.id.lv);
        listView.setAdapter(sa);
    }
    public void back(View v){
        finish();
    }
}
