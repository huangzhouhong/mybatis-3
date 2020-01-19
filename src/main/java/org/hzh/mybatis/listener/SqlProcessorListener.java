package org.hzh.mybatis.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.hzh.mybatis.expression.PropertyUtils;
import org.hzh.mybatis.parser.MySqlBaseListener;
import org.hzh.mybatis.parser.MySqlParser.BinaryComparasionPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.ExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.FromClauseContext;
import org.hzh.mybatis.parser.MySqlParser.InPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalOperatorContext;
import org.hzh.mybatis.parser.MySqlParser.ParamContext;
import org.hzh.mybatis.parser.MySqlParser.PredicateExpressionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlProcessorListener extends MySqlBaseListener {
	private static final Logger logger = LoggerFactory.getLogger(SqlProcessorListener.class);

	private TokenStreamRewriter rewriter;
	private Object param;
	private Set<Object> deletedExprs = new HashSet<>();

//	List<String> paramNameList = new ArrayList<>();
	List<Object> paramList = new ArrayList<>();

	public SqlProcessorListener(TokenStreamRewriter rewriter, Object param) {
		this.rewriter = rewriter;
		this.param = param;
	}

	public List<Object> getParamList() {
		return paramList;
	}

	private Object getExpressionValue(String expr) {
		return PropertyUtils.getExpression(param, expr);
	}

	private Object getWherePartParamValue(ParamContext paramCtx) {
		Object value = null;
		if (paramCtx != null) {
			boolean required = paramCtx.PARAM_PREFIX().getText().equals("#");
			String paramName = paramCtx.paramName().getText();
			value = getExpressionValue(paramName);
			if (value == null) {
				if (required) {
					throw new RuntimeException("param " + paramName + " required");
				}
				deleteWherePart(paramCtx);
			}
		}
		return value;
	}

	@Override
	public void enterBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
		ParamContext paramCtx = ctx.right.param();
		if (paramCtx != null) {
			boolean required = paramCtx.PARAM_PREFIX().getText().equals("#");
			String paramName = paramCtx.paramName().getText();
			Object value = getExpressionValue(paramName);
			if (value == null) {
				if (required) {
					throw new RuntimeException("param " + paramName + " required");
				}
				deleteWherePart(ctx);
			} else {
				rewriter.replace(ctx.right.start, ctx.right.stop, "?");
//				paramNameList.add(paramName);
				paramList.add(value);
			}
		}
	}

	@Override
	public void enterInPredicate(InPredicateContext ctx) {
		ParamContext paramCtx = ctx.param();
		if (paramCtx != null) {
			boolean required = paramCtx.PARAM_PREFIX().getText().equals("#");
			String paramName = paramCtx.paramName().getText();
			Object value = getExpressionValue(paramName);
			if (value == null) {
				if (required) {
					throw new RuntimeException("param " + paramName + " required");
				}
				deleteWherePart(ctx);
			} else {
				List<Object> items = new ArrayList<>();
				if (value instanceof Collection) {
					items.addAll((Collection<?>) value);
				} else {
					items.add(value);
				}
				String questionMarks = String.join(",", Collections.nCopies(items.size(), "?"));
				if (ctx.leftBracket == null) {
					questionMarks = "(" + questionMarks;
				}
				if (ctx.rightBracket == null) {
					questionMarks = questionMarks + ")";
				}
				rewriter.replace(paramCtx.start, paramCtx.stop, questionMarks);
				for (Object object : items) {
					paramList.add(object);
				}
			}
		}
	}

	/**
	 * find ancestor for LogicalExpressionContext delete the expression under
	 * LogicalExpressionContext which is ancestor delete logicalOperator
	 * 
	 * @param ctx
	 */
	private void deleteWherePart(ParserRuleContext ctx) {
		// find child under `LogicalExpressionContext` which if ancestor of `ctx`
		ParserRuleContext underLogicalCtx = ctx;
		while (underLogicalCtx.parent != null && !(underLogicalCtx.parent instanceof LogicalExpressionContext)) {
			underLogicalCtx = (ParserRuleContext) underLogicalCtx.parent;
		}

		if (underLogicalCtx.parent instanceof LogicalExpressionContext) {
			LogicalExpressionContext logicalExpressionContext = (LogicalExpressionContext) underLogicalCtx.parent;
			LogicalOperatorContext opCtx = logicalExpressionContext.logicalOperator();
			rewriter.delete(opCtx.start, opCtx.stop);
			directDeleteExpr(underLogicalCtx);
		} else if (ctx.parent instanceof PredicateExpressionContext) {
			directDeleteExpr((ParserRuleContext) ctx.parent);
		} else {
			directDeleteExpr(ctx);
		}
	}

	private void directDeleteExpr(ParserRuleContext ctx) {
		rewriter.delete(ctx.start, ctx.stop);
		deletedExprs.add(ctx);
		logger.debug("delete " + ctx.getClass());
	}

	@Override
	public void exitLogicalExpression(LogicalExpressionContext ctx) {
		// if two expr deleted,find ancestor `LogicalExpressionContext` and delete
		// `logicalOperator`
		List<ExpressionContext> exprList = ctx.expression();
		assert exprList.size() == 2;
		ExpressionContext expr1 = exprList.get(0);
		ExpressionContext expr2 = exprList.get(1);
		if (deletedExprs.contains(expr1) && deletedExprs.contains(expr2)) {
			deleteWherePart(ctx);
		}
	}

	@Override
	public void exitFromClause(FromClauseContext ctx) {
		ExpressionContext whereExpr = ctx.whereExpr;
		if (deletedExprs.contains(whereExpr)) {
			rewriter.delete(ctx.WHERE().getSymbol());
		}
	}

}
