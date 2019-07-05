package com.calculator.exception;

public abstract class OperatorException extends CalculatorException
{
	private static final long serialVersionUID = -6753414124864400196L;
	
	private String problematicOperator;

	public OperatorException(String message, String problematicOperator) 
	{
		super(message);
		
		this.problematicOperator = problematicOperator;
	}
	
	public final String getProblematicOperator()
	{
		return this.problematicOperator;
	}
}
