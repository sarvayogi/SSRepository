package org.pn.ss.model;

import java.util.Map;

import javax.validation.constraints.NotNull;

public class Node {

	@NotNull
	private String nodeName;
	@NotNull
	private String aggrementType;
	private int versionNo = 0;
	private boolean isLatest = true ;
	private String  lastUpdatedDate ;
	private String createdDate;
	
	Map<String, String> attributes ;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getAggrementType() {
		return aggrementType;
	}

	public void setAggrementType(String aggrementType) {
		this.aggrementType = aggrementType;
	}

	public int getVersionNo() {
		return versionNo;
	}

	public void setVersionNo(int versionNo) {
		this.versionNo = versionNo;
	}

	public boolean isLatest() {
		return isLatest;
	}

	public void setLatest(boolean isLatest) {
		this.isLatest = isLatest;
	}

	public String getLastUpdatedDate() {
		return lastUpdatedDate;
	}

	public void setLastUpdatedDate(String lastUpdatedDate) {
		this.lastUpdatedDate = lastUpdatedDate;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}

	@Override
	public String toString() {
		return "Node [nodeName=" + nodeName + ", aggrementType=" + aggrementType + ", versionNo=" + versionNo
				+ ", isLatest=" + isLatest + ", lastUpdatedDate=" + lastUpdatedDate + ", createdDate=" + createdDate
				+ ", attributes=" + attributes + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((aggrementType == null) ? 0 : aggrementType.hashCode());
		result = prime * result + (isLatest ? 1231 : 1237);
		result = prime * result + ((nodeName == null) ? 0 : nodeName.hashCode());
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
		Node other = (Node) obj;
		if (aggrementType == null) {
			if (other.aggrementType != null)
				return false;
		} else if (!aggrementType.equals(other.aggrementType))
			return false;
		if (isLatest != other.isLatest)
			return false;
		if (nodeName == null) {
			if (other.nodeName != null)
				return false;
		} else if (!nodeName.equals(other.nodeName))
			return false;
		return true;
	}
	
	
	
	
	

	

}
