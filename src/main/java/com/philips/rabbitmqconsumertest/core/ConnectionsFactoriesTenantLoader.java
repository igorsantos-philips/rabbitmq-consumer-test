package com.philips.rabbitmqconsumertest.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

import com.rabbitmq.client.ConnectionFactory;
@Configuration
public class ConnectionsFactoriesTenantLoader implements InitializingBean{

	private Map<String,VhostConnectionFactory> connectionsFactories = new TreeMap<>();
	
	@Value("${spring.rabbitmq.addresses}")
	private String rabbitMQHosts;
	
	//@Value("${philips.tenants}")
	private List<Tenant> tenants = new ArrayList<>();
	
	@Value("${spring.rabbitmq.username}")
	private String rabbitMQUsername;
	
	@Value("${spring.rabbitmq.password}")
	private String rabbitMQPassword;
	
	@Value("${spring.rabbitmq.ssl.enabled:false}")
	private boolean sslEnabled;
	
	@Value("${spring.rabbitmq.ssl.algorithm:}")
	private String sslAlgorithm;
	
	@Value("${spring.rabbitmq.ssl.trust-store-location:}")
	private String trustStoreLocation;
	
	@Value("${spring.rabbitmq.ssl.trust-store-type:}")
	private String trustStoreType;
	
	@Value("${spring.rabbitmq.ssl.trust-store-password:}")
	private String trustStorePassword;
	
//	@Value("${rabbitmq.ssl.sni.host.name:}")
//	private String sniHostName;

	@Value("${spring.rabbitmq.reply-timeout:5000}")
	private int replyTimeout;
	
	@Value("${spring.rabbitmq.consume-timeout:10000}")
	private int consumeTimeout;
	
	@Value("${spring.rabbitmq.connection-timeout:10000}")
	private int connectionTimeout;
	
	public void afterPropertiesSet() throws Exception {
		final Tenant t1 = new Tenant();
		t1.setTenantId("HSC");
		t1.setAsyncConsumers(1);
		t1.setSyncConsumers(3);
		this.tenants.add(t1);
		
		/*final Tenant t2 = new Tenant();
		t2.setTenantId("HSI");
		t2.setAsyncConsumers(2);
		t2.setSyncConsumers(4);
		this.tenants.add(t2);*/
		
		if(this.connectionsFactories.isEmpty() ) {
			//Assert.notNull(this.tenants,message("philips.tenants"));
			Assert.notNull(this.rabbitMQHosts,message("spring.rabbitmq.addresses"));
			Assert.notNull(this.rabbitMQUsername,message("spring.rabbitmq.username"));
			Assert.notNull(this.rabbitMQPassword,message("spring.rabbitmq.password"));
			for (Tenant vhost : this.tenants) {
				VhostConnectionFactory vhostConnectiosFactoriesTemp = new VhostConnectionFactory(vhost.getTenantId());
				vhostConnectiosFactoriesTemp.createConnectionFactory(this.rabbitMQHosts);
				this.connectionsFactories.put(vhost.getTenantId(), vhostConnectiosFactoriesTemp);
			}
		}
	}

	private String message(String property) {
		return "The property ["+property+"] cannot be null";
	}
	

	public org.springframework.amqp.rabbit.connection.ConnectionFactory getTenantConnectionFactory(String tenantId){
		if(this.connectionsFactories.containsKey(tenantId)) {
			return this.connectionsFactories.get(tenantId).getConnectionFactory();
		}
		throw new RuntimeException("There is no Tenant with ID ["+tenantId+"]");
		
	}
	public List<Tenant>getTenants(){
		return this.tenants;
	}
	
	
	private class VhostConnectionFactory {
		String virtualHostName;
		private CachingConnectionFactory connectionFactory;
		
		VhostConnectionFactory(String virtualHostName){
			this.virtualHostName=virtualHostName;
		}
		void createConnectionFactory(String adresses) throws FileNotFoundException, GeneralSecurityException, IOException{
				
				ConnectionFactory connectionFactoryTemp = new ConnectionFactory();
				connectionFactoryTemp.setUsername(rabbitMQUsername);;
				connectionFactoryTemp.setPassword(rabbitMQPassword);
				connectionFactoryTemp.setVirtualHost(virtualHostName);
				connectionFactoryTemp.setAutomaticRecoveryEnabled(true);
				connectionFactoryTemp.setConnectionTimeout(connectionTimeout);
				connectionFactoryTemp.setHandshakeTimeout(connectionTimeout);
				if(sslEnabled) {
					SSLContext sslContext = getSSLContext(adresses);
					connectionFactoryTemp.useSslProtocol(sslContext);
					connectionFactoryTemp.setSocketFactory(sslContext.getSocketFactory());
				}
				this.connectionFactory = new CachingConnectionFactory(connectionFactoryTemp);
				this.connectionFactory.setAddresses(adresses);
		}
		private SSLContext getSSLContext(String hosts) throws GeneralSecurityException, FileNotFoundException, IOException {
	        KeyStore tks = generateKeyStore();
	        TrustManagerFactory tmf = generateTrustManagerFactory(tks);
	        SecureRandom secureRandom = SecureRandom.getInstance(sslAlgorithm);
	        KeyManagerFactory kmf = KeyManagerFactory.getInstance(sslAlgorithm);
	        SSLContext sslContext = SSLContext.getInstance(sslAlgorithm);
	        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), secureRandom);
//	        sslContext.getDefaultSSLParameters().setServerNames(generateListSNIServerName(sniHostName));
	        return sslContext;
		}
//		private List<SNIServerName>generateListSNIServerName(String hosts){
//	        List<SNIServerName> sniHostNames = new ArrayList<SNIServerName>();
//	        String[] hostsAndPorts = hosts.split(",");
//	        for(String hostAndPort : hostsAndPorts) {
//	        	String host = hostAndPort.split(":")[0];
//	        	sniHostNames.add(new SNIHostName(host));
//	        }
//			return sniHostNames;
//		}
		private KeyStore generateKeyStore() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
	        KeyStore tks = KeyStore.getInstance(trustStoreType);
	        tks.load(new FileInputStream(trustStoreLocation), trustStorePassword.toCharArray());
	        return tks;
		}
		private TrustManagerFactory generateTrustManagerFactory(KeyStore tks) throws NoSuchAlgorithmException, KeyStoreException {
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance(sslAlgorithm);
	        tmf.init(tks);
	        return tmf;
	        
		}
		
		org.springframework.amqp.rabbit.connection.ConnectionFactory getConnectionFactory(){
			return this.connectionFactory;
		}
	}
}
