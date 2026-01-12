package com.example.leaflet_geo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard API Response Wrapper
 * 
 * Gunakan class ini untuk SEMUA response dari controller.
 * Ini memastikan format response konsisten di seluruh API.
 * 
 * @param <T> Tipe data yang dikembalikan
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /** true = berhasil, false = gagal */
    private boolean success;

    /** Pesan yang menjelaskan hasil operasi */
    private String message;

    /** Data yang dikembalikan */
    private T data;

    /** Total jumlah data (untuk pagination) */
    private Long totalCount;

    /** Membuat response sukses dengan data */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /** Membuat response sukses dengan data dan count */
    public static <T> ApiResponse<T> success(String message, T data, Long totalCount) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .totalCount(totalCount)
                .build();
    }

    /** Membuat response error */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}