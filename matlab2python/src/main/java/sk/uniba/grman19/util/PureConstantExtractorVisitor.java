package sk.uniba.grman19.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

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
import sk.uniba.grman19.MatlabParser.Otherwise_caseContext;
import sk.uniba.grman19.MatlabParser.Postfix_expressionContext;
import sk.uniba.grman19.MatlabParser.Primary_expressionContext;
import sk.uniba.grman19.MatlabParser.Relational_expressionContext;
import sk.uniba.grman19.MatlabParser.Selection_statementContext;
import sk.uniba.grman19.MatlabParser.StatementContext;
import sk.uniba.grman19.MatlabParser.Statement_listContext;
import sk.uniba.grman19.MatlabParser.Switch_caseContext;
import sk.uniba.grman19.MatlabParser.Switch_statementContext;
import sk.uniba.grman19.MatlabParser.Translation_unitContext;
import sk.uniba.grman19.MatlabParser.Unary_expressionContext;
import sk.uniba.grman19.MatlabParser.Unary_operatorContext;
import sk.uniba.grman19.MatlabVisitor;

public class PureConstantExtractorVisitor implements MatlabVisitor<Optional<TerminalNode>> {	
	private static final Optional<TerminalNode> NONE=Optional.empty();
	private static final Predicate<ParserRuleContext> SINGLE_CHILD = prc->prc.getChildCount()==1;
	
	public static final PureConstantExtractorVisitor PCEV=new PureConstantExtractorVisitor();
	
	private final Function<ParserRuleContext, Optional<TerminalNode>> VISIT = prc->prc.accept(this);
	
	private <A extends ParserRuleContext,B extends ParserRuleContext> Optional<TerminalNode> checkAndPass(A prc, Function<A, B> next){
		return Optional.of(prc).filter(SINGLE_CHILD).map(next).flatMap(VISIT);
	}

	@Override
	public Optional<TerminalNode> visit(ParseTree tree) {
		return NONE;
	}
	
	@Override
	public Optional<TerminalNode> visitChildren(RuleNode node) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitTerminal(TerminalNode node) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitErrorNode(ErrorNode node) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitPrimary_expression(Primary_expressionContext ctx) {
		return Optional.ofNullable(ctx.CONSTANT());
	}

	@Override
	public Optional<TerminalNode> visitPostfix_expression(Postfix_expressionContext ctx) {
		return checkAndPass(ctx, Postfix_expressionContext::primary_expression);
	}

	@Override
	public Optional<TerminalNode> visitIndex_expression(Index_expressionContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitIndex_expression_list(Index_expression_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitArray_expression(Array_expressionContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitUnary_expression(Unary_expressionContext ctx) {
		return checkAndPass(ctx, Unary_expressionContext::postfix_expression);
	}

	@Override
	public Optional<TerminalNode> visitUnary_operator(Unary_operatorContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitArray_mul_expression(Array_mul_expressionContext ctx) {
		return checkAndPass(ctx, Array_mul_expressionContext::unary_expression);
	}

	@Override
	public Optional<TerminalNode> visitMultiplicative_expression(Multiplicative_expressionContext ctx) {
		return checkAndPass(ctx, Multiplicative_expressionContext::array_mul_expression);
	}

	@Override
	public Optional<TerminalNode> visitAdditive_expression(Additive_expressionContext ctx) {
		return checkAndPass(ctx, Additive_expressionContext::multiplicative_expression);
	}

	@Override
	public Optional<TerminalNode> visitRelational_expression(Relational_expressionContext ctx) {
		return checkAndPass(ctx, Relational_expressionContext::additive_expression);
	}

	@Override
	public Optional<TerminalNode> visitEquality_expression(Equality_expressionContext ctx) {
		return checkAndPass(ctx, Equality_expressionContext::relational_expression);
	}

	@Override
	public Optional<TerminalNode> visitAnd_expression(And_expressionContext ctx) {
		return checkAndPass(ctx, And_expressionContext::equality_expression);
	}

	@Override
	public Optional<TerminalNode> visitOr_expression(Or_expressionContext ctx) {
		return checkAndPass(ctx, Or_expressionContext::and_expression);
	}

	@Override
	public Optional<TerminalNode> visitExpression(ExpressionContext ctx) {
		return checkAndPass(ctx, ExpressionContext::or_expression);
	}

	@Override
	public Optional<TerminalNode> visitLambda_definition(Lambda_definitionContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitAssignment_expression(Assignment_expressionContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitEostmt(EostmtContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitStatement(StatementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitStatement_list(Statement_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitIdentifier_list(Identifier_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitGlobal_statement(Global_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitClear_statement(Clear_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitClose_statement(Close_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitExpression_statement(Expression_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitAssignment_statement(Assignment_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitArray_element(Array_elementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitArray_sub_list(Array_sub_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitArray_list(Array_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitSelection_statement(Selection_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitSwitch_statement(Switch_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitSwitch_case(Switch_caseContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitOtherwise_case(Otherwise_caseContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitExpend(ExpendContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitElseif_clause(Elseif_clauseContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitIteration_statement(Iteration_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitJump_statement(Jump_statementContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitTranslation_unit(Translation_unitContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitFunc_ident_list(Func_ident_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitFunc_return_list(Func_return_listContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitFunction_declare_lhs(Function_declare_lhsContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitFunction_declare(Function_declareContext ctx) {
		return NONE;
	}

	@Override
	public Optional<TerminalNode> visitHold_statement(Hold_statementContext ctx) {
		return NONE;
	}
}
