

package au.org.emii;

import java.io.InputStream ;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;

import java.sql.*;

import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.cli.*;



// xslt examples,
// http://fandry.blogspot.com.au/2012/04/java-xslt-processing-with-saxon.html




class Updater 
{

	public Updater ()
	{ }

	private static Transformer getTransformer( String filename ) throws Exception 
	{
		final TransformerFactory tsf = TransformerFactory.newInstance();
		final InputStream is  = new FileInputStream( filename ); 

		return tsf.newTransformer(new StreamSource(is));
	}

	private static String transform ( Transformer transformer, String xmlString) throws Exception 
	{
		final StringReader xmlReader = new StringReader(xmlString);
		final StringWriter xmlWriter = new StringWriter();

		transformer.transform(new StreamSource(xmlReader), new StreamResult(xmlWriter));

		return xmlWriter.toString();
	}

    private static Connection getConn( String url, String user, String pass) throws Exception
	{
		Properties props = new Properties();

		props.setProperty("user", user );
		props.setProperty("password",pass );

		// props.setProperty("search_path","soop_sst,public");
		props.setProperty("ssl","true");
		props.setProperty("sslfactory","org.postgresql.ssl.NonValidatingFactory");
		props.setProperty("driver","org.postgresql.Driver" );

		return DriverManager.getConnection(url, props);
	}

	public static void main(String[] args) throws Exception
	{
		Options options = new Options();
		options.addOption("url", true, "jdbc connection string, eg. jdbc:postgresql://127.0.0.1/geonetwork");
		options.addOption("u", true, "user");
		options.addOption("p", true, "password");
		options.addOption("uuid", true, "metadata record uuid");
		options.addOption("help", false, "show help");
		options.addOption("t", true, "xslt file for transform");
		options.addOption("stdout", false, "dump result to stdout");
		options.addOption("update", false, "actually update the record");
		options.addOption("all", false, "update all records");

		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse( options, args);

		if(cmd.hasOption("help") || !cmd.hasOption("url") || !cmd.hasOption("u") || !cmd.hasOption("u")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "Updater", options );
			return;
		}

		Connection conn = getConn( 
			cmd.getOptionValue("url"), 
			cmd.getOptionValue("u"), 
			cmd.getOptionValue("p")
		); 

		String query = "SELECT id,uuid,data FROM metadata ";
		if(cmd.hasOption("uuid")) {
			query += " where uuid = '" + cmd.getOptionValue("uuid") + "'"; 
		} 

		else if(cmd.hasOption("stdout") || cmd.hasOption( "update")) {
			; 
		}
		else { 
			throw new RuntimeException ( "Options should include -uuid, -all or -stdout" ); 
		}


        PreparedStatement stmt = conn.prepareStatement( query );
        ResultSet rs = stmt.executeQuery();

		Transformer transformer = null; 
		if( cmd.hasOption("t")) { 
			transformer = getTransformer(cmd.getOptionValue("t") );
		}	 

        int count = 0;
        while ( rs.next() ) 
        {
			int id = (Integer) rs.getObject("id");
			String uuid = (String) rs.getObject("uuid");
			String data = (String) rs.getObject("data");

			if( transformer != null ) {
				data = transform( transformer, data);  
			}
	
			if( cmd.hasOption("stdout")) {
				System.out.println( "id " + id + "\nuuid " + uuid + "\n" + data);
			}

			if( cmd.hasOption("update")) {
				PreparedStatement updateStmt = conn.prepareStatement( "update metadata set data=? where id=?" ) ; 
				updateStmt.setString(1, data );
				updateStmt.setInt(2, id );
				updateStmt.executeUpdate();
				// close...? 
			}

            ++count;
			break;
        }

        System.out.println( "records processed " + count );
	}
}


