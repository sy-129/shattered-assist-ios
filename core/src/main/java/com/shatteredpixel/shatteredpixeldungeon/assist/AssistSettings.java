/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;

import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;

/** Volatile fields let the actor thread see changes from the UI thread. */
public final class AssistSettings {
    public static volatile int damageMultiplier = 1;
    public static volatile boolean invulnerable = false;
    public static volatile boolean noHunger = false;
    public static volatile boolean showJoystick = true;
    public static volatile boolean buttonHidden = false;
    public static float buttonX = 0f, buttonY = 0.35f;
    private AssistSettings() {}
    public static void load() {
        damageMultiplier = AssistRules.multiplier(SPDSettings.getInt("assist.damage", 1));
        invulnerable = SPDSettings.getBoolean("assist.shield", false);
        noHunger = SPDSettings.getBoolean("assist.food", false);
        showJoystick = SPDSettings.getBoolean("assist.joystick", true);
        buttonHidden = SPDSettings.getBoolean("assist.button-hidden", false);
        buttonX = TouchStickState.clamp(SPDSettings.getInt("assist.button-x", 0) / 10000f, 0, 1);
        buttonY = TouchStickState.clamp(SPDSettings.getInt("assist.button-y", 3500) / 10000f, 0, 1);
    }
    public static void save() {
        SPDSettings.put("assist.damage", AssistRules.multiplier(damageMultiplier));
        SPDSettings.put("assist.shield", invulnerable);
        SPDSettings.put("assist.food", noHunger);
        SPDSettings.put("assist.joystick", showJoystick);
        SPDSettings.put("assist.button-hidden", buttonHidden);
        SPDSettings.put("assist.button-x", Math.round(TouchStickState.clamp(buttonX, 0, 1)*10000));
        SPDSettings.put("assist.button-y", Math.round(TouchStickState.clamp(buttonY, 0, 1)*10000));
    }
    public static void reset() {
        damageMultiplier = 1;
        invulnerable = noHunger = false;
        save();
    }
}
