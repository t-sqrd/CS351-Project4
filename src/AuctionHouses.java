
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Created by alexschmidt-gonzales on 11/30/17.
 */
public class AuctionHouses extends Thread
{

    private int CENTRAL_PORT = 8081;
    private String host = "127.0.0.1";
    private ObjectOutputStream toCentralServer;
    private ObjectInputStream fromCentralServer;
    private Socket centralSocket;

    private Map<String, Integer> items = new HashMap<>();
    private Map<String, Bid> bids = new HashMap<>();
    private int houseBalance = 0;


    public static void main(String[] args)
    {
        new AuctionHouses();
    }

    // Create some random items and start the thread
    public AuctionHouses()
    {
        randomItems();
        start();
    }


    // Start listening for messages
    public void run()
    {
        try
        {

            System.out.println("Connecting to Agent " + host + " on port " + CENTRAL_PORT + ".");
            try
            {


                centralSocket = new Socket(host, CENTRAL_PORT);


                toCentralServer = new ObjectOutputStream(centralSocket.getOutputStream());
                fromCentralServer = new ObjectInputStream(centralSocket.getInputStream());


            } catch (UnknownHostException e)
            {
                System.err.println("Unknown host: " + host);
                System.exit(1);
            } catch (IOException e)
            {
                System.err.println("Unable to get streams from server in Auction houses");
                System.exit(1);
            }


            Message request;
            Message myName = new Message();
            myName.username = "House";
            myName.newHouse = true;
            toCentralServer.writeObject(myName);
            toCentralServer.flush();

            while ((request = (Message) fromCentralServer.readObject()) != null)
            {
                if (request.getItems)
                {
                    Message response = new Message();
                    response.agentName = request.agentName;
                    response.fromHouse = true;
                    response.isList = true;
                    response.message = getItemList();
                    toCentralServer.writeObject(response);
                    toCentralServer.flush();
                } else if (request.placeBid)
                {
                    Message response = new Message();
                    response.message = requestBid(request.message, request.bid, request.agentName, request.username, request.bankKey);
                    response.fromHouse = true;
                    response.agentName = request.agentName;
                    response.username = request.username;
                    toCentralServer.writeObject(response);
                    toCentralServer.flush();
                }
            }
            Message kill = new Message();
            kill.KILL = true;
            kill.HOUSE_LEAVING = true;
            toCentralServer.writeObject(kill);
            toCentralServer.flush();

            fromCentralServer.close();
            toCentralServer.close();
            centralSocket.close();

        } catch (Exception e)
        {
            e.printStackTrace();

        }
    }


    private String requestBid(String item, Integer bid, String agent, String userName, int bankKey)
    {
        String s;
        if (hasItem(item))
        {
            Boolean won = placeBid(item, bid);
            if (won)
            {
                addBid(item, agent, userName, bankKey);
                s = "You successfully placed a bid of " + bid + " for " + item + "\n" + getItemList();
            } else
            {
                s = "Nahh, that bid ain't enough for " + item;
            }
        } else
        {
            s = "Woah, that item doesn't exist in this house.";

        }
        return s;
    }

    private void addBid(String item, String agent, String userName, int bankKey)
    {
        if (bids.containsKey(item))
        {
            Bid b = bids.get(item);
            b.restartTime(agent);
        } else
        {
            Bid b = new Bid(item, agent, userName, bankKey);
            b.start();
            bids.put(item, b);
        }
    }

    private Boolean hasItem(String item)
    {
        return items.containsKey(item);
    }

    private Boolean placeBid(String item, Integer bid)
    {
        Integer oldBid = items.get(item);
        if (bid > oldBid)
        {
            // replace old bid with higher one
            items.replace(item, oldBid, bid);
            return true;
        } else
        {
            return false;
        }
    }

    private void randomItems()
    {
        Random random = new Random();
        String[] randItems = {"cat", "dog", "tree", "house", "rat", "bug", "alex", "hat", "tyson", "andrew", "tim tebow", "wig", "castle"};
        for (int i = 0; i < 5; i++)
        {
            int index = random.nextInt(7);
            int price = 0;
            items.put(randItems[index], price);
        }
    }


    private String getItemList()
    {
        String s = "";
        Iterator entries = items.entrySet().iterator();
        while (entries.hasNext())
        {
            Map.Entry thisEntry = (Map.Entry) entries.next();
            String key = (String) thisEntry.getKey();
            Integer price = (Integer) thisEntry.getValue();
            s += "~ " + key + " - " + price + " ";
            s += getTime(key) + "\n";
        }
        return s;
    }

    private String getTime(String item)
    {
        if (bids.containsKey(item))
        {
            Bid b = bids.get(item);
            return b.getTimeString();
        }

        return "";

    }

    private void printHouseInfo()
    {
        System.out.println("Just sold an item. The house balance is " + houseBalance);
        System.out.println("\nStill have for sale: ");
        System.out.println(getItemList());
    }


    // Inner class to keep track of timer
    class Bid extends Thread
    {

        private String item;
        private String agent;
        private String userName;
        private int agentKey;
        private int time = 0;
        private Boolean bidding = true;

        public Bid(String item, String agent, String userName, int agentKey)
        {
            this.item = item;
            this.agent = agent;
            this.agentKey = agentKey;
            this.userName = userName;
        }

        public void run()
        {
            while (bidding)
            {
                try
                {
                    time++;
                    sleep(1000);
                    if (time > 30)
                    {
                        winBid();
                    }
                } catch (InterruptedException e)
                {
                    System.out.println("interrupted: " + e.getMessage());
                }

            }

        }

        // Restart the 30 second timer when bid is placed
        public void restartTime(String agent)
        {
            sendOutbid();
            time = 0;
            this.agent = agent;
        }


        // Check the current timer value
        public String getTimeString()
        {
            String timer = " Time remaining: " + (30 - time);
            return timer;
        }


        private void sendOutbid()
        {
            Message response = new Message();
            String s = agent + ", you were outbid for " + item;
            s += "\n The bid was deposited back into your account.";
            response.message = s;
            response.isLoss = true;
            response.agentName = agent;
            response.username = userName;
            response.bankKey = agentKey;
            response.destination = item;
            try
            {
                toCentralServer.writeObject(response);
                toCentralServer.flush();
            } catch (IOException e)
            {
                System.out.println("exception: " + e.getMessage());
            }
        }


        private void winBid()
        {
            houseBalance += items.get(item);
            Message response = new Message();
            response.message = agent + ", you won the bid for " + item + "!";
            items.remove(item);
            bids.remove(item);
            printHouseInfo();
            response.isWin = true;
            response.username = userName;
            response.agentName = agent;
            response.bankKey = agentKey;
            response.destination = item;
            try
            {
                toCentralServer.writeObject(response);
                toCentralServer.flush();
                bidding = false;
            } catch (IOException e)
            {
                System.out.println(e.getMessage());
            }

        }

    }

}







