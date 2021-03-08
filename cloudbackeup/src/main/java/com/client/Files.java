package main.java.com.client;

import java.io.File;

public class Files {
    private String name;
    private long size;
    private boolean flag;
    private File file;

    public Files(){

    }

    public Files(File f){
        this.file=f;
        this.name=f.getName();
        this.size=f.length();
    }

    public File getFile() {
        return file;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlog(boolean flag) {
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public long getSize() {
        return size;
    }


}
