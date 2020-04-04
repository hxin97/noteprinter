package com.example.noteprinter.entity;

import com.example.noteprinter.utility.IOUtil;

public class Note {
    private int iconID;
    private int printType; //1为text
    private int fontSize = 1;
    private int encodeType = 0;
    private int heightImg = 0;
    private int bold = 0;
    private int underline = 0;
    private String baseText;
    String originalImgUrl;
    String originalImgPath;
    boolean isTimeStamp;
    boolean isLongPicture;
    boolean isNoPagerHeader;
    boolean isNoPagerFooter;

    public Note() {
    }

    public Note(int printType, String baseText) {
        this.printType = printType;
        this.baseText = IOUtil.transCode(baseText, printType); //将原字符串转码为Base64字符
    }

    public Note(int printType, String baseText, boolean isTimeStamp) {
        this.printType = printType;
        this.isTimeStamp = isTimeStamp;
        this.baseText = IOUtil.transCode(baseText, printType);
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public int getPrintType() {
        return printType;
    }

    public void setPrintType(int printType) {
        this.printType = printType;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public int getEncodeType() {
        return encodeType;
    }

    public void setEncodeType(int encodeType) {
        this.encodeType = encodeType;
    }

    public int getHeightImg() {
        return heightImg;
    }

    public void setHeightImg(int heightImg) {
        this.heightImg = heightImg;
    }

    public int getBold() {
        return bold;
    }

    public void setBold(int bold) {
        this.bold = bold;
    }

    public void setBold(boolean b) {
        this.bold = b ? 1 : 0;
    }

    public int getUnderline() {
        return underline;
    }

    public void setUnderline(boolean b) {
        this.underline = b ? 1 : 0;
    }

    public void setUnderline(int underline) {
        this.underline = underline;
    }

    public String getBaseText() {
        return baseText;
    }

    public void setBaseText(String baseText) {
        this.baseText = baseText;
    }

    public String getOriginalImgUrl() {
        return originalImgUrl;
    }

    public void setOriginalImgUrl(String originalImgUrl) {
        this.originalImgUrl = originalImgUrl;
    }

    public String getOriginalImgPath() {
        return originalImgPath;
    }

    public void setOriginalImgPath(String originalImgPath) {
        this.originalImgPath = originalImgPath;
    }

    public boolean isTimeStamp() {
        return isTimeStamp;
    }

    public void setTimeStamp(boolean timeStamp) {
        isTimeStamp = timeStamp;
    }

    public boolean isLongPicture() {
        return isLongPicture;
    }

    public void setLongPicture(boolean longPicture) {
        isLongPicture = longPicture;
    }

    public boolean isNoPagerHeader() {
        return isNoPagerHeader;
    }

    public void setNoPagerHeader(boolean noPagerHeader) {
        isNoPagerHeader = noPagerHeader;
    }

    public boolean isNoPagerFooter() {
        return isNoPagerFooter;
    }

    public void setNoPagerFooter(boolean noPagerFooter) {
        isNoPagerFooter = noPagerFooter;
    }
}
