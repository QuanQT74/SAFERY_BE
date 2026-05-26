package com.example.banckend.conmon.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class ApiResponse<T> {

    @Builder.Default
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    private String message = "success";

    private T data;

    public ApiResponse(String message) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC);
        this.message = message;
    }

    public ApiResponse(T data) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC); // Lấy thời gian hiện tại theo UTC
        this.data = data;
        this.message = "Success";
    }

    public ApiResponse(String message, T data) {
        this.timestamp = OffsetDateTime.now(ZoneOffset.UTC); // Lấy thời gian hiện tại theo UTC
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .message("Success")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> of(String message) {
        return ApiResponse.<T>builder()
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .message(message)
                .build();
    }
}
