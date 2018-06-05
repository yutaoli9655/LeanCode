package com.cscs.pojo;

/**
 * 附件类
 */
public class AtFile {

    /**
     * 文件名
     */
    private String name;
    /**
     * base64位编码
     */
    private String image;
    /**
     * 是否是内联附件
     */
    private boolean isInner;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isInner() {
        return isInner;
    }

    public void setInner(boolean inner) {
        isInner = inner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "AtFile{" +
                "name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", isInner=" + isInner +
                '}';
    }
}
