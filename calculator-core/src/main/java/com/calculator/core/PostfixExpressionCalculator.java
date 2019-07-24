package com.calculator.core;

import com.calculator.core.exception.CalculatorException;
import com.calculator.core.exception.InvalidOperatorException;
import com.calculator.core.exception.NumberMisplacementException;
import com.calculator.core.operator.ArithmeticOperator;
import com.calculator.core.operator.OperatorChecker;
import com.calculator.core.operator.OperatorFactory;

import java.util.EmptyStackException;
import java.util.Stack;

public class PostfixExpressionCalculator extends ExpressionContainer
{
    public PostfixExpressionCalculator(Expression expression)
    {
        super(expression);
    }

    public Expression process () throws CalculatorException
    {
        Stack<Double> numbers = new Stack<>();

        try
        {
            numbers = this.getPostfixExpressionValue(numbers);
        }
        catch (EmptyStackException exception)
        {
            throw new NumberMisplacementException("Invalid number of numbers has been received.");
        }

        if (numbers.size() > 1)
        {
            throw new NumberMisplacementException("Invalid number of numbers has been received.");
        }

        return new Expression(numbers.peek().toString());
    }

    private Stack<Double> getPostfixExpressionValue(Stack<Double> numbers) throws CalculatorException
    {
        for (String token : this.expression.getTokens())
        {
            if (NumberValidator.isNumber(token))
            {
                numbers = this.addNumberToNumbersStack(numbers, token);
            }
            else if (OperatorChecker.isArithmeticOperator(OperatorFactory.makeOperator(token)))
            {
                numbers = this.operateWithNumbersFromStackTop(numbers, token);
            }
            else
            {
                throw new InvalidOperatorException("Unexpected operator has been received.");
            }
        }

        return numbers;
    }

    private Stack<Double> addNumberToNumbersStack(Stack<Double> numbers, String token)
    {
        numbers.add(NumberValidator.toNumber(token));
        return numbers;
    }

    private Stack<Double> operateWithNumbersFromStackTop(Stack<Double> numbers, String token) throws CalculatorException
    {
        Double rightNumber = numbers.pop();
        Double leftNumber = numbers.pop();
        ArithmeticOperator operator = (ArithmeticOperator) OperatorFactory.makeOperator(token);

        Double operationResult = operator.operate(leftNumber, rightNumber);
        numbers.push(operationResult);

        return numbers;
    }
}