package ru.iisuslik.tester.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientController {
  public static List<TestInfo> changeClientCount(String host, int port, int arrSize, int delta, int clientCount1,
                                                 int clientCount2, int d, int requestCount, int architecture)
      throws IOException {
    Socket socket = sendInit(host, port, (clientCount2 - clientCount1) / d + 1, architecture);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    List<TestInfo> infoList = new ArrayList<>();
    for (int i = clientCount1; i <= clientCount2; i += d) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
      }
      System.out.println("Invoke tests with client count " + i);
      out.writeInt(i);

      TestInfo info = invokeTests(host, port, arrSize, delta, i, requestCount);
      info.changed = i;
      infoList.add(info);
    }
    socket.close();
    return infoList;
  }

  public static List<TestInfo> changeArrSize(String host, int port, int clientCount, int delta,
                                             int arrSize1, int arrSize2, int d, int requestCount, int architecture)
      throws IOException {
    Socket socket = sendInit(host, port, (arrSize2 - arrSize1) / d + 1, architecture);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    List<TestInfo> infoList = new ArrayList<>();
    for (int i = arrSize1; i <= arrSize2; i += d) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
      }
      System.out.println("Invoke tests with array size " + i);
      out.writeInt(clientCount);
      TestInfo info = invokeTests(host, port, i, delta, clientCount, requestCount);
      info.changed = i;
      infoList.add(info);
    }
    socket.close();
    return infoList;
  }

  public static List<TestInfo> changeDelta(String host, int port, int clientCount, int arrSize,
                                           int delta1, int delta2, int d, int requestCount, int architecture)
      throws IOException {
    Socket socket = sendInit(host, port, (delta2 - delta1) / d + 1, architecture);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.writeInt(clientCount);
    List<TestInfo> infoList = new ArrayList<>();
    for (int i = delta1; i <= delta2; i += d) {
      try {
        Thread.sleep(20);
      } catch (InterruptedException e) {
      }
      System.out.println("Invoke tests with delta " + i);
      TestInfo info = invokeTests(host, port, arrSize, i, clientCount, requestCount);
      info.changed = i;
      infoList.add(info);
    }
    socket.close();
    return infoList;
  }

  private static TestInfo invokeTests(String host, int port, int arrSize,
                                      int delta, int clientCount, int requestCount) throws IOException {
    TestInfo info = new TestInfo();
    Client[] clients = new Client[clientCount];
    Thread[] threads = new Thread[clientCount];
    for (int i = 0; i < clientCount; i++) {
      System.out.println(i);
      Client client = new Client(host, port, arrSize, delta, requestCount);
      Thread thread = new Thread(client::start);
      thread.start();
      threads[i] = thread;
      clients[i] = client;
    }
    for (int i = 0; i < clientCount; i++) {
      try {
        threads[i].join();
        System.out.println("Thread # " + i + " has finished");
        info.add(clients[i].getInfo());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    return info;
  }

  private static Socket sendInit(String host, int port, int testCount, int architecture) throws IOException {
    System.out.println("Send init info for test count " + testCount);
    Socket socket = new Socket(host, port);
    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.writeInt(architecture);
    out.writeInt(testCount);
    return socket;
  }
}
