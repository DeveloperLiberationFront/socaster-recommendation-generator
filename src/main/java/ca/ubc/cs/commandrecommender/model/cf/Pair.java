package ca.ubc.cs.commandrecommender.model.cf;
/**
 * Used to represent antecedent consequent pair
 * where left is the antecedent and right is the consequent.
 * (Note: Slightly modifed from SPMF)
 * 
 * @author emerson
 *
 */
public class Pair {

    private final Integer left;
    private final Integer right;

    public Pair(final Integer left, final Integer right) {
        this.left = left;
        this.right = right;
    }

    public Integer getRight() {
        return right;
    }

    public Integer getLeft() {
        return left;
    }

    public int compareTo(Pair other) {
		int firstC = this.left.compareTo(other.left);
		return firstC==0 ?
			this.right.compareTo(other.right):
			firstC;
	}

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Pair))
            return false;

        final Pair other = (Pair) o;
        return equal(getLeft(), other.getLeft()) && equal(getRight(), other.getRight());
    }

    private static final boolean equal(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        }
        return o1.equals(o2);
    }

    @Override
    public int hashCode() {
        int hLeft = getLeft() == null ? 0 : getLeft().hashCode();
        int hRight = getRight() == null ? 0 : getRight().hashCode();

        return hLeft + (57 * hRight);
    }

    @Override
    public String toString(){
    	return "("+ left +","+ right +")";
    }
}


