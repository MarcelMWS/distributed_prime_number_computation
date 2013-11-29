// Author: Kenneth Chaney
// Acts as a multi-threaded recursive solution to take the power of a BigInteger.

import java.math.BigInteger;
import  java.util.concurrent.*;
import java.util.concurrent.Executors;

public class Power implements Callable<BigInteger>
{


// this class satisfies n^m these are read only variables except when the object is made
private static BigInteger n;// base
private static BigInteger m;// exponent

private static ExecutorService es  = Executors.newCachedThreadPool();

public Power(BigInteger a, BigInteger b)//base, exponent
{
	n=a;
	m=b;
}

public BigInteger call() throws InterruptedException, ExecutionException
{
	BigInteger a;
	if(m.equals(BigInteger.ZERO)==true)//if we reach a point where the exponent is 0 x^0=1
	{
		return BigInteger.ONE;
	}
	else if(m.testBit(0)==false) // taking advantage of the fact that x^2b= x^b * x^b
	{
		Callable<BigInteger> p = new Power(n,m.shiftRight(1));
		Future<BigInteger> f = es.submit(p);
		a=f.get();
		return a.multiply(a);
	}
	else // multiplies by the base
	{
		Callable<BigInteger> p = new Power(n,m.subtract(BigInteger.ONE));
		Future<BigInteger> f = es.submit(p);
		a=f.get();
		return n.multiply(a);
	}
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
		Callable<BigInteger> p = new Power(qwer,rewq);
		Future<BigInteger> f = es.submit(p);
		System.out.println(f.get());
		es.shutdown();
	}

}
