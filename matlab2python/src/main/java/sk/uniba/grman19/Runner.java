package sk.uniba.grman19;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

public class Runner {
	public static void main(String[]args) {
		STGroup templates = new STGroupFile("Python.stg");
		
		PythonTranslatorVisitor ptv=new PythonTranslatorVisitor(templates);
		
		String content="d = + 10 + 5 + 6 +7\n"
				+ "b = (3 + (5 - 1)) * -2\n"
				+ "c = 'An ''\"apostrophe\"'' string.'\n"
				+ "for i=2:20\n"
				+ "    d =d+sqrt(i);;\n"
				+ "end\n"
				+ "pause(.5)\n"
				+ "fprintf('''d'' je %d', d)\n"
				+ "[X,Y] = meshgrid(a,b)\n"
				+ "if b>= c\n"
				+ "    while( d>10 )\n"
				+ "        d = d - 5\n"
				+ "    end\n"
				+ "else\n"
				+ "    ;"
				+ "end\n";

		MatlabLexer lexer=new MatlabLexer(CharStreams.fromString(content));

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		MatlabParser parser=new MatlabParser(tokens);

		ParseTree tree = parser.translation_unit();
		
		System.out.println(ptv.visit(tree).getFullTranslation(templates.getInstanceOf("fullTranslation")));
	}
}
