package javelin.misc;

/**
 * Create pairs of arbitrary classes (do not need to be the same)
 * @author Daniel
 *
 * @param <P1> class of the first element of the pair
 * @param <P2> class of the second element of the pair
 */
public class Pair <P1, P2> {

	private P1 p1;
	private P2 p2;
	
	/**
	 * Create a pair with elements p1 and p2
	 * @param p1
	 * @param p2
	 */
	public Pair (P1 p1, P2 p2) {
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * @return the first element of the pair
	 */
	public P1 first() {
		return this.p1;
	}
	
	/**
	 * Set the first element of this pair
	 * @param first
	 */
	public void setFirst(P1 first) {
		this.p1 = first;
	}
	
	/**
	 * @return the second element of the pair
	 */
	public P2 second() {
		return this.p2;
	}

	/**
	 * Set the second element of this pair
	 * @param second
	 */
	public void setSecond(P2 second) {
		this.p2 = second;
	}
}
