package br.gov.rn.natal.cadpgmapi.exception;

import java.time.Instant;

public record StandardError(
        Instant timestamp, Integer status, String error, String message, String path
) { }
