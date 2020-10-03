package dgen;

import javafx.beans.property.SimpleStringProperty;

public class EnvironmentSetup {
	private final SimpleStringProperty environKey;
	private final SimpleStringProperty environValue;
	
	public EnvironmentSetup(String key, String value) {
		// TODO Auto-generated constructor stub
		this.environKey = new SimpleStringProperty(key);
		this.environValue = new SimpleStringProperty(value);
	}

	public String getEnvironKey() {
		return this.environKey.get();
	}
	
	public void setEnvironKey(String key) {
		this.environKey.set(key);
	}
	
	public String getEnvironValue() {
		return this.environValue.get();
	}
	
	public void setEnvironValue(String value) {
		this.environValue.set(value);
	}
	
}
