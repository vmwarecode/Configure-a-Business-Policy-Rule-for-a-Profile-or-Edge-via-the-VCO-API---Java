import net.velocloud.swagger.api.*;
import net.velocloud.swagger.client.VCApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.model.*;

import java.util.*;

/*
 * Example demonstrating how to add a business policy rule to a profile.
 *
 * Note that this applies only to Network-based Edges and Profiles (i.e. *not* segmntation-enabled
 * ones)
 *
 */

public class QOSRuleExample {

  private static final VCApiClient client = new VCApiClient();
  private static final AllApi api = new AllApi();

  public static void connectAndAuthenticate(String hostname, String username, String password) throws ApiException {

    client.setBasePath("https://"+hostname+"/portal/rest");
    api.setApiClient(client);
    AuthObject auth = new AuthObject().username(username).password(password);
    api.loginOperatorLogin(auth);

  }

  private static ModelConfiguration getConfigurationWithModules(int configurationId, int enterpriseId) throws ApiException {

    ConfigurationGetConfiguration req = new ConfigurationGetConfiguration();
    req.setEnterpriseId(enterpriseId);
    req.setConfigurationId(configurationId);
    req.setWith(Arrays.asList(ConfigurationGetConfiguration.WithEnum.MODULES));
    return api.configurationGetConfiguration(req);

  }

  private static ConfigurationUpdateConfigurationModuleResult updateConfigurationModule(ConfigurationModule update, int enterpriseId) throws ApiException {

    ConfigurationUpdateConfigurationModule req = new ConfigurationUpdateConfigurationModule();
    req.setId(update.getId());
    req.setEnterpriseId(enterpriseId);
    req.setUpdate(update);
    return api.configurationUpdateConfigurationModule(req);
  
  }

  public static void main(String[] args) {

    // EDIT PARAMS AS NEEDED
    String HOSTNAME = "HOSTNAME";
    String USERNAME = "USERNAME";
    String PASSWORD = "PASSWORD";

    int enterpriseId = 1;
    int configurationId = 6;

    // Pull down configuration data (this can be done with a few different methods)
    ModelConfiguration config = null;
    try {
      config = getConfigurationWithModules(configurationId, enterpriseId);
    } catch (ApiException e) {
      System.out.println("Error in getConfigurationWithModules: " + e);
      System.exit(-1);
    }
    List<ConfigurationModule> modules = config.getModules();
    QOS qosModule = null;
    QOSData qosModuleData = null;
    for (int i = 0; i < modules.size(); i++) {
      if (modules.get(i).getName().equals(ConfigurationModule.NameEnum.QOS)) {
        qosModule = mapper.convertValue(modules.get(i), QOS.class);
        qosModuleData = qosModule.getData();
        break;
      }
    }

    // Add a new rule, copied from a comparable target
    ArrayList<QOSDataRules> rules = new ArrayList<QOSDataRules>(qosModuleData.getRules());
    QOSDataRules newRule = null;
    for ( QOSDataRules rule : rules ) {
      if ( rule.getName().equals("Box") ) {
        // Copy this rule using the convert/copy utility
        newRule = mapper.convertValue(rule, QOSDataRules.class);
        break;
      }
    }

    // Modify values here as desired
    newRule.setName("RADIUS");
    QOSDataMatch match = newRule.getMatch();
    match.setAppid(158);  // The ID corresponding to RADIUS in our application map
    newRule.setMatch(match);
    rules.add(newRule);
    qosModuleData.setRules(rules);
    qosModule.setData(qosModuleData);

    // Submit update request
    try {
      updateConfigurationModule(qosModule, enterpriseId);
    } catch (ApiException e) {
      System.out.println("Exception in configuration/updateConfigurationModule: " + e);
      System.exit(-1);
    }
    System.out.println("Configuration module successfully updated.");

  }

}
