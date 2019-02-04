package co.lotc.core;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class DependencyLoader {
	public static void loadJars(File parentFolder) {
    parentFolder.mkdirs();
    for (File jar : parentFolder.listFiles()) {
        if (jar.isFile()) {
            try {
                URL url = jar.toURI().toURL();
                URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

                Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                method.setAccessible(true);
                method.invoke(classLoader, url);

                System.out.println("[Tythan-DependencyLoader] Loaded " + jar.getName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	}
}
