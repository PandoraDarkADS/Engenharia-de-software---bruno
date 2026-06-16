package es;

import es.modelo.PedidoVenda;
import es.repositorio.BancoDadosSimulado;
import es.servico.LogisticaService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// ==========================================
  //  PADRÃO CRIACIONAL: FACTORY METHOD
// ==========================================
interface IOrdemProducao {
    double getFatorProducao();
}

class OrdemProducaoNormal implements IOrdemProducao {
    @Override
    public double getFatorProducao() { return 1.0; }
}

class OrdemProducaoReduzida implements IOrdemProducao {
    @Override
    public double getFatorProducao() { return 0.8; } // Redução de 20% (RD001)
}

class OrdemProducaoFactory {
    public IOrdemProducao criarOrdem(LocalDate data) {
        if (data == null) throw new IllegalArgumentException("Data invalida.");
        DayOfWeek dia = data.getDayOfWeek();
        if (dia == DayOfWeek.MONDAY || dia == DayOfWeek.FRIDAY) {
            return new OrdemProducaoReduzida();
        }
        return new OrdemProducaoNormal();
    }
}

// ==========================================
    //PADRÃO COMPORTAMENTAL: OBSERVER
// ==========================================
interface IObserver {
    void update(String dados);
}

interface ISubject {
    void attach(IObserver obs);
    void detach(IObserver obs);
    void notifyObservers(String dados);
}

class ModuloProducao implements ISubject {
    private final List<IObserver> observadores = new ArrayList<>();

    @Override
    public void attach(IObserver obs) { if (obs != null && !observadores.contains(obs)) observadores.add(obs); }
    @Override
    public void detach(IObserver obs) { observadores.remove(obs); }
    @Override
    public void notifyObservers(String dados) { for (IObserver obs : observadores) obs.update(dados); }

    public void registrarProducao(int idProduto, int quantidade) {
        if (quantidade <= 0) throw new IllegalArgumentException("Quantidade invalida.");
        notifyObservers("PRODUCAO_REGISTRADA:" + idProduto + ":" + quantidade);
    }
}

class ModuloEstoqueObserver implements IObserver {
    private final BancoDadosSimulado bd;
    private String ultimoSinal;

    public ModuloEstoqueObserver(BancoDadosSimulado bd) { this.bd = bd; }

    @Override
    public void update(String dados) {
        this.ultimoSinal = dados;
        if (dados.startsWith("PRODUCAO_REGISTRADA:")) {
            String[] partes = dados.split(":");
            int idProd = Integer.parseInt(partes[1]);
            int qtd = Integer.parseInt(partes[2]);
            bd.atualizarEstoque(idProd, qtd); // Atualiza o estoque automaticamente (RD004)
        }
    }
    public String getUltimoSinal() { return ultimoSinal; }
}

// ==========================================
  // PADRÃO ESTRUTURAL: FACADE
// ==========================================
public class FluxoPedidoFacade {
    private final BancoDadosSimulado bd;
    private final LogisticaService logistica;
    private final ModuloProducao producao;
    private final ModuloEstoqueObserver estoqueObserver;

    public FluxoPedidoFacade(BancoDadosSimulado bd, LogisticaService logistica) {
        this.bd = bd;
        this.logistica = logistica;
        this.producao = new ModuloProducao();
        this.estoqueObserver = new ModuloEstoqueObserver(bd);
        this.producao.attach(estoqueObserver);
    }

    public boolean processarNovoPedido(PedidoVenda pedido) {
        if (pedido == null) throw new IllegalArgumentException("Pedido nulo.");
        if (pedido.getQuantidade() <= 0) throw new IllegalArgumentException("Quantidade zerada.");

        int qtdDisponivel = bd.consultarDisponibilidade(pedido.getIdProduto());

        if (qtdDisponivel >= pedido.getQuantidade()) {
          
            bd.atualizarEstoque(pedido.getIdProduto(), -pedido.getQuantidade());
            logistica.prepararEFiltrarEnvio(pedido);
            return true;
        } else {
   
            pedido.setStatus("SEM_ESTOQUE");
            producao.registrarProducao(pedido.getIdProduto(), pedido.getQuantidade());
            return false;
        }
    }

    public ModuloProducao getProducao() { return producao; }
    public ModuloEstoqueObserver getEstoqueObserver() { return estoqueObserver; }
}