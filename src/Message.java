/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;

    String username;
    String accountNum;
    String balance;
    boolean newAccount;
    boolean viewAuctionHouses;
    boolean listOfAuctionHouses;
    boolean askForList;
    boolean fromHouse;
    String destination;
    String message;
    ArrayList<String> list;


    public Message(){

    }


    public String printInfo(){

        return "User = " + username + '\n' +
                "Account Number = " + accountNum + '\n' +
                "Balance = " + balance;

    }

    public String getMessage(){

        return "From Server: " + message;
    }


    public int getAccountNum(){
        return Integer.getInteger(accountNum);
    }

    public boolean hasNewAccountInfo(){
        return (username != null);
    }











}
