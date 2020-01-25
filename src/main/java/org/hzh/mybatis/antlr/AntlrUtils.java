package org.hzh.mybatis.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;

public class AntlrUtils {
	public static String getText(ParserRuleContext ctx, TokenStream tokenStream) {
		int a = ctx.start.getStartIndex();
		int b = ctx.stop.getStopIndex();
		Interval interval = new Interval(a, b);
		return tokenStream.getText(interval);
	}

	public static <T extends ParserRuleContext> List<T> getDecendants(ParserRuleContext ctx, Class<T> clazz) {
		List<T> list = new ArrayList<T>();
		getDecendants(ctx, list, clazz);
		return list;
	}

	@SuppressWarnings("unchecked")
	private static <T extends ParserRuleContext> void getDecendants(ParserRuleContext ctx, List<T> list,
			Class<T> clazz) {
		if (ctx == null) {
			return;
		}
		for (ParseTree child : ctx.children) {
			if (clazz.isAssignableFrom(child.getClass())) {
				list.add((T) child);
			} else if (child instanceof ParserRuleContext) {
				getDecendants((ParserRuleContext) child, list, clazz);
			}
		}
	}
}
