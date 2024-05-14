package Kademlia;

import java.io.*;

public class ValueWrapper implements Serializable {
    private Object value;

    public ValueWrapper(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Writes the object's state to a stream. Called by the serialization mechanism when serializing an object.
     *
     * @param out The ObjectOutputStream to write the object to.
     * @throws IOException If an I/O error occurs while writing the object.
     */
    @Serial
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(value);
    }

    /**
     * Reads the object's state from a stream. Called by the serialization mechanism when deserializing an object.
     *
     * @param in The ObjectInputStream to read the object from.
     * @throws IOException            If an I/O error occurs while reading the object.
     * @throws ClassNotFoundException If the class of the serialized object cannot be found.
     */
    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        value = in.readObject();
    }
}
