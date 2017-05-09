package com.fighter.fastjson.parser.deserializer;

import java.lang.reflect.Type;

import com.fighter.fastjson.parser.DefaultJSONParser;

public interface ObjectDeserializer {
    <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName);
}
