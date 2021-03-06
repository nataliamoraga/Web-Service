// 
// Decompiled by Procyon v0.5.36
// 

package gnu.getopt;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class LongOpt
{
    public static final int NO_ARGUMENT = 0;
    public static final int REQUIRED_ARGUMENT = 1;
    public static final int OPTIONAL_ARGUMENT = 2;
    protected String name;
    protected int has_arg;
    protected StringBuffer flag;
    protected int val;
    private ResourceBundle _messages;
    
    public LongOpt(final String name, final int n, final StringBuffer flag, final int val) throws IllegalArgumentException {
        this._messages = ResourceBundle.getBundle("gnu/getopt/MessagesBundle", Locale.getDefault());
        if (n != 0 && n != 1 && n != 2) {
            throw new IllegalArgumentException(MessageFormat.format(this._messages.getString("getopt.invalidValue"), new Integer(n).toString()));
        }
        this.name = name;
        this.has_arg = n;
        this.flag = flag;
        this.val = val;
    }
    
    public String getName() {
        return this.name;
    }
    
    public int getHasArg() {
        return this.has_arg;
    }
    
    public StringBuffer getFlag() {
        return this.flag;
    }
    
    public int getVal() {
        return this.val;
    }
}
