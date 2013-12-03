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
	private static BigInteger num;
	private static ServerSocket server = null;
	private static List<BigInteger> primes=new ArrayList<BigInteger>();
	private static List<Boolean> isPrime=new ArrayList<Boolean>();
	private static List<Boolean> isDone=new ArrayList<Boolean>();
	private static List<Boolean> isSent=new ArrayList<Boolean>();
	private static Lock checks_lock = new ReentrantLock();
	private static boolean send;
	private static int nextSpot=0;

	private boolean recieve;
	private boolean connected = true;
	private Socket connSocket;
	private int socketSpot;
	
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Server(Socket conn)
	{
		connSocket = conn;
		socketSpot=nextSpot;
		nextSpot++;
		isPrime.add(Boolean.FALSE);
		isDone.add(Boolean.FALSE);
		isSent.add(Boolean.FALSE);
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
			if(!!isDone.get(socketSpot).booleanValue()||send)//if this client is done you don't need to wait to get stuff cause it shouldn't be sending anything
			{
			if(send&&!recieve)
                        {
	                        checks_lock.lock();
                                try
        	                {
                	                isDone.set(socketSpot,Boolean.FALSE);
                        	        isPrime.set(socketSpot,Boolean.FALSE);
					isSent.set(socketSpot,Boolean.TRUE);
                                }
                                finally
                                {
                 	               checks_lock.unlock();
                        	}

				sendNumber(num);
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
				System.out.println(connSocket+" done and waiting");
			}
			try{
			Thread.sleep(100);}
			catch ( InterruptedException e ){
			System.out.println("A socket got interrupted");}
		}
	}

	public static boolean isReadyForNext()//checks to see if all clients have reported back
	{
		boolean done = true;
		for( int i = 0 ; i < isDone.size() ; i++)
		{
			System.out.println(i + ":" + isDone.get(i).booleanValue());
			done = done && isDone.get(i).booleanValue();
		}
		System.out.println("Total done: " + done);
		return done;
	}

	public static boolean isAllSent()
	{
		if (isSent.size()==0)
		{
			System.out.println("No Clients Connected");
			return false;
		}
		boolean sent = true;
		for( int i = 0 ; i < isSent.size() ; i++)
		{
			sent  = sent && isDone.get(i).booleanValue();
		}
		return sent;
	}
	
	public static void waitForAllSent()
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
		if (!num.testBit(0) )
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
			if ( isReadyForNext() && !send )
			{
				System.out.println("changing");
                                checks_lock.lock();
                                try
	                        {
					boolean isNumPrime = true;
					for(int i = 0 ; i < isDone.size() ; i++)
					{
						isNumPrime = isNumPrime && isPrime.get(i).booleanValue();
        	                        	isDone.set(i,Boolean.FALSE);
                                        	isPrime.set(i,Boolean.FALSE);
                                        	isSent.set(i,Boolean.FALSE);
					}
					if (isNumPrime)
					{
						primes.add(num);
						System.out.println(num);
					}
                                }
                                finally
                                {
                  	              checks_lock.unlock();
                                }

				num=num.add(BigInteger.ONE).add(BigInteger.ONE);
				send = true;
			}
			
			if ( isAllSent() )
			{
				System.out.println("All sent");
				send = false;
			}
                        try{
                        Thread.sleep(10);}//the smaller the sleep time the faster the server will run and the less times there will be error in what gets sent over
                        catch ( InterruptedException e ){
                        System.out.println("A socket got interrupted");}

		}
	}
}
