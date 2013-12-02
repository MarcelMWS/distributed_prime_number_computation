import java.io.*;
import java.net.*;
import java.util.*;
import java.math.BigInteger;
import java.util.concurrent.*;

public class Client
{
	private static Socket s;
	private static ObjectOutputStream out;
	private static ObjectInputStream in;
	private static boolean send;
	private static boolean recieve;
	private static ExecutorService es = Executors.newCachedThreadPool();

	public static void sendMessage(String msg)
	{
		try{
			out.writeObject(msg);
			out.flush();
			System.out.println("client>" + msg);
		}
		catch(IOException ioException){
			ioException.printStackTrace();}
	}
	public static BigInteger recieveNumber()
	{
		try
		{
			return (BigInteger) in.readObject();
		}
		catch(Exception e)
		{
			System.out.println("Bad Object Type");
			System.out.println(e);
			return null;
		}
	}
	public static String recieveMessage()
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
	public static boolean recieveAndCheckMessage(String equ)
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

	public static void main( String[] args )
	{

	try
	{
		s = new Socket(args[0],Integer.parseInt(args[1]));
	}
	catch (Exception e)
	{
		System.out.println("Couldn't create socket--check inputs");
		System.out.println(e);
		return;
	}

	try
	{
		out = new ObjectOutputStream( s.getOutputStream() );
		in = new ObjectInputStream( s.getInputStream() );
	}
	catch ( IOException e )
	{
		System.out.println("Couldn't make network IO");
		return;
	}

	if (recieveAndCheckMessage("connected"))
	{
		sendMessage("connected");
		System.out.println("Connected");
	}

	System.out.println("Connected to:  " + s.getLocalAddress() + ":" + s.getLocalPort());
	send=false;
	recieve=true;
        Miller.setES(es);
	Callable<Void> m1=new Miller(new BigInteger("5"));
	Future<Void> f;
	while (true)
	{
		if(recieve)
		{
			BigInteger num = recieveNumber();
			System.out.println("Number Recieved "+num);
			m1= new Miller(num);
			recieve=false;
			send=true;
			try
			{
                        	f = es.submit(m1);
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
		}
		if(send)
		{
			((Miller)m1).waitTillDone();
			System.out.println(((Miller)m1).isPrime());
			sendMessage(Boolean.toString(((Miller)m1).isPrime()));
			recieve=true;
			send=false;
		}
                        try{
                        Thread.sleep(100);}
                        catch ( InterruptedException e ){
                        System.out.println("A socket got interrupted");}
	}

	}
}
