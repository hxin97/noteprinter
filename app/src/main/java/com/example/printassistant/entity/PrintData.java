package com.example.printassistant.entity;

public class PrintData {
    private int printType;
    private byte[] data;
    private int thisBlockPacket;
    private int haveBold;
    private int haveUnderline;

    boolean isLongPicture;
    boolean isNoPagerHeader;
    boolean isNoPagerFooter;

    public PrintData(int printType, byte[] data, int thisBlockPacket) {
        this.printType = printType;
        this.data = data;
        this.thisBlockPacket = thisBlockPacket;
    }

    public PrintData(int printType, byte[] data, int thisBlockPacket, boolean isLongPicture) {
        this.printType = printType;
        this.data = data;
        this.thisBlockPacket = thisBlockPacket;
        this.isLongPicture = isLongPicture;
    }

    public int getPrintType() {
        return printType;
    }

    public void setPrintType(int printType) {
        this.printType = printType;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getThisBlockPacket() {
        return thisBlockPacket;
    }

    public void setThisBlockPacket(int thisBlockPacket) {
        this.thisBlockPacket = thisBlockPacket;
    }

    public int getHaveBold() {
        return haveBold;
    }

    public void setHaveBold(int haveBold) {
        this.haveBold = haveBold;
    }

    public int getHaveUnderline() {
        return haveUnderline;
    }

    public void setHaveUnderline(int haveUnderline) {
        this.haveUnderline = haveUnderline;
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
