public class Terminal {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Error: Debes proporcionar el nombre del archivo como argumento.");
            System.err.println("Uso: java Main <nombre_archivo>");
            return;
        }

        String fileName = args[0];
        FileReader fileReader = new FileReader(fileName);
        IAnalyzer a1 = new AnalyzerV3();

        if (fileReader.open()) {
            fileReader.readLine(); // Descard line
            IExpression exp = a1.analise(fileReader);
            fileReader.close();

            System.out.println("Final Expression: " + exp);
            System.out.println("Complexity: " + exp.getMaxedComplexity());
        } else {
            System.err.println("Error: No se pudo abrir el archivo '" + fileName + "'");
        }
    }
}
