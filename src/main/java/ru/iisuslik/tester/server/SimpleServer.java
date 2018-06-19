package ru.iisuslik.tester.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import ru.iisuslik.tester.TesterProto.TestRequest;
import ru.iisuslik.tester.TesterProto.TestResponse;

public class SimpleServer implements Server {

  private ServerSocket serverSocket;

  public SimpleServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  private void handleRequests(Socket socket) {
    System.out.println("Start handling requests in thread " + Thread.currentThread().getName());
    try {
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      while (socket.isConnected()) {
        int size = in.readInt();
        System.out.println("Got next request with size " + size);
        long time = System.currentTimeMillis();
        if (size == 0) {
          System.out.println("Stop listen to client in thread " + Thread.currentThread().getName());
          socket.close();
          return;
        }
        byte[] data = new byte[size];
        in.readFully(data);
        TestResponse response = sortArray(data).
            setAllTime(System.currentTimeMillis() - time).build();
        byte[] responseBytes = response.toByteArray();
        System.out.println("Send response with size " + responseBytes.length);
        out.writeInt(responseBytes.length);
        out.write(responseBytes);
      }
    } catch (IOException e) {
      System.out.println("Problems with client: " + e.getMessage());
    }
  }

  @Override
  public void start(int clientCount) {
    System.out.println("Server starts working");
    Thread[] threads = new Thread[clientCount];
    for (int i = 0; i < clientCount; i++) {
      try {
        Socket socket = serverSocket.accept();
        System.out.println("Simple server has client # " + i);
        Thread t = new Thread(() -> {
          handleRequests(socket);
        });
        t.start();
        threads[i] = t;
      } catch (IOException e) {
        System.out.println("Can't connect to client: " + e.getMessage());
        return;
      }
    }
    try {
      for (Thread thread : threads) {
        thread.join();
      }
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
