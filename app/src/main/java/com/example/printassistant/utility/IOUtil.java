package com.example.printassistant.utility;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class IOUtil {
    public static short byteToShort(byte[] var0) {
        if (var0 == null) {
            return 0;
        } else {
            boolean var1 = false;
            short var2 = (short)(var0[0] & 0xff);
            short var3 = (short)(var0[1] & 0xff);
            var3 = (short)(var3 << 8);
            short var4 = (short)(var2 | var3);
            return var4;
        }
    }

    public static byte[] lengthExpressByByte(int i) {
        byte[] bytes = new byte[2];
        for(int j = 0; j < bytes.length; ++j) {
            Log.d("IOUtil", "lengthExpressByByte: here to check1: i = " + i + ">>>" + new Integer(i & 255).toString());
            bytes[j] = (new Integer(i & 255)).byteValue();    //255=0xFF=produceDataPiece_2(11111111)，保留i的低八位
            i >>= 8;
        }
        return bytes;
    }

    public static byte[] toArray1D(byte[]... param) {
        int i = 0;
        byte[][] doubleBytes = param;
        int length = param.length;

        int j;
        byte[] bytes;
        for(j = 0; j < length; ++j) {
            bytes = doubleBytes[j];
            if (bytes != null) {
                i += bytes.length;
            }
        }
        byte[] resultBytes = new byte[i];
        i = 0;
        doubleBytes = param;
        length = param.length;

        for (j = 0;j < length; ++j) {
            bytes = doubleBytes[j];
            if (bytes != null) {
                System.arraycopy(bytes, 0, resultBytes, i, bytes.length);
                i += bytes.length;
            }
        }

        return resultBytes;
    }

    public static byte[] strToByte(String str) {
        if (str != null && !str.equals("")) {
            str = str.toUpperCase();
            int l = str.length() / 2;
            char[] chars = str.toCharArray();
            byte[] bytes = new byte[l];
            for (int i = 0; i < l; ++i) {
                int j = i * 2;
                bytes[j] = (byte)(charToByte(chars[j]) << 4 | charToByte(chars[j + 1]));
            }
            return bytes;
        } else {
            return null;
        }
    }

    private static byte charToByte(char param) {
        return (byte)"0123456789ABCDEF".indexOf(param);
    }

    //将字节数组转码成Base64字符
    public static String encodeToStr(byte[] encodedBytes) {
        try{
            String result = encodedBytes == null ? null : Base64.encodeToString(encodedBytes, 2); //2代表default
            return result;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public static String decodeToStr(String text, String charsetName) {
        if (text == null) {
            return null;
        } else {
            try{
                byte[] bytes = Base64.decode(text, 2);
                return new String(bytes, charsetName);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    //将原字符串转码为Base64字符
    public static String transCode(String text, int printType) {
        String transcodedText = "";
        byte[] encodedBytes;
        if(printType == 1) {
            text = text + "\n"; //文本末尾一定要有换行符
            try {
                encodedBytes = text.getBytes("GBK");
                transcodedText = encodeToStr(encodedBytes);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else if (printType == 3) {
            encodedBytes = text.getBytes();
            transcodedText = encodeToStr(encodedBytes);
        } else {
            transcodedText = text;
        }
        return transcodedText;
    }

}
