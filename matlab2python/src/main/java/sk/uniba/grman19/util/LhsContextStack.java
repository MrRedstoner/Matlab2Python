package sk.uniba.grman19.util;

import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;

import sk.uniba.grman19.MatlabVisitor;

public class LhsContextStack {
	Stack<Boolean> stack=new Stack<Boolean>();
	public boolean isLhs() {
		if(stack.isEmpty()) {
			return false;
		} else {
			return stack.peek();
		}
	}
	private void setLhs(boolean is) {
		stack.add(is);
	}
	private void clear() {
		stack.pop();
	}
	
	public synchronized <T> T visitAsLhs(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
		try {
			setLhs(true);
			return ctx.accept(visitor);
		} finally {
			clear();
		}
	}
	
	public synchronized <T> T visitAsNonLhs(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
		try {
			setLhs(false);
			return ctx.accept(visitor);
		} finally {
			clear();
		}
	}
}
