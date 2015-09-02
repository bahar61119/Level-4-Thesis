package com.thesis.application.serializable;

import com.thesis.application.handler.FileInformation;

import java.io.Serializable;
import java.util.ArrayList;

public class WiFiTransferModal implements Serializable {

	private String FileName;
	private Long FileLength;
	private String InetAddress;
    private String info = null;
    private boolean isDataTransfer;


	public WiFiTransferModal(){
		info = new String();
        isDataTransfer = false;
	}
	
	public WiFiTransferModal(String inetaddress){
		this.InetAddress = inetaddress;
	}

    public WiFiTransferModal(boolean isDataTransfer){
        this.isDataTransfer = isDataTransfer;
    }

	public WiFiTransferModal(String name, Long filelength, boolean isDataTransfer){
		this.FileName = name;
		this.FileLength = filelength;
        this.isDataTransfer = isDataTransfer;
//		this.FileData = in;
	}
    public WiFiTransferModal(String name, Long filelength, boolean isDataTransfer,String info){
        this.info = info;
        this.FileName = name;
        this.FileLength = filelength;
        this.isDataTransfer = isDataTransfer;
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

    public void setInfo(String info){
        this.info = info;
    }

    public String getInfo(){
        return info;
    }

    public void setIsDataTransfer(boolean isDataTransfer){
        this.isDataTransfer = isDataTransfer;
    }

    public boolean getIsDataTransfer(){
        return isDataTransfer;
    }
	
	
	
}
