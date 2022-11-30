package com.ztftrue.unzip;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZIP {
    /**
     * DeCompress the ZIP to the path
     */
    @SuppressWarnings("unused")
    public static void UnZipFolder(File source, String outPath, Charset charset) throws IOException {
        try (ZipFile zipFile = new ZipFile(source, ZipFile.OPEN_READ, charset);) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                byte[] bytes = name.getBytes(charset);
                File target = new File(outPath, name);
                if (entry.isDirectory()) {
                    Files.createDirectories(target.toPath());
                } else {
                    try (InputStream in = zipFile.getInputStream(entry);
                    ) {
                        Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }
    }

}
