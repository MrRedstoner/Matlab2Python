package sk.uniba.grman19.util;

public enum PythonDef {
	FUNC2STR("def func2str(f)\n"
			+ "    return inspect.getsource(f)\n"),
	PRINTF("def printf(format_str, *args):\n" + 
			"    print(format_str % args)\n"),
	SIZE("def size(np_array, dimen=None):\n" + 
			"    if dimen is None:\n" +
			"        return np_array.shape\n" +
			"    else:\n" +
			"        return np_array.shape[dimen-1]\n"),//-1 to correct for different indexing
	SURFC("def surfc(a, b, c):\n" + 
			"    plt.figure().gca(projection=\"3d\").plot_surface(a, b, c)"),
	FPLOT("def fplot(f, r):\n" + 
			"    linspace = np.linspace(*r, 100)\n" + 
			"    plt.plot(linspace, np.vectorize(f)(linspace))\n" + 
			"    plt.show()");
	
	private final String representation;
	
	private PythonDef(String value) {
		representation=value;
	}
	
	@Override
	public String toString() {
		return representation;
	}
}
