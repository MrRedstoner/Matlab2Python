package sk.uniba.grman19;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import sk.uniba.grman19.util.Fragment;
import sk.uniba.grman19.util.PythonDef;
import sk.uniba.grman19.util.PythonImport;

public class TranslatorVisitorTest {
	private static PythonTranslatorVisitor ptv;

	@BeforeAll
	public static void createVisitor() {
		STGroup templates = new STGroupFile("Python.stg");
		ptv=new PythonTranslatorVisitor(templates);
	}

	private static class TestST extends ST{
		private static final Map<String,PythonDef>defMap;
		private static final Map<String,PythonImport>importMap;

		static {
			defMap=Collections.unmodifiableMap(Arrays.stream(PythonDef.values()).collect(Collectors.toMap(def->def.toString(), def->def)));
			importMap=Collections.unmodifiableMap(Arrays.stream(PythonImport.values()).collect(Collectors.toMap(def->def.toString(), def->def)));
		}

		private final EnumSet<PythonDef>defs;
		private final EnumSet<PythonImport>imports;

		public TestST() {
			super("<translation_unit>");
			defs=EnumSet.noneOf(PythonDef.class);
			imports=EnumSet.noneOf(PythonImport.class);
		}

		@Override
		public ST add(String name, Object value) {
			if("imports".equals(name)) {
				assertTrue(value instanceof String);
				assertTrue(importMap.containsKey(value));
				imports.add(importMap.get(value));
			}

			if("defs".equals(name)) {
				assertTrue(value instanceof String);
				assertTrue(defMap.containsKey(value));
				defs.add(defMap.get(value));
			}

			//returns this
			return super.add(name, value);
		}
	}

	private static String program(boolean endNewLine,String...lines) {
		//suffix by a newline
		return Arrays.stream(lines).collect(Collectors.joining("\n","",endNewLine?"\n":""));
	}

	/**@return true for success*/
	private static String translate(ST template, CharStream input) {
		MatlabParser parser;
		MatlabLexer lexer=new MatlabLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new MatlabParser(tokens);
		ParseTree tree = parser.translation_unit();

		Fragment result=ptv.visit(tree);

		assertTrue(parser.getNumberOfSyntaxErrors()==0 && !result.hadError());

		return result.getFullTranslation(template);
	}
	
	private static void check(String input, String output, EnumSet<PythonDef> defs, EnumSet<PythonImport> imports) {
		TestST st=new TestST();
		String translation=translate(st,CharStreams.fromString(input));
		assertEquals(output,translation);
		assertEquals(defs,st.defs);
		assertEquals(imports,st.imports);
	}
	
	@Test
	public void testEmptyProgram() {
		String input=program(true,
				";");
		String output=program(false,
				"pass");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testComment() {
		String input=program(true,
				"% this is a comment",
				"i=0",
				"%this is another comment",
				"i=1;",
				"%   this is a comment spaced right");
		String output=program(false,
				"# this is a comment",
				"i = 0",
				"# this is another comment",
				"i = 1",
				"#   this is a comment spaced right");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testTrickyStringLiterals() {
		//the literal used: A 'crazy string'
		String input=program(true,
				"a='A ''crazy string'''");
		String output=program(false,
				"a = 'A \\'crazy string\\''");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}

	@Test
	public void testIfFormDirect() {
		String input=program(true,
				"if a>b",
				"	;",
				"end");
		String output=program(false,
				"if a > b:",
				"    pass");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testIfWithoutNewline() {
		String input=program(true,
				"if a>b break;end");
		String output=program(false,
				"if a > b:",
				"    break");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testIfFormFuncCall() {
		String input=program(true,
				"if abs(b)>c",
				"	;",
				"end");
		String output=program(false,
				"if abs(b) > c:",
				"    pass");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testPlotting() {
		String input=program(true,
				"f=linspace(0,4,100);",
				"hold on",
				"plot(f,f);",
				"pause(2);",
				"plot(2,2,'ro');",
				"pause(5);");
		String output=program(false,
				"f = np.linspace(0, 4, 100)",
				"# hold on",
				"plot(f, f)",
				"plt.pause(2)",
				"plot(2, 2, 'ro')",
				"plt.pause(5)");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.PLOT);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY,PythonImport.PYPLOT);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testTuppleAssignment() {
		String input=program(true,
				"[X,Y]=abs();");
		String output=program(false,
				"X, Y = abs()");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testInCodeLists() {
		String input=program(true,
				"a=[1 2 3 4]");
		String output=program(false,
				"a = array([1, 2, 3, 4])");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testFunctionCalls() {
		String input=program(true,
				"value=abs();");
		String output=program(false,
				"value = abs()");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testLhsIndexing() {
		String input=program(true,
				"Z(1,2)=3");
		String output=program(false,
				"Z[(1 - 1), (2 - 1)] = 3");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testRhsIndexing() {
		//if the 'function' is not known, assumes indexing instead
		String input=program(true,
				"unknown=[1 2 3 4]",
				"val=unknown(2)");
		String output=program(false,
				"unknown = array([1, 2, 3, 4])",
				"val = unknown[(2 - 1)]");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testSlicing() {
		String input=program(true,
				"val=unknown(:,1)");
		String output=program(false,
				"val = unknown[:, (1 - 1)]");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testClearClose() {
		//assuming the interpreter is fresh anyway
		String input=program(true,
				"clc; clear all; close all",
				"",
				"a=5");
		String output=program(false,
				"a = 5");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testDataLoad() {
		String input=program(true,
				"data = csvread('data2.csv');",
				"u = [ones(size(data,1),1) data(:,2)];",
				"v = data(:,1);");
		String output=program(false,
				"data = np.genfromtxt('data2.csv', delimiter=',')",
				"u = array([np.ones(size(data, 1)), data[:, (2 - 1)]])",
				"v = data[:, (1 - 1)]");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY, PythonDef.SIZE);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testRegularOnes() {
		String input=program(true,
				"data=ones(2, 3, 4)");
		String output=program(false,
				"data = np.ones(2, 3, 4)");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
}
