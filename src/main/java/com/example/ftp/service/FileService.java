package com.example.ftp.service;

import com.example.ftp.dao.FileDao;
import com.example.ftp.dao.DirectoryDao;
import java.io.*;

public class FileService {

    public static void receiveFile(InputStream in, int userId, int dirId, String filename, long size) throws IOException {
        String dirPath = DirectoryDao.getPath(dirId);
        File file = new File(dirPath, filename);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            long remaining = size;
            while (remaining > 0) {
                int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) break;
                fos.write(buffer, 0, read);
                remaining -= read;
            }
        }
        FileDao.insert(userId, filename, dirId, file.getAbsolutePath(), size);
    }

    public static void sendFile(OutputStream out, int fileId) throws IOException {
        String absPath = FileDao.getAbsPath(fileId);
        File file = new File(absPath);
        if (!file.exists()) throw new FileNotFoundException();

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            out.flush();
        }
    }
}
