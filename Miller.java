import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.math.*;

public class Miller implements Callable<Boolean>
{
	private static BigInteger n;
	private static Lock nlock = new ReentrantLock();
	
	public Miller()
	{
		n = new BigInteger("0",2);
	}
	public Miller(BigInteger q)
	{
		n=q;
	}
	public static void setNum(BigInteger q)
	{
		n=q;
	}
		
	public Boolean call() throws InterruptedException
	{
		boolean prime = false;
		BigInteger m; // real number to be kept till end
		BigInteger q; // dummy number to be manipulated
		nlock.lock();
		try
		{
			m = n; // real number
			q = n; // dummy number
		}
		finally
		{
			nlock.unlock();
		}
		q=q.subtract(BigInteger.ONE);
		// finds larget power of 2 that'll divide n-1
		boolean found=false;
		int a = 0;
		while(!found)
		{	
			found=q.testBit(0);
			q=q.shiftRight(1);
			a++;
		}


		return prime;
	}


	public static void main(String[] args)
	{
	}

}
