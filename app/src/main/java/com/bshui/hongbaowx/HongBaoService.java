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
        AccessibilityNodeInfo mReceiveNode;
        AccessibilityNodeInfo mUnpackNode;
        int eventType = accessibilityEvent.getEventType();
        switch (eventType){
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
        }


       rootNodeInfo = accessibilityEvent.getSource();
        if(rootNodeInfo == null)
            return;

    }

    //模拟点击,打开抢红包界面
    private void getPacket(){
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();

      /* AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo !=null){
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ada");
            Log.i("bshui","-------name:"+list.get(0).getClassName().toString());
            Log.i("bshui","-------name:"+list.get(1).getClassName().toString());
          //  if(list.get(0).getClassName().toString().equals("android.widget.LinearLayout")){
              //  list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
           // }

        }*/

       // AccessibilityNodeInfo node = recycle(rootNode);
        //Log.i("bshui","getPacket():click="+node.getParent().isClickable());
        /* node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        AccessibilityNodeInfo parent = node.getParent();
        while(parent !=null){
            if(parent.isClickable()){
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                Log.i("bshui","parent...");
                break;
            }
        }
        */
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
     * 处理通知栏信息,如果微信红包提示,则摸拟点击
     * @param event
     */
    private void handleNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (!texts.isEmpty()) {
            for (CharSequence text : texts) {
                String context = text.toString();
                if (context.contains("微信红包")) {
                    if (event.getParcelableData() != null && event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                            Log.i("bshui","send....");
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
