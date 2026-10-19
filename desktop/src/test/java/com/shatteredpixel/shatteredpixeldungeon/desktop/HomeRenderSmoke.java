/* Host-only graphics smoke check. Not compiled into the iOS application. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.desktop;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.*;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.ScreenUtils;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.home.*;
import com.shatteredpixel.shatteredpixeldungeon.messages.Languages;
import com.watabou.noosa.Game;
import com.watabou.utils.FileUtils;
import java.lang.reflect.Method;

public class HomeRenderSmoke {
    public static void main(String[] args) throws Exception {
        String output=System.getProperty("assist.preview.output",new java.io.File("build/render-check").getAbsolutePath());
        new java.io.File(output).mkdirs();
        String saves=java.nio.file.Files.createTempDirectory(new java.io.File(output).toPath(),"isolated-saves-").toString()+"/";
        Lwjgl3ApplicationConfiguration config=new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280,589);config.setTitle("Shattered Assist graphics verification");
        config.disableAudio(true);config.setForegroundFPS(60);
        config.setPreferencesConfig(saves,Files.FileType.Absolute);
        Game.version="4.0.2-assist.3";Game.versionCode=924;
        new Lwjgl3Application(new ShatteredPixelDungeon(new DesktopPlatformSupport()) {
            int frames, hpBefore, positionBefore, homeX;
            com.shatteredpixel.shatteredpixeldungeon.ui.Window assistMenu;
            float buttonStart;
            private Object field(Class<?> type,Object owner,String name) {try{java.lang.reflect.Field f=type.getDeclaredField(name);f.setAccessible(true);return f.get(owner);}catch(Exception e){throw new RuntimeException(e);}}
            private void pointer(com.watabou.noosa.ui.Component control,int id,float x,float y,com.watabou.input.PointerEvent.Type type) {
                com.watabou.utils.Point p=control.camera().cameraToScreen(control.left()+control.width()*x,control.top()+control.height()*y);
                com.watabou.input.PointerEvent.addPointerEvent(new com.watabou.input.PointerEvent(p.x,p.y,id,type,com.watabou.input.PointerEvent.LEFT));
            }
            @Override public void create(){
                SPDSettings.set(Gdx.app.getPreferences("render-check"));
                SPDSettings.language(Languages.CHI_SMPL);SPDSettings.interfaceSize(0);SPDSettings.scale(0);
                SPDSettings.windowResolution(new com.watabou.utils.Point(1280,589));
                SPDSettings.put(SPDSettings.KEY_FULLSCREEN,false);
                SPDSettings.music(false);SPDSettings.soundFx(false);
                FileUtils.setDefaultFileProperties(Files.FileType.Absolute,saves);
                super.create();HomeService.world=new HomeWorld();HomeService.returnToDungeon=false;
                Game.switchScene(HomeScene.class);
            }
            private void capture(String name){Pixmap p=ScreenUtils.getFrameBufferPixmap(0,0,Gdx.graphics.getBackBufferWidth(),Gdx.graphics.getBackBufferHeight());PixmapIO.writePNG(Gdx.files.absolute(output+"/"+name),p,-1,true);p.dispose();}
            private void invoke(String name){try{Method m=HomeScene.class.getDeclaredMethod(name);m.setAccessible(true);m.invoke(Game.scene());}catch(Exception e){throw new RuntimeException(e);}}
            @Override public void render(){super.render();frames++;
                if(frames==60){homeX=HomeService.world.playerX;pointer((com.watabou.noosa.ui.Component)field(HomeScene.class,Game.scene(),"joystick"),9001,0.9f,0.5f,com.watabou.input.PointerEvent.Type.DOWN);}
                if(frames==70){int before=HomeService.world.playerX;try{Method m=HomeScene.class.getDeclaredMethod("direction",int.class,int.class);m.setAccessible(true);m.invoke(Game.scene(),1,0);}catch(Exception e){throw new RuntimeException(e);}if(HomeService.world.playerX!=before)throw new AssertionError("Repeated direction bypassed movement timer");}
                if(frames==80)pointer((com.watabou.noosa.ui.Component)field(HomeScene.class,Game.scene(),"joystick"),9001,0.9f,0.5f,com.watabou.input.PointerEvent.Type.UP);
                if(frames==90 && HomeService.world.playerX<=homeX)throw new AssertionError("Actual joystick pointer did not move home character");
                if(frames==120){capture("ShatteredAssist-home-render.png");
                    HomeWorld w=HomeService.world;for(int i=0;i<3;i++){w.action("talk");w.action("gift");w.action("rest");}w.action("marry");w.action("child");for(int i=0;i<3;i++)w.action("rest");w.action("mount");invoke("refreshEntities");invoke("updateHeader");}
                if(frames==180){capture("ShatteredAssist-family-render.png");invoke("family");}
                if(frames==240){capture("ShatteredAssist-home-menu-render.png");
                    com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.curSlot=999;
                    com.shatteredpixel.shatteredpixeldungeon.GamesInProgress.selectedClass=com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass.WARRIOR;
                    SPDSettings.intro(false);SPDSettings.zoom(2);
                    com.shatteredpixel.shatteredpixeldungeon.Dungeon.seed=12345;
                    com.shatteredpixel.shatteredpixeldungeon.Dungeon.init();
                    com.shatteredpixel.shatteredpixeldungeon.levels.Level level=com.shatteredpixel.shatteredpixeldungeon.Dungeon.newLevel();
                    com.shatteredpixel.shatteredpixeldungeon.Dungeon.switchLevel(level,level.entrance());
                    com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.mode=com.shatteredpixel.shatteredpixeldungeon.scenes.InterlevelScene.Mode.NONE;
                    Game.switchScene(com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.class);
                }
                if(frames==300){com.shatteredpixel.shatteredpixeldungeon.ui.AssistButton b=(com.shatteredpixel.shatteredpixeldungeon.ui.AssistButton)field(com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.class,Game.scene(),"assistButton");buttonStart=b.left();pointer(b,9002,0.5f,0.5f,com.watabou.input.PointerEvent.Type.DOWN);}
                if(frames==310)pointer((com.watabou.noosa.ui.Component)field(com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.class,Game.scene(),"assistButton"),9002,3.0f,0.5f,com.watabou.input.PointerEvent.Type.DOWN);
                if(frames==320)pointer((com.watabou.noosa.ui.Component)field(com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.class,Game.scene(),"assistButton"),9002,0.5f,0.5f,com.watabou.input.PointerEvent.Type.UP);
                if(frames==330){com.shatteredpixel.shatteredpixeldungeon.ui.AssistButton b=(com.shatteredpixel.shatteredpixeldungeon.ui.AssistButton)field(com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.class,Game.scene(),"assistButton");if(b.left()<=buttonStart || com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.showingWindow())throw new AssertionError("Drag failed or opened settings");com.shatteredpixel.shatteredpixeldungeon.assist.AssistSettings.buttonHidden=true;com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.refreshAssistControls();if(b.visible)throw new AssertionError("Hide failed");com.shatteredpixel.shatteredpixeldungeon.assist.AssistSettings.buttonHidden=false;com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.refreshAssistControls();}
                if(frames==360){capture("ShatteredAssist-dungeon-controls-render.png");assistMenu=new com.shatteredpixel.shatteredpixeldungeon.windows.WndAssist();com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene.show(assistMenu);}
                if(frames==420){capture("ShatteredAssist-assist-menu-render.png");assistMenu.hide();hpBefore=com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.HP;positionBefore=com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.pos;try{HomeService.open(true);}catch(Exception e){throw new RuntimeException(e);}}
                if(frames==540){if(!(Game.scene() instanceof HomeScene))throw new AssertionError("Dungeon to home failed");HomeService.world.action("mount");invoke("leave");}
                if(frames==660){if(!(Game.scene() instanceof com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene))throw new AssertionError("Home return failed");if(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.HP!=hpBefore || com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.pos!=positionBefore)throw new AssertionError("Dungeon hero state changed in home");if(!HomeService.riding())throw new AssertionError("Mount state lost on dungeon return");int id=HomeService.world.ridingId;HomeService.world.ridingId=-1;float speed=com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.speed();HomeService.world.ridingId=id;if(Math.abs(com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero.speed()-speed*1.25f)>0.001f)throw new AssertionError("Actual hero riding speed hook failed");capture("ShatteredAssist-mounted-dungeon-render.png");System.out.println("PASS: real pointer joystick, button drag/hide, home/dungeon transitions, preserved hero state, 25% riding speed, and scene rendering");Gdx.app.exit();}
            }
        },config);
    }
}
