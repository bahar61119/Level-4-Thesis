package com.thesis.application.handler;

import java.util.ArrayList;

/**
 * Created by bahar61119 on 8/31/2015.
 */
public class FileInformation {

    private String fileID;
    private String fileName;
    private String fileExtension;
    private long fileSize;
    private int numberOfChunk;
    private boolean completed;
    private ArrayList<Integer> chunkList;

    public FileInformation() {
        //this.fileID = null;
        this.fileName = null;
        this.fileExtension = null;
        this.fileSize = 0;
        this.numberOfChunk = 0;
        this.completed = false;
        this.chunkList = new ArrayList<>();
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = false;
        this.chunkList = new ArrayList<Integer>();
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk, boolean completed) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = completed;
        this.chunkList = new ArrayList<Integer>();
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk, boolean completed, ArrayList<Integer> chunkList) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = completed;
        this.chunkList = chunkList;
    }

    public void setFileID(String fileID){
        this.fileID = fileID;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public void setFileExtension(String fileExtension){
        this.fileExtension = fileExtension;
    }

    public void setFileSize(long fileSize){
        this.fileSize = fileSize;
    }

    public void setNumberOfChunk(int numberOfChunk){
        this.numberOfChunk = numberOfChunk;
    }

    public void setCompleted(boolean completed){
        this.completed = completed;
    }

    public void setChunkList(ArrayList<Integer> chunkList){
        this.chunkList = chunkList;
    }

    public void addChunkToList(int chunkNumber){
        this.chunkList.add(chunkNumber);
    }

    public boolean checkChunkInList(int chunkNumber){
        return this.chunkList.contains(chunkNumber);
    }

    public String getFileID(){
        return null;//fileID;
    }

    public String getFileName(){
        return fileName;
    }

    public String getFileExtension(){
        return fileExtension;
    }

    public long getFileSize(){
        return fileSize;
    }

    public int getNumberOfChunk(){
        return numberOfChunk;
    }

    public boolean getCompleted(){
        return completed;
    }

    public ArrayList<Integer> getChunkList(){
        return chunkList;
    }

    public int getChunkListSize(){
        return chunkList.size();
    }

}
