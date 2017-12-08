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
    boolean housesAvailable, houseLeaving;
    volatile boolean KILL;
    volatile boolean KILL_HOUSE = false;
    String myName;
    Message init;
    Integer clientBankKey;
    public static int agentId = 1;
    public static int houseId = 1;

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


        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
    }

    public void run() {


        try {

            while (!KILL) {
                Message request;
                Message response;

                newHouseListener(newHouse);
                newHouse = false;
                while ((request = (Message) fromClient.readObject()) != null) {

                    //getting list of items from house then sending them to the requesting agent
                    if (request.fromHouse) {

                        houseResponse(request);
                    }


                    if (request.register) {
                        response = new Message();

                        myName = request.username;
                        convertToInteger(request.username);

                        System.out.println("Agent Name = " + myName);
                        response.bankKey = clientBankKey;
                        response.verify = true;
                        response.username = myName;
                        bankBroadcast(response);
                        Message bankMsg = readFromBank();


                        if (bankMsg.isMember) {
                            Integer key = makeBiddingKey();
                            registeredUsers.put(key, myName);
                            response = new Message();
                            response.biddingKey = key;
                            response.message = "Your account has been activated, Bidding key -> " + key;
                            clientBroadcast(response);

                        } else {

                            //bankMsg.message = "Bank account not found or account is already registered...";
                            clientBroadcast(bankMsg);
                        }


                    }

                    if (request.isMember) {
                        System.out.println("HERE");
                        System.out.println(request.message);
                    }


                    if (request.askForList) {
                        sendHouseList();
                    }

                    if (request.selectHouse) {
                        if (housesAvailable) formCommunication(request, request.selectedHouse); //formCommunication(myName, request.message);


                        else {
                            Message m = new Message();
                            m.message = "There are no houses available...";
                            clientBroadcast(m);
                        }
                    }
                }
            }


        }

        catch (IOException e) {


        } catch (ClassNotFoundException e) {


        } finally {
            try {

                System.out.println(myName + " is logging off...");
                threads.remove(this);
                houseLeavingListener(this);
                fromBank.close();
                toBank.close();
                bankSocket.close();
                fromClient.close();
                toClient.close();
                socket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }


    private void convertToInteger(String str) {
        String key = "";
        String name = "";
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                key += str.charAt(i);
            } else {
                name += str.charAt(i);
            }
        }
        myName = name;
        clientBankKey = Integer.parseInt(key);

    }

//    private synchronized void placingBid(){
//        try{
//            Thread.sleep()
//        }
//    }

    private void houseResponse(Message request) {
        for (AuctionCentral t : threads) {
            if (t.myName.equals(request.username)) {
                t.clientBroadcast(request);
            }
        }

    }


    private void nameClients() {
        for (AuctionCentral t : threads) {
            if (t != null) {
                if (t.myName.equals("House")) {
                    t.myName = "House " + houseId++;
                } else if (t.myName.equals("Agent")) {
                    t.myName = "Agent " + agentId++;

                }
            }

        }

    }

    private void formCommunication(Message msg, String house) {
        for (AuctionCentral t : threads) {
            //AuctionCentral t = threads.get(i);
            if (t.myName.equals(house)) {

                t.clientBroadcast(msg);
            }
        }

    }


//    private void formCommunication(String agent, String house) {
//        Message msg = new Message();
//        for (AuctionCentral t : threads) {
//            if (t.myName.equals(house)) {
//                msg.username = agent;
//                msg.getItems = true;
//
//                t.houseBroadcast(msg);
//            }
//        }
//    }

    private void newHouseListener(boolean newHouse){

        if (newHouse) {
            Message msg = new Message();
            for (AuctionCentral t : threads) {
                if (t.myName.contains("Agent")) {
                    msg.message = "New AuctionHouse has entered! -> " + this.myName;
                    t.clientBroadcast(msg);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(AuctionCentral thread) {

        Message msg = new Message();
        System.out.println("Entered");
        for (AuctionCentral t : threads) {
            if (thread.myName.contains("House") && t.myName.contains("Agent")) {
                msg.message = thread.myName + " is going offline...";
                t.clientBroadcast(msg);
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

    private Message readFromBank() throws ClassNotFoundException {

        Message m = new Message();
        try {

            m = (Message) fromBank.readObject();
            boolean member = m.isMember;

            System.out.println(member);

            System.out.println(m.message);
            return m;
        } catch (IOException e) {

        }
        return m;


    }


    private void clientBroadcast(Message msg) {
        try {

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

//    private void houseBroadcast(Message msg) {
//        try {
//            toClient.writeObject(msg);
//            toClient.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void sendHouseList() {
        boolean hasHouse = false;

        for (AuctionCentral t : threads) {
            System.out.println("USER = " + t.myName + " " + t);
            if (!t.equals(this) && t.myName.contains("House")) {
                Message send = new Message();
                send.message = "All houses online  " + t.myName;
                hasHouse = true;
                housesAvailable = true;
                clientBroadcast(send);

            }

        }
        System.out.println("------------------");
        if (!hasHouse) {
            Message msg = new Message();
            msg.message = "No Auction Houses are online right now";
            housesAvailable = false;
            clientBroadcast(msg);
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


