/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.assist;
import org.junit.Test;
import static org.junit.Assert.*;
public class TouchStickTest {
    @Test public void eightDirectionsAndCenterDeadzone() {
        TouchStickState s = new TouchStickState();
        for (int dx=-1; dx<=1; dx++) for (int dy=-1; dy<=1; dy++) {
            s.cancel(); assertTrue(s.begin(7,dx,dy));
            assertEquals(dx,s.dx); assertEquals(dy,s.dy);
            assertTrue(Math.hypot(s.x,s.y)<=1.00001);
        }
        s.move(7,0.1f,-0.1f); assertEquals(0,s.dx); assertEquals(0,s.dy);
    }
    @Test public void onlyCapturedFingerCanMoveOrRelease() {
        TouchStickState s = new TouchStickState();
        assertTrue(s.begin(2,1,0));
        assertFalse(s.begin(3,-1,0)); assertFalse(s.move(3,-1,0)); assertFalse(s.end(3));
        assertEquals(1,s.dx); assertEquals(2,s.pointer);
        assertTrue(s.move(2,0,-1)); assertEquals(-1,s.dy);
        assertTrue(s.end(2)); assertEquals(-1,s.pointer); assertEquals(0,s.dx); assertEquals(0,s.dy);
        assertFalse(s.move(2,1,0));
    }
    @Test public void pauseAndInvalidCoordinatesReleaseInput() {
        TouchStickState s = new TouchStickState();
        s.begin(1,1,1); s.cancel(); assertEquals(0,s.dx); assertEquals(-1,s.pointer);
        s.begin(1,1,0); s.move(1,Float.NaN,0); assertEquals(-1,s.pointer);
        assertEquals(10,TouchStickState.clamp(-100,10,200),0);
        assertEquals(200,TouchStickState.clamp(10000,10,200),0);
        assertEquals(10,TouchStickState.clamp(Float.NaN,10,200),0);
    }
}
