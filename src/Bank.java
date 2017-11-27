import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/19/17.
 */
public class Bank extends Thread {


    private HashMap<Integer, Account> bankMap = new HashMap<>();
    private Socket socket;
    private final int MAX_ACCOUNTS = 10000;
    private static HashMap<Integer, Account> listOfAccountNums = new HashMap<>();
    private String clientName;

    private Bank(Socket socket){
        this.socket = socket;
        System.out.println("Banking Server connected...");
        start();
    }

    private void createBankAccount() throws IOException{
        InputStream agentIn = socket.getInputStream();
        OutputStream bankOut = socket.getOutputStream();

        DataOutputStream toClient = new DataOutputStream(bankOut);
        BufferedReader agentInfo = new BufferedReader(new InputStreamReader(agentIn));
        String name = agentInfo.readLine();
        this.clientName = name;
        Account account = new Account(name);




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

    }




}
