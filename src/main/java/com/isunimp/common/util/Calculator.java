package com.isunimp.common.util;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * com.isunimp.common.util.Calculator class
 *
 * @author renguiquan
 * @date 2019/8/5
 */
public class Calculator {

    private String express;
    private static List<Character> CALCULATE_SYMBOL = Arrays.asList('+', '-', '*', '/');
    private static List<Character> NUMERICAL_SYMBOL = Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.');

    private void checkSymbol(LinkedList<Character> symbolStack, LinkedList<Double> numberStack) {
        if (symbolStack.size() == 0) return;
        char symbol = symbolStack.getFirst();
        double result;
        switch (symbol) {
            case '*':
                result = numberStack.removeFirst() * numberStack.removeFirst();
                break;
            case '/':
                Double denominator = numberStack.removeFirst();
                result = numberStack.removeFirst() / denominator;
                break;
            default:
                return;
        }
        symbolStack.removeFirst();
        numberStack.addFirst(result);
    }

    private void checkBuilder(LinkedList<Character> symbolStack, LinkedList<Double> numberStack, StringBuilder numberStringBuilder) {
        if (numberStringBuilder.length() == 0) return;
        Double number = Double.valueOf(numberStringBuilder.toString());
        numberStack.addFirst(number);
        checkSymbol(symbolStack, numberStack);
        numberStringBuilder.delete(0, numberStringBuilder.length());
    }

    public double calcImpl(String subExpress) {
        LinkedList<Character> symbolStack = new LinkedList<>();
        LinkedList<Double> numberStack = new LinkedList<>();
        StringBuilder numberStringBuilder = new StringBuilder();

        for (int idx = 0; idx < subExpress.length(); ++idx) {
            char c = subExpress.charAt(idx);
            if (CALCULATE_SYMBOL.contains(c)) {
                if (idx == 0) {
                    throw new IllegalArgumentException("Illegal expression");
                }
                checkBuilder(symbolStack, numberStack, numberStringBuilder);
                symbolStack.addFirst(c);
            } else if (NUMERICAL_SYMBOL.contains(c)) {
                numberStringBuilder.append(c);
            } else if ('=' == c) {
                checkBuilder(symbolStack, numberStack, numberStringBuilder);
            } else {
                throw new IllegalArgumentException("Illegal expression");
            }
        }
        checkBuilder(symbolStack, numberStack, numberStringBuilder);
        if (symbolStack.size() + 1 != numberStack.size())
            throw new IllegalArgumentException("Illegal expression");
        while (symbolStack.size() > 0) {
            double num;
            switch (symbolStack.removeLast()) {
                case '+':
                    num = numberStack.removeLast() + numberStack.removeLast();
                    break;
                case '-':
                    num = numberStack.removeLast() - numberStack.removeLast();
                    break;
                default:
                    throw new IllegalArgumentException("Illegal expression");
            }
            numberStack.addLast(num);
        }
        double result = numberStack.removeFirst();
        System.out.println(subExpress + "=" + result);
        return result;
    }

    private String pre(int flag) {
        String result;
        for (int idx = flag; idx < express.length(); ) {
            if ('(' == express.charAt(idx)) {
                String subExpress = pre(idx + 1);
                result = Double.toString(calcImpl(subExpress));
                express = express.replace("(" + subExpress + ")", result);
                idx += result.length();
            } else if (')' == express.charAt(idx)) {
                return express.substring(flag, idx);
            } else {
                ++idx;
            }
        }
        if (flag == 0)
            return express.substring(flag);
        else
            throw new IllegalArgumentException("Illegal expression");
    }

    private double pre() {
        String last = pre(0);
        return calcImpl(last);
    }

    public double calc(String st) {
        express = st.trim();
        return pre();
    }

    public static void main(String[] args) {
        Calculator calculator = new Calculator();
        double result = calculator.calc(args[0]);
        System.out.println(result);
    }

}
