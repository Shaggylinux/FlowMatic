package com.back.candidatos.drive;

import com.back.drive.Archivos;
import java.util.ArrayList;
import java.util.List;

public class FolderNode {
    private Archivos folder;
    private List<FolderNode> children;
    private int fileCount;

    public FolderNode(Archivos folder) {
        this.folder = folder;
        this.children = new ArrayList<>();
        this.fileCount = 0;
    }

    public Archivos getFolder() {
        return folder;
    }

    public void setFolder(Archivos folder) {
        this.folder = folder;
    }

    public List<FolderNode> getChildren() {
        return children;
    }

    public void setChildren(List<FolderNode> children) {
        this.children = children;
    }

    public int getFileCount() {
        return fileCount;
    }

    public void setFileCount(int fileCount) {
        this.fileCount = fileCount;
    }

    public void addFileCount(int count) {
        this.fileCount += count;
    }
}
