import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.ccci.idm.ldap.Ldap;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * Created by dsgoers on 1/20/16.
 */
public class Onboard
{
    private static final String propertiesFile = "/apps/apps-config/ivisitor.properties";

    private static final String[] returnAttributes = {"ccciGuid", "sn", "givenName", "cn", "telephoneNumber"};

    private static Ldap ldap;

    private static Properties prop;

    public static void main(String[] args) throws Exception
    {
        prop = new Properties();
        prop.load(new FileInputStream(propertiesFile));

        ldap = new Ldap(prop.getProperty("url"), prop.getProperty("bindDn"), prop.getProperty("password"));

        File csvData = new File(prop.getProperty("pshrFile"));
        CSVParser parser = CSVParser.parse(csvData, StandardCharsets.UTF_8, CSVFormat.RFC4180);

        FileWriter fileWriter = new FileWriter(prop.getProperty("outputFile"));

        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.RFC4180.withDelimiter('^'));
        for (CSVRecord csvRecord : parser) { //read in each row from the pshr output file
            Attributes relayUser = getRelayUser(csvRecord.get(2), csvRecord.get(0)); //load the user from relay via
            // employee id (and last name if there's two relay users with the same employee id)

            if(relayUser != null)
            {
                String telephoneNumber = relayUser.get("telephoneNumber") != null ? relayUser.get("telephoneNumber")
                        .get().toString() : null;

                csvPrinter.print("Cru");            //location or tenant name
                csvPrinter.print(csvRecord.get(0)); //last name, from PSHR
                csvPrinter.print(csvRecord.get(1)); //first name, from PSHR
                csvPrinter.print(csvRecord.get(3)); //email address, from PSHR
                csvPrinter.print(telephoneNumber);
                csvPrinter.print(csvRecord.get(4)); //title, from PSHR
                csvPrinter.print(csvRecord.get(3));
                csvPrinter.print(relayUser.get("ccciGuid").get().toString());
                csvPrinter.println();
            }
        }

        ldap.close();
        csvPrinter.close();
    }

    private static Attributes getRelayUser(String employeeId, String lastName) throws NamingException
    {
        Map<String, Attributes> results = ldap.searchAttributes(prop.getProperty("baseDn"), "employeeNumber=" +
                employeeId, returnAttributes);

        if(results.size() > 1)
        {
            //if there's more than one relay user with the employeeNumber, check the last names
            for(Attributes value : results.values())
            {
                if(lastName.equalsIgnoreCase(value.get("sn").get().toString()))
                {
                    return value;
                }
            }

            //is this is thrown, comparing first name and email address could also be done above
            throw new RuntimeException("For employeeId " + employeeId + ", " + results.size() + " results in Relay.  " +
                    "Couldn't pick the correct one.");
        }
        else if(results.size() == 1)
        {
            return results.entrySet().iterator().next().getValue();
        }

        return null;
    }
}
