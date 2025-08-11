package com.example.mypersonalityapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<Message> {

    public ChatAdapter(Context context, List<Message> messages) {
        super(context, 0, messages);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Message message = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView senderView = convertView.findViewById(android.R.id.text1);
        TextView messageView = convertView.findViewById(android.R.id.text2);

        senderView.setText(message.sender);
        messageView.setText(message.text);

        return convertView;
    }
}
