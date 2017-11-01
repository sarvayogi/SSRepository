package org.pn.ss;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:application.properties")
public class AppProperties {

	@Value("${neo4j.uri}")
	private String neo4jUrl;
	@Value("${neo4j.username}")
	private String neo4jUserName;
	@Value("${neo4j.password}")
	private String neo4jPassword;
	@Value("${encrypt.key}")
	private String  encryptKey;
	public String getNeo4jUrl() {
		return neo4jUrl;
	}
	public void setNeo4jUrl(String neo4jUrl) {
		this.neo4jUrl = neo4jUrl;
	}
	public String getNeo4jUserName() {
		return neo4jUserName;
	}
	public void setNeo4jUserName(String neo4jUserName) {
		this.neo4jUserName = neo4jUserName;
	}
	public String getNeo4jPassword() {
		return neo4jPassword;
	}
	public void setNeo4jPassword(String neo4jPassword) {
		this.neo4jPassword = neo4jPassword;
	}
	public String getEncryptKey() {
		return encryptKey;
	}
	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey;
	}
	@Override
	public String toString() {
		return "AppProperties [neo4jUrl=" + neo4jUrl + ", neo4jUserName=" + neo4jUserName + ", neo4jPassword="
				+ neo4jPassword + ", encryptKey=" + encryptKey + "]";
	}

	
}
