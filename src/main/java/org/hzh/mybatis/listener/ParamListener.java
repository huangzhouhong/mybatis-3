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

import java.util.HashSet;
import java.util.Set;

import org.hzh.mybatis.parser.MySqlBaseListener;
import org.hzh.mybatis.parser.MySqlParser.ParamContext;

public class ParamListener extends MySqlBaseListener {

	public static class ParamInfo {
		private String name;
		private boolean require;

		public ParamInfo(String name, boolean require) {
			this.name = name;
			this.require = require;
		}

		public String getName() {
			return name;
		}

		public boolean getRequire() {
			return require;
		}

		@Override
		public int hashCode() {
			return (name + require).hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj != null && obj instanceof ParamInfo) {
				ParamInfo otherInfo = (ParamInfo) obj;
				return otherInfo.name.equals(name) && otherInfo.require == require;
			}
			return false;
		}
	}

	Set<ParamInfo> paramInfos = new HashSet<>();

	public Set<ParamInfo> getParamInfos() {
		return paramInfos;
	}

	@Override
	public void enterParam(ParamContext ctx) {
		String name = ctx.paramName().getText();
		boolean require = ctx.PARAM_PREFIX().getText().equals("#");
		ParamInfo paramInfo = new ParamInfo(name, require);
		paramInfos.add(paramInfo);
	}
}
