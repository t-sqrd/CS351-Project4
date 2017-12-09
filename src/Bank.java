
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */
public class Bank extends Thread
{


    private static HashMap<Integer, Account> bankMap = new HashMap<>();
    private Socket bankSocket;
    private final int MAX_ACCOUNTS = 10000;
    private static HashMap<Integer, Account> listOfAccountNums = new HashMap<>();
    private Account account;


    public static ArrayList<Bank> threads = new ArrayList<>();
    public static final int PORT_NUMBER = 8080;
    public static final int AUCTION_CENTRAL_PORT = 8081;
    public String clientName;

    private volatile boolean KILL;
    ObjectInputStream fromClient;
    ObjectOutputStream toClient;
    String host = "127.0.0.1";


    // Start up bank to hold agent accounts
    public Bank(Socket socket)
    {
        this.bankSocket = socket;

        try
        {

            toClient = new ObjectOutputStream(bankSocket.getOutputStream());
            fromClient = new ObjectInputStream(bankSocket.getInputStream());

        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

    }

    // Start listening for messages
    public void run()
    {

        try
        {
            Message request;
            while (!KILL)
            {

                try
                {

                    while ((request = (Message) fromClient.readObject()) != null)
                    {

                        // Read in messages and respond based on type
                        if (request.newAccount)
                        {
                            makeAccount(request);
                        }
                        if (request.isWin)
                        {
                            System.out.println("transferring funds from " + request.bankKey + " to auction house");
                            Message m = getAccountInfo(request.bankKey);
                            sendToUser(request.username, m);
                            Message msg = new Message();
                            msg.username = request.username;
                            msg.fromBank = true;
                            Account a = bankMap.get(request.bankKey);
                            msg.message = "" + a.getHold(request.destination);
                            sendToUser("CENTRAL", msg);
                        }
                        if (request.isLoss)
                        {
                            String s = removeHold(request.bankKey, request.destination);
                            s += "\n\n" + getAccountInfo(request.bankKey);
                            System.out.println(s);
                            Message m = new Message();
                            m.message = s;
                            m.fromBank = true;
                            sendToUser(request.agentName, m);
                        }

                        if (request.placeBid)
                        {
                            System.out.println("placing a hold of " + request.bid + " on " + request.username + "'s account.");
                            Boolean held = placeHold(request.bankKey, request.message, request.bid);
                            if (held)
                            {
                                System.out.println("Successfully placed a hold on the account of " + request.bid);
                            } else
                            {
                                System.out.println("Not enough money to place a bid of " + request.bid);
                            }
                        }

                        if (request.verify)
                        {
                            Message response = new Message();
                            this.clientName = "CENTRAL";
                            response.isMember = bankMap.containsKey(request.bankKey);
                            response.message = "USER IS MEMBER";
                            response.fromBank = true;
                            sendMessage(response);

                        }
                        if (request.KILL)
                        {
                            Message response = new Message();
                            response.message = "Goodbye...";
                            sendMessage(response);
                            KILL = true;
                            break;
                        }


                    }
                } catch (ClassNotFoundException ex)
                {
                    System.out.println(ex.getCause());
                } catch (IOException ex)
                {
                    System.out.println(ex.getCause());
                }
            }
        } finally
        {
            try
            {
                System.out.println("Agent exiting...");
                threads.remove(this);
                toClient.close();
                fromClient.close();
                bankSocket.close();

            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    private Message getAccountInfo(int bankKey)
    {
        Account a = bankMap.get(bankKey);

        Message m = new Message();
        m.message = a.returnPackage();
        return m;
    }

    private Boolean placeHold(int bankKey, String item, int bid)
    {
        Account a = bankMap.get(bankKey);
        Boolean result = a.placeHold(bid, item);
        return result;
    }

    private String removeHold(int bankKey, String item)
    {
        Account a = bankMap.get(bankKey);
        return a.removeHold(item);
    }


    private void makeAccount(Message request)
    {
        Message response = new Message();
        clientName = request.username;
        Account account = new Account(request.username);
        Integer key = makeBankKey();
        bankMap.put(key, account);
        response.message = account.returnPackage();
        response.message += "\nBank key = " + key;
        response.bankKey = key;
        sendMessage(response);
    }


    private void sendMessage(Message msg)
    {
        try
        {

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e)
        {

        }
    }

    private void sendToUser(String name, Message m)
    {
        for (Bank t : threads)
        {
            if (t.clientName != null)
            {
                if (t.clientName.equals(name))
                {
                    t.sendMessage(m);
                }
            }
        }
    }


    private int makeBankKey()
    {
        Random rand = new Random();
        int key = rand.nextInt(50);
        if (bankMap.containsKey(key))
        {
            makeBankKey();
        }

        bankMap.put(key, account);
        return key;

    }

    public static void main(String[] args)
    {
        System.out.println("Banking Server connected...");
        System.out.println("On port " + PORT_NUMBER);
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(PORT_NUMBER);

            while (true)
            {
                Bank b = new Bank(server.accept());
                threads.add(b);
                b.start();

            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
            System.out.println("Unable to start Banking server.");
        } finally
        {
            try
            {
                if (server != null)
                    server.close();
            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }
}