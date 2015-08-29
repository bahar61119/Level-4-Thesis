package com.thesis.application.serializable;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class WifiTransferSerializable implements java.io.Serializable{
    private String fileName;
    private Long fileLength;
    private String inetAddress;

    public WifiTransferSerializable(){}
    public WifiTransferSerializable(String inetAddress){
        this.inetAddress = inetAddress;
    }
    public WifiTransferSerializable(String fileName, Long fileLength){
        this.fileName = fileName;
        this.fileLength = fileLength;
    }
    public WifiTransferSerializable(String fileName, Long fileLength , String inetAddress){
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.inetAddress = inetAddress;
    }

    public String getInetAddress(){
        return inetAddress;
    }

    public void setInetAddress(String inetAddress){
        this.inetAddress = inetAddress;
    }

    public Long getFileLength(){
        return fileLength;
    }

    public void setFileLength(Long fileLength){
        this.fileLength = fileLength;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
}
