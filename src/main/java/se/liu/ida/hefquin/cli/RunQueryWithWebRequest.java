package se.liu.ida.hefquin.cli;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

public class RunQueryWithWebRequest extends RunQueryWithoutSrcSel {
    protected ByteArrayOutputStream output;
    protected PrintStream ps;
    final String utf8 = StandardCharsets.UTF_8.name();

    public RunQueryWithWebRequest( final String[] argv ) {
        super(argv);
        // produce query results as String
        output = new ByteArrayOutputStream();
        try {
            ps = new PrintStream( output, true, utf8 );
            modResults.setPrintStream(ps);
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
    }

    public String run(){
        mainMethod();
        return getResult();
    }

    protected String getResult(){
        String data = null;
        try {
            ps.flush();
            data = output.toString(utf8);
        } catch ( UnsupportedEncodingException e ) {
            e.printStackTrace();
        }
        return data;
    }

}