/* Shattered Assist modifications, 2026. GPL-3.0-or-later. */
package com.shatteredpixel.shatteredpixeldungeon.home;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.*;
import com.watabou.noosa.ui.Component;

public class HomeWindow extends Window {
    public HomeWindow(String title,String description,String[] labels,Runnable[] actions) {
        int width=Math.min(200,(int)PixelScene.uiCamera.width-24);
        Component content=new Component();
        RenderedTextBlock text=PixelScene.renderTextBlock(title+"\n\n"+description,7);
        text.maxWidth(width);text.setPos(0,0);content.add(text);
        float y=text.bottom()+6;
        for(int i=0;i<labels.length;i++) {
            final Runnable action=actions[i];
            RedButton button=new RedButton(labels[i],8){
                @Override protected void onClick(){hide();action.run();}
            };
            content.add(button);button.setRect(0,y,width,22);y+=25;
        }
        RedButton close=new RedButton("关闭",8){@Override protected void onClick(){hide();}};
        content.add(close);close.setRect(0,y,width,22);y+=22;
        content.setSize(width,y);
        ScrollPane pane=new ScrollPane(content);add(pane);
        int height=Math.min((int)y,Math.max(80,(int)PixelScene.uiCamera.height-24));
        resize(width,height);pane.setRect(0,0,width,height);
    }
}
