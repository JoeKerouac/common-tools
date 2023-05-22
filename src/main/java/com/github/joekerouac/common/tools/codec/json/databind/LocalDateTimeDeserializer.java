package com.github.joekerouac.common.tools.codec.json.databind;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.joekerouac.common.tools.codec.json.annotations.LocalDateTimeFormat;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2023-05-22 11:22
 * @since 2.0.3
 */
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime>
    implements SerializeRegister, ContextualDeserializer {

    private final String format;

    public LocalDateTimeDeserializer() {
        this(DateUtil.BASE);
    }

    public LocalDateTimeDeserializer(String format) {
        this.format = format;
    }

    @Override
    public LocalDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        String datetime = StringDeserializer.instance.deserialize(jsonParser, deserializationContext);
        if (StringUtils.isBlank(datetime)) {
            return null;
        }

        return DateUtil.parseToLocalDateTime(datetime, format);
    }

    @Override
    public void register(SimpleModule module) {
        module.addDeserializer(LocalDateTime.class, this);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
        BeanProperty beanProperty) throws JsonMappingException {
        LocalDateTimeFormat annotation = beanProperty.getAnnotation(LocalDateTimeFormat.class);
        if (annotation == null) {
            return new LocalDateTimeDeserializer(format);
        }

        return new LocalDateTimeDeserializer(
            StringUtils.getOrDefault(annotation.deserializer(), StringUtils.getOrDefault(annotation.value(), format)));
    }
}
