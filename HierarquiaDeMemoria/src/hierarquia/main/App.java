package hierarquia.main;

import hierarquia.model.*;

public class App {
    public static void main(String[] args) {
        MemoriaPrincipal memoriaPrincipal = new MemoriaPrincipal("Memória Principal", 100);
        Cache cacheL1 = new Cache("Cache L1", 10, 1, 64, 16, "write-back");
        Cache cacheL2 = new Cache("Cache L2", 20, 4, 256, 16, "write-back");

        cacheL2.conectarMemoriaInferior(memoriaPrincipal);
        cacheL1.conectarMemoriaInferior(cacheL2);

        Processador processador = new Processador("sequencial", 4, 300, 9000);

        processador.executar(cacheL1);

        cacheL1.imprimirEstatisticas();
        cacheL2.imprimirEstatisticas();
        memoriaPrincipal.imprimirEstatisticas();
    }
}
