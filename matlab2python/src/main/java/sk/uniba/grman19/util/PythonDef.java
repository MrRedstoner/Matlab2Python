package sk.uniba.grman19.util;

public enum PythonDef {
	FUNC2STR("def func2str(f):\n"
			+ "    return inspect.getsource(f)"),
	PRINTF("def printf(format_str, *args):\n" + 
			"    args = tuple(itertools.chain.from_iterable(\n" + 
			"        arg if isinstance(arg, np.ndarray) else [arg] for arg in args))\n" + 
			"    print(format_str % args, end='')"),
	SIZE("def size(np_array, dimen=None):\n" + 
			"    if dimen is None:\n" +
			"        return np_array.shape\n" +
			"    else:\n" +
			"        return np_array.shape[dimen-1]"),//-1 to correct for different indexing
	SURFC("def surfc(a, b, c):\n" + 
			"    plt.figure().gca(projection=\"3d\").plot_surface(a, b, c)"),
	PLOT("def plot(*args):\n" + 
			"    plt.plot(*args)\n" + 
			"    plt.draw()"),
	FPLOT("def fplot(f, r):\n" + 
			"    linspace = np.linspace(*r, 100)\n" + 
			"    plot(linspace, f(linspace))"),
	ARRAY("def array(arg):\n" + 
			"    if isinstance(arg[0], np.ndarray):\n" + 
			"        return np.column_stack(arg)\n" + 
			"    else:\n" + 
			"        return np.array(arg)");
	
	private final String representation;
	
	private PythonDef(String value) {
		representation=value;
	}
	
	@Override
	public String toString() {
		return representation;
	}
}
