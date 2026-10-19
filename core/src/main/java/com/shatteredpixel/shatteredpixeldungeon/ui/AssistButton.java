/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.assist.AssistSettings;
import com.shatteredpixel.shatteredpixeldungeon.assist.TouchStickState;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndAssist;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Game;
import com.watabou.noosa.PointerArea;
import com.watabou.utils.PlatformSupport;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;

/** Draggable game-owned button; the game menu remains a recovery entry. */
public class AssistButton extends RedButton {
    private int pointer = -1;
    private float startX, startY, originX, originY;
    private boolean dragged;
    public AssistButton() {
        super(Messages.get(WndAssist.class, "button"), 9);
        remove(hotArea);
        hotArea.destroy();
        hotArea = new PointerArea(0, 0, 0, 0) {
            @Override public boolean onSignal(PointerEvent event) {
                if (event != null && pointer != -1 && event.id != pointer) return false;
                return super.onSignal(event);
            }
            @Override protected void onPointerDown(PointerEvent e) {
                pointer = e.id;
                PointF p = camera().screenToCamera((int)e.current.x, (int)e.current.y);
                startX = p.x; startY = p.y; originX = left(); originY = top(); dragged = false;
                AssistButton.this.onPointerDown();
            }
            @Override protected void onDrag(PointerEvent e) {
                PointF p = camera().screenToCamera((int)e.current.x, (int)e.current.y);
                if (Math.hypot(p.x-startX, p.y-startY) > 3) dragged = true;
                if (dragged) {
                    setClamped(originX+p.x-startX, originY+p.y-startY);
                }
            }
            @Override protected void onPointerUp(PointerEvent e) {
                if (e.id != pointer) return;
                AssistButton.this.onPointerUp();
                if (dragged) savePosition();
                pointer = -1;
            }
            @Override protected void onClick(PointerEvent e) {
                if (!dragged) AssistButton.this.onClick();
            }
        };
        add(hotArea);
    }
    private RectF bounds() {
        RectF i = Game.platform.getSafeInsets(PlatformSupport.INSET_ALL).scale(1f/PixelScene.defaultZoom);
        return new RectF(i.left+3, i.top+3, camera().width-i.right-width()-3, camera().height-i.bottom-height()-3);
    }
    public void restorePosition() {
        RectF b = bounds();
        setClamped(b.left+(b.right-b.left)*AssistSettings.buttonX, b.top+(b.bottom-b.top)*AssistSettings.buttonY);
    }
    private void setClamped(float x, float y) {
        RectF b = bounds();
        setPos(TouchStickState.clamp(x,b.left,b.right), TouchStickState.clamp(y,b.top,b.bottom));
    }
    private void savePosition() {
        RectF b = bounds();
        AssistSettings.buttonX = (left()-b.left)/Math.max(1,b.right-b.left);
        AssistSettings.buttonY = (top()-b.top)/Math.max(1,b.bottom-b.top);
        AssistSettings.save();
    }
    @Override protected void onClick() {
        if (Dungeon.hero != null && Dungeon.hero.isAlive() && Dungeon.hero.ready
                && !GameScene.showingWindow()) GameScene.show(new WndAssist());
    }
    public void cancelDrag() { pointer=-1; hotArea.reset(); onPointerUp(); }
}
