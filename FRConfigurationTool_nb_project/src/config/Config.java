package config;


import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;



/**
 * (New class) This class, given a properties file, instantiates a configuration object.
 * @author axaridou
 *
 */

public class Config extends PropertiesConfiguration{
	
	
	public Config(String file){
		super();

		Parameters params = new Parameters();
		FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
		    new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
		    .configure(params.fileBased()
		        .setFileName(file));

		try
		{
		    this.append(builder.getConfiguration());
		}
		catch(ConfigurationException cex)
		{
		    // loading of the configuration file failed
			//this.getLogger().info(cex.getMessage());
			cex.printStackTrace();
		}

	}	

	
	
}
