/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;

import com.badlogic.gdx.Preferences;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Dagger;
import com.watabou.utils.Random;
import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.*;

public class AssistIntegrationTest {
    private final Map<String, Object> values = new HashMap<>();
    @Before public void reset() {
        Preferences prefs = (Preferences)Proxy.newProxyInstance(Preferences.class.getClassLoader(),
                new Class<?>[]{Preferences.class}, (proxy, method, args) -> {
            String name = method.getName();
            if (name.startsWith("put")) { values.put((String)args[0], args[1]); return proxy; }
            if (name.equals("contains")) return values.containsKey(args[0]);
            if (name.equals("get")) return values;
            if (name.startsWith("get")) return values.getOrDefault(args[0], args[1]);
            if (name.equals("clear")) values.clear();
            if (name.equals("remove")) values.remove(args[0]);
            return null;
        });
        SPDSettings.set(prefs);
        AssistSettings.reset();
        Dungeon.hero = new Hero();
    }
    @Test public void preferencesRestoreAndReset() {
        AssistSettings.damageMultiplier = 5;
        AssistSettings.invulnerable = true;
        AssistSettings.noHunger = true;
        AssistSettings.save();
        AssistSettings.damageMultiplier = 1;
        AssistSettings.invulnerable = AssistSettings.noHunger = false;
        AssistSettings.load();
        assertEquals(5, AssistSettings.damageMultiplier);
        assertTrue(AssistSettings.invulnerable);
        assertTrue(AssistSettings.noHunger);
        AssistSettings.reset();
        AssistSettings.load();
        assertEquals(1, AssistSettings.damageMultiplier);
        assertFalse(AssistSettings.invulnerable);
        assertFalse(AssistSettings.noHunger);
    }
    @Test public void damageArithmeticDoesNotOverflow() {
        assertEquals(70, AssistRules.weaponDamage(7, 10));
        assertEquals(Integer.MAX_VALUE, AssistRules.weaponDamage(Integer.MAX_VALUE, 10));
        assertEquals(0, AssistRules.weaponDamage(-5, 10));
        assertEquals(7, AssistRules.weaponDamage(7, -1));
        assertEquals(7, AssistRules.weaponDamage(7, Integer.MAX_VALUE));
    }
    @Test public void actualHeroUnarmedDamageUsesMultiplier() {
        for (int multiplier : new int[]{1, 2, 5, 10}) {
            for (int seed = 0; seed < 25; seed++) {
                AssistSettings.damageMultiplier = 1;
                Random.pushGenerator(seed);
                int original;
                try { original = Dungeon.hero.damageRoll(); } finally { Random.popGenerator(); }
                AssistSettings.damageMultiplier = multiplier;
                Random.pushGenerator(seed);
                try { assertEquals(original * multiplier, Dungeon.hero.damageRoll()); }
                finally { Random.popGenerator(); }
            }
        }
    }
    @Test public void actualWeaponDamageUsesMultiplier() {
        Dungeon.hero.belongings.weapon = new Dagger();
        AssistSettings.damageMultiplier = 1;
        Random.pushGenerator(100);
        int original;
        try { original = Dungeon.hero.damageRoll(); } finally { Random.popGenerator(); }
        AssistSettings.damageMultiplier = 5;
        Random.pushGenerator(100);
        try { assertEquals(original * 5, Dungeon.hero.damageRoll()); }
        finally { Random.popGenerator(); }
    }
    @Test public void actualHungerIsBlockedAndResumesAfterDisable() {
        Hunger hunger = new Hunger();
        assertTrue(hunger.attachTo(Dungeon.hero));
        AssistSettings.noHunger = true;
        hunger.affectHunger(-100);
        assertEquals(0, hunger.hunger());
        assertTrue(hunger.act());
        assertEquals(0, hunger.hunger());
        AssistSettings.noHunger = false;
        hunger.affectHunger(-20);
        assertEquals(20, hunger.hunger());
    }
    @Test public void actualHeroRejectsLethalDamageWhenEnabled() {
        Hero hero = Dungeon.hero;
        AssistSettings.invulnerable = true;
        int hp = hero.HP;
        hero.damage(Integer.MAX_VALUE, this);
        assertEquals(hp, hero.HP);
    }
}
