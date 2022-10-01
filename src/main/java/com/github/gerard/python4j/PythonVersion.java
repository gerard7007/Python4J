package com.github.gerard.python4j;

public enum PythonVersion {

    Python2(2),
    Python3(3),
    ;

    public final int versionNum;

    PythonVersion(int versionNum) {
        this.versionNum = versionNum;
    }
}