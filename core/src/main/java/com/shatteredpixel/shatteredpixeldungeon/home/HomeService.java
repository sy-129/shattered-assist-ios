/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.GamesInProgress;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.Bundle;
import com.watabou.utils.FileUtils;
import java.io.IOException;

public final class HomeService {
    public static HomeWorld world;
    public static int slot=1;
    public static boolean returnToDungeon;
    public static String recoveryNotice="";
    private HomeService() {}
    public static String filename(int slot) {
        if(slot<1||slot>1000)throw new IllegalArgumentException("存档槽位无效");
        return "assist-home-v1-slot-"+slot+".dat";
    }
    public static void load(int requested) throws IOException {
        requested=Math.max(1,requested);
        if(world!=null && slot==requested)return;
        String file=filename(requested);
        HomeWorld loaded;
        recoveryNotice="";
        if(!FileUtils.fileExists(file)&&!FileUtils.fileExists(file+".bak"))loaded=new HomeWorld();
        else {
            try { loaded=HomeWorld.restore(FileUtils.bundleFromFile(file)); }
            catch(Exception first) {
                try { loaded=HomeWorld.restore(FileUtils.bundleFromFile(file+".bak"));recoveryNotice="主存档读取失败，已从家园备份恢复。"; }
                catch(Exception second) { throw new IOException("家园存档与备份均无法读取，未覆盖原文件。",first); }
            }
        }
        world=loaded;slot=requested;
    }
    public static void save() throws IOException {
        if(world==null)return;
        Bundle bundle=world.store();
        FileUtils.bundleToFile(filename(slot)+".bak",bundle);
        FileUtils.bundleToFile(filename(slot),bundle);
    }
    public static void open(boolean fromDungeon) throws IOException {
        if(fromDungeon && (Dungeon.hero==null||!Dungeon.hero.ready||!Dungeon.hero.isAlive()))throw new IOException("请等待角色可操作时再进入家园。");
        load(Math.max(1,GamesInProgress.curSlot));
        if(fromDungeon)Dungeon.saveAll();
        returnToDungeon=fromDungeon;
        ShatteredPixelDungeon.switchNoFade(HomeScene.class);
    }
    public static boolean riding() { return world!=null && slot==Math.max(1,GamesInProgress.curSlot) && world.riding(); }
}
