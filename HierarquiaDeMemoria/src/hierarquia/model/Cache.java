package hierarquia.model;

import hierarquia.util.bits;

import java.util.ArrayList;
import java.util.List;

public class Cache extends Memoria {
    private static class LinhaCache {
        int tag;
        int[] dados;
        boolean dirty;
        int ultimoAcesso;

        LinhaCache(int tamanhoLinha) {
            this.dados = new int[tamanhoLinha];
            this.dirty = false;
            this.ultimoAcesso = 0;
        }
    }

    private final int associatividade;
    private final int tamanhoConjunto;
    private final int tamanhoLinha;
    private final String politicaEscrita;
    private static Integer tamanhoLinhaGlobal = null; // Tamanho da linha uniforme em todas as caches
    private int hits = 0;
    private int misses = 0;
    private LinhaCache[][] conjuntos;
    private Memoria memoriaInferior;
    private int contadorTempo = 0; // Para LRU
    private static final List<Cache> cachesCriadas = new ArrayList<>();

    public Cache(String nome, int latencia, int associatividade, int tamanhoConjunto, int tamanhoLinha, String politicaEscrita) {
        if (tamanhoLinhaGlobal == null) {
            tamanhoLinhaGlobal = tamanhoLinha; // Define o tamanho da linha global na primeira cache criada
        } else if (tamanhoLinha != tamanhoLinhaGlobal) {
            throw new IllegalArgumentException("Todas as caches devem ter o mesmo tamanho de linha: " + tamanhoLinhaGlobal + " bytes");
        }

        validarParametros(latencia, associatividade, tamanhoConjunto, politicaEscrita);

        this.nome = nome;
        this.latencia = latencia;
        this.associatividade = associatividade;
        this.tamanhoConjunto = tamanhoConjunto;
        this.tamanhoLinha = tamanhoLinha;
        this.politicaEscrita = politicaEscrita;

        // Inicializar os conjuntos como um array bidimensional
        this.conjuntos = new LinhaCache[tamanhoConjunto][associatividade];
        for (int i = 0; i < tamanhoConjunto; i++) {
            for (int j = 0; j < associatividade; j++) {
                this.conjuntos[i][j] = new LinhaCache(tamanhoLinha);
            }
        }

        cachesCriadas.add(this);
    }

    private void validarParametros(int latencia, int associatividade, int tamanhoConjunto, String politicaEscrita) {
        if (latencia <= 0) {
            throw new IllegalArgumentException("Latência deve ser maior que zero.");
        }
        if (associatividade <= 0) {
            throw new IllegalArgumentException("Associatividade deve ser maior que zero.");
        }
        if (tamanhoConjunto <= 0) {
            throw new IllegalArgumentException("Tamanho do conjunto deve ser maior que zero.");
        }
        if (!politicaEscrita.equalsIgnoreCase("write-back") && !politicaEscrita.equalsIgnoreCase("write-through")) {
            throw new IllegalArgumentException("Política de escrita deve ser 'write-back' ou 'write-through'.");
        }
    }

    public static void verificarConfiguracaoGlobal() {
        if (tamanhoLinhaGlobal == null) {
            throw new IllegalStateException("Nenhuma cache foi configurada na hierarquia de memória.");
        }
        System.out.println("Configuração Global de Tamanho de Linha: " + tamanhoLinhaGlobal + " bytes");
    }

    public void conectarMemoriaInferior(Memoria memoriaInferior) {
        this.memoriaInferior = memoriaInferior;
    }

    // Modificar a classe Cache para:
    @Override
    public int ler(int endereco) {
        quantidadeAcessos++;
        quantidadeLeituras++;

            // Calcula o número de bits necessários
            int bitsOffset = (int) (Math.log(tamanhoLinha) / Math.log(2));
            int bitsIndice = (int) (Math.log(tamanhoConjunto) / Math.log(2));
            System.out.println(bitsOffset + " "+ bitsIndice + " "+ endereco);

            // Calcula offset, índice e tag
            int offset = endereco & ((1 << bitsOffset) - 1); // Máscara para os bits do offset
            int index = (endereco >> bitsOffset) & ((1 << bitsIndice) - 1); // Máscara para os bits do índice
            int tag = bits.extract_bits(endereco, bitsOffset + bitsIndice, 32 - bitsOffset - bitsIndice);

        System.out.printf("Acessando endereço %d (Offset: %d, Index: %d, Tag: %d)%n", endereco, offset, index, tag);

        // Verifica se o dado está no cache (cache hit)
        for (LinhaCache linha : conjuntos[index]) {
            System.out.printf("Verificando linha - Tag atual: %d%n", linha.tag);
            if (linha.tag == tag) {
                hits++;
                linha.ultimoAcesso = contadorTempo++;
                System.out.println("Hit encontrado.");
                return linha.dados[offset];
            }
        }

        // Cache miss
        System.out.println("Miss! Endereço não encontrado no cache.");
        misses++;
        int dado = memoriaInferior.ler(endereco);
        substituirLinha(index, tag, endereco, dado);
        return latencia + dado;
    }




    @Override
    public void escrever(int endereco, int dado) {
        quantidadeAcessos++;
        quantidadeEscritas++;
        int offset = endereco % tamanhoLinha;
        int index = (endereco / tamanhoLinha) % tamanhoConjunto;
        int tag = endereco / (tamanhoLinha * tamanhoConjunto);

        for (LinhaCache linha : conjuntos[index]) {
            if (linha.tag == tag) {
                hits++;
                linha.dados[offset] = dado;
                linha.ultimoAcesso = contadorTempo++;
                if (politicaEscrita.equals("write-back")) {
                    linha.dirty = true;
                } else if (politicaEscrita.equals("write-through")) {
                    memoriaInferior.escrever(endereco, dado);
                }
                return;
            }
        }

        // Cache miss
        misses++;
        if (politicaEscrita.equals("write-through")) {
            memoriaInferior.escrever(endereco, dado);
        }
        substituirLinha(index, tag, endereco, dado);
    }

    private void substituirLinha(int index, int tag, int endereco, int dado) {
        LinhaCache[] conjunto = conjuntos[index];
        LinhaCache linhaSubstituir = conjunto[0];

        // LRU: Encontrar a linha menos recentemente usada
        for (LinhaCache linha : conjunto) {
            if (linha.ultimoAcesso < linhaSubstituir.ultimoAcesso) {
                linhaSubstituir = linha;
            }
        }

        // Write-back: Escrever a linha suja para a memória inferior
        if (linhaSubstituir.dirty) {
            int enderecoBase = (linhaSubstituir.tag * tamanhoConjunto + index) * tamanhoLinha;
            for (int i = 0; i < tamanhoLinha; i++) {
                memoriaInferior.escrever(enderecoBase + i, linhaSubstituir.dados[i]);
            }
        }

        // Substituir a linha na cache
        linhaSubstituir.tag = tag;
        linhaSubstituir.dados[endereco % tamanhoLinha] = dado;
        linhaSubstituir.dirty = politicaEscrita.equals("write-back");
        linhaSubstituir.ultimoAcesso = contadorTempo++;
    }


    public void imprimirEstatisticas() {
        super.imprimirEstatisticas();
        System.out.printf("Hits: %d, Misses: %d, Taxa de Miss: %.2f%%%n", hits, misses, (100.0 * misses) / (hits + misses));
        System.out.printf("Tamanho em KB: %.2f%n", TamanhoEmKB());
        System.out.println();
    }

    public double TamanhoEmKB() {
        int tamanhoEmBytes = tamanhoConjunto * associatividade * tamanhoLinha;
        return tamanhoEmBytes / 1024.0;
    }
}
