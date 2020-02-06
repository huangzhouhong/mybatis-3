/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
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
