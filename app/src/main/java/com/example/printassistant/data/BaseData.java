package com.example.printassistant.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseData {
    public BaseData() {}

    //自身转换为json格式
    public String toJson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.disableHtmlEscaping(); //escapeHtmlChars默认为true,表示会将html中的字符例如< >这样的字符处理转义掉。设置为false后，就不会转义这些字符。
        Gson gson = gsonBuilder.create();
        String str = gson.toJson(this);
        return str;
    }

    //判断字符串是否为json格式
    public static boolean isJson(String string) {
        try {
            if (string == null) {
                return false;
            } else {
                new JSONObject(string);  //使用json字符串中的名称/值映射关系创建一个新的JSONObject。抛出异常说明不是json格式
                return true;
            }
        } catch (JSONException e) {
            return false;
        }
    }

    public static Object jsonStrToObjet(String jsonStr, Class<?> param) {
        if (jsonStr == null) {
            return null;
        } else if (!isJson(jsonStr)) {
            return null;
        } else {
            Gson gson = new Gson();

            try {
                return gson.fromJson(jsonStr, param);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }


}
