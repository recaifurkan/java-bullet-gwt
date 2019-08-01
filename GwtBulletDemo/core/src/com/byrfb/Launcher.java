package com.byrfb;

import com.badlogic.gdx.ApplicationListener;
import com.byrfb.tests.BulletTestContackCallbak;


public class Launcher {
    public static ApplicationListener launch(){
        return new BulletTestContackCallbak();
    }
}
