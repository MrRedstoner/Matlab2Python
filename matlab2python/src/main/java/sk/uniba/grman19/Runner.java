package sk.uniba.grman19;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class Runner {

	private static STGroup templates = new STGroupFile("Log2.stg");
	
	static ST template(String name) {
	    return templates.getInstanceOf(name);
	}
	
	public static void main(String[]args) {
		GrammarVisitor<ST> visitor=new GrammarBaseVisitor<ST>() {
			@Override
			public ST visitMessage(GrammarParser.MessageContext ctx) {
				return template("literal").add("text",ctx.getText());
			}
			
			@Override
			public ST visitTimestamp(GrammarParser.TimestampContext ctx) {
				return template("literal").add("text",ctx.getText());
			}
			
			@Override
			public ST visitEntry(GrammarParser.EntryContext ctx) {
				ST entry=template("entry");
				
				entry.add("timestamp", visit(ctx.timestamp()));
				entry.add("message", visit(ctx.message()));
				
				return entry;
			}
			
			@Override
			public ST visitLog(GrammarParser.LogContext ctx) {
				ST log=template("log2");
				
				for(GrammarParser.EntryContext entry:ctx.entry()) {
					log.add("entries", visit(entry));
				}
				
				return log;
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
		
		System.out.println(visitor.visit(tree).render());
	}
}
