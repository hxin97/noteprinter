package com.example.noteprinter.data;

import com.example.noteprinter.entity.Note;

import java.util.List;

public class NotePacket extends BaseData {
    private String strDate;
    private int msgType = 1;
    private int command = 3;
    private int priority;
    public int pkgCount = 1;
    public int pkgNo = 1;
    public int printID = 1;
    private NoteContainer content;
    private String smartGuid;
    private String userId;
    private String toUserId;
    private String studyToUserId;
    private String originImg;

    public NotePacket() {}

    public NotePacket(NoteContainer noteContainer) {
        this.content = noteContainer;
    }

    public void setContent(NoteContainer noteContainer) {
        this.content = noteContainer;
    }

    public NoteContainer getContent() {
        if (this.content == null) {
            this.content = new NoteContainer();
        }
        return this.content;
    }

    public void setNoteList(List<Note> list) {
        if (this.content == null) {
            this.content = new NoteContainer();
        }
        if (list != null) {
            this.content.setNoteList(list);
        }
    }

    public List<Note> getNoteList() {
        if (this.content == null) {
            this.content = new NoteContainer();
        }
        return this.content.getNoteList();
    }

    public void setPrintID(int id) {
        this.printID = id;
    }

    public int getPrintID() {
        return this.printID;
    }

    public void setPkgCount(int count) {
        this.pkgCount = count;
    }

    public int getPkgCount() {
        return this.pkgCount;
    }

    public void setPkgNo(int no) {
        this.pkgNo = no;
    }

    public int getPkgNo() {
        return this.pkgNo;
    }
}