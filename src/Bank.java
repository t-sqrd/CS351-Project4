
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


    private static HashMap<Integer, Account> bankMap = new HashMap<>();
    private Socket bankSocket;

    private static ArrayList<Bank> threads = new ArrayList<>();
    private static final int PORT_NUMBER = 8080;
    private String clientName;

    public Message forCentral;

    private volatile boolean KILL;
    private ObjectInputStream fromClient;
    private ObjectOutputStream toClient;


    private final String host = "127.0.0.1";

    public Bank(Socket socket) {
        this.bankSocket = socket;


        try {


            toClient = new ObjectOutputStream(bankSocket.getOutputStream());
            fromClient = new ObjectInputStream(bankSocket.getInputStream());



        } catch (IOException e) {

        }

    }

    public void run() {

        try {


            Message request;

            while (!KILL) {

                try {

                    while ((request = (Message) fromClient.readObject()) != null) {

                        if (request.newAccount) {
                            makeAccount(request);
                            printAllClients();

                        }

                        if (request.verify) {
                            Message response = new Message();
                            System.out.println("Entered If statement");
                            clientName = "CENTRAL";


                            if(bankMap.containsKey(request.bankKey)){
                                response.isMember = true;
                                response.message = "USER IS MEMBER";
                                response.fromBank = true;
                                sendMessage(response);

                            }
                            else{
                                response.isMember = false;
                                response.message = "User not found";
                                sendMessage(response);
                            }



                            for(Integer i : bankMap.keySet()){
                                System.out.println("KEYS = " + i);
                            }

                        }

                        if(request.moveFunds){

                        }

                        if (request.KILL) {
                            Message response = new Message();
                            response.message = "Goodbye...";
                            sendMessage(response);
                            KILL = true;
                            break;
                        }

                        printAllClients();

                    }
                } catch (ClassNotFoundException ex) {

                } catch (IOException ex) {

                    //System.out.println("Unable to get streams from client in bank server");

                }
            }
        }

            finally{
                try {
                    System.out.println(clientName + " is exiting bank...");
                    threads.remove(this);
                    toClient.close();
                    fromClient.close();
                    bankSocket.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }


    private void makeAccount(Message request){

        Message response = new Message();
        clientName = request.username;
        Account account = new Account(request.username);
        bankMap.put(account.getKey(), account);
        response.message = account.getAccountInfo();
        response.bankKey = account.getKey();
        sendMessage(response);
    }

    private void sendMessage(Message msg) {
        try {

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e) {

        }
    }


    private void printAllClients(){
        for(Bank t : threads){
            System.out.println("Users Connected to bank-> " + t.clientName);

        }
    }




    public static void main(String[] args) {
        System.out.println("Banking Server connected...");
        System.out.println("On port " + PORT_NUMBER);
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);

            while (!server.isClosed()) {


                Bank b = new Bank(server.accept());
                threads.add(b);
                b.start();

            }

        } catch (IOException ex) {
            ex.printStackTrace();
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
