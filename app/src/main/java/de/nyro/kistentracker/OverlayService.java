package de.nyro.kistentracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OverlayService extends Service {
    private static volatile OverlayService instance;
    private WindowManager wm;
    private View overlay;
    private WindowManager.LayoutParams window;
    private TextView count;
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    static boolean running(){return instance!=null && instance.overlay!=null;}
    static void refreshIfRunning(){
        OverlayService s=instance;
        if(s!=null && s.overlay!=null) s.overlay.post(s::refresh);
    }
    private GradientDrawable background(int color) {
        GradientDrawable d=new GradientDrawable();
        d.setColor(color);d.setCornerRadius(dp(13));d.setStroke(dp(1),0xFF965DDF);
        return d;
    }
    private TextView button(String txt,int width){
        TextView t=new TextView(this);
        t.setText(txt); t.setTextColor(Color.WHITE);t.setTextSize(18);
        t.setGravity(Gravity.CENTER);t.setBackground(background(0xFF40315B));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dp(width),dp(43));
        lp.leftMargin=dp(4);t.setLayoutParams(lp);
        return t;
    }
    @Override public void onCreate(){super.onCreate();instance=this;}
    @Override public int onStartCommand(Intent intent,int flags,int id){
        if(!Settings.canDrawOverlays(this)){stopSelf();return START_NOT_STICKY;}
        NotificationManager mgr=getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(new NotificationChannel("kisten","Kisten Tracker",
            NotificationManager.IMPORTANCE_LOW));
        PendingIntent open=PendingIntent.getActivity(this,0,new Intent(this,MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification n=new Notification.Builder(this,"kisten")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("Kisten Tracker aktiv")
            .setContentText("Kisten mit + zählen")
            .setOngoing(true).setContentIntent(open).build();
        try {
            if(Build.VERSION.SDK_INT>=34)
                startForeground(42,n,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
            else startForeground(42,n);
            if(overlay==null) showOverlay();
        } catch(Exception ex) {stopSelf();return START_NOT_STICKY;}
        return START_STICKY;
    }
    private void showOverlay(){
        wm=(WindowManager)getSystemService(WINDOW_SERVICE);
        LinearLayout bar=new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(5),dp(5),dp(5),dp(5));
        bar.setBackground(background(0xF01B1630));
        TextView drag=button("⋮⋮",32); bar.addView(drag);
        count=new TextView(this);
        count.setTextColor(Color.WHITE);count.setTextSize(12);count.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(count,new LinearLayout.LayoutParams(dp(105),dp(43)));
        TextView plus=button("+",40), minus=button("−",39), close=button("×",35);
        bar.addView(plus);bar.addView(minus);bar.addView(close);
        plus.setOnClickListener(v->{ChestCounter.add(this);refresh();});
        minus.setOnClickListener(v->{ChestCounter.undo(this);refresh();});
        close.setOnClickListener(v->stopSelf());
        window=new WindowManager.LayoutParams(-2,-2,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        window.gravity=Gravity.TOP|Gravity.START;
        window.x=getSharedPreferences("overlay",0).getInt("x",dp(5));
        window.y=getSharedPreferences("overlay",0).getInt("y",dp(115));
        drag.setOnTouchListener(new View.OnTouchListener(){
            private int x,y;private float touchX,touchY;
            @Override public boolean onTouch(View v,MotionEvent e){
                if(e.getActionMasked()==MotionEvent.ACTION_DOWN){
                    x=window.x;y=window.y;touchX=e.getRawX();touchY=e.getRawY();return true;
                }
                if(e.getActionMasked()==MotionEvent.ACTION_MOVE){
                    window.x=x+Math.round(e.getRawX()-touchX);
                    window.y=y+Math.round(e.getRawY()-touchY);
                    try {wm.updateViewLayout(overlay,window);}catch(Exception ignored){}
                    return true;
                }
                if(e.getActionMasked()==MotionEvent.ACTION_UP){
                    getSharedPreferences("overlay",0).edit().putInt("x",window.x)
                        .putInt("y",window.y).apply();return true;
                }
                return false;
            }
        });
        overlay=bar;wm.addView(overlay,window);refresh();
    }
    private void refresh(){
        if(count!=null) count.setText("Runde: "+ChestCounter.round(this)
            +"\nGesamt: "+ChestCounter.total(this));
    }
    @Override public void onDestroy(){
        if(wm!=null && overlay!=null) try{wm.removeView(overlay);}catch(Exception ignored){}
        overlay=null;if(instance==this)instance=null;super.onDestroy();
    }
    @Override public IBinder onBind(Intent i){return null;}
}
