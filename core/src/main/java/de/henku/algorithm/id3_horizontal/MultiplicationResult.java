package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;

public class MultiplicationResult {
	private String classValue;
	private BigInteger result;
	
	public MultiplicationResult(String classValue, BigInteger result) {
		this.classValue = classValue;
		this.result = result;
	}
	
	public String getClassValue() {
		return classValue;
	}
	
	public BigInteger getResult() {
		return result;
	}
}
