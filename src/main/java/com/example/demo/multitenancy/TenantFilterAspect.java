package com.example.demo.multitenancy;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspecto que habilita automáticamente el filtro de tenant
 * antes de ejecutar cualquier método de repositorio.
 */
@Aspect
@Component
public class TenantFilterAspect {
    
    @Autowired
    private EntityManager entityManager;
    
    @Before("execution(* com.example.demo.repositorio..*(..))")
    public void enableTenantFilter() {
        Long tenantId = TenantContext.getTenantId();
        
        if (tenantId != null) {
            Session session = entityManager.unwrap(Session.class);
            Filter filter = session.enableFilter("tenantFilter");
            filter.setParameter("tenantId", tenantId);
            System.out.println("🔍 Filtro de tenant habilitado para empresa: " + tenantId);
        } else {
            System.out.println("⚠️ No hay tenant en contexto, filtro no aplicado");
        }
    }
}
