package com.ferhatelmas;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Enumeration;

// this class holds initialization, operation printing, 
// input getter and input process functions
public class UIHelper {

  // for getting input static standard input reader
  private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

  // get singleton
  public static BufferedReader get(){
    return br;
  }

  // initialization
  // open system catalog file and load indexes
  public static void init() throws IOException {
    if(SystemCat.doesCatalogExist()){
      SystemCat.openCatalogFile();
      SystemCat.fillCatalogTableNames();
    } else {
      SystemCat.createCatalogFile();
    }
  }

  // operations and their related option numbers
  public static void printOperations() {
    System.out.println("\nSIMPLE DATABASE MANAGEMENT SYSTEM");
    System.out.println("\n\tTABLE OPERATIONS");
    System.out.println("\t1.Create Table");
    System.out.println("\t2.Delete Table");
    System.out.println("\t3.List Tables");
    System.out.println("\n\tRECORD OPERATIONS");
    System.out.println("\t4.Add Record");
    System.out.println("\t5.Delete Record");
    System.out.println("\t6.Search Record");
    System.out.println("\t7.List Record");
    System.out.println("\n\t8.Exit");
  }

  //get option and call related catalog function
  public static void processInput() throws IOException {
    int choice = -1;

    do{
      try {
        choice = Integer.parseInt(br.readLine());
      } catch(NumberFormatException nfe) {
        System.out.println("Please choose valid option");
      }
    } while(choice<1 || choice>8);

    switch(choice) {
      case 1:
        createTable();
        break;
      case 2:
        deleteTable();
        break;
      case 3:
        listTables();
        break;
      case 4:
        addRecord();
        break;
      case 5:
        deleteRecord();
        break;
      case 6:
        searchRecord();
        break;
      case 7:
        listRecords();
        break;
      case 8:
        exitSystem();
        break;
      default:
        System.out.println("Please choose valid option");
        break;
    }
  }

  // create table
  // get non-exist table name and field name and lengths
  // check values to fit into one page
  public static void createTable() throws IOException {
    String name = getTableName(false);
    String fields = getTableFieldLabelAndLength();
    int recordLen = getRecordLength(fields);
    int len = 3;
    len += name.length();
    len += fields.length();
    if(len > 256 || recordLen > 256) {
      System.out.println("Entered values are over the preassumed ones");
      return;
    }
    SystemCat.createTable(name, fields);
}

  // delete table
  // get exist table name
  public static void deleteTable() throws IOException {
    if(SystemCat.isTableNamesEmpty()) {
      System.out.println("There is no table to delete");
      return;
    }
    String name = getTableName(true);
    SystemCat.deleteTable(name);
  }

  // list tables
  public static void listTables() {
    SystemCat.listTables();
  }

  // add record
  // get exist table name
  // load data of given table
  // add record into this table
  public static void addRecord() throws IOException {
    if(SystemCat.isTableNamesEmpty()) {
      System.out.println("There is no table to add record to it");
      return;
    }
    String name = getTableName(true);
    SystemCat.loadTable(name);
    SystemCat.addRecord();
  }

  // delete record
  // get exist table name
  // load data of given table
  // delete record from this table
  public static void deleteRecord() throws IOException {
    if(SystemCat.isTableNamesEmpty()) {
      System.out.println("There is no table to delete record from it");
      return;
    }
    String name = getTableName(true);
    SystemCat.loadTable(name);
    SystemCat.deleteRecord();
  }

  // search record
  // get exist table name
  // load data of given table
  // search records for given primary key
  public static void searchRecord() throws IOException {
    if(SystemCat.isTableNamesEmpty()) {
      System.out.println("There is no table to search record in it");
      return;
    }
    String name = getTableName(true);
    SystemCat.loadTable(name);
    SystemCat.searchAndListRecords();
  }

  // list records
  // get exist table name
  // load data of given table
  // list records from file of table
  public static void listRecords() throws IOException {
    if(SystemCat.isTableNamesEmpty()) {
      System.out.println("There is no table to list records in it");
      return;
    }
    String name = getTableName(true);
    SystemCat.loadTable(name);
    SystemCat.listRecords();
  }

  // close system catalog file and exit
  public static void exitSystem() throws IOException {
    SystemCat.closeCatalogFile();
    System.exit(1);
  }

  // get one table name
  // true --> get existing table name
  // false -->get new table name
  public static String getTableName(boolean choice) throws IOException {
    System.out.println("Please enter table name");
    String name = br.readLine();
    if(choice){
      while(!SystemCat.doesTableExist(name)){
        System.out.println("Please enter table name");
        name = br.readLine();
      }
    } else {
      while(SystemCat.doesTableExist(name)){
        System.out.println("Please enter table name");
        name = br.readLine();
      }
    }
    return name;
  }

  // get fields and their lengths
  // form a string that is writable into catalog file
  public static String getTableFieldLabelAndLength() throws IOException {
    int fieldNum = -1;
    while(fieldNum <= 0 || fieldNum > 80){
      try {
        System.out.println("Please enter number of fields");
        fieldNum = Integer.parseInt(br.readLine());
      } catch (NumberFormatException nfe) {
        System.out.println("Please enter number of fields");
        fieldNum = Integer.parseInt(br.readLine());
      }
    }
    StringBuilder sb = new StringBuilder();
    for(int i=0; i<fieldNum; i++){
      System.out.println("Please enter label of field " + "'"+ (i+1) + "'");
      String field = br.readLine();

      sb.append(field);
      sb.append(",");
      while(true){
        try {
          System.out.println("Please enter length of field " + "'"+ field + "'");
          sb.append(new String(Utility.shortToByteArray(Short.parseShort(br.readLine()))));
          break;
        } catch (NumberFormatException nfe){}
      }
    }
    return sb.toString();
  }

  // calculates possible record length
  public static int getRecordLength(String fields) {
    int len = 5;
    for(int i=0; i<fields.length(); i++){
      if(fields.charAt(i) == ','){
        len += Utility.byteArrayToShort(fields.getBytes(), ++i);
        i++;
      }
    }
    return len;
  }
}