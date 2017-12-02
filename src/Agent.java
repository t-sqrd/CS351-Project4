import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    private int BANK_PORT = 8080;
    private int CENTRAL_PORT = 8081;
    private String host = "127.0.0.1";
    public volatile boolean bidding = false;
    private volatile boolean creatingAccount = false;
    private ArrayList<String> userInfo = new ArrayList<>();
    public String myAccountNum = "";
    private Encrypt encrypt;




    public static void main(String args[]) {


        String host = "127.0.0.1";
//        System.out.println("Please enter IP address ");
//        Scanner scanner = new Scanner(System.in);
//        String t = scanner.next();
//        host = t;
        //String host = "129.24.112.247"; //work IP

        int port = 8080;
        new Agent(host, "1110", "Alex");



    }

    public Agent(String host, String initBid, String userName) {

        this.encrypt = new Encrypt();

        userInput();


    }




    private void userInput() {

        try {

            creatingAccount = true;
            System.out.println("Connecting to host " + host + " on ports " + BANK_PORT + ", " + CENTRAL_PORT);

            Socket bankSocket = null;
            Socket centralSocket = null;

            //Reader tempIn = new StringReader(userName);

            //serverOut sends message to server (name, amount, initial bid)


            ObjectOutputStream toBankServer = null;
            ObjectInputStream fromBankServer = null;

            ObjectOutputStream toCentralServer = null;
            ObjectInputStream fromCentralServer = null;

            try {

                bankSocket = new Socket(host, BANK_PORT);
                centralSocket = new Socket(host, CENTRAL_PORT);
                //serverOut = new DataOutputStream(echoSocket.getOutputStream());
                //in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                toBankServer = new ObjectOutputStream(bankSocket.getOutputStream());
                fromBankServer = new ObjectInputStream(bankSocket.getInputStream());

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());

                System.out.println("here");

            }

            catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }

            System.out.println("Options : Make Account / View Bidding Houses ");
            System.out.println("To return to main menu typ HOME ");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            boolean flag = false;
            String ui;
            while (true) {
                ui = stdin.readLine();
                //serverOut.writeBytes(ui + '\n');
                if ("q".equals(ui))break;
                if (ui.equals("Make Account")) {

                        Message send = new Message();
                        String info;
                        System.out.println("Please Enter Name: ");

                       while ((info = stdin.readLine()) != null && !flag) {
                            send.username = info;
                            send.newAccount = true;
                            if (send.hasNewAccountInfo()) flag = true;
                            toBankServer.writeObject(send);
                            System.out.println(((Message)fromBankServer.readObject()).getMessage());

                        }
                    System.out.println("EXITED THIS LOOP");

                    }


                if (ui.equalsIgnoreCase("View Bidding Houses")) {
                    Message send = new Message();
                    send.viewAuctionHouses = true;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).askForList);
                }


                toBankServer.reset();
                toCentralServer.reset();
                flag = false;


            }

            toBankServer.close();
            fromBankServer.close();
            toCentralServer.close();
            fromCentralServer.close();
            bankSocket.close();
            centralSocket.close();

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

}
