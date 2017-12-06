
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */
public class Bank extends Thread {


    private HashMap<Integer, Account> bankMap = new HashMap<>();
    private static LinkedList<Account> accounts = new LinkedList<>();
    private Socket socket;
    private final int MAX_ACCOUNTS = 10000;
    private static HashMap<Integer, Account> listOfAccountNums = new HashMap<>();
    private String clientName;
    private Account account;

    private Socket auctionSocket;
    public static final int PORT_NUMBER = 8080;
    public static final int AUCTION_CENTRAL_PORT = 8081;

    ObjectInputStream fromClient;
    ObjectOutputStream toClient;

    String host = "127.0.0.1";

    public Bank(Socket socket) {
        this.socket = socket;

        try {
            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {

        }

        start();

    }

    public void run() {

        try {


            Message request;
            Message response;

            try {
//
                while ((request = (Message) fromClient.readObject()) != null) {


                    if (request.newAccount) {
                        response = new Message();
                        Account account = new Account(request.username);
                        bankMap.put(account.getAccountNumber(), account);
                        response.message = account.returnPackage();
                        sendMessage(response);
                    }
                    if (request.KILL) {
                        response = new Message();
                        response.message = "Goodbye...";
                        sendMessage(response);
                        break;
                    }
                }
            } catch (ClassNotFoundException ex) {

            } catch (IOException ex) {

                System.out.println("Unable to get streams from client in bank server");

            }


        } finally {
            try {
                System.out.println("Agent exiting...");
                toClient.close();
                fromClient.close();
                socket.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    private void sendMessage(Message msg) {
        try {
            toClient.writeObject(msg);
            toClient.flush();
        } catch (IOException e) {

        }
    }


    private String createAccountNum(Account account) {
        Random rand = new Random();
        int accountNum = rand.nextInt(MAX_ACCOUNTS);
        if (listOfAccountNums.containsKey(accountNum)) {
            createAccountNum(account);
        }

        listOfAccountNums.put(accountNum, account);
        return Integer.toString(accountNum);

    }

    private String makeBankKey() {

        return "";

    }


    public static void main(String[] args) {
        System.out.println("Banking Server connected...");
        System.out.println("On port " + PORT_NUMBER);
        ServerSocket server = null;
        Socket socket = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true) {

//                ClientThreads t = new ClientThreads(server.accept());
//                threads.add(t);
//                t.start();
                new Bank(server.accept());


            }

        } catch (IOException ex) {
            System.out.println("Unable to start Banking server.");
        } finally {
            try {
                if (server != null)
                    server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
