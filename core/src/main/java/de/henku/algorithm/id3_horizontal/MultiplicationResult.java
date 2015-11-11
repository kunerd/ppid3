package de.henku.algorithm.id3_horizontal;

import java.math.BigInteger;

public class MultiplicationResult {
	private Object classValue;
	private BigInteger result;
	
	public MultiplicationResult(Object classValue, BigInteger result) {
		this.classValue = classValue;
		this.result = result;
	}
	
	public Object getClassValue() {
		return classValue;
	}
	
	public BigInteger getResult() {
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		MultiplicationResult other = (MultiplicationResult) obj;
		return classValue.equals(other.classValue)
				&& result.equals(other.result);
	}
}
