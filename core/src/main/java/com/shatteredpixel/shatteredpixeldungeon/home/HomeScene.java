/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.assist.TouchStickState;
import com.shatteredpixel.shatteredpixeldungeon.scenes.*;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndMessage;
import com.watabou.gltextures.TextureCache;
import com.watabou.input.PointerEvent;
import com.watabou.noosa.*;
import com.watabou.utils.PointF;
import com.watabou.utils.RectF;
import java.io.IOException;
import java.util.ArrayList;

/** Independent explorable home: no dungeon actors or enemy AI run here. */
public class HomeScene extends PixelScene {
    private static final String ART="home/superpowers/";
    private HomeWorld world;
    private Group entities;
    private Image player,mount;
    private RenderedTextBlock header;
    private TouchJoystick joystick;
    private int dx,dy;
    private float repeat,clock;
    private final ArrayList<Image> animalImages=new ArrayList<>();
    @Override public void create() {
        super.create();
        world=HomeService.world;
        if(world==null){ShatteredPixelDungeon.switchNoFade(TitleScene.class);return;}
        Camera.main.zoom(defaultZoom+1);
        int[] tiles=new int[HomeWorld.WIDTH*HomeWorld.HEIGHT];
        for(int y=0;y<HomeWorld.HEIGHT;y++)for(int x=0;x<HomeWorld.WIDTH;x++) {
            int tile=21; // interior grass tile from upstream 16px atlas
            if(x==0||y==0||x==HomeWorld.WIDTH-1||y==HomeWorld.HEIGHT-1)tile=9;
            if(x==12||x==13||y==8||y==9)tile=29;
            if(x>=20&&x<=23&&y>=11&&y<=15)tile=37;
            if(x>=2&&x<=7&&y>=12&&y<=13)tile=29;
            tiles[x+y*HomeWorld.WIDTH]=tile;
        }
        Tilemap map=new Tilemap(ART+"tiles.png",new TextureFilm(ART+"tiles.png",16,16));
        map.map(tiles,HomeWorld.WIDTH);add(map);
        addProp("house.png",5*16,2*16,2.1f);
        addProp("stable.png",17*16,2*16,3.0f);
        addProp("well.png",12.5f*16,5.8f*16,1.0f);
        for(int x=2;x<24;x+=4)addProp("tree.png",x*16,15*16,1.6f);
        label("住宅",7*16,6*16);label("牧场与马厩",17*16,5*16);label("农田",2*16,11*16);label("水井",12*16,7.5f*16);
        entities=new Group();add(entities);refreshEntities();
        PointerArea ground=new PointerArea(0,0,Game.width,Game.height){
            @Override protected void onClick(PointerEvent e){
                if(blocked())return;
                PointF p=Camera.main.screenToCamera((int)e.current.x,(int)e.current.y);
                int tx=(int)(p.x/16),ty=(int)(p.y/16);
                if(Math.abs(tx-world.playerX)<=1&&Math.abs(ty-world.playerY)<=1){
                    if(world.move(Integer.signum(tx-world.playerX),Integer.signum(ty-world.playerY)))updatePlayer();
                } else if(Math.hypot(p.x/16-world.playerX,p.y/16-world.playerY)<3)interact();
            }
        };
        ground.camera=uiCamera;add(ground);
        RectF insets=getCommonInsets();
        header=PixelScene.renderTextBlock("",7);header.camera=uiCamera;
        header.maxWidth((int)uiCamera.width-90);header.setPos(insets.left+3,insets.top+3);add(header);
        RedButton exit=new RedButton(HomeService.returnToDungeon?"返回地牢":"返回首页",8){@Override protected void onClick(){leave();}};
        exit.camera=uiCamera;exit.setRect(uiCamera.width-insets.right-62,insets.top+3,59,22);add(exit);
        float bottom=uiCamera.height-insets.bottom-25;
        RedButton manage=new RedButton("家园管理",8){@Override protected void onClick(){management();}};
        manage.camera=uiCamera;manage.setRect(uiCamera.width-insets.right-174,bottom,58,22);add(manage);
        RedButton interact=new RedButton("附近互动",8){@Override protected void onClick(){interact();}};
        interact.camera=uiCamera;interact.setRect(manage.right()+2,bottom,54,22);add(interact);
        RedButton rest=new RedButton("休息一天",8){@Override protected void onClick(){act("rest");}};
        rest.camera=uiCamera;rest.setRect(interact.right()+2,bottom,54,22);add(rest);
        joystick=new TouchJoystick(this::direction,this::blocked);joystick.camera=uiCamera;
        joystick.setRect(insets.left+6,bottom-40,56,56);add(joystick);
        updateHeader();updatePlayer();
        if(!HomeService.recoveryNotice.isEmpty())addToFront(new WndMessage(HomeService.recoveryNotice));
        fadeIn();
    }
    private void addProp(String file,float x,float y,float scale){Image image=new Image(ART+file);image.scale.set(scale);image.x=x;image.y=y;add(image);}
    private void label(String value,float x,float y){RenderedTextBlock t=PixelScene.renderTextBlock(value,6);t.setPos(x,y);add(t);}
    private boolean blocked(){for(Gizmo g:members)if(g instanceof Window)return true;return false;}
    private void refreshEntities(){
        Group old=entities; entities=new Group(); replace(old,entities); old.destroy();
        animalImages.clear();
        Image partner=new Image(ART+"partner.png");partner.scale.set(1.6f);partner.x=10*16;partner.y=6*16;entities.add(partner);
        for(int i=0;i<world.children.size();i++){
            Image child=new Image(ART+"partner.png");child.scale.set(world.children.get(i).age<3?0.8f:1.2f);child.x=(9+i)*16;child.y=7*16;entities.add(child);
        }
        for(HomeWorld.Animal a:world.animals){
            Image image=new Image(ART+(a.species==0?"horse.png":a.species==1?"chicken.png":"rabbit.png"));
            if(a.species==0){image.frame(0,0,63,66);image.scale.set(a.adult()?0.34f:0.22f);}
            else image.scale.set(a.adult()?1.2f:0.8f);
            image.x=a.x*16;image.y=a.y*16;entities.add(image);animalImages.add(image);
        }
        for(int i=0;i<12;i++)if(world.cropAge[i]>=0){
            ColorBlock crop=new ColorBlock(7+world.cropAge[i],5+world.cropAge[i]*2,world.cropAge[i]>=3?0xFFE8BB44:0xFF63B750);
            crop.x=(2+i%6)*16+3;crop.y=(12+i/6)*16+4;entities.add(crop);
        }
        mount=new Image(ART+"horse.png");mount.frame(0,0,63,66);mount.scale.set(0.34f);entities.add(mount);
        player=new Image(ART+"player.png");player.scale.set(1.6f);entities.add(player);updatePlayer();
    }
    private void updateHeader(){header.text("独立家园 · 第"+world.day+"日 · "+world.season()+"季"+(world.rain()?"／雨":"／晴")+"\n粮食"+world.grain+"  种子"+world.seeds+"  动物"+world.animals.size()+"  "+(world.riding()?"骑乘中":"步行"));}
    private void updatePlayer(){
        if(player==null)return;
        player.x=world.playerX*16;player.y=world.playerY*16-(world.riding()?8:0);
        mount.visible=world.riding();mount.x=world.playerX*16-3;mount.y=world.playerY*16;
        float cx=TouchStickState.clamp(world.playerX*16+8,Camera.main.width/2f,HomeWorld.WIDTH*16-Camera.main.width/2f);
        float cy=TouchStickState.clamp(world.playerY*16+8,Camera.main.height/2f,HomeWorld.HEIGHT*16-Camera.main.height/2f);
        Camera.main.snapTo(cx,cy);
    }
    private void direction(int dx,int dy){
        this.dx=dx;this.dy=dy;repeat=0;
        if(dx==0&&dy==0){saveQuietly();return;}
        step();
    }
    private void step(){if(!blocked()&&world.move(dx,dy)){updatePlayer();repeat=world.riding()?0.10f:0.18f;}}
    @Override public void update(){
        super.update();if(world==null)return;clock+=Game.elapsed;
        if(blocked()){dx=dy=0;}else if(dx!=0||dy!=0){repeat-=Game.elapsed;if(repeat<=0)step();}
        for(int i=0;i<animalImages.size()&&i<world.animals.size();i++){
            HomeWorld.Animal a=world.animals.get(i);Image image=animalImages.get(i);
            image.y=a.y*16+(float)Math.sin(clock*2+i)*0.7f;
            if(a.species==0){int frame=(int)(clock*3+i)%4;image.frame(frame*63,0,63,66);}
        }
        if(mount!=null&&world.riding()){int frame=(dx!=0||dy!=0)?(int)(clock*9)%4:0;mount.frame(frame*63,0,63,66);}
    }
    private void act(String command){
        dx=dy=0;if(joystick!=null)joystick.cancel();
        String result=world.action(command);
        try{HomeService.save();}catch(IOException e){result+="\n存档失败："+e.getMessage();}
        refreshEntities();updateHeader();addToFront(new WndMessage(result));
    }
    private void management(){
        addToFront(new HomeWindow("家园管理",world.summary(),new String[]{"家庭与婚姻","农田、饮水与林地","动物与坐骑","家园规则"},new Runnable[]{this::family,this::farm,this::ranch,this::help}));
    }
    private void family(){menu("家庭生活","伴侣林岚为成年人。互动按家园日计算；孩子会长期成长。",new String[]{"交谈（每天一次）","赠礼（5粮食）","求婚／结婚（关系60）","计划孩子（已婚、关系80）","照顾孩子","扩建房舍（20粮10木）"},new String[]{"talk","gift","marry","child","care","upgrade"});}
    private void farm(){menu("农田与林地","12块农田，生长需要水分；冬季停止生长。",new String[]{"播种空地","收获成熟作物","从水井补水","整理林地（获得木材）"},new String[]{"sow","harvest","water","wood"});}
    private void ranch(){menu("动物与坐骑","马、鸡、兔有年龄、性别、饱食、饮水、健康和繁殖周期。成年且健康饱食的异性同种动物会繁殖，上限24只。",new String[]{"喂养全部动物（每只2粮）","补充全部饮水","照料马匹（2粮）",world.riding()?"下马":"骑乘成年马"},new String[]{"feed","water","groom","mount"});}
    private void menu(String title,String text,String[] labels,String[] commands){Runnable[] actions=new Runnable[commands.length];for(int i=0;i<commands.length;i++){final String command=commands[i];actions[i]=()->act(command);}addToFront(new HomeWindow(title,text,labels,actions));}
    private void interact(){
        if(Math.hypot(world.playerX-10,world.playerY-6)<=3||Math.hypot(world.playerX-7,world.playerY-6)<=3)family();
        else if(world.playerX>=15&&world.playerY<=10)ranch();
        else if(world.playerX<=9&&world.playerY>=10)farm();
        else addToFront(new WndMessage("靠近住宅／林岚、马厩或农田后互动；也可随时打开家园管理。"));
    }
    private void help(){addToFront(new HomeWindow("家园规则","家园日只在点击休息一天时推进，不使用真实时间。每12日换季，四季循环；雨天补水，冬季作物和草地不生长。\n\n交谈、赠礼每天各一次。关系60可结婚，已婚关系80可计划孩子，三个家园日后到来。孩子按家园日进入婴儿、儿童、少年、成年阶段，成年子女每日贡献2粮。\n\n草地被动物消耗并按季节恢复；动物会衰老，缺粮缺水会损失健康，成年异性同种配对繁殖，幼崽长大后可继续繁殖。马需成年且健康、饱食、饮水、亲密度40才能骑乘。\n\n作物生长3日成熟，每块收获6粮和2种子。家园与地牢分别保存，每个游戏槽位保留一份家园及备份。",new String[0],new Runnable[0]));}
    private void saveQuietly(){try{HomeService.save();}catch(IOException e){addToFront(new WndMessage("家园存档失败："+e.getMessage()));}}
    private void leave(){
        dx=dy=0;if(joystick!=null)joystick.cancel();
        try{HomeService.save();}catch(IOException e){addToFront(new WndMessage("保存失败，尚未离开家园："+e.getMessage()));return;}
        ShatteredPixelDungeon.switchNoFade(HomeService.returnToDungeon?GameScene.class:TitleScene.class);
    }
    @Override public void onPause(){dx=dy=0;if(joystick!=null)joystick.cancel();saveQuietly();}
    @Override protected void onBackPressed(){leave();}
}
