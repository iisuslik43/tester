package ru.iisuslik.tester.server;

import com.google.protobuf.InvalidProtocolBufferException;
import ru.iisuslik.tester.TesterProto;
import ru.iisuslik.tester.TesterProto.TestRequest;
import ru.iisuslik.tester.TesterProto.TestResponse;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public interface Server {
  default TestResponse.Builder sortArray(byte[] proto) throws InvalidProtocolBufferException {
    long time = System.currentTimeMillis();
    TesterProto.TestRequest request = TestRequest.parseFrom(proto);
    ArrayList<Integer> arr = new ArrayList<>(request.getArrayList());
    sort(arr);
    TestResponse.Builder response = TestResponse.newBuilder().
        addAllArray(arr).
        setSize(arr.size()).
        setSortTime(System.currentTimeMillis() - time);
    return response;
  }

  default void sort(ArrayList<Integer> arr) {
    for (int i = 0; i < arr.size(); i++) {
      int min = arr.get(i);
      int min_i = i;
      for (int j = i + 1; j < arr.size(); j++) {
        if (arr.get(j) < min) {
          min = arr.get(j);
          min_i = j;
        }
      }
      if (i != min_i) {
        int tmp = arr.get(i);
        arr.set(i, arr.get(min_i));
        arr.set(min_i, tmp);
      }
    }
  }

  void start(int clientCount);


}
