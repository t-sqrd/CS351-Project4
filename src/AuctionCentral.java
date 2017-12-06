import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread {

    Socket agentSocket;
    Socket houseSocket;

    public static final int CENTER_PORT = 8081;
    public static final int HOUSE_PORT = 4200;
    public final int HOUSE_PORT2 = 4201;
    public String host = "127.0.0.1";
    private String list;
    public static int count = 0;
    private int location;

    public static int agentId = 1;
    public static int houseId = 1;

    public static ArrayList<ClientThreads> threads = new ArrayList<>();


    public AuctionCentral(Socket socket) {
        this.agentSocket = socket;
        start();

    }


    public void run() {


        ObjectOutputStream toAgent = null;
        ObjectInputStream fromAgent = null;

        try {


            toAgent = new ObjectOutputStream(agentSocket.getOutputStream());
            fromAgent = new ObjectInputStream(agentSocket.getInputStream());

            Message request;
            Message reponse;


//            while ((request = (Message)fromAgent.readObject()) != null ) {
//                System.out.println("In Auction Central Server...");
//
//
//
//
////                System.out.println(request.message);
////                Message x  = new Message();
////                x.message = "From Central";
////                toAgent.writeObject(x);
//
//                if(request.viewAuctionHouses) {
//                    Message response = new Message();
//                    response.askForList = true;
//
//
//                }
//
//
//
//            }

        } catch (IOException e) {

            System.out.println("Unable to get streams from client in Server 2");
        }

//        catch (ClassNotFoundException e) {
//        }

        finally {
            try {


                threads.clear();
                fromAgent.close();
                toAgent.close();
                agentSocket.close();


            } catch (IOException ex) {

                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        System.out.println("Starting Auction Central...");
        ServerSocket fromAgent = null;
        ServerSocket fromHouse = null;
        try {
            fromAgent = new ServerSocket(CENTER_PORT);


            while (true) {


                ClientThreads t = new ClientThreads(fromAgent.accept());
                threads.add(t);
                t.start();
                //new AuctionCentral(fromAgent.accept());


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

    static class ClientThreads extends Thread {
        Socket socket;
        ObjectOutputStream toClient;
        ObjectInputStream fromClient;
        boolean sendlist, newHouse, selectHouse;
        boolean housesAvailable;
        volatile boolean KILL = false;
        String myName;
        Message init;


        public ClientThreads(Socket socket) {
            this.socket = socket;

            try {

                toClient = new ObjectOutputStream(socket.getOutputStream());
                fromClient = new ObjectInputStream(socket.getInputStream());

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

                   try {


                       nameClients();
                       newHouseListener(newHouse);
                       newHouse = false;

                       Message request;
                       if ((request = (Message) fromClient.readObject()) != null) {

                           sendlist = request.askForList;
                           selectHouse = request.selectHouse;
                           KILL = request.KILL;
                           System.out.println(request.KILL);

                           if (request.fromHouse) {
                               for (ClientThreads t : threads) {
                                   if (t.myName.equals(request.agentName)) {
                                       t.agentBroadcast(request.message);
                                   }
                               }
                           }

                       }

                       if (sendlist) sendHouseList();

                       if (selectHouse && housesAvailable) {
                           if (request != null) formCommunication(this.myName, request.message);
                       } else if (selectHouse && !housesAvailable) {
                           agentBroadcast("There are no houses available...");
                       }

                   } catch (IOException e) {


                   } catch (ClassNotFoundException e) {


                   }

                   try {
                       sleep(500);
                   } catch (InterruptedException e) {

                   }
               }
           }



//
            finally {
                try {

                    System.out.println("Exiting this client...");
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
            for (int i = 0; i < threads.size(); i++) {
                ClientThreads t = threads.get(i);
                if (t.myName.equals("House")) {
                    t.myName = "House " + houseId++;
                }
                if (t.myName.equals("Agent")) {
                    t.myName = "Agent " + agentId++;

                }
            }

        }



        private void formCommunication(String agent, String house) {
            Message msg = new Message();
            for (ClientThreads t : threads) {
                if (t.myName.equals(house)) {
                    msg.agentName = agent;
                    msg.getItems = true;
                    t.houseBroadcast(msg);
                }
            }
        }

        private void newHouseListener(boolean newHouse) {
            if (newHouse) {
                for (int i = 0; i < threads.size(); i++) {
                    ClientThreads t = threads.get(i);
                    if (t.myName.contains("Agent")) {
                        t.agentBroadcast("New AuctionHouse has entered! -> " + this.myName);
                        housesAvailable = true;
                    }
                }
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
                for (ClientThreads t : threads) {
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
            for (ClientThreads t : threads) {
                if (!t.isAlive()) {
                    System.out.println("DEAD THREAD -> " + t.myName);
                    threads.remove(t);
                    try {
                        t.toClient.close();
                        t.fromClient.close();
                        t.socket.close();
                    }

                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

