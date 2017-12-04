import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class AuctionHouse {
    private ArrayList<String> items = new ArrayList<>();
    private String host;
    private int port;
    private String name = "";

    public static void main(String args[]) {
        String host = "127.0.0.1";
        int port = 8081;
        new AuctionHouse(port, host);
    }

    public AuctionHouse(int port, String host){
        System.out.println("auction house created");
        this.port = port;
        this.host = host;
        register();
    }

    private void register(){
        try {

            System.out.println("Connecting to host " + host + " on port " + host + ".");
            Socket centralSocket = null;
            BufferedReader in = null;

            ObjectOutputStream toCentralServer = null;
            ObjectInputStream fromCentralServer = null;

            try {

                centralSocket = new Socket(host, port);

                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());

            } catch (UnknownHostException e) {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Unable to get streams from server in Agent");
                System.exit(1);
            }

            System.out.println("Options : Register/Add Item");
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            Boolean registerInput = false;
            String request;

            while((request = stdin.readLine()) != null){
                System.out.println("Message sent: " + request);

                // Waiting on register input from user
                if(registerInput){
                    System.out.println("Registering with id: " + request);
                    name = request;
                    Message send = new Message();
                    send.register = true;
                    send.ip = host;
                    send.username = name;
                    send.items = items;
                    toCentralServer.writeObject(send);
                    System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                    System.out.println("\nOptions : Add Item");
                    registerInput = false;
                }


                // Add check here to see if user has been registered already.
                // If so then it should send an add items message to central
                // that will just get the house object that matches this id/username
                // and add the new items to the list for that House
                if(request.equals("Register")){
                    if(name.equals("")){
                        System.out.println("Enter name/id you would like to register as: ");
                        registerInput = true;
                    } else {
                        System.out.println("You are already registered as: " + name);
                        Message send = new Message();
                        send.addItems = true;
                        send.username = name;
                        send.items = items;
                        toCentralServer.writeObject(send);
                        System.out.println(((Message)fromCentralServer.readObject()).getMessage());
                        System.out.println("\n\nOptions : Add Item");
                    }

                }

                // Add list of items
                if(request.equals("Add Item")){
                    inputItems(stdin);
                }
                toCentralServer.flush();
                toCentralServer.reset();

            }

            /** Closing all the resources */
            toCentralServer.close();
            fromCentralServer.close();

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void inputItems(BufferedReader stdin){
        System.out.println("Enter item you would like to auction off.");
        String item;
        try
        {
            while((item = stdin.readLine()) != null && !item.equals("q")){
                if(!items.contains(item)){
                    items.add(item);
                    System.out.println("Added " + item + " to the auction");
                }
                System.out.println("would you like to add more? (q to quit)");
            }

        }catch (IOException e){
            System.out.println("caught it");
        }
        System.out.println("Auction list: \n" );
        printList(items);
        System.out.println("Options: Register/Add Item");
    }

    private void printList(ArrayList<String> list){
        for(int j = 0; j < list.size(); j ++){
            System.out.println("item " + (j +1) + ": " + list.get(j));
        }
    }


}
