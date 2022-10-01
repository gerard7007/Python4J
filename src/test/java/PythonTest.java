import com.github.gerard.python4j.Python;
import com.github.gerard.python4j.PythonProcess;
import com.github.gerard.python4j.PythonVersion;

import java.io.File;

public class PythonTest {

    public static void main(String[] args) throws Exception {

        Python python = Python.getInstance(PythonVersion.Python3);

        PythonProcess process = python.newProcess()
                .mainFile(new File("main.py"))
                .args("arg1", "arg2")
                .build();

        System.out.println(process.toString());


    }
}
