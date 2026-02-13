package com.example.demo.multitenancy;

/**
 * Contexto de Tenant basado en ThreadLocal.
 * Almacena el ID de la empresa (tenant) para el thread actual.
 */
public class TenantContext {
    
    private static final ThreadLocal<Long> currentTenant = new ThreadLocal<>();
    
    /**
     * Establece el ID del tenant para el thread actual
     */
    public static void setTenantId(Long tenantId) {
        currentTenant.set(tenantId);
    }
    
    /**
     * Obtiene el ID del tenant del thread actual
     */
    public static Long getTenantId() {
        return currentTenant.get();
    }
    
    /**
     * Limpia el tenant del thread actual
     * IMPORTANTE: Siempre llamar en finally o afterCompletion
     */
    public static void clear() {
        currentTenant.remove();
    }
}
