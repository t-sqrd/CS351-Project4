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
    public static final int HOUSE_PORT =  4200;
    public final int HOUSE_PORT2 = 4201;
    public String host = "127.0.0.1";
    private String list;
    public static int count = 0;
    private int location;
    public static int agentId = 0;
    public static int houseId = 0;

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



            while ((request = (Message)fromAgent.readObject()) != null ) {
                System.out.println("In Auction Central Server...");




//                System.out.println(request.message);
//                Message x  = new Message();
//                x.message = "From Central";
//                toAgent.writeObject(x);

                if(request.viewAuctionHouses) {
                    Message response = new Message();
                    response.askForList = true;


                }



            }

        }

        catch (IOException e) {

            System.out.println("Unable to get streams from client in Server 2");
        }

        catch (ClassNotFoundException e) {
        }

        finally {
            try {


                fromAgent.close();
                toAgent.close();
                agentSocket.close();


            }
            catch (IOException ex) {

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


        }

        catch (IOException ex) {
            System.out.println("Unable to start Auction Central.");
        }
        finally {
            try {
                if (fromAgent != null) fromAgent.close();

            }

            catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    static class ClientThreads extends Thread {
        Socket socket;
        ObjectOutputStream toClient;
        ObjectInputStream fromClient;
        boolean sendlist;
        String name;
        String myName;
        Message init;
        int sort;



        public ClientThreads(Socket socket) {
            this.socket = socket;
            ++agentId;

            try {

                toClient = new ObjectOutputStream(socket.getOutputStream());
                fromClient = new ObjectInputStream(socket.getInputStream());

                Message user = (Message)fromClient.readObject();
                this.myName = user.username;



//                Message m = new Message();
//                if(sort == 1){
//                    System.out.println("Auction House Connected...");
//                    this.username = "House " + agentId++;
//                    m.username = username;
//                    toClient.writeObject(m);
//                }
//                else if(sort == 2){
//                    System.out.println("Agent connected...");
//                    this.username = "Agent " + houseId++;
//                    m.username = username;
//                    toClient.writeObject(m);
//
//                }

            } catch (IOException e) {

            }
            catch(ClassNotFoundException e){

            }


        }



        public void run() {

            while (true) {

                try {


                    for(int i = 0; i < threads.size(); i++){
                        ClientThreads t = threads.get(i);
                        if(t.myName.equals("House")){
                            t.myName = "House " + houseId++;
                        }
                        else if(t.myName.equals("Agent")){
                            t.myName = "Agent " + agentId++;

                        }
                    }

                    Message request;
                    if((request  = (Message)fromClient.readObject()) != null){
                        sendlist = request.askForList;

                    }

                    if (sendlist) {

                        if (!threads.isEmpty()) {
                            for (int i = 0; i < threads.size(); i++) {

                                ClientThreads t = threads.get(i);
                                System.out.println("USER = " + t.myName + " " + t);
                                if (!t.equals(this) && t.myName.contains("House")) {
                                    broadcast("All houses online : " + t.myName);
                                }
                            }
                        }
                        else {

                            String msg = "No Auction Houses are online right now";
                            broadcast(msg);

                        }

                    }



                }

                catch (IOException e) {
                }

                catch(ClassNotFoundException e){

                }

            }
        }


        private void broadcast(String msg){
            try {


                Message x = new Message();
                x.message = msg;
                toClient.writeObject(x);
                toClient.flush();
            }
            catch (IOException e){

            }
        }

    }
}

