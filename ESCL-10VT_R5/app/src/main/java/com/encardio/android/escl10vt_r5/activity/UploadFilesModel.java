package com.encardio.android.escl10vt_r5.activity;

public class UploadFilesModel {
    private final String fileName;
    private boolean isSelected;


    public UploadFilesModel(String fileName, boolean isSelected) {
        this.fileName = fileName;
        this.isSelected = isSelected;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
