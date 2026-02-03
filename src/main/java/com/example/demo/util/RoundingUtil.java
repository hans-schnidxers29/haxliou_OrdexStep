package com.example.demo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class RoundingUtil {

    /**
     * Redondea un valor al peso colombiano más cercano (sin decimales).
     * Por ejemplo, 4.99 -> 5.00, 4.49 -> 4.00 (si se usa HALF_UP).
     * El usuario especificó que 4.99 debe redondearse a 5.
     *
     * @param value El valor a redondear.
     * @return El valor redondeado a 0 decimales, con escala 2 para mantener consistencia.
     */
    public static BigDecimal roundToColombianPeso(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(0, RoundingMode.HALF_UP).setScale(2, RoundingMode.HALF_UP);
    }
}
