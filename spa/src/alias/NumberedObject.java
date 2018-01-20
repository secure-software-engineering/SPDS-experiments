package alias;

public class NumberedObject {
	private static long uniqueId = 0;

	protected long id;
	
	protected NumberedObject() {
		this.id = uniqueId++;
	}
	
	public static void reset() {
//		System.out.println("[vA9afASp] " + uniqueId);
		uniqueId = 0;
	}
	
	public long getId() {
		return id;
	}
}
