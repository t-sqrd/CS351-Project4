/**
 * Created by BeauKujath on 30/11/2017.
 */
public class AgentList
{
    private static AgentList single = null;
    private int count = 0;

    // Used for naming agents.
    private AgentList()
    {
    }

    // static method to create instance of Singleton class
    public static AgentList getInstance()
    {
        if (single == null)
            single = new AgentList();

        return single;
    }

    // Just used to name agent threads
    public String getNextAgent()
    {
        count++;
        return "Agent " + count;
    }


}
