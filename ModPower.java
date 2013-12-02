// Author: Kenneth Chaney
// Acts as a multi-threaded recursive solution to take the power of a BigInteger. Theoretically the run time should be around O(log(n))

import java.math.BigInteger;
import  java.util.concurrent.*;
import java.util.concurrent.Executors;

public class ModPower implements Callable<BigInteger>
{

	// this class satisfies n^m these are read only variables except when the object is made
	private BigInteger a;// base
	private BigInteger b;// exponent
	private BigInteger m;// what we will mod by
	private BigInteger c;

	private static ExecutorService es;
	
	public ModPower(BigInteger a, BigInteger b, BigInteger m)//base, exponent, mod by--the starting constructor--sets the dummy to one
	{
		this(a,b,m,BigInteger.ONE);
	}
	public ModPower(BigInteger a, BigInteger b, BigInteger m, BigInteger c) //base,exponent,modby,dummy
	{
		this.a = a;
		this.b = b;
		this.m = m;
		this.c = c;
	}
	// Allows the user to set what ExecutorService is being used. Different applications require different needs.
	public static void setES(ExecutorService newES)
	{
		es = newES;
	}
	
	public BigInteger call() throws InterruptedException, ExecutionException
	{
		if(b.equals(BigInteger.ZERO))
		{
			return c;
		}
		if (b.testBit(0))
		{
			c = c.multiply(a).mod(m);
		}
		else
		{
			c = c;
		}
		b = b.shiftRight(1);
		a = a.multiply(a).mod(m);
		Callable<BigInteger> mp = new ModPower(a,b,m,c);
		Future<BigInteger> f = es.submit(mp);
		return f.get();
	
	}
	//This is to test the modPower class to make sure that everything is running correctly
	//accepts three string integers for calculation
	//sample calculation is provided in readme
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		BigInteger base = new BigInteger(args[0]);
		BigInteger exponent = new BigInteger(args[1]);
		BigInteger mod = new BigInteger(args[2]);
		ModPower.setES(Executors.newCachedThreadPool());
		Callable<BigInteger> p = new ModPower(base,exponent,mod);
		Future<BigInteger> f = es.submit(p);
		System.out.println(f.get());
		es.shutdown();
	}

}
