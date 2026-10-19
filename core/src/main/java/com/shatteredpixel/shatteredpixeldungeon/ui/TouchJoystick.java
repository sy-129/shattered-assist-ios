/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.assist.TouchStickState;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.gltextures.SmartTexture;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.Image;
import com.watabou.noosa.PointerArea;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.PointF;

/** One captured finger; gameplay still uses the original turn-based movement. */
public class TouchJoystick extends Component {
    private final TouchStickState state = new TouchStickState();
    public interface DirectionSink { void direction(int dx, int dy); }
    private final DirectionSink sink;
    private final java.util.function.BooleanSupplier blocked;
    private final Image base, thumb;
    private final PointerArea touch;
    public TouchJoystick(CellSelector selector) {
        this(selector::setTouchDirection, () -> GameScene.interfaceBlockingHero() || Dungeon.hero == null || !Dungeon.hero.isAlive());
    }
    public TouchJoystick(DirectionSink sink, java.util.function.BooleanSupplier blocked) {
        this.sink = sink;
        this.blocked = blocked;
        base = new Image(circle("assist.stick.base", false));
        thumb = new Image(circle("assist.stick.thumb", true));
        add(base);
        add(thumb);
        touch = new PointerArea(0, 0, 0, 0) {
            @Override public boolean onSignal(PointerEvent event) {
                if (event != null && state.pointer != -1 && event.id != state.pointer) return false;
                if (event != null && event.type == PointerEvent.Type.DOWN) {
                    PointF p = camera().screenToCamera((int)event.current.x, (int)event.current.y);
                    if (Math.hypot(p.x - (x + width/2), p.y - (y + height/2)) > width/2) return false;
                }
                return super.onSignal(event);
            }
            @Override protected void onPointerDown(PointerEvent event) { updateFinger(event, true); }
            @Override protected void onDrag(PointerEvent event) { updateFinger(event, false); }
            @Override protected void onPointerUp(PointerEvent event) {
                if (state.end(event.id)) { TouchJoystick.this.sink.direction(0, 0); layout(); }
            }
        };
        add(touch);
    }
    private void updateFinger(PointerEvent event, boolean down) {
        if (blocked.getAsBoolean()) return;
        PointF p = camera().screenToCamera((int)event.current.x, (int)event.current.y);
        float nx = (p.x - (x + width/2)) / (width/2);
        float ny = (p.y - (y + height/2)) / (height/2);
        boolean accepted = down ? state.begin(event.id, nx, ny) : state.move(event.id, nx, ny);
        if (accepted) { sink.direction(state.dx, state.dy); layout(); }
    }
    @Override protected void layout() {
        if (base == null) return;
        base.scale.set(width/128f);
        base.x = x; base.y = y;
        float diameter = width * 0.38f;
        thumb.scale.set(diameter/128f);
        thumb.x = x + (width-diameter)/2 + state.x * width * 0.26f;
        thumb.y = y + (height-diameter)/2 + state.y * width * 0.26f;
        touch.x = x; touch.y = y; touch.width = width; touch.height = height;
    }
    @Override public void update() {
        super.update();
        if (blocked.getAsBoolean()) cancel();
    }
    public void cancel() {
        if (state.pointer != -1) { state.cancel(); sink.direction(0, 0); }
        touch.reset();
        layout();
    }
    @Override public void destroy() { cancel(); super.destroy(); }
    private static SmartTexture circle(String key, boolean knob) {
        SmartTexture texture = TextureCache.create(key, 128, 128);
        Pixmap p = texture.bitmap;
        if (p.getPixel(64,64) == 0) {
            p.setBlending(Pixmap.Blending.None);
            p.setColor(knob ? 0xADC8D9DF : 0x14202BB8);
            p.fillCircle(64, 64, 60);
            p.setColor(knob ? 0xE5F5FFFF : 0xA8D5EECC);
            p.drawCircle(64, 64, 59);
            p.drawCircle(64, 64, 58);
            if (!knob) {
                p.fillTriangle(64, 11, 57, 24, 71, 24);
                p.fillTriangle(64, 117, 57, 104, 71, 104);
                p.fillTriangle(11, 64, 24, 57, 24, 71);
                p.fillTriangle(117, 64, 104, 57, 104, 71);
            }
        }
        return texture;
    }
}
