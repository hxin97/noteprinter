package com.example.printassistant.utility;

import android.util.Log;

import com.example.printassistant.entity.Note;
import com.example.printassistant.entity.PrintData;
import com.example.printassistant.task.PrintTask;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PrintUtil {
    static int fixedLength = 1024;  //每次发送1024字节的数据
    static int b = 0;
    static int ccc = 0;
    private static int e = 0;
    public static boolean INTERRUPTER = false;    //用于中断打印

    private static final String TAG = "PrintUtil";

    public static synchronized int printIt(List<Note> noteList) {
        b = 1;
        ccc = 0;
        e = 0;
        INTERRUPTER = false;
        ArrayList printDataList = new ArrayList();
        Iterator iterator = noteList.iterator();

        while (iterator.hasNext()) {
            Note initialNote = (Note)iterator.next();
            PrintData processedData = null;
            byte[] pieceOfText = null;
            switch (initialNote.getPrintType()) {
                case 1:
                    String textStr = IOUtil.decodeToStr(initialNote.getBaseText(), "GBK");  //取出Base64字符解码回原本的文本输入框内容的字符串
                    PrintTask.timeOutCounter += textStr.length() / 300 * 60;
                    short defaultNum = 500;  //每次处理500个字符
                    int times = textStr.length() / defaultNum + (textStr.length() % defaultNum > 0 ? 1 : 0);
                    Log.d(TAG, "printIt: here to check: times = " + times + " and textStr.length() = " + textStr.length());

                    for (int i = 0; i < times; ++i) {
                        try {
                            int remainingNum = textStr.length() - i * defaultNum;
                            remainingNum = remainingNum < defaultNum ? remainingNum : defaultNum;
                            pieceOfText = textStr.substring(i * defaultNum, i * defaultNum + remainingNum).getBytes("GBK");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        processedData = new PrintData(1, pieceOfText, countBlockPacket(pieceOfText));
                        processedData.setHaveBold(initialNote.getBold());
                        processedData.setHaveUnderline(initialNote.getUnderline());
                        processedData.setNoPagerHeader(initialNote.isNoPagerHeader());
                        processedData.setNoPagerFooter(initialNote.isNoPagerFooter());
                        ccc += processedData.getThisBlockPacket();
                        printDataList.add(processedData);
                    }
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
            }

//            if (processedData != null && pieceOfText != null && initialNote.getPrintType() != 1) {
//                ccc += processedData.getThisBlockPacket();
//                printDataList.add(processedData);
//            }
        }


        for (int i = 0; i < 2; ++i) {
            byte[] b = new byte[1024];
            Arrays.fill(b, (byte)0);
            BluetoothUtil.getInstance().write(b);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        iterator = printDataList.iterator();

        while (iterator.hasNext()) {
            PrintData pData = (PrintData) iterator.next();
            if (INTERRUPTER || BluetoothUtil.getInstance().getConnectDevice() == null) {
                break;
            }
            switch (pData.getPrintType()) {
                case 1:
                    startPrintText(pData);
            }
        }

        INTERRUPTER = false;
        return b >= ccc ? 1 : -1;
    }

    public static int countBlockPacket(byte[] param) {
        int resultNum = 0;
        if (param != null && param.length > 0) {
            resultNum = param.length / fixedLength;
            resultNum += param.length % fixedLength != 0 ? 1 : 0;
        }
        Log.d(TAG, "countBlockPacket: here to check: resultNum = " + resultNum + "bytes.length = " + param.length);
        return resultNum;
    }

    public static int startPrintText(PrintData note) {
        Log.d(TAG, "startPrintText: >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        byte[] bytes1 = processDataPiece(new byte[]{11}, new byte[]{(byte)(ccc % 256), (byte)(ccc / 256)});
        byte[] bytes2 = produceDataPiece_2(note.isNoPagerHeader());
        byte[] bytes3 = produceDataPiece_2(note.isNoPagerFooter());
        byte[] bytes4 = IOUtil.toArray1D(new byte[][]{bytes2, bytes3});

        for(int i = 0; i < note.getThisBlockPacket() && !INTERRUPTER && BluetoothUtil.getInstance().getConnectDevice() != null; ++b) {
            byte[] bytes6 = processDataPiece(new byte[]{12}, new byte[]{(byte)(ccc % 256), (byte)(ccc / 256)});
            int i7 = note.getData().length - i * fixedLength;
            byte[] bytes8 = new byte[i7 >= fixedLength ? fixedLength : i7];
            System.arraycopy(note.getData(), i * fixedLength, bytes8, 0, bytes8.length);
            Log.d(TAG, "startPrintText: currPacketCoding: " + b + " ");
            byte[] bytes9 = produceDataPiece(bytes1, bytes6, bytes4, processTextContentPiece(bytes8, note.getHaveBold(), note.getHaveUnderline()));
            BluetoothUtil.getInstance().write(bytes9);
            try {
                Thread.sleep(50L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ++ i;
        }
        Log.d(TAG, "startPrintText: print text end <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        return -1;
    }

    public static byte[] processDataPiece(byte[] b1, byte[] b2) {
        Log.d(TAG, "processDataPiece: here to check2: b2.length = " + b2.length);
        byte[] b3 = IOUtil.lengthExpressByByte(b2.length);
        byte[] b4 = IOUtil.toArray1D(new byte[][]{b1, b3, b2});
        return b4;
    }

    public static byte[] produceDataPiece_2(boolean b) {
        byte[] bytes = null;
        if (b) {
            bytes = processDataPiece(new byte[]{23}, new byte[]{0});
        }
        return bytes;
    }

    public static byte[] produceDataPiece(byte[] var0, byte[] var1, byte[] var2, byte[] var3) {
        byte[] var4 = new byte[]{4};
        if (var2 != null) {
        }

        byte[] var5 = IOUtil.toArray1D(new byte[][]{var0, var2, var1, var3});
        byte[] var6 = a(var4, var5);
        return var6;
    }

    public static byte[] a(byte[] var0, byte[] var1) {
        byte[] var2 = new byte[]{-86};
        byte[] var3 = c(var0, var1);
        byte[] var4 = a(var2, var3, var0, var1);
        byte[] var5 = IOUtil.toArray1D(new byte[][]{var2, var3, var0, var1, var4});
        byte var6 = 0;

        for(int var7 = 0; var7 < var5.length; ++var7) {
            var6 = (byte)(var6 + var5[var7] & 255);
        }

        return var5;
    }

    public static byte[] a(byte[] var0, byte[] var1, byte[] var2, byte[] var3) {
        byte var4 = 0;

        int var5;
        for(var5 = 0; var5 < var0.length; ++var5) {
            var4 += var0[var5];
            var4 = (byte)(var4 & 255);
        }

        for(var5 = 0; var5 < var1.length; ++var5) {
            var4 += var1[var5];
            var4 = (byte)(var4 & 255);
        }

        for(var5 = 0; var5 < var2.length; ++var5) {
            var4 += var2[var5];
            var4 = (byte)(var4 & 255);
        }

        for(var5 = 0; var5 < var3.length; ++var5) {
            var4 += var3[var5];
            var4 = (byte)(var4 & 255);
        }

        var4 = (byte)(256 - var4);
        return new byte[]{var4};
    }

    public static byte[] c(byte[] var0, byte[] var1) {
        int var2 = var0.length + var1.length;
        byte[] var3 = IOUtil.lengthExpressByByte(var2);
        return var3;
    }

    public static byte[] processTextContentPiece(byte[] data, int bold, int underline) {
        byte[] var3 = processDataPiece(new byte[]{13}, new byte[]{0});
        Object var4 = null;
        Object var5 = null;
        byte[] data_bold;
        if (bold != 0) {
            data_bold = processDataPiece(new byte[]{17}, new byte[]{1});
        } else {
            data_bold = processDataPiece(new byte[]{17}, new byte[]{0});
        }

        byte[] data_underline;
        if (underline != 0) {
            data_underline = processDataPiece(new byte[]{16}, new byte[]{-126});
        } else {
            data_underline = processDataPiece(new byte[]{16}, new byte[]{0});
        }

        byte[] var6 = new byte[]{7};
        byte[] data_text = processDataPiece(var6, data);
        byte[] dataPiece_TextContent = IOUtil.toArray1D(new byte[][]{var3, data_bold, data_underline, data_text});
        return dataPiece_TextContent;
    }
}
