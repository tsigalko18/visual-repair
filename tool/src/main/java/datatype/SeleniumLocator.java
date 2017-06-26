package main.java.datatype;

public class SeleniumLocator {

//	Pair<String, String> pair;
//	
//	public SeleniumLocator(String strategy, String value){
//		pair = new MutablePair<String, String>(strategy, value);
//	}
//
//	@Override
//	public String toString() {
//		return "By." + pair.getLeft() + "(\"" + pair.getRight() + "\")";
//	}
//
//	public Pair<String, String> getPair() {
//		return pair;
//	}
//
//	public void setPair(Pair<String, String> pair) {
//		this.pair = pair;
//	}
	
	String strategy, value;

	public SeleniumLocator(String strategy, String value) {
		this.strategy = strategy;
		this.value = value;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "By." + strategy + "(\"" + value + "\")";
	}
	
	
}
