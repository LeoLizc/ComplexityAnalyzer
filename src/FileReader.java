import java.io.BufferedReader;

public class FileReader {
    String path;
    BufferedReader reader;
    private long linesRead = 0;
    private String lastLine = null;

    long getLinesRead() {
        return this.linesRead;
    }

    String getLastLine() {
        return this.lastLine;
    }

    public FileReader(String path) {
        this.path = path;
        this.reader = null;
    }

    public boolean open() {
        try {
            this.reader = new BufferedReader(new java.io.FileReader(this.path));
            this.linesRead = 0;
            this.lastLine = null;
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String readLine() {
        try {
            if (this.reader != null) {
                while ((this.lastLine = this.reader.readLine()) != null && this.lastLine.trim().isEmpty()) {
                    // Skip empty lines
                }
                this.linesRead += 1;
                if (this.lastLine != null) {
                    this.lastLine = this.lastLine.trim();
                }

//                System.out.println("Read line " + this.linesRead + ": " + this.lastLine);
                return this.lastLine;
            } else {
                throw new IllegalStateException("File not opened. Call open() before reading.");
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        try {
            if (this.reader != null) {
                this.reader.close();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
