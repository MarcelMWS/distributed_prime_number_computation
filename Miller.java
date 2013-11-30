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

	//this chunk is used to compile data for each test into one conclusive set of data
	private static int nextSpotInArray=0;
	private int thisSpotInArray;
	private static List<Boolean> primeArray=new ArrayList<Boolean>();
	private static List<Boolean> threadDone=new ArrayList<Boolean>();
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
		thisSpotInArray=nextSpotInArray;
		nextSpotInArray++;
		primeArray.add(new Boolean(false));
		threadDone.add(new Boolean(false));
	}
	
	public static void setES(ExecutorService newES)
	{
		es=newES;
	}

	public void setNum(BigInteger q)
	{
		n=q;
	}
	
	public void newCheck()
	{
                check = new BigInteger(n.bitLength()-1,r);
		if(check.compareTo(new BigInteger("2"))<=0)
		{
			check = new BigInteger("3");
		}
	}	

	public static boolean isPrime()
	{
		boolean prime = true;
		for ( int i = 0; i < primeArray.size(); i++)
		{
	//		System.out.println(primeArray.get(i).booleanValue());
			prime = (prime && primeArray.get(i).booleanValue());
		}
		return prime;
	}
	public static boolean isDone()
	{
		boolean done = true;
		for ( int i = 0 ; i < threadDone.size() ; i++)
		{
			done = (done && threadDone.get(i).booleanValue());
		}
		return done;
	}
	public Void call() throws ExecutionException, InterruptedException //the callable will run in the background with multiple going at once. You have the option to get the value but that isn't cared about so a null void is sent back. << never going to be read
	{
		BigInteger num; // real number to be kept till end
		BigInteger pMinus1; // dummy number to be manipulated
		nlock.lock();
		try
		{
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
			palock.lock();
	//		System.out.println("PA Lock");
			try
			{
				primeArray.set(thisSpotInArray, new Boolean(true));
				threadDone.set(thisSpotInArray,new Boolean(true));
			}
			finally
			{
				palock.unlock();
	//			System.out.println("PA Unlock");
			}
			return null;
		}
	
		int i = 0;
		
		for(i=0;i<a;)
		{
	//		System.out.println("i: "+i + "  z: " + z);
			if(z.equals(BigInteger.ONE))
			{
				palock.lock();
				try
				{
	//				System.out.println("z equaled one");
					primeArray.set(thisSpotInArray, new Boolean(false));
					threadDone.set(thisSpotInArray,new Boolean(true));
				}
				finally
				{
					palock.unlock();
				}
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
			palock.lock();
	//		System.out.println("Lock for i==a and z!=pMinus1");
			try
			{
	//			System.out.println( "i=="+i+ " : z==" + z);
				primeArray.set(thisSpotInArray,new Boolean(false));
				threadDone.set(thisSpotInArray,new Boolean(true));
			}
			finally
			{
				palock.unlock();
			}
			return null;
		}
		else
		{
			palock.lock();
	//		System.out.println("Lock for else");
			try
			{
				primeArray.set(thisSpotInArray,new Boolean(true));
				threadDone.set(thisSpotInArray,new Boolean(true));
			}
			finally
			{
				palock.unlock();
	//			System.out.println("Unlocked from else");
			}
			return null;
		}
	}
//this is a test that runs through 100 times for a given prime number--100 times gives a decent certainty as to if a number is prime of not
	public static void main(String[] args) throws ExecutionException, InterruptedException
	{
		BigInteger n1 = new BigInteger(args[0]);//prime check
		BigInteger n2 = new BigInteger(args[1]);//number of trials
		Miller.setES(Executors.newCachedThreadPool());
		for ( int i = 0 ; i < n2.intValue() ; i++)
		{
			Callable<Void> m1 = new Miller(n1);
			Future<Void> f = es.submit(m1);
		}
		boolean wait=true;
		while(wait){
			wait=!isDone();
		}
		System.out.println("is prime: " + isPrime());
	}

}
