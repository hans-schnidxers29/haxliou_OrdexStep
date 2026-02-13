package com.example.demo.multitenancy;

import com.example.demo.Seguridad.SecurityService;
import com.example.demo.entidad.Empresa;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que captura el tenant (empresa) del usuario autenticado
 * y lo establece en el TenantContext para cada request.
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {
    
    @Autowired
    private SecurityService securityService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        // 1. LIMPIEZA PREVENTIVA: Asegurar que el hilo no tenga basura de requests anteriores
        TenantContext.clear();
        
        try {
            // 2. intente recuperar de la sesión primero (Caché)
            HttpSession session = request.getSession(false);
            if (session != null) {
                Long cachedTenantId = (Long) session.getAttribute("TENANT_ID");
                if (cachedTenantId != null) {
                    TenantContext.setTenantId(cachedTenantId);
                    // System.out.println("⚡ Tenant recuperado de sesión: " + cachedTenantId);
                    return true;
                }
            }

            // 3. Fallback a BD solo si es necesario
            if (securityService.estaAutenticado()) {
                 Empresa empresa = securityService.ObtenerEmpresa();
                if (empresa != null && empresa.getId() != null) {
                    TenantContext.setTenantId(empresa.getId());
                    
                    // Guardar en sesión para el futuro
                    if (session != null) {
                        session.setAttribute("TENANT_ID", empresa.getId());
                    }
                    
                    System.out.println("🏢 Tenant establecido (DB): " + empresa.getId() + " - " + empresa.getNombre());
                }
            }
        } catch (Exception e) {
            // Log silencioso: No bloquear el request si falla la obtención del tenant
            // System.out.println("ℹ️ Request sin contexto de tenant: " + e.getMessage());
        }
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) throws Exception {
        // CRÍTICO: Limpiar el tenant después de cada request
        TenantContext.clear();
    }
}
