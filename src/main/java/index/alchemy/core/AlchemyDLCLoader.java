package index.alchemy.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

import index.alchemy.api.annotation.DLC;
import index.alchemy.api.annotation.Loading;
import index.alchemy.core.debug.AlchemyRuntimeException;
import index.alchemy.util.Tool;
import net.minecraft.client.gui.GuiErrorScreen;
import sun.reflect.annotation.AnnotationParser;

@Loading
public class AlchemyDLCLoader {
	
	public static final String DESCRIPTOR = Tool.getDescriptor(DLC.class), DLCS_PATH = "/mods/dlcs/alchemy";
	
	private static final Map<DLC, Throwable> errors = new HashMap<DLC, Throwable>();
	
	private static final Map<String, DLC> dlc_mapping = new HashMap<String, DLC>();
	
	private static final Map<String, File> file_mapping = new HashMap<String, File>();
	
	@Nullable
	public static DLC findDLC(String name) {
		return dlc_mapping.get(name);
	}
	
	public static boolean isDLCLoaded(String name) {
		return findDLC(name) != null;
	}
	
	public static void init(Class<?> clazz) {
		AlchemyModLoader.checkState();
		DLC dlc = clazz.getAnnotation(DLC.class);
		if (dlc != null)
			dlc_mapping.put(clazz.getName(), dlc);
	}
	
	public static void setup() throws Exception {
		AlchemyModLoader.checkInvokePermissions();
		AlchemyModLoader.checkState();
		
		AlchemyModLoader.logger.info("Setup: " + AlchemyDLCLoader.class.getName());

		String val = System.getProperty("index.alchemy.dlcs.bin", "");
		if (!val.isEmpty())
			for (String path : val.split(";"))
				addDLCFile(new File(path.replace("$mc_dir", AlchemyModLoader.mc_dir)));
		else 
			AlchemyModLoader.logger.info("index.alchemy.dlcs.bin is EMPTY");
		
		File dlcs = new File(AlchemyModLoader.mc_dir + DLCS_PATH);
		if (!dlcs.exists())
			dlcs.mkdirs();
		File files[] = dlcs.listFiles();
		if (files != null)
			for (File file : files)
				if (file.getName().endsWith(".dlc"))
					addDLCFile(file);
		
		if (!errors.isEmpty());
	}
	
	public static void addDLCFile(File file) {
		AlchemyModLoader.logger.info("Add DLC: " + file.getPath());
		DLC dlc = null;
		try {
			if ((dlc = checkFileIsDLC(file)) != null) {
				file = update(dlc, file);
				URL url = file.toURI().toURL();
				Tool.addURLToClassLoader(Thread.currentThread().getContextClassLoader(), url);
				List<String> classes = AlchemyModLoader.findClassFromURL(url);
				AlchemyModLoader.addClass(classes);
				file_mapping.put(dlc.name(), file);
				AlchemyModLoader.logger.info("Successfully loaded DLC: " + file.getPath());
			} else
				AlchemyModLoader.logger.warn("DLC: " + file.getPath() + ", is not a standard Alchemy DLC");
		} catch (Exception e) {
			errors.put(dlc, e);
			AlchemyModLoader.logger.warn("Failed to load DLC: " + file.getPath(), e);
		}
	}
	
	public static File update(DLC dlc, File file) {
		if (file.isDirectory())
			return file;
		// Test
		return file;
	}
	
	@Nullable
	public static DLC checkFileIsDLC(File file) throws Exception {
		List<DLC> result = new LinkedList<DLC>();
		DLC dlc;
		if (file.isDirectory()) {
			List<URL> list = new LinkedList<URL>();
			Tool.getAllURL(file, list);
			for (URL url : list)
				if ((dlc = checkClassIsDLC(url.openStream())) != null) {
					result.add(dlc);
					dlc = null;
				}
		} else {
			ZipInputStream input = new ZipInputStream(new FileInputStream(file));
			for (ZipEntry entry; (entry = input.getNextEntry()) != null;)
				if (!entry.isDirectory() && entry.getName().endsWith(".class") && (dlc = checkClassIsDLC(input)) != null) {
					result.add(dlc);
					dlc = null;
				}
			if (result.size() > 1)
				AlchemyRuntimeException.onException(new RuntimeException("This file has multiple DLC"));
		}
		return Tool.getSafe(result, 0);
	}
	
	@Nullable
	public static DLC checkClassIsDLC(InputStream input) throws Exception {
		try {
			ClassReader reader = new ClassReader(input);
			ClassNode node = new ClassNode(Opcodes.ASM5);
			reader.accept(node, 0);
			if (node.visibleAnnotations != null)
				for (AnnotationNode annotation : node.visibleAnnotations)
					if (DESCRIPTOR.equals(annotation.desc))
						return (DLC) AnnotationParser.annotationForMap(DLC.class, toMap(annotation.values));
		} finally {
			IOUtils.closeQuietly(input);
		}
		return null;
	}
	
	private static Map<String, Object> toMap(List<Object> list) {
		Map<String, Object> result = new HashMap<String, Object>();
		if (list.size() % 2 != 0) {
			AlchemyRuntimeException.onException(new RuntimeException("list.size() % 2 != 0"));
		} else {
			String temp = null;
			for (Object obj : list)
				if (temp == null)
					temp = (String) obj;
				else {
					result.put(temp, obj);
					temp = null;
				}
		}
		return result;
	}
	
	public static class GuiAlchemyDLCError extends GuiErrorScreen {

		public GuiAlchemyDLCError(String titleIn, String messageIn) {
			super(null, null);
		}
		
	}

}