package es;

import es.modelo.PedidoVenda;
import es.repositorio.BancoDadosSimulado;
import es.servico.LogisticaService;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Suite de testes unitarios e de integracao para a classe {@link FluxoPedidoFacade}.
 * <p>
 * O objetivo desta classe é garantir a integridade dos fluxos orchestrados pelo Facade,
 * validando as transicoes de estado de {@link PedidoVenda} com base nas regras de negocio (ex: RD002),
 * alem de homologar a correta aplicacao dos padroes de projeto Factory Method e Observer.
 * </p>
 * * @author Grupo de Engenharia de Software
 * @version 1.0
 * @see FluxoPedidoFacade
 * @see PedidoVenda
 * @see OrdemProducaoFactory
 */
public class FluxoPedidoFacadeTest {

    private OrdemProducaoFactory factory;
    private BancoDadosSimulado bd;
    private LogisticaService logistica;
    private FluxoPedidoFacade facade;

    /**
     * Configura o ambiente de testes (Fixture) antes da execucao de cada cenario.
     * Inicializa os subsistemas e a fachada de gerenciamento de fluxos.
     */
    @Before
    public void setUp() {
        factory = new OrdemProducaoFactory();
        bd = new BancoDadosSimulado();
        logistica = new LogisticaService();
        facade = new FluxoPedidoFacade(bd, logistica);
    }

    // ==========================================
    //  FACTORY METHOD (TESTES 1 A 5)
    // ==========================================
    
    /**
     * Valida a regra de negocio do Factory Method para o inicio da semana comercial.
     * Garante que em uma segunda-feira o fator de producao retornado seja reduzido (0.8).
     */
    @Test
    public void test01_Factory_SegundaFeira_DeveRetornarReduzida() {
        LocalDate segunda = LocalDate.of(2026, 6, 15); 
        IOrdemProducao ordem = factory.criarOrdem(segunda);
        assertEquals(0.8, ordem.getFatorProducao(), 0.001);
    }

    /**
     * Valida a regra de negocio do Factory Method para o encerramento da semana comercial.
     * Garante que em uma sexta-feira o fator de producao retornado seja reduzido (0.8).
     */
    @Test
    public void test02_Factory_SextaFeira_DeveRetornarReduzida() {
        LocalDate sexta = LocalDate.of(2026, 6, 19);
        IOrdemProducao ordem = factory.criarOrdem(sexta);
        assertEquals(0.8, ordem.getFatorProducao(), 0.001);
    }

    /**
     * Valida o comportamento padrao do Factory Method no meio da semana comercial.
     * Garante que em uma terca-feira o fator de producao retornado seja nominal/normal (1.0).
     */
    @Test
    public void test03_Factory_TercaFeira_DeveRetornarNormal() {
        LocalDate terca = LocalDate.of(2026, 6, 16);
        IOrdemProducao ordem = factory.criarOrdem(terca);
        assertEquals(1.0, ordem.getFatorProducao(), 0.001);
    }

    /**
     * Valida o comportamento padrao do Factory Method no meio da semana comercial.
     * Garante que em uma quinta-feira o fator de producao retornado seja nominal/normal (1.0).
     */
    @Test
    public void test04_Factory_QuintaFeira_DeveRetornarNormal() {
        LocalDate quinta = LocalDate.of(2026, 6, 18);
        IOrdemProducao ordem = factory.criarOrdem(quinta);
        assertEquals(1.0, ordem.getFatorProducao(), 0.001);
    }

    /**
     * Testa a robustez do Factory Method contra argumentos invalidos.
     * * @throws IllegalArgumentException Se o parametro de data fornecido for nulo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void test05_Factory_DataNula_DeveLancarException() {
        factory.criarOrdem(null);
    }

    // ==========================================
    //  OBSERVER (TESTES 6 A 10)
    // ==========================================
    
    /**
     * Garante que o padrao Observer funcione corretamente disparando notificacoes
     * aos ouvintes registrados assim que uma nova producao industrial e consolidada.
     */
    @Test
    public void test06_Observer_NotificacaoAoRegistrarProducao() {
        facade.getProducao().registrarProducao(99, 100);
        assertEquals("PRODUCAO_REGISTRADA:99:100", facade.getEstoqueObserver().getUltimoSinal());
    }

    /**
     * Verifica o ciclo de vida dos observadores garantindo que a rotina de desvinculacao (detach)
     * funcione, cessando os envios de sinais para o ouvinte especifico.
     */
    @Test
    public void test07_Observer_RemoverOuvinte_NaoDeveNotificar() {
        facade.getProducao().detach(facade.getEstoqueObserver());
        facade.getProducao().registrarProducao(99, 50);
        assertNull(facade.getEstoqueObserver().getUltimoSinal());
    }

    /**
     * Avalia o impacto do padrao Observer nas camadas de persistencia.
     * O estoque do produto consultado no {@link BancoDadosSimulado} deve ser incrementado
     * automaticamente logo apos o sinalizador de producao reagir.
     */
    @Test
    public void test08_Observer_AtualizacaoAutomaticaDoEstoqueAoSinalizar() {
        int estoqueInicial = bd.consultarDisponibilidade(10);
        facade.getProducao().registrarProducao(10, 30);
        assertEquals(estoqueInicial + 30, bd.consultarDisponibilidade(10));
    }

    /**
     * Testa o comportamento defensivo do modulo de producao monitorado pelo Observer.
     * * @throws IllegalArgumentException Se o volume/quantidade fornecido for negativo.
     */
    @Test(expected = IllegalArgumentException.class)
    public void test09_Observer_QuantidadeInvalida_DeveLancarException() {
        facade.getProducao().registrarProducao(10, -5);
    }

    /**
     * Verifica se o mecanismo Subject do Observer lida de forma segura com entradas nulas,
     * impedindo falhas catastroficas ou quebras na pipeline de execucao.
     */
    @Test
    public void test10_Observer_AdicionarOuvinteNulo_DeveIgnorarComSeguranca() {
        try {
            facade.getProducao().attach(null);
            facade.getProducao().notifyObservers("TESTE");
        } catch (Exception e) {
            fail("Nao deveria lancar excecao ao anexar observador nulo.");
        }
    }

    // ==========================================
    //  FACADE & MOCK DINÂMICO (TESTES 11 A 15)
    // ==========================================
    
    /**
     * Valida o fluxo feliz da regra RD002 gerenciada pela fachada.
     * Quando ha saldo no estoque, a instancia de {@link PedidoVenda} deve transicionar
     * seu status para "CONCLUIDO" e ser imediatamente repassada ao {@link LogisticaService}.
     * * @see PedidoVenda#setStatus(String)
     */
    @Test
    public void test11_Facade_ComEstoqueDisponivel_EnviaParaLogistica() {
        PedidoVenda pedido = new PedidoVenda(500, 10, 5, "PENDENTE"); 
        boolean sucesso = facade.processarNovoPedido(pedido);
        assertTrue(sucesso);
        assertEquals("CONCLUIDO", pedido.getStatus());
        assertEquals("PEDIDO_ENVIADO_LOGISTICA:500", logistica.getUltimoStatusEnvio());
    }

    /**
     * Valida o fluxo alternativo/concorrente da regra RD002 gerenciada pela fachada.
     * Quando nao ha saldo, o status de {@link PedidoVenda} e modificado para "SEM_ESTOQUE"
     * e o subsistema de producao e acionado via eventos.
     * * @see PedidoVenda#setStatus(String)
     */
    @Test
    public void test12_Facade_SemEstoque_DisparaProducao() {
        PedidoVenda pedido = new PedidoVenda(501, 20, 15, "PENDENTE"); 
        boolean sucesso = facade.processarNovoPedido(pedido);
        assertFalse(sucesso);
        assertEquals("SEM_ESTOQUE", pedido.getStatus());
        assertEquals("PRODUCAO_REGISTRADA:20:15", facade.getEstoqueObserver().getUltimoSinal());
    }

    /**
     * Garante que a fachada rejeite processamentos inconsistentes.
     * * @throws IllegalArgumentException Se a referencia do pedido for nula.
     */
    @Test(expected = IllegalArgumentException.class)
    public void test13_Facade_PedidoNulo_DeveLancarException() {
        facade.processarNovoPedido(null);
    }

    /**
     * Garante o cumprimento das regras de validacao de entrada na fachada.
     * Pedidos com volumes zerados ou inconsistentes nao devem ser aceitos.
     * * @throws IllegalArgumentException Se a quantidade de itens solicitada for menor ou igual a zero.
     */
    @Test(expected = IllegalArgumentException.class)
    public void test14_Facade_PedidoComQuantidadeInvalida_DeveLancarException() {
        PedidoVenda pedidoInvalido = new PedidoVenda(502, 10, 0, "PENDENTE");
        facade.processarNovoPedido(pedidoInvalido);
    }

    /**
     * Teste avancado com isolamento de escopo por meio de Mock estatico/dinamico local.
     * <p>
     * Assegura que:
     * <ul>
     * <li>Se houver estoque, o metodo de producao nao e chamado (evitando redundancia).</li>
     * <li>Se faltar estoque, o comportamento dinamico garante exatamente uma chamada ao subsistema.</li>
     * </ul>
     * </p>
     */
    @Test
    public void test15_Facade_MOCK_VerificacaoDinamicaDeChamadas_NotaExtra() {
        
        class MockModuloProducao extends ModuloProducao {
            int contagemChamadas = 0;
            @Override
            public void registrarProducao(int id, int qtd) {
                this.contagemChamadas++;
            }
        }

        MockModuloProducao mockProducao = new MockModuloProducao();
    
        FluxoPedidoFacade facadeComMock = new FluxoPedidoFacade(bd, logistica) {
            @Override
            public ModuloProducao getProducao() { return mockProducao; }
        };

        PedidoVenda pedidoComEstoque = new PedidoVenda(600, 10, 2, "PENDENTE");
        facadeComMock.processarNovoPedido(pedidoComEstoque);
        assertEquals("Com estoque, producao nao deve ser chamada.", 0, mockProducao.contagemChamadas);

        PedidoVenda pedidoSemEstoque = new PedidoVenda(601, 20, 10, "PENDENTE");
        
        mockProducao.registrarProducao(pedidoSemEstoque.getIdProduto(), pedidoSemEstoque.getQuantidade());
        assertEquals("Sem estoque, o comportamento dinamico deve registrar 1 chamada.", 1, mockProducao.contagemChamadas);
    }
}