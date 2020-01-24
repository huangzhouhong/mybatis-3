package org.hzh.mybatis.antlr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.misc.Interval;

public class AntlrUtils {
	public static String getText(ParserRuleContext ctx, TokenStream tokenStream) {
		int a = ctx.start.getStartIndex();
		int b = ctx.stop.getStopIndex();
		Interval interval = new Interval(a, b);
		return tokenStream.getText(interval);
	}
}
