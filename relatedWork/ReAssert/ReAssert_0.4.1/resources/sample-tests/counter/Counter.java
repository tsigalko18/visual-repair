package sample.counter;


public class Counter {

	private int init;
	private int value;
	
	public Counter(int init) {
		this.init = init;
		reset();
	}

	public void reset() {
		value = init;
	}
	
	public void increment() {
		value = value + 2;
	}
	
	public int getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Counter)) {
			return false;
		}
		Counter c = (Counter)obj;
		if (this.getValue() != c.getValue()) return false;
		return true;
	}
}
