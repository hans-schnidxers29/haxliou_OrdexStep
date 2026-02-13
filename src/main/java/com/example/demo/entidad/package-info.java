/**
 * Paquete de entidades del dominio.
 * Contiene la definición centralizada del filtro de tenant.
 */
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
package com.example.demo.entidad;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
