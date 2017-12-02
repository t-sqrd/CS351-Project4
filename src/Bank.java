
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
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
    String host = "127.0.0.1";

    public Bank(Socket socket){
        this.socket = socket;


        start();

    }

    public void run() {
        InputStream agentIn = null;
        OutputStream bankOut = null;

        ObjectOutputStream toClient = null;
        ObjectInputStream agentInfo = null;




        try {

            bankOut = socket.getOutputStream();
            agentIn = socket.getInputStream();


            toClient = new ObjectOutputStream(bankOut);
            agentInfo = new ObjectInputStream(agentIn);

            Message request;
            Message response;
            while((request = (Message)agentInfo.readObject()) != null) {

                if(request.newAccount){
                    response = new Message();
                    Account account = new Account(request.username);
                    bankMap.put(account.getAccountNumber(), account);
                    response.message = account.returnPackage();
                    toClient.writeObject(response);
                }

            }



        }
        catch(ClassNotFoundException ex){
            System.err.print(ex.getCause());

        }
        catch (IOException ex) {

            System.err.print(ex.getCause());
            System.out.println("Unable to get streams from client in bank server");
        }
        finally {
            try {
                agentIn.close();
                bankOut.close();
                socket.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    }


    private String createAccountNum(Account account) {
        Random rand = new Random();
        int accountNum = rand.nextInt(MAX_ACCOUNTS);
        if(listOfAccountNums.containsKey(accountNum)) {
            createAccountNum(account);
        }

        listOfAccountNums.put(accountNum, account);
        return Integer.toString(accountNum);

    }

    private String makeBankKey(){

        return "";

    }



    public static void main(String[] args) {
        System.out.println("Banking Server connected...");
        System.out.println("On port " + PORT_NUMBER);
        ServerSocket server = null;
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true) {
                /**
                 * create a new {@link SocketServer} object for each connection
                 * this will allow multiple client connections
                 */
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
