package com.example.demo.servicio;

import com.example.demo.entidad.AbonosCompra;
import com.example.demo.entidad.Compras;
import com.example.demo.entidad.ComprasCreditos;
import com.example.demo.entidad.Enum.EstadoPedido;
import com.example.demo.entidad.Enum.MetodoPago;
import com.example.demo.entidad.Pagos;
import com.example.demo.repositorio.ComprasCreditoRepositorio;
import com.example.demo.repositorio.ComprasRepositorio;
import com.example.demo.repositorio.FinanzasRepositorio;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FinanzasServicioImp  implements FinanzasServicio{

    @Autowired
    private ComprasCreditoRepositorio comprasCreditoRepo;

    @Autowired
    private FinanzasRepositorio abonoRepo;

    @Override
    @Transactional
    public void ProcesarAbono(Long cuentaId, BigDecimal monto, MetodoPago metodoPago,
                              boolean afectoCaja,BigDecimal montoEfec, BigDecimal montoTrns) {

        ComprasCreditos cuentaPagar = comprasCreditoRepo.findById(cuentaId)
                .orElseThrow(() -> new RuntimeException("No se encontró la deuda"));

        if (!cuentaPagar.getEstadoDeuda().equals(EstadoPedido.PENDIENTE) ||
                !cuentaPagar.getCompra().getMetodoPago().equals(MetodoPago.CREDITO)) {
            throw new RuntimeException("La cuenta no está pendiente o no es una compra a crédito");
        }

        BigDecimal saldoActual = cuentaPagar.getSaldoPendiente();
        if (monto.compareTo(saldoActual) > 0) {
            throw new RuntimeException("El abono ($" + monto + ") no puede ser mayor al saldo pendiente ($" + saldoActual + ")");
        }

        // 3. Validar monto positivo
        if (monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("El monto del abono debe ser mayor a cero");
        }

        // 4. Crear el registro del abono
        AbonosCompra nuevoAbono = new AbonosCompra();
        if(MetodoPago.MIXTO.equals(metodoPago)){
            BigDecimal montoProcesar = montoEfec.add(montoTrns);
            if(montoProcesar.compareTo(monto) != 0){
                throw new RuntimeException("El abono ($" + monto + ") no puede ser mayor al total ($" + montoProcesar + ")");
            }else{
                nuevoAbono.addPago("EFECTIVO", montoEfec);
                nuevoAbono.addPago("TRANFERENCIA", montoTrns);
            }

        }else{
            nuevoAbono.addPago(metodoPago.name().toUpperCase(),monto);
        }
        nuevoAbono.setCuentaPorPagar(cuentaPagar);
        nuevoAbono.setMontoAbonado(monto);
        nuevoAbono.setMetodoPago(metodoPago);
        nuevoAbono.setFechaAbono(java.time.LocalDateTime.now());
        if(afectoCaja){
            nuevoAbono.setSalioCaja(true);
        }
        abonoRepo.save(nuevoAbono);

        // 5. Actualizar saldo y cambiar estado si llega a cero
        BigDecimal nuevoSaldo = saldoActual.subtract(monto);
        cuentaPagar.setSaldoPendiente(nuevoSaldo);

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            cuentaPagar.setEstadoDeuda(EstadoPedido.CONFIRMADO);
        }
        comprasCreditoRepo.save(cuentaPagar);
    }
}
