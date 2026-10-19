/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

/** Volatile fields let the actor thread see changes from the UI thread. */
public final class AssistSettings {
    public static volatile int damageMultiplier = 1;
    public static volatile boolean invulnerable = false;
    public static volatile boolean noHunger = false;
    private AssistSettings() {}
    public static void load() {
        damageMultiplier = AssistRules.multiplier(SPDSettings.getInt("assist.damage", 1));
        invulnerable = SPDSettings.getBoolean("assist.shield", false);
        noHunger = SPDSettings.getBoolean("assist.food", false);
    }
    public static void save() {
        SPDSettings.put("assist.damage", AssistRules.multiplier(damageMultiplier));
        SPDSettings.put("assist.shield", invulnerable);
        SPDSettings.put("assist.food", noHunger);
    }
    public static void reset() {
        damageMultiplier = 1;
        invulnerable = noHunger = false;
        save();
    }
}
