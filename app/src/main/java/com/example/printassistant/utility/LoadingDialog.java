package com.example.printassistant.utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.WindowManager;

import com.example.printassistant.R;

public class LoadingDialog extends Dialog {

    private static final String TAG = "LoadingDialog";

    public LoadingDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.loading_dialog);
        //设置窗口大小
        WindowManager windowManager = getWindow().getWindowManager();
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        //设置窗口背景透明度
        attributes.alpha = 0.3f;
        //设置窗口宽高为屏幕的三分之一
        attributes.width = screenWidth/3;
        attributes.height = attributes.width;
        getWindow().setAttributes(attributes);
        setCancelable(true);
    }

}