package it.ilogreco.levelup.converters;

import android.net.Uri;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class, converts a List of Uris to Json and viceversa
 */
public class ListPhotosConverter {
    @TypeConverter
    public static List<Uri> fromString(String json) {
        if(json == null || json.isEmpty()) return null;
        Type listType = new TypeToken<ArrayList<Uri>>() {}.getType();
        return new GsonBuilder()
                .registerTypeAdapter(Uri.class, new UriAdapterDeserializer())
                        .create().fromJson(json, listType);
    }

    @TypeConverter
    public static String fromList(List<Uri> photos) {
        if(photos == null || photos.size() == 0) return null;
        return new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<Uri>>() {}.getType(), new UriAdapterSerializer())
                .create().toJson(photos, new TypeToken<List<Uri>>() {}.getType());
    }

    public static class UriAdapterSerializer implements JsonSerializer<List<Uri>> {
        @Override
        public JsonElement serialize(List<Uri> src, Type typeOfSrc, JsonSerializationContext context) {
            JsonArray array = new JsonArray();
            if(src == null) return array;
            for (Uri uri : src) {
                array.add(uri.toString());
            }
            return array;
        }
    }

    public static class UriAdapterDeserializer implements JsonDeserializer<Uri> {
        @Override
        public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                String str = json.getAsString();
                return Uri.parse(str);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
