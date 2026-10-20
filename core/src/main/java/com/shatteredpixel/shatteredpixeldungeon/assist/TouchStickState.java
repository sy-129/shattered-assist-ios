/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;

public final class TouchStickState {
    public int pointer = -1;
    public int dx, dy;
    public float x, y;
    public boolean begin(int id, float x, float y) {
        if (pointer != -1) return false;
        pointer = id;
        move(id, x, y);
        return true;
    }
    public boolean move(int id, float x, float y) {
        if (id != pointer || pointer == -1) return false;
        if (!finite(x) || !finite(y)) { cancel(); return false; }
        double length = Math.hypot(x, y);
        float scale = (float)Math.max(1, length);
        this.x = x / scale;
        this.y = y / scale;
        if (length < 0.22) { dx = dy = 0; return true; }
        int sector = ((int)Math.round(Math.atan2(y, x) / (Math.PI / 4)) + 8) % 8;
        dx = new int[]{1,1,0,-1,-1,-1,0,1}[sector];
        dy = new int[]{0,1,1,1,0,-1,-1,-1}[sector];
        return true;
    }
    public boolean end(int id) {
        if (id != pointer || pointer == -1) return false;
        cancel();
        return true;
    }
    public void cancel() { pointer = -1; dx = dy = 0; x = y = 0; }
    public static float clamp(float value, float min, float max) {
        if (!finite(value)) value = min;
        return Math.max(min, Math.min(Math.max(min, max), value));
    }
    private static boolean finite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }
}
