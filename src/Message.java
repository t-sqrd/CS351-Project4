/**
 * Created by alexschmidt-gonzales on 11/28/17.
 */

import java.io.Serializable;

public class Message implements Serializable
{
    private static final long serialVersionUID = -5399605122490343339L;

    String username;
    String agentName;
    boolean newAccount;
    volatile boolean KILL;
    boolean HOUSE_LEAVING;

    boolean askForList, getItems, houseList, newHouse;
    boolean fromHouse, register, selectHouse, isList, isWin;
    boolean placeBid, verify, isMember, fromBank, isLoss;
    int bid, bankKey;
    String destination;
    String message;


    public Message()
    {
    }

    public Message(String message)
    {

        username = message;
    }

    public String getMessage()
    {
        return message;
    }


}