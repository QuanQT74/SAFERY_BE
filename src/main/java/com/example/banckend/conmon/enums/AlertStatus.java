package com.example.banckend.conmon.enums;

public enum AlertStatus {
    ALL,
    ACTIVE,    // Chưa đọc (acknowledged = false)
    RESOLVED   // Đã đọc (acknowledged = true)
}
