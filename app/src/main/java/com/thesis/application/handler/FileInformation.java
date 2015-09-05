package com.thesis.application.handler;

import android.content.Context;

import java.util.ArrayList;
import java.util.Hashtable;

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
    private Hashtable<Integer,Long> chunkReceivedTime;
    private long totalTimeTakenToReceive;
    private boolean isReceived;

    public FileInformation() {
        //this.fileID = null;
        this.fileName = null;
        this.fileExtension = null;
        this.fileSize = 0;
        this.numberOfChunk = 0;
        this.completed = false;
        this.chunkList = new ArrayList<>();
        this.isReceived = false;
        this.chunkReceivedTime = new Hashtable<>();
        this.totalTimeTakenToReceive = 0;
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk, boolean isReceived) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = false;
        this.chunkList = new ArrayList<Integer>();
        this.isReceived = isReceived;
        this.chunkReceivedTime = new Hashtable<>();
        this.totalTimeTakenToReceive = 0;
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk, boolean completed, boolean isReceived) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = completed;
        this.chunkList = new ArrayList<Integer>();
        this.isReceived = isReceived;
        this.chunkReceivedTime = new Hashtable<>();
        this.totalTimeTakenToReceive = 0;
    }

    public FileInformation(String fileName, String fileExtension, long fileSize,
                           int numberOfChunk, boolean completed, boolean isReceived, ArrayList<Integer> chunkList) {
        //this.fileID = fileID;
        this.fileName = fileName;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.numberOfChunk = numberOfChunk;
        this.completed = completed;
        this.chunkList = chunkList;
        this.isReceived = isReceived;
        this.chunkReceivedTime = new Hashtable<>();
        this.totalTimeTakenToReceive = 0;
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

    public void addChunkReceivedTimeKeyValue(Integer key, Long value){
        chunkReceivedTime.put(key,value);
        totalTimeTakenToReceive += value.longValue();
    }

    public Long getChunkReceivedTimeValue(Integer key){
        return chunkReceivedTime.get(key);
    }

    public void setCompleted(boolean completed){
        this.completed = completed;
    }

    public void setIsReceived(boolean isReceived){
        this.isReceived = isReceived;
    }

    public boolean getIsReceived(){
        return this.isReceived;
    }

    public void setChunkList(ArrayList<Integer> chunkList){
        this.chunkList.clear();
        this.chunkList = chunkList;
    }

    public void addChunkNumber(int number){
        this.chunkList.add(number);
    }

    public void setChunkReceivedTime(Hashtable<Integer,Long> chunkReceivedTime){
        this.chunkReceivedTime = chunkReceivedTime;
    }

    public Hashtable<Integer,Long> getChunkReceivedTime(){
        return chunkReceivedTime;
    }

    public void removeChunkNumber(int index){
        this.chunkList.remove(index);
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

    public boolean isChunkCompleted(){
        return getChunkListSize() == getNumberOfChunk();
    }

    public Long getTotalTimeTakenToReceive(){
        return totalTimeTakenToReceive;
    }

}
