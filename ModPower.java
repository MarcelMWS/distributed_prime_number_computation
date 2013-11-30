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

public ModPower(BigInteger a, BigInteger b, BigInteger m)//base, exponent, mod by
{
	this(a,b,m,BigInteger.ONE);
}
public ModPower(BigInteger a, BigInteger b, BigInteger m, BigInteger c) //base,exponent,modby,dummy set to one to when starting
{
	this.a = a;
	this.b = b;
	this.m = m;
	this.c = c;
}

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
//This is to test the Power class to make sure that everything is running correctly 10^10 should have 10 zeros to it
//accepts two string integers for calculation
	public static void main(String[] args) throws InterruptedException, ExecutionException
	{
		test(args);
	}
	public static void test(String[] args) throws InterruptedException, ExecutionException
	{
		BigInteger qwer = new BigInteger(args[0]);
		BigInteger rewq = new BigInteger(args[1]);
		BigInteger asdf = new BigInteger(args[2]);
		ModPower.setES(Executors.newCachedThreadPool());
		Callable<BigInteger> p = new ModPower(qwer,rewq,asdf);
		Future<BigInteger> f = es.submit(p);
		System.out.println(f.get());
		es.shutdown();
	}

}
