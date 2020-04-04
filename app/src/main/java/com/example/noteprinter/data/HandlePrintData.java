package com.example.noteprinter.data;

import android.util.Log;

import com.example.noteprinter.entity.Note;
import com.example.noteprinter.utility.BluetoothUtil;
import com.example.noteprinter.utility.IOUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlePrintData {
    private static final String TAG = "PrintLog";
    private int d = 5120;
    public NotePacket notePacket = null;
    public NoteContainer noteContainer = null;
    static HandlePrintData handlePrintData;
    private List<NotePacket> packetList;

    public static HandlePrintData getInstance() {
        if (handlePrintData == null) {
            handlePrintData = new HandlePrintData();
        }
        return handlePrintData;
    }

    public void handle(List<Note> noteList) {
        Log.d(TAG, "handle: MAX_DATA_SIZE " + this.d);
        this.notePacket = new NotePacket();
        this.notePacket.getContent().setNoteList(noteList);
        this.notePacket = (NotePacket) NotePacket.jsonStrToObjet(this.notePacket.toJson(), NotePacket.class);
        if (this.notePacket.getContent() != null) {
            this.noteContainer = this.notePacket.getContent();
        }
        if (((Note)noteList.get(noteList.size() - 1)).getPrintType() == 1) {
            this.d = 256;
            this.packetList = this.processingFormat_A();
        } else {
            this.d = 5120;
            this.packetList = this.processingFormat_B();
        }

        Log.d(TAG, "handle: format data after package count " + this.packetList.size());

        for (int i = 0; i < 2; ++i) {
            byte[] bytes = new byte[1024];
            Arrays.fill(bytes, (byte)0);
            BluetoothUtil.getInstance().write(bytes);

            Log.d(TAG, "handle: here to check" + bytes);

            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (this.packetList != null) {
            NotePacket packet = this.packetList.get(0);
            packet.setPkgNo(1);
            packet.setPkgCount(this.packetList.size());
            packet.setPrintID((int)System.currentTimeMillis());
            Log.d(TAG, " sendPrintData, pkgCunt " + packet.getPkgCount() + " currentPkgNo " + packet.pkgNo + "PrintID " + packet.printID + "  " + packet.toJson().length());
            String str1 = packet.toJson();
            String str2 = "1250";
            byte[] bytes = IOUtil.strToByte(str2);
            if (str1.length() > 0) {
                byte[] bytes1 = str1.getBytes();
                byte[] bytes2 = a(new byte[][]{bytes, IOUtil.lengthExpressByByte(str1.length()), bytes1});
                BluetoothUtil.getInstance().write(bytes2);
            }
        }
    }

    public static byte[] a(byte[]... var0) {
        Object var1 = null;
        int var2 = 0;
        byte[][] var3 = var0;
        int var4 = var0.length;

        int var5;
        byte[] var6;
        for(var5 = 0; var5 < var4; ++var5) {
            var6 = var3[var5];
            var2 += var6.length;
        }

        byte[] var7 = new byte[var2];
        var2 = 0;
        var3 = var0;
        var4 = var0.length;

        for(var5 = 0; var5 < var4; ++var5) {
            var6 = var3[var5];
            System.arraycopy(var6, 0, var7, var2, var6.length);
            var2 += var6.length;
        }

        return var7;
    }

    public List<NotePacket> processingFormat_A() {
        List list = this.noteContainer.getNoteList();
        ArrayList resultList = new ArrayList();

        for (int i = 0; i < list.size(); ++i) {
            Note note = (Note)list.get(i);
            String str = IOUtil.decodeToStr(note.getBaseText(), "GBK");
            if (str == null) {
                return resultList;
            }
            int length = str.length() / this.d;
            int remainderNum = str.length() % this.d;
            if (remainderNum > 0) {
                length += 1;
            }
            for (int j = 0; j <length; ++j) {
                NotePacket packet = new NotePacket();
                Note decodedNote = new Note();
                NoteContainer content = new NoteContainer();
                decodedNote.setIconID(note.getIconID());
                decodedNote.setBold(note.getBold());
                decodedNote.setFontSize(note.getFontSize());
                decodedNote.setPrintType(note.getPrintType());
                decodedNote.setUnderline(note.getUnderline());
                String remainingStr;
                if (j == length - 1 && remainderNum > 0) {
                    remainingStr = str.substring(j * this.d, j * this.d + remainderNum);
                } else {
                    remainingStr = str.substring(j * this.d, j * this.d + this.d);
                }
                String finalStr = null;
                try {
                    finalStr = IOUtil.encodeToStr(remainingStr.getBytes("GBK"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                decodedNote.setBaseText(finalStr);
                content.addNote(decodedNote);
                packet.setContent(content);
                resultList.add(packet);
            }
        }

        return resultList;
    }

    public List<NotePacket> processingFormat_B() {
        ArrayList resultList = new ArrayList();
        int lengthCounter = 0;
        List list = this.noteContainer.getNoteList();
        NoteContainer content = new NoteContainer();
        NotePacket packet = new NotePacket();
        Log.d(TAG, "processingFormat_A: 原始数据共有 " + list.size() + " 个元素需要打印");
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); ++i) {
                Note note = (Note)list.get(i);
                if (note.getBaseText() != null) {
                    Log.d(TAG, "processingFormat_A: every noteSize" + note.getBaseText().length() + " printType " + note.getPrintType());
                    if (note.getBaseText().length() <= this.d) {
                        if (lengthCounter + note.getBaseText().length() < this.d) {
                            lengthCounter += note.getBaseText().length();
                            content.addNote(note);
                            if (i == list.size() - 1) {
                                packet.setPrintID((int)System.currentTimeMillis());
                                packet.setContent(content);
                                resultList.add(packet);
                                lengthCounter = 0;
                                content = new NoteContainer();
                            }
                        } else {
                            --i;
                            packet.setContent(content);
                            resultList.add(packet);
                            lengthCounter = 0;
                            content = new NoteContainer();
                            packet = new NotePacket();
                        }
                    } else {
                        int remainingLength = this.d - lengthCounter;
                        int a = remainingLength / 4;
                        remainingLength = a * 4;
                        Note extraNote = new Note();
                        extraNote.setIconID(note.getIconID());
                        extraNote.setBold(note.getBold());
                        extraNote.setFontSize(note.getFontSize());
                        extraNote.setPrintType(note.getPrintType());
                        extraNote.setUnderline(note.getUnderline());
                        String str = note.getBaseText();
                        extraNote.setBaseText(str.substring(0, remainingLength));
                        note.setBaseText(str.substring(remainingLength));
                        content.addNote(extraNote);
                        packet.setContent(content);
                        resultList.add(packet);
                        content = new NoteContainer();
                        packet = new NotePacket();
                        lengthCounter = 0;
                        --i;

                    }
                } else {
                    content.addNote(note);
                    packet.setContent(content);
                    resultList.add(packet);
                    lengthCounter = 0;
                    content = new NoteContainer();
                    packet = new NotePacket();
                }
            }
        }
        return resultList;
    }
}
