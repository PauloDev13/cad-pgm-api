package br.gov.rn.natal.cadpgmapi.utils;

import lombok.Getter;

@Getter
public class EntityChangeEvent {
    private final Object entity;

    public EntityChangeEvent(final Object entity) {
        this.entity = entity;
    }
}
