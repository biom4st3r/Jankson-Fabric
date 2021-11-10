package io.github.cottonmc.jankson;

import java.util.Map;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import blue.endless.jankson.JsonArray;
import blue.endless.jankson.JsonElement;
import blue.endless.jankson.JsonNull;
import blue.endless.jankson.JsonObject;
import blue.endless.jankson.JsonPrimitive;

/**
 * A DynamicOps instance for Jankson. Loosely based on Mojang's JsonOps for Gson.
 */
public class JanksonOps implements DynamicOps<JsonElement> {
    public static final JanksonOps INSTANCE = new JanksonOps();
    private static final String ElementNotFound = "Missing";
    protected JanksonOps() {}

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }


    @Override
    public <U> U convertTo(DynamicOps<U> outOps, JsonElement input) {
        if(input instanceof JsonObject) {
            return this.convertMap(outOps, input);
        } else if (input instanceof JsonArray) {
            return this.convertList(outOps, input);
        } else if(input instanceof JsonNull) {
            return outOps.empty();
        }
        
        final JsonPrimitive primitive = (JsonPrimitive) input;
        Object value = primitive.getValue();
        if (value instanceof String) {
            return outOps.createString((String) value);
        } else if (value instanceof Boolean) {
            return outOps.createBoolean((boolean) value);
        } else if (value instanceof Byte) {
            return outOps.createByte((byte) value);
        } else if (value instanceof Character ch) {
            return outOps.createString(String.valueOf(new char[]{ch}));
        } else if (value instanceof Short) {
            return outOps.createShort((short) value);
        } else if (value instanceof Integer) {
            return outOps.createInt((int) value);
        } else if (value instanceof Long) {
            return outOps.createLong((long) value);
        } else if (value instanceof Float) {
            return outOps.createFloat((float) value);
        } else if (value instanceof Double) {
            return outOps.createDouble((double) value);
        } else {
            return outOps.empty();
        }
    }

    @Override
    public JsonElement createMap(Stream<Pair<JsonElement, JsonElement>> map) {
        JsonObject result = new JsonObject();
        map.forEach(p -> result.put(p.getFirst().toJson(), p.getSecond()));
        return result;
    }

    @Override
    public DataResult<Number> getNumberValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();
            if (value instanceof Number) {
                return DataResult.success((Number) value);
            } else if (value instanceof Boolean) {
                return DataResult.success((Boolean) value ? 1 : 0);
            }
        }
        return DataResult.error(ElementNotFound);
    }

    @Override
    public JsonElement createNumeric(Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public JsonElement createBoolean(boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<String> getStringValue(JsonElement input) {
        if (input instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) input).getValue();
            if (value instanceof String) {
                return DataResult.success((String) value);
            }
        }
        return DataResult.error(ElementNotFound);
    }

    @Override
    public JsonElement createString(String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(JsonElement input, JsonElement key, JsonElement value) {
        JsonObject output = new JsonObject();
        if (input instanceof JsonObject) {
            output.putAll((JsonObject) input);
        } else if (!(input instanceof JsonNull)) {
            return DataResult.success(input);
        }

        output.put(((JsonPrimitive) key).asString(), value);
        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonElement> mergeToList(JsonElement first, JsonElement second) {
        if (first instanceof JsonNull) {
            return DataResult.success(second);
        } else if (second instanceof JsonNull) {
            return DataResult.success(first);
        }

        if (first instanceof JsonObject && second instanceof JsonObject) {
            JsonObject result = new JsonObject();
            result.putAll((JsonObject) first);
            result.putAll((JsonObject) second);
            return DataResult.success(result);
        } else if (first instanceof JsonArray && second instanceof JsonArray) {
            JsonArray result = new JsonArray();
            result.addAll((JsonArray) first);
            result.addAll((JsonArray) second);
            return DataResult.success(result);
        }

        throw new IllegalArgumentException("Could not merge " + first + " and " + second);
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(JsonElement input) {
        if (input instanceof JsonObject) {
            JsonObject inputObj = (JsonObject) input;
            ImmutableMap.Builder<JsonElement, JsonElement> builder = ImmutableMap.builder();
            for (Map.Entry<String, JsonElement> entry : inputObj.entrySet()) {
                builder.put(new JsonPrimitive(entry.getKey()), entry.getValue());
            }
            return DataResult.success(builder.build().entrySet().stream().map(e->new Pair<>(e.getKey(), e.getValue())));
        }
        return DataResult.error(ElementNotFound);
    }

    @Override
    public JsonElement createMap(Map<JsonElement, JsonElement> map) {
        JsonObject result = new JsonObject();
        for (Map.Entry<JsonElement, JsonElement> entry : map.entrySet()) {
            result.put(((JsonPrimitive) entry.getKey()).asString(), entry.getValue());
        }
        return result;
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(JsonElement input) {
        if (input instanceof JsonArray) {
            return DataResult.success(((JsonArray) input).stream());
        }
        return DataResult.error(ElementNotFound);
    }

    @Override
    public JsonElement createList(Stream<JsonElement> input) {
        JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
    }

    @Override
    public JsonElement remove(JsonElement input, String key) {
        if (input instanceof JsonObject) {
            JsonObject result = new JsonObject();
            for (Map.Entry<String, JsonElement> entry : ((JsonObject) input).entrySet()) {
                if (!entry.getKey().equals(key)) {
                    result.put(entry.getKey(), entry.getValue());
                }
            }
            return result;
        }
        return input;
    }

    @Override
    public String toString() {
        return "Jankson";
    }

}
