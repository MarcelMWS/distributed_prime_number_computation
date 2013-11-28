import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import java.math.*;

public class Miller implements Callable
{
	public static BigInteger n;
	public static Lock nlock = new ReentrantLock();
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
		BigInteger m;
		nlock.lock();
		try
		{
			m = n;
		}
		finally
		{
			nlock.unlock();
		}
		
		return prime;
	}


	public static void main(String[] args)
	{
	}

}
