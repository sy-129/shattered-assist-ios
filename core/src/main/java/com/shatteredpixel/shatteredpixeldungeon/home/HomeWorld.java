/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;

import com.watabou.utils.Bundle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Deterministic home-day simulation, separate from dungeon actors and wall-clock time. */
public final class HomeWorld {
    public static final int WIDTH=26, HEIGHT=18, CAPACITY=24;
    public int day=1, grain=40, wood=30, seeds=12, affection=20, houseLevel=1;
    public boolean married=false;
    public int birthDue=-1, chatDay=-1, giftDay=-1, careDay=-1;
    public int playerX=13, playerY=9, ridingId=-1, nextId=1;
    public long randomState=777;
    public final int[] cropAge=new int[12], cropWater=new int[12], grass=new int[WIDTH*HEIGHT];
    public final List<Animal> animals=new ArrayList<>();
    public final List<Child> children=new ArrayList<>();
    public String events="欢迎来到独立家园。主人公与伴侣均为成年人。";
    public static final class Animal {
        public int id, species, age, food=85, water=85, health=100, bond=20, due=-1, x, y;
        public boolean female;
        public Animal(int id,int species,boolean female,int x,int y) {
            this.id=id; this.species=species; this.female=female; this.x=x; this.y=y;
        }
        public int adultAge() { return species==0?6:species==1?3:4; }
        public boolean adult() { return age>=adultAge(); }
        public String name() { return new String[]{"马","鸡","兔"}[species]+"#"+id; }
        int[] data() { return new int[]{id,species,age,food,water,health,bond,due,x,y,female?1:0}; }
    }
    public static final class Child {
        public int age=0, happiness=80;
        public String stage() { return age<3?"婴儿":age<12?"儿童":age<18?"少年":"成年子女"; }
    }
    public HomeWorld() {
        java.util.Arrays.fill(cropAge,-1);
        java.util.Arrays.fill(grass,70);
        for (int species=0;species<3;species++) for(int sex=0;sex<2;sex++) {
            Animal a=new Animal(nextId++,species,sex==1,17+species,6+sex);
            a.age=species==0?12:6; a.bond=species==0?50:20; animals.add(a);
        }
    }
    public String season() { return new String[]{"春","夏","秋","冬"}[((day-1)/12)%4]; }
    public boolean rain() { return day%5==0 || (season().equals("春") && day%3==0); }
    private int next(int bound) {
        randomState=randomState*6364136223846793005L+1442695040888963407L;
        long remainder=randomState%bound;
        return (int)(remainder<0?remainder+bound:remainder);
    }
    public static boolean passable(int x,int y) {
        if(x<1 || y<1 || x>=WIDTH-1 || y>=HEIGHT-1) return false;
        if(x>=20 && x<=23 && y>=11 && y<=15) return false; // pond
        if(x>=5 && x<=8 && y>=2 && y<=5) return false; // house
        if(x>=17 && x<=20 && y>=2 && y<=4) return false; // stable
        return !(x==13 && y==7); // well
    }
    public boolean move(int dx,int dy) {
        dx=Integer.signum(dx);dy=Integer.signum(dy);
        if(dx==0 && dy==0) return false;
        if(!passable(playerX+dx,playerY+dy)) return false;
        if(dx!=0 && dy!=0 && (!passable(playerX+dx,playerY)||!passable(playerX,playerY+dy))) return false;
        playerX+=dx;playerY+=dy;return true;
    }
    public Animal animal(int id) { for(Animal a:animals) if(a.id==id) return a; return null; }
    public boolean riding() {
        Animal a=animal(ridingId);
        return a!=null && a.species==0 && a.adult() && a.health>=30 && a.food>=20 && a.water>=20;
    }
    public String mount(int id) {
        Animal a=animal(id);
        if(a==null || a.species!=0 || !a.adult() || a.bond<40 || a.health<30 || a.food<20 || a.water<20)
            return "需要成年、健康、饱食饮水且亲密度至少40的马。";
        ridingId=id;return "已骑上"+a.name()+"，家园移动加快，地牢移动速度提高25%。";
    }
    public String action(String command) {
        switch(command) {
            case "talk":
                if(chatDay==day)return "今天已经和林岚交谈过，明天再来。";
                chatDay=day;affection=Math.min(100,affection+8);return "与成年伴侣林岚交谈，关系+8。";
            case "gift":
                if(giftDay==day)return "今天已经送过礼物。";
                if(grain<5)return "赠送礼物需要5份粮食。";
                grain-=5;giftDay=day;affection=Math.min(100,affection+12);return "送出家园礼物，关系+12。";
            case "marry":
                if(married)return "你们已经结婚。";
                if(affection<60)return "求婚需要关系达到60。";
                married=true;return "你与成年伴侣林岚举行了婚礼，开始共同生活。";
            case "child":
                if(!married || affection<80)return "计划孩子需要已婚且关系至少80。";
                if(birthDue>=0)return "家庭正在迎接新成员，请等待家园日推进。";
                if(children.size()>=houseLevel)return "房舍空间不足，先扩建房舍。";
                if(grain<20)return "迎接孩子需要准备20份粮食。";
                grain-=20;birthDue=day+3;return "家庭计划已确定，三个家园日后迎接新成员。";
            case "care":
                if(children.isEmpty())return "家里暂时没有孩子。";
                if(careDay==day)return "今天已经照顾过孩子。";
                if(grain<2*children.size())return "照顾孩子需要每位2份粮食。";
                grain-=2*children.size();careDay=day;
                for(Child c:children)c.happiness=Math.min(100,c.happiness+20);
                return "一起吃饭、玩耍与学习，孩子心情提升。";
            case "upgrade":
                if(houseLevel>=3)return "房舍已经达到三级。";
                if(grain<20 || wood<10)return "扩建需要20份粮食和10份木材。";
                grain-=20;wood-=10;houseLevel++;return "房舍扩建到"+houseLevel+"级。";
            case "feed":
                int cost=animals.size()*2;
                if(grain<cost)return "喂养全部动物需要"+cost+"份粮食。";
                grain-=cost;for(Animal a:animals){a.food=Math.min(100,a.food+45);a.bond=Math.min(100,a.bond+5);}
                return "已喂养所有动物。";
            case "water":
                for(Animal a:animals)a.water=100;
                for(int i=0;i<cropWater.length;i++)cropWater[i]=100;
                return "从水井取水，动物和农田已补水。";
            case "groom":
                if(grain<2)return "照料坐骑需要2份粮食。";
                grain-=2;for(Animal a:animals)if(a.species==0){a.bond=Math.min(100,a.bond+10);a.health=Math.min(100,a.health+10);}
                return "清理马厩并梳理马匹，健康与亲密度提升。";
            case "mount":
                if(riding()){ridingId=-1;return "已下马。";}
                for(Animal a:animals)if(a.species==0 && a.adult() && a.bond>=40 && a.health>=30 && a.food>=20 && a.water>=20)return mount(a.id);
                return "暂时没有可骑乘的成年马，请喂养、饮水和照料。";
            case "sow":
                int planted=0;
                for(int i=0;i<cropAge.length && seeds>0;i++)if(cropAge[i]<0){cropAge[i]=0;cropWater[i]=80;seeds--;planted++;}
                return "播种了"+planted+"块农田，浇水后推进三个生长日可收获。";
            case "harvest":
                int harvested=0;
                for(int i=0;i<cropAge.length;i++)if(cropAge[i]>=3){cropAge[i]=-1;grain+=6;seeds+=2;harvested++;}
                return "收获"+harvested+"块农田，每块获得6份粮食和2份种子。";
            case "wood":wood+=5;return "整理林地获得5份木材。";
            case "rest":return advanceDay();
            default:return "未知家园操作。";
        }
    }
    public String advanceDay() {
        List<String> news=new ArrayList<>();
        int previous=day;day++;
        for(Child c:children){c.age++;c.happiness=Math.max(0,c.happiness-(careDay==previous?0:3));if(c.age==3||c.age==12||c.age==18)news.add("孩子进入"+c.stage()+"阶段。");if(c.age>=18)grain+=2;}
        if(birthDue>=0 && day>=birthDue){children.add(new Child());birthDue=-1;news.add("家庭迎来一位新生儿。");}
        for(int i=0;i<cropAge.length;i++){
            if(rain())cropWater[i]=100;
            if(cropAge[i]>=0 && cropAge[i]<3 && cropWater[i]>=25 && !season().equals("冬"))cropAge[i]++;
            cropWater[i]=Math.max(0,cropWater[i]-25);
        }
        int regrow=season().equals("冬")?0:rain()?12:5;
        for(int i=0;i<grass.length;i++)grass[i]=Math.min(100,grass[i]+regrow);
        List<Animal> newborns=new ArrayList<>();
        for(Animal a:animals){
            a.age++;a.food=Math.max(0,a.food-18);a.water=Math.max(0,a.water-18);
            if(rain())a.water=100;
            int cell=a.x+a.y*WIDTH;
            if(grass[cell]>=15){grass[cell]-=15;a.food=Math.min(100,a.food+20);}
            if(a.x>=18 && a.y>=9)a.water=Math.min(100,a.water+30);
            if(a.food<15||a.water<15)a.health-=18;else a.health=Math.min(100,a.health+3);
            if(a.species==1 && a.adult() && a.female && a.food>=40 && a.health>=50)grain++;
            if(a.due>=0 && day>=a.due){
                if(animals.size()+newborns.size()<CAPACITY && a.health>=30){newborns.add(new Animal(nextId++,a.species,next(2)==1,a.x,a.y));news.add(a.name()+"诞下幼崽。");}
                a.due=-1;
            }
            if(a.female && a.adult() && a.due<0 && a.food>=55 && a.water>=40 && a.health>=60 && animals.size()+newborns.size()<CAPACITY){
                boolean mate=false;for(Animal male:animals)if(male.species==a.species&&!male.female&&male.adult()&&male.health>=60&&male.food>=55){mate=true;break;}
                if(mate)a.due=day+(a.species==0?5:3);
            }
            int nx=a.x+next(3)-1,ny=a.y+next(3)-1;
            if(passable(nx,ny) && nx>=14 && ny>=5){a.x=nx;a.y=ny;}
        }
        for(Iterator<Animal> it=animals.iterator();it.hasNext();){Animal a=it.next();int life=a.species==0?160:a.species==1?60:75;if(a.health<=0||a.age>life){news.add(a.name()+"离开了家园生态。");it.remove();}}
        animals.addAll(newborns);
        if(!riding())ridingId=-1;
        if(news.isEmpty())news.add("作物生长、草地恢复，动物完成一天的觅食和饮水。");
        StringBuilder text=new StringBuilder();
        for(String item:news){if(text.length()>0)text.append('\n');text.append(item);}
        events="第"+day+"日 · "+season()+"季 · "+(rain()?"雨天":"晴天")+"\n"+text;
        return events;
    }
    public String summary() {
        StringBuilder text=new StringBuilder("第"+day+"日 / "+season()+"季 / "+(rain()?"雨":"晴")+"\n粮食 "+grain+"  木材 "+wood+"  种子 "+seeds+"\n成年伴侣林岚：关系 "+affection+" / "+(married?"已婚":"未婚")+"，房舍"+houseLevel+"级\n");
        if(birthDue>=0)text.append("新成员将在第").append(birthDue).append("日到来。\n");
        for(int i=0;i<children.size();i++){Child c=children.get(i);text.append("孩子").append(i+1).append("：").append(c.stage()).append(" / 成长").append(c.age).append("日 / 心情").append(c.happiness).append("\n");}
        text.append("动物 ").append(animals.size()).append("/").append(CAPACITY).append(riding()?" · 正在骑乘":" · 未骑乘").append("\n");
        for(Animal a:animals)text.append(a.name()).append(a.female?"♀":"♂").append(a.adult()?"成年":"幼年").append(" 食").append(a.food).append(" 水").append(a.water).append(" 健康").append(a.health).append(a.due>=0?" 待产":"").append("\n");
        return text.append("\n最近事件：\n").append(events).toString();
    }
    public Bundle store() {
        Bundle b=new Bundle();b.put("version",1);
        b.put("stats",new int[]{day,grain,wood,seeds,affection,houseLevel,married?1:0,birthDue,chatDay,giftDay,careDay,playerX,playerY,ridingId,nextId});
        b.put("rng",randomState);b.put("events",events);b.put("crops",cropAge);b.put("water",cropWater);b.put("grass",grass);
        b.put("animals",animals.size());for(int i=0;i<animals.size();i++)b.put("animal"+i,animals.get(i).data());
        b.put("children",children.size());for(int i=0;i<children.size();i++)b.put("child"+i,new int[]{children.get(i).age,children.get(i).happiness});return b;
    }
    public static HomeWorld restore(Bundle b) {
        if(b.getInt("version")!=1)throw new IllegalArgumentException("不支持的家园存档版本");
        int[] s=b.getIntArray("stats"),c=b.getIntArray("crops"),w=b.getIntArray("water"),g=b.getIntArray("grass");
        if(s==null||s.length!=15||c==null||c.length!=12||w==null||w.length!=12||g==null||g.length!=WIDTH*HEIGHT)throw new IllegalArgumentException("家园存档字段损坏");
        HomeWorld h=new HomeWorld();h.day=s[0];h.grain=s[1];h.wood=s[2];h.seeds=s[3];h.affection=s[4];h.houseLevel=s[5];h.married=s[6]==1;h.birthDue=s[7];h.chatDay=s[8];h.giftDay=s[9];h.careDay=s[10];h.playerX=s[11];h.playerY=s[12];h.ridingId=s[13];h.nextId=s[14];
        if(h.day<1||h.grain<0||h.wood<0||h.seeds<0||h.affection<0||h.affection>100||h.houseLevel<1||h.houseLevel>3||!passable(h.playerX,h.playerY))throw new IllegalArgumentException("家园存档数值无效");
        for(int i=0;i<12;i++)if(c[i]<-1||c[i]>3||w[i]<0||w[i]>100)throw new IllegalArgumentException("农田数据损坏");
        System.arraycopy(c,0,h.cropAge,0,12);System.arraycopy(w,0,h.cropWater,0,12);System.arraycopy(g,0,h.grass,0,g.length);
        h.randomState=b.getLong("rng");h.events=b.getString("events");h.animals.clear();h.children.clear();
        int ac=b.getInt("animals"),cc=b.getInt("children");if(ac<0||ac>CAPACITY||cc<0||cc>3)throw new IllegalArgumentException("家园人口数据损坏");
        java.util.HashSet<Integer> ids=new java.util.HashSet<>();
        for(int i=0;i<ac;i++){int[] a=b.getIntArray("animal"+i);if(a==null||a.length!=11||a[1]<0||a[1]>2||!passable(a[8],a[9])||!ids.add(a[0])||a[0]<=0)throw new IllegalArgumentException("动物数据损坏");Animal n=new Animal(a[0],a[1],a[10]==1,a[8],a[9]);n.age=a[2];n.food=a[3];n.water=a[4];n.health=a[5];n.bond=a[6];n.due=a[7];if(n.age<0||n.food<0||n.food>100||n.water<0||n.water>100||n.health<1||n.health>100||n.bond<0||n.bond>100||n.id>=h.nextId)throw new IllegalArgumentException("动物状态损坏");h.animals.add(n);}
        for(int i=0;i<cc;i++){int[] d=b.getIntArray("child"+i);if(d==null||d.length!=2||d[0]<0||d[1]<0||d[1]>100)throw new IllegalArgumentException("家庭数据损坏");Child n=new Child();n.age=d[0];n.happiness=d[1];h.children.add(n);}
        if(!h.riding())h.ridingId=-1;return h;
    }
}
