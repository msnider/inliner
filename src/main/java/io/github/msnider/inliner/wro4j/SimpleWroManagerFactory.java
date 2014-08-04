package io.github.msnider.inliner.wro4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;

import ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.factory.XmlModelFactory;

public class SimpleWroManagerFactory extends ConfigurableWroManagerFactory {

	private final Properties configProperties;
	private final String resourceName;

    public SimpleWroManagerFactory(String resourceName,Properties configProperties) {
        this.configProperties = configProperties;
        this.resourceName = resourceName;
    }

    @Override
    protected Properties newConfigProperties() {
        return configProperties;
    }
    
    @Override
    protected WroModelFactory newModelFactory() {
    	return new XmlModelFactory() {
    		@Override
    		protected InputStream getModelResourceAsStream() throws IOException {
    			return new ClassPathResource(resourceName).getInputStream();
    		}
    	};
    }
}
