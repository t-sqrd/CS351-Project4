/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class Main extends Application {
    private final int WIDTH = 1000;
    private final int HEIGHT = 1000;
    private GridPane pane = new GridPane();
    private Group root = new Group();

    public static void main(String[] args) {

        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        root.getChildren().add(pane);
        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();

        Display display = new Display(HEIGHT, WIDTH, pane);
        display.initialize();

    }
}
