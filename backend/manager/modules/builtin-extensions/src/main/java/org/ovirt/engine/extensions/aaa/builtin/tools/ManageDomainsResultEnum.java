package org.ovirt.engine.extensions.aaa.builtin.tools;

public enum ManageDomainsResultEnum {
    OK("Manage Domains completed successfully", 0),
    INVALID_ACTION("%1$s is not a valid action.", 1),
    ARGUMENT_IS_REQUIRED("Argument %1$s is required.", 3),
    DOMAIN_ALREADY_EXISTS_IN_CONFIGURATION("Domain %1$s already exists in the configuration.", 5),
    DOMAIN_DOESNT_EXIST_IN_CONFIGURATION("Domain %1$s doesn't exist in the configuration.", 6),
    FAILED_READING_CONFIGURATION(
             "Failed reading engine-manage-domains configuration. Details: %1$s",
             7),
    FAILURE_WHILE_TESTING_DOMAIN("Failure while testing domain %1$s. Details: %2$s", 8),
    FAILURE_WHILE_APPLYING_KERBEROS_CONFIGURATION("Failure while applying Kerberos configuration. Details: %1$s", 9),
    FAILURE_WHILE_APPLYING_CHANGES_IN_DATABASE("Failure while applying changes in database. Details: %1$s", 10),
    DB_EXCEPTION("Failure while connecting to database. Details: %1$s", 11),
    FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION("Failed setting configuration value for option %1$s", 12),
    FAILED_SETTING_CONFIGURATION_VALUE_FOR_OPTION_WITH_DETAILS(
            "Failed setting configuration value for option %1$s. Details: %2$s",
            13),
    FAILED_READING_CURRENT_CONFIGURATION(
            "Failed reading current configuration. Details: %1$s",
            15),
    FAILURE_CREATING_KERBEROS_CONFIGURATION("Failure while applying Kerberos configuration. Details: %1$s",
            16),
    ERROR_CREATING_PROPERTIES_FILE("Error creating engine-config properties file. Details: %1$s", 17),
    ILLEGAL_PASSWORD("Password contains illegal characters.", 18),
    UNKNOWN_ERROR(
            "Operation failed due to exception. Details: %1$s",
            20),
    FAILURE_READING_PASSWORD_FILE("failed reading password from password file", 21),
    EMPTY_PASSWORD_FILE("password file is empty", 22),
    NO_LDAP_SERVERS_FOR_DOMAIN("No LDAP servers can be obtained for domain %1$s", 23),
    NO_KDC_SERVERS_FOR_DOMAIN("No KDC can be obtained for domain %1$s", 24),
    INVALID_ARGUMENT_VALUE("Invalid argument value. Details: %1$s", 26),
    ARGUMENT_PARSING_ERROR("Error parsing arguments. Details: %1$s", 27);
    private String detailedMessage;
    private final int exitCode;

    private ManageDomainsResultEnum(String detailedMsg, int exitCode) {
        this.detailedMessage = detailedMsg;
        this.exitCode = exitCode;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public int getExitCode() {
        return exitCode;
    }

}
