/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAssist;

/** Owned by GameScene: never requests a system overlay or private entitlement. */
public class AssistButton extends RedButton {
    public AssistButton() { super(Messages.get(WndAssist.class, "button"), 8); }
    @Override protected void onClick() {
        if (Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.hero.ready
                && !GameScene.showingWindow()) GameScene.show(new WndAssist());
    }
    @Override protected boolean onLongClick() {
        setPos(left() < camera().width / 2f ? camera().width - width() - 4 : 4, top());
        return true;
    }
}
