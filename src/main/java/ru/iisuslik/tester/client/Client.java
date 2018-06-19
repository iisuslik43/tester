package ru.iisuslik.tester.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.iisuslik.tester.TesterProto.TestRequest;
import ru.iisuslik.tester.TesterProto.TestResponse;

public class Client {
  private int arrSize;
  private Socket socket;
  private DataInputStream in;
  private DataOutputStream out;
  private int delta;
  private int requestCount;
  private TestInfo info = new TestInfo();

  public Client(String host, int port, int arrSize, int delta, int requestCount) throws IOException {
    System.out.println(host + port);
    socket = new Socket(host, port);
    in = new DataInputStream(socket.getInputStream());
    out = new DataOutputStream(socket.getOutputStream());
    this.arrSize = arrSize;
    this.delta = delta;
    this.requestCount = requestCount;
  }

  public void start(){
    try {
      for (int i = 0; i < requestCount; i++) {
        TestRequest request = TestRequest.newBuilder().
            addAllArray(getRandomList()).
            setSize(arrSize).
            build();
        byte[] requestBytes = request.toByteArray();
        System.out.println("Request size: " + requestBytes.length);
        long time = System.currentTimeMillis();
        out.writeInt(requestBytes.length);
        out.write(requestBytes);
        int size = in.readInt();
        System.out.println("Got response with size: " + size);
        byte[] responseBytes = new byte[size];
        in.readFully(responseBytes);
        info.clientTime += System.currentTimeMillis() - time;
        TestResponse response = TestResponse.parseFrom(responseBytes);
        info.allTime += response.getAllTime();
        info.sortTime += response.getSortTime();
        try {
          Thread.sleep(delta);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        info.print();
      }
      out.writeInt(0);
      socket.close();
    } catch (IOException e) {
      e.printStackTrace();
      info.clientTime = info.allTime = info.sortTime = -1;
    }
  }

  private List<Integer> getRandomList() {
    Random rand = new Random();
    ArrayList<Integer> list = new ArrayList<>();
    for (int i = 0; i < arrSize; i++) {
      list.add(rand.nextInt());
    }
    return list;
  }

  public TestInfo getInfo() {
    return info;
  }
}
