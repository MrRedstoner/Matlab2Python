package sk.uniba.grman19.util;

public enum PythonImport {
	NUMPY("import numpy as np"),
	PYPLOT("import matplotlib.pyplot as plt");

	private final String representation;
	
	private PythonImport(String value) {
		representation=value;
	}
	
	@Override
	public String toString() {
		return representation;
	}
}
