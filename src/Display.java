import javafx.animation.AnimationTimer;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */
public class Display extends AnimationTimer{
    private int HEIGHT;
    private int WIDTH;
    private GridPane pane;
    private String username;
    private String initAmount;
    Button enter = new Button("Enter");
    VBox box = new VBox();
    Label label = new Label();
    TextField name = new TextField();
    TextField amount = new TextField();


    public Display(int HEIGHT, int WIDTH, GridPane pane){
        this.pane = pane;
        this.HEIGHT = HEIGHT;
        this.WIDTH = WIDTH;

    }

    public void initialize(){

        enter.setOnAction(event->{
            if(name.getText().isEmpty() || amount.getText().isEmpty()){
                System.out.println("Incomplete Fields!");
            }
            else {
                for (int i = 0; i < amount.getText().length(); i++) {
                    if (!Character.isDigit(amount.getText().charAt(i))) {
                        System.out.println("Amount must be a number!");
                        break;
                    } else {
                        label.setText("Name : " + name.getText() + "\n Amount = " + amount.getText());
                        //Agent agent = new Agent("127.0.0.1", 8081, amount.getText(), name.getText());
                        username = name.getText();
                        initAmount = amount.getText();
                        Agent agent = new Agent("127.0.0.1", 8081, initAmount, username);


                    }
                }
            }

        });


        box.setPrefSize(200, 200);
        box.setTranslateX(200);
        box.setTranslateY(200);
        name.setPromptText("Enter Name: ");
        amount.setPromptText("Enter initial amount: ");
        box.getChildren().addAll(enter, name, amount);
        pane.getChildren().addAll(box, label);
    }


    @Override
    public void handle(long now) {


    }
}
