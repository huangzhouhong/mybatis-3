package org.hzh.mybatis.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.parse.ANTLRParser.option_return;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.hzh.mybatis.expression.PropertyUtils;
import org.hzh.mybatis.parser.MySqlBaseListener;
import org.hzh.mybatis.parser.MySqlParser.BetweenPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.BinaryComparasionPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.ExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.FromClauseContext;
import org.hzh.mybatis.parser.MySqlParser.InPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.LikePredicateContext;
import org.hzh.mybatis.parser.MySqlParser.LimitClauseContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalOperatorContext;
import org.hzh.mybatis.parser.MySqlParser.ParamContext;
import org.hzh.mybatis.parser.MySqlParser.PredicateExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.RegexpPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.SoundsLikePredicateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlProcessorListener extends MySqlBaseListener {
	private static final Logger logger = LoggerFactory.getLogger(SqlProcessorListener.class);

	private TokenStreamRewriter rewriter;
	private Object param;
	private Set<Object> deletedExprs = new HashSet<>();

	List<Object> paramList = new ArrayList<>();

	public SqlProcessorListener(TokenStreamRewriter rewriter, Object param) {
		this.rewriter = rewriter;
		this.param = param;
	}

	public List<Object> getParamList() {
		return paramList;
	}

	@Override
	public void enterBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
		processWhereSimpleParamContext(ctx.right.param());
//		Object value = getWherePartParamValue(ctx.right.param());
//		if (value != null) {
//			rewriter.replace(ctx.right.start, ctx.right.stop, "?");
//			paramList.add(value);
//		}
	}

	@Override
	public void enterInPredicate(InPredicateContext ctx) {
		ParamContext paramCtx = ctx.param();
		Object value = getWherePartParamValue(paramCtx);
		if (value != null) {
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

	@Override
	public void enterBetweenPredicate(BetweenPredicateContext ctx) {
		Object value1 = getWherePartParamValue(ctx.p1.param());
		Object value2 = getWherePartParamValue(ctx.p2.param());
		if (value1 != null && value2 != null) {
			rewriter.replace(ctx.p1.start, ctx.p1.stop, "?");
			rewriter.replace(ctx.p2.start, ctx.p2.stop, "?");
			paramList.add(value1);
			paramList.add(value2);
		}
	}

	@Override
	public void enterSoundsLikePredicate(SoundsLikePredicateContext ctx) {
		processWhereSimpleParamContext(ctx.predicateOrParam().param());
	}

	@Override
	public void enterLikePredicate(LikePredicateContext ctx) {
		processWhereSimpleParamContext(ctx.predicateOrParam().param());
	}

	@Override
	public void enterRegexpPredicate(RegexpPredicateContext ctx) {
		processWhereSimpleParamContext(ctx.predicateOrParam().param());
	}

	@Override
	public void enterLimitClause(LimitClauseContext ctx) {
		ParamContext limitParamCtx = ctx.limit.param();
		Object limitValue = getParamValue(limitParamCtx, () -> rewriter.delete(ctx.start, ctx.stop));
		if (ctx.offset == null) {
			if (limitValue != null) {
				rewriter.replace(ctx.limit.start, ctx.limit.stop, "?");
				paramList.add(limitValue);
			}
		} else {
			boolean offsetFirst = ctx.OFFSET() == null;
			Object offsetValue = getParamValue(ctx.offset.param(), () -> {
				if (offsetFirst) {
					rewriter.delete(ctx.op);
				} else {
					rewriter.delete(ctx.OFFSET().getSymbol());
				}
				rewriter.delete(ctx.offset.start, ctx.offset.stop);
			});

			List<Object> limitParamValueList = new ArrayList<>(2);
			if (limitValue != null) {
				rewriter.replace(ctx.limit.start, ctx.limit.stop, "?");
				limitParamValueList.add(limitValue);
			}
			if (offsetValue != null) {
				rewriter.replace(ctx.offset.start, ctx.offset.stop, "?");
				int offsetValueIndex = offsetFirst ? 0 : 1;
				limitParamValueList.add(offsetValueIndex, offsetValue);
			}
			paramList.addAll(limitParamValueList);
		}
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

	private void processWhereSimpleParamContext(ParamContext paramCtx) {
		Object value = getWherePartParamValue(paramCtx);
		if (value != null) {
			rewriter.replace(paramCtx.start, paramCtx.stop, "?");
			paramList.add(value);
		}
	}

	private Object getWherePartParamValue(ParamContext paramCtx) {
		return getParamValue(paramCtx, () -> deleteWherePart(paramCtx));
	}

	// try get param
	// if ParamContext not null(defined) and value null,call delete callback
	private Object getParamValue(ParamContext paramCtx, DeleteOperation op) {
		Object value = null;
		if (paramCtx != null) {
			boolean required = paramCtx.PARAM_PREFIX().getText().equals("#");
			String paramName = paramCtx.paramName().getText();
			value = getExpressionValue(paramName);
			if (value == null) {
				if (required) {
					throw new RuntimeException("param " + paramName + " required");
				}
				op.delete();
			}
		}
		return value;
	}

	interface DeleteOperation {
		void delete();
	}

	private void deleteWherePart(ParserRuleContext ctx) {
		ParserRuleContext toDeleteCtx = ctx;
		while (toDeleteCtx.parent != null) {
			if (toDeleteCtx.parent instanceof LogicalExpressionContext) {
				LogicalExpressionContext logicalExpressionContext = (LogicalExpressionContext) toDeleteCtx.parent;
				LogicalOperatorContext opCtx = logicalExpressionContext.logicalOperator();
				rewriter.delete(opCtx.start, opCtx.stop);
				directDeleteExpr(toDeleteCtx);
				break;
			} else if (toDeleteCtx.parent instanceof FromClauseContext) {
				directDeleteExpr(toDeleteCtx);
				break;
			}
			toDeleteCtx = (ParserRuleContext) toDeleteCtx.parent;
		}
	}

	private void directDeleteExpr(ParserRuleContext ctx) {
		rewriter.delete(ctx.start, ctx.stop);
		deletedExprs.add(ctx);
		logger.debug("delete " + ctx.getClass());
	}

	private Object getExpressionValue(String expr) {
		return PropertyUtils.getExpression(param, expr);
	}

}
