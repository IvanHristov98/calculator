package com.calculator.core;

import com.calculator.core.exception.*;

public interface IExpressionFilter {
	public abstract Expression process(Expression expression) throws CalculatorException;
}
