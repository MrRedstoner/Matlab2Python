package sk.uniba.grman19.util;

public enum PythonDef {
	FUNC2STR("def func2str(f):\n"
			+ "    return inspect.getsource(f)"),
	PRINTF("def printf(format_str, *args):\n" + 
			"    args = tuple(itertools.chain.from_iterable(\n" + //auto-unpack numpy arrays
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
	//fplot calls f with shape (len,1)
	FPLOT("def fplot(f, r):\n" + 
			"    ls = np.linspace(*r, 100)[...,np.newaxis]\n" + 
			"    plot(ls, f(ls))"),
	//ezplot calls f with shape (1,len)
	EZPLOT("def ezplot(f, r):\n" + 
			"    ls = np.linspace(*r, 100)\n" + 
			"    plot(ls, f(ls[np.newaxis,...])[0,...])"),
	ZEROS("def zeros(*args):\n" + 
			"    if len(args) == 1:\n" + //already a tuple
			"        return np.zeros(*args)\n" + 
			"    else:\n" + 
			"        return np.zeros(args)"),
	M_SUM("def m_sum(np_array):\n" + 
			"    try:\n" + 
			"        axis = next(filter(lambda e: e[1] != 1, enumerate(np_array.shape)))[0]\n" + 
			"        return np.sum(np_array, axis=axis, keepdims=True)\n" + 
			"    except StopIteration:\n" + 
			"        return np_array"),//all dimensions are 1, just return
	ARRAY("def array(arg):\n" + 
			"    if isinstance(arg[0], np.ndarray):\n" + 
			"        return np.column_stack(arg)\n" + 
			"    else:\n" + 
			"        return np.array(arg)");
	
	private final String representation;
	
	private PythonDef(String value) {
		representation=value;
	}
	
	public String getRepresentation() {
		return representation;
	}
}
