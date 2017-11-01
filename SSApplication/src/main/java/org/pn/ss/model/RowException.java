package org.pn.ss.model;

import java.util.List;

public class RowException<T>{

	 T  inputObject ;
	 List<String> messages;
	public T getInputObject() {
		return inputObject;
	}
	public void setInputObject(T inputObject) {
		this.inputObject = inputObject;
	}
	public List<String> getMessages() {
		return messages;
	}
	public void setMessages(List<String> messages) {
		this.messages = messages;
	}
	@Override
	public String toString() {
		return "RowException [inputObject=" + inputObject + ", messages=" + messages + "]";
	}
	 
}
