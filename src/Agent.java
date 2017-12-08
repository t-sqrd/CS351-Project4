import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

public class Agent extends Thread {
    public String agentName;
    private String initBid;
    private static int BANK_PORT = 8080;
    private static int CENTRAL_PORT = 8081;
    public static final String host = "127.0.0.1";
    public volatile boolean bidding = false;
    private volatile boolean isRunning = true;
    public int myKey;
    private ArrayList<ListenFromServer> servers = new ArrayList<>();
    private Encrypt encrypt;
    Socket bankSocket = null;
    Socket centralSocket = null;

    ObjectOutputStream toBankServer;
    ObjectOutputStream toCentralServer;

    ObjectInputStream fromBankServer;
    ObjectInputStream fromCentralServer;

    BufferedReader stdin;


    public static void main(String args[]) {

        ArrayList<Integer> online = new ArrayList<>();
        Random rand = new Random();
        Integer user = rand.nextInt(10000);
        Scanner scanner = new Scanner(System.in);
//        System.out.println("Please Enter Name: ");
//
//        String name = scanner.nextLine();
//        if (name != null && !online.contains(Integer.parseInt(name))) {
//            System.out.println("Welcome Agent " + name + "!");
//            online.add(user);
//            new Agent(name);
//        }
//        else{
//            System.out.println("Entered Else");
//        }
        if(!online.contains(user)){
            System.out.println("Welcome Agent #" + user + "!");
            new Agent(Integer.toString(user));
        }

    }

    public Agent(String userName) {

        try{
            bankSocket = new Socket(host, BANK_PORT);
            centralSocket = new Socket(host, CENTRAL_PORT);
            bankSocket.connect(centralSocket.getLocalSocketAddress());
        }
        catch (IOException e){

        }

        this.agentName = "Agent " + bankSocket.getLocalPort();
        start();

    }


    private void sendMsgToCentral(Message msg) {
        try {
            toCentralServer.writeObject(msg);
            toCentralServer.flush();


        } catch (IOException e) {

        }

    }

    private void sendMsgToBank(Message msg) {
        try {
            toBankServer.writeObject(msg);
            toBankServer.flush();

        } catch (IOException e) {

        }
    }


    public void run() {

        try {


            System.out.println("Connecting to host " + host + " on ports " + BANK_PORT + ", " + CENTRAL_PORT);



            try {

                toBankServer = new ObjectOutputStream(bankSocket.getOutputStream());
                fromBankServer = new ObjectInputStream(bankSocket.getInputStream());

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());

                new ListenFromServer(fromBankServer, "Bank").start();
                new ListenFromServer(fromCentralServer, "Central").start();


                stdin = new BufferedReader(new InputStreamReader(System.in));


            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }

            System.out.println("Options : Make Account / View Houses / Register / Select House / Place Bid");


            Message myName = new Message();
            myName.username = agentName;
            sendMsgToCentral(myName);

            String ui;

            boolean registered = false;

            while ((ui = stdin.readLine()) != null) {

                if (ui.equals("QUIT")) {
                    Message msg = new Message();
                    msg.KILL = true;
                    sendMsgToBank(msg);
                    sendMsgToCentral(msg);
                    isRunning = false;
                    break;

                } else if (ui.equalsIgnoreCase("Make Account")) {

                    System.out.println("Please Enter Name: ");
                    if ((ui = stdin.readLine()) != null) {
                        Message request = new Message();
                        request.newAccount = true;
                        this.agentName = ui;
                        request.username = ui;
                        sendMsgToBank(request);
                    }

                } else if (ui.equalsIgnoreCase("Register")) {

                    System.out.println("Please Provide Name and Bank Key: ");
                    if ((ui = stdin.readLine()) != null) {
                        Message request = new Message();
                        request.register = true;
                        request.username = ui;
                        registered = true;
                        sendMsgToCentral(request);

                    }

                } else if (ui.equals("View Houses") && registered) {

                    Message request = new Message();
                    request.message = "View";
                    //central.username = "Agent";
                    request.username = agentName;
                    request.askForList = true;
                    sendMsgToCentral(request);

                } else if (ui.equals("Select House") && registered) {
                    System.out.println("Please Enter House Number: ");
                    Message request = new Message();
                    if ((ui = stdin.readLine()) != null) {
                        if (!ui.contains("House")){
                            request.selectedHouse = "House " + ui;
                        }
                        else{
                            request.selectedHouse = ui;
                        }
                        request.selectHouse = true;
                        request.username = agentName;
                        request.getItems = true;
                        sendMsgToCentral(request);
                    }

                } else if (ui.equalsIgnoreCase("Place bid") && registered) {
                    Message bid = new Message();
                    System.out.println("Please Choose House: ");
                    if ((ui = stdin.readLine()) != null) {
                        bid.selectedHouse = ui;
                        bid.selectHouse = true;
                        bid.placeBid = true;
                        bid.username = agentName;
                        System.out.println("Please Enter Item Number: ");
                        if ((ui = stdin.readLine()) != null) {
                            bid.index = Integer.parseInt(ui);
                            sendMsgToCentral(bid);
                        }

                    }
                }

                else {
                    System.out.println("Please try again...");
                    if(!registered)
                    System.out.println("You may still need to register or create an account...");
                }


            }
            try {
                System.out.println("Logging out...");
                sleep(1000);
                toBankServer.close();
                fromBankServer.close();
                toCentralServer.close();
                fromCentralServer.close();
                bankSocket.close();
                centralSocket.close();

            } catch (InterruptedException e) {

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ListenFromServer extends Thread {

        ObjectInputStream myServer;
        String serverName;

        public ListenFromServer(ObjectInputStream fromServer, String serverName) {
            myServer = fromServer;
            this.serverName = serverName;
        }

        public void run() {

            Message server;
            while (isRunning) {

                try {
                        server = (Message) myServer.readObject();

                        if (server != null) {
                            System.out.println(serverName + " > " + server.message);

                        }
                    }

                catch (IOException e) {

                }
                catch (ClassNotFoundException ex) {

                }

            }

        }

    }

}








