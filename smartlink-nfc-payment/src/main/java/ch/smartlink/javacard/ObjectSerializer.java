package ch.smartlink.javacard;

import java.io.*;

/**
 * Created by caoky on 12/15/2015.
 */
public class ObjectSerializer {
    public static byte[] serialize(Object obj)  {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = null;
        try {
            o = new ObjectOutputStream(b);
            o.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Cannot serialize object");
        }

        return b.toByteArray();
    }

    public static <T> T deserialize(byte[] bytes)  {
        try {
            ByteArrayInputStream b = new ByteArrayInputStream(bytes);
            ObjectInputStream o = new ObjectInputStream(b);
            return (T) o.readObject();
        }catch (Exception ex) {
            throw new RuntimeException("Cannot serialize object");
        }
    }
}
