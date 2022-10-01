package com.github.gerard.python4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PythonProcess implements AutoCloseable {

    private final Python python;
    protected File mainFile;
    protected File workingDir;
    protected List<String> args;
    protected boolean inheritIO;
    protected Executor executor;
    protected Process process;

    public static class Builder {

        private final Python python;
        private File mainFile;
        private File workingDir;
        private List<String> args;
        protected boolean inheritIO;
        private Executor executor;

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

        public Builder executor(Executor executor) {
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

    protected PythonProcess(Python python) {
        this.python = python;
    }

    /**
     * @return Result future
     */
    public CompletableFuture<Result> asyncRun() {
        CompletableFuture<Result> future = new CompletableFuture<>();

        executor.execute(() -> {
            try {
                ProcessBuilder processBuilder = new ProcessBuilder();
                processBuilder.directory(workingDir);
                if (inheritIO) processBuilder.inheritIO();

                List<String> command = new ArrayList<>();
                command.add(python.getPath());
                command.add(mainFile.getAbsolutePath());
                command.addAll(args);

                process = processBuilder.command(command).start();

                String output = null;

                try (Scanner scanner = new Scanner(System.in)) {
                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = scanner.nextLine()) != null) {
                        sb.append(line).append("\n");
                    }

                    output = sb.toString();
                }

                Result result = new Result(process.waitFor(), output);

                future.complete(result);
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });

        return future;
    }

    @Override
    public void close() throws Exception {
        // TODO: auto close
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
}
