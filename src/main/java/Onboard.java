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
    private static final String propertiesFile = "/apps/apps-config/onboard.properties";
    private static final String pshrFile = "/Users/dsgoers/Desktop/lakehartstaff.csv";
    private static final String onboardFile = "/Users/dsgoers/Desktop/onboard.csv";

    private static final String[] returnAttributes = {"ccciGuid", "sn", "givenName", "cn", "telephoneNumber"};

    private static Ldap ldap;

    private static Properties prop;

    public static void main(String[] args) throws Exception
    {
        prop = new Properties();
        prop.load(new FileInputStream(propertiesFile));

        ldap = new Ldap(prop.getProperty("url"), prop.getProperty("bindDn"), prop.getProperty("password"));

        File csvData = new File(pshrFile);
        CSVParser parser = CSVParser.parse(csvData, StandardCharsets.UTF_8, CSVFormat.RFC4180);

        FileWriter fileWriter = new FileWriter(onboardFile);

        CSVPrinter csvPrinter = new CSVPrinter(fileWriter, CSVFormat.RFC4180);
        for (CSVRecord csvRecord : parser) {
            Attributes relayUser = getRelayUser(csvRecord.get(2), csvRecord.get(0));

            if(relayUser != null)
            {
                String telephoneNumber = relayUser.get("telephoneNumber") != null ? relayUser.get("telephoneNumber")
                        .toString().substring(17) : null;

                String username = relayUser.get("cn").toString();
                username = username.substring(4, username.indexOf("@"));

                csvPrinter.print(csvRecord.get(0)); //last name, from PSHR
                csvPrinter.print(csvRecord.get(1)); //first name, from PSHR
                csvPrinter.print(csvRecord.get(3)); //email address, from PSHR
                csvPrinter.print(telephoneNumber);
                csvPrinter.print(csvRecord.get(4)); //title, from PSHR
                csvPrinter.print(username);
                csvPrinter.print(relayUser.get("ccciGuid").toString().substring(10));
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
            for(Attributes value : results.values())
            {
                if(lastName.equalsIgnoreCase(value.get("sn").toString().substring(4)))
                {
                    return value;
                }
            }

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
