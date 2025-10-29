public class Terminal {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Debes proporcionar el nombre del archivo como argumento.");
            System.err.println("Uso: java Main <nombre_archivo>");
            return;
        }

        String fileName = args[0];
        FileReader fileReader = new FileReader(fileName);
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
