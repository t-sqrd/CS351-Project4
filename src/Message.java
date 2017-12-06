/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;

    String username;
    String accountNum;
    String balance;
    boolean newAccount;
    boolean viewAuctionHouses;

    boolean askForList;
    boolean fromHouse;
    boolean selectHouse;
    boolean addAgent;
    boolean placeBid;
    boolean newHouse;
    int bid;
    String destination;
    String message;
    ArrayList<String> list;


    public Message(){}

    public Message(String message){

        username = message;
    }

    public String getMessage(){
        return message;
    }



    public String printInfo(){

        return "User = " + username + '\n' +
                "Account Number = " + accountNum + '\n' +
                "Balance = " + balance;

    }

    public int getAccountNum(){
        return Integer.getInteger(accountNum);
    }

    public boolean hasNewAccountInfo(){
        return (username != null);
    }



}
