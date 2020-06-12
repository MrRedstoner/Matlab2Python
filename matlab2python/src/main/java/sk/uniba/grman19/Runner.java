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
		
		String content="a = 10\n"
				+ "b = 5\n";

		MatlabLexer lexer=new MatlabLexer(CharStreams.fromString(content));

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		MatlabParser parser=new MatlabParser(tokens);

		ParseTree tree = parser.translation_unit();
		
		System.out.println(ptv.visit(tree).getFullTranslation(templates.getInstanceOf("fullTranslation")));
	}
}
