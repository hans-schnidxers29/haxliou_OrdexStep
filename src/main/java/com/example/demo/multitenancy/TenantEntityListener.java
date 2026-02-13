package com.example.demo.multitenancy;

import com.example.demo.entidad.Empresa;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.stereotype.Component;

/**
 * Entity Listener que automáticamente setea la empresa (tenant)
 * en entidades nuevas antes de persistirlas.
 */
@Component
public class TenantEntityListener {
    
    @PrePersist
    public void setTenantOnCreate(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            
            // Solo setear si no tiene empresa ya asignada
            if (tenantAware.getEmpresa() == null) {
                Long tenantId = TenantContext.getTenantId();
                
                if (tenantId != null) {
                    Empresa empresa = new Empresa();
                    empresa.setId(tenantId);
                    tenantAware.setEmpresa(empresa);
                    System.out.println("✅ Empresa auto-seteada en " + entity.getClass().getSimpleName() + ": " + tenantId);
                } else {
                    System.out.println("⚠️ No hay tenant en contexto para " + entity.getClass().getSimpleName());
                }
            }
        }
    }
    
    @PreUpdate
    public void preventTenantChange(Object entity) {
        if (entity instanceof TenantAware) {
            TenantAware tenantAware = (TenantAware) entity;
            Long currentTenantId = TenantContext.getTenantId();
            
            // Validar que no se cambie la empresa de una entidad existente
            if (tenantAware.getEmpresa() != null && currentTenantId != null) {
                if (!tenantAware.getEmpresa().getId().equals(currentTenantId)) {
                    throw new SecurityException(
                        "⛔ Intento de modificar entidad de otro tenant detectado! " +
                        "Tenant actual: " + currentTenantId + 
                        ", Tenant de entidad: " + tenantAware.getEmpresa().getId()
                    );
                }
            }
        }
    }
}
