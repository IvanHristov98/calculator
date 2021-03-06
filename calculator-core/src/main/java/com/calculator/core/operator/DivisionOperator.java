package com.calculator.core.operator;

import com.calculator.core.exception.*;

public final class DivisionOperator extends ArithmeticOperator {
	protected static final int PRIORITY = 1;

	{
		symbolicRepresentation = Operators.DIVISION;
	}

	@Override
	public int getPriority() {
		return DivisionOperator.PRIORITY;
	}

	@Override
	public boolean isLeftAssociative() {
		return true;
	}

	@Override
	public Double operate(Double leftNumber, Double rightNumber) throws CalculatorException {
		if (rightNumber == 0) {
			throw new DivisionByZeroException("Division by zero is forbidden.");
		}

		return leftNumber / rightNumber;
	}
}
