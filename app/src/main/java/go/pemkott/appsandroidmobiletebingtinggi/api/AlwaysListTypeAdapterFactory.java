package go.pemkott.appsandroidmobiletebingtinggi.api;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom TypeAdapterFactory that allows Gson to parse JSON lists safely.
 * If the server returns a single JSON Object instead of a JSON Array [...],
 * this factory wraps the object into a single-item List.
 * If the server returns a raw String or text/HTML, it throws an IOException
 * with descriptive information instead of unhandled IllegalStateException.
 */
public class AlwaysListTypeAdapterFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Type type = typeToken.getType();

        if (typeToken.getRawType() != List.class && typeToken.getRawType() != ArrayList.class) {
            return null;
        }

        if (!(type instanceof ParameterizedType)) {
            return null;
        }

        Type elementType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter<Object> elementAdapter = (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(elementType));

        return (TypeAdapter<T>) new TypeAdapter<List<Object>>() {
            @Override
            public void write(JsonWriter out, List<Object> value) throws IOException {
                if (value == null) {
                    out.nullValue();
                    return;
                }
                out.beginArray();
                for (Object item : value) {
                    elementAdapter.write(out, item);
                }
                out.endArray();
            }

            @Override
            public List<Object> read(JsonReader in) throws IOException {
                List<Object> list = new ArrayList<>();
                JsonToken peek = in.peek();

                if (peek == JsonToken.NULL) {
                    in.nextNull();
                    return list;
                }

                if (peek == JsonToken.BEGIN_ARRAY) {
                    in.beginArray();
                    while (in.hasNext()) {
                        Object item = elementAdapter.read(in);
                        if (item != null) {
                            list.add(item);
                        }
                    }
                    in.endArray();
                } else if (peek == JsonToken.BEGIN_OBJECT) {
                    // Server returned a single JSON object instead of a JSON array
                    Object item = elementAdapter.read(in);
                    if (item != null) {
                        list.add(item);
                    }
                } else if (peek == JsonToken.STRING) {
                    // Server returned raw string, HTML or plain text error
                    String strVal = in.nextString();
                    throw new IOException("Format respon server berupa teks (bukan JSON): " + strVal);
                } else {
                    in.skipValue();
                }

                return list;
            }
        };
    }
}
