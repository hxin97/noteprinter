package com.example.noteprinter.data;

import com.example.noteprinter.entity.Note;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NoteContainer extends BaseData {
    long noteId = -1L;
    String bluetoothMac = "";
    public List<Note> noteList = new ArrayList();
    public transient long totalDataSize = 0L;

    public NoteContainer(){}

    public NoteContainer(List<Note> list) {
        this.noteList = list;
        Iterator iterator = list.iterator();

        while(iterator.hasNext()) {
            Note note = (Note) iterator.next();
            if (note.getBaseText() != null) {
                this.totalDataSize += (long)note.getBaseText().length();
            }
        }
    }

    public NoteContainer(long id, String bluetoothMac, List<Note> list) {
        this(list);
        this.noteId = id;
        this.bluetoothMac = bluetoothMac;
    }

    public int addNote(Note note) {
        if (note == null) {
            return -1;
        } else {
            if (note.getBaseText() != null) {
                this.totalDataSize += (long)note.getBaseText().length();
            }
            this.noteList.add(note);
            return this.noteList.indexOf(note);
        }
    }

    public void countTotalDataSize() {
        Iterator iterator = this.noteList.iterator();
        while(iterator.hasNext()) {
            Note note = (Note) iterator.next();
            if (note.getBaseText() != null) {
                this.totalDataSize += (long) note.getBaseText().length();
            }
        }
    }

    public long getNoteId() {return this.noteId; }

    public String getBluetoothMac() {return this.bluetoothMac; }

    public List<Note> getNoteList() {return this.noteList; }

    public void setNoteId(long id) {this.noteId = id; }

    public void setNoteList(List<Note> list) {this.noteList = list; }

}
