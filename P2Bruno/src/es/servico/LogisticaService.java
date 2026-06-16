package es.servico;

import es.modelo.PedidoVenda;

/**
 * Gerencia o fluxo de envio para a logística caso haja estoque (RF008 / RF009).
 */
public class LogisticaService {
    private String ultimoStatusEnvio;

    public void prepararEFiltrarEnvio(PedidoVenda pedido) {
        pedido.setStatus("CONCLUIDO");
        this.ultimoStatusEnvio = "PEDIDO_ENVIADO_LOGISTICA:" + pedido.getIdVenda();
    }

    public String getUltimoStatusEnvio() {
        return ultimoStatusEnvio;
    }
}