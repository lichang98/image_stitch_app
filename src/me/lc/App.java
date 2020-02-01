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

    private List<File> resultFiles;

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
                    resultFiles=stitchImgs(files);
                }
            }
        });

        buttonSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                System.out.println("选择保存文件夹");
                JFileChooser jFileChooser = new JFileChooser(".");
                jFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                jFileChooser.showDialog(App.this,"选择保存文件夹");
                File fileDir=jFileChooser.getSelectedFile();
                System.out.println("选择的文件夹 "+fileDir);
                if(fileDir.isDirectory()){
                    if(resultFiles == null){
                        JOptionPane.showMessageDialog(App.this,"请先选择图片",
                                "警告",JOptionPane.WARNING_MESSAGE);
                    }else{
                        for(File file : resultFiles){
                            String name= file.getName();
                            try {
                                Files.move(Paths.get(file.toString()),Paths.get(fileDir.toString(),name));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }else{
                    JOptionPane.showMessageDialog(App.this,"请选择文件夹",
                            "警告",JOptionPane.WARNING_MESSAGE);
                }
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
                        System.out.println("file " + file1.getName() + " and file " + file2.getName() + " ==> " + dstName);
                        fileList.remove(file1);
                        fileList.remove(file2);
                        fileList.add(new File(Paths.get(resRootPath.toString(),dstName).toString()));
                        flag = true;
                    }
                }
            }
        } while (preSize != fileList.size() && fileList.size() > 1);
        return  fileList;
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
        buttonSelect.setBounds(40, 10, 120, 30);
        buttonSave = new JButton("保存拼接结果");
        buttonSave.setBounds(240, 10, 120, 30);
        panelCtrls.add(buttonSelect);
        panelCtrls.add(buttonSave);

        getContentPane().setLayout(null);
        getContentPane().setBackground(Color.WHITE);
        add(panelResultImg);
        add(panelCtrls);
    }

    public static void main(String[] args) {
        App app = new App("图像拼接");
    }
}
