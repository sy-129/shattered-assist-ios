/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;

/** Pure arithmetic shared by the game and regression tests. */
public final class AssistRules {
    private AssistRules() {}
    public static int multiplier(int value) {
        return value == 2 || value == 5 || value == 10 ? value : 1;
    }
    public static int weaponDamage(int original, int multiplier) {
        return (int)Math.min(Integer.MAX_VALUE, (long)Math.max(0, original) * multiplier(multiplier));
    }
}
