package dgen;

import javafx.beans.property.SimpleStringProperty;

public class TestDataTreeStructure {
	
	private final SimpleStringProperty node;
	private final SimpleStringProperty nodeType; 
	
	public TestDataTreeStructure(String node, String nodeType) {
		// TODO Auto-generated constructor stub
		this.node = new SimpleStringProperty(node);
		this.nodeType = new SimpleStringProperty(nodeType);
		
	}

	public String getNode() {
		return this.node.get();
	}
	
	public void setNode(String node) {
		this.node.set(node);
	}
	
	public String getNodeType() {
		return this.nodeType.get();
	}
	
	public void setNodeType(String nodeType) {
		this.nodeType.set(nodeType);
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.node.get();
	}

}
