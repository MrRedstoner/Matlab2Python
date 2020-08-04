package sk.uniba.grman19.util;

public enum PythonImport {
	NUMPY("import numpy as np"),
	PYPLOT("import matplotlib.pyplot as plt"),
	INSPECT("import inspect"),
	SQRT("from math import sqrt"),
	RANDOM("import random"),
	ITERTOOLS("import itertools"),
	AXES3D("from mpl_toolkits.mplot3d import Axes3D");

	private final String representation;
	
	private PythonImport(String value) {
		representation=value;
	}
	
	@Override
	public String toString() {
		return representation;
	}
}
