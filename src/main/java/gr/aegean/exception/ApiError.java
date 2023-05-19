package gr.aegean.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

public record ApiError(String path, String message, Integer statusCode,
                       @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
                       @JsonSerialize(using = LocalDateTimeSerializer.class) LocalDateTime localDateTime) {}
