package com.android.photour;

import java.io.File;

public class ImageElement {
    private int image = -1;
    private File file = null;


    public ImageElement(int image) {
        this.image = image;
    }

    public ImageElement(File fileX) {
        file = fileX;
    }

    public int getImage() {
        return image;
    }

    public File getFile() {
        return file;
    }
}
