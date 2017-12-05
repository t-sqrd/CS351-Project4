/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Message implements Serializable {
    private static final long serialVersionUID = -5399605122490343339L;

    String username;
    Integer accountNum;
    String balance;
    String ip;
    Integer bid;
    boolean newAccount;
    boolean viewAuctionHouses;
    boolean selectHouse;
    boolean register;
    boolean HOME;
    boolean addItems;
    boolean addAgent;
    boolean placeBid;
    String destination;
    public HashMap<String, Integer> items = new HashMap<>();
    String message;

    private ArrayList<String> itemList = new ArrayList<>();


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

    public void setAuctionItems(ArrayList<String> items){
        this.itemList = items;
    }

    public String getItemList(){
        return itemList.get(0);
    }

    public Integer getAccountNum(){
        return accountNum;
    }

    public boolean hasNewAccountInfo(){
        return (username != null);
    }











}
