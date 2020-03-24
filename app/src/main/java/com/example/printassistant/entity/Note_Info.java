package com.example.printassistant.entity;

import android.graphics.Bitmap;

import java.io.Serializable;

public class Note_Info implements Serializable, Comparable<Note_Info>{
    private String textContent;
    private Bitmap bitmap;
    private String createTime;

    public Note_Info () {}

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @Override
    public int compareTo(Note_Info o) {
        if (Long.parseLong(this.createTime) < Long.parseLong(o.createTime))
            return 1;
        else if (this.createTime == o.createTime)
            return 0;
        else
            return -1;

    }
}
