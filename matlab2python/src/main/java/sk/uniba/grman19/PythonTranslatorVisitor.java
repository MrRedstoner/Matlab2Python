package sk.uniba.grman19;

import static java.util.function.Predicate.isEqual;
import static sk.uniba.grman19.util.PythonDef.ARRAY;
import static sk.uniba.grman19.util.PythonDef.EZPLOT;
import static sk.uniba.grman19.util.PythonDef.FPLOT;
import static sk.uniba.grman19.util.PythonDef.FUNC2STR;
import static sk.uniba.grman19.util.PythonDef.M_SUM;
import static sk.uniba.grman19.util.PythonDef.PLOT;
import static sk.uniba.grman19.util.PythonDef.PRINTF;
import static sk.uniba.grman19.util.PythonDef.SIZE;
import static sk.uniba.grman19.util.PythonDef.SURFC;
import static sk.uniba.grman19.util.PythonDef.ZEROS;
import static sk.uniba.grman19.util.PythonImport.AXES3D;
import static sk.uniba.grman19.util.PythonImport.INSPECT;
import static sk.uniba.grman19.util.PythonImport.ITERTOOLS;
import static sk.uniba.grman19.util.PythonImport.NUMPY;
import static sk.uniba.grman19.util.PythonImport.PYPLOT;
import static sk.uniba.grman19.util.PythonImport.RANDOM;
import static sk.uniba.grman19.util.PythonImport.SQRT;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.STGroup;

import sk.uniba.grman19.MatlabParser.Additive_expressionContext;
import sk.uniba.grman19.MatlabParser.And_expressionContext;
import sk.uniba.grman19.MatlabParser.Array_elementContext;
import sk.uniba.grman19.MatlabParser.Array_expressionContext;
import sk.uniba.grman19.MatlabParser.Array_listContext;
import sk.uniba.grman19.MatlabParser.Array_mul_expressionContext;
import sk.uniba.grman19.MatlabParser.Array_sub_listContext;
import sk.uniba.grman19.MatlabParser.Assignment_expressionContext;
import sk.uniba.grman19.MatlabParser.Assignment_statementContext;
import sk.uniba.grman19.MatlabParser.Clear_statementContext;
import sk.uniba.grman19.MatlabParser.Close_statementContext;
import sk.uniba.grman19.MatlabParser.Elseif_clauseContext;
import sk.uniba.grman19.MatlabParser.EostmtContext;
import sk.uniba.grman19.MatlabParser.Equality_expressionContext;
import sk.uniba.grman19.MatlabParser.ExpendContext;
import sk.uniba.grman19.MatlabParser.ExpressionContext;
import sk.uniba.grman19.MatlabParser.Expression_statementContext;
import sk.uniba.grman19.MatlabParser.Func_ident_listContext;
import sk.uniba.grman19.MatlabParser.Func_return_listContext;
import sk.uniba.grman19.MatlabParser.Function_declareContext;
import sk.uniba.grman19.MatlabParser.Function_declare_lhsContext;
import sk.uniba.grman19.MatlabParser.Global_statementContext;
import sk.uniba.grman19.MatlabParser.Hold_statementContext;
import sk.uniba.grman19.MatlabParser.Identifier_listContext;
import sk.uniba.grman19.MatlabParser.Index_expressionContext;
import sk.uniba.grman19.MatlabParser.Index_expression_listContext;
import sk.uniba.grman19.MatlabParser.Iteration_statementContext;
import sk.uniba.grman19.MatlabParser.Jump_statementContext;
import sk.uniba.grman19.MatlabParser.Lambda_definitionContext;
import sk.uniba.grman19.MatlabParser.Multiplicative_expressionContext;
import sk.uniba.grman19.MatlabParser.Or_expressionContext;
import sk.uniba.grman19.MatlabParser.Postfix_expressionContext;
import sk.uniba.grman19.MatlabParser.Primary_expressionContext;
import sk.uniba.grman19.MatlabParser.Relational_expressionContext;
import sk.uniba.grman19.MatlabParser.Selection_statementContext;
import sk.uniba.grman19.MatlabParser.StatementContext;
import sk.uniba.grman19.MatlabParser.Statement_listContext;
import sk.uniba.grman19.MatlabParser.Translation_unitContext;
import sk.uniba.grman19.MatlabParser.Unary_expressionContext;
import sk.uniba.grman19.MatlabParser.Unary_operatorContext;
import sk.uniba.grman19.util.ContextStack.IndexingContextStack;
import sk.uniba.grman19.util.ContextStack.LhsContextStack;
import sk.uniba.grman19.util.Fragment;

public class PythonTranslatorVisitor implements MatlabVisitor<Fragment> {
	
	private static void doAssert(boolean condition) {
		if(!condition) {
			throw new RuntimeException("assertion failure");
		}
	}
	
	//TODO write more translator tests
	
	private final STGroup templates;
	private final Optional<Set<String>> ignore;

	private final LhsContextStack lhsCont=new LhsContextStack();
	private final IndexingContextStack indexCont=new IndexingContextStack();
	
	private Fragment template(String name) {
		return new Fragment(templates.getInstanceOf(name));
	}
	
	private Fragment literal(String value) {
		return template("literal").add("text", value);
	}
	
	private String pythonString(String text) {
		//matlab escapes ' as '', Python as \'
		//make sure not to clobber the outer '
		return "'"+text.substring(1, text.length()-1).replace("''", "\\'")+"'";
	}
	

	private Fragment comment(String text) {
		//at minimum contains the starting % and ending \n
		if(text.charAt(1)==' ') {
			//if the comment was already styled as "% text of comment"
			text=text.substring(2, text.length()-1);
		} else {
			text=text.substring(1, text.length()-1);
		}
		return template("comment")
				.add("text", text);
	}
	
	/**
	 * @param templates used as a source of template instances
	 * */
	public PythonTranslatorVisitor(STGroup templates){
		this(templates,Optional.empty());
	}
	
	/**
	 * @param templates used as a source of template instances
	 * @param indexIgnore if non-empty, anything not contained when indexing will be reported
	 * */
	public PythonTranslatorVisitor(STGroup templates, Optional<Set<String>>indexIgnore){
		this.templates=templates;
		this.ignore=indexIgnore;
	}

	@Override
	public Fragment visit(ParseTree tree) {
		return tree.accept(this);
	}

	@Override
	public Fragment visitChildren(RuleNode node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitTerminal(TerminalNode node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitErrorNode(ErrorNode node) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitPrimary_expression(Primary_expressionContext ctx) {
		if(ctx.IDENTIFIER()!=null) {
			//option IDENTIFIER
			return literal(ctx.getText());
		}
		if(ctx.CONSTANT()!=null) {
			//option CONSTANT
			return literal(ctx.getText());
		}
		if(ctx.STRING_LITERAL()!=null) {
			//option STRING_LITERAL
			return literal(pythonString(ctx.STRING_LITERAL().getText()));
		}
		if(ctx.expression()!=null) {
			//option '(' expression ')'
			return template("bracketed_expression").add("expression", ctx.expression().accept(this));
		}
		if(ctx.array_list()!=null) {
			//option '[' array_list ']'
			//is this the lhs?
			if(lhsCont.isLhs()) {
				return lhsCont.visitAsNonLhs(ctx.array_list(),this).get();
			}
			//return as a numpy array
			return template("function_call")
						.addImport(NUMPY)
						.addDef(ARRAY)
						.add("name", "array")
						.add("arg_list", template("square_bracketed_expression").add("expression", ctx.array_list().accept(this)));
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitPostfix_expression(Postfix_expressionContext ctx) {
		if(ctx.primary_expression()!=null) {
			//option primary_expression
			return ctx.primary_expression().accept(this);
		}
		if(ctx.array_expression()!=null) {
			//option array_expression
			return ctx.array_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitIndex_expression(Index_expressionContext ctx) {
		if(ctx.expression()!=null) {
			//option expression
			if(indexCont.isIndexing()) {
				//correct for difference in indexing
				return template("minus_one").add("expression", indexCont.visitAsNonIndex(ctx.expression(),this).get());
			} else {
				return ctx.expression().accept(this);
			}
		}
		//option ':'
		//used for slicing, same in Python
		//pad with np.newaxis to not loose a dimension
		return literal(":, np.newaxis").addImport(NUMPY);
	}

	@Override
	public Fragment visitIndex_expression_list(Index_expression_listContext ctx) {
		if(ctx.index_expression_list()==null) {
			//option index_expression
			return ctx.index_expression().accept(this);
		} else {
			//option index_expression_list ',' index_expression
			return template("comma_separated")
						.add("list", ctx.index_expression_list().accept(this))
						.add("element", ctx.index_expression().accept(this));
		}
	}
	
	private Set<String>knownFunctions=Collections.unmodifiableSet(Stream.of(
			"fprintf", "func2str", "linspace", "meshgrid", "pause", "size",
			"zeros", "sqrt", "title", "plot", "legend", "surfc",
			"contour", "figure", "fplot", "rand", "abs", "ones",
			"csvread", "exp", "log", "norm", "sum", "ezplot",
			//names used for functions
			"f", "df", "d2f"
			).collect(Collectors.toSet()));
	
	@Override
	public Fragment visitArray_expression(Array_expressionContext ctx) {
		//IDENTIFIER '(' index_expression_list ')'
		String identifier = ctx.IDENTIFIER().getText();
		
		//is this the lhs?
		if(lhsCont.isLhs()) {
			return template("index_call")
					.add("name", identifier)
					.add("arg_list", lhsCont.visitAsNonLhs(indexCont.visitAsIndex(ctx.index_expression_list(),this)).get());
		}
		
		//is this the rhs with an unknown function?
		if(!knownFunctions.contains(identifier)) {
			//probably actual indexing then
			final String ident=identifier;
			ignore.ifPresent(set->{
				if(!set.contains(ident))System.out.println("Indexing on "+ident+" text: "+ctx.getText());
			});
			
			return template("index_call")
					.add("name", identifier)
					.add("arg_list", indexCont.visitAsIndex(ctx.index_expression_list(),this).get());
		}
		
		//otherwise used as a function call
		
		Fragment ret=template("function_call");
		Fragment argList;
		if(ctx.index_expression_list()!=null) {
			argList=ctx.index_expression_list().accept(this);
		} else {
			argList=template("empty");
		}
						
		//add imports and defs as needed
		switch(identifier) {
		case"fprintf":{
			ret.addImport(ITERTOOLS).addDef(PRINTF);
			identifier="printf";
		}break;
		case"func2str":{
			ret.addImport(INSPECT).addDef(FUNC2STR);
		}break;
		case"linspace":{
			ret.addImport(NUMPY);
			identifier="np.linspace";
		}break;
		case"meshgrid":{
			ret.addImport(NUMPY);
			identifier="np.meshgrid";
		}break;
		case"pause":{
			ret.addImport(PYPLOT);
			identifier="plt.pause";
		}break;
		case"size":{
			ret.addDef(SIZE);
		}break;
		case"zeros":{
			ret.addImport(NUMPY).addDef(ZEROS);
		}break;
		case"sqrt":{
			ret.addImport(SQRT);
		}break;
		case"title":{
			ret.addImport(PYPLOT);
			identifier="plt.title";
		}break;
		case"plot":{
			ret.addImport(PYPLOT).addDef(PLOT);
		}break;
		case"legend":{
			ret.addImport(PYPLOT);
			identifier="plt.legend";
			//wrap in []
			argList=template("square_bracketed_expression").add("expression", argList);
		}break;
		case"surfc":{
			ret.addImport(PYPLOT).addImport(AXES3D).addDef(SURFC);
		}break;
		case"contour":{
			ret.addImport(PYPLOT);
			identifier="plt.contour";
		}break;
		case"figure":{
			ret.addImport(PYPLOT);
			identifier="plt.figure";
		}break;
		case"ezplot":{
			ret.addImport(NUMPY).addImport(PYPLOT).addDef(PLOT).addDef(EZPLOT);
		}break;
		case"fplot":{
			ret.addImport(NUMPY).addImport(PYPLOT).addDef(PLOT).addDef(FPLOT);
		}break;
		case"rand":{
			//assuming no-arg variant
			doAssert(ctx.index_expression_list()==null);
			ret.addImport(RANDOM);
			identifier="random.random";
		}break;
		case"abs":{
			//available as-is
		}break;
		case"ones":{
			//skip last argument if it's a 1
			if(Optional.of(ctx)
				.map(Array_expressionContext::index_expression_list)
				.map(Index_expression_listContext::index_expression)
				.map(Index_expressionContext::getText)
				.filter(isEqual("1"))
				.isPresent()) {
				argList=ctx.index_expression_list().index_expression_list().accept(this);
			}
			ret.addImport(NUMPY);
			identifier="np.ones";
		}break;
		case"csvread":{
			ret.addImport(NUMPY);
			identifier="np.genfromtxt";
			argList=template("comma_separated").add("list", argList).add("element", "delimiter=','");
		}break;
		case"exp":{
			ret.addImport(NUMPY);
			identifier="np.exp";
		}break;
		case"log":{
			ret.addImport(NUMPY);
			identifier="np.log";
		}break;
		case"norm":{
			ret.addImport(NUMPY);
			identifier="np.linalg.norm";
		}break;
		case"sum":{
			//assuming single-arg variant
			doAssert(ctx.index_expression_list().index_expression_list()==null);
			ret.addDef(M_SUM);
			identifier="m_sum";
		}break;
		//names used for functions
		case"f":
		case"df":
		case"d2f":
			break;
		default:{
			//should never happen is the knownFunctions set is correct
			throw new RuntimeException("Default on function "+identifier);
		}
		}

		return ret
				.add("name", identifier)
				.add("arg_list", argList);
	}

	@Override
	public Fragment visitUnary_expression(Unary_expressionContext ctx) {
		if(ctx.unary_operator()==null) {
			if(ctx.getChildCount()==1) {
				//option postfix_expression
				return ctx.postfix_expression().accept(this);
			} else {
				//option postfix_expression '\''
				//means the complex conjugate transpose
				return template("conjT").add("in", ctx.postfix_expression().accept(this));
			}
		} else {
			//option unary_operator postfix_expression
			return template("unary_operator_expression")
						.add("operator", ctx.unary_operator().accept(this))
						.add("expression", ctx.postfix_expression().accept(this));
		}
	}

	@Override
	public Fragment visitUnary_operator(Unary_operatorContext ctx) {
		switch(ctx.getChild(0).getText()) {
		//option '+'
		case "+": return literal("+");
		//option '-'
		case "-": return literal("-");
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Fragment visitArray_mul_expression(Array_mul_expressionContext ctx) {
		if(ctx.array_mul_expression()==null) {
			//option unary_expression
			return ctx.unary_expression().accept(this);
		} else {
			//option array_mul_expression ARRAYMUL unary_expression
			//option array_mul_expression ARRAYDIV unary_expression
			//option array_mul_expression ARRAYRDIV unary_expression
			//option array_mul_expression ARRAYPOW unary_expression
			String operator=null;
			switch(ctx.getChild(1).getText()) {
			//numpy arrays make it map over the array automatically
			case "./":operator="/";break;
			case ".^":operator="**";break;
			//elementwise multiplication
			case ".*":operator="*";break;
			default:{
				throw new UnsupportedOperationException();
			}
			}
			return template("binary_operator_expression")
					.add("expression0", ctx.array_mul_expression().accept(this))
					.add("operator", operator)
					.add("expression1", ctx.unary_expression().accept(this));
		}
	}

	@Override
	public Fragment visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		if(ctx.multiplicative_expression()==null) {
			//option array_mul_expression
			return ctx.array_mul_expression().accept(this);
		} else {
			//option multiplicative_expression '*' array_mul_expression
			//option multiplicative_expression '/' array_mul_expression
			//option multiplicative_expression '\\' array_mul_expression
			//option multiplicative_expression '^' array_mul_expression
			String operator=null;
			switch(ctx.getChild(1).getText()) {
			case "/":operator="/";break;
			case "^":operator="**";break;
			
			//matrix multiplication (if matrices)
			case "*":{
				//to get matrix multiplication for matrixes and scalar for scalars
				return template("function_call")
							.addImport(NUMPY)
							.add("name", "np.dot")
							.add("arg_list", template("comma_separated_elems")
									.add("element", ctx.multiplicative_expression().accept(this))
									.add("element", ctx.array_mul_expression().accept(this)));
			}
			
			//matlab mldivide
			case "\\":{
				return template("index_call")
							.add("arg_list", "0")
							.add("name", template("function_call")
									.addImport(NUMPY)
									.add("name", "np.linalg.lstsq")
									.add("arg_list", template("comma_separated_elems")
											.add("element", ctx.multiplicative_expression().accept(this))
											.add("element", ctx.array_mul_expression().accept(this))
											.add("element", "rcond=None")));
			}
			
			default:{
				throw new UnsupportedOperationException();
			}
			}
			return template("binary_operator_expression")
					.add("expression0", ctx.multiplicative_expression().accept(this))
					.add("operator", operator)
					.add("expression1", ctx.array_mul_expression().accept(this));
		}
	}

	@Override
	public Fragment visitAdditive_expression(Additive_expressionContext ctx) {
		if(ctx.additive_expression()==null) {
			//option multiplicative_expression
			return ctx.multiplicative_expression().accept(this);
		} else {
			//option additive_expression '+' multiplicative_expression
			//option additive_expression '-' multiplicative_expression
			return template("binary_operator_expression")
						.add("expression0", ctx.additive_expression().accept(this))
						.add("operator", ctx.getChild(1).getText())
						.add("expression1", ctx.multiplicative_expression().accept(this));
		}
	}

	@Override
	public Fragment visitRelational_expression(Relational_expressionContext ctx) {
		if(ctx.relational_expression()==null) {
			//option additive_expression
			return ctx.additive_expression().accept(this);
		} else {
			//option relational_expression '<' additive_expression
			//option relational_expression '>' additive_expression
			//option relational_expression LE_OP additive_expression
			//option relational_expression GE_OP additive_expression
			return template("binary_operator_expression")
					.add("expression0", ctx.relational_expression().accept(this))
					.add("operator", ctx.getChild(1).getText())
					.add("expression1", ctx.additive_expression().accept(this));
		}
	}

	@Override
	public Fragment visitEquality_expression(Equality_expressionContext ctx) {
		if(ctx.equality_expression()==null) {
			//option relational_expression
			return ctx.relational_expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitAnd_expression(And_expressionContext ctx) {
		if(ctx.and_expression()==null) {
			//option equality_expression
			return ctx.equality_expression().accept(this);
		} else {
			//option and_expression '&' equality_expression
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitOr_expression(Or_expressionContext ctx) {
		if(ctx.or_expression()==null) {
			//option and_expression
			return ctx.and_expression().accept(this);
		} else {
			//option or_expression '|' and_expression
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitExpression(ExpressionContext ctx) {
		if(ctx.lambda_definition()!=null) {
			//option lambda_definition
			return ctx.lambda_definition().accept(this);
		}
		if(ctx.expression()==null) {
			//option or_expression
			return ctx.or_expression().accept(this);
		} else {
			//option expression ':' or_expression
			//used as range, + 1 because matlab uses inclusive ranges
			return template("range")
						.add("start", ctx.expression().accept(this))
						.add("stop", template("plus_one")
								.add("expression", ctx.or_expression().accept(this)));
		}
	}

	@Override
	public Fragment visitAssignment_expression(Assignment_expressionContext ctx) {
		return template("assignment_expression")
					.add("postfix_expression", lhsCont.visitAsLhs(ctx.postfix_expression(),this).get())
					.add("expression", ctx.expression().accept(this));
	}

	@Override
	public Fragment visitEostmt(EostmtContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitStatement(StatementContext ctx) {
		if(ctx.clear_statement()!=null || ctx.close_statement()!=null) {
			return null;
		}
		if(ctx.assignment_statement()!=null) {
			//option assignment_statement
			return ctx.assignment_statement().accept(this);
		}
		if(ctx.expression_statement()!=null) {
			//option expression_statement
			return ctx.expression_statement().accept(this);
		}
		if(ctx.selection_statement()!=null) {
			//option selection_statement
			return ctx.selection_statement().accept(this);
		}
		if(ctx.iteration_statement()!=null) {
			//option iteration_statement
			return ctx.iteration_statement().accept(this);
		}
		if(ctx.jump_statement()!=null) {
			//option jump_statement
			return ctx.jump_statement().accept(this);
		}
		if(ctx.COMMENT_STATEMENT()!=null) {
			//option COMMENT_STATEMENT
			return comment(ctx.COMMENT_STATEMENT().getText());
		}
		if(ctx.hold_statement()!=null) {
			//option hold_statement
			return ctx.hold_statement().accept(this);
		}
		
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitStatement_list(Statement_listContext ctx) {
		if(ctx.statement_list()==null) {
			//option statement
			return ctx.statement().accept(this);
		} else {
			//option statement_list statement
			Fragment statementList=ctx.statement_list().accept(this);
			if(statementList==null) {
				return ctx.statement().accept(this);
			}
			Fragment statement=ctx.statement().accept(this);
			if(statement==null) {
				return statementList;
			}
			return template("statement_list")
						.add("statement_list", statementList)
						.add("statement", ctx.statement().accept(this));
		}
	}

	@Override
	public Fragment visitIdentifier_list(Identifier_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitGlobal_statement(Global_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitClear_statement(Clear_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitExpression_statement(Expression_statementContext ctx) {
		if(ctx.expression()==null) {
			//option eostmt
			//noop command
			return null;
		} else {
			String text=ctx.getText().trim();
			switch(text) {
			//ignore tic-toc for now, TODO implement it
			case"tic":
			case"toc":
				return template("comment").add("text", text);
			}
			//option expression eostmt
			return ctx.expression().accept(this);
		}
	}

	@Override
	public Fragment visitAssignment_statement(Assignment_statementContext ctx) {
		//assignment_expression eostmt
		return ctx.assignment_expression().accept(this);
	}

	@Override
	public Fragment visitArray_element(Array_elementContext ctx) {
		if(ctx.array_expression()==null) {
			//option expression
			return ctx.expression().accept(this);
		} else {
			//option array_expression
			return ctx.array_expression().accept(this);
		}
	}

	@Override
	public Fragment visitArray_sub_list(Array_sub_listContext ctx) {
		//option array_element (',' array_element) *
		//option array_element +
		//separator is irrelevant
		Fragment result=template("comma_separated_elems");
		ctx.array_element()
			.stream()
			.map(this::visit)
			.filter(Objects::nonNull)
			.forEach(elem->result.add("element", elem));
		return result;
	}
	
	@Override
	public Fragment visitArray_list(Array_listContext ctx) {
		//array_sub_list (';' array_sub_list) *
		if(ctx.array_sub_list().size()==1) {
			//actually array_sub_list
			return ctx.array_sub_list(0).accept(this);
		} else {
			//actually array_sub_list (';' array_sub_list) +
			Fragment result=template("comma_separated_elems");
			ctx.array_sub_list()
				.stream()
				.map(this::visit)
				.forEach(elem->result.add("element", template("square_bracketed_expression").add("expression",elem)));
			return result;
		}
	}

	@Override
	public Fragment visitSelection_statement(Selection_statementContext ctx) {
		if(ctx.elseif_clause()==null) {
			if(ctx.ELSE()==null) {
				//option IF expression statement_list END eostmt
				return template("pureif")
							.add("condition", ctx.expression().accept(this))
							.add("statement_list", Optional.ofNullable(ctx.statement_list(0).accept(this)).orElse(template("pass")));
			} else {
				//option IF expression statement_list ELSE statement_list END eostmt
				return template("ifelse")
						.add("condition", ctx.expression().accept(this))
						.add("statement_list0", Optional.ofNullable(ctx.statement_list(0).accept(this)).orElse(template("pass")))
						.add("statement_list1", Optional.ofNullable(ctx.statement_list(1).accept(this)).orElse(template("pass")));
			}
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitElseif_clause(Elseif_clauseContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitIteration_statement(Iteration_statementContext ctx) {
		if(ctx.FOR()!=null) {
			//option FOR IDENTIFIER '=' expression statement_list END eostmt
			//option FOR '(' IDENTIFIER '=' expression ')' statement_list END eostmt
			return template("foreach")
						.add("variable", literal(ctx.IDENTIFIER().getText()))
						.add("iterable", ctx.expression().accept(this))
						.add("statement_list", Optional.ofNullable(ctx.statement_list().accept(this)).orElse(template("pass")));
		} else {
			//option WHILE expression statement_list END eostmt
			return template("while_loop")
						.add("condition", ctx.expression().accept(this))
						.add("statement_list", Optional.ofNullable(ctx.statement_list().accept(this)).orElse(template("pass")));
		}
	}

	@Override
	public Fragment visitJump_statement(Jump_statementContext ctx) {
		if(ctx.BREAK()!=null) {
			//option BREAK eostmt
			return template("break");
		} else {
			//option RETURN eostmt
			return template("return");
		}
	}

	@Override
	public Fragment visitTranslation_unit(Translation_unitContext ctx) {
		if(ctx.FUNCTION()==null) {
			//option: statement_list
			return Optional.ofNullable(ctx.statement_list().accept(this)).orElse(template("pass"));
		} else {
			//option: FUNCTION function_declare eostmt statement_list
			//statement_list contains inner code, function_declare has name and return list
			return ctx.function_declare().accept(this)
						.add("statement_list", Optional.ofNullable(ctx.statement_list().accept(this)).orElse(template("pass")));
			
		}
	}

	@Override
	public Fragment visitFunc_ident_list(Func_ident_listContext ctx) {
		if(ctx.func_ident_list()==null) {
			//option IDENTIFIER
			return literal(ctx.IDENTIFIER().getText());
		} else {
			//option func_ident_list ',' IDENTIFIER
			return template("comma_separated")
					.add("list", ctx.func_ident_list().accept(this))
					.add("element", literal(ctx.IDENTIFIER().getText()));
		}
	}

	@Override
	public Fragment visitFunc_return_list(Func_return_listContext ctx) {
		if(ctx.IDENTIFIER()!=null) {
			//option IDENTIFIER
			return literal(ctx.IDENTIFIER().getText());
		} else {
			//option '[' func_ident_list ']'
			return ctx.func_ident_list().accept(this);
		}
	}

	@Override
	public Fragment visitFunction_declare_lhs(Function_declare_lhsContext ctx) {
		// IDENTIFIER
		// IDENTIFIER '(' ')'
		// IDENTIFIER '(' func_ident_list ')'
		return template("function_def")
					.add("name", literal(ctx.IDENTIFIER().getText()))
					.add("args", Optional.ofNullable(ctx.func_ident_list()).map(fil->fil.accept(this)).orElse(template("empty")));
	}

	@Override
	public Fragment visitFunction_declare(Function_declareContext ctx) {
		//function_declare_lhs has name and args
		if(ctx.func_return_list()==null) {
			//option function_declare_lhs
		} else {
			//option func_return_list '=' function_declare_lhs
			return ctx.function_declare_lhs().accept(this)
						.add("return_list", ctx.func_return_list().accept(this));
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitHold_statement(Hold_statementContext ctx) {
		return template("comment").add("text", ctx.getText().trim());
	}

	@Override
	public Fragment visitLambda_definition(Lambda_definitionContext ctx) {
		//'@(' index_expression_list ')' expression
		return template("lambda")
					.add("args", ctx.index_expression_list().accept(this))
					.add("expression", ctx.expression().accept(this));
	}

	@Override
	public Fragment visitExpend(ExpendContext ctx) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitClose_statement(Close_statementContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}
