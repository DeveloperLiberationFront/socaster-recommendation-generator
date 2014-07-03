package ca.ubc.cs.commandrecommender.model.cf.matejka;

public class MatejkaOptions {
	
	public boolean calculateJustWithOverlap;
	public boolean inferMissingValues;
	public double alpha;

	public MatejkaOptions(boolean calculateJustWithOverlap,
			boolean inferMissingValues, double alpha) {
		
		super();
		this.calculateJustWithOverlap = calculateJustWithOverlap;
		this.inferMissingValues = inferMissingValues;
		this.alpha = alpha;
	}
	
	public String toString(){
		return "alpha="+alpha+", inference="+inferMissingValues+", overlap="+calculateJustWithOverlap;
	}
}
