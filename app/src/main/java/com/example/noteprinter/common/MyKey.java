package com.example.noteprinter.common;

public class MyKey {
    public MyKey() {}

    public static class RESULT {
        public static final int COMMON_SUCCESS = 1;
        public static final int COMMON_FAIL = -1004;
        public static final int COMMON_LOST = -1005;

        public RESULT(){}
    }

    public static class PRINT_TYPE {
        public static final int TEXT = 1;
        public static final int IMAGE = 2;


        public PRINT_TYPE(){}
    }

    public static class Major {
        public static final int MISC              = 0x0000;
        public static final int COMPUTER          = 0x0100;
        public static final int PHONE             = 0x0200;
        public static final int NETWORKING        = 0x0300;
        public static final int AUDIO_VIDEO       = 0x0400;
        public static final int PERIPHERAL        = 0x0500;
        public static final int IMAGING           = 0x0600;
        public static final int WEARABLE          = 0x0700;
        public static final int TOY               = 0x0800;
        public static final int HEALTH            = 0x0900;
        public static final int UNCATEGORIZED     = 0x1F00;

        public Major(){}
    }

}
