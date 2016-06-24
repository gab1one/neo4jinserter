package de.einsdorf.neo4insert;

public class ReadNode {

	private String fieldID;
	private String nodelvl;

	public ReadNode(String nodelvl, String fieldID) {
		this.nodelvl = nodelvl;
		this.fieldID = fieldID;
	}

	public String getFieldID() {
		return fieldID;
	}

	public String getNodelvl() {
		return nodelvl;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fieldID == null) ? 0 : fieldID.hashCode());
		result = prime * result + ((nodelvl == null) ? 0 : nodelvl.hashCode());
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
		ReadNode other = (ReadNode) obj;
		if (fieldID == null) {
			if (other.fieldID != null)
				return false;
		} else if (!fieldID.equals(other.fieldID))
			return false;
		if (nodelvl == null) {
			if (other.nodelvl != null)
				return false;
		} else if (!nodelvl.equals(other.nodelvl))
			return false;
		return true;
	}

}
