package ru.iisuslik.tester.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
  public static void main(String[] args) {
    int PORT = Integer.parseInt(args[0]);
    try {
      ServerSocket mainSocket = new ServerSocket(PORT);
      System.out.println("Waiting for client  in port " + PORT);
      Socket socket = mainSocket.accept();
      System.out.println("Start Working with client");
      DataInputStream in = new DataInputStream(socket.getInputStream());
      int type = in.readInt();
      int testsCount = in.readInt();
      System.out.println("Have got from client architecture # " + type + " and test count " + testsCount);
      mainSocket.close();
      for (int i = 0; i < testsCount; i++) {
        Server server;
        switch (type) {
          case 1:
            server = new SimpleServer(PORT);
            break;
          case 2:
            server = new BlockingServer(PORT);
            break;
          default:
            System.err.println("Bad architecture");
            return;
        }
        int clientCount = in.readInt();
        System.out.println("Start server # " + i);
        server.start(clientCount);
      }
      socket.close();
    } catch (IOException e) {
      System.err.println("Can't archive first request");
      e.printStackTrace();
      return;
    }
  }

}
