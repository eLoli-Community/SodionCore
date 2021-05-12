package com.eloli.sodioncore.file;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;

public class BaseFileService {
    private String base;

    public BaseFileService(String base) {
        this.base = base;
    }

    public String getConfigPath(String filename) {
        return Paths.get(base, filename).toString();
    }

    public void saveResource(String resourcePath, boolean replace) throws IOException {
        if (resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found");
        }

        File outFile = new File(base, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(base, resourcePath.substring(0, Math.max(lastIndex, 0)));

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        if (!outFile.exists() || replace) {
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        }
    }

    public InputStream getResource(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = this.getClass().getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            }
            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }
}
