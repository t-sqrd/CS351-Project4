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
    int clientBankKey;
    public static int agentId = 1;
    public static int houseId = 1;

    public ObjectOutputStream toBank;
    public ObjectInputStream fromBank;

    private final String host = "127.0.0.1";





    public static ArrayList<AuctionCentral> threads = new ArrayList<>();
    public static HashMap<Integer, AuctionCentral> registeredUsers = new HashMap<>();
    public Bank b;
    public Socket bankSocket;



    public AuctionCentral(Socket socket) {
        this.socket = socket;

        try {

            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());

//            b = new Bank(bankSocket = new Socket(host, 8080));
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


                nameClients();
                newHouseListener(newHouse);
                newHouse = false;
                while ((request = (Message) fromClient.readObject()) != null) {

                    sendlist = request.askForList;
                    selectHouse = request.selectHouse;


                    //getting list of items from house then sending them to the requesting agent
                    if (request.fromHouse) {
                        for (AuctionCentral t : threads) {
                            if (t.myName.equals(request.agentName)) {
                                t.agentBroadcast(request.message);
                            }
                        }
                    }

                    if(request.fromBank){
                        System.out.println(request.message);
                    }

                    if (request.register) {
                        response = new Message();
                        this.clientBankKey = request.bankKey;
                        this.myName = request.agentName;
                        System.out.println("Agent Name = " + myName);
                        //registeredUsers.put(makeBiddingKey(), this);
                        response.bankKey = request.bankKey;
                        response.verify = true;
                        bankBroadcast(response);
                        readFromBank();



                    }

                    if (request.isMember) {
                        System.out.println("HERE");
                        System.out.println(request.message);
                    }


                    if (sendlist) {

                        sendHouseList();
                    }

                    if (selectHouse && housesAvailable) {
                        if (request != null) formCommunication(this.myName, request.message);
                    } else if (selectHouse && !housesAvailable) {
                        agentBroadcast("There are no houses available...");
                    }
                }
                houseLeavingListener(true);
            }


        } catch (IOException e) {


        } catch (ClassNotFoundException e) {


        } finally {
            try {

                System.out.println(myName + " is logging off...");
                threads.remove(this);
                fromClient.close();
                toClient.close();
                socket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void nameClients() {
        for (AuctionCentral t : threads) {
            if (t.myName.equals("House")) {
                t.myName = "House " + houseId++;
            }
            else if (t.myName.equals("Agent")) {
                t.myName = "Agent " + agentId++;

            }

        }

    }



    private void formCommunication(String agent, String house) {
        Message msg = new Message();
        for (AuctionCentral t : threads) {
            if (t.myName.equals(house)) {
                msg.agentName = agent;
                msg.getItems = true;
                t.houseBroadcast(msg);
            }
        }
    }

    private void newHouseListener(boolean newHouse) {
        if (newHouse) {
            for (AuctionCentral t : threads) {
                if (t.myName.contains("Agent")) {
                    t.agentBroadcast("New AuctionHouse has entered! -> " + this.myName);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(boolean leave) {
        if (leave) {
            System.out.println("Entered");
            for (AuctionCentral t : threads) {
                if (t.myName.contains("Agent")) {
                    t.agentBroadcast("Auction House " + this.myName + " is offline.");

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
    private void getMsgFromBank() throws ClassNotFoundException {
        try {

            Bank b = new Bank(new Socket(host, 8080));
            b.start();
            System.out.println(b.fromClient.readObject());

        } catch (IOException e) {

        }

    }





    private void agentBroadcast(String msg) {
        try {
            Message x = new Message();
            x.message = msg;
            toClient.writeObject(x);
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
            for (AuctionCentral t : threads) {
                System.out.println("USER = " + t.myName + " " + t);

                if (!t.equals(this) && t.myName.contains("House") && t.isAlive()) {
                    agentBroadcast("All houses online  " + t.myName);
                    hasHouse = true;
                    housesAvailable = true;

                }
            }
            System.out.println("------------------");
            if (!hasHouse) {
                String msg = "No Auction Houses are online right now";
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


    private int makeBiddingKey() {
        Random rand = new Random();
        int key = rand.nextInt(50);
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

//                ClientThreads t = new ClientThreads(fromAgent.accept());
//                threads.add(t);
//                t.start();


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

//    static class ClientThreads extends Thread {
//        Socket socket;
//        public ObjectOutputStream toClient;
//        public ObjectInputStream fromClient;
//        boolean sendlist, newHouse, selectHouse;
//        boolean housesAvailable, houseLeaving;
//        volatile boolean KILL;
//        volatile boolean KILL_HOUSE = false;
//        String myName;
//        Message init;
//        int clientBankKey;
//
//
//        public ClientThreads(Socket socket) {
//            this.socket = socket;
//
//            try {
//
//                toClient = new ObjectOutputStream(socket.getOutputStream());
//                fromClient = new ObjectInputStream(socket.getInputStream());
//
//                Message user = (Message) fromClient.readObject();
//                this.myName = user.username;
//                this.newHouse = user.newHouse;
//
//
//            } catch (IOException e) {
//
//            } catch (ClassNotFoundException e) {
//
//            }
//
//
//        }
//
//
//        public void run() {
//
//            try {
//                while (!KILL) {
//                    try {
//
//                        nameClients();
//                        newHouseListener(newHouse);
//                        newHouse = false;
//
//                        Message request;
//                        while ((request = (Message) fromClient.readObject()) != null) {
//
//                            sendlist = request.askForList;
//                            selectHouse = request.selectHouse;
//                            System.out.println(request.message);
//
//
//                            //getting list of items from house then sending them to the requesting agent
//                            if (request.fromHouse) {
//                                for (ClientThreads t : threads) {
//                                    if (t.myName.equals(request.agentName)) {
//                                        t.agentBroadcast(request.message);
//                                    }
//                                }
//                            }
//
//                            if(request.isMember){
//                                System.out.println("HERE");
//                                System.out.println(request.message);
//                            }
//
//                            if (request.register){
//                                Message msg = new Message();
//                                clientBankKey = request.bankKey;
//                                this.myName = request.agentName;
//                                System.out.println("Agent Name = " + myName);
//                                registeredUsers.put(makeBiddingKey(), null);
//                                msg.bankKey = clientBankKey;
//                                msg.verify = true;
//                                bankBroadcast(msg);
//                            }
//
//
//                            if (sendlist) {
//
//                                sendHouseList();
//                            }
//
//                            if (selectHouse && housesAvailable) {
//                                if (request != null) formCommunication(this.myName, request.message);
//                            } else if (selectHouse && !housesAvailable) {
//                                agentBroadcast("There are no houses available...");
//                            }
//                        }
//
//
//                    } catch (IOException e) {
//
//
//                    } catch (ClassNotFoundException e) {
//
//
//                    }
//
////                   try {
////                       sleep(500);
////                   }
////                   catch (InterruptedException e) {
////
////                   }
//                }
//            }
//
//
////
//            finally {
//                try {
//
//                    System.out.println("Exiting this client...");
//                    threads.remove(this);
//                    fromClient.close();
//                    toClient.close();
//                    socket.close();
//
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//        }
//
//
//        private void nameClients() {
//            for (int i = 0; i < threads.size(); i++) {
//                ClientThreads t = threads.get(i);
//                if (t.myName.equals("House")) {
//                    t.myName = "House " + houseId++;
//                }
//                if (t.myName.equals("Agent")) {
//                    t.myName = "Agent " + agentId++;
//
//                }
//
//            }
//
//        }
//
//
//        private void formCommunication(String agent, String house) {
//            Message msg = new Message();
//            for (ClientThreads t : threads) {
//                if (t.myName.equals(house)) {
//                    msg.agentName = agent;
//                    msg.getItems = true;
//                    t.houseBroadcast(msg);
//                }
//            }
//        }
//
//        private void newHouseListener(boolean newHouse) {
//            if (newHouse) {
//                for (int i = 0; i < threads.size(); i++) {
//                    ClientThreads t = threads.get(i);
//                    if (t.myName.contains("Agent")) {
//                        t.agentBroadcast("New AuctionHouse has entered! -> " + this.myName);
//                        housesAvailable = true;
//                    }
//                }
//            }
//
//        }
//
//        private void houseLeavingListener(boolean leave) {
//            if (leave) {
//                System.out.println("Entered");
//                for (ClientThreads t : threads) {
//                    if (t.myName.contains("Agent")) {
//                        t.agentBroadcast("Auction House " + this.myName + " is offline.");
//
//                    }
//                }
//            }
//        }
//        private void bankBroadcast(Message msg){
//            try {
//
//                Bank b = new Bank(new Socket("127.0.0.1", 8080));
//                b.toClient.writeObject(msg);
//                b.toClient.flush();
//                Message m = (Message)fromClient.readObject();
//                System.out.println("HERE" + m);
//
//
//            }
//
//            catch (IOException e){
//
//            }
//            catch (ClassNotFoundException e){
//
//            }
//
//        }
//
//
//        private void agentBroadcast(String msg) {
//            try {
//                Message x = new Message();
//                x.message = msg;
//                toClient.writeObject(x);
//                toClient.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//
//            }
//        }
//
//        private void houseBroadcast(Message msg) {
//            try {
//                toClient.writeObject(msg);
//                toClient.flush();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        private void sendHouseList() {
//            boolean hasHouse = false;
//            if (!threads.isEmpty()) {
//                for (ClientThreads t : threads) {
//                    System.out.println("USER = " + t.myName + " " + t);
//
//                    if (!t.equals(this) && t.myName.contains("House") && t.isAlive()) {
//                        agentBroadcast("All houses online  " + t.myName);
//                        hasHouse = true;
//                        housesAvailable = true;
//
//                    }
//                }
//                System.out.println("------------------");
//                if (!hasHouse) {
//                    String msg = "No Auction Houses are online right now";
//                    housesAvailable = false;
//                    agentBroadcast(msg);
//                }
//            }
//
//        }
//
//
//        private void remove() {
//            for (ClientThreads t : threads) {
//                if (!t.isAlive()) {
//                    System.out.println("DEAD THREAD -> " + t.myName);
//                    threads.remove(t);
//                    System.out.println(t.myName);
//                    try {
//                        t.toClient.close();
//                        t.fromClient.close();
//                        t.socket.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//
//        private int makeBiddingKey() {
//            Random rand = new Random();
//            int key = rand.nextInt(50);
//            if(registeredUsers.containsKey(key)){
//                makeBiddingKey();
//            }
//            return key;
//        }
//    }
//}
//
