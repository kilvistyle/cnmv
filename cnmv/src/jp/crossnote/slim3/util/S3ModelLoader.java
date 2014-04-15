/**
 * Copyright (c) 2009 kilvistyle
 */
package jp.crossnote.slim3.util;

import java.io.File;
import java.io.FileFilter;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.slim3.datastore.ModelMetaUtil;
import org.slim3.util.ClassUtil;

/**
 * S3ModelLoader.
 * 
 * @author kilvistyle
 *         thanks bufferings (creator of Ktrwjr).
 * @since 2010/10/15
 *
 */
public class S3ModelLoader {

	protected static final String CLASSES_DIRECTORY = "WEB-INF/classes";
	protected static final String CLASSFILE_SUFIX = ".class";
	protected static final String FILE_SEPARATOR_REGEX = "[\\\\/]";
	protected static final String PACKAGE_SEPARATOR = ".";

	private static List<Class<?>> modelClassList = null;

	public static final List<Class<?>> load(boolean useCache) {
		return loadModelClass(CLASSES_DIRECTORY, useCache);
	}
	
	protected static List<Class<?>> loadModelClass(String classRootDir, boolean useCache) {
		List<Class<?>> classList = null;
		if (useCache) {
			if (modelClassList != null) {
				return modelClassList;
			}
		}
		if (classRootDir == null) classRootDir = CLASSES_DIRECTORY;
		File dir = new File(classRootDir);
		if (!dir.exists()) throw new IllegalStateException(CLASSES_DIRECTORY+" is not exists.");
		if (!dir.isDirectory()) throw new IllegalStateException(CLASSES_DIRECTORY+" is not directory.");

		classList = new ArrayList<Class<?>>();
		addModelClass(classList, classRootDir, dir);
		
		modelClassList = classList;
		return modelClassList;
	}
	
	private static void addModelClass(
	        List<Class<?>> classList,
	        String classRootDir,
            File currentDir) {

        File[] classFiles = listClassFiles(currentDir);
        for (File classFile : classFiles) {
            String className =
	            getClassNameFromClassFile(classRootDir, classFile);
            try {
                Class<?> clazz = ClassUtil.forName(className);
                if (ModelMetaUtil.isModelClass(clazz)
                	&& !Modifier.isAbstract(clazz.getModifiers())) {
                    classList.add(clazz);
                }
            }
            catch (Throwable e) {
				// skip at the class load error
			}
        }
        File[] dirFiles = listDirectories(currentDir);
        for (File dirFile : dirFiles) {
        	addModelClass(classList, classRootDir, dirFile);
        }
    }

	protected static File[] listClassFiles(File dir) {
		return dir.listFiles(new FileFilter() {
			public boolean accept(File pathname) {
				return !pathname.isDirectory()
				&& pathname.getName().endsWith(CLASSFILE_SUFIX);
			}
		});
	}

	protected static File[] listDirectories(File dir) {
	    return dir.listFiles(new FileFilter() {
	        public boolean accept(File pathname) {
	            return pathname.isDirectory();
	        }
	    });
	}

	protected static String getClassNameFromClassFile(String searchRootDirPath,
			File classFile) {
		String path = classFile.getPath();
		int beginIndex = searchRootDirPath.length() + 1;
		int endIndex = path.length() - CLASSFILE_SUFIX.length();
		return path.substring(beginIndex, endIndex).replaceAll(
				FILE_SEPARATOR_REGEX,
				PACKAGE_SEPARATOR);
	}

}
