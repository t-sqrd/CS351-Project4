# Project-4-CS-351

Alex Schmidt, Tyson Craner(tcranerunm), Andrew Morin(admorin), Beau Kujath(bkuj15).

The auction simulator we made is mostly working, with a few minor bugs. You create just one Bank, and one Central Server. You can make as many Agent/House Clients as you want. Follow the prompts in the Agent Applications only, as the House Clients require no I/O. It uses integers for all bids and has a 30 second timer before you can win an item.

### Future Additions/Issues:

1) Allow users to deposit funds instead of always starting with 100.

2) Add UI to make interface more appealing than just the shell text.

3) The Bank does not communicate perfectly with the Central Server so sometimes their will be a delay in receiving
a message from the bank.

### Setup Instructions:
Steps to compile:
   1) clone the rep -> git clone https://github.com/alexGonzales/Project-4-CS-351.git
   2) to run Bank, Agent, AuctionCentral,  or AuctionHouses: 
     
     * Run through the shell: 
        - navigate into /src directory 
        - javac *.java
        - java [classname].java 
      
     * Run using jar files:
        - change into [classname]_jar/ directory
        - java -jar [classname].jar
         
   
3) Create the Bank or AuctionCentral as the first two running threads. 
4) Then run as many Agents or AuctionHouses as you'd like. 
5) The agents will need to make an account and register using their bidding key before they can view the houses online and bid.

