package com.bshui.hongbaowx;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

public class HongBaoService extends AccessibilityService {
    public HongBaoService() {
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.i("bshui","onServiceConnected");
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        AccessibilityNodeInfo rootNodeInfo;//获取微信界面的根结点
        rootNodeInfo = accessibilityEvent.getSource();

        int eventType = accessibilityEvent.getEventType();
        switch (eventType){
            //监听通知消息,不在微信界面会最先触发这个通知
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(accessibilityEvent);
                break;
            //监听是否进入微信红包消息界面
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = accessibilityEvent.getClassName().toString();
                Log.i("bshui","className:"+className);
                if(className.equals("com.tencent.mm.ui.LauncherUI")){

                    //点击还没有拆过的红包,自已发的是查看红包,别人发的是领取红包
                    if(rootNodeInfo == null)
                        return;

                    getPacket(rootNodeInfo);
                }

                break;
                //在微信列表内但没有在微信界面,进入微信界面
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
              //  Log.i("bshui","TYPE_WINDOW_CONTENT_CHANGED");
                break;

            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                Log.i("bshui","TYPE_WINDOWS_CHANGED");
                break;

            /*
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                //界面点击
                Log.i("bshui","通知栏信息变化");
                handleNotification(accessibilityEvent);
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                //界面文字改动
                String className = accessibilityEvent.getClassName().toString();
                Log.i("bshui","className:"+className);
                if(className.equals("com.tencent.mm.ui.LauncherUI")){
                    //如果在微信界面
                    getPacket();
                }else if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")){
                    //开始打开红包
                    openPacket();
                }


                break;
                */
        }


       rootNodeInfo = accessibilityEvent.getSource();
        if(rootNodeInfo == null)
            return;

    }

    //模拟点击,打开抢红包界面
    private void getPacket(AccessibilityNodeInfo rootNodeInfo){
        List<AccessibilityNodeInfo> nodes = rootNodeInfo.findAccessibilityNodeInfosByText("领取红包");

        if (!nodes.isEmpty()){
            //判断nodes有几个,每个都要模拟点击,从最新的开始点
            for(int i=nodes.size(); i>0; i--){
                Log.i("bshui","nodes.size:"+nodes.size()+" name:"+nodes.get(i-1).getParent().getClassName().toString());
                nodes.get(i-1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }


        }else{
            Log.i("bshui","nodes is empty");
            List<AccessibilityNodeInfo> mynodes = rootNodeInfo.findAccessibilityNodeInfosByText("查看红包");
            if(!mynodes.isEmpty()) {
                if (mynodes.size() > 0)
                    mynodes.get(mynodes.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }

    public AccessibilityNodeInfo recycle(AccessibilityNodeInfo node){
        if(node.getChildCount()==0){
            if(node.getText() != null){
                if("查看红包".equals(node.getClassName().toString())){

                    Log.i("bshui","get node-------");
                    return node;
                }
            }
        }else{
            for(int i=0; i<node.getChildCount();i++){
                if(node.getChild(i)!=null){
                    recycle(node.getChild(i));
                }
            }
        }
        return node;

    }

    private void openPacket(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo !=null){
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/c2i");

            if(list.get(0).getClassName().toString().equals("android.widget.Button")){
                list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }

        }
    }

    /**
     * 处理通知栏信息,如果微信红包提示,则触发通知消息的intent
     * 进入当前的聊天界面
     * @param event
     */
    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String context = text.toString();
                //[微信红包]恭喜发财，大吉大利
                if (context.contains("微信红包")) {
                    //打开通知栏状态
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();

                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Log.i("bshui","onInterrupt");
    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("bshui","onUnbind");
        return super.onUnbind(intent);

    }
}
