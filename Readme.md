# iVisitor onboard

# Setup
1. Run the following query and export results to CSV: `SELECT DISTINCT A.LAST_NAME, A.FIRST_NAME, A.EMPLID, A
.EMAIL_ADDR, A.JOBTITLE
FROM SYSADM.PS_EMPLOYEES A
WHERE A.PAYGROUP IN ('HFT','DIS','HTP','HPT','INT','SAL','SNB','SNO','SSP','STN','TFH','USS', 'RCE') 
AND A.LOCATION = 'ORL' AND DEPTID NOT IN ('UCF','IM','CITY','EXEC','TOI','PARC','CHSORL','AIAFSE7');`
2. Create /apps/apps-config/ivisitor.properties and set the properties.
3. Run `mvn clean package`.
4. Run `main` in `Onboard` (from your IDE).
5. Upload to iVisitor.

# iVisitor.properties
1. `url=ldaps://relayldap.cru.org`
2. `baseDn=ou=sso,ou=account,dc=ccci,dc=org`
3. `bindDn=cn=yourRelayUsername,ou=sso,ou=account,dc=ccci,dc=org`
4. `pshrFile=location of query export`
5. `outputFile=output location`
6. `password=your relay password`

# Upload
1. Open a terminal in the directory with output file from this program.
2. Run `sftp yourUsername@www.myivisitor.com`
3. Accept the RSA key.
4. Enter your password.
5. Run `put <your output file>`.
6. When it finishes, run `quit`.
