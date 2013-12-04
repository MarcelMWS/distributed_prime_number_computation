import java.io.*;
import java.net.*;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.util.Scanner;

public class Server implements Runnable
{
	private static BigInteger num; // the tested number
	private BigInteger prevNum; // the previous tested number
	private static ServerSocket server = null; // the socket that will find all incoming clients
	private static List<BigInteger> primes=new ArrayList<BigInteger>(); // array of primes that is stored
	private static List<Boolean> isPrime=new ArrayList<Boolean>(); // variable used to check to see what each computer thinks of a specific number (prime or not)
	private static List<Boolean> isDone=new ArrayList<Boolean>();  // used to check if all clients are done computing
	private static List<Boolean> isSent=new ArrayList<Boolean>();  // used to check if all clients have the number
	private static List<Boolean> goodClients = new ArrayList<Boolean>(); // true if the client is still sending proper data--false if it's not
	private static Lock checks_lock = new ReentrantLock(); //locks down isPrime isDone and isSent for writing
	private static int numClients=0;
	private static boolean send; // tells the threads if it should send to a client
	private static int nextSpot=0; // the next number available in the arrays

	private boolean recieve;
	private boolean connected = true;
	private Socket connSocket;
	private int socketSpot; // this thread's spot in the arrays
	private int badAttempts=0; // counts the number of bad attempts for any given client before it's shutdown
	private final int maxBadAttempts=3;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Server(Socket conn)
	{
		connSocket = conn;
		socketSpot=nextSpot;
		nextSpot++;

		checks_lock.lock();
		try
		{
			isPrime.add(Boolean.FALSE);
			isDone.add(Boolean.FALSE);
			isSent.add(Boolean.FALSE);
			goodClients.add(Boolean.TRUE); // indicates a client is good and doesn't need to be ignored
			numClients++;
		}
		finally
		{
			checks_lock.unlock();
		}
	}


	private static void setClientToBad(int c)// the spot to start shifting down
	{
		checks_lock.lock();
		try
		{
			goodClients.set(c,Boolean.FALSE);
			isDone.set(c,Boolean.TRUE); //these values are set to avoid any interference
                        isPrime.set(c,Boolean.TRUE);//with the values from other threads if accidentally
                        isSent.set(c,Boolean.TRUE); //accessed.
			numClients--;

		}
		finally
		{
			checks_lock.unlock();
		}
		return;
	}
	public void sendNumber(BigInteger num)
	{
		try{
			out.writeObject(num);
			out.flush();
		}
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
        public void sendMessage(String msg)
        {
                try{
                        out.writeObject(msg);
                        out.flush();
                }
                catch(IOException ioException){
                        ioException.printStackTrace();}
        }

        public String recieveMessage()
        {
                try
                {
                        return (String) in.readObject();
                }
                catch(Exception e)
                {
                        System.out.println("Bad Object Type");
                        System.out.println(e);
                        badAttempts++;
			return null;
                }
        }
        public boolean recieveAndCheckMessage(String equ)
        {
                try
                {
                        return recieveMessage().equals(equ);
                }
                catch(Exception e)
                {
                        System.out.println("Bad Object Type");
                        System.out.println(e);
			badAttempts++;
                        return false;
                }
        }

	public void run()
	{
		System.out.println( " A new client connected--thread running" );
		try
		{
			System.out.print("Creating PrintWriter...");
			out = new ObjectOutputStream( connSocket.getOutputStream() );
			System.out.println("Created");
			System.out.print("Creating Input Scanner...");
			in = new ObjectInputStream( connSocket.getInputStream() );
			System.out.println("Created");
		}
		catch ( IOException e )
		{
			System.out.println("Couldn't make network IO with client");
			return;
		}
		sendMessage("connected");
		System.out.println("Sending verification...waiting...");
		if(!recieveAndCheckMessage("connected"))
		{
			System.out.println("Not the client you're looking for.");
			return;
		}
		System.out.println("Connected to "+connSocket);
		while( connected )
		{	
			if(badAttempts>=maxBadAttempts)
			{
				System.out.println(connSocket+" went bad. Ending connection");
                                setClientToBad(socketSpot);
				return; // ends the thread by exiting the void

			}
			if(!isDone.get(socketSpot).booleanValue()||send)//if this client is done you don't need to wait to get stuff cause it shouldn't be sending anything
			{
			if(!isSent.get(socketSpot).booleanValue()&&!num.equals(prevNum))//don't saturate the network with two or more of the same number
                        {
	                        checks_lock.lock();
                                try
        	                {
                	                isDone.set(socketSpot,Boolean.FALSE);
                        	        isPrime.set(socketSpot,Boolean.FALSE);
					isSent.set(socketSpot,Boolean.TRUE);
					sendNumber(num);
					prevNum=num.add(BigInteger.ZERO);
                                }
                                finally
                                {
                 	               checks_lock.unlock();
                        	}

				//System.out.println("number sent to " + connSocket.getLocalAddress());
				recieve=true;
			}
			if(recieve)
			{
				Boolean temp= Boolean.FALSE;
				try
				{
					temp = Boolean.parseBoolean(recieveMessage());
					recieve=false;
				}
				catch (Exception e)
				{
					System.out.println("Bad connection--retrying");
					badAttempts++;
				}
				if(!recieve)
				{
					checks_lock.lock();
					try
					{		
						isDone.set(socketSpot,Boolean.TRUE);
						isPrime.set(socketSpot,temp);
						isSent.set(socketSpot,Boolean.FALSE);
					}
					finally
					{
						checks_lock.unlock();
					}
				}
			}
			}
			else
			{
				//System.out.println(connSocket+" done and waiting");
			}
			try{
			Thread.sleep(100);}
			catch ( InterruptedException e ){
			System.out.println("A socket got interrupted");
			badAttempts++;}
		}
	}

	public static boolean isReadyForNext()//checks to see if all clients have reported back
	{
		boolean done = true;
		for( int i = 0 ; i < isDone.size() ; i++)
		{	if (goodClients.get(i).booleanValue()){
				done = done && isDone.get(i).booleanValue();}
		}
		return done;
	}

	public static boolean isAllSent()//checks to see if the server sent out the data to all of the clients
	{
		if (isSent.size()==0)
		{
			//System.out.println("No Clients Connected");
			return false;
		}
		boolean sent = true;
		for( int i = 0 ; i < isSent.size() ; i++)
		{	if (goodClients.get(i).booleanValue()){
				sent  = sent && isDone.get(i).booleanValue();}
		}
		return sent;
	}
	
	public static void waitForAllSent()//simply waits for all of the data 
	{
		while( !isAllSent() )
		{
                        try{
                        Thread.sleep(100);}
                        catch ( InterruptedException e ){
                        System.out.println(e);}

		}
	}
	
	public static void main( String[] args )//First argument is starting number
	{
		num = new BigInteger(args[0]);
		if (!num.testBit(0) ) // avoids starting with an even number
		{
			num=num.add(BigInteger.ONE);
		}
		try{
			server = new ServerSocket( 33333 );
			server.setSoTimeout(50);
		}
		catch ( IOException e ){
			System.out.println( "Could not open server socket." );
		}		
		System.out.println( "Waiting for clients to connect ... " );
		send = true;
		while ( true )
		{
			Server newClient=null;
			try
			{
				newClient = new Server(server.accept());
				Thread t = new Thread(newClient);
				t.start();

			}
			catch ( Exception e )
			{
//				System.out.println("server timeout"); //can be ignored because it times out quite often
			
			}
			if ( isReadyForNext() && !send && numClients>0)
			{
                                checks_lock.lock();
                                try
	                        {
					boolean isNumPrime = true;
					for(int i = 0 ; i < isDone.size() ; i++)
					{
						if(goodClients.get(i).booleanValue())
						{
							isNumPrime = isNumPrime && isPrime.get(i).booleanValue();
        	                        		isDone.set(i,Boolean.FALSE);
                                        		isPrime.set(i,Boolean.FALSE);
                                        		isSent.set(i,Boolean.FALSE);
						}
					}
					if (isNumPrime)
					{
						primes.add(num); // adds to the list of prime numbers
						System.out.println(num);
					}
					num=num.add(BigInteger.ONE).add(BigInteger.ONE); //adding two avoids evens cause they're not prime anyway
                                }
                                finally
                                {
                  	              checks_lock.unlock();
                                }
				send = true;
			}
			
			if ( isAllSent() )
			{
				send = false;
			}
                        try{
                        Thread.sleep(10);}//the smaller the sleep time the faster the server will run and the less times there will be error in what gets sent over
                        catch ( InterruptedException e ){
                        System.out.println("Main loop got interrupted");}

		}
	}
}
