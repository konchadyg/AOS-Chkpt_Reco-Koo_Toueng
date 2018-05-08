//========================================================
//|     CS6378 - PROJECT 3                               |
//|     Member 1: Piyush Makani [PXM140430]              |
//|     Member 2: Konchady Gaurav Shenoy [KXS168430]     |
//|     Instructor: Dr. Neeraj Mittal                    |
//|     Nov-Dec 2016                                     |
//|     The University of Texas at Dallas                |
//========================================================


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;



public class Node extends Thread {
	
	static int identifier = 3;
	
	static int totalNoOfNodes;
	static int totalNoOfOperations;
	static int minInstanceDelay;
	static int minSendDelay;
	static int totalNoOfMessages;
	
	static NodeLocation[] nodeLocations;
	
	static Operations operations;
	
	static int[] neighbours;
	
	
	
	static int msgsSentYet = 0;
	static int[] vectorClock;
	
	static HashSet<Integer> grantSet = new HashSet<Integer>();
	static HashSet<Integer> failedSet = new HashSet<Integer>();
	//static PriorityQueue < Message >  queue = new PriorityQueue < Message > () ;
	
	static PrintWriter pw = null;
	
	static boolean operationInProgress = false;
	
	
	static Checkpoint[] checkpoints = new Checkpoint[50];
	static int checkpointsTOP = -1;
	
	
	static int[] last_label_rcvd;
	static int[] first_label_sent;
	
	static int[] last_label_sent;
	
	
	
	static int[] last_label_rcvd_backup;
	static int[] first_label_sent_backup;
	
	static int[] last_label_sent_temp;
	
	  
	
	
	
	
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException
	{
		identifier = Integer.parseInt(args[0]);
		readConfigFile();
 		
		//initialise timestamp
		vectorClock = new int[totalNoOfNodes];		
		
		
		//if (identifier == 0) {timestamp=10;}
		//else if (identifier == 1) {timestamp=8;}
		//else if (identifier == 2) {timestamp=5;}
		
		startServer();
		
		//************Take initail checkpoint
		takeCheckpoint();
		
		pw = new PrintWriter(new FileOutputStream(new File("GlobalStates.txt"),true));
		
		for(int i=0;i<totalNoOfNodes;i++)
			{
				pw.println("PROCESS "+i+" : 0 ");
			}
		
		pw.close();
		
		
		Thread.sleep(5000);
		
		initiateOperation();
		
		startSendingApplicationMessages();
		
		
		
	}
	
	
	

	public static void startSendingApplicationMessages() throws InterruptedException {
		
		
		
		
		while(msgsSentYet < totalNoOfMessages && !operationInProgress)
		{
			msgsSentYet++; 
			Thread.sleep(minSendDelay);
			
			//update vectorclock
			vectorClock[identifier]++;
			
			//create message
			Message m = new Message();
			m.originator = identifier;
			m.application = true;
			m.vectorClock = vectorClock;
			m.label = msgsSentYet;
			
			
			//choose random neighbor
			int randomNeighbourIndex = (new Random()).nextInt(neighbours.length);
			int randomNeighbour =  neighbours[randomNeighbourIndex];
			
			
			//update first_label_sent
			if(first_label_sent[randomNeighbour] == -1)
			{
				first_label_sent[randomNeighbour] = m.label;
			}
			
			//update last_label_sent_temp
				last_label_sent_temp[randomNeighbour] = m.label; 
			
				
			//send application message to this random neighbour
			sendMessage(m,randomNeighbour);
			
		}
		
	}





	public static void startServer() {
		
		//start listening as a server
		Node t1 = new Node();
		t1.start();
		//
	}

	
	
	
	private static void initiateOperation() throws InterruptedException, FileNotFoundException {
		
		if(operations.currentPointer<totalNoOfOperations && operations.initiaterID[operations.currentPointer] == identifier)
		{	
			
			//initiate checkpoint event
			if(operations.operationType[operations.currentPointer] == 'c')
			{
				pw = new PrintWriter(new FileOutputStream(new File("GlobalStates.txt"),true));
				pw.println("Global consistent state modifications: ");
				pw.close();
				
				
				System.out.println("Node "+identifier+ " will execute Checkpointing");
				operationInProgress=true;
				
				///////checkpointing algo
					takeCheckpoint();
				////////////////////////
				
				operationInProgress=false;
				sendCompleteToAll();
			
			}
			//initiate recovery event
			else
			{
				System.out.println("Node "+identifier+ " will execute Recovery");
				operationInProgress=true;
				
				///////recovery algo
					rollback();
				///////////////////
				
				operationInProgress=false;
				sendCompleteToAll();
				
				
			}
		}
		
	}
	


		
	
	private static void rollback() throws InterruptedException {
		
		System.out.println(identifier+" : ROLLBACK");
		
		//phase 1
			//the process rolls back  means process starts from the last checkpoint
			
			//**reset last_label_rcvd
			for(int i : neighbours)
			{
				last_label_rcvd[i] = -1;
			}
			
		//phase 2
		for(int j : neighbours)
		{
			//update vectorclock
			vectorClock[identifier]++;
			
			Message m = new Message();
			m.recoveryRequest = true;
			m.vectorClock = vectorClock;
			m.originator = identifier;
			m.llsValue = last_label_sent[j];
			
			//send checkpoint request to this cohort
			sendMessage(m, j);
		}
		
		//wait for minInstanceDelay before initiating appropriate protocol 
		Thread.sleep(minInstanceDelay);
		
	}




	private static void takeCheckpoint() throws InterruptedException, FileNotFoundException {
		
		System.out.println(identifier+" : CHECKPOINT");
		
		//First Phase
			//**********1-take tentative checkpoint
			//store seqNo, currentVectorClock, anyOtherInformationForApplicationRecovery
			
			checkpointsTOP++;
			Checkpoint newCheckPoint = new Checkpoint();
			
			//populate newCheckpoint
			//
			newCheckPoint.sequenceNumber = checkpointsTOP;
			newCheckPoint.vectorClock = vectorClock;
			newCheckPoint.last_label_rcvd = last_label_rcvd;
			newCheckPoint.first_label_sent = first_label_sent;
			newCheckPoint.last_label_sent = last_label_sent_temp;
			//
			pw = new PrintWriter(new FileOutputStream(new File("checkpointData"+identifier+".txt"),true));
			pw.println("CheckPoint Version : "+checkpointsTOP);
			pw.println("VectorClock : "+arrarIntoString(vectorClock));
			pw.println("LastLabelRcvd : "+arrarIntoString(last_label_rcvd));
			pw.println("FirstLabelSent : "+arrarIntoString(first_label_sent));
			pw.println("LastLabelSent : "+arrarIntoString(last_label_sent_temp));
			pw.println("");
			pw.println("----------------------------------------------------------------------------------");
			pw.println("");
			pw.close();
			
			
			
			pw = new PrintWriter(new FileOutputStream(new File("GlobalStates.txt"),true));
			pw.println("PROCESS "+identifier+" : "+checkpointsTOP);
			pw.close();
			//
			
			checkpoints[checkpointsTOP] = newCheckPoint;
			
		
		
			
		
		
		//Second Phase
			for(int j : neighbours)// all other processes Pj in my cohort
			{	
				if(last_label_rcvd[j] != -1)
				{
					//Send checkpoint request to Pj and send last_label_rcvd[j] to Pj
					
					//update vectorclock
					vectorClock[identifier]++;
					
					Message m = new Message();
					m.checkpointRequest = true;
					m.vectorClock = vectorClock;
					m.originator = identifier;
					m.llrValue = last_label_rcvd[j]; 
					
					//send checkpoint request to this cohort
					sendMessage(m, j);
				}
			}
			
			//reset all the entries of llr fls
			last_label_rcvd_backup = last_label_rcvd;
			first_label_sent_backup = first_label_sent;
			for(int i : neighbours)
			{
				last_label_rcvd[i] = -1;
				first_label_sent[i] = -1;
			}
			
			
			last_label_sent = last_label_sent_temp ;
			
			
			
			//Third Phase 
				//wait till all the processes did not checkpointed
			//wait for minInstanceDelay before initiating appropriate protocol 
			Thread.sleep(minInstanceDelay);
			
			
			
			
	}










	private static String arrarIntoString(int[] array) {
		String str= "[";
		for(int i : array)
		{
			str = str + "   " + i;
		}
		str= str + "]" ;
		return str;
	}




	public void run(){  
		Socket socket = null;
		try
        {	 
    		 int port = nodeLocations[identifier].portNumber; //knowing the portnumber I should host my server on 
    		 ServerSocket serverSocket = new ServerSocket(port);
             //System.out.println("Server Started and listening to the port "+ port);
             ObjectInputStream inStream = null;
             //Server is running always. This is done using this while(true) loop
             while(true)
             {
                 //Reading the message from the client
                 socket = serverSocket.accept();
                 inStream = new ObjectInputStream(socket.getInputStream());
                 Message m = (Message) inStream.readObject();
                 
//               System.out.println("Sever: object received");
//               System.out.println("originator: "+ m.originator);
//               System.out.println("timestamp: "+ m.timestamp);
//               System.out.println("request: "+ m.request);
//               System.out.println("grant: "+ m.grant);
//               System.out.println("release: "+ m.release);
//               System.out.println("inquire: "+ m.inquire);
//               System.out.println("yield: "+ m.yield);
//               System.out.println("failed: "+ m.failed);
//               System.out.println();
                 
                 //updating the timestamp
                 for(int i=0;i<totalNoOfNodes;i++)
                 {
                	 vectorClock[i] = ((vectorClock[i]>=m.vectorClock[i])?vectorClock[i]:m.vectorClock[i]);
                 }	
                 vectorClock[identifier]++;
                 
				 
                 
                 
                 
                 if(m.complete == true)
                 {
					 operations.currentPointer++;
					 initiateOperation();

                 }
                 
                 if(m.application == true)
                 {
                	 
                	 if(m.label > last_label_rcvd[m.originator])
                	 {
                		 last_label_rcvd[m.originator] = m.label;
                	 }
                 }
                 
                 
                 
                 if(m.checkpointRequest == true)
                 {
                	 if(m.llrValue >= first_label_sent[m.originator] && first_label_sent[m.originator] > -1 )
                	 {
                		 //I  take tentative checkpoint
                		 	takeCheckpoint();
                	 }
                 }
                 
                 
                 
                 if(m.recoveryRequest == true)
                 {
                	 if(last_label_rcvd[m.originator] > m.llsValue )
                	 {
                		 rollback();
                		 	
                	 }
                 }
                 
                 
             }
        }
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{ socket.close();
			}
			catch(Exception e){}
		}
	}
	
	
	

	
	
	public static void sendCompleteToAll()
	{		vectorClock[identifier]++;
		    for(int i=0; i<totalNoOfNodes; i++)
		    {
		    	Message m = new Message();
		    	m.vectorClock = vectorClock;
		    	m.complete = true;
		    	m.originator = identifier;
				//System.out.println("Sending request message from "+identifier+ " to "+quorumsMembers[i]);
				sendMessage(m, i);
		    }
	}
	
	
	
	
	
	
	
	
	
	
	public static void sendMessage( Message m, int toNode )
	{
		Socket socket = null;
		ObjectOutputStream outputStream = null;
		NodeLocation nodeLocation = nodeLocations[toNode];
		try
        {
			//System.out.println("!!!!!!!!Process Identfier: "+identifier+" trying to connect to server "+nodeLocation.serverName+" port: "+nodeLocation.portNumber);
			socket = new Socket(nodeLocation.serverName, nodeLocation.portNumber);
            //Send the message to the server
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(m);
            //System.out.println("Token sent to "+nextNodeLocation.serverName + "at port " + nextNodeLocation.portNumber);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            //Closing the socket
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
	}
	
	
	
	
	
	
	
	
	
	
	

	public static void readConfigFile()
	{
		BufferedReader br = null;
		try {
				String sCurrentLine;
				//br = new BufferedReader(new FileReader("/home/010/p/px/pxm140430/aos/project/config.txt"));
				//br = new BufferedReader(new FileReader("config.txt"));
				br = new BufferedReader(new FileReader("/home/012/k/kx/kxs168430/proj3/code/config.txt"));
				
				//Extracting fisrt line
				while ((sCurrentLine = br.readLine()) != null) 
				{
					if((int)sCurrentLine.charAt(0)>=48 && (int)sCurrentLine.charAt(0)<=57)
					{
						String[] splitArray = sCurrentLine.trim().replaceAll("\\s{2,}", " ").split("\\s+");	
						totalNoOfNodes = Integer.parseInt(splitArray[0]);
						totalNoOfOperations = Integer.parseInt(splitArray[1]);
						minInstanceDelay = Integer.parseInt(splitArray[2]);
						minSendDelay = Integer.parseInt(splitArray[3]);
						totalNoOfMessages = Integer.parseInt(splitArray[4]);
						break;
						
					}	
				}
				
//				System.out.println("totalNoOfNodes : " + totalNoOfNodes);
//				System.out.println("totalNoOfOperations : " + totalNoOfOperations);
//				System.out.println("minInstanceDelay : " + minInstanceDelay);
//				System.out.println("minSendDelay : " + minSendDelay);
//				System.out.println("totalNoOfMessages : " + totalNoOfMessages);

				//Extracting Location of all nodes.
				nodeLocations = new NodeLocation[totalNoOfNodes];
				for(int i=0;i<totalNoOfNodes;)
				{	sCurrentLine = br.readLine();
					if(!sCurrentLine.equals(null) && !sCurrentLine.equals("") && (int)sCurrentLine.charAt(0)>=48 && (int)sCurrentLine.charAt(0)<=57)
					{	
						sCurrentLine = sCurrentLine.trim();
						String[] splitArray = sCurrentLine.replaceAll("\\s{2,}", " ").split("\\s+");
						nodeLocations[i]=new NodeLocation(splitArray[1],Integer.parseInt(splitArray[2])); 
						i++;
					}
				}
				
				//testing if dataStructure nodeLocations is populated correctly or not
//					System.out.println("Node Location : ");
//					for(NodeLocation n :nodeLocations )
//					{	
//						System.out.println(n.serverName);
//						System.out.println(n.portNumber);
//						System.out.println();
//					}
				
					
					
				//	Extracting own neighbours
				for(int i=0;i<totalNoOfNodes;)
				{	sCurrentLine = br.readLine();
					if(!sCurrentLine.equals(null) && !sCurrentLine.equals("") && (int)sCurrentLine.charAt(0)>=48 && (int)sCurrentLine.charAt(0)<=57)
					{	
						sCurrentLine = sCurrentLine.trim();
						String[] splitArray = sCurrentLine.replaceAll("\\s{2,}", " ").split("\\s+");
						if(Integer.parseInt(splitArray[0]) == identifier)
						{	neighbours = new int[splitArray.length-1];
							for(int x=0; x<neighbours.length; x++)
								{neighbours[x] = Integer.parseInt(splitArray[x+1]);}
						}
						i++;
					}
				}
				
				
				
				
				//testing if dataStructure neighbours is populated correctly or not
//				for(int x=0; x<neighbours.length; x++)
//				{System.out.println(neighbours[x]);
//				}
				
//				for(int neighbour : neighbours)
//				{
//					System.out.println(neighbour);
//				}
				
				
				
				System.out.println();
				//Extracting operation
				operations = new Operations(totalNoOfOperations);
				for(int i=0;i<totalNoOfOperations;)
				{	
					sCurrentLine = br.readLine();
					if(!sCurrentLine.equals(null) && !sCurrentLine.equals("") && sCurrentLine.charAt(0)>='(')
					{	
						sCurrentLine = sCurrentLine.trim();
						sCurrentLine = sCurrentLine.replaceAll("\\s","");
						operations.operationType[i] = sCurrentLine.charAt(1);
						operations.initiaterID[i] = Integer.parseInt(""+sCurrentLine.charAt(3));
						i++;
					}
				}
				
				
				
				//testing wheather operations is populated properly or not
				
//				for(int i=0;i<totalNoOfOperations;i++)
//				{
//					System.out.println(operations.operationType[i]+"  " +operations.initiaterID[i]);
//				}
			
				
				
				
				// initialise llr and fls
				
				last_label_rcvd = new int[totalNoOfNodes];
				first_label_sent = new int[totalNoOfNodes];
				last_label_sent_temp = new int[totalNoOfNodes];
				last_label_sent = new int[totalNoOfNodes];
				
				for(int neighbour : neighbours)
				{
					last_label_rcvd[neighbour]=-1;
					first_label_sent[neighbour]=-1;
					last_label_sent_temp[neighbour]=-1;
					last_label_sent[neighbour]=-1;
				}
				
				
				
				

		}
		catch (IOException e) {
				e.printStackTrace();
			} 
		finally {
			try {
				if (br != null)br.close();
				}
			catch (IOException ex) {
				ex.printStackTrace();
				}
			}
		//System.out.println("Read Config file function finished here");
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}