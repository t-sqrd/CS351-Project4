import java.util.ArrayList;

/**
 * Created by BeauKujath on 02/12/2017.
 */
public class House
{

    private String userName;
    private ArrayList<String> items;


    public House(String userName, ArrayList<String> items)
    {
        this.userName = userName;
        this.items = items;
    }

    public String getUserName(){
        return userName;
    }

    public void addItems(ArrayList<String> newItems){
        for(int j = 0; j < newItems.size(); j ++){
            String item = newItems.get(j);
            if(!items.contains(item)){
                items.add(item);
            }
        }
    }

    public String getItemString(){
        String s = "";
        s += "Items in " + userName + ":\n";
        for(int i = 0; i < items.size(); i ++){
            s += items.get(i) + "\n";
        }
        return s;
    }
}
