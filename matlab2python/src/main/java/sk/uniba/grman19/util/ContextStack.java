package sk.uniba.grman19.util;

import java.util.Stack;
import java.util.function.Supplier;

import org.antlr.v4.runtime.ParserRuleContext;

import sk.uniba.grman19.MatlabVisitor;

public class ContextStack {
	Stack<Boolean> stack=new Stack<Boolean>();
	private void set(boolean is) {
		stack.add(is);
	}
	private void clear() {
		stack.pop();
	}
	private boolean check() {
		if(stack.isEmpty()) {
			return false;
		} else {
			return stack.peek();
		}
	}
	private synchronized <T> T setAndVisit(boolean value, ParserRuleContext ctx, MatlabVisitor<T>visitor) {
		try {
			set(value);
			return ctx.accept(visitor);
		} finally {
			clear();
		}
	}
	private synchronized <T> T setAndVisit(boolean value, Supplier<T>visitor) {
		try {
			set(value);
			return visitor.get();
		} finally {
			clear();
		}
	}
	
	public static class LhsContextStack extends ContextStack{
		public <T> Supplier<T> visitAsLhs(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
			return ()->super.setAndVisit(true, ctx, visitor);
		}
		public <T> Supplier<T> visitAsNonLhs(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
			return ()->super.setAndVisit(false, ctx, visitor);
		}
		public <T> Supplier<T> visitAsLhs(Supplier<T>visitor) {
			return ()->super.setAndVisit(true, visitor);
		}
		public <T> Supplier<T> visitAsNonLhs(Supplier<T>visitor) {
			return ()->super.setAndVisit(false, visitor);
		}
		public boolean isLhs() {
			return super.check();
		}
	}
	
	public static class IndexingContextStack extends ContextStack{
		public <T> Supplier<T> visitAsIndex(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
			return ()->super.setAndVisit(true, ctx, visitor);
		}
		public <T> Supplier<T> visitAsNonIndex(ParserRuleContext ctx, MatlabVisitor<T>visitor) {
			return ()->super.setAndVisit(false, ctx, visitor);
		}
		public <T> Supplier<T> visitAsIndex(Supplier<T>visitor) {
			return ()->super.setAndVisit(true, visitor);
		}
		public <T> Supplier<T> visitAsNonIndex(Supplier<T>visitor) {
			return ()->super.setAndVisit(false, visitor);
		}
		public boolean isIndexing() {
			return super.check();
		}
	}
}
