import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Agent extends Thread {
    public String userName;
    private String initBid;
    private static int BANK_PORT = 8080;
    private static int CENTRAL_PORT = 8081;
    public static final String host = "127.0.0.1";
    public volatile boolean bidding = false;
    private volatile boolean creatingAccount = false;
    private ArrayList<String> userInfo = new ArrayList<>();
    private String accountName = "";
    public String myAccountNum = "";
    private Encrypt encrypt;
    Socket bankSocket = null;
    Socket centralSocket = null;
    private Boolean registered = false;

    ObjectOutputStream toBankServer;
    ObjectOutputStream toCentralServer;

    ObjectInputStream fromBankServer;
    ObjectInputStream fromCentralServer;

    public static  volatile boolean changeServer;





    public static void main(String args[]) {

        new Agent("ALEX");


    }

    public Agent(String userName){

        this.userName = userName;
        start();


    }



    public void getInput(String m, boolean routeToBank, int type){
        try{

            if(routeToBank){
                if(type == 1){
                    // make account message
                    Message send = new Message();
                    send.username = m;
                    accountName = m;
                    send.newAccount = true;
                    //accountInput = false;
                    toBankServer.writeObject(send);
                }
                Message mess = (Message)fromBankServer.readObject();
                System.out.println("From Bank = " + mess.getMessage());
                //accountNum = m.accountNum;
                printOptions();
                toBankServer.flush();
            }
            if(!routeToBank){

                Message central = new Message();
                central.message = m;

                central.username = "Agent";
                central.askForList = true;
                toCentralServer.writeObject(central);
                toCentralServer.flush();
                // System.out.println("From Central = " + fromCentralServer.readObject());
            }

        }
        catch(IOException e){

        }
        catch(ClassNotFoundException e){

        }
    }



    public void run() {

        try {


            System.out.println("Connecting to host " + host + " on ports " + BANK_PORT + ", " + CENTRAL_PORT);

            bankSocket = new Socket(host, BANK_PORT);
            centralSocket = new Socket(host, CENTRAL_PORT);




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

            printOptions();
            System.out.println("To return to main menu typ HOME ");

            new ListenFromServer().start();
            Scanner in = new Scanner(System.in);

            Message x = new Message();
            x.username = "Agent";
            toCentralServer.writeObject(x);


            String request;
            Boolean accountInput = false;
            Boolean clientInput = false;
            Boolean bidInput = false;
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

            while((request = stdin.readLine()) != null){
                System.out.println("Message sent: " + request);


                if(accountInput){
                    Message send = new Message();
                    send.username = request;
                    accountName = request;
                    send.newAccount = true;
                    accountInput = false;
                    toBankServer.writeObject(send);
                    Message m = (Message)fromBankServer.readObject();
                    //accountNum = m.accountNum;
                    System.out.println(m.getMessage());
                    printOptions();
                }

                if (clientInput){
                    System.out.println("client you chose from list: " + request);
                    Message send = new Message();
                    send.message = request;
                    send.selectHouse = true;
                    clientInput = false;
                    toCentralServer.writeObject(send);
                }

                if (bidInput){
                    String[] s = request.split(" ");
                    String item = s[0];
                    String bid = s[1];
                    Integer bidInt = Integer.valueOf(bid);
                    System.out.println("your bid: " + bid);
                    Message send = new Message();
                    send.message = item;
                    send.bid = bidInt;
                    send.placeBid = true;
                    bidInput = false;
                    toCentralServer.writeObject(send);

                }

                if(request.equals("Make Account") || request.equals("m")){
                    System.out.println("Please Enter Name: ");
                    accountInput = true;
                }

                if(request.equals("Register with Auction Central") || request.equals("r")){
                    System.out.println("Registering with auction central...");
                    registered = true;
                    Message send = new Message();
                    //send.message = accountKey;
                    registered = true;
                    send.username = accountName;
                    send.addAgent = true;
                    toCentralServer.writeObject(send);
                }

                if(request.equals("View Bidding Houses") || request.equals("v")){
                    Message send = new Message();
                    System.out.println("sending to central ");
                    send.message = "hey there central";
                    send.viewAuctionHouses = true;
                    toCentralServer.writeObject(send);
                }


                if(request.equals("Place Bid") || request.equals("p")){
                    System.out.println("Select number of item to place bid on followed by a space and bid (Ex: tree 7)");
                    bidInput = true;
                }

                if(request.equals("Select House") || request.equals("s")){
                    System.out.println("Choose a house from the list");
                    clientInput = true;
                }

                toBankServer.flush();
                toCentralServer.flush();
            }



            /*
            Boolean accountInput = false;

            while(true){


                String ui = in.nextLine();
                if(accountInput){
                    getInput(ui, true, 1);
                    accountInput = false;
                }
                if (ui.equals("Make Account") || ui.equals("m")) {
                    //getInput(ui, true);
                    accountInput = true;
                    System.out.println("Enter new account name: ");

                }
                if (ui.equals("View") || ui.equals("v")) {
                    getInput(ui, false, 0);
                }
            }
            */

//            try{
//                Message toBank = new Message(userName);
//                Message toCentral = new Message(userName);
//
//                setName(userName);
//
//                toBankServer.writeObject(toBank);
//                toCentralServer.writeObject(toCentral);
//            }
//            catch(IOException e){
//
//            }








            //BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));






//            while (true) {
//
//
//
//
//            }



//            boolean flag = false;
//            String ui;
//            while (true) {
//                ui = stdin.readLine();
//                //serverOut.writeBytes(ui + '\n');
//                if ("q".equals(ui))break;
//                if (ui.equals("Make Account")) {
//
//                        Message send = new Message();
//                        String info;
//                        System.out.println("Please Enter Name: ");
//
//                       while ((info = stdin.readLine()) != null && !flag) {
//                            send.username = info;
//                            send.newAccount = true;
//                            if (send.hasNewAccountInfo()) flag = true;
//                            toBankServer.writeObject(send);
//                            System.out.println(((Message)fromBankServer.readObject()).getMessage());
//
//                        }
//                    System.out.println("EXITED THIS LOOP");
//
//                    }
//
//
//                if (ui.equalsIgnoreCase("View Bidding Houses")) {
//                    Message send = new Message();
//                    send.viewAuctionHouses = true;
//                    toCentralServer.writeObject(send);
//                    System.out.println(((Message)fromCentralServer.readObject()).askForList);
//                }
//
//
//                toBankServer.reset();
//                toCentralServer.reset();
//                flag = false;
//
//
//            }

//            toBankServer.close();
//            fromBankServer.close();
//            toCentralServer.close();
//            fromCentralServer.close();
//            bankSocket.close();
//            centralSocket.close();

            //     }
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }

    class ListenFromServer extends Thread {

        public void run() {

            while(true) {

                try {
                    Message central = (Message) fromCentralServer.readObject();

                    if (central != null) {
                        System.out.println("Central > " + central.message);
                        if(central.viewAuctionHouses){
                            System.out.println("Houses from central");
                            System.out.println(central.message);
                            printOptions();
                        }
                        if(central.selectHouse){
                            System.out.println("List of all the items in the house");
                            System.out.println(central.message);
                            printOptions();
                        }
                        if(central.addAgent){
                            printOptions();
                        }

                    }


                }

                catch(IOException e) {

                }



                catch(ClassNotFoundException e2) {

                }

            }

        }

    }

    private void printOptions(){
        if(accountName.equals("")){
            System.out.println("Options : Make Account (m) / View Bidding Houses (v) / Select House (s)");
        }else if(!registered){
            System.out.println("Options :  View Bidding Houses (v) / Register w Auction Central (r)");
        } else {
            System.out.println("Options : View Bidding Houses (v) / Select House (s) / Place Bid (p)");
        }
    }

}






