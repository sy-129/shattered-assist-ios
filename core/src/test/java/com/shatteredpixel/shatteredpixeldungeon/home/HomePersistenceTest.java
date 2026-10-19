/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.files.FileHandle;
import com.watabou.utils.FileUtils;
import org.junit.*;
import org.junit.rules.TemporaryFolder;
import java.lang.reflect.Proxy;
import java.io.IOException;
import java.nio.file.Path;
import static org.junit.Assert.*;
public class HomePersistenceTest {
    @Rule public TemporaryFolder folder=new TemporaryFolder();
    @Before public void setup(){
        Gdx.files=(Files)Proxy.newProxyInstance(Files.class.getClassLoader(),new Class<?>[]{Files.class},
                (p,m,a)->new FileHandle((String)a[0]));
        FileUtils.setDefaultFileProperties(Files.FileType.Absolute,folder.getRoot().getAbsolutePath()+"/");
        HomeService.world=null;HomeService.slot=1;
    }
    @After public void cleanup(){HomeService.world=null;FileUtils.setDefaultFileProperties(Files.FileType.Local,"");}
    @Test public void independentSlotsAndCorruptPrimaryRecovery() throws Exception {
        HomeService.load(1);HomeService.world.day=7;HomeService.save();
        HomeService.load(2);assertEquals(1,HomeService.world.day);HomeService.world.day=20;HomeService.save();
        HomeService.load(1);assertEquals(7,HomeService.world.day);
        Path primary=folder.getRoot().toPath().resolve(HomeService.filename(1));java.nio.file.Files.write(primary,new byte[]{1,2,3});
        HomeService.world=null;HomeService.load(1);assertEquals(7,HomeService.world.day);assertFalse(HomeService.recoveryNotice.isEmpty());
    }
    @Test public void twoCorruptFilesAreNotSilentlyOverwritten() throws Exception {
        HomeService.load(1);HomeService.save();
        Path primary=folder.getRoot().toPath().resolve(HomeService.filename(1));
        java.nio.file.Files.write(primary,new byte[]{9});java.nio.file.Files.write(Path.of(primary+".bak"),new byte[]{8});
        HomeService.world=null;
        try{HomeService.load(1);fail();}catch(IOException expected){}
        assertArrayEquals(new byte[]{9},java.nio.file.Files.readAllBytes(primary));
    }
}
