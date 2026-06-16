package es.modelo;

/**
 * Classe de modelo que representa um Pedido de Venda no sistema de gestão industrial.
 * Armazena as informações básicas da transação e o estado atual do processamento.
 * * @author Grupo de Engenharia de Software
 */
public class PedidoVenda {
    private int idVenda;
    private int idProduto;
    private int quantidade;
    private String status;

    /**
     * Construtor completo para a criação de uma instância de PedidoVenda.
     * * @param idVenda Identificador único da transação de venda.
     * @param idProduto Identificador do produto solicitado.
     * @param quantidade Volume de itens requisitados no pedido.
     * @param status Estado inicial do pedido (ex: "PENDENTE").
     */
    public PedidoVenda(int idVenda, int idProduto, int quantidade, String status) {
        this.idVenda = idVenda;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.status = status;
    }

    /**
     * Recupera o identificador único da venda.
     * * @return O ID da venda.
     */
    public int getIdVenda() { 
        return idVenda; 
    }

    /**
     * Recupera o identificador do produto associado ao pedido.
     * * @return O ID do produto.
     */
    public int getIdProduto() { 
        return idProduto; 
    }

    /**
     * Recupera a quantidade de itens solicitados no pedido.
     * * @return A quantidade total.
     */
    public int getQuantidade() { 
        return quantidade; 
    }

    /**
     * Recupera o status atual do processamento do pedido.
     * * @return Uma String contendo o status (ex: "CONCLUIDO", "SEM_ESTOQUE").
     */
    public String getStatus() { 
        return status; 
    }

    /**
     * Atualiza o status do pedido durante o ciclo de vida do fluxo de venda.
     * * <p><b>Observação:</b> Este método é invocado pela classe 
     * {@link es.controlador.FluxoPedidoFacade} (ou pacote correspondente) conforme 
     * as regras de negócio <b>RD002</b> são validadas [2, 3].</p>
     * * @param status O novo status a ser atribuído ao pedido.
     */
    public void setStatus(String status) { 
        this.status = status; 
    }
}