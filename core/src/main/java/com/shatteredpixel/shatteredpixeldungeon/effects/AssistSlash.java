/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.effects;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTilemap;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.watabou.noosa.MovieClip;
import com.watabou.noosa.TextureFilm;
import com.watabou.utils.PointF;

/** CC0 five-frame slash; cosmetic only, never advances combat callbacks. */
public final class AssistSlash extends MovieClip {
    public AssistSlash(CharSprite owner,int target) {
        super("home/superpowers/slash.png");
        TextureFilm film=new TextureFilm(texture,13,13);
        PointF to=DungeonTilemap.tileCenterToWorld(target);
        PointF from=owner.center();
        scale.set(2f);origin.set(6.5f);
        x=(from.x+to.x)/2-13;y=(from.y+to.y)/2-13;
        angle=(float)(Math.atan2(to.y-from.y,to.x-from.x)*180/Math.PI)-45;
        play(new Animation(24,false).frames(film,0,1,2,3,4));
    }
    @Override public void update(){super.update();if(finished)killAndErase();}
}
