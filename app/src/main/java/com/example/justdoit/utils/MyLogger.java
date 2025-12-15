package com.example.justdoit.utils;

import android.content.Context;
import android.widget.Toast;

public class MyLogger {
    public static void toast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }
}
