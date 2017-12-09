import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
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
    private static String options = "*** Options : Make Account / View Houses / Register / Select House / Place Bid ***";
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

    private void connectToServer(){
        System.out.println("Connecting to host " + host + " on ports " + BANK_PORT + ", " + CENTRAL_PORT);
        try {
            bankSocket = new Socket(host, BANK_PORT);

            centralSocket = new Socket(host, CENTRAL_PORT);
            //bankSocket.connect(centralSocket.getLocalSocketAddress());
           // centralSocket = new Socket(host, CENTRAL_PORT);

            try {

                toBankServer = new ObjectOutputStream(bankSocket.getOutputStream());
                fromBankServer = new ObjectInputStream(bankSocket.getInputStream());

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());



            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }
        }
        catch (IOException e){

        }
    }



    public void run() {
//
       try {


           System.out.println("Connecting to host " + host + " on ports " + BANK_PORT + ", " + CENTRAL_PORT);
           System.out.println(options);



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


            Message myName = new Message();
            myName.username = agentName;
            sendMsgToCentral(myName);

            String ui;

            boolean registered = false;

            while((ui = stdin.readLine()) != null) {

                System.out.println(options);

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

                } else if (ui.contains("vi") && registered) {

                    Message request = new Message();
                    request.message = "View";
                    request.username = agentName;
                    request.askForList = true;
                    sendMsgToCentral(request);

                } else if (ui.contains("se") ){//&& registered) {
                    System.out.println("Please Enter House Number: ");
                    Message request = new Message();
                    if ((ui = stdin.readLine()) != null) {

                        request.selectedHouse = ui.trim();
                        request.selectHouse = true;
                        request.username = agentName;
                        request.getItems = true;
                        sendMsgToCentral(request);
                    }
                } else if (ui.contains("bi")) {
                    Message bid = new Message();
                    System.out.println("Please Choose House: ");
                    if ((ui = stdin.readLine()) != null) {
                        bid.selectedHouse = ui;
                        bid.selectHouse = true;
                        bid.placeBid = true;
                        bid.username = agentName;
                        System.out.println("Please Enter Item Number: ");
                        if((ui = stdin.readLine()) != null) {
                            bid.index = Integer.parseInt(ui) - 1;
                        }
                        System.out.println("Please Enter Your Bid Amount: ");
                        if((ui = stdin.readLine()) != null) {
                            bid.bidAmount = Integer.parseInt(ui);
                        }
                        sendMsgToCentral(bid);

                    }
                } else if(ui.contains("test")){
                    Message test = new Message();
                    test.test = true;
                    test.selectHouse = true;
                    test.selectedHouse = "1";
                    sendMsgToCentral(test);
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

        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }


    class ListenFromServer extends Thread {

       ObjectInputStream myServer;
       String serverName;

       public ListenFromServer(ObjectInputStream fromServer, String serverName){
           this.myServer = fromServer;
           this.serverName = serverName;
       }

       public void printItems(String[] items, double[] prices, int[] timeLeft){
           System.out.println("itemPack from this house:");
           DecimalFormat format = new DecimalFormat("0.00");
           for(int i = 0; i < items.length; i++){
               String price = format.format(prices[i]);
               if(timeLeft[i] > -1){
                   System.out.println("   " + (i + 1) + ") " + items[i] + " $" + price + " | Time Left: " + timeLeft[i]);
               } else {
                   System.out.println("   " + (i + 1) + ") " + items[i] + " $" + price);
               }
           }
       }

        public void printHouses(String[] houses){
            System.out.println("All Auction Houses Online:");
            for(int i = 0; i < houses.length; i++){
                System.out.println("   " + (i+1) + ") " + houses[i]);
            }
        }

        public void run() {

            while(isRunning) {

                try {

                    Message server = (Message) myServer.readObject();

                    if (server != null) {
                        if(server.isItems){
                            System.out.println("Is Items: " + server.items[0]);
                            System.out.println(server.timeLeft[0]);
                            printItems(server.items, server.prices, server.timeLeft);
                        } else if(server.houseList) {
                            printHouses(server.houses);
                        } else if(server.placeBid){
                            if(server.invalidBid) System.out.println("Your bid must be higher than the existing bid!");
                            else System.out.println("Your bid was successful!");
                            printItems(server.items, server.prices, server.timeLeft);
                        } else {
                            System.out.println(serverName + " > " + server.message);
                        }
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








