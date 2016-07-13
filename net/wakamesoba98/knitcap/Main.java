package net.wakamesoba98.knitcap;

import com.sun.javafx.application.LauncherImpl;
import net.wakamesoba98.knitcap.application.MainApplication;
import net.wakamesoba98.knitcap.application.SplashScreenLoader;

public class Main {
    public static void main(String[] args) {
        LauncherImpl.launchApplication(MainApplication.class, SplashScreenLoader.class, args);
    }
}
