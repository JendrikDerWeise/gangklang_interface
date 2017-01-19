package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JOptionPane;

public class ObjectSaver {

	public static void saveObject(Object object, String fileName, String objectType){
		File directory = new File(objectType);
		directory.mkdirs();
		FileOutputStream fileOut;
		ObjectOutputStream objectOut;
		try {
			fileOut = new FileOutputStream(objectType + "/" + fileName + ".gk");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(object);
			objectOut.close();
		} catch (FileNotFoundException e) {
			System.out.println("Can´t init FileOutputStream: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can´t init objectOut. " + e);
			e.printStackTrace();
		}
	}
	
	public static Object loadObject(String fileName, String objectType){
		FileInputStream fileIn;
		Object object = null;
		try {
			fileIn = new FileInputStream(objectType + "/" + fileName);//evtl ohne Pfad?
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			object = objectIn.readObject();
			fileIn.close();
		} catch (FileNotFoundException e) {
			System.out.println("Can´t init FileInputStream: " + e);
			e.printStackTrace();
		} 
		catch (Exception e) {
			System.out.println("Can´t init fileIn: " + e);
			e.printStackTrace();
		}
		return object;	
	}
	
	public static void saveFlowMachinePath(File path){
		FileOutputStream fileOut;
		ObjectOutputStream objectOut;
		try {
			fileOut = new FileOutputStream("flowmachine.path");
			objectOut = new ObjectOutputStream(fileOut);
			objectOut.writeObject(path);
			objectOut.close();
		} catch (FileNotFoundException e) {
			System.out.println("Can´t init FileOutputStream: " + e);
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Can´t init objectOut. " + e);
			e.printStackTrace();
		}
	}
	
	public static File loadPath(){
		FileInputStream fileIn;
		File path = new File("path");
		try {
			fileIn = new FileInputStream("flowmachine.path");
			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
			path = (File) objectIn.readObject();
			fileIn.close();
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Please set the path to the Flow-Maschine project.\nClick on the middle button in the top to set the path.", "Cannot save", JOptionPane.ERROR_MESSAGE, null);
			path = null;
		} 
		catch (Exception e) {
			System.out.println("Can´t init fileIn: " + e);
			e.printStackTrace();
		}
		return path;	
	}
}
