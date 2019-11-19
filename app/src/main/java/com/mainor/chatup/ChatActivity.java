package com.mainor.chatup;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    String activerUser = "";
    ArrayList<String> messages = new ArrayList<>();
    ArrayAdapter arrayAdapter;

    public void sendChat(View view){
        final TextView chatEditText = (EditText)findViewById(R.id.chatEditText);
        final ParseObject message = new ParseObject("Messages");
        if (chatEditText.getText().toString().isEmpty()){
            Toast.makeText(this, "empty message", Toast.LENGTH_SHORT).show();
        }else {
            final String messageContent = chatEditText.getText().toString();
            message.put("sender", ParseUser.getCurrentUser().getUsername());

            message.put("recipient", activerUser);
            message.put("message", messageContent);

            chatEditText.setText("");

            message.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e ==null){
                        Toast.makeText(ChatActivity.this, "Message sent.", Toast.LENGTH_SHORT).show();
                        messages.add(ParseUser.getCurrentUser().getUsername() + ":> " + messageContent);
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        activerUser = intent.getStringExtra("username");
        setTitle("Chat with - " + activerUser.toUpperCase());

        ListView chatListView = (ListView)findViewById(R.id.chatListView);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, messages);
        chatListView.setAdapter(arrayAdapter);

        ParseQuery<ParseObject> query1 = new ParseQuery<ParseObject>("Messages");
        query1.whereEqualTo("sender", ParseUser.getCurrentUser().getUsername());
        query1.whereEqualTo("recipient", activerUser);

        ParseQuery<ParseObject> query2 = new ParseQuery<ParseObject>("Messages");
        query2.whereEqualTo("recipient", ParseUser.getCurrentUser().getUsername());
        query2.whereEqualTo("sender", activerUser);

        List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
        queries.add(query1);
        queries.add(query2);

        ParseQuery<ParseObject> query = ParseQuery.or(queries);
        query.orderByAscending("createdAt");

        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e==null){
                    if (objects.size() > 0){
                        messages.clear();
                        for (ParseObject message : objects){
                            String messageContent = message.getString("message");
                            if (!message.getString("sender").equals(ParseUser.getCurrentUser().getUsername())){
                                messageContent = activerUser.toLowerCase() + ":> " + messageContent;
                            }else {
                                messageContent = ParseUser.getCurrentUser().getUsername().toLowerCase()  + ":>" + messageContent;
                            }
                            messages.add(messageContent);
                        }
                        arrayAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
}
