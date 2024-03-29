package sk.uniba.grman19;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
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
import sk.uniba.grman19.util.IdentifierTypeStorage;
import sk.uniba.grman19.util.PythonDef;
import sk.uniba.grman19.util.PythonImport;
import sk.uniba.grman19.util.TreeUtils;

public class TranslatorVisitorTest {
	private static STGroup templates;
	private static PTVFactory ptvFactory;

	@BeforeAll
	public static void createVisitor() {
		templates = new STGroupFile("Python.stg");
		ptvFactory=new PTVFactory(true, templates, Optional.empty(),new IdentifierTypeStorage());
	}

	private static class TestST extends ST{
		private static final Map<String,PythonDef>defMap;
		private static final Map<String,PythonImport>importMap;

		static {
			defMap=Collections.unmodifiableMap(Arrays.stream(PythonDef.values()).collect(Collectors.toMap(def->def.getRepresentation(), def->def)));
			importMap=Collections.unmodifiableMap(Arrays.stream(PythonImport.values()).collect(Collectors.toMap(imp->imp.getRepresentation(), imp->imp)));
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
		
		@Override
		public String render() {
			return super.render().replace("\r", "");
		}
	}

	private static String program(boolean endNewLine,String...lines) {
		//suffix by a newline
		return Arrays.stream(lines).collect(Collectors.joining("\n","",endNewLine?"\n":""));
	}
	
	private static String translate(ST template, CharStream input, boolean dump, PythonTranslatorVisitor ptv) {
		MatlabParser parser;
		MatlabLexer lexer=new MatlabLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		parser = new MatlabParser(tokens);
		ParseTree tree = parser.translation_unit();
		
		if(dump)TreeUtils.dump(parser, tree);
		dump=false;

		Fragment result=ptv.visit(tree);

		assertTrue(parser.getNumberOfSyntaxErrors()==0 && !result.hadError());

		return result.getFullTranslation(template);
	}
	
	private static void check(String input, String output, EnumSet<PythonDef> defs, EnumSet<PythonImport> imports) {
		check(input,output,defs,imports,false);
	}

	private static void check(String input, String output, EnumSet<PythonDef> defs, EnumSet<PythonImport> imports, boolean dump) {
		check(input,output,defs,imports,ptvFactory.getNew(),dump);
	}
	
	private static void check(String input, String output, EnumSet<PythonDef> defs, EnumSet<PythonImport> imports, PythonTranslatorVisitor ptv, boolean dump) {
		TestST st=new TestST();
		String translation=translate(st,CharStreams.fromString(input), dump, ptv);
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
				"Z(1,i,3+4)=3");
		String output=program(false,
				"Z[0, ((i) - 1), ((3 + 4) - 1)] = 3");
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
				"val = unknown[1]");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testFunctionNamePickup() {
		String input=program(true,
				"fun=@(x)x",
				"fun(5)");
		String output=program(false,
				"fun = lambda x: x",
				"fun(5)");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testSlicing() {
		String input=program(true,
				"val=unknown(:,1)");
		String output=program(false,
				"val = unknown[:, np.newaxis, 0]");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
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
				"u = array([np.ones(size(data, 1)), data[:, np.newaxis, 1]])",
				"v = data[:, np.newaxis, 0]");
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
	
	@Test
	public void testExpressions() {
		String input=program(true,
				"a=2;b=3;",
				"c=(a+b)",
				"d=a - (b^c)");
		String output=program(false,
				"a = 2",
				"b = 3",
				"c = (a + b)",
				"d = a - (b ** c)");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testComparisons() {
		String input=program(true,
				"a==b",
				"c~=b");
		String output=program(false,
				"a == b",
				"c != b");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testMultiplications() {
		String input=program(true,
				"a=2*3",
				"b=x*y",
				"c=x.*y");
		String output=program(false,
				"a = np.dot(2, 3)",
				"b = np.dot(x, y)",
				"c = x * y");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testMultiplicationsPriority() {
		String input=program(true,
				"a=4*x.^2");
		String output=program(false,
				"a = np.dot(4, x ** 2)");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testMultiplicationsPriority2() {
		String input=program(true,
				"a=4*x^2");
		String output=program(false,
				"a = np.dot(4, x ** 2)");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testMldivide() {
		String input=program(true,
				"a=x\\y");
		String output=program(false,
				"a = np.linalg.lstsq(x, y, rcond=None)[0]");//numpy returns a tuple of things, [0] being the solution
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testSum() {
		String input=program(true,
				"a=sum(x+y+5)");
		String output=program(false,
				"a = m_sum(x + y + 5)");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.M_SUM);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testNestedLists() {
		String input=program(true,
				"a=[1 2 3;4 5 6]");
		String output=program(false,
				"a = array([[1, 2, 3], [4, 5, 6]])");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testListsOfIndexed() {
		String input=program(true,
				"x=[0 1 2]",
				"y=[X(1) X(3)]");
		String output=program(false,
				"x = array([0, 1, 2])",
				"y = array([X[0], X[2]])");
		EnumSet<PythonDef> defs=EnumSet.of(PythonDef.ARRAY);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void eliminateTicToc() { //TODO implement tic-toc fully
		String input=program(true,
				"tic",
				"a=5",
				"toc");
		String output=program(false,
				"# tic",
				"a = 5",
				"# toc");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void add1ToRangeEvaluate() {
		String input=program(true,
				"a=1:5");
		String output=program(false,
				"a = np.array(range(1, 6))");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void add1ToRange() {
		String input=program(true,
				"a=1:b",
				"c=2:b+1");
		String output=program(false,
				"a = np.array(range(1, ((b) + 1)))",
				"c = np.array(range(2, ((b + 1) + 1)))");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.of(PythonImport.NUMPY);
		check(input,output,defs,imports);
	}
	
	@Test
	public void renameReservedWords() {
		String input=program(true,
				"a=5",
				"del=6",
				"del(1)");
		String output=program(false,
				"a = 5",
				"del_ = 6",
				"del_[0]");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void translateSwitch() {
		String input=program(true,
				"switch variable",
				"case 'ab'",
				"	a=1",
				"case 'abc'",
				"	a=10",
				"otherwise",
				"	a=100",
				"end");
		String output=program(false,
				"if variable == 'ab':",
				"    a = 1",
				"elif variable == 'abc':",
				"    a = 10",
				"else:",
				"    a = 100");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}

	@Test
	public void testWhileFormDirect() {
		String input=program(true,
				"while a>b",
				"	;",
				"end");
		String output=program(false,
				"while a > b:",
				"    pass");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testWhileWithoutNewline() {
		String input=program(true,
				"while a>b break;end");
		String output=program(false,
				"while a > b:",
				"    break");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
	
	@Test
	public void testWhileFormFuncCall() {
		String input=program(true,
				"while abs(b)>c",
				"	;",
				"end");
		String output=program(false,
				"while abs(b) > c:",
				"    pass");
		EnumSet<PythonDef> defs=EnumSet.noneOf(PythonDef.class);
		EnumSet<PythonImport> imports=EnumSet.noneOf(PythonImport.class);
		check(input,output,defs,imports);
	}
}
