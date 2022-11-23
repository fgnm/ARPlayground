package games.rednblack.ar.playground.ios;

import org.robovm.apple.foundation.NSAutoreleasePool;
import org.robovm.apple.uikit.UIApplication;

import com.badlogic.gdx.backends.iosrobovm.IOSApplication;
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration;
import games.rednblack.ar.playground.ARPlayground;
import games.rednblack.gdxar.GdxARConfiguration;
import games.rednblack.gdxar.GdxArApplicationListener;
import games.rednblack.gdxar.GdxLightEstimationMode;
import games.rednblack.gdxar.GdxPlaneFindingMode;
import games.rednblack.gdxar.ios.ARKitApplication;

/** Launches the iOS (RoboVM) application. */
public class IOSLauncher extends IOSApplication.Delegate {
	@Override
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration configuration = new IOSApplicationConfiguration();
		configuration.useGL30 = true;
		GdxARConfiguration gdxARConfiguration = new GdxARConfiguration();
		gdxARConfiguration.debugMode = true;
		gdxARConfiguration.enableDepth = false;
		gdxARConfiguration.planeFindingMode = GdxPlaneFindingMode.HORIZONTAL;
		gdxARConfiguration.lightEstimationMode = GdxLightEstimationMode.AMBIENT_INTENSITY;

		GdxArApplicationListener applicationListener = new ARPlayground();

		ARKitApplication arKitApplication = new ARKitApplication(applicationListener, gdxARConfiguration);
		IOSApplication iosApplication = new IOSApplication(arKitApplication, configuration);
		//Enable Coaching View
		arKitApplication.setIosApplication(iosApplication);

		return iosApplication;
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSLauncher.class);
		pool.close();
	}
}