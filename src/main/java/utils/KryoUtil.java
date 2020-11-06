package utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import common.RequestsBody;

import java.io.*;
import java.util.Base64;


/**
 * @author mirror
 */
public class KryoUtil {


    public static void main(String[] args) {
        RequestsBody requestsBody = new RequestsBody();
        requestsBody.setProtocol("afsadsdassasdsaaa");


        String s = KryoUtil.setSerializableObject(requestsBody);
        System.out.println(s);

        RequestsBody serializableObject = KryoUtil.getSerializableObject(RequestsBody.class, s);
        System.out.println(serializableObject.getProtocol());
    }

    public static <T extends Serializable> String setSerializableObject(T obj) {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(obj.getClass(), new JavaSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = new Output(baos);
        kryo.writeClassAndObject(output, obj);
        output.flush();
        output.close();

        byte[] b = baos.toByteArray();
        try {
            baos.flush();
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(Base64.getEncoder().encode(b));
    }

    public static <T extends Serializable> T getSerializableObject(Class<T> clazz,String obj) {
        Kryo kryo = new Kryo();
        kryo.setReferences(false);
        kryo.register(clazz, new JavaSerializer());

        ByteArrayInputStream bais = new ByteArrayInputStream(
                Base64.getDecoder().decode(obj));
        Input input = new Input(bais);
        return (T) kryo.readClassAndObject(input);
    }
}
