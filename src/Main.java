//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

import java.util.*;

public class Main {
    public static void main(String[] args) {

        FileReader fileReader = new FileReader("burbuja.txt");
        IAnalyzer a1 = new AnalyzerV4();
        if (fileReader.open()) {//+5.0a+5.0b-5.0
            fileReader.readLine(); // Descard line
            IExpression exp = a1.analise(fileReader);
            fileReader.close();

            System.out.println("Final Expression: " + exp);
            System.out.println("Complexity: " + exp.getMaxedComplexity());
        }

        if (fileReader.open()) {//+5.0a+5.0b-5.0
            fileReader.readLine(); // Descard line
            String exp = a1.analiseExtended(fileReader);
            fileReader.close();

            System.out.println("Extended Expression: " + exp);
        }

    }
}

