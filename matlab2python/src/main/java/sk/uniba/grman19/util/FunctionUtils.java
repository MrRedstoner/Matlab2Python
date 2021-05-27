package sk.uniba.grman19.util;

import java.util.function.Function;

public class FunctionUtils {
	public static final <A,B,C> Function<A,C> compose(Function<A,B> f0, Function<B,C> f1){
		return f1.compose(f0);
	}
}
