package sample.editor.controller;

import javafx.scene.control.Tab;
import javafx.stage.FileChooser;

import java.io.*;

public class FileIO {

    public void saveFile(String fileToSave, String contentToSave) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileToSave));
            bw.write(contentToSave);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public File saveFileAs(Tab tab, String contentToSave) {
        FileChooser fileChooser = createFileChooser();
        fileChooser.setInitialFileName("Untitled.txt");
        File file = fileChooser.showSaveDialog(null);
        if (file==null) return null;
        String fileToSave = file.getAbsolutePath();
        saveFile(fileToSave, contentToSave);
        tab.setId(fileToSave);
        tab.setText(file.getName());
        tab.getContent().setId("true");
        return file;
    }

    public String readFile(String fileToRead) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileToRead));
            String txt;
            while ((txt = bf.readLine()) !=null) {
                sb.append(txt).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public FileChooser createFileChooser() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Files", "*.*"));
        fc.setInitialFileName("*.txt");
        return fc;
    }

    public boolean isContentSame(String fileForCheck, String textToCheck) {
        if (fileForCheck==null) return false;
        return readFile(fileForCheck).equals(textToCheck);
    }
}
