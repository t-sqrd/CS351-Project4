import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.stream.Stream;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    String message;
    private int port;
    private String host;
    public volatile boolean bidding = false;
    private volatile boolean creatingAccount = false;
    private ArrayList<String> userInfo = new ArrayList<>();
    public String myAccountNum = "";
    private Encrypt encrypt;



    public static void main(String args[]) {
        //String host = "129.24.112.247"; //work IP
        String host = "127.0.0.1";

        int port = 8081;
         new Agent(host, port, "1110", "Alex");



    }

    public Agent(String host, int port, String initBid, String userName) {
        userName += initBid;
        this.initBid = initBid;
        this.userName = userName;
        this.host = host;
        this.port = port;
        this.encrypt = new Encrypt();

        createAccount();


    }

    public double placeBid(double bid) {


        return 0;
    }

    public void run() {

        while (true) {
            try {
                if (!bidding || !creatingAccount) {
                    wait();
                } else {

                    createAccount();
                }
            } catch (InterruptedException e) {

            }
        }

    }


    public void createAccount() {

        try {
            //String serverHostname = new String("127.0.0.1");
            //String serverHostname = new String("129.24.112.247");

            creatingAccount = true;
            System.out.println("Connecting to host " + host + " on port " + port + ".");
            Socket echoSocket = null;
            Reader tempIn = new StringReader(userName);
            BufferedReader in = null;


            //serverOut sends message to server (name, amount, initial bid)
            DataOutputStream serverOut = null;

            BufferedReader accountInfo = null;
            BufferedReader serverIn = null;

            try {

                echoSocket = new Socket(host, 8081);
                serverOut = new DataOutputStream(echoSocket.getOutputStream());
                in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                //accountInfo = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
               // serverIn = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));


            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server");
                System.exit(1);
            }

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String ui = stdin.readLine();
                serverOut.writeBytes(ui + '\n');
                if ("q".equals(ui)) break;
                System.out.println("SERVER : " + in.readLine());

            }

//            String message = clientIn.readLine();
//            serverOut.writeBytes(message + '\n');

            // String accountNum = accountInfo.readLine();
            //String x = serverIn.readLine();
            //sortInfo(accountNum);
//            Stream<String> s = accountInfo.lines();
//            sortInfo(s);


            System.out.print("Client: NAME & AMOUNT ");

            //System.out.println("Your account number is " + accountNum);


            /** Closing all the resources */
            serverOut.close();
            serverIn.close();
            in.close();
            echoSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public void sortInfo(Stream<String> info){
        Object[] temp = info.toArray();
        System.out.println("My info");

        for(int i = 0; i < temp.length; i++){

            System.out.println(temp[i]);

        }

    }


}
