package ru.iisuslik.tester.client;

import javafx.application.Application;
import javafx.stage.Stage;


/**
 * Special class that will start at the beginning
 */
public class ClientMain extends Application {

  @Override
  public void start(Stage primaryStage) {
    new Controller(primaryStage);
  }

  public static void main(String[] args) {
    launch(args);
  }
}
