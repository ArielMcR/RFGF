// File: hierarquia/model/MemoriaPrincipal.java
package hierarquia.model;

public class MemoriaPrincipal extends Memoria {
    public MemoriaPrincipal(String nome, int latencia) {
        this.nome = nome;
        this.latencia = latencia;
    }

    @Override
    public int ler(int endereco) {
        quantidadeAcessos++;
        quantidadeLeituras++;
        return latencia;
    }

    @Override
    public void escrever(int endereco, int dado) {
        quantidadeAcessos++;
        quantidadeEscritas++;
        //dados.put(endereco, dado);
    }
}
