# iVisitor onboard

# Setup
1. Run the following query and export results to CSV: `SELECT DISTINCT A.LAST_NAME, A.FIRST_NAME, A.EMPLID, A
.EMAIL_ADDR, A.JOBTITLE
FROM SYSADM.PS_EMPLOYEES A
WHERE A.PAYGROUP IN ('HFT','DIS','HTP','HPT','INT','SAL','SNB','SNO','SSP','STN','TFH','USS', 'RCE') 
AND A.LOCATION = 'ORL' AND DEPTID NOT IN ('UCF','IM','CITY','EXEC','TOI','PARC','CHSORL','AIAFSE7');`
2. Create /apps/apps-config/ivisitor.properties and set the properties.

# iVisitor.properties
1. `url=ldaps://relayldap.cru.org`
2. `baseDn=ou=sso,ou=account,dc=ccci,dc=org`
3. `bindDn=cn=yourRelayUsername,ou=sso,ou=account,dc=ccci,dc=org`
4. `pshrFile=location of query export`
5. `outputFile=output location`
6. `password=your relay password`