
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class Account extends HashMap{

    private String clientName;
    private double initialAmount;
    private HashMap<String, Double> NAME_AMOUNT = new HashMap<>();
    //private HashMap<Integer, HashMap<String, Double>> accountNumbers = new HashMap<>();
    private static double FIXED_INITIAL_DEPOSIT = 5.00;


    private String clientInfo = "";


    public Account(String clientName){
        this.clientName = clientName;

    }

}
