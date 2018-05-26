package de.x8bit.Fantasya.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * inspired by : http://snippets.dzone.com/posts/show/4831,
 * but modified to work with JAR file.
 * @author vtatai
 * @author hapebe
 */
@SuppressWarnings("rawtypes")
public class PackageLister {

/**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     */
    public static List<Class> getClasses(String packageName)
            throws ClassNotFoundException, IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        
        URL url = classLoader.getResource(path);
		if (url == null) throw new RuntimeException("ClassLoader URL is null for '" + packageName + "'.");

		String resPath = url.toString();
        if ((resPath.indexOf("jar:file:/") == -1) || (resPath.indexOf("!") == -1) ) {
        	// in Eclipse (und andere??)
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dirs = new ArrayList<File>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            ArrayList<Class> classes = new ArrayList<Class>();
            for (File directory : dirs) {
                classes.addAll(findFSClasses(directory, packageName));
            }

            return classes;
        } else {
        	// über Konsole
            int jarPos = resPath.indexOf("jar:file:/");
            int endPos = resPath.indexOf("!");

            String jarURL = resPath.substring(jarPos + 9, endPos); // alt + 10
            
            // showJar(jarURL);

            return findJARClasses(jarURL, path);
        }
    }

    /**
     * find class files matching <em>path</em> in a JAR, also if located in sub-packages.
     */
    @SuppressWarnings("resource")
	private static List<Class> findJARClasses(String jarURL,  String path) throws ClassNotFoundException, IOException {
        List<Class> classes = new ArrayList<Class>();

        JarFile jar = new JarFile(jarURL);
        Enumeration<JarEntry> e = jar.entries();

        while (e.hasMoreElements()) {
            JarEntry entry = e.nextElement();
            if (entry.getName().endsWith(".class")) {
                if (entry.getName().contains(path)) {
                    String className = entry.getName().replace("/", ".");
                    className = className.substring(0, className.length() - 6);

                    try {
						classes.add(Class.forName(className));
					} catch (NoClassDefFoundError ex) {
						System.err.println("Warnung: Kann Klasse " + className + " nicht laden!");
					}
                }
            }
        }
        return classes;
    }

    private static List<Class> findFSClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();

        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findFSClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }

        return classes;
    }

}
