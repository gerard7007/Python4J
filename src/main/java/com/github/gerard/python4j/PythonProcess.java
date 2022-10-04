package com.github.gerard.python4j;

import com.github.gerard.python4j.exceptions.PythonException;
import sun.misc.IOUtils;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PythonProcess {

    private final Python python;
    protected File mainFile;
    protected File workingDir;
    protected List<String> args;
    protected boolean inheritIO;
    protected ExecutorService executor;
    protected Process process;

    protected PythonProcess(Python python) {
        this.python = python;
    }

    private CompletableFuture<String> readStreamAsync(InputStream inputStream) {
        return CompletableFuture.supplyAsync(() -> {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1];

                while (inputStream.read(buffer) != -1) {
                    baos.write(buffer, 0, buffer.length);
                }

                String output = baos.toString("UTF-8");
                return output.endsWith("\n") ? output.substring(0, output.length() - 1) : output;
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        });
    }

    /**
     * @return Result future
     */
    public CompletableFuture<String> asyncRun() throws IOException, PythonException {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.directory(workingDir);
        if (inheritIO) processBuilder.inheritIO();

        List<String> command = new ArrayList<>();
        command.add(python.getPath());
        command.add(mainFile.getAbsolutePath());
        command.addAll(args);

        process = processBuilder.command(command).start();

        CompletableFuture<String> outFuture = readStreamAsync(process.getInputStream());
        CompletableFuture<String> errFuture = readStreamAsync(process.getErrorStream());

        String error = errFuture.join();

        if (error != null && !error.isEmpty()) {
            throw new PythonException(error.endsWith("\n") ? error.substring(0, error.length() - 1) : error);
        }

        return outFuture;
    }

    public File getMainFile() {
        return mainFile;
    }

    public File getWorkingDir() {
        return workingDir;
    }

    public List<String> getArgs() {
        return args;
    }

    public Python getPython() {
        return python;
    }

    public static class Builder {

        private final Python python;
        private File mainFile;
        private File workingDir;
        private List<String> args;
        protected boolean inheritIO;
        private ExecutorService executor;

        public Builder(Python python) {
            this.python = python;
        }

        public Builder mainFile(File file) {
            this.mainFile = file;
            return this;
        }

        public Builder workingDir(File dir) {
            this.workingDir = dir;
            return this;
        }

        public Builder args(List<String> args) {
            this.args = args;
            return this;
        }

        public Builder args(String... args) {
            return args(Arrays.asList(args));
        }

        public Builder inheritIO(boolean inheritIO) {
            this.inheritIO = inheritIO;
            return this;
        }

        public Builder executor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public PythonProcess build() {
            if (mainFile == null) {
                throw new NullPointerException("mainFile must not be null");
            }

            PythonProcess process = new PythonProcess(python);
            process.mainFile = mainFile;
            process.workingDir = workingDir != null ? new File(Paths.get("").toFile().getAbsolutePath()) : null;
            process.args = args != null ? args : new ArrayList<>();
            process.inheritIO = inheritIO;
            process.executor = executor != null ?  executor : Executors.newSingleThreadExecutor();
            return process;
        }
    }

    public static class Result {

        protected int exitCode;
        protected String output;

        public Result(int exitCode, String output) {
            this.exitCode = exitCode;
            this.output = output;
        }

        public String getOutput() {
            return output;
        }

        public int getExitCode() {
            return exitCode;
        }
    }
}
