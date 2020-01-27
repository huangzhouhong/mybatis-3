package org.hzh.mybatis.listener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.hzh.mybatis.antlr.AntlrUtils;
import org.hzh.mybatis.expression.PropertyUtils;
import org.hzh.mybatis.parser.MySqlBaseListener;
import org.hzh.mybatis.parser.MySqlParser.BetweenPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.BinaryComparasionPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.ConcatInItemsContext;
import org.hzh.mybatis.parser.MySqlParser.ConcatUpdatedElementsContext;
import org.hzh.mybatis.parser.MySqlParser.ExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.ExpressionOrDefaultContext;
import org.hzh.mybatis.parser.MySqlParser.FromClauseContext;
import org.hzh.mybatis.parser.MySqlParser.InItemContext;
import org.hzh.mybatis.parser.MySqlParser.InPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.InsertValueListContext;
import org.hzh.mybatis.parser.MySqlParser.LikePredicateContext;
import org.hzh.mybatis.parser.MySqlParser.LimitClauseContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalExpressionContext;
import org.hzh.mybatis.parser.MySqlParser.LogicalOperatorContext;
import org.hzh.mybatis.parser.MySqlParser.ParamContext;
import org.hzh.mybatis.parser.MySqlParser.RegexpPredicateContext;
import org.hzh.mybatis.parser.MySqlParser.SoundsLikePredicateContext;
import org.hzh.mybatis.parser.MySqlParser.UpdatedElementContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqlProcessorListener extends MySqlBaseListener {
	private static final Logger logger = LoggerFactory.getLogger(SqlProcessorListener.class);

	private ParamTokenRewriter rewriter;
	private TokenStream tokentStream;
	private Object param;
	private Set<Object> deletedExprs = new HashSet<>();

	List<Object> paramList = new ArrayList<>();

	public SqlProcessorListener(TokenStream tokentStream, Object param) {
		this.rewriter = new ParamTokenRewriter(tokentStream);
		this.tokentStream = tokentStream;
		this.param = param;
	}

	public String getText() {
		return rewriter.getText();
	}

	public List<Object> getParamList() {
		return paramList;
	}

	@Override
	public void enterBinaryComparasionPredicate(BinaryComparasionPredicateContext ctx) {
		processWhereSimpleParamContext(ctx.right.param());
	}

	@Override
	public void enterInItem(InItemContext ctx) {
		ParamHandler paramHandler = new ParamHandler(ctx.param());
		Object value = paramHandler.value;
		if (paramHandler.hasNullValue()) {
			paramHandler.delete();
			if (ctx.parent != null && ctx.parent.parent instanceof ConcatInItemsContext) {
				ConcatInItemsContext parent = (ConcatInItemsContext) ctx.parent.parent;
				rewriter.delete(parent.comma);
			}
			deletedExprs.add(paramHandler.context);
		} else if (value != null) {
			List<Object> items = new ArrayList<>();
			if (value instanceof Collection) {
				items.addAll((Collection<?>) value);
			} else {
				items.add(value);
			}
			String questionMarks = String.join(",", Collections.nCopies(items.size(), "?"));

			rewriter.replace(ctx, questionMarks);
			for (Object object : items) {
				paramList.add(object);
			}
		}
	}

	@Override
	public void exitInPredicate(InPredicateContext ctx) {
		List<InItemContext> itemContexts = AntlrUtils.getDecendants(ctx, InItemContext.class);

		boolean delete = true;
		for (InItemContext inItemContext : itemContexts) {
			if (inItemContext.param() == null || !deletedExprs.contains(inItemContext.param())) {
				delete = false;
				break;
			}
		}
		if (delete) {
			deleteWherePart(ctx);
		}
	}

	@Override
	public void enterBetweenPredicate(BetweenPredicateContext ctx) {
		ParamHandler handler1 = new ParamHandler(ctx.p1.param());
		if (handler1.hasNullValue()) {
			deleteWherePart(ctx);
			return;
		}
		ParamHandler handler2 = new ParamHandler(ctx.p2.param());
		if (handler2.hasNullValue()) {
			deleteWherePart(ctx);
			return;
		}
		handler1.replaceParamContextWithValue();
		handler2.replaceParamContextWithValue();
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
		ParamHandler limitHandler = new ParamHandler(ctx.limit.param());
		if (limitHandler.hasNullValue()) {
			rewriter.delete(ctx);
			return;
		}
		if (ctx.offset == null) {
			if (limitHandler.value != null) {
				limitHandler.replaceParamContextWithValue();
			}
		} else {
			ParamHandler offsetHandler = new ParamHandler(ctx.offset.param());
			boolean offsetFirst = ctx.OFFSET() == null;
			if (offsetHandler.hasNullValue()) {
				if (offsetFirst) {
					rewriter.delete(ctx.op);
				} else {
					rewriter.delete(ctx.OFFSET());
				}
				rewriter.delete(ctx.offset);
			}

			// `offset,limit` or `limit OFFSET offset`
			List<Object> limitParamValueList = new ArrayList<>(2);
			if (limitHandler.value != null) {
				rewriter.replaceWithQuestionMark(ctx.limit);
				limitParamValueList.add(limitHandler.value);
			}
			if (offsetHandler.value != null) {
				rewriter.replaceWithQuestionMark(ctx.offset);
				int offsetValueIndex = offsetFirst ? 0 : 1;
				limitParamValueList.add(offsetValueIndex, offsetHandler.value);
			}
			paramList.addAll(limitParamValueList);
		}
	}

	@Override
	public void enterUpdatedElement(UpdatedElementContext ctx) {
		ParamHandler paramHandler = new ParamHandler(ctx.param(), false);
		if (paramHandler.hasNullValue()) {
			if (paramHandler.required) {
				paramHandler.replaceParamContextWithValue();
			} else {
				rewriter.delete(ctx);
				if (ctx.parent != null && ctx.parent.parent instanceof ConcatUpdatedElementsContext) {
					ConcatUpdatedElementsContext parent = (ConcatUpdatedElementsContext) ctx.parent.parent;
					rewriter.delete(parent.comma);
				}
			}
		}
		if (paramHandler.value != null) {
			paramHandler.replaceParamContextWithValue();
		}
	}

	@Override
	public void enterInsertValueList(InsertValueListContext ctx) {
		List<ExpressionOrDefaultContext> expressionOrDefaultContexts = ctx.expressionOrDefault();

		List<String> paramDefineList = new ArrayList<>(expressionOrDefaultContexts.size());
		List<Object> valueList = new ArrayList<Object>(expressionOrDefaultContexts.size());
		for (ExpressionOrDefaultContext exprCtx : expressionOrDefaultContexts) {
			ParamContext paramCtx = exprCtx.param();
			Object value = null;
			if (paramCtx != null) {
				boolean required = paramCtx.PARAM_PREFIX().getText().equals("#");
				String paramName = paramCtx.paramName().getText();
				if (!required) {
					throw new RuntimeException(paramName + " : insert value must not be optional (#?)");
				}
				value = getExpressionValue(paramName);
				paramDefineList.add("?");
				valueList.add(value);
			} else {
				String exprText = AntlrUtils.getText(exprCtx, tokentStream);
				paramDefineList.add(exprText);
			}
		}

		// get entity count
		Integer entityCount = null;
		for (Object value : valueList) {
			if (value != null) {
				if (value instanceof Collection) {
					int count = ((Collection<?>) value).size();
					if (entityCount != null && count != entityCount) {
						throw new RuntimeException("values for insert size inconsistent");
					}
					entityCount = count;
				} else {
					entityCount = 1;
				}
			}
		}
		if (entityCount == null) {
			entityCount = 1;
		}

		String itemDefine = "(" + String.join(",", paramDefineList) + ")";
		String valueDefines = String.join(",", Collections.nCopies(entityCount, itemDefine));
		rewriter.replace(ctx.start, ctx.stop, valueDefines);

		for (int i = 0; i < entityCount; i++) {
			for (Object value : valueList) {
				if (value instanceof Collection) {
					paramList.add(PropertyUtils.getByIndex(value, i));
				} else {
					paramList.add(value);
				}
			}
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
		if (paramCtx != null) {
			ParamHandler paramHandler = new ParamHandler(paramCtx);
			if (paramHandler.value == null) {
				deleteWherePart(paramCtx);
			} else {
				paramHandler.replaceParamContextWithValue();
			}
		}
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

	class ParamHandler {
		ParamContext context;
		boolean defined;
		boolean required;
		String paramName;
		Object value;

		ParamHandler(ParamContext context) {
			this(context, true);
		}

		ParamHandler(ParamContext context, boolean checkRequire) {
			this.context = context;
			if (context != null) {
				defined = true;
				required = context.PARAM_PREFIX().getText().equals("#");
				paramName = context.paramName().getText();
				value = PropertyUtils.getExpression(param, paramName);
				if (checkRequire) {
					checkRequire();
				}
			}
		}

		public boolean hasNullValue() {
			return defined && value == null;
		}

		public void replaceParamContextWithValue() {
			if (context != null) {
				rewriter.replaceWithQuestionMark(context);
				paramList.add(value);
			}
		}

		public void delete() {
			if (context != null) {
				rewriter.delete(context);
			}
		}

		private void checkRequire() {
			assert context != null;
			if (value == null && required) {
				throw new RuntimeException("param " + paramName + " required");
			}
		}
	}

}
