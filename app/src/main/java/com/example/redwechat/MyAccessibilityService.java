package com.example.redwechat;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    private final static String WECHAT_UI = "com.tencent.mm.ui.LauncherUI";
    private final static String WECHAT_RED = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
    private final static String WECHAT_RED_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";
    private final static String WECHAT_RED_BUTTON = "android.widget.Button";
    private final static String WECHAT_RED_NAME = "微信红包";
    private final static String IS_GET_WECHAT_RED_NAME = "已领取";
    private final static String IS_ALL_GET_WECHAT_RED_NAME = "已被领完";
    private final static String WECHAT_UI_2 = "android.widget.FrameLayout";
    private static final String RED_PACKET_ID = "com.tencent.mm:id/ape";
    private boolean isOpenRP = false;
    private boolean isOpenDetail = false;
    private Handler handler = new Handler();
    /**
     * 已领过的红包有个"已领取"字眼，这个字眼对应的控件 id
     */
    private static final String OPENED_ID = "com.tencent.mm:id/xs";

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();

        switch (eventType) {
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                List<CharSequence> texts = event.getText();
                for (CharSequence text : texts) {
                    String content = text.toString();
                    if (!TextUtils.isEmpty(content)) {
                        if (content.contains("微信")) {
                            Log.d("Mr.xw", "step====000==");
                            isOpenRP = false;
                        }
                    }
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                AccessibilityNodeInfo rootNode = getRootInActiveWindow();
                if (WECHAT_UI.equals(className) || WECHAT_UI_2.equals(className)) {
                    findRedPacket(rootNode);
                }
                if (WECHAT_RED.equals(className) && !isOpenRP) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            openRedPacket();
                            isOpenDetail = true;
                        }
                    }, 200);

                }
                if (isOpenDetail && WECHAT_RED_DETAIL.equals(className)) {
                    isOpenDetail = false;
                    performGlobalAction(GLOBAL_ACTION_BACK);
                    release();
                }
                break;
        }
    }

    private void release() {
        isOpenRP = false;
        isOpenDetail = false;
    }

    /**
     * 遍历查找红包
     */
    private void findRedPacket(AccessibilityNodeInfo rootNode) {
      /*  if (rootNode != null) {
            for (int i = rootNode.getChildCount() - 1; i >= 0; i--) {
                AccessibilityNodeInfo node = rootNode.getChild(i);
                if (node == null) {
                    continue;
                }
                CharSequence text = node.getText();
                if (text != null && text.toString().equals(WECHAT_RED_NAME)) {
                    AccessibilityNodeInfo parent = node.getParent();
                    while (parent != null) {
                        if (parent.isClickable()) {
                            if (!parent.getChild(i).getText().equals(IS_GET_WECHAT_RED_NAME) &&
                                    !parent.getChild(i).getText().equals(IS_ALL_GET_WECHAT_RED_NAME)) {
                                if (!isOpenRP) {
                                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    isOpenRP = true;
                                }
                                Log.d("Mr.xw==", "isOpenRP===" + isOpenRP);
                            }
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
                findRedPacket(node);
                if (isOpenRP) {
                    break;
                } else {
                    findRedPacket(node);
                }

            }
        }*/
        if (rootNode == null) {
            return;
        }
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            //获取到子控件
            AccessibilityNodeInfo node = rootNode.getChild(i);
            //获取红包控件
            AccessibilityNodeInfo target = findViewByID(node, RED_PACKET_ID);
            if (target != null) {
                //已领取这个控件为空，红包还没有被领取
                if (findViewByID(node, OPENED_ID) == null) {
                    Log.i("Mr.xw==", "找到未领取的红包，点击红包===" + isOpenRP);
                    performViewClick(target);
                    break;
                }
            }
            findRedPacket(node);
        }
    }


    /**
     * 模拟点击事件
     *
     * @param nodeInfo nodeInfo
     */
    public void performViewClick(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return;
        }
        while (nodeInfo != null) {
            if (nodeInfo.isClickable() && !isOpenRP) {
                Log.i("Mr.xw==", "打开红包" + isOpenRP);
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 开始打开红包
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void openRedPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            if (rootNode.getChildCount() == 0) {
                continue;
            }
            AccessibilityNodeInfo node = rootNode.getChild(i);
            if (WECHAT_RED_BUTTON.equals(node.getClassName())) {
                Log.i("Mr.xw==", "打开拆字" + isOpenRP);
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                isOpenRP = true;
                break;
            } else {
//                openRedPacket(node);
            }
        }
    }

    private void openRedPacket(AccessibilityNodeInfo rootNode) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            if (rootNode.getChildCount() == 0) {
                continue;
            }
            AccessibilityNodeInfo node = rootNode.getChild(i);
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (WECHAT_RED_BUTTON.equals(node.getClassName())) {
                Log.i("Mr.xw==", "打开了1111===" );
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("Mr.xw==", "onInterrupt===");
    }

    public static String execByRuntime(String cmd) {
        Log.e("demo", "Mr.xw===cmd==" + cmd);
        Process process = null;
        BufferedReader bufferedReader = null;
        InputStreamReader inputStreamReader = null;
        try {
            process = Runtime.getRuntime().exec("su");
            process = Runtime.getRuntime().exec(cmd);
            inputStreamReader = new InputStreamReader(process.getInputStream());
            bufferedReader = new BufferedReader(inputStreamReader);
            int read;
            char[] buffer = new char[4096];
            StringBuilder output = new StringBuilder();
            while ((read = bufferedReader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != inputStreamReader) {
                try {
                    inputStreamReader.close();
                } catch (Throwable t) {

                }
            }
            if (null != bufferedReader) {
                try {
                    bufferedReader.close();
                } catch (Throwable t) {

                }
            }
            if (null != process) {
                try {
                    process.destroy();
                } catch (Throwable t) {

                }
            }
        }
    }


    /**
     * 查找对应ID的View
     *
     * @param accessibilityNodeInfo AccessibilityNodeInfo
     * @param id                    id
     * @return View
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public AccessibilityNodeInfo findViewByID(AccessibilityNodeInfo accessibilityNodeInfo, String id) {
        if (accessibilityNodeInfo == null) {
            return null;
        }
        List<AccessibilityNodeInfo> nodeInfoList = accessibilityNodeInfo.findAccessibilityNodeInfosByViewId(id);
        if (nodeInfoList != null && !nodeInfoList.isEmpty()) {
            for (AccessibilityNodeInfo nodeInfo : nodeInfoList) {
                if (nodeInfo != null) {
                    return nodeInfo;
                }
            }
        }
        return null;
    }
}
