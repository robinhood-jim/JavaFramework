package com.robin.core.compress.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;


public class ZipUtils {


	public static void zip(File srcDir, File zipFile) throws IOException {
		if (!srcDir.exists()) {
			return;
		}
		if (!zipFile.getParentFile().exists()) {
			zipFile.getParentFile().mkdirs();
		}

		ZipOutputStream out = null;
		try {
			out = new ZipOutputStream(new FileOutputStream(zipFile));
			pack(out, srcDir, "");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}


	public static void jar(File srcDir, File jarFile, Manifest manifest)
			throws IOException {
		if (!srcDir.exists()) {
			return;
		}
		if (!jarFile.getParentFile().exists()) {
			jarFile.getParentFile().mkdirs();
		}
		
		JarOutputStream out = null;
		try {
			if (manifest == null) {
				out = new JarOutputStream(new FileOutputStream(jarFile));
			} else {
				out = new JarOutputStream(new FileOutputStream(jarFile),
						manifest);
			}
			pack(out, srcDir, "");
		} finally {
			IOUtils.closeQuietly(out);
		}
	}

	private static void pack(ZipOutputStream out, File src, String base)
			throws IOException {
		if (src.isDirectory()) {
			File[] files = src.listFiles();
			out.putNextEntry(new ZipEntry(base + "/"));
			base = StringUtils.isBlank(base) ? "" : base + "/";
			for (File f : files) {
				pack(out, f, base + f.getName());
			}
		} else {
			out.putNextEntry(new JarEntry(base));

			FileInputStream in = null;
			try {
				in = new FileInputStream(src);
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
	}


	public static void unzip(File srcFile, File destDir) throws IOException {
		ZipFile zipFile = new ZipFile(srcFile);
		try {
			Enumeration<?> zipEnum = zipFile.entries();
			while (zipEnum.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipEnum.nextElement();
				if (entry.isDirectory()) {
					new File(destDir, entry.getName()).mkdirs();
					continue;
				} 
				File file = new File(destDir, entry.getName());
				if (!file.getParentFile().exists()) {
					file.getParentFile().mkdirs();
				}

				InputStream input = null;
				OutputStream output = null;
				try {
					input = zipFile.getInputStream(entry);
					output = new FileOutputStream(file);
					IOUtils.copy(input, output);
				} finally {
					IOUtils.closeQuietly(output);
					IOUtils.closeQuietly(input);
				}
			}
		} finally {
			zipFile.close();
		}
	}

}
