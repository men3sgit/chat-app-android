package com.example.chat_app.utilities;

import java.util.StringTokenizer;

public final class DateAdapter {
    private int day;
    private int month;
    private int year;
    private String src;

    public DateAdapter(String src) {
        this.src = src;
        loadDate();
    }

    private void loadDate() {
        StringTokenizer token  = new StringTokenizer(src,"/");
        this.day = Integer.parseInt(token.nextToken());
        this.month = Integer.parseInt(token.nextToken());
        this.year = Integer.parseInt(token.nextToken());

    }

    public void setDate(String src) {
        this.src = src;
        loadDate();
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }
}
