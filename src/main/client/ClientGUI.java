package main.client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ClientGUI extends JFrame {
    private JList<String> fileList;
    private DefaultListModel<String> model;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public ClientGUI() {
        try {
            socket = new Socket("localhost", 9000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法连接服务器");
            return;
        }

        setTitle("Socket File Client");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        model = new DefaultListModel<>();
        fileList = new JList<>(model);
        add(new JScrollPane(fileList), BorderLayout.CENTER);

        JPanel panel = new JPanel();
        JButton uploadBtn = new JButton("Upload");
        JButton downloadBtn = new JButton("Download");
        JButton createDirBtn = new JButton("Create Dir");

        uploadBtn.addActionListener(e -> uploadFile());
        downloadBtn.addActionListener(e -> downloadFile());
        createDirBtn.addActionListener(e -> createDir());

        panel.add(uploadBtn);
        panel.add(downloadBtn);
        panel.add(createDirBtn);
        add(panel, BorderLayout.SOUTH);

        loadFileList();
        setVisible(true);
    }

    private void loadFileList() {
        try {
            dos.writeUTF("LIST");
            dos.flush();
            int size = dis.readInt();
            model.clear();
            for (int i = 0; i < size; i++) model.addElement(dis.readUTF());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "无法连接服务器");
        }
    }

    private void uploadFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            String dir = JOptionPane.showInputDialog(this, "输入目录名:");
            if (dir == null || dir.isEmpty()) return;

            try {
                dos.writeUTF("UPLOAD");
                dos.writeUTF(dir);
                dos.writeUTF(f.getName());
                dos.writeLong(f.length());
                try (FileInputStream fis = new FileInputStream(f)) {
                    byte[] buffer = new byte[4096];
                    int r;
                    while ((r = fis.read(buffer)) != -1) dos.write(buffer, 0, r);
                }
                dos.flush();
                JOptionPane.showMessageDialog(this, dis.readUTF());
                loadFileList();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void downloadFile() {
        String file = fileList.getSelectedValue();
        if (file == null) return;

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(file));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                dos.writeUTF("DOWNLOAD");
                dos.writeUTF(file);
                dos.flush();
                String status = dis.readUTF();
                if (!"DOWNLOAD_START".equals(status)) {
                    JOptionPane.showMessageDialog(this, "文件不存在");
                    return;
                }
                long len = dis.readLong();
                File out = chooser.getSelectedFile();
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    byte[] buffer = new byte[4096];
                    long read = 0;
                    while (read < len) {
                        int r = dis.read(buffer, 0, (int) Math.min(buffer.length, len - read));
                        fos.write(buffer, 0, r);
                        read += r;
                    }
                }
                JOptionPane.showMessageDialog(this, "下载完成");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void createDir() {
        String dir = JOptionPane.showInputDialog(this, "输入目录名:");
        if (dir == null || dir.isEmpty()) return;
        try {
            dos.writeUTF("CREATE_DIR");
            dos.writeUTF(dir);
            dos.flush();
            JOptionPane.showMessageDialog(this, dis.readUTF());
            loadFileList();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
