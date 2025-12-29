package com.example.ftp.client.UI;

import com.example.ftp.client.ClientSocket;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;

public class MainPanel extends JPanel {
    private ClientSocket clientSocket;
    private JTree dirTree;
    private DefaultTreeModel treeModel;
    private JButton mkdirButton, uploadButton, downloadButton;

    public MainPanel(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
        initUI();
        refreshTree();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
        treeModel = new DefaultTreeModel(root);
        dirTree = new JTree(treeModel);
        add(new JScrollPane(dirTree), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        mkdirButton = new JButton("New Folder");
        uploadButton = new JButton("Upload File");
        downloadButton = new JButton("Download File");

        buttonPanel.add(mkdirButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(downloadButton);
        add(buttonPanel, BorderLayout.SOUTH);

        mkdirButton.addActionListener(e -> createFolder());
        uploadButton.addActionListener(e -> uploadFile());
        downloadButton.addActionListener(e -> downloadFile());
    }

    private void refreshTree() {
        try {
            String listResp = clientSocket.sendCommand("LIST");
            String[] items = listResp.split(",");
            DefaultMutableTreeNode root = new DefaultMutableTreeNode("root");
            for (String item : items) {
                String[] parts = item.split(":");
                if(parts.length>=2)
                    root.add(new DefaultMutableTreeNode(parts[1] + " [" + parts[0] + "]"));
            }
            treeModel.setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createFolder() {
        String name = JOptionPane.showInputDialog("Folder name:");
        if (name != null && !name.isEmpty()) {
            try {
                clientSocket.sendCommand("MKDIR|" + name);
                refreshTree();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void uploadFile() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                clientSocket.sendFile(file);
                refreshTree();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void downloadFile() {
        String input = JOptionPane.showInputDialog("Enter file ID to download:");
        if (input != null && !input.isEmpty()) {
            int fileId = Integer.parseInt(input);
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(new File("downloaded_" + fileId));
            int ret = chooser.showSaveDialog(this);
            if (ret == JFileChooser.APPROVE_OPTION) {
                File targetFile = chooser.getSelectedFile();
                try {
                    clientSocket.getFile(fileId, targetFile);
                    JOptionPane.showMessageDialog(this, "Download completed");
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
}
