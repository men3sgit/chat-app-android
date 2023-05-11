package com.example.chat_app.utilities;

import android.widget.EditText;

public class ViewAdapter {
    public static void setEndOfText(EditText input){
        int index = input.getText().length();
        input.setSelection(index);
    }
}
