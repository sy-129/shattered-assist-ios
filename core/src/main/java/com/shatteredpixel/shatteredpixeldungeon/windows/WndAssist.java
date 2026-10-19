/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.assist.AssistSettings;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.CheckBox;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.ui.ScrollPane;
import com.watabou.noosa.ui.Component;

public class WndAssist extends Window {
    private static final int WIDTH = 170;
    private int nextY;
    private final Component content = new Component();
    public WndAssist() {
        RenderedTextBlock title = PixelScene.renderTextBlock(Messages.get(this, "title"), 9);
        title.hardlight(TITLE_COLOR);
        title.setPos(0, 0);
        content.add(title);
        RenderedTextBlock note = PixelScene.renderTextBlock(Messages.get(this, "note"), 6);
        note.maxWidth(WIDTH);
        note.setPos(0, title.bottom() + 4);
        content.add(note);
        nextY = (int)note.bottom() + 5;
        row(new RedButton(damageLabel(), 8) {
            @Override protected void onClick() {
                int n = AssistSettings.damageMultiplier;
                AssistSettings.damageMultiplier = n == 1 ? 2 : n == 2 ? 5 : n == 5 ? 10 : 1;
                AssistSettings.save();
                text(damageLabel());
            }
        });
        CheckBox shield = new CheckBox(Messages.get(this, "shield")) {
            @Override protected void onClick() {
                super.onClick();
                AssistSettings.invulnerable = checked();
                AssistSettings.save();
            }
        };
        shield.checked(AssistSettings.invulnerable);
        row(shield);
        CheckBox food = new CheckBox(Messages.get(this, "food")) {
            @Override protected void onClick() {
                super.onClick();
                AssistSettings.noHunger = checked();
                AssistSettings.save();
            }
        };
        food.checked(AssistSettings.noHunger);
        row(food);
        row(new RedButton(Messages.get(this, "heal"), 8) {
            @Override protected void onClick() {
                if (Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.hero.ready) {
                    Dungeon.hero.HP = Dungeon.hero.HT;
                    text(Messages.get(WndAssist.class, "healed"));
                }
            }
        });
        row(new RedButton(Messages.get(this, "reset"), 8) {
            @Override protected void onClick() {
                AssistSettings.reset();
                hide();
            }
        });
        row(new RedButton(Messages.get(this, "close"), 8) {
            @Override protected void onClick() { hide(); }
        });
        content.setSize(WIDTH, nextY - 3);
        ScrollPane pane = new ScrollPane(content);
        add(pane);
        int viewportHeight = Math.min(nextY - 3, Math.max(80, (int)PixelScene.uiCamera.height - 24));
        pane.setRect(0, 0, WIDTH, viewportHeight);
        resize(WIDTH, viewportHeight);
    }
    private String damageLabel() {
        return Messages.get(WndAssist.class, "damage", AssistSettings.damageMultiplier);
    }
    private void row(RedButton button) {
        content.add(button);
        button.setRect(0, nextY, WIDTH, 20);
        nextY += 23;
    }
}
