package org.allservice.exceptions.records;

import java.time.LocalDateTime;

public record StandardError(Integer status,
                            String error,
                            String message,
                            String path,
                            LocalDateTime timestamp) {
}
