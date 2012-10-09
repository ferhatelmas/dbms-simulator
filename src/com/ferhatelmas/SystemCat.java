package com.ferhatelmas;


import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

// this class is the most essential part of project
// all operation functions in it
public class SystemCat {

  // system catalog file name
  private static final String catalogFileName = "catalog.cat";            

  // buffer on which all operations are processed
  private static byte[] catalogCurrentPage = new byte[256];               

  // page number of page in memory
  private static int catalogCurrentPageNumber;                            

  // created random access file
  private static RandomAccessFile catalogFile;                            

  // table to load information
  private static Table catalogCurrentTable = new Table();                 

  // empty page list, filled in init
  private static List<Integer> catalogEmptyPageList = new ArrayList<Integer>();

  // table names and page numbers, filled in initialization
  private static Hashtable catalogTableNames = new Hashtable();           

  //**************************** getter and setters ****************************
  public static byte[] getCatalogCurrentPage() {
    return catalogCurrentPage;
  }

  public static void setCatalogCurrentPage(byte[] catalogCurrentPage) {
    SystemCat.catalogCurrentPage = catalogCurrentPage;
  }

  public static int getCatalogCurrentPageNumber() {
    return catalogCurrentPageNumber;
  }

  public static void setCatalogCurrentPageNumber(int catalogCurrentPageNumber) {
    SystemCat.catalogCurrentPageNumber = catalogCurrentPageNumber;
  }

  public static RandomAccessFile getCatalogFile() {
    return catalogFile;
  }

  public static void setCatalogFile(RandomAccessFile catalogFile) {
    SystemCat.catalogFile = catalogFile;
  }

  public static Table getCatalogCurrentTable() {
    return catalogCurrentTable;
  }

  public static void setCatalogCurrentTable(Table catalogCurrentTable) {
    SystemCat.catalogCurrentTable = catalogCurrentTable;
  }
  //********************************* getter and setters end *******************

  //******* system catalog file existence check, open, create and close ********
  public static boolean doesCatalogExist() {
    return new File(catalogFileName).exists();
  }

  public static void openCatalogFile() throws IOException {
    catalogFile = new RandomAccessFile(catalogFileName, "rws");
  }

  public static void createCatalogFile() throws IOException {
    catalogFile = new RandomAccessFile(catalogFileName, "rws");
  }

  public static void closeCatalogFile() throws IOException {
    catalogFile.close();
  }
  //******* system catalog file existence check, open, create and close end ****

  //******* buffer read from and write into catalog file ***********************
  public static void readCatalogPage(int pageNumber) throws IOException {
    catalogFile.seek(256 * pageNumber);
    catalogFile.readFully(catalogCurrentPage);
  }

  public static void writeCatalogPage(int pageNumber) throws IOException {
    catalogFile.seek(256 * pageNumber);
    catalogFile.write(catalogCurrentPage);
  }
  //******* buffer read from and write into catalog file end *******************

  // table existence test
  public static boolean doesTableExist(String tableName){
    return catalogTableNames.containsKey(tableName);
  }

  // check whether table has already been loaded,
  // just return or load table
  public static void loadTable(String tableName) throws IOException {
    String name = catalogCurrentTable.getTableName();
    if(name == null || !name.equals(tableName)){
      readCatalogPage((Integer)catalogTableNames.get(tableName));
      setTable(tableName);
    } 
  }

  // create table with given name and fields
  public static void createTable(String tableName, String fields) throws IOException {
    
    // create data file with dat extension
    File file = new File(tableName + ".dat");   
    if(!file.createNewFile()) {
      System.out.println("'" + tableName + ".dat' couldn't be created");
      return;
    }

    // clear buffer
    clearPage();    
    // set marker to full
    catalogCurrentPage[0] = '1';  

    // check whether there is space from deleted tables
    // according to this set table page number
    if(!catalogEmptyPageList.isEmpty()) {
      catalogCurrentPageNumber = catalogEmptyPageList.get(catalogEmptyPageList.size() - 1);
      catalogEmptyPageList.remove((Integer)catalogCurrentPageNumber);
    } else 
      catalogCurrentPageNumber = (int)catalogFile.length()/256;

    // fill buffer with fields
    fillTableData(tableName, fields); 
    // write buffer
    writeCatalogPage(catalogCurrentPageNumber);   
    // add table name into table name list
    catalogTableNames.put(tableName, catalogCurrentPageNumber);  

    System.out.println("New table is written into page '" + 
      catalogCurrentPageNumber + "' of catalog.txt");
    System.out.println("Create table is successfully completed\n");
  }

  public static void deleteTable(String tableName) throws IOException {

    // delete data file
    File file = new File(tableName + ".dat");  
    if(!file.delete()) {
      System.out.println(tableName + ".dat couldn't be deleted");
      return;
    }

    // get page number of table
    int pageNumber = (Integer)catalogTableNames.get(tableName); 
    
    // set marker to deleted
    catalogFile.seek(pageNumber * 256);     
    catalogFile.write('0');

    // delete table name from table name list
    catalogTableNames.remove(tableName);                        

    // add its page to empty page list
    catalogEmptyPageList.add(pageNumber);                       

    System.out.println("Table is deleted from page '" + pageNumber + "' of catalog.txt");
    System.out.println("Delete Table is successfully completed\n");
  }

  // check whether there is table
  // if exists, print their names
  public static void listTables(){
    if(catalogTableNames.isEmpty()) 
      System.out.println("There is no table");
    else {
      Enumeration e = catalogTableNames.keys();
      while(e.hasMoreElements())
        System.out.println((String)e.nextElement());
    }
    System.out.println();
  }

  // read catalog file sequentially, note which table is in which page
  public static void fillCatalogTableNames() throws IOException {

    for(int i=0; i<catalogFile.length()/256; i++) {
      readCatalogPage(i);                                             
      catalogCurrentPageNumber = i;

      // check if it is valid
      if(catalogCurrentPage[0] == '1'){                               
          int j = 1;
          while(catalogCurrentPage[j] != ':') { j++; }
          String tableName = new String(catalogCurrentPage, 1, j-1);  
          catalogTableNames.put(tableName, catalogCurrentPageNumber); 
      } else {
          catalogEmptyPageList.add(new Integer(i));                   
      }
    }
  }

  // this function just copies given strings into buffer with predefined markers
  public static void fillTableData(String tableName, String fields){
    int offset = tableName.length();
    System.arraycopy(tableName.getBytes(), 0, catalogCurrentPage, 1, offset);
    catalogCurrentPage[++offset] = ':';
    System.arraycopy(fields.getBytes(), 0, catalogCurrentPage, ++offset, fields.length());
    offset += fields.length();
    catalogCurrentPage[offset] = ';';
  }

  // using buffer data fill previously created table object
  public static void setTable(String name){
    catalogCurrentTable.setTableName(name);
    setFieldsOfTable(2 + name.length());
  }

  // called by above function to fill fields
  public static void setFieldsOfTable(int offset){

    Hashtable hash = catalogCurrentTable.getTableFields();
    hash.clear();

    int start;
    while(catalogCurrentPage[offset] != ';'){
      start = offset;
      while(catalogCurrentPage[offset] != ',') offset++;
      String fieldName = new String(catalogCurrentPage, start, offset - start);
      int fieldLength = Utility.byteArrayToShort(catalogCurrentPage, ++offset);
      hash.put(fieldName, fieldLength);
      offset +=2;
    }
  }

  // check whether given primary key exists
  public static boolean doesPkExist(int reqPk) throws IOException {

    RandomAccessFile r = new RandomAccessFile(catalogCurrentTable.getTableName() + ".dat", "rws");
    int len = catalogCurrentTable.getFieldsTotalLength();

    for(int i=0; i<r.length()/256; i++) {       
      r.seek(i * 256);
      r.readFully(catalogCurrentPage);       
      int j = 0;
      while(j<256){
        // get record from buffer
        if(catalogCurrentPage[j] == '1'){   
          if(reqPk == Utility.byteArrayToInt(catalogCurrentPage, j+1)) 
            return true;
        }
        j += len;
      }
    }
    r.close();       
    return false;
  }

  //add record to preloaded table
  public static void addRecord() throws IOException {

    // get new primary key
    int pk = getPk(false);     
    // get record to be inserted
    String record = getRecordFromUser(pk);    
    // table name to insert
    String tableName = catalogCurrentTable.getTableName();  
    // record length
    int len = record.length();  
    // open related data file
    RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws"); 

    int pageNumber = 0;
    boolean isAdded = false;
    // read records
    for(int i=0; i<r.length()/256; i++) {       
      r.seek(i * 256);
      // read page
      r.readFully(catalogCurrentPage);        

      int j = 0;
      while(j<256){
        // get record from buffer
        if(catalogCurrentPage[j] == '0'){   
          if(j + len < 256) {
            System.arraycopy(record.getBytes(), 0, catalogCurrentPage, j, len);
            r.seek(i * 256);
            r.write(catalogCurrentPage);
            pageNumber = i;
            isAdded = true;
            break;
          }
        }
        j += len;
      }
    }

    if(!isAdded) {
      clearPage();
      System.arraycopy(record.getBytes(), 0, catalogCurrentPage, 0, len);
      r.seek(r.length());
      r.write(catalogCurrentPage);
      pageNumber = (int)r.length() / 256 - 1;
    }

    //close file
    r.close();  

    //print success message
    System.out.println("Record is added to " + pageNumber + 
      " page of " + tableName + ".dat");
    System.out.println("Add Record is successfully completed\n");
  }

  public static void deleteRecord() throws IOException {
    // get table name
    String tableName = catalogCurrentTable.getTableName(); 
    // get existing primary key
    int reqPk = getPk(true);     
    // get record length
    int len = catalogCurrentTable.getFieldsTotalLength(); 
    // open data file
    RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws"); 

    int pageNumber = 0;
    // read records
    for(int i=0; i<r.length()/256; i++) {       
      r.seek(i * 256);
      // read page
      r.readFully(catalogCurrentPage);        

      int j = 0;
      while(j<256){
        // get record from buffer
        if(catalogCurrentPage[j] == '1'){   
          if(pk == reqPk == Utility.byteArrayToInt(catalogCurrentPage, j+1)) {
            catalogCurrentPage[j] = '0';
            r.seek(i * 256);
            r.write(catalogCurrentPage);
            pageNumber = i;
            break;
          }
        }
        j += len;
      }
    }

    // close file
    r.close();  

    // print operation results
    System.out.println("Record is deleted from page '" + pageNumber + 
      "' of " + tableName + ".dat");
    System.out.println("Delete Record is successfully completed\n");

  }

  public static void listRecords() throws IOException {
    // open file
    RandomAccessFile r = new RandomAccessFile(catalogCurrentTable.getTableName() + 
      ".dat", "rws"); 

    Hashtable hash = catalogCurrentTable.getTableFields();
    Enumeration e = hash.keys();
    // print names of fields with formatting
    System.out.print("PK        \t");    
    while(e.hasMoreElements()) {         
      String fieldName = (String)e.nextElement();
      int fieldNameLen = fieldName.length();
      System.out.print(fieldName);
      if(fieldNameLen < (Integer)hash.get(fieldName)) {
          for(int k=0; k<(Integer)hash.get(fieldName)-fieldNameLen; k++) 
            System.out.print(" "); 
      }
      System.out.print("\t");
    }
    System.out.println();
    
    int len = catalogCurrentTable.getFieldsTotalLength();
    // read records
    for(int i=0; i<r.length()/256; i++) {       
      r.seek(i * 256);
      // read page
      r.readFully(catalogCurrentPage);        
      
      int j = 0;
      while(j<256){
        // get record from buffer
        if(catalogCurrentPage[j] == '1'){   
          e = hash.keys();
          int pk = Utility.byteArrayToInt(catalogCurrentPage, j+1); 
          System.out.print(pk);
          for(int k=0; k<10-Integer.toString(pk).length(); k++) System.out.print(" ");
          System.out.print("\t");
          String record = new String(catalogCurrentPage, j+5, len-5);

          int length;
          int k = 0;
          while(e.hasMoreElements()){        
            String s = (String)e.nextElement();
            length = (Integer)hash.get(s);
            System.out.print(record.substring(k, k+length));
            for(int x=0; x<s.length()-length; x++) System.out.print(" ");   //formatting
            System.out.print("\t");
            k += length;
          }
          System.out.println();
        }
        j += len; 
      } 
    }

    // close file
    r.close();        
    System.out.println();
  }

  public static void searchAndListRecords() throws IOException {

    int reqPk;
    // get search key
    while(true){    
      try {
        System.out.println("Please enter key");
        reqPk = Integer.parseInt(UIHelper.get().readLine());
        break;
      } catch (NumberFormatException nfe) { }
    }

    String tableName = catalogCurrentTable.getTableName(); 
    Hashtable hash = catalogCurrentTable.getTableFields();
    Enumeration e = hash.keys();
    System.out.print("PK        \t");      
    while(e.hasMoreElements()) {           
      String fieldName = (String)e.nextElement();
      int fieldNameLen = fieldName.length();
      System.out.print(fieldName);
      if(fieldNameLen < (Integer)hash.get(fieldName)) {
        for(int k=0; k<(Integer)hash.get(fieldName)-fieldNameLen; k++) 
          System.out.print(" "); 
      }
      System.out.print("\t");
    }
    System.out.println();

    int len = catalogCurrentTable.getFieldsTotalLength();
    RandomAccessFile r = new RandomAccessFile(tableName + ".dat", "rws"); 
    
    // read records
    for(int i=0; i<r.length()/256; i++) {       
      // read page
      r.seek(i * 256);
      r.readFully(catalogCurrentPage);        
      
      int j = 0;
      while(j<256){
        // get record from buffer
        if(catalogCurrentPage[j] == '1'){   
          e = hash.keys();
          int pk = Utility.byteArrayToInt(catalogCurrentPage, j+1);   
          if(reqPk == pk) {
            System.out.print(pk);
            for(int k=0; k<10-Integer.toString(pk).length(); k++) 
              System.out.print(" "); 
            System.out.print("\t");
            
            String record = new String(catalogCurrentPage, j+5, len-5);
            int length, k = 0;
            while(e.hasMoreElements()){      
              String s = (String)e.nextElement();
              length = (Integer)hash.get(s);
              System.out.print(record.substring(k, k+length));
              for(int x=0; x<s.length()-length; x++) System.out.print(" ");
              System.out.print("\t");
              k += length;
            }
            System.out.println();
          }
        }
        j += len;
      }
    }

    r.close();        
    System.out.println();
  }

  //  get primary key from user
  //  according to parameter exist or nox-exist primary key
  public static int getPk(boolean b) throws IOException {
    int pk = -1;
    if(!b) {
      do {
        try {
          System.out.println("Please enter key");
          pk = Integer.parseInt(UIHelper.get().readLine());
        } catch (NumberFormatException nfe) { }
      } while(doesPkExist(pk) || (pk == -1));
    } else {

      do {
        try {
          System.out.println("Please enter key");
          pk = Integer.parseInt(UIHelper.get().readLine());
        } catch (NumberFormatException nfe) { }
      } while(!doesPkExist(pk) || (pk == -1));
    }
    return pk;
  }

  // get record data from user
  public static String getRecordFromUser(int pk) throws IOException {

    StringBuilder sb = new StringBuilder();

    sb.append("1");
    sb.append(new String(Utility.intToByteArray(pk)));

    Hashtable fields = catalogCurrentTable.getTableFields();
    Enumeration e = fields.keys();

    String fieldName;
    String input;

    while(e.hasMoreElements()) {
      fieldName = (String)e.nextElement();
      int fieldLen = (Integer)fields.get(fieldName);
      System.out.println("Please enter data of field '" + fieldName + 
        "'" + " max:'" + fieldLen + "'");
      input = UIHelper.get().readLine();
      while(input.length() > fieldLen) {
        System.out.println("Please enter data of field '" + fieldName + 
          "'" + " max:'" + fieldLen + "'");
        input = UIHelper.get().readLine();
      }
      sb.append(input);
      for(int i=0; i<fieldLen-input.length(); i++)
          sb.append(" ");
    }
    return sb.toString();
  }

  // clear buffer
  public static void clearPage(){
    clearPage(0);
  }

  // clear buffer starting from given index
  public static void clearPage(int offset){
    for(int i=offset; i<256; i++) catalogCurrentPage[i] = '0';
  }

  // check whether table name hash is empty
  public static boolean isTableNamesEmpty() {
    return catalogTableNames.isEmpty();
  }
}
