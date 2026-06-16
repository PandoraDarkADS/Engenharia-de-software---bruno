package es.repositorio;

import java.util.HashMap;
import java.util.Map;

/**
 * Simula o controle de estoque em memória (RF004 / RF005).
 */
public class BancoDadosSimulado {
    private final Map<Integer, Integer> estoque = new HashMap<>();

    public BancoDadosSimulado() {
    
        estoque.put(10, 50);  
        estoque.put(20, 0);  
    }

    public int consultarDisponibilidade(int idProduto) {
        return estoque.getOrDefault(idProduto, 0);
    }

    public void atualizarEstoque(int idProduto, int quantidade) {
        int atual = consultarDisponibilidade(idProduto);
        estoque.put(idProduto, atual + quantidade);
    }
}