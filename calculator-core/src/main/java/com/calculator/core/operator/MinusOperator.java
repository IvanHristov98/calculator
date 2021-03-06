package com.calculator.core.operator;

public final class MinusOperator extends ArithmeticOperator {
	protected static final int PRIORITY = 0;

	{
		symbolicRepresentation = Operators.MINUS;
	}

	@Override
	public int getPriority() {
		return MinusOperator.PRIORITY;
	}

	@Override
	public boolean isLeftAssociative() {
		return true;
	}

	@Override
	public Double operate(Double leftNumber, Double rightNumber) {
		return leftNumber - rightNumber;
	}
}
