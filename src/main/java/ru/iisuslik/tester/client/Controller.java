package ru.iisuslik.tester.client;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class Controller {
  private Stage primaryStage;
  private GridPane root;

  public static void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setContentText(content);
    alert.setHeaderText(null);
    alert.showAndWait();
  }

  private TextField textField(int i, String text) {
    TextField res = new TextField();
    res.setPromptText(text);
    res.setPrefSize(500, 100);
    res.setPrefColumnCount(10);
    root.add(res, 0, i);
    return res;
  }

  public Controller(Stage stage) {
    primaryStage = stage;
    root = new GridPane();
    root.setPadding(new Insets(10, 10, 10, 10));
    root.setVgap(5);
    root.setHgap(5);
    primaryStage.setScene(new Scene(root, 600, 600));
    ;
    primaryStage.show();
    TextField hostT = textField(0, "Choose host");
    TextField portT = textField(1, "Choose port");
    TextField architectureT = textField(2, "Choose architecture: 1,2,3");
    TextField arrSizeT = textField(3, "Choose array size");
    TextField deltaT = textField(4, "Choose delta");
    TextField requestCountT = textField(5, "Choose request count");
    TextField clientCountT = textField(6, "Choose client count");
    TextField minT = textField(7, "Choose min");
    TextField maxT = textField(8, "Choose max");
    TextField dT = textField(9, "Choose d");
    Button run = new Button();
    run.setPrefSize(200, 100);
    run.setText("RUN");
    run.setOnAction(s -> {
      try {
        String host = hostT.getText();
        int port = Integer.parseInt(portT.getText());
        int architecture = Integer.parseInt(architectureT.getText());
        int arrSize = Integer.parseInt(arrSizeT.getText());
        int delta = Integer.parseInt(deltaT.getText());
        int requestCount = Integer.parseInt(requestCountT.getText());
        int clientCount = Integer.parseInt(clientCountT.getText());
        int min = Integer.parseInt(minT.getText());
        int max = Integer.parseInt(maxT.getText());
        int d = Integer.parseInt(dT.getText());
      } catch (NumberFormatException e) {
        e.printStackTrace();
      }
    });
    root.add(run, 1, 0);

    Button defaultRun = new Button();
    defaultRun.setPrefSize(200, 100);
    defaultRun.setText("DEFAULT");
    defaultRun.setOnAction(e -> {
      try {
        List<TestInfo> res = ClientController.changeArrSize("localhost", 8888, 4, 20,
            5000, 30000, 4000, 10, 2);
        showGraphics(res);
      } catch (IOException e1) {
        e1.printStackTrace();
        showAlert("ERROR", "Can't run tests:\n" + e1.getMessage());
        System.exit(1);
      }
    });
    root.add(defaultRun, 1, 1);
  }

  private void showGraphics(List<TestInfo> infos) {
    SplitPane splitPane = new SplitPane();
    splitPane.setOrientation(Orientation.HORIZONTAL);

    LineChart chart = new LineChart(new NumberAxis(), new NumberAxis());
    List<XYChart.Data<Object, Object>> listAll = infos.stream().map(i -> new XYChart.Data<Object, Object>(i.changed, i.allTime)).
        collect(Collectors.toList());
    List<XYChart.Data<Object, Object>> listClient = infos.stream().map(i -> new XYChart.Data<Object, Object>(i.changed, i.clientTime)).
        collect(Collectors.toList());
    List<XYChart.Data<Object, Object>> listSort = infos.stream().map(i -> new XYChart.Data<Object, Object>(i.changed, i.sortTime)).
        collect(Collectors.toList());
    chart.getData().add(new XYChart.Series<>("ALL", FXCollections.observableArrayList(listAll)));
    chart.getData().add(new XYChart.Series<>("CLIENT", FXCollections.observableArrayList(listClient)));
    chart.getData().add(new XYChart.Series<>("SORT", FXCollections.observableArrayList(listSort)));
    splitPane.getItems().addAll(chart);
    Scene scene = new Scene(splitPane, 600, 600);
    primaryStage.setScene(scene);
  }
}
