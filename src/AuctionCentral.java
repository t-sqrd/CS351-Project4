import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread {
    Socket socket;

    public static final int PORT_NUMBER = 8081;
    private Items itemList;
    private ArrayList<String> agents = new ArrayList<>();
    private ArrayList<String> bids = new ArrayList<>();


    public AuctionCentral(Socket socket) {
        this.socket = socket;
        this.itemList = Items.getInstance();
        start();
    }

    public void run() {
        InputStream in = null;
        OutputStream out = null;

        ObjectOutputStream toBank = null;
        ObjectInputStream fromBank = null;

        try {

            in = socket.getInputStream();
            out = socket.getOutputStream();

            toBank = new ObjectOutputStream(out);
            fromBank = new ObjectInputStream(in);

            Message request;
            System.out.println("Entered");
            Message response;
           while ((request = (Message)fromBank.readObject()) != null) {

               if(request.HOME) break;

               if(request.viewAuctionHouses){
                   response = new Message();
                   response.message = itemList.getClientString();
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
               }
               if(request.addAgent){
                   response = new Message();
                   response.message = addAgent(request.message, request.username);
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
               }
               // An agent is selecting the house they
               // would like to see the items of
               if(request.selectHouse){
                   String house = request.message;
                   response = new Message();
                   response.message = itemList.getItems(house);
                   System.out.println("sent items to agent");
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
               }
               if(request.register){
                   response = new Message();
                   response.message = "You successfully registered with auction central as " + request.username;
                   System.out.println("new auction items: " + request.items);
                   /*
                   When a AuctionHouse sends a 'Register' message to AuctionCentral
                   then it will initialize a new House object with the name of the
                   Registered AuctionHouse and it's items. It will add the house object
                   to the itemList object so they it can go back and get which items belong
                   to a specific house when requested by the agent.
                   */
                   House h = new House(request.username, request.items);
                   itemList.addHouse(request.username,h);
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
               }
               if(request.addItems){
                   response = new Message();
                   response.message = "Successfully added your items to the auction.";
                   System.out.println("adding these items: " + request.items);
                   response.message = itemList.addItems(request.username, request.items);
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
               }

               if(request.placeBid){
                   Boolean result = itemList.placeBid(request.username, request.message, request.bid);
                   response = new Message();
                   if(result){
                       response.message = "We placed a bid on " + request.username;
                   }else{
                       response.message = "Unable to place a bid on " + request.username;
                   }
                   response.placeBid = true;
                   toBank.writeObject(response);
                   toBank.flush();
                   toBank.reset();
                   System.out.println("should put a hold on the bank now");
               }

            }

            System.out.println("Exited");
        }



        catch (IOException e) {

            System.out.println("Unable to get streams from client in Server 2");
        }

        catch (ClassNotFoundException e) {
        }

        finally {
            try {

                fromBank.close();
                toBank.close();
                in.close();
                out.close();
                socket.close();

            }
            catch (IOException ex) {

                ex.printStackTrace();
            }
        }
    }


    public static String generateRandomChars(String candidateChars, int length) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(candidateChars.charAt(random.nextInt(candidateChars
                    .length())));
        }

        return sb.toString();
    }


    public static void main(String[] args) {
        System.out.println("Starting AuctionCentral");
        ServerSocket server = null;
        //String s = generateRandomChars("ABDCEF", 2);
        try {
            server = new ServerSocket(PORT_NUMBER);
            while (true) {
                /**
                 * create a new {@link SocketServer} object for each connection
                 * this will allow multiple client connections
                 */
                new AuctionCentral(server.accept());
            }

        } catch (IOException ex) {
            System.out.println("Unable to start AuctionCentral.");
        } finally {
            try {
                if (server != null)
                    server.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private String addAgent(String banKey, String name){
        agents.add(name);

        return "Successfully registered with auction central.\nYour bidding key is: @";
    }


}
