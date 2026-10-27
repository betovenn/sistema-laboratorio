package com.uaemex.laboratorio.iam.dto;

import java.util.UUID;

import com.uaemex.laboratorio.iam.model.Usuario;

public record UsuarioResponse(UUID id, String cuenta, String rol) {
    /** Convierte la entidad en el DTO que se envía al cliente **/
    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
            usuario.getId(),
            usuario.getCuenta(), 
            usuario.getRol().getNombre()
        ); 
    }
}
