package de.nyro.kistentracker;

import android.content.Context;
import android.content.SharedPreferences;

final class ChestCounter {
    private static SharedPreferences p(Context c) {
        return c.getSharedPreferences("counter", Context.MODE_PRIVATE);
    }
    static synchronized int round(Context c) { return p(c).getInt("round", 0); }
    static synchronized int total(Context c) { return p(c).getInt("total", 0); }
    static synchronized void add(Context c) {
        p(c).edit().putInt("round", round(c)+1).putInt("total", total(c)+1).apply();
    }
    static synchronized void undo(Context c) {
        if (round(c) == 0) return;
        p(c).edit().putInt("round", round(c)-1)
            .putInt("total", Math.max(0, total(c)-1)).apply();
    }
    static synchronized void newRound(Context c) {
        p(c).edit().putInt("round", 0).apply();
    }
    static synchronized void reset(Context c) {
        p(c).edit().putInt("round", 0).putInt("total", 0).apply();
    }
}
