import com.sun.deploy.util.StringUtils;
import sun.tools.java.ClassNotFound;

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

    public Bank(Socket socket){
        this.socket = socket;
        System.out.println("Banking Server connected...");

    }

    private boolean login(String input){

            for(int i = 0; i < input.length(); i++) {
                if(!Character.isDigit(input.charAt(i))) {
                    return false;
                }
            }

        return true;
    }

    private void createBankAccount() {
        InputStream agentIn = null;
        OutputStream bankOut = null;
        try {

            agentIn = socket.getInputStream();
            bankOut = socket.getOutputStream();

            DataOutputStream toClient = new DataOutputStream(bankOut);


            BufferedReader agentInfo = new BufferedReader(new InputStreamReader(agentIn));


            String message = " you are in the banking server" + '\n';
            toClient.writeBytes("From bank : " + message);
            String response = "";
            String request;

            while((request = agentInfo.readLine()) != null){

                toClient.writeBytes("Request to bank : " + request);
                printAccount();

                if(request.contains("login")){
                    String str = request.replace("login" , "");
                    if(login(str)){
                    int temp = Integer.parseInt(request);
                    if(!bankMap.isEmpty() && bankMap.containsKey(temp)) {
                        System.out.println("entered");
                        bankMap.get(temp);
                        response = " Account info " + bankMap.get(temp).returnPackage();
                        System.out.println(bankMap.get(temp).returnPackage());

                        }

                    }
                    else{
                        response = " Account does not exist";
                    }

                }

                else {

                    account = new Account(request);
                    bankMap.put(account.getAccountNumber(), account);
                    String temp = account.returnPackage();

                    response = " Your account has been created... " + temp;

                }

                toClient.writeBytes(response + '\n');

            }



        } catch (IOException ex) {

            System.out.println("Unable to get streams from client");
        } finally {
            try {
                agentIn.close();
                bankOut.close();
                socket.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }


    }

    private void printAccount(){
        for(Account a : accounts){
            System.out.println(a.returnPackage());
        }
    }


    private void buildAccount(String name){

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

    public void run(){

        createBankAccount();
    }

    public static void main(String[] args) {
        System.out.println("SocketServer Example");
        ServerSocket server = null;
        try {
            server = new ServerSocket(8080);
            while (true) {
                /**
                 * create a new {@link SocketServer} object for each connection
                 * this will allow multiple client connections
                 */
                new Bank(server.accept());
            }

        } catch (IOException ex) {
            System.out.println("Unable to start server.");
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
