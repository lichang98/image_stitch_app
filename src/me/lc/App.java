package me.lc;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class App extends JFrame {

    private JPanel panelResultImg;
    private JLabel labelImg;

    private JPanel panelCtrls;
    private JButton buttonSelect;
    private JButton buttonSave;
    private JButton buttonLeft;
    private JButton buttonRight;
    private JLabel labelDisplayTip;

    private List<File> resultFiles;
    private int currentDisplayImgIdx;

    public App(String title) throws HeadlessException {
        super(title);
        setSize(800, 600);
        init();
        addListeners();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addListeners() {
        buttonSelect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("选择图片");
                JFileChooser jFileChooser = new JFileChooser(".");
                jFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                jFileChooser.setMultiSelectionEnabled(true);
                jFileChooser.showDialog(App.this, "选择");
                File[] files = jFileChooser.getSelectedFiles();
                if (files.length <= 1) {
                    JOptionPane.showMessageDialog(App.this, "请选择多个图片",
                            "警告", JOptionPane.WARNING_MESSAGE);
                } else {
                    System.out.println("选择的文件");
                    for (File f : files) {
                        System.out.println(f.getAbsolutePath());
                    }
                    // 拼接图片
                    labelImg.setText("图像拼接中...");
                    labelImg.setForeground(Color.RED);
                    labelImg.setFont(new Font("黑体", Font.BOLD, 20));
                    labelImg.paintImmediately(0, 0, labelImg.getWidth(), labelImg.getHeight());
                    resultFiles = stitchImgs(files);
                    labelImg.removeAll();
                    labelImg.repaint();
                    Image image = new ImageIcon(resultFiles.get(0).getAbsolutePath()).getImage();
                    image = image.getScaledInstance(labelImg.getWidth() - 10,
                            labelImg.getHeight() - 10, Image.SCALE_DEFAULT);
                    labelImg.setIcon(new ImageIcon(image));
                    currentDisplayImgIdx = 0;
                    labelDisplayTip.setText("拼接结果：第 " + (currentDisplayImgIdx + 1) +
                            " 张/共 " + resultFiles.size() + " 张");
                    labelDisplayTip.paintImmediately(0, 0, labelDisplayTip.getWidth(),
                            labelDisplayTip.getHeight());
                }
            }
        });

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("选择保存文件夹");
                JFileChooser jFileChooser = new JFileChooser(".");
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jFileChooser.showDialog(App.this, "选择保存文件夹");
                File fileDir = jFileChooser.getSelectedFile();
                System.out.println("选择的文件夹 " + fileDir);
                if (fileDir.isDirectory()) {
                    if (resultFiles == null) {
                        JOptionPane.showMessageDialog(App.this, "请先选择图片",
                                "警告", JOptionPane.WARNING_MESSAGE);
                    } else {
                        for (File file : resultFiles) {
                            String name = file.getName();
                            try {
                                Files.copy(Paths.get(file.toString()), Paths.get(fileDir.toString(), name));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(App.this, "请选择文件夹",
                            "警告", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        buttonLeft.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentDisplayImgIdx -= 1;
                if (currentDisplayImgIdx < 0)
                    currentDisplayImgIdx = resultFiles.size() - 1;
                labelImg.removeAll();
                labelImg.repaint();
                Image image = new ImageIcon(resultFiles.get(currentDisplayImgIdx).getAbsolutePath()).getImage();
                image = image.getScaledInstance(labelImg.getWidth() - 10,
                        labelImg.getHeight() - 10, Image.SCALE_DEFAULT);
                labelImg.setIcon(new ImageIcon(image));
                labelDisplayTip.setText("拼接结果：第 " + (currentDisplayImgIdx + 1) +
                        " 张/共 " + resultFiles.size() + " 张");
                labelDisplayTip.paintImmediately(0, 0, labelDisplayTip.getWidth(),
                        labelDisplayTip.getHeight());
            }
        });

        buttonRight.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                currentDisplayImgIdx += 1;
                if (currentDisplayImgIdx >= resultFiles.size())
                    currentDisplayImgIdx = 0;
                labelImg.removeAll();
                labelImg.repaint();
                Image image = new ImageIcon(resultFiles.get(currentDisplayImgIdx).getAbsolutePath()).getImage();
                image = image.getScaledInstance(labelImg.getWidth() - 10,
                        labelImg.getHeight() - 10, Image.SCALE_DEFAULT);
                labelImg.setIcon(new ImageIcon(image));
                labelDisplayTip.setText("拼接结果：第 " + (currentDisplayImgIdx + 1) +
                        " 张/共 " + resultFiles.size() + " 张");
                labelDisplayTip.paintImmediately(0, 0, labelDisplayTip.getWidth(),
                        labelDisplayTip.getHeight());
            }
        });
    }

    /**
     * 图像拼接
     *
     * @param files
     */
    private List<File> stitchImgs(File[] files) {
        Stitch stitch = new Stitch();
        Path resRootPath = Paths.get(System.getProperty("user.dir"), "res");
        List<File> fileList = new ArrayList<>();
        for (int i = 0; i < files.length; ++i) {
            fileList.add(files[i]);
        }
        int preSize = fileList.size(), id = 0;
        boolean flag = false;
        do {
            preSize = fileList.size();
            flag = false;
            for (int i = 0; i < fileList.size() - 1 && !flag; i++) {
                for (int j = i + 1; j < fileList.size() && !flag; ++j) {
                    String dstName = String.valueOf(id) + "_tmp.png";
                    id++;
                    System.out.println("file List=" + fileList);
                    System.out.println("i=" + i + ", j=" + j);
                    boolean retVal = stitch.stitchPairImg(Paths.get(fileList.get(i).getAbsolutePath()).toString(),
                            Paths.get(fileList.get(j).getAbsolutePath()).toString(),
                            Paths.get(resRootPath.toString(), dstName).toString());
                    if (retVal) {
                        File file1 = fileList.get(i), file2 = fileList.get(j);
                        System.out.println("file " + file1.getName() + " and file " +
                                file2.getName() + " ==> " + dstName);
                        fileList.remove(file1);
                        fileList.remove(file2);
                        fileList.add(new File(Paths.get(resRootPath.toString(), dstName).toString()));
                        flag = true;
                    }
                }
            }
        } while (preSize != fileList.size() && fileList.size() > 1);
        return fileList;
    }

    private void init() {
        // image display panel
        panelResultImg = new JPanel(null);
        panelResultImg.setBounds(10, 10, 720, 400);
        panelResultImg.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
        panelResultImg.setBackground(Color.WHITE);
        labelImg = new JLabel();
        labelImg.setBounds(0, 0, panelResultImg.getWidth(), panelResultImg.getHeight());
        panelResultImg.add(labelImg);

        // control panel
        panelCtrls = new JPanel(null);
        panelCtrls.setBackground(Color.WHITE);
        panelCtrls.setBounds(10, 450, 720, 150);
        buttonSelect = new JButton("选择图片");
        buttonSelect.setBounds(140, 80, 120, 30);
        buttonSelect.setBackground(Color.BLUE);
        buttonSelect.setForeground(Color.WHITE);
        buttonSave = new JButton("保存拼接结果");
        buttonSave.setBounds(420, 80, 120, 30);
        buttonSave.setForeground(Color.WHITE);
        buttonSave.setBackground(Color.BLUE);
        buttonLeft = new JButton("前一张图片");
        buttonLeft.setBackground(Color.WHITE);
        buttonLeft.setIcon(new ImageIcon(Paths.get(System.getProperty("user.dir"),
                "res", "ui", "left_arrow.png").toString()));
        buttonLeft.setBounds(160, 20, 160, 40);
        buttonRight = new JButton("后一张图片");
        buttonRight.setBackground(Color.WHITE);
        buttonRight.setIcon(new ImageIcon(Paths.get(System.getProperty("user.dir"),
                "res", "ui", "right_arrow.png").toString()));
        buttonRight.setBounds(340, 20, 160, 40);

        labelDisplayTip = new JLabel();
        labelDisplayTip.setBounds(260, 0, 200, 20);
        labelDisplayTip.setText("拼接结果：第 0 张/共 0 张");
        labelDisplayTip.setFont(new Font("黑体", Font.BOLD, 14));
        panelCtrls.add(buttonSelect);
        panelCtrls.add(buttonSave);
        panelCtrls.add(buttonLeft);
        panelCtrls.add(buttonRight);
        panelCtrls.add(labelDisplayTip);

        getContentPane().setLayout(null);
        getContentPane().setBackground(Color.WHITE);
        add(panelResultImg);
        add(panelCtrls);
    }

    public static void main(String[] args) {
        App app = new App("图像拼接");
    }
}
