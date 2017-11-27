package org.gcta.sdw.web.viewmodel.data;

import org.gcta.sdw.persistence.entity.Metavariableenum;

public class VariableLevel {
	private Metavariableenum variable;
	private Long level;

	public VariableLevel(Metavariableenum variable, Long level) {
		this.variable = variable;
		this.level = level;
	}

	public Metavariableenum getVariable() {
		return variable;
	}

	public void setVariable(Metavariableenum variable) {
		this.variable = variable;
	}

	public Long getLevel() {
		return level;
	}

	public void setLevel(Long level) {
		this.level = level;
	}
}
