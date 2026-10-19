/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;
import com.watabou.utils.Bundle;
import org.junit.Test;
import java.io.*;
import static org.junit.Assert.*;
public class HomeWorldTest {
    @Test public void dailyRelationshipGatesCannotBeSpammed(){
        HomeWorld h=new HomeWorld();h.action("marry");assertFalse(h.married);
        h.action("talk");h.action("talk");assertEquals(28,h.affection);
        h.action("gift");int grain=h.grain;h.action("gift");assertEquals(grain,h.grain);
        h.advanceDay();h.action("talk");h.action("gift");h.action("marry");assertTrue(h.married);
    }
    @Test public void plannedChildArrivesOnceAndGrowsThroughStages(){
        HomeWorld h=new HomeWorld();h.affection=100;h.married=true;h.grain=1000;
        h.action("child");int due=h.birthDue;int grain=h.grain;h.action("child");assertEquals(grain,h.grain);
        while(h.day<due)h.advanceDay();assertEquals(1,h.children.size());assertEquals(0,h.children.get(0).age);
        h.action("child");assertEquals(-1,h.birthDue); // default one-room capacity
        for(int i=0;i<3;i++)h.advanceDay();assertEquals("儿童",h.children.get(0).stage());
        for(int i=3;i<12;i++)h.advanceDay();assertEquals("少年",h.children.get(0).stage());
        for(int i=12;i<18;i++)h.advanceDay();assertEquals("成年子女",h.children.get(0).stage());
    }
    @Test public void careAndHouseExpansionHaveRealCosts(){
        HomeWorld h=new HomeWorld();h.children.add(new HomeWorld.Child());h.grain=100;
        h.children.get(0).happiness=30;h.action("care");assertEquals(50,h.children.get(0).happiness);
        int after=h.grain;h.action("care");assertEquals(after,h.grain);
        h.action("upgrade");assertEquals(2,h.houseLevel);assertEquals(after-20,h.grain);assertEquals(20,h.wood);
    }
    @Test public void cropsNeedTimeAndWaterAndYieldSeeds(){
        HomeWorld h=new HomeWorld();h.action("sow");assertEquals(0,h.seeds);
        h.action("harvest");assertEquals(40,h.grain);
        for(int i=0;i<3;i++){h.action("water");h.advanceDay();}
        h.action("harvest");assertTrue(h.grain>=112);assertEquals(24,h.seeds);
        h.action("harvest");assertEquals(24,h.seeds);
    }
    @Test public void winterPausesCropsAndGrassRegrowth(){
        HomeWorld h=new HomeWorld();h.day=37;h.action("sow");java.util.Arrays.fill(h.grass,0);
        h.advanceDay();assertEquals(0,h.cropAge[0]);assertEquals(0,h.grass[0]);
    }
    @Test public void healthyAdultPairsProduceYoungThatMature(){
        HomeWorld h=new HomeWorld();h.grain=10000;
        for(int i=0;i<7;i++){h.action("feed");h.action("water");h.advanceDay();}
        assertTrue(h.animals.size()>6);
        HomeWorld.Animal young=null;for(HomeWorld.Animal a:h.animals)if(a.id>6){young=a;break;}
        assertNotNull(young);int id=young.id;
        for(int i=0;i<8;i++){h.action("feed");h.action("water");h.advanceDay();}
        assertTrue(h.animal(id).adult());
    }
    @Test public void ridingRequiresAdultHealthyHorseAndStopsIfRemoved(){
        HomeWorld h=new HomeWorld();assertFalse(h.riding());h.mount(1);assertTrue(h.riding());
        h.animal(1).food=0;assertFalse(h.riding());h.animal(1).food=100;h.animal(1).age=0;
        assertFalse(h.mount(1).startsWith("已骑上"));h.animals.remove(h.animal(1));assertFalse(h.riding());
    }
    @Test public void unfedAndAgedAnimalsLeavePopulation(){
        HomeWorld h=new HomeWorld();h.day=37;java.util.Arrays.fill(h.grass,0);
        for(HomeWorld.Animal a:h.animals){a.food=0;a.water=0;a.health=1;}
        h.advanceDay();assertTrue(h.animals.isEmpty());
        HomeWorld old=new HomeWorld();old.animal(1).age=160;old.advanceDay();assertNull(old.animal(1));
    }
    @Test public void ecologyRemainsBoundedOverTwoHundredDays(){
        HomeWorld h=new HomeWorld();h.grain=100000;
        for(int i=0;i<200;i++){
            h.action("feed");h.action("water");h.advanceDay();
            assertTrue(h.animals.size()<=HomeWorld.CAPACITY);
            HomeWorld copy=HomeWorld.restore(h.store());assertEquals(h.animals.size(),copy.animals.size());
        }
    }
    @Test public void serializedWorldResumesDeterministically() throws Exception {
        HomeWorld h=new HomeWorld();h.action("sow");h.action("talk");h.mount(1);h.advanceDay();
        ByteArrayOutputStream out=new ByteArrayOutputStream();Bundle.write(h.store(),out);
        HomeWorld copy=HomeWorld.restore(Bundle.read(new ByteArrayInputStream(out.toByteArray())));
        assertEquals(h.summary(),copy.summary());assertEquals(h.playerX,copy.playerX);assertEquals(h.ridingId,copy.ridingId);
        for(int i=0;i<10;i++){h.advanceDay();copy.advanceDay();assertEquals(h.summary(),copy.summary());}
    }
    @Test public void collisionsBlockBuildingsWaterAndDiagonalCorners(){
        HomeWorld h=new HomeWorld();h.playerX=4;h.playerY=5;
        assertFalse(h.move(1,0));assertFalse(h.move(1,1));assertTrue(h.move(0,1));
        assertFalse(HomeWorld.passable(21,12));assertFalse(HomeWorld.passable(0,0));
    }
    @Test public void corruptedAndUnsupportedSavesAreRejected(){
        Bundle invalid=new Bundle();invalid.put("version",99);
        try{HomeWorld.restore(invalid);fail();}catch(IllegalArgumentException expected){}
        HomeWorld h=new HomeWorld();Bundle damaged=h.store();damaged.put("crops",new int[]{3});
        try{HomeWorld.restore(damaged);fail();}catch(IllegalArgumentException expected){}
    }
}
