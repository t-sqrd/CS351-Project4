import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    private int port;
    private String host;
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






    public void createAccount() {

        try {

            creatingAccount = true;
            System.out.println("Connecting to host " + host + " on port " + port + ".");
            Socket echoSocket = null;
            Reader tempIn = new StringReader(userName);
            BufferedReader in = null;


            //serverOut sends message to server (name, amount, initial bid)


            ObjectOutputStream toServer = null;
            ObjectInputStream fromServer = null;

            try {

                echoSocket = new Socket(host, port);
                //serverOut = new DataOutputStream(echoSocket.getOutputStream());
                //in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));
                toServer = new ObjectOutputStream(echoSocket.getOutputStream());
                fromServer = new ObjectInputStream(echoSocket.getInputStream());





            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }

            System.out.println("Options : Make Account / View Bidding Houses ");
            System.out.println("To return to main menu type HOME ");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            boolean flag = false;
            String ui;
            while (true) {

                ui = stdin.readLine();
                //serverOut.writeBytes(ui + '\n');
                if ("q".equals(ui)) break;
                if (ui.equals("Make Account")) {

                        Message send = new Message();
                        String info;
                        System.out.println("Please Enter Name: ");

                       while ((info = stdin.readLine()) != null) {
                            send.username = info;
                            send.newAccount = true;
                            if (send.hasNewAccountInfo()) break;
                            toServer.writeObject(send);
                            System.out.println(((Message)fromServer.readObject()).getMessage());
                        }
                    System.out.println("EXITED THIS LOOP");

                    }


                if (ui.equalsIgnoreCase("View Bidding Houses")) {
                    Message send = new Message();
                    send.viewAuctionHouses = true;
                    toServer.writeObject(send);
                    System.out.println(((Message)fromServer.readObject()).getMessage());

                    }

                if(ui.equals("HOME")){
                    Message send = new Message();
                    send.HOME = true;
                    toServer.writeObject(send);
                }


                toServer.flush();

            }




            System.out.print("Client: NAME & AMOUNT ");

            //System.out.println("Your account number is " + accountNum);


            /** Closing all the resources */
            toServer.close();
            fromServer.close();
            in.close();
            echoSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
