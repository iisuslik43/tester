package ru.iisuslik.tester.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.iisuslik.tester.TesterProto;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class BlockingServer implements Server {

  private ServerSocket serverSocket;
  private ExecutorService pool = Executors.newCachedThreadPool();
  private List<ExecutorService> singlePools = new ArrayList<>();

  public BlockingServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
  }

  private void handleRequests(Socket socket) {
    System.out.println("Start handling requests in thread " + Thread.currentThread().getName());
    try {
      DataInputStream in = new DataInputStream(socket.getInputStream());
      DataOutputStream out = new DataOutputStream(socket.getOutputStream());
      ExecutorService singlePool = Executors.newSingleThreadExecutor();
      synchronized (singlePools) {
        singlePools.add(singlePool);
      }
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

        Runnable task = () -> {
          TesterProto.TestResponse response = null;
          try {
            response = sortArray(data).
                setAllTime(System.currentTimeMillis() - time).build();
          } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
          }
          byte[] responseBytes = response.toByteArray();
          singlePool.submit(() -> {
            try {
              System.out.println("Send response with size " + responseBytes.length);
              out.writeInt(responseBytes.length);
              out.write(responseBytes);
            } catch (IOException e) {
              e.printStackTrace();
            }
          });
        };
        synchronized (pool) {
          pool.submit(task);
        }
      }
      socket.close();
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
    pool.shutdown();
    for (ExecutorService p : singlePools) {
      p.shutdown();
    }
    try {
      serverSocket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
