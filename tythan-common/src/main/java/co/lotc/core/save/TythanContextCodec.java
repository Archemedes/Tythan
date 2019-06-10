package co.lotc.core.save;

import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import co.lotc.core.CoreLog;
import co.lotc.core.util.Context;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.val;

@RequiredArgsConstructor
public class TythanContextCodec implements Codec<Context> {
	@Setter private CodecRegistry registry;
	
	
	@SuppressWarnings("unchecked")
	private Codec<Object> getCodec(Class<?> clazz) {
		return (Codec<Object>) registry.get(clazz);
	}

	@Override
	public void encode(BsonWriter writer, Context value, EncoderContext encoderContext) {
		
		writer.writeStartDocument();
		value.getMap().forEach( (k,v)->{
			writer.writeName(k);
			
			writer.writeStartArray();
			val clz = v.getClass();
			writer.writeString(clz.getName());

			Codec<Object> codec = getCodec(clz);
			codec.encode(writer, v, encoderContext);
			writer.writeEndArray();
			
		});
		writer.writeEndDocument();
	}

	@Override
	public Class<Context> getEncoderClass() {
		return Context.class;
	}

	@Override
	public Context decode(BsonReader reader, DecoderContext decoderContext) {
		Context context = new Context();

		reader.readStartDocument();
		while( reader.readBsonType() != BsonType.END_OF_DOCUMENT ) {
			String name = reader.readName();
			
			
			//Going to ignore the Object ID field altogether. This may or may not be a good idea?
			if(name.equals("_id")) {
				reader.skipValue();
				continue;
			}
			
			reader.readStartArray();
			String className = reader.readString();
			
			try {
				Class<?> clz = Class.forName(className);
				val codec = getCodec(clz);
				Object value = codec.decode(reader, decoderContext);
				context.set(name, value);
			} catch (ClassNotFoundException e) {
				CoreLog.severe("Unknown class when decoding context key " + name + ": " + className);
			}
			reader.readEndArray();
		}
		
		reader.readEndDocument();
		return context;
	}

}
