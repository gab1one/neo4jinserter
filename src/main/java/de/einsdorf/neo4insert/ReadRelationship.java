package de.einsdorf.neo4insert;

public class ReadRelationship {

	private ReadNode node;
	private ReadNode parent;
	private double certainty;

	public ReadRelationship(ReadNode node, ReadNode parent, double certainty) {
		this.node = node;
		this.parent = parent;
		this.certainty = certainty;
	}

	public ReadNode getNode() {
		return node;
	}

	public ReadNode getParent() {
		return parent;
	}

	public double getCertainty() {
		return certainty;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(certainty);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((node == null) ? 0 : node.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReadRelationship other = (ReadRelationship) obj;
		if (Double.doubleToLongBits(certainty) != Double.doubleToLongBits(other.certainty))
			return false;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

}
