
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */
public class Bank extends Thread
{


    private static HashMap<Integer, Account> bankMap = new HashMap<>();
    private static ArrayList<Integer> registeredKeys = new ArrayList<>();
    private Socket bankSocket;

    private static ArrayList<Bank> threads = new ArrayList<>();
    private static final int PORT_NUMBER = 8080;
    private String clientName;

    private volatile boolean KILL;
    private ObjectInputStream fromClient;
    private ObjectOutputStream toClient;


    // Create Bank object from socket
    public Bank(Socket socket)
    {
        this.bankSocket = socket;

        try
        {

            toClient = new ObjectOutputStream(bankSocket.getOutputStream());
            fromClient = new ObjectInputStream(bankSocket.getInputStream());

        } catch (IOException e)
        {

        }

    }

    // start bank server and listen
    // for messages
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

                        // Form response based on message request

                        if (request.newAccount)
                        {
                            makeAccount(request);
                            printAllClients();
                        }

                        if (request.verify)
                        {
                            Message response = new Message();
                            System.out.println("Entered If statement");
                            clientName = "CENTRAL";

                            Integer key = request.bankKey;
                            if (bankMap.containsKey(key))
                            {

                                if (!registeredKeys.contains(key))
                                {

                                    response.isMember = true;
                                    response.message = "USER IS MEMBER";
                                    response.fromBank = true;
                                    sendMessage(response);
                                } else
                                {
                                    response.message = "Account already registered";
                                    response.isMember = false;
                                    sendMessage(response);

                                }

                            } else
                            {

                                response.isMember = false;
                                response.message = "Bank Account not found";
                                sendMessage(response);
                            }


                            for (Account i : bankMap.values())
                            {
                                System.out.println("Account = " + i.clientName);
                            }

                        }
                        if (request.isOver)
                        {
                            System.out.println("TESTING");
                            placeHold(request);
                        }

                        if (request.placeHold)
                        {
                            System.out.println("ENTER PLACE HOLD");
                            placeHold(request);

                        }

                        if (request.KILL)
                        {
                            Message response = new Message();
                            response.message = "Goodbye...";
                            sendMessage(response);
                            KILL = true;
                            break;
                        }

                        printAllClients();

                    }
                } catch (ClassNotFoundException ex)
                {

                } catch (IOException ex)
                {
                    System.out.println("Unable to get streams from client in bank server " + ex.getMessage());

                }
            }
        } finally
        {
            try
            {
                System.out.println(clientName + " is exiting bank...");
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

    private void placeHold(Message request)
    {

        Integer bankKey = request.bankKey;
        Integer amount = request.bidAmount;
        System.out.println("bankkey->" + bankKey);
        System.out.println("Amount->" + amount);
        System.out.println(request.username);

        if (bankMap.containsKey(bankKey))
        {
            System.out.println("ENTERED");
            Account account = bankMap.get(bankKey);

            if (account != null)
            {
                Message response = account.placeHoldOnAccount(request);
                System.out.println("A " + response.username + " " + response.message);
                response.username = request.username;
                response.fromBank = true;
                response.toUser = true;
                sendMessage(response);
            }

        }
    }


    private void makeAccount(Message request)
    {

        Message response = new Message();
        clientName = request.username;
        Account account = new Account(request.username);
        bankMap.put(account.getKey(), account);
        response.message = account.getAccountInfo();
        response.bankKey = account.getKey();
        sendMessage(response);
    }

    private void sendMessage(Message msg)
    {
        try
        {

            System.out.println(msg.username + " " + msg.message);

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e)
        {

        }
    }


    private void printAllClients()
    {
        for (Bank t : threads)
        {
            System.out.println("Users Connected to bank-> " + t.clientName);

        }
    }


    // Start the bank
    public static void main(String[] args)
    {
        System.out.println("Banking Server connected...");
        System.out.println("On port " + PORT_NUMBER);
        ServerSocket server = null;
        try
        {
            server = new ServerSocket(PORT_NUMBER);

            while (!server.isClosed())
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
