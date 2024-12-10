package hierarquia.model;

import java.util.Random;

public class Processador {
    private String padraoAcesso;
    private int stride;
    private int buffer;
    private int quantidadeAcessos;

    public Processador(String padraoAcesso, int stride, int buffer, int quantidadeAcessos) {
        this.padraoAcesso = padraoAcesso;
        this.stride = stride;
        this.buffer = buffer;
        this.quantidadeAcessos = quantidadeAcessos;
    }

    public void executar(Memoria primeiroNivel) {
        int tempoTotal = 0;
        for (int i = 0; i < quantidadeAcessos; i++) {
            int endereco = gerarEndereco(i);
            int latencia = primeiroNivel.ler(endereco);
            tempoTotal += latencia;
        }
        System.out.println("Tempo total de execução: " + tempoTotal + " ciclos\n");
    }

    private int gerarEndereco(int i) {
        if (padraoAcesso.equals("sequencial")) {
            return (i * stride) % buffer;
        } else if (padraoAcesso.equals("aleatorio")) {
            return new Random().nextInt(buffer);
        }
        return 0;
    }
}
