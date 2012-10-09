package com.ferhatelmas;

import java.io.IOException;

public class UI {

  public static void main(String[] args) throws IOException {

    // checks system catalog and loads table names
    UIHelper.init();                

    do {
      // print operations and their numbers
      UIHelper.printOperations(); 
      // get input and process input according to operation
      UIHelper.processInput();    
    } while(true);
  }
}
