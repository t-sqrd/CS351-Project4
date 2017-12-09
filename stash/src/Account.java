
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Account extends HashMap
{

    private String clientName;
    private static HashMap<Integer, String> ACCOUNT_MAP = new HashMap<>();
    private Encrypt encrypt;
    private BigInteger MY_PUBLIC_KEY;
    private Integer clientNumber;
    private int balance;
    private Map<String, Integer> holds = new HashMap<>();


    public Account(String clientName)
    {
        this.clientName = clientName;
        this.clientNumber = makeAccountNumber();
        ACCOUNT_MAP.put(clientNumber, clientName);
        this.encrypt = new Encrypt(clientNumber.toString());
        this.MY_PUBLIC_KEY = encrypt.getPublic();
        int initialDeposit = 100;
        this.balance = initialDeposit;

        System.out.println("Account [" + clientName + "] has been created...");

    }


    // Get account information to send back
    public String returnPackage()
    {

        String packet = "User = " + clientName + ", " + "Account Number = " + clientNumber + " , "
                + "Your Public Key = " + MY_PUBLIC_KEY + " , "
                + "Balance = " + balance;

        return packet;

    }

    private int makeAccountNumber()
    {
        Random rand = new Random();
        int x = rand.nextInt(100000);
        if (ACCOUNT_MAP.containsKey(x))
        {
            makeAccountNumber();
        }

        return x;
    }

    public int getHold(String item)
    {
        if (holds.containsKey(item))
        {
            return holds.get(item);
        }
        return -1;
    }

    // places a hold on an account and
    // removes bid amount from balance
    public Boolean placeHold(Integer bid, String item)
    {
        System.out.println("bid is " + bid);
        System.out.println("placed hold on account of bid value: " + bid.intValue());
        if (bid > balance)
        {
            System.out.println(clientName + " does not have enough money to bid " + bid);
            System.out.println("account balance is: " + balance);
            return false;
        } else
        {
            holds.put(item, bid);
            balance -= bid;
            System.out.println("account balance now is: " + balance);
            return true;
        }
    }

    // Removes a hold from an account
    // and adds the bid back to account balance
    public String removeHold(String item)
    {
        if (holds.containsKey(item))
        {
            Integer bid = holds.get(item);
            balance += bid;
            holds.remove(item);
            return "Bank added back the " + bid + " that was used to bid for " + item;
        }
        return "";
    }


}