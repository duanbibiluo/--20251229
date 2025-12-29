package com.example.ftp.service;

import com.example.ftp.dao.DirectoryDao;
import java.io.File;

public class DirectoryService {
    private static final String ROOT = "./data";

    public static int initUserRoot(int userId) {
        String path = ROOT + "/user_" + userId;
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();
        int rootId = DirectoryDao.getRootId(userId);
        if (rootId == -1) rootId = DirectoryDao.insertRoot(userId, path);
        return rootId;
    }

    public static void createDirectory(int userId, int parentId, String name) {
        String parentPath = DirectoryDao.getPath(parentId);
        String newPath = parentPath + "/" + name;
        new File(newPath).mkdirs();
        DirectoryDao.insert(userId, name, parentId, newPath);
    }

    public static String list(int parentId) {
        return DirectoryDao.listAsString(parentId);
    }
}
