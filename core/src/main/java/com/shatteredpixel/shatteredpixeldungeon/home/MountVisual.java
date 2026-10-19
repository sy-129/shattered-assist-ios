/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.watabou.noosa.Game;
import com.watabou.noosa.Image;

/** Follows the existing hero sprite, without replacing collision or combat logic. */
public final class MountVisual extends Image {
    private final HeroSprite hero;
    private float time;
    public MountVisual(HeroSprite hero){super("home/superpowers/horse.png");this.hero=hero;frame(0,0,63,66);scale.set(0.28f);alpha(0.85f);}
    @Override public void update(){
        super.update();visible=HomeService.riding()&&hero.visible;
        if(!visible)return;
        time+=Game.elapsed;
        frame((hero.isMoving?(int)(time*10)%4:0)*63,0,63,66);
        x=hero.x-1;y=hero.y+hero.height()-height()+3;flipHorizontal=hero.flipHorizontal;
    }
}
