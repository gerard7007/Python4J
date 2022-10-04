import com.github.gerard.python4j.Python;
import com.github.gerard.python4j.PythonProcess;
import com.github.gerard.python4j.PythonVersion;

import java.io.File;

public class PythonTest {

    public static void main(String[] args) throws Exception {

        Python python = Python.getInstance(PythonVersion.Python3);

        PythonProcess process = python.newProcess()
                .mainFile(new File("ext/main.py"))
                .args("arg1", "arg2")
                // .inheritIO(true)
                .build();

        String result = process.asyncRun().join();

        System.out.println(result);
        System.out.println("Finished !");


    }
}
