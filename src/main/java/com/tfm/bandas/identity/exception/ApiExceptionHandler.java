package com.tfm.bandas.identity.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, Object> handleInvalidPassword(InvalidPasswordException ex) {
        return errorBody("Petición Inválida", "INVALID_PASSWORD",
                "La contraseña no cumple los requisitos de seguridad: mínimo 8 caracteres, al menos una mayúscula, una minúscula y un número.");
    }

    @ExceptionHandler(UsernameExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, Object> handleUsernameExists(UsernameExistsException ex) {
        return errorBody("Conflicto de Datos", "USERNAME_EXISTS",
                "Ya existe un usuario con ese nombre de usuario o email en el proveedor de identidad.");
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleUserNotFound(UserNotFoundException ex) {
        return errorBody("No Encontrado", "USER_NOT_FOUND",
                "No se encontró el usuario en el proveedor de identidad.");
    }

    @ExceptionHandler(NotAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, Object> handleNotAuthorized(NotAuthorizedException ex) {
        return errorBody("No Autorizado", "NOT_AUTHORIZED",
                "No autorizado para realizar esta operación en el proveedor de identidad.");
    }

    // Catch-all para el resto de excepciones del SDK de Cognito no mapeadas explícitamente arriba.
    // Spring MVC despacha siempre al @ExceptionHandler de la clase más específica en la jerarquía
    // de la excepción lanzada, independientemente del orden en que los métodos aparezcan en este
    // fichero: el orden aquí es solo por legibilidad, no tiene efecto funcional.
    @ExceptionHandler(CognitoIdentityProviderException.class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    public Map<String, Object> handleCognitoGeneric(CognitoIdentityProviderException ex) {
        logger.error("Error no mapeado del proveedor de identidad (Cognito) — awsErrorDetails: {}",
                ex.awsErrorDetails(), ex);
        return errorBody("Error del Proveedor de Identidad", "IDENTITY_PROVIDER_ERROR",
                "El proveedor de identidad no está disponible en este momento. Inténtalo de nuevo más tarde.");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> handleUnexpected(Exception ex) {
        logger.error("Unhandled exception", ex);
        return errorBody("Error Interno", "INTERNAL_ERROR",
                "Ha ocurrido un error inesperado. Inténtalo de nuevo más tarde.");
    }

    private Map<String, Object> errorBody(String error, String errorCode, String message) {
        return Map.of("error", error, "errorCode", errorCode, "message", message);
    }
}
