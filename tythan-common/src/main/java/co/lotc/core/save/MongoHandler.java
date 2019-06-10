package co.lotc.core.save;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class MongoHandler implements Closeable {
	private final String dbName;
	private final MongoClient client;
	
	private final List<Codec<?>> codecs = new ArrayList<>();
	
	public MongoHandler(String dbName) {
		this.dbName = dbName;
		client = MongoClients.create();
	}
	
	public MongoHandler(String dbName, String ip, int port) {
		this.dbName = dbName;
		client = MongoClients.create(MongoClientSettings.builder()
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(ip, port))))
				.build());
	}
	
	public MongoHandler(String dbName, String ip, int port, String username, String password) {
		this.dbName = dbName;
		client = MongoClients.create(MongoClientSettings.builder()
				.credential(MongoCredential.createCredential(username, ip, password.toCharArray()))
				.applyToClusterSettings(builder -> builder.hosts(Arrays.asList(new ServerAddress(ip, port))))
				.build());
	}

	public MongoHandler withDatabase(String databaseName) {
		return new MongoHandler(databaseName,client);
	}
	
	public MongoConnection connect() {
		return new MongoConnection(client, dbName, getCodecRegistry());
	}
	
	public CodecRegistry getCodecRegistry() {
		TythanContextCodec contexts = new TythanContextCodec();
		
		val result = CodecRegistries.fromRegistries(
				CodecRegistries.fromCodecs(codecs),
				CodecRegistries.fromCodecs(contexts),
				new PreventBukkitEncodingRegistry(),
				MongoClientSettings.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build())
				);
		
		contexts.setRegistry(result);
		return result;
	}

	@Override
	public void close() {
		client.close();
	}
}
