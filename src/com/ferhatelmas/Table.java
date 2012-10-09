package com.ferhatelmas;

import java.util.Hashtable;
import java.util.Enumeration;

public class Table {

  private String tableName;     

  // length of each field
  private Hashtable tableFields; 

  public Table() {
    this.tableName = null;
    this.tableFields = new Hashtable();
  }

  public Table(String tableName, Hashtable tableFields) {
    this.tableName = tableName;
    this.tableFields = tableFields;
  }

  //************************* getter and setters *****************************
  public String getTableName() {
    return tableName;
  }

  public void setTableName(String tableName) {
    this.tableName = tableName;
  }

  public Hashtable getTableFields() {
    return tableFields;
  }

  public void setTableFields(Hashtable tableFields) {
    this.tableFields = tableFields;
  }
  //************************** getter and setters end ************************


  // get sum of length of fields in the table
  // initial 5 is for primary key + deletion marker
  public int getFieldsTotalLength(){

    Enumeration e = tableFields.keys();
    int total = 5;
    while(e.hasMoreElements()){
      total += (Integer)tableFields.get(e.nextElement());
    }
    return total;
  }
}