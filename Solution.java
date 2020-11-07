import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

class CircularDependencyException extends Exception {
    static final long serialVersionUID = 1L;

    private final String operand;

    public CircularDependencyException(String operand)
    {
        this.operand = operand;
    }

    public String getOperand()
    {
        return operand;
    }
}

class Sheet {

    int total_cell;
    Map<String, Object> list_cell;
    Map<String, Integer> list_evaluated;
    boolean circular_dependency_detected;
    PrintStream printStream;
    boolean useDebugMode;

    public Sheet()
    {
        total_cell = 0;
        list_cell = new HashMap<>();
        list_evaluated = new HashMap<>();
        circular_dependency_detected = false;
        printStream = System.out;
        useDebugMode = true;

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            total_cell = Integer.parseInt(br.readLine());
            if (total_cell <= 0)
            {
                System.err.println("total_cell <= 0");
                return;
            }

            for (int i = 1; i <= total_cell; i++)
            {
                String cell_name = br.readLine();
                String cell_content = br.readLine();
                Integer cell_value = cell_content.contains(" ") ? null : Integer.parseInt(cell_content);
                list_cell.put(cell_name, cell_value == null ? cell_content : cell_value);
                list_evaluated.put(cell_name, cell_value != null ? 1 : 0);
            }
        } catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
    }

    public void evaluate()
    {
        // Can run this in thread pool
        list_cell.forEach((cell_name,cell) -> {
            list_cell.put(cell_name, __evaluate(cell_name, cell_name));
        });
    }

    private Integer getOperandValue(String root_cell, String operand_content) throws CircularDependencyException
    {
        printLog(root_cell + " Get operand value: " + operand_content);

        if (list_cell.get(operand_content) != null && list_cell.get(operand_content) instanceof Integer)
        {
            return (Integer)list_cell.get(operand_content);
        }

        try {
            return Integer.parseInt(operand_content);
        } catch (Exception e)
        {
            //e.printStackTrace();
            if (list_evaluated.get(operand_content) == null || list_evaluated.get(operand_content) > 2)
            {
                throw new CircularDependencyException(operand_content);
            }
            return __evaluate(root_cell, operand_content);
        }
    }

    private Integer __evaluate(Integer a, Integer b, String op) {
        printLog(String.format("Evaluate value of expr: %s %s %s", a, b, op));
        switch (op)
        {
            case "+":
                return a + b;
            case "-":
                return a - b;
            case "*":
                return a * b;
            case "/":
                return a / b;
            default:
                return null;
        }
    }

    private Integer __evaluate(String root_cell, String cell_name)
    {
        Object cell = list_cell.get(cell_name);
        if (cell instanceof Integer)
        {
            return (Integer)cell;
        }

        printLog(root_cell + " Evaluating cell: " + cell_name);
        list_evaluated.put(cell_name, list_evaluated.get(cell_name) + 1);


        String[] arr_ops = ((String)cell).split(" ");
        Stack<String> list_ops = new Stack<>();
        for (int i = arr_ops.length - 1; i >= 0; i--)
        {
            printLog(root_cell + " Add " + arr_ops[i] + " to stack");
            list_ops.add(arr_ops[i]);
        }

        Integer value = null;
        String temp = null;
        while (list_ops.size() > 1)
        {
            String operand_a = list_ops.pop();
            String operand_b = list_ops.pop();
            String op = list_ops.pop();
            if (!isOperator(op))
            {
                temp = operand_a;
                operand_a = operand_b;
                operand_b = op;
                op = list_ops.pop();
            }

            printLog(String.format(root_cell + " Evalue expr: %s %s %s", operand_a, op, operand_b));

            try {
                Integer operand_a_value = getOperandValue(root_cell, operand_a);
                Integer operand_b_value = getOperandValue(root_cell, operand_b);
                if (operand_a_value == null || operand_b_value == null)
                {
                    return null;
                }

                value = __evaluate(operand_a_value, operand_b_value, op);
                list_ops.push(value.toString());
                if (temp != null)
                {
                    list_ops.push(temp);
                    temp = null;
                }

            } catch (CircularDependencyException e)
            {
                if (circular_dependency_detected)
                {
                    return null;
                }

                printCircularDependency(e.getOperand(), cell_name);
                circular_dependency_detected = true;
                return null;
            }
        }

        return value;
    }

    private boolean isOperator(String content)
    {
        return content.equals("+") || content.equals("-") || content.equals("*") || content.equals("/");
    }

    private void printCircularDependency(String op1, String op2)
    {
        String[] list_op = {op1, op2};
        Arrays.sort(list_op);
        printStream.println("Circular dependency detected: " + list_op[0] + ", " + list_op[1]);
    }

    public void printLog(String log)
    {
        if (!useDebugMode)
        {
            return;
        }

        printStream.println(log);
    }

    public void print()
    {
        printStream.println("Total cell: " + total_cell);
        list_cell.forEach((cell_name,cell) -> printStream.println(cell_name + " = " + cell));
    }

    public void printForGarding()
    {
        if (circular_dependency_detected)
        {
            return;
        }

        list_cell.forEach((cell_name,cell) -> {
            printStream.println(cell_name);
            printStream.println(cell);
        });
    }
}

/*class Cell {
    String cell_name;
    String rawContent;
    List<Object> content;
    Integer intValue;

    public Cell(String cell_name, String cell_content)
    {
        this.cell_name = cell_name;
        this.rawContent = cell_content;
        if (cell_content.contains(" "))  // is expr
        {
            for (Object o : cell_content.split(" "))
            {

            }

        } else {
            this.intValue = Integer.parseInt(cell_content);
        }

        System.out.print("Read cell: ");
        printCell();
    }

    public void evaluate()
    {
        if (intValue != null)
        {
            return;
        }


    }

    public void printCell()
    {
        System.out.println(cell_name + " = " + (intValue == null ? rawContent : intValue));
    }

}*/

public class Solution {

    private static Sheet sheet;

    public static void main(String args[] ) throws Exception {

        sheet = new Sheet();

        // test read input
        //sheet.print();

        sheet.evaluate();

        //sheet.print();
        sheet.printForGarding();
    }
}

