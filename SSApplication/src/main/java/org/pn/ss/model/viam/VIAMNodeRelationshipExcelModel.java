package org.pn.ss.model.viam;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.hibernate.validator.constraints.NotBlank;

public class VIAMNodeRelationshipExcelModel {
	
	@NotNull(message = "Source Node Name cannot be null")
	@NotBlank(message = "Source Node Name cannot be empty")
	private String sourceNodeName;
	
	@NotNull(message = "Source Aggrement Type cannot be null")
	@NotBlank(message = "Source Aggrement Type cannot be empty")
	private String sourceAggrementType;
	
	@NotNull(message = "Source IsPhysicalNode cannot be null")
	@NotBlank(message = "Source IsPhysicalNode Type cannot be empty")
	@Pattern(regexp = "yes|no|y|n", flags = Pattern.Flag.CASE_INSENSITIVE, message = " Source Node Physical flag should have values of yes or no or y or n")
	private String isSourceNodePhysical;

	@NotNull(message = "Target Node Name cannot be null")
	@NotBlank(message = "Target Node Name cannot be empty")
	private String targetNodeName;
	
	
	@NotNull(message = "Target Aggrement Type cannot be null")
	@NotBlank(message = "Target Aggrement Type cannot be empty")
	private String targetAggrementType;
	
	@NotNull(message = "Target IsPhysicalNode cannot be null")
	@NotBlank(message = "Target IsPhysicalNode Type cannot be empty")
	@Pattern(regexp = "yes|no|y|n", flags = Pattern.Flag.CASE_INSENSITIVE, message = " Target Node Physical flag should have values of yes or no or y or n")
	private String isTargetNodePhysical;
	
	@NotNull(message = "Action cannot be null")
	@NotBlank(message = "Action cannot be empty")
	@Pattern(regexp = "Insert|Update|Delete", flags = Pattern.Flag.CASE_INSENSITIVE, message = " Action should have values of Insert or Update or Delete")
	private String action;

	public String getSourceNodeName() {
		return sourceNodeName;
	}

	public void setSourceNodeName(String sourceNodeName) {
		this.sourceNodeName = sourceNodeName;
	}

	public String getSourceAggrementType() {
		return sourceAggrementType;
	}

	public void setSourceAggrementType(String sourceAggrementType) {
		this.sourceAggrementType = sourceAggrementType;
	}

	public String getIsSourceNodePhysical() {
		return isSourceNodePhysical;
	}

	public void setIsSourceNodePhysical(String isSourceNodePhysical) {
		this.isSourceNodePhysical = isSourceNodePhysical;
	}

	public String getTargetNodeName() {
		return targetNodeName;
	}

	public void setTargetNodeName(String targetNodeName) {
		this.targetNodeName = targetNodeName;
	}

	public String getTargetAggrementType() {
		return targetAggrementType;
	}

	public void setTargetAggrementType(String targetAggrementType) {
		this.targetAggrementType = targetAggrementType;
	}

	public String getIsTargetNodePhysical() {
		return isTargetNodePhysical;
	}

	public void setIsTargetNodePhysical(String isTargetNodePhysical) {
		this.isTargetNodePhysical = isTargetNodePhysical;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Override
	public String toString() {
		return "VIAMNodeRelationshipExcelModel [sourceNodeName=" + sourceNodeName + ", sourceAggrementType="
				+ sourceAggrementType + ", isSourceNodePhysical=" + isSourceNodePhysical + ", targetNodeName="
				+ targetNodeName + ", targetAggrementType=" + targetAggrementType + ", isTargetNodePhysical="
				+ isTargetNodePhysical + ", action=" + action + "]";
	}

	


	
	

}
