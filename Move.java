

public class Move {
	
	public static final String FORWARD = "forward";
	public static final String TURN = "turn";
	
	String type;
	int size;
	
	public Move(String type, int size){
		this.type = type;
		this.size = size;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}
	

}
