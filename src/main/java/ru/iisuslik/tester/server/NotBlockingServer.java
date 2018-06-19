package ru.iisuslik.tester.server;

import ru.iisuslik.tester.client.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotBlockingServer implements Server {

  private ServerSocketChannel serverChannel;
  private Selector readSelector = Selector.open();
  private Selector writeSelector = Selector.open();
  private int currentClientCount;
  private ExecutorService pool = Executors.newCachedThreadPool();


  public NotBlockingServer(int port) throws IOException {
    serverChannel = ServerSocketChannel.open();
    serverChannel.socket().bind(new InetSocketAddress(port));
  }

  private void read() {
    while (currentClientCount != 0) {
      Set<SelectionKey> selectedKeys = readSelector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        ClientData data = (ClientData) key.attachment();
        SocketChannel channel = (SocketChannel) key.channel();
        try {
          if (data.readBuffer == null) {
            channel.read(data.readSize);
            if (data.readSize.position() == 4) {
              data.time = System.currentTimeMillis();
              data.readSize.flip();
              int size = data.readSize.getInt();
              data.readSize.clear();
              data.readBuffer = ByteBuffer.allocate(size);
            }
          } else {
            channel.read(data.readBuffer);
            if(data.readBuffer.position() == data.readBuffer.capacity()) {
              Runnable task = () -> {
                
              };
              synchronized (pool) {
                pool.submit(task);
              }
            }
          }
        } catch (IOException e) {
          e.printStackTrace();
        }
        keyIterator.remove();
      }
    }

  }

  private void write() {
    while (currentClientCount != 0) {
      Set<SelectionKey> selectedKeys = writeSelector.selectedKeys();
      Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
      while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();

        keyIterator.remove();
      }
    }
  }

  @Override
  public void start(int clientCount) {
    System.out.println("Server starts working");
    currentClientCount = clientCount;
    Thread reader = new Thread(this::read);
    Thread writer = new Thread(this::write);
    reader.start();
    writer.start();

    for (int i = 0; i < clientCount; i++) {
      try {
        SocketChannel socketChannel = serverChannel.accept();
        socketChannel.configureBlocking(false);
        ClientData clientData = new ClientData();
        synchronized (readSelector) {
          SelectionKey key = socketChannel.register(readSelector, SelectionKey.OP_READ, clientData);
        }
      } catch (IOException e) {
        e.printStackTrace();

      }
    }
  }
}
