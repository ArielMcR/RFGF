    // File: hierarquia/model/Memoria.java
    package hierarquia.model;

    public abstract class Memoria {
        protected String nome;
        protected int latencia;
        protected int quantidadeAcessos = 0;
        protected int quantidadeLeituras = 0;
        protected int quantidadeEscritas = 0;

        public abstract int ler(int endereco);
        public abstract void escrever(int endereco, int dado);

        public int obterLatencia() {
            return latencia;
        }

        public void imprimirEstatisticas() {
            System.out.println("Memória: " + nome);
            System.out.println("Acessos: " + quantidadeAcessos);
            System.out.println("Leituras: " + quantidadeLeituras);
            System.out.println("Escritas: " + quantidadeEscritas);
        }
    }
