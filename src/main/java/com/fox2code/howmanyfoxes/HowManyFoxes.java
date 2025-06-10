package com.fox2code.howmanyfoxes;

import com.fox2code.foxloader.client.gui.GuiConfigProvider;
import com.fox2code.foxloader.event.FoxLoaderEvents;
import com.fox2code.foxloader.launcher.FoxLauncher;
import com.fox2code.foxloader.loader.Mod;
import com.fox2code.howmanyfoxes.hmi.Config;
import com.fox2code.howmanyfoxes.hmi.GuiOptionsHMI;
import com.fox2code.howmanyfoxes.hmi.Utils;

import java.util.logging.Logger;

public class HowManyFoxes extends Mod {
    public static Logger logger;

    public HowManyFoxes() {
        logger = this.getLogger();
    }

    @Override
    public void onPreInit() {
        if (FoxLauncher.isClient()) {
            this.setConfigObject((GuiConfigProvider) GuiOptionsHMI::new);
            Config.init();
        }
    }

    @Override
    public void onPostInit() {
        if (FoxLauncher.isClient()) {
            System.out.println("HowManyFoxes: Post init!");
            FoxLoaderEvents.INSTANCE.registerEvents(HMIClient.INSTANCE);
            new Thread(Utils::itemList, "HowManyFoxes async pre-init thread!").start();
        }
    }
}
