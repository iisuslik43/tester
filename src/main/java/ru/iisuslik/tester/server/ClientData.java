package ru.iisuslik.tester.server;

import java.nio.ByteBuffer;

public class ClientData {
  public ByteBuffer readBuffer;
  public ByteBuffer writeBuffer;
  public ByteBuffer readSize = ByteBuffer.allocate(4);
  public ByteBuffer writeSize = ByteBuffer.allocate(4);
  public long time;
}
