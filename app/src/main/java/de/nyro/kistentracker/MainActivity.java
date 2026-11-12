package de.nyro.kistentracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView roundView, totalView, status;
    private Button overlayButton;
    private boolean waitingForOverlay;
    private final int purple = Color.rgb(157, 109, 255);
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }

    private TextView text(String s, int size, int color) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(color);
        v.setTextSize(size);
        return v;
    }
    private void add(LinearLayout root, android.view.View v, int margin) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1,-2);
        p.topMargin = dp(margin);
        root.addView(v,p);
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(23,17,39));
        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp(22),dp(30),dp(22),dp(30));
        root.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(root);
        TextView title = text("FORTNITE KISTEN TRACKER",26, Color.WHITE);
        title.setTypeface(null,Typeface.BOLD);
        add(root,title,5);
        add(root,text("Kisten mit +1 selbst zählen – auch als Fenster über Fortnite.",14,0xFFBEB5D5),10);
        roundView = text("",30,Color.WHITE);
        totalView = text("",23,purple);
        add(root,roundView,28);
        add(root,totalView,7);
        Button plus = new Button(this);
        plus.setText("+1 KISTE");
        plus.setOnClickListener(v->{ChestCounter.add(this); refresh();});
        add(root,plus,20);
        Button minus = new Button(this);
        minus.setText("−1 RÜCKGÄNGIG");
        minus.setOnClickListener(v->{ChestCounter.undo(this); refresh();});
        add(root,minus,4);
        overlayButton = new Button(this);
        overlayButton.setText("OVERLAY STARTEN");
        overlayButton.setOnClickListener(v->{
            if (OverlayService.running()) {
                stopService(new Intent(this,OverlayService.class));
                refresh();
            } else {
                startOverlay();
            }
        });
        add(root,overlayButton,15);
        Button next = new Button(this);
        next.setText("NEUE RUNDE");
        next.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Neue Runde?")
            .setMessage("Nur der Rundenzähler wird auf 0 gesetzt.")
            .setNegativeButton("Abbrechen",null)
            .setPositiveButton("Starten",(d,w)->{ChestCounter.newRound(this); refresh();})
            .show());
        add(root,next,7);
        Button reset = new Button(this);
        reset.setText("ALLE ZÄHLER ZURÜCKSETZEN");
        reset.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Alles zurücksetzen?")
            .setMessage("Runde und Gesamtzahl werden gelöscht.")
            .setNegativeButton("Abbrechen",null)
            .setPositiveButton("Löschen",(d,w)->{ChestCounter.reset(this);refresh();})
            .show());
        add(root,reset,7);
        status = text("Overlay starten und danach Fortnite öffnen.",13,0xFFBEB5D5);
        status.setGravity(Gravity.CENTER);
        add(root,status,20);
        setContentView(scroll);
        refresh();
    }
    private void refresh() {
        roundView.setText("Diese Runde: "+ChestCounter.round(this));
        totalView.setText("Gesamt: "+ChestCounter.total(this));
        overlayButton.setText(OverlayService.running()?"OVERLAY SCHLIESSEN":"OVERLAY STARTEN");
        OverlayService.refreshIfRunning();
    }
    @Override protected void onResume() {
        super.onResume();
        if (roundView != null) refresh();
        if (waitingForOverlay && Settings.canDrawOverlays(this)) {
            waitingForOverlay=false;
            startOverlay();
        }
    }
    private void startOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            waitingForOverlay=true;
            status.setText("Bitte Einblenden über anderen Apps erlauben.");
            Intent i=new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:"+getPackageName()));
            startActivity(i);
            return;
        }
        try {
            startForegroundService(new Intent(this, OverlayService.class));
            status.setText("Overlay angefordert. Jetzt Fortnite öffnen.");
            refresh();
        } catch(Exception ex) {
            status.setText("Start fehlgeschlagen: "+ex.getClass().getSimpleName());
        }
    }
}
