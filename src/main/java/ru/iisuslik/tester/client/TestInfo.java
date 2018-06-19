package ru.iisuslik.tester.client;

public class TestInfo {
  public int sortTime;
  public int allTime;
  public int clientTime;
  public int changed;
  public void add(TestInfo o) {
    sortTime += o.sortTime;
    allTime += o.allTime;
    clientTime += o.clientTime;
  }

  public String print() {
    return print("");
  }
  public String print(String text) {
    String s = text + "all time " + allTime + " | sort time " + sortTime + " | client time " + clientTime;
    System.out.println(s);
    return s;
  }
}
