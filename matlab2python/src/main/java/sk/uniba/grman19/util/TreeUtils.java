package sk.uniba.grman19.util;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;
import org.stringtemplate.v4.ST;

//for pretty-dumping trees in short form
public class TreeUtils {
	private static final ST template() {
		return new ST("<rule_text>\n\t<child; separator=\"\n\">");
	}
	private static final ST literal(String text) {
		return new ST("<text>").add("text", text);
	}

	public static void dump(Parser parser, Tree tree) {
		System.out.println(process(parser.getRuleNames(),tree).render());
	}
	
	private static String getNodeText(Tree t, String[] ruleNames) {
		if ( t instanceof RuleContext ) {
			int ruleIndex = ((RuleContext)t).getRuleContext().getRuleIndex();
			String ruleName = ruleNames[ruleIndex];
			return ruleName;
		}
		else if ( t instanceof ErrorNode) {
			return t.toString();
		}
		else if ( t instanceof TerminalNode) {
			Token symbol = ((TerminalNode)t).getSymbol();
			if (symbol != null) {
				String s = symbol.getText();
				return s;
			}
		}
		return "<failure>";
	}

	private static ST process(String[] ruleNames, Tree t) {
		if(t.getChildCount()==0) {
			return literal(getNodeText(t, ruleNames));
		} else if(t.getChildCount()==1) {
			//pass-through rules
			return process(ruleNames,t.getChild(0));
		} else {
			ST out=template();
			out.add("rule_text", getNodeText(t, ruleNames));
			for(int i=0;i<t.getChildCount();i++) {
				out.add("child", process(ruleNames,t.getChild(i)));
			}
			return out;
		}
	}
}