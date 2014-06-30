package ca.ubc.cs.commandrecommender.model.cf;


public interface ItemFactory{

	Long toolForToolID(Long itemID);

	long[] tools();
	
}