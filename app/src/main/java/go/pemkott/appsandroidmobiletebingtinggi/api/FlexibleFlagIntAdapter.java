package go.pemkott.appsandroidmobiletebingtinggi.api;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * Tolerant Gson TypeAdapter for binary integer flags (0/1) that may arrive as
 * JSON booleans, numbers, numeric/boolean strings, or null.
 */
public class FlexibleFlagIntAdapter extends TypeAdapter<Integer> {
    @Override
    public void write(JsonWriter out, Integer value) throws IOException {
        if (value == null) {
            out.value(0);
        } else {
            out.value(value);
        }
    }

    @Override
    public Integer read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return 0;
        }
        if (token == JsonToken.BOOLEAN) {
            return in.nextBoolean() ? 1 : 0;
        }
        if (token == JsonToken.NUMBER) {
            return (int) in.nextDouble();
        }
        if (token == JsonToken.STRING) {
            String raw = in.nextString();
            if (raw == null) return 0;
            String normalized = raw.trim().toLowerCase();
            if ("true".equals(normalized) || "1".equals(normalized)) return 1;
            if ("false".equals(normalized) || "0".equals(normalized) || normalized.isEmpty()) return 0;
            try {
                return Integer.parseInt(normalized);
            } catch (NumberFormatException ignored) {
                return 0;
            }
        }
        in.skipValue();
        return 0;
    }
}

