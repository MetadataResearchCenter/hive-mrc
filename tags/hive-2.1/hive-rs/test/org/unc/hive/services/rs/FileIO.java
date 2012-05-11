/********************************************************************
 * Copyright 2010 the University of New Mexico.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 ********************************************************************/

package org.unc.hive.services.rs;

import java.io.*;

/**
 * FileIO class contains simple file I/O primitives implemented in Java.
 * All methods are static, since there is no state.
 */
public class FileIO {
  
  /*
   * Class fields
   */
  
  /** 
   * The size of blocking to use 
   */
  protected static final int BLKSIZ = 16384;


  /*
   * Constructors
   */
  
  /** 
   * Nobody should need to create an instance, so set access to private; 
   * all methods are static.
   */
  private FileIO() {
  }

  
  /*
   * Class methods
   */
  
  /** 
   * Copy a file from one filename to another 
   */
  public static void copyFile(String inName, String outName)
          throws FileNotFoundException, IOException {
    BufferedInputStream is = 
      new BufferedInputStream(new FileInputStream(inName));
    BufferedOutputStream os = 
      new BufferedOutputStream(new FileOutputStream(outName));
    copyFile(is, os, true);
  }

    
  /** 
   * Copy a file from an opened InputStream to opened OutputStream 
   */
  public static void copyFile(InputStream is, OutputStream os, boolean close) 
          throws IOException {
    byte[] b = new byte[BLKSIZ];        // the byte read from the file
    int i;
    
    while ((i = is.read(b)) != -1) {
      os.write(b, 0, i);
    }
    
    is.close();
    if (close)
      os.close();
  }

  
  /** 
   * Copy a file from an opened Reader to opened Writer 
   */
  public static void copyFile(Reader is, Writer os, boolean close) 
          throws IOException {
    int b;        // the byte read from the file
    BufferedReader bis = new BufferedReader(is);
    
    while ((b = is.read()) != -1) {
      os.write(b);
    }
    
    is.close(); 
    if (close)
      os.close();
  }

  
  /** 
   * Copy a file from a filename to a PrintWriter. 
   */
  public static void copyFile(String inName, PrintWriter pw, boolean close) 
          throws FileNotFoundException, IOException {
    BufferedReader ir = new BufferedReader(new FileReader(inName));
    copyFile(ir, pw, close);
  }

  
  /** 
   * Open a file and read the first line from it. 
   */
  public static String readLine(String inName)
          throws FileNotFoundException, IOException {
    BufferedReader is = new BufferedReader(new FileReader(inName));
    String line = null;
    line = is.readLine();
    is.close();
    return line;
  }

  
  /** 
   * Copy a data file from one filename to another, alternate method.
   * As the name suggests, use my own buffer instead of letting
   * the BufferedReader allocate and use the buffer.
   */
  public static void copyFileBuffered(String inName, String outName)
          throws FileNotFoundException, IOException {
    InputStream is = new FileInputStream(inName);
    OutputStream os = new FileOutputStream(outName);
    int count = 0;    // the byte count
    byte[] b = new byte[BLKSIZ];  // the bytes read from the file
    
    while ((count = is.read(b)) != -1) {
      os.write(b, 0, count);
    }
    
    is.close();
    os.close();
  }

  
  /** 
   * Read the entire content of a Reader into a String 
   */
  public static String readerToString(Reader is) throws IOException {
    StringBuffer sb = new StringBuffer();
    char[] b = new char[BLKSIZ];
    int n;

    // Read a block. If it gets any chars, append them.
    while ((n = is.read(b)) > 0) {
      sb.append(b, 0, n);
    }

    // Only construct the String object once, here.
    return sb.toString();
  }

  
  /** 
   * Read the content of a Stream into a String 
   */
  public static String inputStreamToString(InputStream is) throws IOException {
    return readerToString(new InputStreamReader(is));
  }

  
  /** 
   * Write a String as the entire content of a File 
   */
  public static void stringToFile(String text, String fileName)
          throws IOException {
    BufferedWriter os = new BufferedWriter(new FileWriter(fileName));
    os.write(text);
    os.flush();
    os.close();
  }

  
  /** 
   * Open a BufferedReader from a named file. 
   */
  public static BufferedReader openFile(String fileName) throws IOException {
    return new BufferedReader(new FileReader(fileName));
  }
  
}
