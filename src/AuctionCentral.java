import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */
public class AuctionCentral extends Thread
{


    public static final int CENTER_PORT = 8081;

    Socket socket;
    public ObjectOutputStream toClient;
    public ObjectInputStream fromClient;
    private boolean sendlist, newHouse, selectHouse;
    private boolean housesAvailable;
    private volatile boolean KILL;
    private String myName;
    private int clientBankKey;
    private static int houseId = 1;

    public ObjectOutputStream toBank;
    public ObjectInputStream fromBank;
    private final String host = "127.0.0.1";


    public static ArrayList<AuctionCentral> threads = new ArrayList<>();
    public static HashMap<Integer, AuctionCentral> registeredUsers = new HashMap<>();
    private AgentList agentList;
    public Socket bankSocket;


    // Connect to socket passed in
    public AuctionCentral(Socket socket)
    {
        this.socket = socket;
        this.agentList = AgentList.getInstance();

        try
        {
            toClient = new ObjectOutputStream(socket.getOutputStream());
            fromClient = new ObjectInputStream(socket.getInputStream());

            bankSocket = new Socket(host, 8080);
            toBank = new ObjectOutputStream(bankSocket.getOutputStream());
            fromBank = new ObjectInputStream(bankSocket.getInputStream());

            Message user = (Message) fromClient.readObject();
            this.myName = user.username;
            this.newHouse = user.newHouse;

        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
    }

    // Start server to listen for messages
    public void run()
    {

        try
        {

            while (!KILL)
            {
                Message request;
                Message response;

                nameClients();
                newHouseListener(newHouse);
                newHouse = false;
                while ((request = (Message) fromClient.readObject()) != null)
                {

                    // Read messages and respond based on types
                    sendlist = request.askForList;
                    selectHouse = request.selectHouse;

                    if (request.isWin)
                    {
                        System.out.println("Transferring money from " + request.username + "'s account to auction house.");
                        bankBroadcast(request);
                        agentMessage(request);
                    }
                    if (request.isLoss)
                    {
                        bankBroadcast(request);
                        agentMessage(request);
                    }


                    if (request.fromHouse)
                    {
                        response = new Message();
                        response.isList = true;
                        response.message = request.message;
                        response.agentName = request.agentName;
                        agentMessage(response);
                    }

                    if (request.fromBank)
                    {
                        System.out.println(request.message);
                    }

                    if (request.register)
                    {
                        response = new Message();
                        String name = agentList.getNextAgent();
                        this.clientBankKey = request.bankKey;
                        this.myName = name;
                        response.bankKey = request.bankKey;
                        response.agentName = request.agentName;
                        response.verify = true;
                        bankBroadcast(response);
                        Boolean result = readFromBank();
                        Message m = new Message();
                        m.isMember = result;
                        m.register = true;
                        m.agentName = name;
                        agentSend(m);
                    }

                    if (request.placeBid)
                    {
                        // Try: can we just pass it request instead of this...
                        //Message m = new Message();
                        //m.message = request.message;
                        //m.bid = request.bid;
                        //m.username = request.username;
                        //m.placeBid = true;
                        //m.bankKey = request.bankKey;
                        //m.agentName = request.agentName;
                        bankBroadcast(request);
                        houseSend(request.username, request.destination, request); // the house agent wants to send to
                    }

                    if (request.isMember)
                    {
                        System.out.println("member " + request.message);
                    }

                    if (sendlist)
                    {
                        sendHouseList();
                    }

                    if (selectHouse && housesAvailable)
                    {
                        if (request != null)
                        {
                            Message m = new Message();
                            m.getItems = true;
                            m.agentName = request.agentName;
                            houseSend(request.username, request.message, m);
                        }
                    } else if (selectHouse && !housesAvailable)
                    {
                        agentBroadcast("There are no houses available...");
                    }
                }
                houseLeavingListener(true);
            }


        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e)
        {
            System.out.println(e.getMessage());
        } finally
        {
            try
            {

                System.out.println(myName + " is logging off...");
                threads.remove(this);
                fromClient.close();
                toClient.close();
                socket.close();


            } catch (IOException e)
            {
                e.printStackTrace();
            }

        }

    }

    private void nameClients()
    {
        for (AuctionCentral t : threads)
        {
            if (t.myName.equals("House"))
            {
                t.myName = "House " + houseId++;
            }

        }

    }


    private void houseSend(String agent, String house, Message m)
    {
        for (AuctionCentral t : threads)
        {
            if (t.myName != null)
            {
                if (t.myName.equals(house))
                {
                    //m.agentName = agent;
                    t.houseBroadcast(m);
                }
            }
        }
    }

    private void newHouseListener(boolean newHouse)
    {
        if (newHouse)
        {
            for (AuctionCentral t : threads)
            {
                if (t.myName.contains("Agent"))
                {
                    t.agentBroadcast("New AuctionHouse has entered! -> " + this.myName);
                    housesAvailable = true;
                }
            }
        }

    }

    private void houseLeavingListener(boolean leave)
    {
        if (leave)
        {
            System.out.println("Entered");
            for (AuctionCentral t : threads)
            {
                if (t.myName.contains("Agent"))
                {
                    t.agentBroadcast("Auction House " + this.myName + " is offline.");

                }
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

    private boolean readFromBank() throws ClassNotFoundException
    {

        try
        {

            Message m = (Message) fromBank.readObject();
            boolean member = m.isMember;

            System.out.println(member);

            System.out.println(m.message);
            return m.isMember;
        } catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
        return false;


    }


    private void agentMessage(Message m)
    {
        for (AuctionCentral t : threads)
        {
            if (t.myName != null)
            {
                if (t.myName.equals(m.agentName))
                {
                    t.agentSend(m);
                }
            }
        }
    }

    private void agentSend(Message m)
    {
        try
        {
            toClient.writeObject(m);
            toClient.flush();
        } catch (IOException e)
        {
            e.printStackTrace();

        }
    }


    private void agentBroadcast(String msg)
    {
        try
        {
            Message x = new Message();
            x.message = msg;
            toClient.writeObject(x);
            toClient.flush();
        } catch (IOException e)
        {
            e.printStackTrace();

        }
    }

    private void houseBroadcast(Message msg)
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
        String s = "";
        if (!threads.isEmpty())
        {
            for (AuctionCentral t : threads)
            {
                System.out.println("USER = " + t.myName + " " + t);

                if (!t.equals(this) && t.myName.contains("House") && t.isAlive())
                {
                    s += "~ " + t.myName + "\n";
                }
            }
            Message m = new Message();
            m.message = "All houses online  \n" + s;
            m.houseList = true;
            hasHouse = true;
            housesAvailable = true;
            agentSend(m);
            System.out.println("------------------");
            if (!hasHouse)
            {
                String msg = "No Auction Houses are online right now";
                housesAvailable = false;
                agentBroadcast(msg);
            }
        }

    }


    // Start up Auction Central server
    public static void main(String[] args)
    {
        System.out.println("Starting Auction Central...");
        ServerSocket fromAgent = null;
        try
        {
            fromAgent = new ServerSocket(CENTER_PORT);

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
