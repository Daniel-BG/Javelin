package javelin.misc;

/**
 * @author Daniel
 * various math operations to help us along
 */
public class MathOperations {
	
	
	/**
	 * @param absoluteMaxValue
	 * @param d
	 * @return the maximum distance between two samples existing in a "d"-dimensional space,
	 * bounded from (0,...,0) to (absoluteMaxValue,...,absoluteMaxValue)
	 */
	public static float getMaximumDistance(float absoluteMaxValue, float d) {
		float valueIncrement = (float) Math.sqrt(d);
		return absoluteMaxValue * valueIncrement;
	}

	
	
	/**
	 * @param val
	 * @param base
	 * @return the logarithm of "val" in base "base"
	 */
	public static float logBase(float val, float base) {
		return (float) (Math.log10(val) / Math.log10(base));
	}
	
	
	/**
	 * from <a href="https://stackoverflow.com/questions/8071363/calculating-powers-in-java">here</a>
	 * @param a
	 * @param b
	 * @return a^b
	 * 
	 */
	public static int pow (int a, int b) {
	    if ( b == 0)        return 1;
	    if ( b == 1)        return a;
	    if ( b % 2 == 0)    return     pow ( a * a, b/2); //even a=(a^2)^b/2
	    else                return a * pow ( a * a, b/2); //odd  a=a*(a^2)^b/2
	}


	/**
	 * Clamp data to the specified interval (both inclusve)
	 * @param data
	 * @param l
	 * @param m
	 * @return
	 */
	public static long clamp(long data, long lowerLimit, long upperLimit) {
		return data < lowerLimit ? lowerLimit : data > upperLimit ? upperLimit : data;
	}
}
