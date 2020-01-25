package org.hzh.mybatis.listener;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ParamTokenRewriter extends TokenStreamRewriter {

	public ParamTokenRewriter(TokenStream tokens) {
		super(tokens);
	}

	public void delete(ParserRuleContext ctx) {
		super.delete(ctx.start, ctx.stop);
	}

	public void delete(TerminalNode node) {
		super.delete(node.getSymbol());
	}

	public void replace(ParserRuleContext ctx, String content) {
		super.replace(ctx.start, ctx.stop, content);
	}

	public void replaceWithQuestionMark(ParserRuleContext ctx) {
		replace(ctx, "?");
	}

}
