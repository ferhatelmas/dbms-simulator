package com.ferhatelmas;

public class Utility {

  // convert byte array to short from starting zeroth index
  public static short byteArrayToShort(byte [] b) {
    return byteArrayToShort(b, 0);
  }

  // convert byte array to short from starting given index
  public static short byteArrayToShort(byte[] b, int offset) {
    short value = 0;
    for (int i = 0; i < 2; i++) {
      int shift = (1 - i) * 8;
      value += (b[i + offset] & 0x000000FF) << shift;
    }
    return value;
  }

  // convert byte array to int from starting zeroth index
  public static int byteArrayToInt(byte[] b) {
    return byteArrayToInt(b, 0);
  }

  // convert byte array to int from starting given index
  public static int byteArrayToInt(byte[] b, int offset) {
    int value = 0;
    for (int i = 0; i < 4; i++) {
      int shift = (3 - i) * 8;
      value += (b[i + offset] & 0x000000FF) << shift;
    }
    return value;
  }

  // convert given short to new byte array
  public static byte[] shortToByteArray(int value) {
    byte[] b = new byte[2];
    b[0] = (byte)((value >>> 8) & 0xFF);
    b[1] = (byte)(value & 0xFF);
    return b;
  }

  // convert given short to byte array by filling given buffer from given index
  public static void shortToByteArray(byte[] b, int offset, int value) {
    for (int i = 0; i < 2; i++) {
      int shift = (1 - i) * 8;
      b[i + offset] = (byte) ((value >>> shift) & 0xFF);
    }
  }

  // convert given int to new byte array
  public static byte[] intToByteArray(int value) {
    byte[] b = new byte[4];
    for (int i = 0; i < 4; i++) {
      int offset = (b.length - 1 - i) * 8;
      b[i] = (byte) ((value >>> offset) & 0xFF);
    }
    return b;
  }

  // convert given int to byte array by filling given buffer from given index
  public static void intToByteArray(byte[] b, int offset, int value) {
    for (int i = 0; i < 4; i++) {
      int shift = (3 - i) * 8;
      b[i + offset] = (byte) ((value >>> shift) & 0xFF);
    }
  }
}