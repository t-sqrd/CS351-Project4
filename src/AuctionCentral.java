import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread
{

    public static final int CENTER_PORT = 8081;


    Socket socket;
    public ObjectOutputStream toClient;
    public ObjectInputStream fromClient;
    private boolean newHouse, listener;
    private boolean housesAvailable, amHouse;
    volatile boolean KILL;
    private String myName;
    private Integer clientBankKey;
    public static int houseId = 0;

    public ObjectOutputStream toBank;
    public ObjectInputStream fromBank;

    private final String host = "127.0.0.1";
    public static ArrayList<AuctionCentral> threads = new ArrayList<>(); // keep track of threads to talk to

    public static HashMap<Integer, Integer> keys = new HashMap<>();
    public static HashMap<HashMap<Integer, Integer>, String> registeredUsers = new HashMap<>();
    public Socket bankSocket;

    public AuctionCentral(boolean listener)
    {
        this.listener = listener;
    }

    // Creates a new socket to connect to Central
    public AuctionCentral(Socket socket)
    {
        this.socket = socket;

        try
        {

            bankSocket = new Socket(host, 8080);

            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());

            toBank = new ObjectOutputStream(bankSocket.getOutputStream());
            fromBank = new ObjectInputStream(bankSocket.getInputStream());


            Message user = (Message) fromClient.readObject();
            this.myName = user.username;
            System.out.println(myName);
            this.newHouse = user.newHouse;


        } catch (IOException e)
        {

        } catch (ClassNotFoundException e)
        {

        }
    }

    // Starts the server and listens for
    // messages from agent or houses
    public void run()
    {

        if (listener)
        {
            while (!KILL)
            {
                int counter = 0;
                for (int i = 0; i < threads.size(); i++)
                {
                    if (threads.get(i).newHouse || threads.get(i).amHouse)
                    {
                        counter++;
                        String casted = String.valueOf(counter);
                        Message msg = new Message();
                        msg.notification = true;
                        threads.get(i).communicationBeta(msg, casted);
                    }
                }
                try
                {
                    sleep(1000);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        } else
        {

            try
            {

                while (!KILL)
                {
                    Message request;
                    Message response;

                    newHouseListener(newHouse);
                    newHouse = false;
                    while ((request = (Message) fromClient.readObject()) != null)
                    {
                        //check what type of message was received
                        if (request.fromHouse)
                        {
                            houseResponse(request);
                            if (request.isOver)
                            {
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

                            if (request.placeBid)
                            {

                                Message msg = new Message();
                                msg.username = request.username;
                                msg.placeHold = true;
                                msg.bankKey = keys.get(request.biddingKey);
                                msg.bidAmount = request.bidAmount;
                                bankBroadcast(msg);
                                houseResponse(readFromBank());
                            }
                        }

                        if (request.notification)
                        {
                            communicationBeta(request, request.selectedHouse);
                        }


                        if (request.register)
                        {
                            response = new Message();

                            myName = request.username;
                            clientBankKey = request.bankKey;

                            response.bankKey = clientBankKey;
                            response.verify = true;
                            response.username = myName;
                            bankBroadcast(response);
                            Message bankMsg = readFromBank();


                            if (bankMsg.isMember)
                            {
                                Integer biddingKey = makeBiddingKey();
                                keys.put(biddingKey, clientBankKey);
                                registeredUsers.put(keys, myName);
                                response = new Message();
                                response.biddingKey = biddingKey;
                                response.message = "Your account has been activated, Bidding key -> " + biddingKey;
                                clientBroadcast(response);

                            } else
                            {
                                clientBroadcast(bankMsg);
                            }


                        }

                        if (request.isMember)
                        {
                            System.out.println(request.message);
                        }

                        if (request.askForList)
                        {
                            sendHouseList();
                        }
                        if (request.fromBank && request.toUser)
                        {
                            System.out.println("ENTERED new if");
                            houseResponse(request);
                        }

                        if (request.selectHouse)
                        {
                            if (housesAvailable)
                                communicationBeta(request, request.selectedHouse);

                            else
                            {
                                Message m = new Message();
                                m.message = "There are no houses available...";
                                clientBroadcast(m);
                            }

                        }


                    }

                }

            } catch (IOException e)
            {


            } catch (ClassNotFoundException e)
            {


            } finally
            {
                try
                {

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


                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

        }
    }


    private void houseResponse(Message request)
    {
        for (AuctionCentral t : threads)
        {
            if (t.myName.equals(request.username))
            {
                t.clientBroadcast(request);
            }
        }
    }

    private void communicationBeta(Message msg, String house)
    {
        int houseNumber = Integer.parseInt(house);
        for (AuctionCentral t : threads)
        {
            if (t.amHouse && t.myName.equals("House #" + houseNumber))
            {
                t.clientBroadcast(msg);
                break;
            }
        }

    }

    private void newHouseListener(boolean newHouse)
    {
        if (newHouse)
        {
            houseId++;
            this.amHouse = true;
            Message msg = new Message();
            for (AuctionCentral t : threads)
            {
                if (t.myName.contains("Agent"))
                {
                    msg.message = "New AuctionHouse has entered! -> " + this.myName;
                    t.clientBroadcast(msg);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(AuctionCentral thread)
    {

        Message msg = new Message();
        for (AuctionCentral t : threads)
        {
            if (thread.myName.contains("House") && t.myName.contains("Agent"))
            {
                msg.message = thread.myName + " is going offline...";
                t.clientBroadcast(msg);
            }
        }
    }

    private void bankBroadcast(Message m) throws ClassNotFoundException
    {
        try
        {
            toBank.writeObject(m);
            toBank.flush();

        } catch (IOException e)
        {

        }

    }

    private Message readFromBank() throws ClassNotFoundException
    {

        Message m = new Message();
        try
        {

            m = (Message) fromBank.readObject();

            return m;
        } catch (IOException e)
        {

        }
        return m;


    }

    private void clientBroadcast(Message msg)
    {
        try
        {

            toClient.writeObject(msg);
            toClient.flush();

        } catch (IOException e)
        {
            e.printStackTrace();

        }
    }

    private void sendHouseList()
    {
        boolean hasHouse = false;
        if (!threads.isEmpty())
        {
            Message send = new Message();
            send.houseList = true;
            String[] houses = new String[houseId];
            int counter = 0;
            for (AuctionCentral t : threads)
            {
                if (!t.equals(this) && t.myName.contains("House"))
                {
                    houses[counter] = t.myName;
                    hasHouse = true;
                    housesAvailable = true;
                    counter++;
                }
            }
            if (hasHouse)
            {
                send.houses = houses;
                clientBroadcast(send);
            } else
            {
                Message msg = new Message();
                msg.message = "No Auction Houses are online right now";
                housesAvailable = false;
                clientBroadcast(msg);
            }
        }

    }


    private Integer makeBiddingKey()
    {
        Random rand = new Random();
        Integer key = rand.nextInt(50);
        if (registeredUsers.containsKey(key))
        {
            makeBiddingKey();
        }
        return key;
    }


    public static void main(String[] args)
    {
        System.out.println("Starting Auction Central...");
        ServerSocket fromAgent = null;
        try
        {
            fromAgent = new ServerSocket(CENTER_PORT);
            AuctionCentral listener = new AuctionCentral(true);
            listener.start();
            while (true)
            {
                AuctionCentral c = new AuctionCentral(fromAgent.accept());
                threads.add(c);
                c.start();

            }


        } catch (IOException ex)
        {
            System.out.println("Unable to start Auction Central.");
        } finally
        {
            try
            {
                if (fromAgent != null) fromAgent.close();

            } catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

}


