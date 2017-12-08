import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread {

    Socket agentSocket;
    Socket houseSocket;

    public static final int CENTER_PORT = 8081;


    Socket socket;
    public ObjectOutputStream toClient;
    public ObjectInputStream fromClient;
    boolean sendlist, newHouse, selectHouse;
    boolean housesAvailable, houseLeaving, amHouse;
    volatile boolean KILL;
    volatile boolean KILL_HOUSE = false;
    String myName;
    Message init;
    Integer clientBankKey;
    public static int agentId = 0;
    public static int houseId = 0;

    public ObjectOutputStream toBank;
    public ObjectInputStream fromBank;

    private final String host = "127.0.0.1";





    public static ArrayList<AuctionCentral> threads = new ArrayList<>();
    public static HashMap<Integer, String> registeredUsers = new HashMap<>();
    public Bank b;
    public Socket bankSocket;



    public AuctionCentral(Socket socket) {
        this.socket = socket;

        try {

            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());

            bankSocket = new Socket(host, 8080);
            toBank = new ObjectOutputStream(bankSocket.getOutputStream());
            fromBank = new ObjectInputStream(bankSocket.getInputStream());



            Message user = (Message) fromClient.readObject();
            this.myName = user.username;
            this.newHouse = user.newHouse;



        }
        catch (IOException e) {

        }
        catch (ClassNotFoundException e) {

        }
    }

    public void run() {


        try {

            while (!KILL) {
                Message request;
                Message response;


//                nameClients();
                newHouseListener(newHouse);
                newHouse = false;
                while ((request = (Message) fromClient.readObject()) != null) {


                    //selectHouse = request.selectHouse;


                    //getting list of items from house then sending them to the requesting agent
                    if (request.fromHouse) {
                        houseResponse(request);
                    }


                    if (request.register) {
                        response = new Message();

                        //myName = request.agentName;
                        convertToInteger(request.username);

                        System.out.println("Agent Name = " + myName);
                        response.bankKey = clientBankKey;
                        response.verify = true;
                        response.username = myName;
                        bankBroadcast(response);


                        if(readFromBank()){
                            Integer key = makeBiddingKey();
                            registeredUsers.put(key, myName);
                            response = new Message();
                            response.biddingKey = key;
                            response.message = "Your account has been activated, Bidding key -> " + key;
                            agentBroadcast(response);

                        }
                        else{
                            response = new Message();
                            response.message = "Bank account not found...";
                            agentBroadcast(response);
                        }



                    }

                    if (request.isMember) {
                        System.out.println("HERE");
                        System.out.println(request.message);
                    }


                    if (request.askForList) {

                        sendHouseList();
                    }

                    if (request.selectHouse && housesAvailable) {
                        communicationBeta(request, request.selectedHouse);


                    }else if (request.selectHouse && !housesAvailable) {
                            Message m = new Message();
                            m.message = "There are no houses available...";
                            agentBroadcast(m);
                    }

                }
                houseLeavingListener(true);
            }


        } catch (IOException e) {


        } catch (ClassNotFoundException e) {


        } finally {
            try {

                System.out.println(myName + " is logging off...");
                if(this.amHouse) houseId--;
                threads.remove(this);
                fromClient.close();
                toClient.close();
                socket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }


    private void convertToInteger(String str){
        String key = "";
        String name = "";
        for(int i = 0; i < str.length(); i++){
            if(Character.isDigit(str.charAt(i))){
                key += str.charAt(i);
            }
            else{
                name += str.charAt(i);
            }
        }
        myName = name;
        clientBankKey = Integer.parseInt(key);

    }

    private void houseResponse(Message request){
        for (AuctionCentral t : threads) {
            if (t.myName.equals(request.username)) {
                t.agentBroadcast(request);
            }
        }

    }

    private void communicationBeta(Message msg, String house){
        int houseNumber = Integer.parseInt(house);
        int counter = 1;
       for(AuctionCentral t : threads){
           if(t.amHouse && counter == houseNumber){
               t.houseBroadcast(msg);
               break;
           } else if(t.amHouse){
               counter++;
           }
       }

    }

    private void newHouseListener(boolean newHouse) {

        if (newHouse) {
            houseId++;
            this.amHouse = true;
            Message msg = new Message();
            for (AuctionCentral t : threads) {
                if (t.myName.contains("Agent")) {
                    msg.message = "New AuctionHouse has entered! -> " + this.myName;
                    t.agentBroadcast(msg);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(boolean leave) {
        if (leave) {
            Message msg = new Message();
            System.out.println("Entered");
            for (AuctionCentral t : threads) {
                if (t.myName.contains("Agent")) {
                    msg.message = "Auction House " + this.myName + " is offline.";
                    t.agentBroadcast(msg);

                }
            }
        }
    }

    private void bankBroadcast(Message m) throws ClassNotFoundException {
        try {


            toBank.writeObject(m);
            toBank.flush();

        } catch (IOException e) {

        }

    }

    private boolean readFromBank() throws ClassNotFoundException{

        try {

            Message m = (Message)fromBank.readObject();
            boolean member = m.isMember;

            System.out.println(member);

            System.out.println(m.message);
            return m.isMember;
        }
        catch(IOException e){

        }
        return false;


    }


    private void agentBroadcast(Message msg) {
        try {

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private void houseBroadcast(Message msg) {
        try {
            toClient.writeObject(msg);
            toClient.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendHouseList() {
        boolean hasHouse = false;
        if (!threads.isEmpty()) {
            Message send = new Message();
            send.houseList = true;
            String[] houses = new String[houseId];
            int counter = 0;
            for (AuctionCentral t : threads) {
                if (!t.equals(this) && t.myName.contains("House")) {
                    houses[counter] = t.myName;
                    hasHouse = true;
                    housesAvailable = true;
                    counter++;
                }
            }
            if(hasHouse){
                send.houses = houses;
                agentBroadcast(send);
            } else {
                Message msg = new Message();
                msg.message = "No Auction Houses are online right now";
                housesAvailable = false;
                agentBroadcast(msg);
            }
        }

    }


    private void remove() {
        for (AuctionCentral t : threads) {
            if (!t.isAlive()) {
                System.out.println("DEAD THREAD -> " + t.myName);
                threads.remove(t);
                System.out.println(t.myName);
                try {
                    t.toClient.close();
                    t.fromClient.close();
                    t.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private Integer makeBiddingKey() {
        Random rand = new Random();
        Integer key = rand.nextInt(50);
        if (registeredUsers.containsKey(key)) {
            makeBiddingKey();
        }
        return key;
    }


    //start();


//    public void run() {
//
//
//
//        try {
//
//
//            toAgent = new ObjectOutputStream(agentSocket.getOutputStream());
//            fromAgent = new ObjectInputStream(agentSocket.getInputStream());
//
//            Message request;
//            Message reponse;
//
//
//            while(true) {
//                while ((request = (Message) fromAgent.readObject()) != null) {
//                    System.out.println("In Auction Central Server...");
//
//
//
//                    if (request.viewAuctionHouses) {
//                        Message response = new Message();
//                        response.askForList = true;
//
//
//                    }
////
////
////
//                }
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//
//            System.out.println("Unable to get streams from client in Server 2");
//        }
//
//        catch (ClassNotFoundException e) {
//        }
//
//        finally {
//            try {
//
//
//                threads.clear();
//                fromAgent.close();
//                toAgent.close();
//                agentSocket.close();
//
//
//            } catch (IOException ex) {
//
//                ex.printStackTrace();
//            }
//        }
//    }


    public static void main(String[] args) {
        System.out.println("Starting Auction Central...");
        ServerSocket fromAgent = null;
        ServerSocket fromHouse = null;
        try {
            fromAgent = new ServerSocket(CENTER_PORT);

            while (true) {


                AuctionCentral c = new AuctionCentral(fromAgent.accept());
                threads.add(c);
                c.start();

            }


        } catch (IOException ex) {
            System.out.println("Unable to start Auction Central.");
        } finally {
            try {
                if (fromAgent != null) fromAgent.close();

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}


