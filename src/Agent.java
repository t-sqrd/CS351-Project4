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
    private static Integer myBankKey;
    private static Integer myBiddingKey;
    private static String options = "*** Options : Make Account / View Houses / Register / Select House / Place Bid ***";
    public int myKey;
    private ArrayList<ListenFromServer> servers = new ArrayList<>();
    private static String userInfo;
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
        System.out.println("Please Enter Name: ");

        String name = scanner.nextLine();
        if (name != null) {
            System.out.println("Welcome Agent " + name + "!");
            new Agent(name);
        }

//        if(!online.contains(user)){
//            System.out.println("Welcome Agent #" + user + "!");
//            new Agent(Integer.toString(user));
//        }

    }

    public Agent(String userName) {

        try{
            bankSocket = new Socket(host, BANK_PORT);
            centralSocket = new Socket(host, CENTRAL_PORT);
            bankSocket.connect(centralSocket.getLocalSocketAddress());
        }
        catch (IOException e){

        }

        this.agentName = userName;

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

    private Integer convertToInt(String str){

        String temp = "";
        boolean safe = false;
        for(int i = 0; i < str.length(); i++){
            if(Character.isDigit(str.charAt(i))){
                temp += str.charAt(i);
                safe = true;

            }

        }

        if(safe) {
            return Integer.parseInt(temp);
        }
        else{
            System.out.println("Invalid input! Missing Bank Key");
        }
        return -1;
    }
    private boolean validStr(String str){
        int counter = 0;
        for(int i = 0; i < str.length(); i++){
            if(Character.isLetter(str.charAt(i))){
                counter++;
            }
        }
        return (str.length() == counter);
    }

    private String formatUsername(String name){
        return "Agent " + name;
    }

    private void printInfo(){
        System.out.print(userInfo);
    }

    private boolean biddingInfo(String house, String key, String amount){
        if(convertToInt(house) != -1 && convertToInt(key) != -1 && convertToInt(key) != -1){
            return true;
        }
        System.out.println("Must be a number!");
        return false;
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
           boolean accountMade = false;

            while((ui = stdin.readLine()) != null) {

                System.out.println(options);

                if (ui.equals("QUIT")) {
                    Message msg = new Message();
                    msg.KILL = true;
                    sendMsgToBank(msg);
                    sendMsgToCentral(msg);
                    isRunning = false;
                    break;

                }
                else if (ui.equalsIgnoreCase("Make Account")) {

                    System.out.println("Please Enter Name: ");

                    if ((ui = stdin.readLine()) != null) {
                        Message request = new Message();
                        if(validStr(ui) && ui.equals(agentName)) {
                            request.newAccount = true;
                            //agentName = formatUsername(ui);
                            request.username = agentName;
                            accountMade = true;
                            userInfo = "Name: " + agentName + '\n';
                            printInfo();
                            sendMsgToBank(request);
                        }
                        else{
                            System.out.println("Invalid Input");
                        }
                    }

                } else if (ui.equalsIgnoreCase("Register") && accountMade) {
                    boolean cleared1, cleared;
                    cleared = cleared1 = false;

                    System.out.println("Please enter Username: ");
                    Message request = new Message();
                    if((ui = stdin.readLine()) != null){
                        if(validStr(ui) && ui.equals(agentName)) {

                            request.username = agentName;
                            cleared1 = true;
                        }
                            else{
                            System.out.println("Conflicts in names");
                        }

                    }
                    System.out.println("Please Enter your Bank Key: ");

                    if ((ui = stdin.readLine()) != null) {
                        if(convertToInt(ui) != -1) {
                            request.register = true;
                            request.bankKey = convertToInt(ui);
                            userInfo += " Bank Key: " + convertToInt(ui) + "\n";
                            cleared = true;
                        }

                        registered = cleared && cleared1;
                        if(cleared && cleared1){
                            printInfo();
                            registered = true;
                            sendMsgToCentral(request);
                        }
                    }

                } else if (ui.equalsIgnoreCase("View Houses") && registered) {

                    Message request = new Message();
                    request.username = agentName;
                    request.askForList = true;
                    sendMsgToCentral(request);

                } else if (ui.equalsIgnoreCase("Select House") && registered) {
                    System.out.println("Please Enter House Number: ");
                    Message request = new Message();
                    if ((ui = stdin.readLine()) != null) {
                        if(convertToInt(ui) != -1 ) {
                            request.selectedHouse = ui.trim();
                            request.selectHouse = true;
                            request.username = agentName;
                            request.getItems = true;
                            sendMsgToCentral(request);
                        }
                        System.out.println("Must be a number!");
                    }
                }

                else if (ui.equalsIgnoreCase("Place Bid")) {
                    Message bid = new Message();
                    boolean house = false;
                    boolean key = false;
                    boolean amount = false;
                    boolean item = false;

                    System.out.println("Please Choose House: ");
                    if ((ui = stdin.readLine()) != null) {

                        if(convertToInt(ui) != -1) {
                            bid.selectedHouse = ui.trim();
                            bid.selectHouse = true;
                            bid.placeBid = true;
                            bid.username = agentName;
                            System.out.println("House = " + ui.trim());
                            house = true;
                        }

                        System.out.println("Please enter bidding key");
                        if((ui = stdin.readLine()) != null && convertToInt(ui) != -1){
                            key = true;
                            bid.biddingKey = Integer.parseInt(ui);
                            System.out.println("Bidding key = " +bid.biddingKey);
                            userInfo += " Bidding Key: " + Integer.parseInt(ui) + '\n';
                        }

                        System.out.println("Please Enter Item Number: ");
                        if((ui = stdin.readLine()) != null && convertToInt(ui) != -1) {
                            bid.index = Integer.parseInt(ui) - 1;
                            item = true;
                        }
                        System.out.println("Please Enter Your Bid Amount: ");
                        if((ui = stdin.readLine()) != null && convertToInt(ui) != -1) {
                            bid.bidAmount = Integer.parseInt(ui);
                            amount = true;

                        }

                        if(house && key && amount && item) sendMsgToCentral(bid);

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
           System.out.println("Items from this house:");
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
                            printItems(server.items, server.prices, server.timeLeft);
                        } else if(server.houseList) {
                            printHouses(server.houses);
                        }else if(server.placeBid){
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








