package com.example.justdoit.utils;

import android.content.Context;
import android.widget.Toast;

import com.example.justdoit.application.HomeApplication;

public class MyLogger {
    public static void toast(String text) {
        Toast.makeText(HomeApplication.getAppContext(), text, Toast.LENGTH_SHORT).show();
    }
}
