package com.thesis.application.handler;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thesis.application.activities.ThesisActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by bahar61119 on 8/29/2015.
 */
public class MethodHandler {

    public static final String InformationFilePath = Environment.getExternalStorageDirectory() + "/thesis/information.txt";
    public static final String FilesDirectory =  Environment.getExternalStorageDirectory() + "/thesis/files";
    public static final String ChunkFilesDirectory = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles";
    public static final String FILEINFORMSTION = "fileinformation";
    public static final String CHUNKFILEINFORMATION = "chunkfileinformation";
    public static final String CHUNKFILETOSEND = "chunkfiletosend";
    public static final String IsFileSend = "isfilesend";

    public static String getPath(Uri uri, Context context) {
        if (uri == null) {
            return null;
        }
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        Log.e(ThesisActivity.TAG, "get path method->> " + uri.getPath());
        return uri.getPath();
    }

    public static String getRealPathFromURI(ContentResolver contentResolver, Uri contentURI) {
        String result;
        Cursor cursor = contentResolver.query(contentURI, null, null, null, null);
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static String convertObjectToJsonString(Object object){
        Gson gson = new Gson();
        return  gson.toJson(object);
    }

    public static Object convertJsonStringToInfoObject(String jsonString){

        Gson gson = new Gson();
        Type collectionType = new TypeToken<FileInformation>() {
        }.getType();

        return gson.fromJson(jsonString, collectionType);
    }
    public static Object convertJsonStringToInfoObjectArray(String jsonString){

        Gson gson = new Gson();
        Type collectionType = new TypeToken<ArrayList<FileInformation>>() {
        }.getType();
        return gson.fromJson(jsonString, collectionType);
    }

    public static void writeJsonStringToFIle(String jsonString, String filePath) throws IOException {

        try {
            FileWriter file = new FileWriter(filePath);
            file.write(jsonString);
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readJsonStringToFIle(String filePath) throws IOException {
        byte[] fileBytes;
        int bytesRead = 0;
        String json = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            fileBytes = new byte[(int) file.length()];
            bytesRead = fis.read(fileBytes, 0,(int)  file.length());
            json = new String(fileBytes, "UTF-8");
            fis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public static File[] listOfFilesInDirectory(String path){
        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();

        if(file == null) return null;

        Log.d("Files", "Size: "+ file.length);

        for (int i=0; i < file.length; i++)
        {
            Log.d("Files", "FileName:" + file[i].getName());
        }
        return file;
    }

    public static void createFolder(String filepath){
        File dir = new File(filepath);
        try{
            if(dir.mkdirs()) {
                System.out.println("Directory created");
            } else {
                System.out.println("Directory is not created");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public static int splitFile(String fileName)
    {
        String filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles";
        File checkFile[] =  listOfFilesInDirectory(filePath);
        int j=fileName.lastIndexOf('.');
        String name=fileName.substring(0, j);

        if(checkFile!=null){
            for(int i=0;i<checkFile.length;i++) {
                if(checkFile[i].getName().equalsIgnoreCase(name)){
                    filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles/"+name;
                    File check[] =  listOfFilesInDirectory(filePath);
                    return check.length;
                }
            }
        }


        ///////////////////////////////////////////////////////////////
        filePath = Environment.getExternalStorageDirectory() + "/thesis/files/"+fileName;
        File inputFile = new File(filePath);
        FileInputStream inputStream;
        String newFileName = null;
        FileOutputStream filePart;

        int fileSize = (int) inputFile.length();

        System.err.println("====================    : "+fileSize);

        int nChunks = 0, read = 0, readLength = 1;

        readLength=(int)(fileSize/10);

        byte[] byteChunkPart;

        try {
            inputStream = new FileInputStream(inputFile);
            while (fileSize > 0) {
                if (fileSize <= 5) {
                    readLength = fileSize;
                }

                byteChunkPart = new byte[readLength];

                read = inputStream.read(byteChunkPart, 0, readLength);

                fileSize -= read;

                assert (read == byteChunkPart.length);

                nChunks++;

                j=fileName.lastIndexOf('.');
                name=fileName.substring(0, j);

                filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles/"+name;
                Log.d("File Name:",name);

                newFileName = filePath+"/"+ fileName + ".part"+ Integer.toString(nChunks - 1);

                File f = new File(newFileName);
                File dirs = new File(f.getParent());
                if(!dirs.exists()) dirs.mkdirs();
                f.createNewFile();

                filePart = new FileOutputStream(f);
                filePart.write(byteChunkPart);


                filePart.flush();
                filePart.close();
                byteChunkPart = null;
                filePart = null;
            }
            inputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return nChunks;
    }


    public static void mergeFile(String fileName){

        String filePath = Environment.getExternalStorageDirectory() + "/thesis/files/"+fileName;

        File ofile = new File(filePath);
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> list = new ArrayList<File>();

        for(int i=0;i<10;i++){
            int j=fileName.lastIndexOf('.');
            String str=fileName.substring(0, j);
            filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles/"+str+"/"+fileName+".part"+i;
            list.add(new File(filePath));
        }

        try {
            fos = new FileOutputStream(ofile,true);
            for (File file : list) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                bytesRead = fis.read(fileBytes, 0,(int)  file.length());
                assert(bytesRead == fileBytes.length);
                assert(bytesRead == (int) file.length());
                fos.write(fileBytes);

                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
        }catch (Exception exception){
            exception.printStackTrace();
        }
    }

    public static ArrayList<FileInformation> readInformationFile(){
        String filePath = Environment.getExternalStorageDirectory() + "/thesis/information.txt";
        File ff = new File(filePath);
        MethodHandler.createFolder(ff.getParent());

        ArrayList<FileInformation> fileInformation = new ArrayList<>();
        try {
            String jsonString = MethodHandler.readJsonStringToFIle(filePath);
            fileInformation = (ArrayList<FileInformation>)MethodHandler.convertJsonStringToInfoObjectArray(jsonString);
            //Log.d("File Directory Read:",fileInformation.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Log.d("File Directory",fileInformation.toString());
        return fileInformation;
    }

    public static ArrayList<FileInformation> readFileList(){
        String filePath = Environment.getExternalStorageDirectory() + "/thesis/files";
        File ff = new File(filePath);
        MethodHandler.createFolder(ff.getParent());

        File file[] = MethodHandler.listOfFilesInDirectory(filePath);
        if(file != null )Log.d("Number of Files:",""+file.length);
        else Log.d("Number of Files:","Null");


        ArrayList<FileInformation> fileInformations = new ArrayList<>();
        for (int i=0; i < file.length; i++)
        {
            FileInformation info = new FileInformation();
            Log.d("Files", "FileName:" + file[i].getName());
            info.setFileName(file[i].getName());
            info.setFileSize(file[i].length());

            info.setCompleted(true);


            int chunk = MethodHandler.splitFile(info.getFileName());
            info.setNumberOfChunk(chunk);
            ArrayList<Integer> arrayList = new ArrayList<>();
            for(int j=0;j<chunk;j++){
                arrayList.add(j);
            }
            info.setChunkList(arrayList);

            fileInformations.add(info);
        }

        return fileInformations;
    }

    public static void writeInformationFile(ArrayList<FileInformation> fileInformations){
        try {
            String jsonString = MethodHandler.convertObjectToJsonString(fileInformations);
            String filePath = Environment.getExternalStorageDirectory() + "/thesis/information.txt";
            File ff = new File(filePath);
            MethodHandler.createFolder(ff.getParent());
            MethodHandler.writeJsonStringToFIle(jsonString,filePath);
            Log.d("File Directory Write:",jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("File Directory",MethodHandler.convertObjectToJsonString(fileInformations));
    }

    public static boolean writeFileFromChunkFiles(String fileName){
        String filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles";
        File ff = new File(filePath);
        MethodHandler.createFolder(ff.getParent());

        File margefile[] = MethodHandler.listOfFilesInDirectory(filePath);


        int j = fileName.lastIndexOf('.');
        String name = fileName.substring(0,j);

        if(margefile!= null){
            for(int i=0;i<margefile.length;i++){
                if(!margefile[i].getName().equalsIgnoreCase(name)) continue;

                filePath = Environment.getExternalStorageDirectory() + "/thesis/chunkfiles/"+margefile[i].getName();
                File chunkFiles[] = MethodHandler.listOfFilesInDirectory(filePath);
                name = null;
                if(margefile!=null){
                    name = chunkFiles[0].getName();
                    j = name.lastIndexOf('.');
                    name = name.substring(0,j);
                }

                if(name != null) MethodHandler.mergeFile(name);
                else return false;

                return true;
            }
            return false;
        }
        else{
            return false;
        }
    }

    public static ArrayList<FileInformation> updateInformationFile(ArrayList<FileInformation> updateTo, ArrayList<FileInformation> updateFrom){

        Log.d("Info File: ",updateTo.toString());
        Log.d("File List: ",updateFrom.toString());
        ArrayList<FileInformation> newInformation = new ArrayList<>();
        for(int i=0;i<updateFrom.size();i++){
            boolean flag = false;
            for(int j=0;j<updateTo.size();j++){
                if(updateTo.get(j).getFileName().equalsIgnoreCase(updateFrom.get(i).getFileName())){
                    updateTo.remove(j);
                    break;
                }
            }
        }

        for(int i=0;i<updateFrom.size();i++){
            newInformation.add(updateFrom.get(i));
        }
        for(int i=0;i<updateTo.size();i++){
            newInformation.add(updateTo.get(i));
        }

        Log.d("New Info File: ",updateFrom.toString());
        return newInformation;
    }

    public static ArrayList<FileInformation> getChangeInformation(ArrayList<FileInformation> originalInformation, ArrayList<FileInformation> receivedInformation){
        ArrayList<FileInformation> fileInformations = new ArrayList<>();

        for(int i=0;i<originalInformation.size();i++){
            boolean flag = false;
            for(int j=0;j<receivedInformation.size();j++){
                if(originalInformation.get(i).getFileName().equalsIgnoreCase(receivedInformation.get(j).getFileName())){
                   if(receivedInformation.get(j).getChunkList().size()==receivedInformation.get(j).getNumberOfChunk()){
                       flag = true;
                       break;
                   }
                    FileInformation info = originalInformation.get(i);
                    ArrayList<Integer> chunkList = new ArrayList<>();

                    for(int k=0;k<originalInformation.get(i).getChunkList().size();k++){
                        if(!receivedInformation.get(j).getChunkList().contains(originalInformation.get(i).getChunkList().get(k))){
                            chunkList.add(originalInformation.get(i).getChunkList().get(k));
                        }
                    }


                    if(chunkList!=null && chunkList.size()>0) info.setChunkList(chunkList);
                    flag = true;

                    fileInformations.add(info);

                    break;
                }
            }
            if(!flag) fileInformations.add(originalInformation.get(i));
        }


        return fileInformations;
    }

    public static ArrayList<FileInformation> updateReceivedChunk(ArrayList<FileInformation> originalInformation, FileInformation receivedChunk){
        Log.d("Received Chunk: ", receivedChunk.getFileName());
        Log.d("Received Chunk Number: ",""+receivedChunk.getChunkList().get(0));
        boolean flag = false;
        for(int i=0;i<originalInformation.size();i++){
            if(originalInformation.get(i).getFileName().equalsIgnoreCase(receivedChunk.getFileName())){
                if(originalInformation.get(i).checkChunkInList(receivedChunk.getChunkList().get(0))) continue;
                originalInformation.get(i).addChunkNumber(receivedChunk.getChunkList().get(0));
                flag = true;
                Log.d("New Chunk List : ", originalInformation.get(i).getChunkList().toString());

                if(originalInformation.get(i).isChunkCompleted()){
                    originalInformation.get(i).setCompleted(true);
                    MethodHandler.writeFileFromChunkFiles(originalInformation.get(i).getFileName());
                }

                break;
            }
        }
        if(!flag) originalInformation.add(receivedChunk);
        return originalInformation;
    }

}
