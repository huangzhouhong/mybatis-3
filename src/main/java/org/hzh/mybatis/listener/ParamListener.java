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
