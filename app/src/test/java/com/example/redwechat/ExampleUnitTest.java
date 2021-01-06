package com.example.redwechat;

import org.junit.Test;

import java.security.MessageDigest;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        /**后面需要加上zq*/
        String test = md5Encode("9c419570a64cbd59zq");
        System.out.println("正确的注册码是： " + test);
    }
    public String md5Encode(String inStr){
        try {
            byte[] md5Bytes = MessageDigest.getInstance("MD5").digest(inStr.getBytes("UTF-8"));
            StringBuffer hexValue = new StringBuffer();
            for (byte b : md5Bytes) {
                int val = b & 255;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
    }
}