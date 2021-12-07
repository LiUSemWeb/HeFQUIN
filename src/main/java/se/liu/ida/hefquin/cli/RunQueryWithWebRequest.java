package se.liu.ida.hefquin.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class RunQueryWithWebRequest extends RunQueryWithoutSrcSel {
    protected final ByteArrayOutputStream output;
    protected PrintStream printStream;
    protected final String utf8 = StandardCharsets.UTF_8.name();

    public RunQueryWithWebRequest( final String[] argv ) {
        super(argv);
        output = new ByteArrayOutputStream();

        try {
            printStream = new PrintStream( output, true, utf8 );
            modResults.setPrintStream(printStream);
        } catch ( final UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
    }

    public String run(){
        mainMethod();
        return getResult();
    }

    protected String getResult(){
        String results = null;
        try {
            printStream.flush();
            results = output.toString(utf8);
        } catch ( final UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
        return results;
    }

}