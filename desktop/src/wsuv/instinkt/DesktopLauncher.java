package wsuv.instinkt;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	public static void main (String[] arg) {
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("inSTINKt");
		config.setWindowedMode(1152, 896);
		config.useVsync(true);
		config.setWindowIcon("Images/icon 16.png", "Images/icon 32.png", "Images/icon 64.png", "Images/icon 128.png");
		new Lwjgl3Application(new Game(), config);
	}
}
