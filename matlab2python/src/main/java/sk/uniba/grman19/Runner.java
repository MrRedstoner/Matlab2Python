package sk.uniba.grman19;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Runner {
	public static void main(String[]args) {
		GrammarBaseListener gl=new GrammarBaseListener(){
			@Override public void enterEntry(GrammarParser.EntryContext ctx) {
				System.out.println("Enter entry "+ctx.getText());
			}
			
			@Override public void exitEntry(GrammarParser.EntryContext ctx) {
				System.out.println("Exit entry");
			}
		};
		
		String content="2018-May-05 14:20:18 INFO some error occurred\n" + 
				"2018-May-05 14:20:19 INFO yet another error\n" + 
				"2018-May-05 14:20:20 INFO some method started\n" + 
				"2018-May-05 14:20:21 DEBUG another method started\n" + 
				"2018-May-05 14:20:21 DEBUG entering awesome method\n" + 
				"2018-May-05 14:20:24 ERROR Bad thing happened\n";
		
		GrammarLexer lexer=new GrammarLexer(CharStreams.fromString(content));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		GrammarParser parser=new GrammarParser(tokens);
		
		ParseTree tree = parser.log();
		
		ParseTreeWalker walker = new ParseTreeWalker();
		
		walker.walk(gl, tree);
	}
}
