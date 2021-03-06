package sk.uniba.grman19;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

public class GrammarTest {
	@Test
	public void testSimpleProgram() {
		//parse as translation_unit
		String content=
				"a=[0...\n" +
				"1];\n" +
				"[f(0) 1]\n"+
				"for i = 1:10\n" +
				"a = a+i;\n" +
				"end\n" +
				"fprintf('''a'' je %d', a);\n" +
				"z= x' + y'\n" +
				"if d(a)>0 return; end\n" +
				"rand();\n";
		
		MatlabLexer lexer=new MatlabLexer(CharStreams.fromString(content));
		
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		
		MatlabParser parser=new MatlabParser(tokens);
		parser.setBuildParseTree(true);
		
		parser.translation_unit().getText();
		
		assertEquals(0,parser.getNumberOfSyntaxErrors());
	}
}
