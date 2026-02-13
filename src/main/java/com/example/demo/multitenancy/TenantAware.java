package com.example.demo.multitenancy;

import com.example.demo.entidad.Empresa;

/**
 * Interface marker para entidades que pertenecen a un tenant (empresa).
 * Las entidades que implementen esta interface tendrán la empresa
 * seteada automáticamente al persistir.
 */
public interface TenantAware {
    
    Empresa getEmpresa();
    
    void setEmpresa(Empresa empresa);
}
