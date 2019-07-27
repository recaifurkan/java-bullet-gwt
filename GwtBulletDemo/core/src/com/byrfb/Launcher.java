package com.byrfb;

import com.badlogic.gdx.ApplicationListener;

public class Launcher {
    public static ApplicationListener launch(){
        return new BulletTestContackCallbak();
    }
}
