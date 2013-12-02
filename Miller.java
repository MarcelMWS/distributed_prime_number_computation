import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.math.BigInteger;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.lang.InterruptedException;
import java.util.concurrent.ExecutionException;

public class Miller implements Callable<Void>// the return will never be read by anything
{

	//this chunk of variables is used for each individual test
	private BigInteger n;
	private static Lock nlock = new ReentrantLock();//lock for reading the number into a temp variable--BigInteger doesn't say if it's a volatile read
	private BigInteger check;
	private static boolean readyToBeChanged=true;

	//this chunk is used to compile data for each test into one conclusive set of data
	private boolean prime=false;
	private boolean done=false;
	private static Lock palock = new ReentrantLock();//lock for writing to the primeArray

	//this chunk is used for all the tests--generic services
	private static Random r = new Random();
	private static ExecutorService es;


	public Miller()
	{
		this(new BigInteger("0",2));
	}
	public Miller(BigInteger q)
	{
		n=q;
	//	System.out.println("n: " + n + " n.bitLength(): " + n.bitLength());
                newCheck();
	//	System.out.println("check: " + check);
	}
	// Allows the user to set what ExecutorService is being used. Different applications require different needs.
	public static void setES(ExecutorService newES)
	{
		es=newES;
	}
	// Changes the number being checked--should only be used after isDone has been checked
	public void setNum(BigInteger q)
	{
		if(readyToBeChanged)
		{
			n=q;
		}
	}
	
	public void newCheck()
	{
                check = new BigInteger(Math.min(n.bitLength()-1,20),10,r);
		if(check.compareTo(new BigInteger("2"))<=0)
		{
			check = new BigInteger("3");
		}
	}	

	public boolean isPrime()
	{
		return prime;
	}
	
	public boolean isDone()
	{
		return done;
	}

	public void waitTillDone()
	{
		while(!isDone()){
		try{
		Thread.sleep(100);}
		catch (InterruptedException e){System.out.println(e);}		}
		readyToBeChanged=true;
		return;

	}

	public Void call() throws ExecutionException, InterruptedException //the callable will run in the background with multiple going at once. You have the option to get the value but that isn't cared about so a null void is sent back. << never going to be read
	{
		BigInteger num; // real number to be kept till end
		BigInteger pMinus1; // dummy number to be manipulated
		nlock.lock();
		try
		{
			readyToBeChanged=false;
			num = n; // real number
			pMinus1 = n; // dummy number
		}
		finally
		{
			nlock.unlock();
		}
		pMinus1=pMinus1.subtract(BigInteger.ONE);
		// finds larget power of 2 that'll divide n-1
		int a = pMinus1.getLowestSetBit();
	//	System.out.println("a=" + a);
		BigInteger q=pMinus1.divide(new BigInteger("2").shiftLeft(a-1));
	//	System.out.println("q=" + q);
		ModPower.setES(es);
	//	System.out.println("ES Transfered");
		Callable<BigInteger> mp = new ModPower(check,q,num);//(check^q) mod num
		Future<BigInteger> f = es.submit(mp);
	//	System.out.println("Future submitted"); 
		BigInteger z = f.get();
	//	System.out.println("z=="+z);
		if(z.equals(BigInteger.ONE)||z.equals(q))
		{
				prime = true;
				done = true;
			return null;
		}
	
		int i = 0;
		
		for(i=0;i<a;)
		{
	//		System.out.println("i: "+i + "  z: " + z);
			if(z.equals(BigInteger.ONE))
			{
					prime = false;
					done = true;
				return null;
			}
			i++;
			if(z.equals(pMinus1))
				break;
			try
			{
				mp = new ModPower(z,BigInteger.valueOf(2),num);// (z^2) mod num
				f = es.submit(mp);
				z = f.get();
	//			System.out.println("z==" + z);
			}
			catch (InterruptedException e)
			{
				System.out.println("FAILED CALCULATION--"+e);
				throw e;
			}
			catch (ExecutionException e)
			{
				System.out.println("FAILED CALCULATION--"+e);
				throw e;
			}
		}
	//	System.out.println("Out of for loop");
		if(i==a&&!z.equals(pMinus1))
		{
				prime = false;
				done = true;
				return null;
		}
		else
		{
				prime = true;
				done = true;
			return null;
		}
}

	//sample calculations provided in readme
	//there will also be a test case in the .sh file for Linux
	public static void main(String[] args) throws ExecutionException, InterruptedException
	{
		// demo of how to calculate Miller-Rabin checks on one machine
		BigInteger n1 = new BigInteger(args[0]);//prime check
		Miller.setES(Executors.newCachedThreadPool());
		Callable<Void> m1 = new Miller(n1);
		Future<Void> f = es.submit(m1);
		((Miller) m1).waitTillDone();
		System.out.println("is prime: " + ((Miller) m1).isPrime());
	}
}
