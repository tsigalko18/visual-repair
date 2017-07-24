package datatype;

import java.util.LinkedHashMap;
import java.util.Map;

public class EnhancedTestCase {

	String name;
	Map<Integer, Statement> statements;
	String path;
	
	public EnhancedTestCase(String testName, Map<Integer, Statement> statements, String path) {
		setName(testName);
		this.statements = statements;
		setPath(path);
	}
	
	public EnhancedTestCase(String testcasename, String path) {
		setName(testcasename);
		this.statements = new LinkedHashMap<Integer, Statement>();
		setPath(path);
	}
	
	public EnhancedTestCase(String testcasename) {
		setName(testcasename);
		this.statements = new LinkedHashMap<Integer, Statement>();
		setPath(null);
	}

	public String getName() { return name; }
	public Map<Integer, Statement> getStatements() { return statements; }
	
	public void setName(String name) { this.name = name; }
	public void setStatements(LinkedHashMap<Integer, Statement> statements) { this.statements = statements; }
	
	public void addStatement(Integer i, Statement st){ statements.put(i, st); }
	
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }

	@Override
	public String toString() {
		return "TestCase [name=" + name + ", statements=" + statements + "]";
	}
	
}
