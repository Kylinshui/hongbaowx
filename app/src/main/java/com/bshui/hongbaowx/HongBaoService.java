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
       // Log.i("bshui","eventType:=0x"+Integer.toHexString(eventType));

        switch (eventType){
            //监听通知消息,不在微信界面会最先触发这个通知0x40
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                handleNotification(accessibilityEvent);
                break;

            //监听是否进入微信红包消息界面 0x20
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = accessibilityEvent.getClassName().toString();

                if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI")){
                    //进入开红包界面,点击拆红包,根据id找节点

                    openPacket();
                }
                if(className.equals("com.tencent.mm.ui.base.p")){
                    return;
                }
                if(className.equals("mm.plugin.luckymoney.ui.LuckyMoneyBeforeDetailUI")){
                    return;
                }
              //  Log.i("bshui","className:"+className);
                if(className.equals("com.tencent.mm.ui.LauncherUI")){

                    //点击还没有拆过的红包,自已发的是查看红包,别人发的是领取红包
                    if(rootNodeInfo == null) {
                       // Log.i("bshui","rootNodeInfo is null");
                        return;
                    }

                    getPacket(rootNodeInfo);

                }
                if(className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")){
                    //拆开红包后进入红包详情页返回聊天界面

                    backPacket();
                    //GLOBAL_ACTION_HOME
                    //修改成返回GLOBAL_ACTION_HOME界面才能接收到下一次的红包通知
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                }

                break;
        }
    }




    //模拟点击,打开抢红包界面
    private void getPacket(AccessibilityNodeInfo rootNodeInfo) {
        List<AccessibilityNodeInfo> nodes = rootNodeInfo.findAccessibilityNodeInfosByText("微信红包");
        List<AccessibilityNodeInfo> nodes1 = rootNodeInfo.findAccessibilityNodeInfosByText("已被领完");
      //  Log.i("bshui","getPacket 红包数:"+nodes.size()+" 已领取:"+nodes1.size());

        if (!nodes.isEmpty()) {
            //判断nodes有几个,点击最新的一个
            //当红包数大于已领取数时才点击
            if(nodes.size()>nodes1.size()) {

                nodes.get(nodes.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }else if(nodes.size() == nodes1.size()){
                //全部已被领取，返回
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
            }
        }else{
          //  Log.i("bshui","getPacket nodes empty");
            //纯文字，假红包
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
    }

    /**
     * 拆红包
     */
    private void openPacket(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo !=null){
         //   Log.i("bshui","OpenPacket");
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cv0");

            //if(list.get(0).getClassName().toString().equals("android.widget.Button")){
                list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);

        }
    }

    //从红包返回界面
    private void backPacket(){
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if(nodeInfo !=null){
            //找到返回的id android.widget.LinearLayout
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/k4");

            if(list.size() > 0) {
               // if (list.get(0).getClassName().toString().equals("android.widget.LinearLayout")) {
                    list.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
               // }
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
                if (context.contains("[微信红包]")) {
                  //  Log.i("bshui","-------handeNotification");
                    //打开通知栏状态,进入相应的聊天窗口
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
    public void onInterrupt()
    {

    }

    @Override
    public boolean onUnbind(Intent intent) {

        Log.i("bshui","onUnbind");
        return super.onUnbind(intent);

    }
}
