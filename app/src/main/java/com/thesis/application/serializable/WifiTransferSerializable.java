package com.thesis.application.serializable;

/**
 * Created by bahar61119 on 8/28/2015.
 */
public class WifiTransferSerializable implements java.io.Serializable{
    private String FileName;
    private Long FileLength;
    private String InetAddress;


    public WifiTransferSerializable(){

    }

    public WifiTransferSerializable(String inetaddress){
        this.InetAddress = inetaddress;
    }

    public WifiTransferSerializable(String name, Long filelength){
        this.FileName = name;
        this.FileLength = filelength;
//		this.FileData = in;
    }

    public String getInetAddress() {
        return InetAddress;
    }

    public void setInetAddress(String inetAddress) {
        InetAddress = inetAddress;
    }


    public Long getFileLength() {
        return FileLength;
    }
    public void setFileLength(Long fileLength) {
        FileLength = fileLength;
    }

    public String getFileName() {
        return FileName;
    }
    public void setFileName(String fileName) {
        FileName = fileName;
    }

}
