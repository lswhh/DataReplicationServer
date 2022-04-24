package kr.datareplicationserver.database;

public class JdbcDriver 
{
    public String mDriverPath      = null;
    public String mDriverClassName = null;

    public String toString()
    {
        String sString = "";

        if( mDriverPath != null )
        {
            sString += mDriverPath;
        }
        else
        {
            /*do nothing*/
        }

        if( mDriverClassName != null )
        {
            sString += ": " + mDriverClassName;
        }
        else
        {
            /*do nothing*/
        }

        return sString;
    }
}

