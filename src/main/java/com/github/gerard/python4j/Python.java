package com.github.gerard.python4j;

import com.github.gerard.python4j.exceptions.PythonException;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

public class Python {

    private static final String[] COMMON_PATHS = { "python", "python3" };

    public static Python instance;

    private final String path;
    private final PythonVersion version;

    public Python(String path, PythonVersion version) {
        this.path = path;
        this.version = version;
    }

    public PythonProcess.Builder newProcess() throws FileNotFoundException {
        return new PythonProcess.Builder(this);
    }

    public PythonVersion getVersion() {
        return version;
    }

    public String getPath() {
        return path;
    }

    public static Python getInstance(PythonVersion version) throws PythonException {
        if (instance == null) {
            HashMap<String, IOException> exceptions = new HashMap<>();

            for (String path : COMMON_PATHS) {
                try {
                    Process process = new ProcessBuilder().command(path, "--version").start();

                    String output;

                    try (InputStream is = process.getInputStream(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                        byte[] buffer = new byte[64];

                        for (int length; (length = is.read(buffer)) != -1; ) {
                            baos.write(buffer, 0, length);
                        }

                        output = baos.toString("UTF-8").trim();
                    }

                    if (!output.isEmpty()) {
                        String versionNum = output.replaceAll("[^0-9]", "");

                        if (versionNum.startsWith(version.versionNum + "")) {
                            return new Python(path, version);
                        }
                    }
                } catch (IOException e) {
                    exceptions.put(path, e);
                }
            }

            throw new PythonException("Python not found. PATHS=" + Arrays.toString(COMMON_PATHS) + ", TARGETED_VERSION=" + version.name());

        }
        return instance;
    }
}

