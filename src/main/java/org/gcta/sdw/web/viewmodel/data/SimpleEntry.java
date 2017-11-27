package org.gcta.sdw.web.viewmodel.data;

public class SimpleEntry<V, W> {
	V value0;
	W value1;

	public SimpleEntry(V value0, W value1) {
		super();
		this.value0 = value0;
		this.value1 = value1;
	}

	public V getValue0() {
		return value0;
	}

	public void setValue0(V value0) {
		this.value0 = value0;
	}

	public W getValue1() {
		return value1;
	}

	public void setValue1(W value1) {
		this.value1 = value1;
	}

	@Override
	public int hashCode() {
		int hash = value0.hashCode() + value1.hashCode();
		return hash;
	}

	@Override
	public boolean equals(Object object) {
		// TODO: Warning - this method won't work in the case the id fields are
		// not set
		if (!(object instanceof SimpleEntry)) {
			return false;
		}
		SimpleEntry<?, ?> other = (SimpleEntry<?, ?>) object;
		if (((this.value0 == null && other.value0 == null) || (this.value1 == null && other.value1 == null))

				|| (this.value0 != other.value0 || this.value1 != other.value1)) {
			return false;
		}
		return true;
	}

}
