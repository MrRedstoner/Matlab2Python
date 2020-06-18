package sk.uniba.grman19;

import static sk.uniba.grman19.util.PythonDef.FPLOT;
import static sk.uniba.grman19.util.PythonDef.FUNC2STR;
import static sk.uniba.grman19.util.PythonDef.PRINTF;
import static sk.uniba.grman19.util.PythonDef.SURFC;
import static sk.uniba.grman19.util.PythonImport.AXES3D;
import static sk.uniba.grman19.util.PythonImport.INSPECT;
import static sk.uniba.grman19.util.PythonImport.NUMPY;
import static sk.uniba.grman19.util.PythonImport.PYPLOT;
import static sk.uniba.grman19.util.PythonImport.RANDOM;
import static sk.uniba.grman19.util.PythonImport.SLEEP;
import static sk.uniba.grman19.util.PythonImport.SQRT;

import java.util.Optional;

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
import sk.uniba.grman19.MatlabParser.Assignment_expressionContext;
import sk.uniba.grman19.MatlabParser.Assignment_statementContext;
import sk.uniba.grman19.MatlabParser.Clear_statementContext;
import sk.uniba.grman19.MatlabParser.Elseif_clauseContext;
import sk.uniba.grman19.MatlabParser.EostmtContext;
import sk.uniba.grman19.MatlabParser.Equality_expressionContext;
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
import sk.uniba.grman19.util.Fragment;

public class PythonTranslatorVisitor implements MatlabVisitor<Fragment> {
	
	//TODO write translator tests
	
	private final STGroup templates;
	
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
	
	protected PythonTranslatorVisitor(STGroup templates){
		this.templates=templates;
	}

	@Override
	public Fragment visit(ParseTree tree) {
		return tree.accept(this);
	}

	@Override
	public Fragment visitChildren(RuleNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub
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
			if(ctx.getParent() instanceof Postfix_expressionContext && ctx.getParent().getParent() instanceof Assignment_expressionContext) {
				return ctx.array_list().accept(this);
			}
			//return as a python list
			return template("bracketed_expression").add("expression", ctx.array_list().accept(this));
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
			return ctx.expression().accept(this);
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
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

	@Override
	public Fragment visitArray_expression(Array_expressionContext ctx) {
		//IDENTIFIER '(' index_expression_list ')'
		//used as a function call
		String identifier = ctx.IDENTIFIER().getText();
		
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
			ret.addDef(PRINTF);
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
			ret.addImport(SLEEP);
			identifier="sleep";
		}break;
		case"size":{
			identifier="len";
			//toss the second argument
			assert ctx.index_expression_list().index_expression_list()!=null;
			argList=ctx.index_expression_list().index_expression_list().accept(this);
		}break;
		case"zeros":{
			ret.addImport(NUMPY);
			identifier="np.ma.zeros";
		}break;
		case"sqrt":{
			ret.addImport(SQRT);
		}break;
		case"title":{
			ret.addImport(PYPLOT);
			identifier="plt.title";
		}break;
		case"plot":{
			ret.addImport(PYPLOT);
			identifier="plt.plot";
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
		case"fplot":{
			ret.addImport(NUMPY).addImport(PYPLOT).addDef(FPLOT);
		}break;
		case"rand":{
			//assuming no-arg variant
			assert ctx.index_expression_list()==null;
			ret.addImport(RANDOM);
			identifier="random.random";
		}break;
		}

		return ret
				.add("name", identifier)
				.add("arg_list", argList);
	}

	@Override
	public Fragment visitUnary_expression(Unary_expressionContext ctx) {
		if(ctx.unary_operator()==null) {
			//option postfix_expression
			return ctx.postfix_expression().accept(this);
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
	public Fragment visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		if(ctx.multiplicative_expression()==null) {
			//option unary_expression
			return ctx.unary_expression().accept(this);
		} else {
			//option multiplicative_expression '*' unary_expression
			//option multiplicative_expression '/' unary_expression
			//option multiplicative_expression '\\' unary_expression
			//option multiplicative_expression '^' unary_expression
			//option multiplicative_expression ARRAYMUL unary_expression
			//option multiplicative_expression ARRAYDIV unary_expression
			//option multiplicative_expression ARRAYRDIV unary_expression
			//option multiplicative_expression ARRAYPOW unary_expression
			String operator=null;
			switch(ctx.getChild(1).getText()) {
			case "*":operator="*";break;
			case "/":operator="/";break;
			
			//downgrade to single-element operations
			case ".*":operator="*";break;
			case "./":operator="/";break;
			case ".^":operator="**";break;
			
			default:{
				throw new UnsupportedOperationException();
			}
			}
			return template("binary_operator_expression")
					.add("expression0", ctx.multiplicative_expression().accept(this))
					.add("operator", operator)
					.add("expression1", ctx.unary_expression().accept(this));
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
						.add("stop", template("binary_operator_expression")
								.add("expression0", template("bracketed_expression").add("expression", ctx.or_expression().accept(this)))
								.add("operator", "+")
								.add("expression1", "1"));
		}
	}

	@Override
	public Fragment visitAssignment_expression(Assignment_expressionContext ctx) {
		return template("assignment_expression")
					.add("postfix_expression", ctx.postfix_expression().accept(this))
					.add("expression", ctx.expression().accept(this));
	}

	@Override
	public Fragment visitEostmt(EostmtContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitStatement(StatementContext ctx) {
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
			return template("comment")
						.add("text", ctx.COMMENT_STATEMENT().getText().substring(1));
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
		if(ctx.expression()==null) {
			//option expression_statement
			return ctx.expression_statement().accept(this);
		} else {
			//option expression
			return ctx.expression().accept(this);
		}
	}

	@Override
	public Fragment visitArray_list(Array_listContext ctx) {
		if(ctx.array_list()==null) {
			//option array_element
			return ctx.array_element().accept(this);
		} else {
			//option array_list array_element
			Fragment element=ctx.array_element().accept(this);
			if(element==null) {
				return ctx.array_list().accept(this);
			}
			return template("comma_separated")
						.add("list", ctx.array_list().accept(this))
						.add("element", element);
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
			return ctx.statement_list().accept(this);
		} else {
			//option: FUNCTION function_declare eostmt statement_list
		}
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunc_ident_list(Func_ident_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunc_return_list(Func_return_listContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunction_declare_lhs(Function_declare_lhsContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitFunction_declare(Function_declareContext ctx) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Fragment visitHold_statement(Hold_statementContext ctx) {
		return template("comment").add("text", ctx.getText());
	}

	@Override
	public Fragment visitLambda_definition(Lambda_definitionContext ctx) {
		//'@(' index_expression_list ')' expression
		return template("lambda")
					.add("args", ctx.index_expression_list().accept(this))
					.add("expression", ctx.expression().accept(this));
	}
}
