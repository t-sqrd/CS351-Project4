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
    boolean sendlist, newHouse, selectHouse, listener;
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

    //BiddingKey, BankKey
    public static HashMap<Integer, Integer> keys = new HashMap<>();
    public static HashMap<HashMap<Integer, Integer>, String> registeredUsers = new HashMap<>();

    public Bank b;
    public Socket bankSocket;

    public AuctionCentral(boolean listener) {
        this.listener = listener;
    }

    public AuctionCentral(Socket socket) {
        this.socket = socket;

        try {

            bankSocket = new Socket(host, 8080);

            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());

            toBank = new ObjectOutputStream(bankSocket.getOutputStream());
            fromBank = new ObjectInputStream(bankSocket.getInputStream());


            Message user = (Message) fromClient.readObject();
            this.myName = user.username;
            System.out.println(myName);
            this.newHouse = user.newHouse;


        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }
    }

    public void run() {

        if (listener) {
            while (!KILL) {
                int counter = 0;
                for (int i = 0; i < threads.size(); i++) {
                    if (threads.get(i).newHouse || threads.get(i).amHouse) {
                        counter++;
                        String casted = String.valueOf(counter);
                        Message msg = new Message();
                        msg.notification = true;
                        threads.get(i).communicationBeta(msg, casted);
                    }
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {

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
                            if(request.isOver){
                                Message msg = new Message();
                                msg.username = request.username;
                                msg.placeHold = true;
                                msg.isOver = true;
                                msg.WON = true;
                                msg.bankKey = keys.get(request.biddingKey);
                                msg.bidAmount = request.bidAmount;
                                bankBroadcast(msg);
                                houseResponse(readFromBank());

                            }
                            if (request.houseList) houseResponse(request);

                            if (request.placeBid) {

                                Message msg = new Message();
                                msg.username = request.username;
                                msg.placeHold = true;
                                msg.bankKey = keys.get(request.biddingKey);
                                msg.bidAmount = request.bidAmount;
                                bankBroadcast(msg);
                                houseResponse(readFromBank());
                            }


                        }

                        if (request.notification) {
                            communicationBeta(request, request.selectedHouse);
                        }


                        if (request.register) {
                            response = new Message();

                            myName = request.username;
                            convertToInteger(request.username);

                            response.bankKey = clientBankKey;
                            response.verify = true;
                            response.username = myName;
                            bankBroadcast(response);
                            Message bankMsg = readFromBank();


                            if (bankMsg.isMember) {
                                Integer biddingKey = makeBiddingKey();
                                keys.put(biddingKey, clientBankKey);
                                registeredUsers.put(keys, myName);
                                response = new Message();
                                response.biddingKey = biddingKey;
                                response.message = "Your account has been activated, Bidding key -> " + biddingKey;
                                clientBroadcast(response);

                            } else {

                                bankMsg.message = "Bank account not found or account is already registered...";
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
                        if (request.fromBank && request.toUser) {
                            System.out.println("ENTERED new if");
                            houseResponse(request);
                        }

                        if (request.selectHouse) {
                            if(housesAvailable)
                                communicationBeta(request, request.selectedHouse);

                            else{
                                Message m = new Message();
                                m.message = "There are no houses available...";
                                clientBroadcast(m);
                            }

                        }


                    }

                }

            } catch (IOException e) {


            } catch (ClassNotFoundException e) {


            } finally {
                try {

                    System.out.println(myName + " is logging off...");
                    threads.remove(this);
                    if (this.amHouse) houseId--;
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
    }


    private void convertToInteger(String str) {
        String key = "";
        String name = "";
        boolean safe = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                key += str.charAt(i);
                safe = true;
            } else {
                name += str.charAt(i);
            }
        }
        myName = name.trim();
        clientBankKey = Integer.parseInt(key);
    }

    private void houseResponse(Message request) {
        for (AuctionCentral t : threads) {
            if (t.myName.equals(request.username)) {
                t.clientBroadcast(request);
            }
        }
    }

    private void communicationBeta(Message msg, String house) {
        int houseNumber = Integer.parseInt(house);
        int counter = 1;
        for (AuctionCentral t : threads) {
            if (t.amHouse && counter == houseNumber) {
                t.clientBroadcast(msg);
                break;
            } else if (t.amHouse) {
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
                    t.clientBroadcast(msg);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(AuctionCentral thread) {

        Message msg = new Message();
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
            if (hasHouse) {
                send.houses = houses;
                clientBroadcast(send);
            } else {
                Message msg = new Message();
                msg.message = "No Auction Houses are online right now";
                housesAvailable = false;
                clientBroadcast(msg);
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
            System.out.println("Testing");
            AuctionCentral listener = new AuctionCentral(true);
            listener.start();
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


